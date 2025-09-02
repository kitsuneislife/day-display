package com.example.daydisplay.forge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import com.example.daydisplay.forge.config.DayDisplayConfig;

@Mod(DayDisplayModForge.MODID)
public class DayDisplayModForge {
    public static final String MODID = "daydisplay";
    private static final Logger LOGGER = LogManager.getLogger(DayDisplayModForge.class);

    @SuppressWarnings("removal")
    public DayDisplayModForge() {
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DayDisplayConfig.CLIENT_SPEC, "daydisplay-client.toml");
    LOGGER.info("DayDisplay mod loaded (config registered)");
    }
}
