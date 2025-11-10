package de.hasait.mcmod.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import de.hasait.mcmod.task.RepairGolemTask;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.GatherItemsVillagerTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    @Inject(method = "createWorkTasks", cancellable = true, at = @At("RETURN"))
    private static void mcmod$createWorkTasks(RegistryEntry<VillagerProfession> profession, float speed, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (profession.matchesKey(VillagerProfession.TOOLSMITH) || profession.matchesKey(VillagerProfession.WEAPONSMITH)) {
            List<Pair<Integer, ? extends Task<? super VillagerEntity>>> villagerList = new ArrayList<>(cir.getReturnValue());
            villagerList.add(Pair.of(2, new CompositeTask<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), CompositeTask.Order.ORDERED, CompositeTask.RunMode.RUN_ONE, ImmutableList.of(Pair.of(new RepairGolemTask(), 1), Pair.of(new GatherItemsVillagerTask(), 1)))));
            cir.setReturnValue(ImmutableList.copyOf(villagerList));
        }
    }
}
