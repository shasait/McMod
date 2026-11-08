package de.hasait.mcmod;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hasait.mcmod.config.McModConfig;
import de.hasait.mcmod.config.McModConfigBuilder;
import de.hasait.mcmod.config.McModConfigNetworking;
import de.hasait.mcmod.config.McModConfigStore;

public class McMod implements ModInitializer {
    public static final String MOD_ID = "mcmod";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile McModConfig CONFIG = new McModConfigBuilder().build();

    private static final McModConfigStore CONFIG_STORE = new McModConfigStore("config/mc_mod.json");

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static McModConfig getConfig() {
        return CONFIG;
    }

    public static void updateConfig(McModConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }
        CONFIG = config;
        LOGGER.info("Config updated");
        CONFIG_STORE.save();
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Initializing server...");

        CONFIG_STORE.load();

        McModConfigNetworking.registerPayloads();
        McModConfigNetworking.registerServerHandlers();
    }

}
