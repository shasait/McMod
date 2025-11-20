package de.hasait.mcmod.task;

import de.hasait.mcmod.McMod;
import de.hasait.mcmod.McModConfigStore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public abstract class AbstractTargetEntityActionTask<T extends Entity, TC> extends MultiTickTask<VillagerEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(McModConfigStore.class);

    private final int ticksToNextActionBase;
    private final int ticksToNextActionRandom;

    private final float maxActionDistance;

    private T target;
    private TC targetContext;
    private int ticksToNextAction;

    public AbstractTargetEntityActionTask(int ticksToNextActionBase, int ticksToNextActionRandom, float maxActionDistance) {
        super(Map.of());

        this.ticksToNextActionBase = ticksToNextActionBase;
        this.ticksToNextActionRandom = ticksToNextActionRandom;
        this.maxActionDistance = maxActionDistance;
    }

    @Override
    protected final boolean shouldRun(ServerWorld world, VillagerEntity villager) {
        McMod.LOGGER.debug("shouldRun");
        List<T> candidates = findTargetCandidates(world, villager);
        if (candidates.isEmpty()) {
            return false;
        }

        // prevent target re-assignment
        if (target != null && candidates.contains(target)) {
            return true;
        }

        // select candidate closest to villager
        Float minDistance = null;
        for (T candidate : candidates) {
            float distance = villager.distanceTo(candidate);
            if (minDistance == null || distance < minDistance) {
                TC candidateContext = createTargetContextIfSuitable(world, villager, candidate, true);
                if (candidateContext != null) {
                    minDistance = distance;
                    target = candidate;
                    targetContext = candidateContext;
                }
            }
        }

        // minDistance is only set if we found a target
        return minDistance != null;
    }

    @Override
    protected final void run(ServerWorld world, VillagerEntity villager, long time) {
        LOGGER.debug("run");
        executeActionThrottled(world, villager, time);
    }

    @Override
    protected final boolean shouldKeepRunning(ServerWorld world, VillagerEntity villager, long time) {
        LOGGER.debug("shouldKeepRunning");
        return createTargetContextIfSuitable(world, villager, target, false) != null;
    }

    @Override
    protected final void keepRunning(ServerWorld world, VillagerEntity villager, long time) {
        LOGGER.debug("keepRunning");
        this.executeActionThrottled(world, villager, time);
    }

    @Override
    protected final void finishRunning(ServerWorld world, VillagerEntity villager, long time) {
        LOGGER.debug("finishRunning");
        finishAction(world, villager, time, target, targetContext);
        target = null;
        targetContext = null;
    }

    protected final void executeActionThrottled(ServerWorld world, VillagerEntity villager, long time) {
        LOGGER.debug("executeAction: ticksToNextAction={}", ticksToNextAction);
        villager.getNavigation().startMovingTo(target, 0.5);
        if (villager.distanceTo(target) <= maxActionDistance) {
            if (ticksToNextAction-- <= 0) {
                executeActionInRange(world, villager, time, target, targetContext);
                ticksToNextAction = target.getRandom().nextInt(ticksToNextActionRandom) + ticksToNextActionBase;
            }
        } else {
            executeActionOutOfRange(world, villager, time, target, targetContext);
        }
    }

    protected abstract List<T> findTargetCandidates(ServerWorld world, VillagerEntity villager);

    /**
     * Check if candidate matches and is a suitable target - if so return a new target context - else return null.
     */
    protected abstract TC createTargetContextIfSuitable(ServerWorld world, VillagerEntity villager, T candidate, boolean startActionCheck);

    protected abstract void executeActionInRange(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

    protected abstract void executeActionOutOfRange(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

    protected abstract void finishAction(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

}