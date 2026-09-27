package com.vexira.mysticalterrain.block;

import java.util.List;
import net.minecraft.world.level.block.Block;

public record WoodSet(Block log, Block wood, Block strippedLog, Block strippedWood, Block planks, Block stairs, Block slab, Block fence,
		Block fenceGate, Block door, Block trapdoor, boolean flammable) {
	public List<Block> building() {
		return List.of(planks, stairs, slab, fence, fenceGate);
	}

	public List<Block> logs() {
		return List.of(log, wood, strippedLog, strippedWood);
	}
}
