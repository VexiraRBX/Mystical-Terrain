package com.vexira.mysticalterrain.formation;

import com.vexira.mysticalterrain.tree.Noise;
import com.vexira.mysticalterrain.tree.V3;

sealed interface Solid permits Solid.Prism, Solid.Tube, Solid.Ellipsoid, Solid.Ring, Solid.Box {
	double TAU = Math.PI * 2;
	V3 X = new V3(1, 0, 0);

	// min x, min y, min z, max x, max y, max z
	double[] bounds();

	// What fills this block, or null when it lies outside.
	Material at(int x, int y, int z, long seed);

	boolean force();

	default int axis() {
		return 1;
	}

	private static int dominant(V3 d) {
		double x = Math.abs(d.x()), y = Math.abs(d.y()), z = Math.abs(d.z());
		return y >= x && y >= z ? 1 : x >= z ? 0 : 2;
	}

	private static double[] span(V3 a, V3 b, double pad) {
		return new double[]{Math.min(a.x(), b.x()) - pad, Math.min(a.y(), b.y()) - pad, Math.min(a.z(), b.z()) - pad,
				Math.max(a.x(), b.x()) + pad, Math.max(a.y(), b.y()) + pad, Math.max(a.z(), b.z()) + pad};
	}

	/**
	 * A crystal: a regular polygon swept from a to b that then narrows to a point over tip blocks. Blocks within shell of the
	 * surface are skin, a thin line down the middle is core and the point is point.
	 */
	final class Prism implements Solid {
		private final V3 a;
		private final V3 dir;
		private final V3 u;
		private final V3 v;
		private final double length;
		private final double r;
		private final int sides;
		private final double twist;
		private final double tip;
		private final double shell;
		private final Material core;
		private final Material body;
		private final Material skin;
		private final Material point;
		private final boolean force;

		Prism(V3 a, V3 b, double r, int sides, double twist, double tip, double shell, Material core, Material body, Material skin,
				Material point, boolean force) {
			this.a = a;
			this.dir = b.sub(a).normalize();
			this.u = dir.cross(Math.abs(dir.y()) > 0.9 ? X : V3.UP).normalize();
			this.v = dir.cross(u);
			this.length = b.sub(a).length();
			this.r = r;
			this.sides = sides;
			this.twist = twist;
			this.tip = tip;
			this.shell = shell;
			this.core = core;
			this.body = body;
			this.skin = skin;
			this.point = point;
			this.force = force;
		}

		@Override
		public double[] bounds() {
			return span(a, a.add(dir.mul(length + tip)), r + 1);
		}

		@Override
		public Material at(int x, int y, int z, long seed) {
			V3 p = new V3(x - a.x(), y - a.y(), z - a.z());
			double t = p.dot(dir);
			if (t < -0.3 || t > length + tip) {
				return null;
			}
			V3 q = p.sub(dir.mul(t));
			double rho = q.length();
			double radius = t <= length ? r : r * (1 - (t - length) / tip);
			if (rho > radius + 0.6) {
				return null;
			}
			double sector = TAU / sides;
			double angle = ((StrictMath.atan2(q.dot(v), q.dot(u)) - twist) % sector + sector) % sector - sector / 2;
			double edge = radius * Math.cos(Math.PI / sides) / Math.cos(angle);
			if (rho > edge + 0.3) {
				return null;
			}
			if (t > length) {
				return point;
			}
			if (edge - rho < shell) {
				return skin;
			}
			return rho < 0.9 ? core : body;
		}

		@Override
		public boolean force() {
			return force;
		}

		@Override
		public int axis() {
			return dominant(dir);
		}
	}

	record Tube(V3 a, V3 b, double ra, double rb, Material material, boolean force) implements Solid {
		@Override
		public double[] bounds() {
			return span(a, b, Math.max(ra, rb) + 1);
		}

		@Override
		public Material at(int x, int y, int z, long seed) {
			double abx = b.x() - a.x(), aby = b.y() - a.y(), abz = b.z() - a.z();
			double len2 = abx * abx + aby * aby + abz * abz;
			double t = len2 < 1e-9 ? 0 : ((x - a.x()) * abx + (y - a.y()) * aby + (z - a.z()) * abz) / len2;
			t = Math.max(0, Math.min(1, t));
			double dx = x - a.x() - abx * t, dy = y - a.y() - aby * t, dz = z - a.z() - abz * t;
			double r = ra + (rb - ra) * t;
			return dx * dx + dy * dy + dz * dz <= r * r + 0.15 ? material : null;
		}

		@Override
		public int axis() {
			return dominant(b.sub(a));
		}
	}

	/**
	 * A lumpy ellipsoid. With a shell only the outer shell blocks get material and the rest gets inside (null leaves it
	 * alone, AIR hollows it). Nothing above maxY is filled, and when cap is set the top layer under that cut gets it.
	 */
	record Ellipsoid(V3 c, double rx, double ry, double rz, double lump, double shell, Material material, Material inside,
			double maxY, Material cap, boolean force) implements Solid {
		@Override
		public double[] bounds() {
			double s = 1 + lump;
			return new double[]{c.x() - rx * s - 1, c.y() - ry * s - 1, c.z() - rz * s - 1,
					c.x() + rx * s + 1, Math.min(c.y() + ry * s + 1, maxY), c.z() + rz * s + 1};
		}

		@Override
		public Material at(int x, int y, int z, long seed) {
			if (y > maxY) {
				return null;
			}
			double dx = (x - c.x()) / rx, dy = (y - c.y()) / ry, dz = (z - c.z()) / rz;
			double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
			double limit = 1 + lump * Noise.smooth(seed + 17, x / 3.5, y / 3.0, z / 3.5);
			if (d > limit) {
				return null;
			}
			if (cap != null && y > maxY - 1) {
				return cap;
			}
			if (shell <= 0) {
				return material;
			}
			return (limit - d) * Math.min(rx, Math.min(ry, rz)) < shell ? material : inside;
		}
	}

	// An open-topped round wall rising from c, its top edge broken down by up to ragged of its height.
	record Ring(V3 c, double outer, double inner, double height, double ragged, Material material, boolean force) implements Solid {
		@Override
		public double[] bounds() {
			return new double[]{c.x() - outer - 1, c.y(), c.z() - outer - 1, c.x() + outer + 1, c.y() + height + 1, c.z() + outer + 1};
		}

		@Override
		public Material at(int x, int y, int z, long seed) {
			double dy = y - c.y();
			if (dy < 0) {
				return null;
			}
			double hx = x - c.x(), hz = z - c.z();
			double rho = Math.sqrt(hx * hx + hz * hz);
			if (rho > outer + 0.3 || rho < inner - 0.3) {
				return null;
			}
			double top = height * (1 - ragged * (0.5 + 0.5 * Noise.smooth(seed + 23, x / 3.0, 0, z / 3.0)));
			return dy <= top ? material : null;
		}
	}

	// Inclusive corners; decay knocks out that share of its blocks at random, like missing floor tiles.
	record Box(int x0, int y0, int z0, int x1, int y1, int z1, double decay, Material material, boolean force) implements Solid {
		@Override
		public double[] bounds() {
			return new double[]{Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1)};
		}

		@Override
		public Material at(int x, int y, int z, long seed) {
			if (x < Math.min(x0, x1) || x > Math.max(x0, x1) || y < Math.min(y0, y1) || y > Math.max(y0, y1)
					|| z < Math.min(z0, z1) || z > Math.max(z0, z1)) {
				return null;
			}
			return decay > 0 && Noise.unit(seed + 31, x, y, z) < decay ? null : material;
		}
	}
}
