package de.hasait.mcmod.task;

import net.minecraft.world.item.Item;

public record HealVillagerBehaviorContext(Item actionItem, float healAmount) {
}
