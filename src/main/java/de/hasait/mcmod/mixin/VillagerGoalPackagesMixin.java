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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.hasait.mcmod.McMod;
import de.hasait.mcmod.config.McModConfig;
import de.hasait.mcmod.task.HealVillagerBehavior;
import de.hasait.mcmod.task.RepairGolemBehavior;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {

    @Inject(method = "getWorkPackage", cancellable = true, at = @At("RETURN"))
    private static void mcmod$getWorkPackage(Holder<VillagerProfession> profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        McModConfig config = McMod.getConfig();
        List<ResourceKey<VillagerProfession>> repairGolemProfessions = config.getRepairGolemProfessions();
        if (repairGolemProfessions.stream().anyMatch(profession::is)) {
            List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(12, new RepairGolemBehavior(repairGolemProfessions)));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
        Map<ResourceKey<VillagerProfession>, Item> healVillagerProfessions = config.getHealVillagerProfessions();
        if (healVillagerProfessions.keySet().stream().anyMatch(profession::is)) {
            List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(12, new HealVillagerBehavior(healVillagerProfessions)));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
    }

    @Inject(method = "getMeetPackage", cancellable = true, at = @At("RETURN"))
    private static void mcmod$getMeetPackage(float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        McModConfig config = McMod.getConfig();
        Map<ResourceKey<VillagerProfession>, Item> healVillagerProfessions = config.getHealVillagerProfessions();
        List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
        villagerList.add(Pair.of(12, new HealVillagerBehavior(healVillagerProfessions)));
        cir.setReturnValue(ImmutableList.copyOf(villagerList));
    }
}
