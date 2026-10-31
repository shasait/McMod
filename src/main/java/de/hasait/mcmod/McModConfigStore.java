package de.hasait.mcmod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;

public class McModConfigStore {

    private static final Logger LOGGER = McMod.LOGGER;

    private final File configFile;
    private final McModConfig config;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean saveConfigNeeded = false;

    public McModConfigStore(String configFilePath, McModConfig config) {
        this.configFile = new File(configFilePath);
        this.config = config;
    }

    public void load() {
        LOGGER.info("Loading config... {}", configFile);

        if (configFile.exists()) {
            List<String> validationMessages = new ArrayList<>();
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                McModConfig loaded = new McModConfig().fromJson(json, validationMessages);
                if (loaded.isValid(validationMessages)) {
                    config.copyFrom(loaded);
                    LOGGER.info("Config {} loaded", configFile);
                }
            } catch (IOException e) {
                LOGGER.error("Loading config {} failed - using defaults", configFile, e);
            }
            validationMessages.forEach(LOGGER::warn);
        } else {
            save();
        }
    }

    public void save() {
        try {
            configFile.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            config.toJson(json);
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(json, writer);
                LOGGER.info("Config {} saved", configFile);
            }
        } catch (IOException e) {
            LOGGER.error("Saving config {} failed", configFile, e);
        }
    }

}
