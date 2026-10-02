package de.hasait.mcmod.mixin;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.TradeWithVillager;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import de.hasait.mcmod.task.RepairGolemBehavior;

@Mixin(VillagerGoalPackages.class)
public class VillagerTaskListProviderMixin {
    @Inject(method = "getWorkPackage", cancellable = true, at = @At("RETURN"))
    private static void mcmod$getWorkPackage(Holder<VillagerProfession> profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        if (profession.is(VillagerProfession.TOOLSMITH) || profession.is(VillagerProfession.WEAPONSMITH)) {
            List<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(2, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new RepairGolemBehavior(), 1), Pair.of(new TradeWithVillager(), 1)))));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
    }
}
