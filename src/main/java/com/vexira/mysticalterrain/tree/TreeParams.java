package com.vexira.mysticalterrain.tree;

/**
 * Everything the builder needs, already sampled. roots and branches of -1 mean "whatever suits the style";
 * reach and ceiling are the furthest the tree may extend sideways and upward from its base. growth only matters to the
 * grown styles.
 */
public record TreeParams(TreeStyle style, int height, double radius, double crown, double lean, double gnarl, int roots,
		int branches, double droop, double shape, double density, int veins, int reach, int ceiling, long seed, Growth growth) {
}
