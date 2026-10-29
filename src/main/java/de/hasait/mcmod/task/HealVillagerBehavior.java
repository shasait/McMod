package de.hasait.mcmod.task;

import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SwingAnimation;
import org.slf4j.Logger;

import de.hasait.mcmod.McMod;

public class HealVillagerBehavior extends AbstractVillagerTargetEntityBehavior<Villager, HealVillagerBehaviorContext> {

    private static final Logger LOGGER = McMod.LOGGER;

    private final Map<ResourceKey<VillagerProfession>, Item> professionActionItems;

    private static final int TIMEOUT_TICKS = 20 * 60 * 5; // 20 ticks per second * 60 seconds * 5 minutes

    public HealVillagerBehavior(Map<ResourceKey<VillagerProfession>, Item> professionActionItems) {
        super(professionActionItems.keySet(), TIMEOUT_TICKS, 40, 20, 2.0F);
        this.professionActionItems = professionActionItems;
    }

    @Override
    protected List<Villager> findTargetCandidates(ServerLevel level, Villager villager) {
        var scanBox = villager.getBoundingBox().expandTowards(McMod.CONFIG.getHealVillagerScanX(), McMod.CONFIG.getHealVillagerScanY(), McMod.CONFIG.getHealVillagerScanZ());
        return level.getEntitiesOfClass(Villager.class, scanBox);
    }

    @Override
    protected void executeActionInRange(ServerLevel level, Villager villager, long time, Villager target, HealVillagerBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem(), EquipmentSlot.MAINHAND);
        villager.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
        target.heal(targetContext.healAmount());
        float pitch = 1.0F + (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.2F;
        target.playSound(SoundEvents.GENERIC_EAT.value(), 0.5F, pitch);
    }

    @Override
    protected void executeActionOutOfRange(ServerLevel level, Villager villager, long time, Villager target, HealVillagerBehaviorContext targetContext) {
        villagerEquipItem(villager, targetContext.actionItem(), EquipmentSlot.MAINHAND);
    }

    @Override
    protected void finishAction(ServerLevel level, Villager villager, long time, Villager target, HealVillagerBehaviorContext targetContext) {
        villagerEquipItem(villager, null, EquipmentSlot.MAINHAND);
    }

    @Override
    protected boolean checkIfSuitableTargetQuick(ServerLevel level, Villager villager, Villager candidate, boolean startActionCheck) {
        var logMsgPrefix = logMethodEntry("checkIfSuitableTargetQuick", villager);
        if (candidate == null) {
            LOGGER.debug("-> false: no candidate");
            return false;
        }
        float health = candidate.getHealth();
        float maxHealth = candidate.getMaxHealth();
        // always repair up to maxHealth
        float startActionHealthLimit = startActionCheck ? maxHealth * McMod.CONFIG.getHealVillagerStartHealthPercentage() / 100.0F : maxHealth;
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
    protected HealVillagerBehaviorContext createTargetContextIfSuitable(ServerLevel level, Villager villager, Villager candidate, boolean startActionCheck) {
        var logMsgPrefix = logMethodEntry("createTargetContextIfSuitable", villager);
        if (candidate == null) {
            LOGGER.debug("-> null: no candidate");
            return null;
        }

        Holder<VillagerProfession> profession = villager.getVillagerData().profession();
        Item actionItem = professionActionItems.entrySet().stream().filter(entry -> profession.is(entry.getKey())).map(Map.Entry::getValue).findFirst().orElse(null);
        if (actionItem == null) {
            LOGGER.debug("-> null: no action item for {} with profession {}", candidate, profession);
            return null;
        }
        float candidateMaxHealth = candidate.getMaxHealth();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("-> new context for {}", candidate);
        } else {
            LOGGER.info("{} -> new context for {}", logMsgPrefix, candidate);
        }
        return new HealVillagerBehaviorContext(actionItem, Math.max(1.0F, candidateMaxHealth * McMod.CONFIG.getHealVillagerHealthStepPercentage() / 100.0F));
    }

}