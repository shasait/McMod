package de.hasait.mcmod.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

public class McModConfigStore {

    private static final Logger LOGGER = McMod.LOGGER;

    private final File configFile;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public McModConfigStore(String configFilePath) {
        this.configFile = new File(configFilePath);
    }

    public void load() {
        if (configFile.exists()) {
            LOGGER.info("Loading config... {}", configFile);
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                McModConfig loaded = new McModConfigBuilder().fromJson(json, null).build();
                LOGGER.info("Config {} loaded", configFile);
                McMod.updateConfig(loaded);
            } catch (Exception e) {
                LOGGER.error("Loading config {} failed - using defaults", configFile, e);
                save();
            }
        } else {
            LOGGER.info("Creating new config... {}", configFile);
            save();
        }
    }

    public void save() {
        try {
            configFile.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            McMod.getConfig().toJson(json);
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(json, writer);
                LOGGER.info("Config {} saved", configFile);
            }
        } catch (IOException e) {
            LOGGER.error("Saving config {} failed", configFile, e);
        }
    }

}
