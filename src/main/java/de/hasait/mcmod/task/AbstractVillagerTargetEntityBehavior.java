package de.hasait.mcmod.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

public abstract class AbstractVillagerTargetEntityBehavior<T extends Entity, TC> extends Behavior<Villager> {

    private static final Logger LOGGER = McMod.LOGGER;

    private final Collection<ResourceKey<VillagerProfession>> professions;
    private final int ticksToNextActionBase;
    private final int ticksToNextActionRandom;

    private final float maxActionDistance;

    private T target;
    private TC targetContext;
    private int ticksToNextAction;

    public AbstractVillagerTargetEntityBehavior(Collection<ResourceKey<VillagerProfession>> professions, int timeoutTicks, int ticksToNextActionBase, int ticksToNextActionRandom, float maxActionDistance) {
        super(Map.of(), timeoutTicks);

        this.professions = professions;
        this.ticksToNextActionBase = ticksToNextActionBase;
        this.ticksToNextActionRandom = ticksToNextActionRandom;
        this.maxActionDistance = maxActionDistance;
    }

    @Override
    protected final boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        var logMsgPrefix = logMethodEntry("checkExtraStartConditions", villager);

        var profession = villager.getVillagerData().profession();
        if (professions.stream().noneMatch(profession::is)) {
            LOGGER.debug("-> false: unsupported profession={}", profession);
            return false;
        }

        List<T> candidates = findTargetCandidates(level, villager);
        if (candidates.isEmpty()) {
            LOGGER.debug("-> false: no candidates");
            return false;
        }

        // prevent target re-assignment
        // TODO this should actually not happen if start and stop works correctly
        if (target != null && candidates.contains(target)) {
            LOGGER.warn("{}: BUG? Prevented target re-assignment", logMsgPrefix);
            return super.checkExtraStartConditions(level, villager);
        }

        // select candidate closest to villager
        Float minDistance = null;
        for (T candidate : candidates) {
            if (checkIfSuitableTargetQuick(level, villager, candidate, true)) {
                float distance = villager.distanceTo(candidate);
                if (minDistance == null || distance < minDistance) {
                    TC candidateContext = createTargetContextIfSuitable(level, villager, candidate, true);
                    if (candidateContext != null) {
                        target = candidate;
                        targetContext = candidateContext;
                        minDistance = distance;
                    }
                }
            }
        }

        // minDistance is only set if we found a target
        if (minDistance == null) {
            LOGGER.debug("-> false: no target");
            return false;
        }
        if (!super.checkExtraStartConditions(level, villager)) {
            LOGGER.debug("-> false: checkExtraStartConditions");
            target = null;
            targetContext = null;
            return false;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> true: minDistance={}, target={}", minDistance, target);
        } else {
            LOGGER.info("{} -> true: villager={}, minDistance={}, target={}", logMsgPrefix, villager, minDistance, target);
        }
        return true;
    }

    @Override
    protected final void start(ServerLevel level, Villager villager, long timestamp) {
        logMethodEntry("start", villager);
        super.start(level, villager, timestamp);
        executeActionThrottled(level, villager, timestamp);
    }

    @Override
    protected final boolean canStillUse(ServerLevel level, Villager villager, long timestamp) {
        logMethodEntry("canStillUse", villager);
        if (!checkIfSuitableTargetQuick(level, villager, target, false)) {
            return false;
        }
        return createTargetContextIfSuitable(level, villager, target, false) != null;
    }

    @Override
    protected final void tick(ServerLevel level, Villager villager, long timestamp) {
        logMethodEntry("tick", villager);
        super.tick(level, villager, timestamp);
        this.executeActionThrottled(level, villager, timestamp);
    }

    @Override
    protected final void stop(ServerLevel level, Villager villager, long timestamp) {
        logMethodEntry("stop", villager);
        finishAction(level, villager, timestamp, target, targetContext);
        target = null;
        targetContext = null;
        ticksToNextAction = 0;
        super.stop(level, villager, timestamp);
    }

    protected final void executeActionThrottled(ServerLevel level, Villager villager, long time) {
        logMethodEntry("executeActionThrottled", villager);
        villager.getNavigation().moveTo(target, 0.5);
        if (villager.distanceTo(target) <= maxActionDistance) {
            if (ticksToNextAction-- <= 0) {
                executeActionInRange(level, villager, time, target, targetContext);
                ticksToNextAction = target.getRandom().nextInt(ticksToNextActionRandom) + ticksToNextActionBase;
            }
        } else {
            executeActionOutOfRange(level, villager, time, target, targetContext);
        }
    }

    protected abstract List<T> findTargetCandidates(ServerLevel level, Villager villager);

    /**
     * Check if candidate is most likely a suitable target.
     * Expensive checks can be executed in {@link #createTargetContextIfSuitable}.
     */
    protected abstract boolean checkIfSuitableTargetQuick(ServerLevel level, Villager villager, T candidate, boolean startActionCheck);

    /**
     * Check if candidate matches and is a suitable target - if so return a new target context - else return null.
     * Checks of #checkIfSuitableTargetQuick are not needed to be checked again.
     */
    protected abstract TC createTargetContextIfSuitable(ServerLevel level, Villager villager, T candidate, boolean startActionCheck);

    protected abstract void executeActionInRange(ServerLevel level, Villager villager, long time, T target, TC targetContext);

    protected abstract void executeActionOutOfRange(ServerLevel level, Villager villager, long time, T target, TC targetContext);

    protected abstract void finishAction(ServerLevel level, Villager villager, long time, T target, TC targetContext);

    protected final void villagerEquipItem(Villager villager, Item item, EquipmentSlot slot) {
        ItemStack newItemStack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
        villager.setItemSlot(slot, newItemStack);
    }

    protected final String logMethodEntry(String methodName, Villager villager) {
        var logMsgPrefix = getClass().getSimpleName() + "." + methodName;
        LOGGER.debug("{}: villager={}", logMsgPrefix, villager);
        return logMsgPrefix;
    }

}
