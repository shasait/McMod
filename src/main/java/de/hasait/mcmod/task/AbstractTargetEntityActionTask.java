package de.hasait.mcmod.task;

import de.hasait.mcmod.McMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Map;

public abstract class AbstractTargetEntityActionTask<T extends Entity, TC> extends MultiTickTask<VillagerEntity> {
    private static final int TICKS_TO_NEXT_ACTION_BASE = 2;
    private static final int TICKS_TO_NEXT_ACTION_RANDOM = 5;

    private final float maxActionDistance;

    private T target;
    private TC targetContext;
    private int ticksToNextAction;

    public AbstractTargetEntityActionTask(float maxActionDistance) {
        super(Map.of());

        this.maxActionDistance = maxActionDistance;
    }

    @Override
    protected final boolean shouldRun(ServerWorld world, VillagerEntity villager) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.shouldRun");
        List<T> candidates = findCandidates(world, villager);
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
            TC candidateContext = determineActionItemIfSuitable(world, villager, candidate, true);
            if (candidateContext != null) {
                float distance = villager.distanceTo(candidate);
                if (minDistance == null || distance < minDistance) {
                    minDistance = distance;
                    target = candidate;
                    targetContext = candidateContext;
                }
            }
        }

        // minDistance is set if we found a candidate
        return minDistance != null;
    }

    @Override
    protected final void run(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.run");
        executeActionThrottled(world, villager, time);
    }

    @Override
    protected final boolean shouldKeepRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.shouldKeepRunning");
        return determineActionItemIfSuitable(world, villager, target, false) != null;
    }

    @Override
    protected final void keepRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.keepRunning");
        this.executeActionThrottled(world, villager, time);
    }

    @Override
    protected final void finishRunning(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.finishRunning");
        finishAction(world, villager, time, target, targetContext);
        target = null;
        targetContext = null;
    }

    protected final void executeActionThrottled(ServerWorld world, VillagerEntity villager, long time) {
        McMod.LOGGER.debug("AbstractTargetEntityActionTask.executeAction: ticksToNextAction={}", ticksToNextAction);
        villager.getNavigation().startMovingTo(target, 0.5);
        if (villager.distanceTo(target) <= maxActionDistance) {
            if (ticksToNextAction-- <= 0) {
                executeAction(world, villager, time, target, targetContext);
                ticksToNextAction = target.getRandom().nextInt(TICKS_TO_NEXT_ACTION_RANDOM) + TICKS_TO_NEXT_ACTION_BASE;
            }
        } else {
            outOfRange(world, villager, time, target, targetContext);
        }
    }

    protected abstract List<T> findCandidates(ServerWorld world, VillagerEntity villager);

    protected abstract TC determineActionItemIfSuitable(ServerWorld world, VillagerEntity villager, T candidate, boolean startActionCheck);

    protected abstract void executeAction(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

    protected abstract void outOfRange(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

    protected abstract void finishAction(ServerWorld world, VillagerEntity villager, long time, T target, TC targetContext);

}