package com.vexira.mysticalterrain.tree;

sealed interface Shape permits Shape.Tube, Shape.Blob, Shape.Skirt, Shape.Cap, Shape.Canopy {
	Part part();

	// min x, min y, min z, max x, max y, max z
	double[] bounds();

	boolean contains(int x, int y, int z, long seed);

	Shape scaled(double horizontal, double vertical);

	default int axis() {
		return 1;
	}

	private static V3 scale(V3 v, double h, double vert) {
		return new V3(v.x() * h, v.y() * vert, v.z() * h);
	}

	record Tube(V3 a, V3 b, double ra, double rb, Part part, double bark) implements Shape {
		@Override
		public double[] bounds() {
			double r = Math.max(ra, rb) * (1 + bark) + 1;
			return new double[]{Math.min(a.x(), b.x()) - r, Math.min(a.y(), b.y()) - r, Math.min(a.z(), b.z()) - r,
					Math.max(a.x(), b.x()) + r, Math.max(a.y(), b.y()) + r, Math.max(a.z(), b.z()) + r};
		}

		@Override
		public boolean contains(int x, int y, int z, long seed) {
			double abx = b.x() - a.x(), aby = b.y() - a.y(), abz = b.z() - a.z();
			double len2 = abx * abx + aby * aby + abz * abz;
			double t = len2 < 1e-9 ? 0 : ((x - a.x()) * abx + (y - a.y()) * aby + (z - a.z()) * abz) / len2;
			t = Math.max(0, Math.min(1, t));
			double dx = x - a.x() - abx * t, dy = y - a.y() - aby * t, dz = z - a.z() - abz * t;
			double r = ra + (rb - ra) * t;
			if (bark > 0) {
				r *= 1 + bark * Noise.smooth(seed, x / 2.5, y / 5.0, z / 2.5);
			}
			return dx * dx + dy * dy + dz * dz <= r * r + 0.15;
		}

		@Override
		public int axis() {
			double x = Math.abs(b.x() - a.x()), y = Math.abs(b.y() - a.y()), z = Math.abs(b.z() - a.z());
			return y >= x && y >= z ? 1 : x >= z ? 0 : 2;
		}

		@Override
		public Shape scaled(double horizontal, double vertical) {
			double r = Math.sqrt(Math.min(1, Math.min(horizontal, vertical)));
			return new Tube(scale(a, horizontal, vertical), scale(b, horizontal, vertical), ra * r, rb * r, part, bark);
		}
	}

	// An ellipsoid of leaves with a lumpy outline; floor limits how far below the centre it reaches (1 = all the way).
	record Blob(V3 c, double rx, double ry, double rz, double lump, double holes, double floor, Part part) implements Shape {
		@Override
		public double[] bounds() {
			double s = 1 + lump;
			return new double[]{c.x() - rx * s - 1, c.y() - ry * Math.min(s, floor) - 1, c.z() - rz * s - 1,
					c.x() + rx * s + 1, c.y() + ry * s + 1, c.z() + rz * s + 1};
		}

		@Override
		public boolean contains(int x, int y, int z, long seed) {
			if (y - c.y() < -ry * floor) {
				return false;
			}
			double dx = (x - c.x()) / rx, dy = (y - c.y()) / ry, dz = (z - c.z()) / rz;
			double d2 = dx * dx + dy * dy + dz * dz;
			double limit = 1 + lump * Noise.smooth(seed + 17, x / 3.5, y / 3.0, z / 3.5);
			if (d2 > limit * limit) {
				return false;
			}
			return holes <= 0 || d2 < 0.6 * limit * limit || Noise.unit(seed + 31, x, y, z) >= holes;
		}

		@Override
		public Shape scaled(double horizontal, double vertical) {
			return new Blob(scale(c, horizontal, vertical), rx * horizontal, ry * vertical, rz * horizontal, lump, holes, floor, part);
		}
	}

	// A drooping disc of foliage around a trunk, as on spruces and firs.
	record Skirt(V3 c, double r, double thickness, double droop, double holes) implements Shape {
		@Override
		public Part part() {
			return Part.LEAVES;
		}

		@Override
		public double[] bounds() {
			double s = r * 1.25 + 1;
			return new double[]{c.x() - s, c.y() - droop * 1.5 - thickness - 1, c.z() - s, c.x() + s, c.y() + thickness + 1, c.z() + s};
		}

		@Override
		public boolean contains(int x, int y, int z, long seed) {
			double hx = x - c.x(), hz = z - c.z();
			double rho = Math.sqrt(hx * hx + hz * hz);
			double limit = r * (1 + 0.22 * Noise.smooth(seed + 5, x / 2.5, c.y() / 3.0, z / 2.5));
			if (rho > limit) {
				return false;
			}
			double u = rho / Math.max(1, r);
			double mid = c.y() - droop * u * u;
			if (Math.abs(y - mid) > thickness * (1 - 0.45 * u) / 2 + 0.3) {
				return false;
			}
			return holes <= 0 || u < 0.7 || Noise.unit(seed + 13, x, y, z) >= holes;
		}

		@Override
		public Shape scaled(double horizontal, double vertical) {
			return new Skirt(scale(c, horizontal, vertical), r * horizontal, thickness * vertical, droop * vertical, holes);
		}
	}

	// A mushroom cap: a shell over the stem whose rim can curl downward.
	record Cap(V3 c, double r, double height, double thickness, double rim, double holes) implements Shape {
		@Override
		public Part part() {
			return Part.LEAVES;
		}

		@Override
		public double[] bounds() {
			double s = r * 1.06 + 1;
			return new double[]{c.x() - s, c.y() - rim - thickness - 1, c.z() - s, c.x() + s, c.y() + height + 1, c.z() + s};
		}

		@Override
		public boolean contains(int x, int y, int z, long seed) {
			double hx = x - c.x(), hz = z - c.z();
			double u = Math.sqrt(hx * hx + hz * hz) / r;
			u += 0.06 * Noise.smooth(seed + 3, x / 3.0, c.y(), z / 3.0);
			if (u > 1) {
				return false;
			}
			double top = c.y() + height * Math.sqrt(Math.max(0, 1 - u * u));
			double bottom = top - thickness;
			if (u > 0.8) {
				bottom -= rim * (u - 0.8) / 0.2;
			}
			if (y > top + 0.5 || y < bottom - 0.5) {
				return false;
			}
			return holes <= 0 || Noise.unit(seed + 7, x, y, z) >= holes;
		}

		@Override
		public Shape scaled(double horizontal, double vertical) {
			return new Cap(scale(c, horizontal, vertical), r * horizontal, height * vertical, thickness * vertical, rim * vertical, holes);
		}
	}

	// The dome of a canopy tree: overlapping lobes of leaves, a shell thick at the crown and thin at the rim over a hollow
	// underside, and fringes hanging from the rim. Each lobe is x, y, z, radius and height above y.
	final class Canopy implements Shape {
		private final double[][] lobes;
		private final double thick, drip, lump, holes;
		private final long seed;
		private final int minX, minZ, sizeZ;
		private final float[] top, bottom, low;
		private final double[] bounds;

		Canopy(double[][] lobes, double thick, double drip, double lump, double holes, long seed) {
			this.lobes = lobes;
			this.thick = thick;
			this.drip = drip;
			this.lump = lump;
			this.holes = holes;
			this.seed = seed;
			double x0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE, z0 = Double.MAX_VALUE, z1 = -Double.MAX_VALUE;
			for (double[] l : lobes) {
				x0 = Math.min(x0, l[0] - l[3]);
				x1 = Math.max(x1, l[0] + l[3]);
				z0 = Math.min(z0, l[2] - l[3]);
				z1 = Math.max(z1, l[2] + l[3]);
			}
			minX = (int) Math.floor(x0);
			minZ = (int) Math.floor(z0);
			int sizeX = (int) Math.ceil(x1) - minX + 1;
			sizeZ = (int) Math.ceil(z1) - minZ + 1;
			top = new float[sizeX * sizeZ];
			bottom = new float[top.length];
			low = new float[top.length];
			double y0 = Double.MAX_VALUE, y1 = -Double.MAX_VALUE;
			for (int i = 0; i < sizeX; i++) {
				for (int k = 0; k < sizeZ; k++) {
					int index = i * sizeZ + k;
					if (!column(minX + i, minZ + k, index)) {
						top[index] = Float.NaN;
						continue;
					}
					y0 = Math.min(y0, low[index]);
					y1 = Math.max(y1, top[index]);
				}
			}
			bounds = y0 > y1 ? new double[]{0, 0, 0, 0, 0, 0} : new double[]{minX, Math.floor(y0), minZ, minX + sizeX - 1, Math.ceil(y1), minZ + sizeZ - 1};
		}

		private boolean column(int x, int z, int index) {
			double best = -Double.MAX_VALUE, fmax = 0, height = 0;
			for (double[] l : lobes) {
				double dx = x - l[0], dz = z - l[2];
				double f = 1 - (dx * dx + dz * dz) / (l[3] * l[3]);
				if (f > 0) {
					double y = l[1] + l[4] * Math.sqrt(f);
					if (y > best) {
						best = y;
						height = l[4];
					}
					fmax = Math.max(fmax, f);
				}
			}
			if (fmax <= 0.02 + 0.025 * (Noise.smooth(seed + 2, x / 5.0, 0, z / 5.0) + 1)) {
				return false;
			}
			double edge = Math.sqrt(fmax);
			best += height * lump * 0.35 * edge * (0.65 * Noise.smooth(seed, x / 7.0, 0, z / 7.0) + 0.35 * Noise.smooth(seed + 1, x / 3.0, 0, z / 3.0));
			double under = best - (thick * (0.2 + 0.8 * edge) + 1);
			double fine = Noise.unit(seed + 41, x, 0, z);
			double coarse = Noise.unit(seed + 43, Math.floorDiv(x, 2), 0, Math.floorDiv(z, 2));
			double hang = fmax < 0.3 ? drip * Math.max(Math.pow(fine, 5), 0.75 * Math.pow(coarse, 3)) : drip * 0.3 * Math.pow(fine, 6);
			top[index] = (float) best;
			bottom[index] = (float) under;
			low[index] = (float) (under - Math.floor(hang));
			return true;
		}

		// Inside the shell of leaves itself, ignoring drips and holes.
		boolean holds(double x, double y, double z) {
			int i = (int) Math.floor(x) - minX, k = (int) Math.floor(z) - minZ;
			if (i < 0 || k < 0 || k >= sizeZ || i * sizeZ + k >= top.length) {
				return false;
			}
			int index = i * sizeZ + k;
			return !Float.isNaN(top[index]) && y <= top[index] && y >= bottom[index];
		}

		@Override
		public Part part() {
			return Part.LEAVES;
		}

		@Override
		public double[] bounds() {
			return bounds;
		}

		@Override
		public boolean contains(int x, int y, int z, long ignored) {
			int i = x - minX, k = z - minZ;
			if (i < 0 || k < 0 || k >= sizeZ || i * sizeZ + k >= top.length) {
				return false;
			}
			int index = i * sizeZ + k;
			float t = top[index];
			if (Float.isNaN(t) || y > t || y < low[index]) {
				return false;
			}
			if (y < bottom[index] || holes <= 0) {
				return true;
			}
			boolean surface = t - y < 1.5 || y - bottom[index] < 1.5;
			return !surface || Noise.unit(seed + 31, x, y, z) >= holes;
		}

		@Override
		public Shape scaled(double horizontal, double vertical) {
			double[][] scaled = new double[lobes.length][];
			for (int i = 0; i < lobes.length; i++) {
				double[] l = lobes[i];
				scaled[i] = new double[]{l[0] * horizontal, l[1] * vertical, l[2] * horizontal, l[3] * horizontal, l[4] * vertical};
			}
			return new Canopy(scaled, thick * vertical, drip * vertical, lump, holes, seed);
		}
	}
}
