package com.vexira.mysticalterrain.tree;

/**
 * The shape genes of a grown tree. lobes is how many masses the crown is split into (domes on a canopy tree, pads on a layered
 * one) and forks how many leaders the trunk splits into; everything else is a 0-1 fraction, apart from tropism (-1 droops,
 * 1 reaches for the sky) and twig, which scales the foliage clumps.
 */
public record Growth(int lobes, double spread, double flat, double tiers, double lopside, double bends, double swing, int forks,
		double split, double sprout, double tropism, double twig, double drip, double hollow, double buttress, double jitter) {
	public static final Growth DEFAULT = new Growth(5, 0.5, 0.5, 0.5, 0.2, 0.5, 0.3, 1, 0.6, 0.4, 0.1, 1, 0.3, 0.5, 0.3, 0.3);
}
