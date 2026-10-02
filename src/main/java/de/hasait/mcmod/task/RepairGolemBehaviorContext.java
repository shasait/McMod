package de.hasait.mcmod.task;

import net.minecraft.world.item.Item;

public class RepairGolemBehaviorContext {
    public final Item actionItem;
    public final float healAmount;

    public RepairGolemBehaviorContext(Item actionItem, float healAmount) {
        this.actionItem = actionItem;
        this.healAmount = healAmount;
    }
}
