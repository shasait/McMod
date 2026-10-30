package de.hasait.mcmod.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.hasait.mcmod.task.HealVillagerBehavior;
import de.hasait.mcmod.task.RepairGolemBehavior;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {

    private static final List<ResourceKey<VillagerProfession>> REPAIR_GOLEM_PROFESSIONS = List.of( //
            VillagerProfession.TOOLSMITH, //
            VillagerProfession.WEAPONSMITH, //
            VillagerProfession.ARMORER //
    );
    private static final Map<ResourceKey<VillagerProfession>, Item> HEAL_VILLAGER_PROFESSIONS = Map.of( //
            VillagerProfession.FARMER, Items.BREAD, //
            VillagerProfession.FISHERMAN, Items.COOKED_SALMON, //
            VillagerProfession.CLERIC, Items.POTION //
    );

    @Inject(method = "getWorkPackage", cancellable = true, at = @At("RETURN"))
    private static void mcmod$getWorkPackage(Holder<VillagerProfession> profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        if (REPAIR_GOLEM_PROFESSIONS.stream().anyMatch(profession::is)) {
            List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(12, new RepairGolemBehavior(REPAIR_GOLEM_PROFESSIONS)));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
        if (HEAL_VILLAGER_PROFESSIONS.keySet().stream().anyMatch(profession::is)) {
            List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(12, new HealVillagerBehavior(HEAL_VILLAGER_PROFESSIONS)));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
    }

    @Inject(method = "getMeetPackage", cancellable = true, at = @At("RETURN"))
    private static void mcmod$getMeetPackage(float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
        villagerList.add(Pair.of(12, new HealVillagerBehavior(HEAL_VILLAGER_PROFESSIONS)));
        cir.setReturnValue(ImmutableList.copyOf(villagerList));
    }
}
