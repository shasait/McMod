package de.hasait.mcmod.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

/**
 * Immutable config - therefore thread safe.
 */
public class McModConfig {

    private static final Logger LOGGER = McMod.LOGGER;

    private final List<ResourceKey<VillagerProfession>> repairGolemProfessions;

    private final Map<EntityType<?>, Item> repairGolemActionItems;

    private final int repairGolemStartHealthPercentage;
    private final int repairGolemHealthStepPercentage;

    private final int repairGolemScanX;
    private final int repairGolemScanY;
    private final int repairGolemScanZ;

    private final Map<ResourceKey<VillagerProfession>, Item> healVillagerProfessions;

    private final int healVillagerStartHealthPercentage;
    private final int healVillagerHealthStepPercentage;

    private final int healVillagerScanX;
    private final int healVillagerScanY;
    private final int healVillagerScanZ;

    McModConfig(McModConfigBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder cannot be null");
        }
        builder.validate();

        repairGolemProfessions = List.copyOf(builder.getRepairGolemProfessions());
        repairGolemActionItems = Map.copyOf(builder.getRepairGolemActionItems());
        repairGolemStartHealthPercentage = builder.getRepairGolemStartHealthPercentage();
        repairGolemHealthStepPercentage = builder.getRepairGolemHealthStepPercentage();
        repairGolemScanX = builder.getRepairGolemScanX();
        repairGolemScanY = builder.getRepairGolemScanY();
        repairGolemScanZ = builder.getRepairGolemScanZ();

        healVillagerProfessions = Map.copyOf(builder.getHealVillagerProfessions());
        healVillagerStartHealthPercentage = builder.getHealVillagerStartHealthPercentage();
        healVillagerHealthStepPercentage = builder.getHealVillagerHealthStepPercentage();
        healVillagerScanX = builder.getHealVillagerScanX();
        healVillagerScanY = builder.getHealVillagerScanY();
        healVillagerScanZ = builder.getHealVillagerScanZ();
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

    public int getRepairGolemHealthStepPercentage() {
        return repairGolemHealthStepPercentage;
    }

    public int getRepairGolemScanX() {
        return repairGolemScanX;
    }

    public int getRepairGolemScanY() {
        return repairGolemScanY;
    }

    public int getRepairGolemScanZ() {
        return repairGolemScanZ;
    }

    public Map<ResourceKey<VillagerProfession>, Item> getHealVillagerProfessions() {
        return healVillagerProfessions;
    }

    public int getHealVillagerStartHealthPercentage() {
        return healVillagerStartHealthPercentage;
    }

    public int getHealVillagerHealthStepPercentage() {
        return healVillagerHealthStepPercentage;
    }

    public int getHealVillagerScanX() {
        return healVillagerScanX;
    }

    public int getHealVillagerScanY() {
        return healVillagerScanY;
    }

    public int getHealVillagerScanZ() {
        return healVillagerScanZ;
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

}
