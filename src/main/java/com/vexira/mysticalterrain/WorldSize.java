package com.vexira.mysticalterrain;

// Vanilla's world ends at ±30,000,000. Block positions are repacked with 27 bits per horizontal axis
// (see BlockPosMixin and SectionPosMixin), which reaches ±67,108,864, so the world ends a little inside that.
public final class WorldSize {
	public static final int LIMIT = 67_000_000;
	public static final int BORDER_RADIUS = LIMIT - 16;
	public static final double BORDER_SIZE = BORDER_RADIUS * 2.0;

	private WorldSize() {
	}
}
