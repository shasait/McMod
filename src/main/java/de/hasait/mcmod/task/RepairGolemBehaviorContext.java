package de.hasait.mcmod.task;

import net.minecraft.world.item.Item;

public record RepairGolemBehaviorContext(Item actionItem, float healAmount) {
}
