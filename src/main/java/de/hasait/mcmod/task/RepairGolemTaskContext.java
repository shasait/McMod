package de.hasait.mcmod.task;

import net.minecraft.item.Item;

public class RepairGolemTaskContext {
    public final Item actionItem;
    public final float healAmount;

    public RepairGolemTaskContext(Item actionItem, float healAmount) {
        this.actionItem = actionItem;
        this.healAmount = healAmount;
    }
}
