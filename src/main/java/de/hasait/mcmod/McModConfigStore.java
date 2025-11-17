package de.hasait.mcmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

public class McModConfigStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(McModConfigStore.class);

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
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                loadDouble(json, "repairGolemScanX", config::setRepairGolemScanX, 0.0, 50.0);
                loadDouble(json, "repairGolemScanY", config::setRepairGolemScanY, 0.0, 50.0);
                loadDouble(json, "repairGolemScanZ", config::setRepairGolemScanZ, 0.0, 50.0);
                loadFloat(json, "repairGolemStartHealthPercentage", config::setRepairGolemStartHealthPercentage, 0.0F, 100.0F);
                loadFloat(json, "repairGolemHealthStepPercentage", config::setRepairGolemHealthStepPercentage, 0.1F, 100.0F);
                LOGGER.info("Config {} loaded", configFile);
            } catch (IOException e) {
                LOGGER.error("Loading config {} failed - using defaults", configFile, e);
            }
        } else {
            saveConfigNeeded = true;
        }

        // Updating if configFile does not fit expected format or is simply missing
        if (saveConfigNeeded) {
            save();
        }
    }

    public void save() {
        try {
            configFile.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            json.addProperty("repairGolemScanX", config.getRepairGolemScanX());
            json.addProperty("repairGolemScanY", config.getRepairGolemScanY());
            json.addProperty("repairGolemScanZ", config.getRepairGolemScanZ());
            json.addProperty("repairGolemStartHealthPercentage", config.getRepairGolemStartHealthPercentage());
            json.addProperty("repairGolemHealthStepPercentage", config.getRepairGolemHealthStepPercentage());

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(json, writer);
                LOGGER.info("Config {} saved", configFile);
            }
        } catch (IOException e) {
            LOGGER.error("Saving config {} failed", configFile, e);
        }
    }

    private void loadFloat(JsonObject json, String key, Consumer<Float> setter, float minInclusive, float maxInclusive) {
        if (json.has(key)) {
            float value = json.get(key).getAsFloat();
            if (value < minInclusive || value > maxInclusive) {
                LOGGER.info("Invalid config {} - value {} out of range {} {} - using default", configFile, key, minInclusive, maxInclusive);
            } else {
                setter.accept(value);
            }
        } else {
            saveConfigNeeded = true;
        }
    }

    private void loadDouble(JsonObject json, String key, Consumer<Double> setter, double minInclusive, double maxInclusive) {
        if (json.has(key)) {
            double value = json.get(key).getAsDouble();
            if (value < minInclusive || value > maxInclusive) {
                LOGGER.info("Invalid config {} - value {} out of range {} {} - using default", configFile, key, minInclusive, maxInclusive);
            } else {
                setter.accept(value);
            }
        } else {
            saveConfigNeeded = true;
        }
    }

}
