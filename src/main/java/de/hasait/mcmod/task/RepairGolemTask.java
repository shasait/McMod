package de.hasait.mcmod.task;

import de.hasait.mcmod.McMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.Map;

public class RepairGolemTask extends MultiTickTask<VillagerEntity> {
    private static final Map<EntityType<?>, Item> TARGET_TYPE_TO_ACTION_ITEM = Map.of(EntityType.IRON_GOLEM, Items.IRON_INGOT, EntityType.COPPER_GOLEM, Items.COPPER_INGOT);
    private static final float START_HEAL_IF_MAX_HEALTH_PERCENTAGE = 75.0F;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final double SCAN_X = 15.00;
    private static final double SCAN_Y = 5.00;
    private static final double SCAN_Z = 15.00;
    private static final int TICKS_TO_NEXT_ACTION_BASE = 2;
    private static final int TICKS_TO_NEXT_ACTION_RANDOM = 5;
    private GolemEntity target;
    private Item actionItem;
    private int ticksToNextAction;

    public RepairGolemTask() {
        super(Map.of());
    }

    @Override
    protected boolean shouldRun(ServerWorld world, VillagerEntity villager) {
        McMod.LOGGER.debug("RepairGolemTask.shouldRun");
        List<GolemEntity> candidates = villager.getEntityWorld().getNonSpectatingEntities(GolemEntity.class, villager.getBoundingBox().expand(SCAN_X, SCAN_Y, SCAN_Z));
        if (candidates.isEmpty()) {
            return false;
        }

        // prevent target re-assignment
        if (target != null && candidates.contains(target)) {
            return true;
        }

        // select candidate closest to villager
        Float minDistance = null;
        for (GolemEntity candidate : candidates) {
            float healthLimit = candidate.getMaxHealth() * START_HEAL_IF_MAX_HEALTH_PERCENTAGE / 100.0F;
            Item candidateActionItem = determineActionItemIfSuitable(candidate, healthLimit);
            if (candidateActionItem != null) {
                float distance = villager.distanceTo(target);
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    this.target = candidate;
                    this.actionItem = candidateActionItem;
                }
            }
        }

        // minDistance is set if we found a candidate
        return minDistance != null;
    }

    @Override
    protected void run(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("RepairGolemTask.run");
        villager.equipStack(EquipmentSlot.MAINHAND, new ItemStack(actionItem));
        executeAction(villager);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("RepairGolemTask.shouldKeepRunning");
        return determineActionItemIfSuitable(target, null) != null;
    }

    @Override
    protected void keepRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("RepairGolemTask.keepRunning");
        this.executeAction(villager);
    }

    @Override
    protected void finishRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("RepairGolemTask.finishRunning");
        villager.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        target = null;
        actionItem = null;
    }

    protected void executeAction(VillagerEntity villager) {
        McMod.LOGGER.debug("RepairGolemTask.executeAction: ticksToNextAction={}", ticksToNextAction);
        villager.getNavigation().startMovingTo(target, 0.5);
        if (villager.distanceTo(target) <= 2.0D) {
            if (ticksToNextAction-- <= 0) {
                villager.equipStack(EquipmentSlot.MAINHAND, new ItemStack(actionItem));
                villager.swingHand(Hand.MAIN_HAND);
                target.heal(HEAL_AMOUNT);
                float pitch = 5.0F + (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.2F;
                target.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 0.5F, pitch);
                ticksToNextAction = target.getRandom().nextInt(TICKS_TO_NEXT_ACTION_RANDOM) + TICKS_TO_NEXT_ACTION_BASE;
            }
        }
    }

    protected Item determineActionItemIfSuitable(GolemEntity candidate, Float optionalHealthLimit) {
        McMod.LOGGER.debug("RepairGolemTask.determineActionItemIfSuitable: {}", candidate);
        if (candidate == null) {
            return null;
        }
        EntityType<?> type = candidate.getType();
        float health = candidate.getHealth();
        float healthLimit = optionalHealthLimit != null ? optionalHealthLimit : candidate.getMaxHealth();
        McMod.LOGGER.debug("RepairGolemTask.determineActionItemIfSuitable: Type={} Health={}/{}", type, health, healthLimit);
        if (!candidate.isAlive() || health >= healthLimit) {
            return null;
        }
        return TARGET_TYPE_TO_ACTION_ITEM.get(type);
    }

}