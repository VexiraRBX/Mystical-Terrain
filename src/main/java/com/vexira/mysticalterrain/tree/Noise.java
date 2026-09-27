package com.vexira.mysticalterrain.tree;

public final class Noise {
	private Noise() {
	}

	static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
		z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
		return z ^ (z >>> 31);
	}

	public static long hash(long seed, int x, int y, int z) {
		return mix(seed ^ mix(x * 0x9E3779B97F4A7C15L ^ mix(y * 0xC2B2AE3D27D4EB4FL ^ mix(z * 0x165667B19E3779F9L))));
	}

	public static double unit(long seed, int x, int y, int z) {
		return (hash(seed, x, y, z) >>> 11) * 0x1.0p-53;
	}

	// Smooth value noise in [-1, 1].
	public static double smooth(long seed, double x, double y, double z) {
		int ix = (int) Math.floor(x);
		int iy = (int) Math.floor(y);
		int iz = (int) Math.floor(z);
		double fx = fade(x - ix);
		double fy = fade(y - iy);
		double fz = fade(z - iz);
		double x00 = lerp(fx, unit(seed, ix, iy, iz), unit(seed, ix + 1, iy, iz));
		double x10 = lerp(fx, unit(seed, ix, iy + 1, iz), unit(seed, ix + 1, iy + 1, iz));
		double x01 = lerp(fx, unit(seed, ix, iy, iz + 1), unit(seed, ix + 1, iy, iz + 1));
		double x11 = lerp(fx, unit(seed, ix, iy + 1, iz + 1), unit(seed, ix + 1, iy + 1, iz + 1));
		return lerp(fz, lerp(fy, x00, x10), lerp(fy, x01, x11)) * 2 - 1;
	}

	private static double fade(double t) {
		return t * t * (3 - 2 * t);
	}

	private static double lerp(double t, double a, double b) {
		return a + (b - a) * t;
	}
}
