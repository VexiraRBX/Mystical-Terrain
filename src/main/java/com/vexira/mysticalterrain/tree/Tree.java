package com.vexira.mysticalterrain.tree;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public final class Tree {
	private final List<Shape> wood;
	private final List<Shape> veins;
	private final List<Shape> foliage;
	private final long seed;
	private final double foot;
	private final int[] bounds = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

	Tree(List<Shape> wood, List<Shape> veins, List<Shape> foliage, long seed, double foot) {
		this.wood = wood;
		this.veins = veins;
		this.foliage = foliage;
		this.seed = seed;
		this.foot = foot;
		for (List<Shape> shapes : List.of(wood, veins, foliage)) {
			for (Shape shape : shapes) {
				double[] b = shape.bounds();
				for (int i = 0; i < 3; i++) {
					bounds[i] = Math.min(bounds[i], (int) Math.floor(b[i]));
					bounds[i + 3] = Math.max(bounds[i + 3], (int) Math.ceil(b[i + 3]));
				}
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

	// How far the base of the trunk spreads from the middle, flare included.
	public double foot() {
		return foot;
	}

	public int reach() {
		return Math.max(Math.max(-bounds[0], bounds[3]), Math.max(-bounds[2], bounds[5]));
	}

	// Coordinates are relative to the base of the trunk; the box is inclusive.
	public void rasterize(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Sink sink) {
		minX = Math.max(minX, bounds[0]);
		minY = Math.max(minY, bounds[1]);
		minZ = Math.max(minZ, bounds[2]);
		maxX = Math.min(maxX, bounds[3]);
		maxY = Math.min(maxY, bounds[4]);
		maxZ = Math.min(maxZ, bounds[5]);
		if (minX > maxX || minY > maxY || minZ > maxZ) {
			return;
		}
		draw(wood, minX, minY, minZ, maxX, maxY, maxZ, sink, null);
		draw(veins, minX, minY, minZ, maxX, maxY, maxZ, sink, null);
		BitSet done = new BitSet((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1));
		draw(foliage, minX, minY, minZ, maxX, maxY, maxZ, sink, done);
	}

	private void draw(List<Shape> shapes, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Sink sink, BitSet done) {
		int sizeY = maxY - minY + 1;
		int sizeZ = maxZ - minZ + 1;
		for (Shape shape : shapes) {
			double[] b = shape.bounds();
			int x0 = Math.max(minX, (int) Math.floor(b[0])), x1 = Math.min(maxX, (int) Math.ceil(b[3]));
			int y0 = Math.max(minY, (int) Math.floor(b[1])), y1 = Math.min(maxY, (int) Math.ceil(b[4]));
			int z0 = Math.max(minZ, (int) Math.floor(b[2])), z1 = Math.min(maxZ, (int) Math.ceil(b[5]));
			int axis = shape.axis();
			for (int x = x0; x <= x1; x++) {
				for (int y = y0; y <= y1; y++) {
					for (int z = z0; z <= z1; z++) {
						int index = done == null ? 0 : ((x - minX) * sizeY + (y - minY)) * sizeZ + (z - minZ);
						if (done != null && done.get(index) || !shape.contains(x, y, z, seed)) {
							continue;
						}
						if (done != null) {
							done.set(index);
						}
						sink.place(x, y, z, shape.part(), axis);
					}
				}
			}
		}
	}
}
