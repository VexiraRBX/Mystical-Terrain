package com.vexira.mysticalterrain.tree;

@FunctionalInterface
public interface Sink {
	// axis: 0 = x, 1 = y, 2 = z
	void place(int x, int y, int z, Part part, int axis);
}
