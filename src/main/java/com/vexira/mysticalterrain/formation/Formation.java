package com.vexira.mysticalterrain.formation;

import java.util.Arrays;
import java.util.List;

public final class Formation {
	private final List<Solid> solids;
	private final List<int[]> chests;
	private final long seed;
	private final int[] bounds = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

	Formation(List<Solid> solids, List<int[]> chests, long seed) {
		this.solids = solids;
		this.chests = chests;
		this.seed = seed;
		for (Solid solid : solids) {
			double[] b = solid.bounds();
			for (int i = 0; i < 3; i++) {
				bounds[i] = Math.min(bounds[i], (int) Math.floor(b[i]));
				bounds[i + 3] = Math.max(bounds[i + 3], (int) Math.ceil(b[i + 3]));
			}
		}
		for (int[] chest : chests) {
			for (int i = 0; i < 3; i++) {
				bounds[i] = Math.min(bounds[i], chest[i]);
				bounds[i + 3] = Math.max(bounds[i + 3], chest[i]);
			}
		}
		if (bounds[0] > bounds[3]) {
			Arrays.fill(bounds, 0);
		}
	}

	public int minX() {
		return bounds[0];
	}

	public int minY() {
		return bounds[1];
	}

	public int minZ() {
		return bounds[2];
	}

	public int maxX() {
		return bounds[3];
	}

	public int maxY() {
		return bounds[4];
	}

	public int maxZ() {
		return bounds[5];
	}

	public int reach() {
		return Math.max(Math.max(-bounds[0], bounds[3]), Math.max(-bounds[2], bounds[5]));
	}

	// Relative to the base, like everything else here.
	public List<int[]> chests() {
		return chests;
	}

	// Solids are drawn in the order they were built, so later ones can carve or cover earlier ones. The box is inclusive.
	public void rasterize(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Sink sink) {
		for (Solid solid : solids) {
			double[] b = solid.bounds();
			int x0 = Math.max(minX, (int) Math.floor(b[0])), x1 = Math.min(maxX, (int) Math.ceil(b[3]));
			int y0 = Math.max(minY, (int) Math.floor(b[1])), y1 = Math.min(maxY, (int) Math.ceil(b[4]));
			int z0 = Math.max(minZ, (int) Math.floor(b[2])), z1 = Math.min(maxZ, (int) Math.ceil(b[5]));
			int axis = solid.axis();
			boolean force = solid.force();
			for (int x = x0; x <= x1; x++) {
				for (int y = y0; y <= y1; y++) {
					for (int z = z0; z <= z1; z++) {
						Material material = solid.at(x, y, z, seed);
						if (material != null) {
							sink.place(x, y, z, material, axis, force);
						}
					}
				}
			}
		}
	}
}
