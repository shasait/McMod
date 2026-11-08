package de.hasait.mcmod.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Modifiable config which should be used from a single thread.
 * This is not thread safe.
 */
public class McModConfigBuilder {

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

    private static final List<ResourceKey<VillagerProfession>> DEFAULT_repairGolemProfessions = List.of( //
            VillagerProfession.TOOLSMITH, //
            VillagerProfession.WEAPONSMITH, //
            VillagerProfession.ARMORER //
    );

    private static final Map<EntityType<?>, Item> DEFAULT_repairGolemActionItems = Map.of(EntityTypes.IRON_GOLEM, Items.IRON_INGOT, EntityTypes.COPPER_GOLEM, Items.COPPER_INGOT);

    private static final Map<ResourceKey<VillagerProfession>, Item> DEFAULT_healVillagerProfessions = Map.of( //
            VillagerProfession.FARMER, Items.BREAD, //
            VillagerProfession.FISHERMAN, Items.COOKED_SALMON, //
            VillagerProfession.CLERIC, Items.POTION //
    );

    private final List<ResourceKey<VillagerProfession>> repairGolemProfessions;

    private final Map<EntityType<?>, Item> repairGolemActionItems;

    private int repairGolemStartHealthPercentage;
    private int repairGolemHealthStepPercentage;

    private int repairGolemScanX;
    private int repairGolemScanY;
    private int repairGolemScanZ;

    private final Map<ResourceKey<VillagerProfession>, Item> healVillagerProfessions;

    private int healVillagerStartHealthPercentage;
    private int healVillagerHealthStepPercentage;

    private int healVillagerScanX;
    private int healVillagerScanY;
    private int healVillagerScanZ;

    public McModConfigBuilder() {
        repairGolemProfessions = new ArrayList<>(DEFAULT_repairGolemProfessions);
        repairGolemActionItems = new LinkedHashMap<>(DEFAULT_repairGolemActionItems);
        repairGolemStartHealthPercentage = 99;
        repairGolemHealthStepPercentage = 5;
        repairGolemScanX = 15;
        repairGolemScanY = 10;
        repairGolemScanZ = 15;

        healVillagerProfessions = new LinkedHashMap<>(DEFAULT_healVillagerProfessions);
        healVillagerStartHealthPercentage = 99;
        healVillagerHealthStepPercentage = 5;
        healVillagerScanX = 15;
        healVillagerScanY = 10;
        healVillagerScanZ = 15;
    }

    public McModConfigBuilder(McModConfig config) {
        repairGolemProfessions = new ArrayList<>(config.getRepairGolemProfessions());
        repairGolemActionItems = new LinkedHashMap<>(config.getRepairGolemActionItems());
        repairGolemStartHealthPercentage = config.getRepairGolemStartHealthPercentage();
        repairGolemHealthStepPercentage = config.getRepairGolemHealthStepPercentage();
        repairGolemScanX = config.getRepairGolemScanX();
        repairGolemScanY = config.getRepairGolemScanY();
        repairGolemScanZ = config.getRepairGolemScanZ();

        healVillagerProfessions = new LinkedHashMap<>(config.getHealVillagerProfessions());
        healVillagerStartHealthPercentage = config.getHealVillagerStartHealthPercentage();
        healVillagerHealthStepPercentage = config.getHealVillagerHealthStepPercentage();
        healVillagerScanX = config.getHealVillagerScanX();
        healVillagerScanY = config.getHealVillagerScanY();
        healVillagerScanZ = config.getHealVillagerScanZ();
    }

    public McModConfigBuilder fromJson(JsonObject json, List<String> validationMessages) {
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

    private static void loadJsonInt(JsonObject json, List<String> validationMessages, String key, Consumer<Integer> setter) {
        if (json.has(key)) {
            int value = json.get(key).getAsInt();
            setter.accept(value);
        } else if (validationMessages != null) {
            validationMessages.add("Value " + key + " not found - using default");
        }
    }

    public McModConfigBuilder fromBuf(RegistryFriendlyByteBuf buf) {
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

    public void validate() {
        List<String> validationMessages = new ArrayList<>();
        if (!isValid(validationMessages)) {
            throw new IllegalArgumentException("builder is invalid: " + validationMessages);
        }
    }

    public List<ResourceKey<VillagerProfession>> getRepairGolemProfessions() {
        return repairGolemProfessions;
    }

    public Map<EntityType<?>, Item> getRepairGolemActionItems() {
        return repairGolemActionItems;
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

    public Map<ResourceKey<VillagerProfession>, Item> getHealVillagerProfessions() {
        return healVillagerProfessions;
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

    public McModConfig build() {
        return new McModConfig(this);
    }

}
