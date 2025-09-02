package com.example.daydisplay.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Forge client config spec (daydisplay-client.toml) supporting per-line customization.
 */
public final class DayDisplayConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    public static final class Client {
        // Day line
        public final ForgeConfigSpec.BooleanValue dayEnabled;
        public final ForgeConfigSpec.IntValue dayX;
        public final ForgeConfigSpec.IntValue dayY;
        public final ForgeConfigSpec.IntValue dayColor;
    public final ForgeConfigSpec.IntValue dayShadowColor;
    public final ForgeConfigSpec.IntValue dayShadowOffsetX;
    public final ForgeConfigSpec.IntValue dayShadowOffsetY;
    public final ForgeConfigSpec.IntValue dayScaleMilli;
        // Time line
        public final ForgeConfigSpec.BooleanValue timeEnabled;
        public final ForgeConfigSpec.IntValue timeX;
        public final ForgeConfigSpec.IntValue timeY;
        public final ForgeConfigSpec.IntValue timeColor;
        public final ForgeConfigSpec.BooleanValue use24h;
    public final ForgeConfigSpec.IntValue timeShadowColor;
    public final ForgeConfigSpec.IntValue timeShadowOffsetX;
    public final ForgeConfigSpec.IntValue timeShadowOffsetY;
    public final ForgeConfigSpec.IntValue timeScaleMilli;
        // Season line
        public final ForgeConfigSpec.BooleanValue seasonEnabled;
        public final ForgeConfigSpec.IntValue seasonX;
        public final ForgeConfigSpec.IntValue seasonY;
        public final ForgeConfigSpec.IntValue seasonColor;
    public final ForgeConfigSpec.IntValue seasonShadowColor;
    public final ForgeConfigSpec.IntValue seasonShadowOffsetX;
    public final ForgeConfigSpec.IntValue seasonShadowOffsetY;
    public final ForgeConfigSpec.IntValue seasonScaleMilli;
        // Season icon
        public final ForgeConfigSpec.BooleanValue iconEnabled;
        public final ForgeConfigSpec.IntValue iconX;
        public final ForgeConfigSpec.IntValue iconY;
        public final ForgeConfigSpec.IntValue iconSize;
    public final ForgeConfigSpec.ConfigValue<String> iconProcessingMethod; // none | bleed | premultiply | bleed_premultiply
    public final ForgeConfigSpec.IntValue iconBleedPasses; // passes for bleed
    public final ForgeConfigSpec.IntValue iconPadding; // pixels of padding replication
        // Shadow
        public final ForgeConfigSpec.BooleanValue shadowEnabled;
        public final ForgeConfigSpec.IntValue shadowColor;
        public final ForgeConfigSpec.IntValue shadowOffsetX;
        public final ForgeConfigSpec.IntValue shadowOffsetY;
        // Seasonal refresh
        public final ForgeConfigSpec.IntValue seasonRefreshInterval;
        // Global base offsets (added to each line/icon)
        public final ForgeConfigSpec.IntValue baseX;
        public final ForgeConfigSpec.IntValue baseY;

        Client(ForgeConfigSpec.Builder b) {
            b.push("hud");
            baseX = b.defineInRange("baseX", 8, -10000, 10000);
            baseY = b.defineInRange("baseY", 8, -10000, 10000);

            b.push("day");
            dayEnabled = b.define("enabled", true);
            dayX = b.defineInRange("x", 64, -10000, 10000);
            dayY = b.defineInRange("y", 7, -10000, 10000);
            dayColor = b.defineInRange("color", 5569620, 0x000000, 0xFFFFFF);
            dayShadowColor = b.defineInRange("shadowColor", 2445361, 0x000000, 0xFFFFFF);
            dayShadowOffsetX = b.defineInRange("shadowOffsetX", 0, -16, 16);
            dayShadowOffsetY = b.defineInRange("shadowOffsetY", 0, -16, 16);
            dayScaleMilli = b.defineInRange("scaleMilli", 1200, 100, 5000); // scale *1000 (float)
            b.pop();

            b.push("time");
            timeEnabled = b.define("enabled", true);
            timeX = b.defineInRange("x", 34, -10000, 10000);
            timeY = b.defineInRange("y", 8, -10000, 10000);
            timeColor = b.defineInRange("color", 0xFFFFFF, 0x000000, 0xFFFFFF);
            use24h = b.comment("If false, show 12h clock with AM/PM").define("use24h", true);
            timeShadowColor = b.defineInRange("shadowColor", 0, 0x000000, 0xFFFFFF);
            timeShadowOffsetX = b.defineInRange("shadowOffsetX", 0, -16, 16);
            timeShadowOffsetY = b.defineInRange("shadowOffsetY", 0, -16, 16);
            timeScaleMilli = b.defineInRange("scaleMilli", 1000, 100, 5000);
            b.pop();

            b.push("season");
            seasonEnabled = b.define("enabled", true);
            seasonX = b.defineInRange("x", 34, -10000, 10000);
            seasonY = b.defineInRange("y", 20, -10000, 10000);
            seasonColor = b.defineInRange("color", 0xFFFFFF, 0x000000, 0xFFFFFF);
            seasonShadowColor = b.defineInRange("shadowColor", 0, 0x000000, 0xFFFFFF);
            seasonShadowOffsetX = b.defineInRange("shadowOffsetX", 0, -16, 16);
            seasonShadowOffsetY = b.defineInRange("shadowOffsetY", 0, -16, 16);
            seasonScaleMilli = b.defineInRange("scaleMilli", 1000, 100, 5000);
            b.pop();

            b.push("icon");
            iconEnabled = b.define("enabled", true);
            iconX = b.defineInRange("x", -52, -10000, 10000);
            iconY = b.defineInRange("y", -43, -10000, 10000);
            iconSize = b.defineInRange("size", 256, 4, 512);
            iconProcessingMethod = b.comment("Icon color fringe mitigation: none | bleed | premultiply | bleed_premultiply").define("processingMethod", "bleed_premultiply");
            iconBleedPasses = b.comment("Number of bleed (edge color spread) passes (0-8)").defineInRange("bleedPasses", 2, 0, 8);
            iconPadding = b.comment("Extra padding pixels copied around icon (0-8)").defineInRange("padding", 2, 0, 8);
            b.pop();

            b.push("shadow");
            shadowEnabled = b.define("enabled", true);
            shadowColor = b.defineInRange("color", 0x000000, 0x000000, 0xFFFFFF);
            shadowOffsetX = b.defineInRange("offsetX", 0, -16, 16);
            shadowOffsetY = b.defineInRange("offsetY", 0, -16, 16);
            b.pop();

            seasonRefreshInterval = b.comment("Season lookup throttle (ticks)").defineInRange("seasonRefreshInterval", 40, 1, 1200);
            b.pop();
        }
    }
}
