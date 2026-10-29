package de.hasait.mcmod.task;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

public class RepairGolemBehavior extends AbstractVillagerTargetEntityBehavior<AbstractGolem, RepairGolemBehaviorContext> {

    private static final Logger LOGGER = McMod.LOGGER;

    private static final Map<EntityType<?>, Item> TARGET_TYPE_TO_ACTION_ITEM = Map.of(EntityTypes.IRON_GOLEM, Items.IRON_INGOT, EntityTypes.COPPER_GOLEM, Items.COPPER_INGOT);

    private static final int TIMEOUT_TICKS = 20 * 60 * 5; // 20 ticks per second * 60 seconds * 5 minutes

    public RepairGolemBehavior(List<ResourceKey<VillagerProfession>> professions) {
        super(professions, TIMEOUT_TICKS, 20, 30, 2.0F);
    }

    @Override
    protected List<AbstractGolem> findTargetCandidates(ServerLevel level, Villager villager) {
        var scanBox = villager.getBoundingBox().expandTowards(McMod.CONFIG.getRepairGolemScanX(), McMod.CONFIG.getRepairGolemScanY(), McMod.CONFIG.getRepairGolemScanZ());
        return level.getEntitiesOfClass(AbstractGolem.class, scanBox);
    }

    @Override
    protected void executeActionInRange(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem(), EquipmentSlot.MAINHAND);
        villager.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
        target.heal(targetContext.healAmount());
        float pitch = 2.0F + (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.5F;
        target.playSound(SoundEvents.IRON_GOLEM_REPAIR, 0.5F, pitch);
    }

    @Override
    protected void executeActionOutOfRange(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem(), EquipmentSlot.MAINHAND);
    }

    @Override
    protected void finishAction(ServerLevel level, Villager villager, long time, AbstractGolem target, RepairGolemBehaviorContext targetContext) {
        villagerEquipItem(villager, null, EquipmentSlot.MAINHAND);
    }

    @Override
    protected boolean checkIfSuitableTargetQuick(ServerLevel level, Villager villager, AbstractGolem candidate, boolean startActionCheck) {
        var logMsgPrefix = logMethodEntry("checkIfSuitableTargetQuick", villager);
        if (candidate == null) {
            LOGGER.debug("-> false: no candidate");
            return false;
        }
        float health = candidate.getHealth();
        float maxHealth = candidate.getMaxHealth();
        // always repair up to maxHealth
        float startActionHealthLimit = startActionCheck ? maxHealth * McMod.CONFIG.getRepairGolemStartHealthPercentage() / 100.0F : maxHealth;
        if (!candidate.isAlive()) {
            LOGGER.debug("-> false: {} already dead", candidate);
            return false;
        } else if (health >= maxHealth) {
            LOGGER.debug("-> false: {} (current health) >= {} (max health) of {}", health, maxHealth, candidate);
            return false;
        } else if (health > startActionHealthLimit) {
            LOGGER.debug("-> false: {} (current health) > {} (start action health) of {}", health, startActionHealthLimit, candidate);
            return false;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> true: {} (current health) <= {} (start action health) of {}", health, startActionHealthLimit, candidate);
        } else {
            LOGGER.info("{} -> true: {} (current health) <= {} (start action health) of {}", logMsgPrefix, health, startActionHealthLimit, candidate);
        }
        return true;
    }

    @Override
    protected RepairGolemBehaviorContext createTargetContextIfSuitable(ServerLevel level, Villager villager, AbstractGolem candidate, boolean startActionCheck) {
        var logMsgPrefix = logMethodEntry("createTargetContextIfSuitable", villager);
        if (candidate == null) {
            LOGGER.debug("-> null: no candidate");
            return null;
        }
        EntityType<?> type = candidate.getType();
        Item actionItem = TARGET_TYPE_TO_ACTION_ITEM.get(type);
        if (actionItem == null) {
            LOGGER.debug("-> null: no action item for {} with type {}", candidate, type);
            return null;
        }
        float candidateMaxHealth = candidate.getMaxHealth();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> new context for {}", candidate);
        } else {
            LOGGER.info("{} -> new context for {}", logMsgPrefix, candidate);
        }
        return new RepairGolemBehaviorContext(actionItem, Math.max(1.0F, candidateMaxHealth * McMod.CONFIG.getRepairGolemHealthStepPercentage() / 100.0F));
    }

}