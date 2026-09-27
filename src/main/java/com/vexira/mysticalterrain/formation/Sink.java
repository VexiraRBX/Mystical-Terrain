package com.vexira.mysticalterrain.formation;

@FunctionalInterface
public interface Sink {
	// axis: 0 = x, 1 = y, 2 = z. force lets the block replace solid ground instead of only air and plants.
	void place(int x, int y, int z, Material material, int axis, boolean force);
}
