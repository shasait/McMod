package de.hasait.mcmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McMod implements ModInitializer {
    public static final String MOD_ID = "mcmod";

    public static final Logger LOGGER = LoggerFactory.getLogger(McMod.class);

    public static final McModConfig CONFIG = new McModConfig();
    public static final McModConfigStore CONFIG_STORE = new McModConfigStore("config/mc_mod.json", CONFIG);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("onInitialize");

        CONFIG_STORE.load();
    }

}