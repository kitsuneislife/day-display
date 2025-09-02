package com.example.daydisplay.client;

import com.example.daydisplay.forge.config.DayDisplayConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lightweight hot-reload for the client config file. Polls modification time every N ticks.
 * Avoids WatchService overhead and stays simple/robust across OSes.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientConfigReloader {
    private static final Logger LOG = LogManager.getLogger();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("daydisplay-client.toml");
    private static long lastKnownTimestamp = -1L;
    private static int tickCounter = 0;
    private static final int POLL_INTERVAL_TICKS = 20; // check once per second

    private ClientConfigReloader() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter < POLL_INTERVAL_TICKS) return;
        tickCounter = 0;
        if (!Files.isRegularFile(CONFIG_PATH)) return;
        try {
            long ts = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            if (lastKnownTimestamp != -1L && ts != lastKnownTimestamp) {
                reload();
            }
            lastKnownTimestamp = ts; // set after first discovery
        } catch (Exception ignored) {}
    }

    private static void reload() {
        try (CommentedFileConfig cfg = CommentedFileConfig.of(CONFIG_PATH)) {
            cfg.load();
            DayDisplayConfig.CLIENT_SPEC.setConfig(cfg);
            LOG.info("daydisplay-client.toml reloaded");
        } catch (Exception e) {
            LOG.warn("Failed to hot-reload daydisplay-client.toml", e);
        }
    }
}
