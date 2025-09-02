package com.example.daydisplay.client;

import com.example.daydisplay.forge.config.DayDisplayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-side HUD renderer that displays the current in-game day.
 * Registered on the client event bus by the mod initializer.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class ClientHudHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static int lastLoggedDay = -1;
    // (Legacy flag removed; integration handled by SeasonIntegrationHelper)

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent event) {
        try {
            renderOverlay(event);
        } catch (Throwable t) {
            LOGGER.error("DayDisplay HUD rendering failed (caught):", t);
        }
    }

    private static void renderOverlay(RenderGuiOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        Level world = mc.level;
        if (world == null) return; // only render when in a world

        GuiGraphics gui = event.getGuiGraphics();
        if (gui == null) return;

        // Dia atual (Day 1 começa em dayTime 0). Usamos getDayTime para consistência por dimensão.
        long absoluteDay = (world.getDayTime() / 24000L) + 1; // Evita +1 por tick mosso de getGameTime global
        String dayText = "Day " + absoluteDay;

        long dayTime = world.getDayTime() % 24000L; // 0..23999, 0=06:00
        int hour24 = (int)((dayTime / 1000L + 6) % 24);
        int minute = (int)((dayTime % 1000L) * 60L / 1000L);
        boolean use24 = DayDisplayConfig.CLIENT.use24h.get();
        String timeText;
        if (use24) {
            timeText = String.format("%02d:%02d", hour24, minute);
        } else {
            int h12 = hour24 % 12; if (h12 == 0) h12 = 12;
            String ampm = hour24 < 12 ? "AM" : "PM";
            timeText = String.format("%d:%02d %s", h12, minute, ampm);
        }

        // log when the day changes (helps debug without GUI)
        if (absoluteDay != lastLoggedDay) {
            lastLoggedDay = (int) absoluteDay;
            LOGGER.info("DayDisplay HUD rendering: Day {}", absoluteDay);
        }
        // Positioning and color from config
    int baseX = DayDisplayConfig.CLIENT.baseX.get();
    int baseY = DayDisplayConfig.CLIENT.baseY.get();
    final int shadowColor = DayDisplayConfig.CLIENT.shadowColor.get();

        try {
            // draw day then time below it
            Component dayComp = Component.literal(dayText);
            Component timeComp = Component.literal(timeText);

            if (DayDisplayConfig.CLIENT.dayEnabled.get())
                drawLine(gui, mc, dayComp,
                        baseX + DayDisplayConfig.CLIENT.dayX.get(), baseY + DayDisplayConfig.CLIENT.dayY.get(),
                        DayDisplayConfig.CLIENT.dayColor.get(), shadowColor,
                        DayDisplayConfig.CLIENT.shadowEnabled.get(),
                        DayDisplayConfig.CLIENT.dayShadowOffsetX.get(), DayDisplayConfig.CLIENT.dayShadowOffsetY.get(),
                        DayDisplayConfig.CLIENT.dayShadowColor.get(),
                        DayDisplayConfig.CLIENT.dayScaleMilli.get() / 1000f);
            if (DayDisplayConfig.CLIENT.timeEnabled.get())
                drawLine(gui, mc, timeComp,
                        baseX + DayDisplayConfig.CLIENT.timeX.get(), baseY + DayDisplayConfig.CLIENT.timeY.get(),
                        DayDisplayConfig.CLIENT.timeColor.get(), shadowColor,
                        DayDisplayConfig.CLIENT.shadowEnabled.get(),
                        DayDisplayConfig.CLIENT.timeShadowOffsetX.get(), DayDisplayConfig.CLIENT.timeShadowOffsetY.get(),
                        DayDisplayConfig.CLIENT.timeShadowColor.get(),
                        DayDisplayConfig.CLIENT.timeScaleMilli.get() / 1000f);

            try {
                String season = getSereneSeason(world);
                if (season != null) {
                    if (DayDisplayConfig.CLIENT.iconEnabled.get()) {
                        drawSeasonIcon(gui, baseX + DayDisplayConfig.CLIENT.iconX.get(), baseY + DayDisplayConfig.CLIENT.iconY.get(), season, DayDisplayConfig.CLIENT.iconSize.get());
                    }
                    if (DayDisplayConfig.CLIENT.seasonEnabled.get()) {
                        Component seasonComp = Component.literal(season);
                        drawLine(gui, mc, seasonComp,
                                baseX + DayDisplayConfig.CLIENT.seasonX.get(), baseY + DayDisplayConfig.CLIENT.seasonY.get(),
                                DayDisplayConfig.CLIENT.seasonColor.get(), shadowColor,
                                DayDisplayConfig.CLIENT.shadowEnabled.get(),
                                DayDisplayConfig.CLIENT.seasonShadowOffsetX.get(), DayDisplayConfig.CLIENT.seasonShadowOffsetY.get(),
                                DayDisplayConfig.CLIENT.seasonShadowColor.get(),
                                DayDisplayConfig.CLIENT.seasonScaleMilli.get() / 1000f);
                    }
                }
            } catch (Throwable t) {
                LOGGER.debug("Serene Seasons integration failed (non-fatal)", t);
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to draw DayDisplay HUD text:", t);
        }
    }

    /**
     * Attempt to obtain a human-friendly season string from Serene Seasons.
     * Uses reflection and ModList so the mod is optional and will not crash when absent.
     */
    private static String getSereneSeason(Level world) {
        return SeasonIntegrationHelper.getSeasonDisplay(world);
    }
    // --- Helper interno isolando integração ---
    private static final class SeasonIntegrationHelper {
        private static boolean initTried = false;
        private static boolean enabled = false;
        private static java.lang.reflect.Method getSeasonStateMethod; // SeasonHelper.getSeasonState(Level)
        private static java.lang.reflect.Method getSubSeasonMethod;   // ISeasonState.getSubSeason()
        private static java.lang.reflect.Method getSeasonMethod;     // ISeasonState.getSeason()
        private static java.lang.reflect.Method getTropicalSeasonMethod; // ISeasonState.getTropicalSeason()
        private static java.lang.reflect.Method usesTropicalMethod;      // SeasonHelper.usesTropicalSeasons(Holder<Biome>)
        private static long lastFetchTick = Long.MIN_VALUE;
        private static String cachedDisplay = null;

        private static String getSeasonDisplay(Level level) {
            if (level == null) return null;
            // 1) Initialize reflection once
            if (!initTried) {
                init();
            }
            if (!enabled) return null;

            long now = level.getGameTime();
            // Throttle: update at most once per second (20 ticks)
            int interval = DayDisplayConfig.CLIENT.seasonRefreshInterval.get();
            if (now - lastFetchTick < interval && cachedDisplay != null) {
                return cachedDisplay;
            }

            lastFetchTick = now;
            try {
                Object seasonState = getSeasonStateMethod.invoke(null, level);
                if (seasonState == null) return null;

                // Prefer sub-season for richer detail (e.g., EARLY_SPRING)
                Object subSeason = null;
                if (getSubSeasonMethod != null) {
                    try { subSeason = getSubSeasonMethod.invoke(seasonState); } catch (Throwable ignored) {}
                }
                String display = null;

                // Detect tropical biome usage (player biome) and show tropical sub-season if applicable
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.player != null && usesTropicalMethod != null && getTropicalSeasonMethod != null) {
                    try {
                        Object biomeHolder = mc.level.getBiome(mc.player.blockPosition()); // Holder<Biome>
                        boolean tropical = (boolean) usesTropicalMethod.invoke(null, biomeHolder);
                        if (tropical) {
                            Object tropicalSeason = getTropicalSeasonMethod.invoke(seasonState);
                            if (tropicalSeason != null) {
                                display = translateTropicalSubSeason(tropicalSeason.toString());
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                if (display == null) {
                    if (subSeason != null) {
                        display = translateSubSeason(subSeason.toString());
                    } else {
                        Object season = getSeasonMethod != null ? getSeasonMethod.invoke(seasonState) : null;
                        display = season != null ? translateSeason(season.toString()) : null;
                    }
                }
                cachedDisplay = display;
                return display;
            } catch (Throwable t) {
                LOGGER.debug("SereneSeasons season fetch failed (will disable until restart)", t);
                enabled = false; // avoid spamming
                return null;
            }
        }

        private static void init() {
            initTried = true;
            try {
                if (!ModList.get().isLoaded("sereneseasons")) {
                    LOGGER.info("SereneSeasons not present - skipping season integration");
                    enabled = false;
                    return;
                }
                // Correct fully-qualified class name from repo: sereneseasons.api.season.SeasonHelper
                Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
                getSeasonStateMethod = helper.getMethod("getSeasonState", Level.class);
                // Also attempt to resolve usesTropicalSeasons(LevelHolder) method
                try { usesTropicalMethod = helper.getMethod("usesTropicalSeasons", net.minecraft.core.Holder.class); } catch (NoSuchMethodException ignored) {}
                // Obtain returned type via first invocation later (can't load internal types safely beforehand)
                // We'll lazily resolve sub-season methods after first successful call.
                enabled = true;
                LOGGER.info("SereneSeasons detected - season HUD enabled");

                // Attempt a dry run with client world if available to resolve methods
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.level != null) {
                    tryResolveStateMethods(mc.level);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.info("SereneSeasons class not found (API path mismatch?) - disabling integration");
                enabled = false;
            } catch (Throwable t) {
                LOGGER.warn("Failed initializing SereneSeasons integration - disabling", t);
                enabled = false;
            }
        }

        private static void tryResolveStateMethods(Level level) {
            if (!enabled) return;
            try {
                Object state = getSeasonStateMethod.invoke(null, level);
                if (state != null) {
                    Class<?> stateCls = state.getClass();
                    try { getSubSeasonMethod = stateCls.getMethod("getSubSeason"); } catch (NoSuchMethodException ignored) {}
                    try { getSeasonMethod = stateCls.getMethod("getSeason"); } catch (NoSuchMethodException ignored) {}
                    try { getTropicalSeasonMethod = stateCls.getMethod("getTropicalSeason"); } catch (NoSuchMethodException ignored) {}
                }
            } catch (Throwable ignored) {
            }
        }

        private static String translateSeason(String s) {
            if (s == null) return null;
            return switch (s.toUpperCase()) {
                case "SPRING" -> "Primavera";
                case "SUMMER" -> "Verão";
                case "AUTUMN", "FALL" -> "Outono";
                case "WINTER" -> "Inverno";
                default -> s;
            };
        }

        private static String translateSubSeason(String s) {
            if (s == null) return null;
            // Expect patterns like EARLY_SPRING, MID_SUMMER, LATE_AUTUMN, etc.
            String upper = s.toUpperCase();
            String base;
            String period;
            if (upper.startsWith("EARLY_")) { period = "Início"; base = upper.substring(6); }
            else if (upper.startsWith("MID_")) { period = "Meio"; base = upper.substring(4); }
            else if (upper.startsWith("LATE_")) { period = "Final"; base = upper.substring(5); }
            else { return translateSeason(upper); }
            String seasonPt = translateSeason(base);
            return period + " " + seasonPt;
        }

        private static String translateTropicalSubSeason(String s) {
            if (s == null) return null;
            String upper = s.toUpperCase();
            // Tropical enums: EARLY_DRY, MID_DRY, LATE_DRY, EARLY_WET, MID_WET, LATE_WET
            boolean dry = upper.endsWith("_DRY");
            boolean wet = upper.endsWith("_WET");
            String period;
            if (upper.startsWith("EARLY_")) period = "Início";
            else if (upper.startsWith("MID_")) period = "Meio";
            else if (upper.startsWith("LATE_")) period = "Final";
            else period = "";
            String seasonType = dry ? " Seca" : wet ? " Chuvosa" : "";
            if (period.isEmpty()) return (dry?"Seca": wet?"Chuvosa": upper);
            return period + seasonType;
        }
    }

    // ---------------------------------------------------------------------
    // Ícone da estação
    // ---------------------------------------------------------------------
    private static void drawSeasonIcon(GuiGraphics gui, int x, int y, String seasonDisplayPt, int size) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return;
            String base = baseSeasonKey(seasonDisplayPt);
            if (base == null) return;
            ResourceLocation rl = SeasonIconCache.get(base);
            if (rl == null) return; // nenhum sprite encontrado
            // Ícone desenhado integralmente; halos eram causados por mipmap/blur. .mcmeta define blur:false clamp:true.
            gui.blit(rl, x, y, 0, 0, size, size, size, size);
        } catch (Throwable ignored) {}
    }

    private static ResourceLocation locateSeasonTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath("daydisplay", "textures/gui/seasons/" + name + ".png");
    }

    // textureExists removido após mudança para processamento dinâmico

    private static String baseSeasonKey(String display) {
        if (display == null) return null;
        String d = display.toLowerCase();
        if (d.contains("primavera")) return "spring";
        if (d.contains("verão") || d.contains("verao")) return "summer";
        if (d.contains("outono")) return "autumn"; // autumn base
        if (d.contains("inverno")) return "winter";
        // Fallback inglês caso traduções não tenham ocorrido
        if (d.contains("spring")) return "spring";
        if (d.contains("summer")) return "summer";
        if (d.contains("autumn") || d.contains("fall")) return "autumn";
        if (d.contains("winter")) return "winter";
        return null;
    }

    private static void drawLine(GuiGraphics gui, Minecraft mc, Component text,
                                 int x, int y, int color, int globalShadowColor,
                                 boolean shadowEnabled, int shadowOx, int shadowOy, int shadowColor,
                                 float scale) {
        // Use matrix scaling if scale != 1
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        if (scale != 1f) {
            pose.scale(scale, scale, 1f);
        }
        int drawX = 0;
        int drawY = 0;
        if (shadowEnabled) {
            gui.drawString(mc.font, text, drawX + Math.round(shadowOx / scale), drawY + Math.round(shadowOy / scale), shadowColor);
        }
        gui.drawString(mc.font, text, drawX, drawY, color);
        pose.popPose();
    }

    // Removed per-line dynamic lookup helper (replaced by direct config fields)

    // Cache simples para evitar lookups repetidos por tick
    private static final class SeasonIconCache {
        private static boolean initialized = false;
        private static ResourceLocation spring;
        private static ResourceLocation summer;
        private static ResourceLocation autumn;
        private static ResourceLocation winter;

        private static void init() {
            if (initialized) return;
            initialized = true;
            spring = loadIfExists("spring");
            summer = loadIfExists("summer");
            autumn = loadIfExists("autumn");
            winter = loadIfExists("winter");
        }

    private static ResourceLocation loadIfExists(String name) {
            ResourceLocation rl = locateSeasonTexture(name);
            Minecraft mc = Minecraft.getInstance();
            if (mc == null) return null;
            var opt = mc.getResourceManager().getResource(rl);
            if (opt.isEmpty()) return null;
            try (var stream = opt.get().open()) {
        NativeImage original = NativeImage.read(stream);
        NativeImage processed = processIcon(original);
        DynamicTexture dyn = new DynamicTexture(processed);
                ResourceLocation dynLoc = ResourceLocation.fromNamespaceAndPath("daydisplay", "processed/" + name + "_icon");
                mc.getTextureManager().register(dynLoc, dyn); // Forge 1.20.1: register(ResourceLocation, Texture)
                return dynLoc;
            } catch (Throwable t) {
                return rl; // fallback original
            }
        }

        static ResourceLocation get(String base) {
            init();
            return switch (base) {
                case "spring" -> spring;
                case "summer" -> summer;
                case "autumn" -> autumn;
                case "winter" -> winter;
                default -> null;
            };
        }

        private static NativeImage processIcon(NativeImage src) {
            String method = DayDisplayConfig.CLIENT.iconProcessingMethod.get();
            int bleedPasses = DayDisplayConfig.CLIENT.iconBleedPasses.get();
            int padding = DayDisplayConfig.CLIENT.iconPadding.get();
            // Clone base
            NativeImage work = new NativeImage(src.getWidth(), src.getHeight(), false);
            for (int y=0;y<src.getHeight();y++)
                for (int x=0;x<src.getWidth();x++)
                    work.setPixelRGBA(x,y, src.getPixelRGBA(x,y));
            if (!"none".equalsIgnoreCase(method)) {
                if (method.contains("bleed")) {
                    bleedTransparentRGB(work, bleedPasses);
                }
                if (method.contains("premultiply")) {
                    premultiplyRGB(work);
                }
            }
            if (padding > 0) {
                work = addPadding(work, padding);
            }
            return work;
        }

        // Spread edge color into fully transparent pixels (keep alpha 0)
        private static void bleedTransparentRGB(NativeImage img, int passes) {
            passes = Math.max(0, Math.min(8, passes));
            int w = img.getWidth();
            int h = img.getHeight();
            int[] buf = new int[w*h];
            for (int p=0;p<passes;p++) {
                for (int y=0;y<h;y++) for (int x=0;x<w;x++) buf[y*w+x] = img.getPixelRGBA(x,y);
                for (int y=0;y<h;y++) {
                    for (int x=0;x<w;x++) {
                        int argb = buf[y*w+x];
                        int a = (argb>>>24)&0xFF;
                        if (a==0) {
                            int rSum=0,gSum=0,bSum=0,count=0;
                            for (int dy=-1;dy<=1;dy++) for (int dx=-1;dx<=1;dx++) if (dx!=0||dy!=0) {
                                int nx=x+dx, ny=y+dy; if(nx<0||ny<0||nx>=w||ny>=h) continue;
                                int n=buf[ny*w+nx]; int na=(n>>>24)&0xFF; if(na>220){rSum+=(n>>>16)&0xFF; gSum+=(n>>>8)&0xFF; bSum+=n&0xFF; count++;}
                            }
                            if (count>0) {
                                int r=rSum/count, g=gSum/count, b=bSum/count; img.setPixelRGBA(x,y,(0<<24)|(r<<16)|(g<<8)|b);
                            }
                        }
                    }
                }
            }
        }

        // Premultiply RGB by alpha to reduce fringe on linear filtering
        private static void premultiplyRGB(NativeImage img) {
            int w=img.getWidth(), h=img.getHeight();
            for(int y=0;y<h;y++) for(int x=0;x<w;x++) {
                int c=img.getPixelRGBA(x,y); int a=(c>>>24)&0xFF; int r=(c>>>16)&0xFF; int g=(c>>>8)&0xFF; int b=c&0xFF;
                r = r*a/255; g=g*a/255; b=b*a/255; img.setPixelRGBA(x,y,(a<<24)|(r<<16)|(g<<8)|b);
            }
        }

        // Add padding border replicating nearest edge pixels
        private static NativeImage addPadding(NativeImage img, int pad) {
            int w=img.getWidth(), h=img.getHeight();
            int nw=w+pad*2, nh=h+pad*2;
            NativeImage out=new NativeImage(nw, nh, false);
            for(int y=0;y<nh;y++) for(int x=0;x<nw;x++) {
                int sx = Math.min(Math.max(x-pad,0), w-1);
                int sy = Math.min(Math.max(y-pad,0), h-1);
                out.setPixelRGBA(x,y, img.getPixelRGBA(sx,sy));
            }
            return out;
        }
    }
}
