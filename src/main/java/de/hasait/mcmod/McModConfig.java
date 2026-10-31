package de.hasait.mcmod;

import java.util.List;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.slf4j.Logger;

public class McModConfig {

    private static final Logger LOGGER = McMod.LOGGER;

    // Min inclusive
    public static final int MINI_START_PERCENTAGE = 1;
    // Max inclusive
    public static final int MAXI_START_PERCENTAGE = 99;
    // Min inclusive
    public static final int MINI_STEP_PERCENTAGE = 1;
    // Max inclusive
    public static final int MAXI_STEP_PERCENTAGE = 100;
    // Min inclusive
    public static final int MINI_SCAN = 1;
    // Max inclusive
    public static final int MAXI_SCAN = 30;

    private int repairGolemStartHealthPercentage = 99;
    private int repairGolemHealthStepPercentage = 5;

    private int repairGolemScanX = 15;
    private int repairGolemScanY = 10;
    private int repairGolemScanZ = 15;

    private int healVillagerStartHealthPercentage = 99;
    private int healVillagerHealthStepPercentage = 5;

    private int healVillagerScanX = 15;
    private int healVillagerScanY = 10;
    private int healVillagerScanZ = 15;

    public McModConfig copyFrom(McModConfig other) {
        repairGolemStartHealthPercentage = other.repairGolemStartHealthPercentage;
        repairGolemHealthStepPercentage = other.repairGolemHealthStepPercentage;
        repairGolemScanX = other.repairGolemScanX;
        repairGolemScanY = other.repairGolemScanY;
        repairGolemScanZ = other.repairGolemScanZ;
        healVillagerStartHealthPercentage = other.healVillagerStartHealthPercentage;
        healVillagerHealthStepPercentage = other.healVillagerHealthStepPercentage;
        healVillagerScanX = other.healVillagerScanX;
        healVillagerScanY = other.healVillagerScanY;
        healVillagerScanZ = other.healVillagerScanZ;
        return this;
    }

    public McModConfig fromJson(JsonObject json, List<String> validationMessages) {
        loadJsonInt(json, validationMessages, "repairGolemStartHealthPercentage", this::setRepairGolemStartHealthPercentage);
        loadJsonInt(json, validationMessages, "repairGolemHealthStepPercentage", this::setRepairGolemHealthStepPercentage);
        loadJsonInt(json, validationMessages, "repairGolemScanX", this::setRepairGolemScanX);
        loadJsonInt(json, validationMessages, "repairGolemScanY", this::setRepairGolemScanY);
        loadJsonInt(json, validationMessages, "repairGolemScanZ", this::setRepairGolemScanZ);
        loadJsonInt(json, validationMessages, "healVillagerStartHealthPercentage", this::setHealVillagerStartHealthPercentage);
        loadJsonInt(json, validationMessages, "healVillagerHealthStepPercentage", this::setHealVillagerHealthStepPercentage);
        loadJsonInt(json, validationMessages, "healVillagerScanX", this::setHealVillagerScanX);
        loadJsonInt(json, validationMessages, "healVillagerScanY", this::setHealVillagerScanY);
        loadJsonInt(json, validationMessages, "healVillagerScanZ", this::setHealVillagerScanZ);
        return this;
    }

    public void toJson(JsonObject json) {
        json.addProperty("repairGolemStartHealthPercentage", getRepairGolemStartHealthPercentage());
        json.addProperty("repairGolemHealthStepPercentage", getRepairGolemHealthStepPercentage());
        json.addProperty("repairGolemScanX", getRepairGolemScanX());
        json.addProperty("repairGolemScanY", getRepairGolemScanY());
        json.addProperty("repairGolemScanZ", getRepairGolemScanZ());
        json.addProperty("healVillagerStartHealthPercentage", getHealVillagerStartHealthPercentage());
        json.addProperty("healVillagerHealthStepPercentage", getHealVillagerHealthStepPercentage());
        json.addProperty("healVillagerScanX", getHealVillagerScanX());
        json.addProperty("healVillagerScanY", getHealVillagerScanY());
        json.addProperty("healVillagerScanZ", getHealVillagerScanZ());
    }

    private static void loadJsonInt(JsonObject json, List<String> validationMessages, String key, Consumer<Integer> setter) {
        if (json.has(key)) {
            int value = json.get(key).getAsInt();
            setter.accept(value);
        } else {
            validationMessages.add("Value " + key + " not found - using default");
        }
    }

    public McModConfig fromBuf(RegistryFriendlyByteBuf buf) {
        setRepairGolemStartHealthPercentage(buf.readInt());
        setRepairGolemHealthStepPercentage(buf.readInt());
        setRepairGolemScanX(buf.readInt());
        setRepairGolemScanY(buf.readInt());
        setRepairGolemScanZ(buf.readInt());
        setHealVillagerStartHealthPercentage(buf.readInt());
        setHealVillagerHealthStepPercentage(buf.readInt());
        setHealVillagerScanX(buf.readInt());
        setHealVillagerScanY(buf.readInt());
        setHealVillagerScanZ(buf.readInt());
        return this;
    }

    public void toBuf(RegistryFriendlyByteBuf buf) {
        buf.writeInt(getRepairGolemStartHealthPercentage());
        buf.writeInt(getRepairGolemHealthStepPercentage());
        buf.writeInt(getRepairGolemScanX());
        buf.writeInt(getRepairGolemScanY());
        buf.writeInt(getRepairGolemScanZ());
        buf.writeInt(getHealVillagerStartHealthPercentage());
        buf.writeInt(getHealVillagerHealthStepPercentage());
        buf.writeInt(getHealVillagerScanX());
        buf.writeInt(getHealVillagerScanY());
        buf.writeInt(getHealVillagerScanZ());
    }

    public boolean isValid(List<String> validationMessages) {
        return true //
                && isInRange(validationMessages, "repairGolemStartHealthPercentage", repairGolemStartHealthPercentage, MINI_START_PERCENTAGE, MAXI_START_PERCENTAGE) //
                && isInRange(validationMessages, "repairGolemHealthStepPercentage", repairGolemHealthStepPercentage, MINI_STEP_PERCENTAGE, MAXI_STEP_PERCENTAGE) //
                && isInRange(validationMessages, "repairGolemScanX", repairGolemScanX, MINI_SCAN, MAXI_SCAN) //
                && isInRange(validationMessages, "repairGolemScanY", repairGolemScanY, MINI_SCAN, MAXI_SCAN) //
                && isInRange(validationMessages, "repairGolemScanZ", repairGolemScanZ, MINI_SCAN, MAXI_SCAN) //
                && isInRange(validationMessages, "healVillagerStartHealthPercentage", healVillagerStartHealthPercentage, MINI_START_PERCENTAGE, MAXI_START_PERCENTAGE) //
                && isInRange(validationMessages, "healVillagerHealthStepPercentage", healVillagerHealthStepPercentage, MINI_STEP_PERCENTAGE, MAXI_STEP_PERCENTAGE) //
                && isInRange(validationMessages, "healVillagerScanX", healVillagerScanX, MINI_SCAN, MAXI_SCAN) //
                && isInRange(validationMessages, "healVillagerScanY", healVillagerScanY, MINI_SCAN, MAXI_SCAN) //
                && isInRange(validationMessages, "healVillagerScanZ", healVillagerScanZ, MINI_SCAN, MAXI_SCAN) //
                ;
    }

    private static boolean isInRange(List<String> validationMessages, String key, int value, int minInclusive, int maxInclusive) {
        if (value >= minInclusive && value <= maxInclusive) {
            return true;
        }
        if (validationMessages != null) {
            validationMessages.add("Value " + key + " = " + value + " out of range [" + minInclusive + "; " + maxInclusive + "]");
        }
        return false;
    }

    public int getRepairGolemStartHealthPercentage() {
        return repairGolemStartHealthPercentage;
    }

    public void setRepairGolemStartHealthPercentage(int repairGolemStartHealthPercentage) {
        this.repairGolemStartHealthPercentage = repairGolemStartHealthPercentage;
    }

    public int getRepairGolemHealthStepPercentage() {
        return repairGolemHealthStepPercentage;
    }

    public void setRepairGolemHealthStepPercentage(int repairGolemHealthStepPercentage) {
        this.repairGolemHealthStepPercentage = repairGolemHealthStepPercentage;
    }

    public int getRepairGolemScanX() {
        return repairGolemScanX;
    }

    public void setRepairGolemScanX(int repairGolemScanX) {
        this.repairGolemScanX = repairGolemScanX;
    }

    public int getRepairGolemScanY() {
        return repairGolemScanY;
    }

    public void setRepairGolemScanY(int repairGolemScanY) {
        this.repairGolemScanY = repairGolemScanY;
    }

    public int getRepairGolemScanZ() {
        return repairGolemScanZ;
    }

    public void setRepairGolemScanZ(int repairGolemScanZ) {
        this.repairGolemScanZ = repairGolemScanZ;
    }

    public int getHealVillagerStartHealthPercentage() {
        return healVillagerStartHealthPercentage;
    }

    public void setHealVillagerStartHealthPercentage(int healVillagerStartHealthPercentage) {
        this.healVillagerStartHealthPercentage = healVillagerStartHealthPercentage;
    }

    public int getHealVillagerHealthStepPercentage() {
        return healVillagerHealthStepPercentage;
    }

    public void setHealVillagerHealthStepPercentage(int healVillagerHealthStepPercentage) {
        this.healVillagerHealthStepPercentage = healVillagerHealthStepPercentage;
    }

    public int getHealVillagerScanX() {
        return healVillagerScanX;
    }

    public void setHealVillagerScanX(int healVillagerScanX) {
        this.healVillagerScanX = healVillagerScanX;
    }

    public int getHealVillagerScanY() {
        return healVillagerScanY;
    }

    public void setHealVillagerScanY(int healVillagerScanY) {
        this.healVillagerScanY = healVillagerScanY;
    }

    public int getHealVillagerScanZ() {
        return healVillagerScanZ;
    }

    public void setHealVillagerScanZ(int healVillagerScanZ) {
        this.healVillagerScanZ = healVillagerScanZ;
    }

}
