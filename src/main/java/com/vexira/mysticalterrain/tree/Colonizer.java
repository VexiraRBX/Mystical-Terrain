package com.vexira.mysticalterrain.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

// Space colonization (Runions et al.): every branch tip grows toward the attraction points nearest to it and each point is
// used up once a branch reaches it, so branches spread out to fill the crown instead of following a fixed pattern.
final class Colonizer {
	private final double influence, kill, step, jitter;
	private final V3 tropism;
	private final SplittableRandom random;
	private final Map<Long, List<Integer>> grid = new HashMap<>();
	double[] x = new double[256], y = new double[256], z = new double[256];
	int[] parent = new int[256];
	boolean[] seed = new boolean[256];
	int size;

	Colonizer(double influence, double kill, double step, V3 tropism, double jitter, SplittableRandom random) {
		this.influence = influence;
		this.kill = kill;
		this.step = step;
		this.tropism = tropism;
		this.jitter = jitter;
		this.random = random;
	}

	int seed(V3 p) {
		int i = add(p.x(), p.y(), p.z(), -1);
		seed[i] = true;
		return i;
	}

	V3 at(int i) {
		return new V3(x[i], y[i], z[i]);
	}

	void grow(List<V3> attractors, int iterations, int limit) {
		double[] ax = new double[attractors.size()], ay = new double[ax.length], az = new double[ax.length];
		for (int i = 0; i < ax.length; i++) {
			V3 a = attractors.get(i);
			ax[i] = a.x();
			ay[i] = a.y();
			az[i] = a.z();
		}
		int alive = ax.length;
		boolean[] dead = new boolean[ax.length];
		for (int round = 0; round < iterations && alive > 0 && size < limit; round++) {
			double[] dx = new double[size], dy = new double[size], dz = new double[size];
			int[] pulls = new int[size];
			for (int a = 0; a < ax.length; a++) {
				if (dead[a]) {
					continue;
				}
				int best = nearest(ax[a], ay[a], az[a]);
				if (best < 0) {
					continue;
				}
				double vx = ax[a] - x[best], vy = ay[a] - y[best], vz = az[a] - z[best];
				double d = Math.sqrt(vx * vx + vy * vy + vz * vz);
				if (d < kill) {
					dead[a] = true;
					alive--;
					continue;
				}
				dx[best] += vx / d;
				dy[best] += vy / d;
				dz[best] += vz / d;
				pulls[best]++;
			}
			int grown = 0;
			int before = size;
			for (int i = 0; i < before && size < limit; i++) {
				if (pulls[i] == 0) {
					continue;
				}
				double nx = dx[i] / pulls[i] + tropism.x() + jitter * (random.nextDouble() - 0.5);
				double ny = dy[i] / pulls[i] + tropism.y() + jitter * (random.nextDouble() - 0.5);
				double nz = dz[i] / pulls[i] + tropism.z() + jitter * (random.nextDouble() - 0.5);
				double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (len < 1e-6) {
					continue;
				}
				add(x[i] + nx / len * step, y[i] + ny / len * step, z[i] + nz / len * step, i);
				grown++;
			}
			if (grown == 0) {
				break;
			}
		}
	}

	// Pipe model: a branch is as thick as all the twigs it feeds put together, so taper follows from the branching itself.
	double[] pipes(double exponent) {
		double[] acc = new double[size];
		double[] out = new double[size];
		for (int i = size - 1; i >= 0; i--) {
			out[i] = acc[i] == 0 ? 1 : Math.pow(acc[i], 1 / exponent);
			if (parent[i] >= 0 && !seed[i]) {
				acc[parent[i]] += Math.pow(out[i], exponent);
			}
		}
		return out;
	}

	boolean[] tips() {
		boolean[] tip = new boolean[size];
		Arrays.fill(tip, true);
		for (int i = 0; i < size; i++) {
			if (parent[i] >= 0) {
				tip[parent[i]] = false;
			}
			if (seed[i]) {
				tip[i] = false;
			}
		}
		return tip;
	}

	private int nearest(double px, double py, double pz) {
		int cx = cell(px), cy = cell(py), cz = cell(pz);
		int best = -1;
		double bestD = influence * influence;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					List<Integer> nodes = grid.get(key(cx + i, cy + j, cz + k));
					if (nodes == null) {
						continue;
					}
					for (int n : nodes) {
						double vx = px - x[n], vy = py - y[n], vz = pz - z[n];
						double d = vx * vx + vy * vy + vz * vz;
						if (d < bestD) {
							bestD = d;
							best = n;
						}
					}
				}
			}
		}
		return best;
	}

	private int add(double px, double py, double pz, int from) {
		if (size == x.length) {
			int n = size * 2;
			x = Arrays.copyOf(x, n);
			y = Arrays.copyOf(y, n);
			z = Arrays.copyOf(z, n);
			parent = Arrays.copyOf(parent, n);
			seed = Arrays.copyOf(seed, n);
		}
		x[size] = px;
		y[size] = py;
		z[size] = pz;
		parent[size] = from;
		grid.computeIfAbsent(key(cell(px), cell(py), cell(pz)), k -> new ArrayList<>()).add(size);
		return size++;
	}

	private int cell(double v) {
		return (int) Math.floor(v / influence);
	}

	private static long key(int i, int j, int k) {
		return ((long) i & 0x1FFFFF) << 42 | ((long) j & 0x1FFFFF) << 21 | (long) k & 0x1FFFFF;
	}
}
