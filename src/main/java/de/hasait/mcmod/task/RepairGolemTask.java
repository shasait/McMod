package de.hasait.mcmod.task;

import de.hasait.mcmod.McMod;
import de.hasait.mcmod.McModConfigStore;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RepairGolemTask extends AbstractTargetEntityActionTask<GolemEntity, RepairGolemTaskContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(McModConfigStore.class);

    private static final Map<EntityType<?>, Item> TARGET_TYPE_TO_ACTION_ITEM = Map.of(EntityType.IRON_GOLEM, Items.IRON_INGOT, EntityType.COPPER_GOLEM, Items.COPPER_INGOT);

    public RepairGolemTask() {
        super(2, 5, 2.0F);
    }

    @Override
    protected List<GolemEntity> findTargetCandidates(ServerWorld world, VillagerEntity villager) {
        Box scanBox = villager.getBoundingBox().expand(McMod.CONFIG.getRepairGolemScanX(), McMod.CONFIG.getRepairGolemScanY(), McMod.CONFIG.getRepairGolemScanZ());
        return villager.getEntityWorld().getNonSpectatingEntities(GolemEntity.class, scanBox);
    }

    @Override
    protected void executeActionInRange(ServerWorld world, VillagerEntity villager, long time, GolemEntity target, RepairGolemTaskContext targetContext) {
        villager.equipStack(EquipmentSlot.MAINHAND, new ItemStack(targetContext.actionItem));
        villager.swingHand(Hand.MAIN_HAND);
        target.heal(targetContext.healAmount);
        float pitch = 5.0F + (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.2F;
        target.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 0.5F, pitch);
    }

    @Override
    protected void executeActionOutOfRange(ServerWorld world, VillagerEntity villager, long time, GolemEntity target, RepairGolemTaskContext targetContext) {
        villager.equipStack(EquipmentSlot.MAINHAND, new ItemStack(targetContext.actionItem));
    }

    @Override
    protected void finishAction(ServerWorld world, VillagerEntity villager, long time, GolemEntity target, RepairGolemTaskContext targetContext) {
        villager.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }

    protected RepairGolemTaskContext determineTargetContextIfRunnable(ServerWorld world, VillagerEntity villager, GolemEntity candidate, boolean startActionCheck) {
        LOGGER.debug("RepairGolemTask.determineActionItemIfSuitable: {}", candidate);
        if (candidate == null) {
            return null;
        }
        EntityType<?> type = candidate.getType();
        float health = candidate.getHealth();
        float candidateMaxHealth = candidate.getMaxHealth();
        float startActionHealthLimit = startActionCheck ? candidateMaxHealth * McMod.CONFIG.getRepairGolemStartHealthPercentage() / 100.0F : candidateMaxHealth;
        LOGGER.debug("RepairGolemTask.determineActionItemIfSuitable: Type={} Health={}/{}", type, health, startActionHealthLimit);
        if (!candidate.isAlive() || health >= startActionHealthLimit) {
            return null;
        }
        Item actionItem = TARGET_TYPE_TO_ACTION_ITEM.get(type);
        if (actionItem == null) {
            return null;
        }
        return new RepairGolemTaskContext(actionItem, Math.max(1.0F, candidateMaxHealth * McMod.CONFIG.getRepairGolemHealthStepPercentage() / 100.0F));
    }

}