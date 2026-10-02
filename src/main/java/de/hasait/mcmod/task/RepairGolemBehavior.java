package de.hasait.mcmod.task;

import java.util.List;
import java.util.Map;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

public class RepairGolemBehavior extends AbstractVillagerTargetEntityBehavior<AbstractGolem, RepairGolemBehaviorContext> {

    private static final Logger LOGGER = McMod.LOGGER;

    private static final Map<EntityType<?>, Item> TARGET_TYPE_TO_ACTION_ITEM = Map.of(EntityTypes.IRON_GOLEM, Items.IRON_INGOT, EntityTypes.COPPER_GOLEM, Items.COPPER_INGOT);

    public RepairGolemBehavior() {
        super(2, 5, 2.0F);
    }

    @Override
    protected List<AbstractGolem> findTargetCandidates(ServerLevel level, Villager villager) {
        var scanBox = villager.getBoundingBox().expandTowards(McMod.CONFIG.getRepairGolemScanX(), McMod.CONFIG.getRepairGolemScanY(), McMod.CONFIG.getRepairGolemScanZ());
        return level.getEntitiesOfClass(AbstractGolem.class, scanBox);
    }

    @Override
    protected void executeActionInRange(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem, EquipmentSlot.MAINHAND);
        villager.swing(InteractionHand.MAIN_HAND);
        target.heal(targetContext.healAmount);
        float pitch = 5.0F + (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.2F;
        target.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.5F, pitch);
    }

    @Override
    protected void executeActionOutOfRange(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem, EquipmentSlot.MAINHAND);
    }

    @Override
    protected void finishAction(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, null, EquipmentSlot.MAINHAND);
    }

    @Override
    protected boolean checkIfSuitableTargetQuick(ServerLevel level, Villager villager, AbstractGolem candidate, boolean startActionCheck) {
        LOGGER.debug("RepairGolemTask.checkIfSuitableTargetQuick: {}", candidate);
        if (candidate == null) {
            return false;
        }
        EntityType<?> type = candidate.getType();
        float health = candidate.getHealth();
        float candidateMaxHealth = candidate.getMaxHealth();
        float startActionHealthLimit = startActionCheck ? candidateMaxHealth * McMod.CONFIG.getRepairGolemStartHealthPercentage() / 100.0F : candidateMaxHealth;
        LOGGER.debug("RepairGolemTask.checkIfSuitableTargetQuick: Type={} Health={}/{}", type, health, startActionHealthLimit);
        if (!candidate.isAlive() || health >= startActionHealthLimit) {
            return false;
        }
        return true;
    }

    @Override
    protected RepairGolemBehaviorContext createTargetContextIfSuitable(ServerLevel level, Villager villager, AbstractGolem candidate, boolean startActionCheck) {
        LOGGER.debug("RepairGolemTask.createTargetContextIfSuitable: {}", candidate);
        if (candidate == null) {
            return null;
        }
        EntityType<?> type = candidate.getType();
        Item actionItem = TARGET_TYPE_TO_ACTION_ITEM.get(type);
        if (actionItem == null) {
            return null;
        }
        float candidateMaxHealth = candidate.getMaxHealth();
        return new RepairGolemBehaviorContext(actionItem, Math.max(1.0F, candidateMaxHealth * McMod.CONFIG.getRepairGolemHealthStepPercentage() / 100.0F));
    }

}