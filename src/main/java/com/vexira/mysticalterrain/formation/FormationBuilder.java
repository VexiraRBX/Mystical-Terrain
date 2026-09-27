package com.vexira.mysticalterrain.formation;

import static com.vexira.mysticalterrain.formation.Material.ACCENT;
import static com.vexira.mysticalterrain.formation.Material.AIR;
import static com.vexira.mysticalterrain.formation.Material.BASE;
import static com.vexira.mysticalterrain.formation.Material.BODY;
import static com.vexira.mysticalterrain.formation.Material.LIGHT;
import static com.vexira.mysticalterrain.formation.Material.SHELL;
import static com.vexira.mysticalterrain.formation.Material.TIP;

import com.vexira.mysticalterrain.tree.V3;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

// Coordinates are relative to the base: y 0 is the first free block above the ground (or the chosen height when floating).
public final class FormationBuilder {
	private static final double TAU = Math.PI * 2;
	private static final double INF = Double.POSITIVE_INFINITY;
	// A four-sided prism at this twist has its faces square to the world axes.
	private static final double SQUARE = -Math.PI / 4;

	private final FormationParams p;
	private final SplittableRandom random;
	private final List<Solid> solids = new ArrayList<>();
	private final List<int[]> chests = new ArrayList<>();

	private FormationBuilder(FormationParams p) {
		this.p = p;
		this.random = new SplittableRandom(p.seed());
	}

	public static Formation build(FormationParams params) {
		FormationBuilder b = new FormationBuilder(params);
		switch (params.kind()) {
			case SPIRES -> b.spires();
			case SHARDS -> b.shards();
			case MONOLITHS -> b.monoliths();
			case FOSSIL -> b.fossil();
			case CRATER -> b.crater();
			case RUINS -> b.ruins();
			case SHRINE -> b.shrine();
			case GATE -> b.gate();
			case ISLAND -> b.island();
			case GEODE -> b.geode();
			case STAR -> b.star();
			case REEF -> b.reef();
		}
		return new Formation(b.solids, b.chests, params.seed());
	}

	private void spires() {
		double h = p.size();
		double r = Math.max(1.3, h * 0.075) * p.girth();
		solids.add(new Solid.Ellipsoid(new V3(0, -r * 0.6, 0), r * 2.6, r * 1.4, r * 2.6, 0.35, 0, BASE, null, r * 0.5, null, true));
		int n = count(4, 7);
		double az0 = random.nextDouble() * TAU;
		for (int i = 0; i < n; i++) {
			boolean main = i == 0;
			double hi = main ? h : h * range(0.3, 0.8);
			double ri = main ? r : Math.max(1, r * Math.pow(hi / h, 0.8) * range(0.8, 1.1));
			double az = main ? az0 : az0 + i * TAU / (n - 1) + range(-0.4, 0.4);
			double tilt = p.lean() * (main ? range(0, 0.25) : range(0.3, 0.8));
			double off = main ? 0 : r * range(0.8, 1.8);
			V3 start = new V3(Math.cos(az) * off, -ri * 1.5, Math.sin(az) * off);
			crystal(start, tilted(az, tilt), hi * 0.8 + ri * 1.5, ri, hi * 0.2 + ri);
		}
		int small = 3 + random.nextInt(4) + (int) (h / 15);
		for (int i = 0; i < small; i++) {
			double az = random.nextDouble() * TAU;
			double dist = r * range(1.8, 3.2);
			double hs = range(3, 4 + h * 0.12);
			crystal(new V3(Math.cos(az) * dist, -1, Math.sin(az) * dist), tilted(az, range(0.3, 0.9)), hs * 0.7, Math.max(0.7, hs * 0.12), hs * 0.3 + 1);
		}
	}

	// Great crystals that have fallen and lie half sunk in the ground, some with their points snapped off.
	private void shards() {
		double len = p.size();
		int n = count(2, 5);
		for (int i = 0; i < n; i++) {
			double l = len * range(0.5, 1);
			double r = Math.max(1.1, l * 0.14) * p.girth();
			double az = random.nextDouble() * TAU;
			double spot = random.nextDouble() * TAU;
			double off = len * 0.3 * random.nextDouble();
			V3 start = new V3(Math.cos(spot) * off, -r * 0.7, Math.sin(spot) * off);
			double tip = random.nextDouble() < 0.3 ? 0 : r * 1.8;
			solids.add(new Solid.Prism(start, start.add(tilted(az, range(1.05, 1.45)).mul(l)), r, p.sides(), random.nextDouble() * TAU, tip, 1,
					LIGHT, BODY, SHELL, TIP, true));
		}
		int upright = 2 + random.nextInt(4);
		for (int i = 0; i < upright; i++) {
			double az = random.nextDouble() * TAU;
			double dist = len * range(0.2, 0.6);
			double hs = range(2, 3 + len * 0.2);
			crystal(new V3(Math.cos(az) * dist, -1, Math.sin(az) * dist), tilted(az, range(0.1, 0.6)), hs * 0.7, Math.max(0.7, hs * 0.13), hs * 0.3 + 1);
		}
		scatter(BASE, 3 + random.nextInt(5), len * 0.2, len * 0.8, 0.7, 1.6);
	}

	// Obelisks standing in a ring on a paved floor, around a pool and an altar with a chest on it.
	private void monoliths() {
		double ring = p.size();
		int n = count(5, 8);
		double h = ring * range(1.4, 2.0);
		double w = Math.max(1, ring * 0.12) * p.girth();
		solids.add(new Solid.Ellipsoid(new V3(0, -1, 0), ring + 3, 1.6, ring + 3, 0.15, 0, BASE, null, -1, null, true));
		solids.add(new Solid.Ellipsoid(new V3(0, -1, 0), 3.5, 1.5, 3.5, 0, 0, ACCENT, null, -1, null, true));
		solids.add(new Solid.Box(-1, 0, -1, 1, 0, 1, 0, BODY, true));
		chests.add(new int[]{0, 1, 0});
		double az0 = random.nextDouble() * TAU;
		for (int i = 0; i < n; i++) {
			double az = az0 + i * TAU / n + range(-0.15, 0.15);
			V3 foot = new V3(Math.cos(az) * ring, 0, Math.sin(az) * ring);
			double hi = h * range(0.75, 1);
			if (random.nextDouble() < 0.2) {
				V3 start = foot.add(0, w * 0.6, 0);
				V3 outward = new V3(Math.cos(az), 0, Math.sin(az));
				solids.add(new Solid.Prism(start, start.add(outward.mul(hi * 0.8)), w, 4, SQUARE, w * 2, 0.8, BODY, BODY, SHELL, TIP, true));
				continue;
			}
			solids.add(new Solid.Prism(foot.add(0, -3, 0), foot.add(0, hi, 0), w, 4, SQUARE, w * 2, 0.8, BODY, BODY, SHELL, TIP, true));
		}
	}

	// The skeleton of something enormous: an arched spine, ribs curling into the ground, a horned skull at the front.
	private void fossil() {
		double len = p.size();
		double s = Math.max(1.4, len * 0.07) * p.girth();
		double heading = random.nextDouble() * TAU;
		V3 fwd = new V3(Math.cos(heading), 0, Math.sin(heading));
		V3 side = new V3(-fwd.z(), 0, fwd.x());
		double sway = range(-1, 1) * len * 0.08;
		double phase = random.nextDouble() * TAU;
		int bones = Math.max(6, (int) (len / (s * 1.6)));
		List<V3> spine = new ArrayList<>();
		for (int i = 0; i <= bones; i++) {
			double t = i / (double) bones;
			double lateral = Math.sin(t * Math.PI * 0.9 + phase) * sway;
			double up = -s * 0.3 + Math.sin(t * Math.PI) * s * 1.6;
			spine.add(fwd.mul((t - 0.5) * len).add(side.mul(lateral)).add(0, up, 0));
		}
		for (int i = 0; i <= bones; i++) {
			double taper = Math.min(1, 0.5 + i / (bones * 0.5));
			V3 c = spine.get(i);
			solids.add(new Solid.Ellipsoid(c, s * taper, s * 0.9 * taper, s * taper, 0.15, 0, i % 3 == 0 ? ACCENT : BODY, null, INF, null, false));
			if (i > 0) {
				solids.add(new Solid.Tube(spine.get(i - 1), c, s * 0.45 * taper, s * 0.45 * taper, BODY, false));
			}
		}
		for (int i = (int) (bones * 0.3); i <= (int) (bones * 0.75); i++) {
			double t = i / (double) bones;
			double rr = s * range(3.2, 4.2) * (1 - Math.abs(t - 0.52) * 1.2);
			V3 centre = spine.get(i).add(0, -rr * 0.35, 0);
			for (int sign = -1; sign <= 1; sign += 2) {
				V3 last = null;
				for (int k = 0; k <= 6; k++) {
					double phi = Math.PI * (0.15 + 0.8 * k / 6.0);
					V3 pt = centre.add(side.mul(sign * rr * Math.sin(phi) * 1.1)).add(0, rr * Math.cos(phi), 0);
					if (last != null) {
						solids.add(new Solid.Tube(last, pt, s * 0.32, s * 0.26, BODY, false));
					}
					last = pt;
				}
			}
		}
		V3 head = spine.get(bones).add(fwd.mul(s * 2.2)).add(0, s * 0.4, 0);
		solids.add(new Solid.Ellipsoid(head, s * 2.4, s * 1.7, s * 1.9, 0.12, 1.3, BODY, null, INF, null, false));
		solids.add(new Solid.Tube(head.add(fwd.mul(-s)).add(0, -s * 0.9, 0), head.add(fwd.mul(s * 3.2)).add(0, -s * 1.4, 0), s * 0.5, s * 0.35, BODY, false));
		for (int sign = -1; sign <= 1; sign += 2) {
			V3 eye = head.add(fwd.mul(s * 0.9)).add(side.mul(sign * s * 1.3)).add(0, s * 0.5, 0);
			solids.add(new Solid.Ellipsoid(eye, s * 0.6, s * 0.55, s * 0.6, 0, 0, AIR, null, INF, null, true));
			V3 root = head.add(fwd.mul(-s * 0.8)).add(side.mul(sign * s * 1.2)).add(0, s * 1.2, 0);
			V3 bend = root.add(fwd.mul(-s * 1.8)).add(side.mul(sign * s * 1.2)).add(0, s * 1.6, 0);
			V3 end = bend.add(fwd.mul(-s * 2.2)).add(side.mul(sign * s * 0.3)).add(0, s * 0.2, 0);
			solids.add(new Solid.Tube(root, bend, s * 0.5, s * 0.35, ACCENT, false));
			solids.add(new Solid.Tube(bend, end, s * 0.35, 0.5, ACCENT, false));
		}
	}

	// A scorched bowl with the meteor still sitting in the bottom and its ejecta thrown around the rim.
	private void crater() {
		double r = p.size();
		double depth = r * range(0.35, 0.5);
		double lift = r * 0.3;
		V3 c = new V3(0, lift, 0);
		solids.add(new Solid.Ellipsoid(c, r, lift + depth, r, 0.12, 0, AIR, null, INF, null, true));
		solids.add(new Solid.Ellipsoid(c, r + 1.5, lift + depth + 1.5, r + 1.5, 0.12, 2, BASE, null, -1, null, true));
		double mr = Math.max(1.5, r * 0.22) * p.girth();
		V3 rock = new V3(range(-0.2, 0.2) * r, -depth + mr * 0.5, range(-0.2, 0.2) * r);
		solids.add(new Solid.Ellipsoid(rock, mr, mr * 0.85, mr, 0.35, 0, BODY, null, INF, null, true));
		scatter(BASE, 8 + (int) r, r * 0.9, r * 1.3, r * 0.06 + 0.8, r * 0.14 + 0.8);
		scatter(BODY, 2 + random.nextInt(4), r * 1.3, r * 2, 0.8, 1.4);
	}

	// A broken floor with a tower or two, a line of columns and one that has fallen. The first tower keeps a chest.
	private void ruins() {
		int s = p.size();
		solids.add(new Solid.Box(-s, -1, -s, s, -1, s, 0.3, BASE, true));
		int towers = count(1, 3);
		for (int i = 0; i < towers; i++) {
			double az = random.nextDouble() * TAU;
			double d = i == 0 ? 0 : s * range(0.4, 0.7);
			int cx = (int) Math.round(Math.cos(az) * d), cz = (int) Math.round(Math.sin(az) * d);
			V3 c = new V3(cx, -1, cz);
			double tr = range(2.5, 4.5) * p.girth();
			solids.add(new Solid.Ring(c, tr, tr - 1.3, s * range(0.8, 1.6), 0.55, BODY, true));
			solids.add(new Solid.Ellipsoid(c, tr - 0.6, 0.6, tr - 0.6, 0, 0, BASE, null, -1, null, true));
			int door = (int) Math.round(tr);
			solids.add(new Solid.Box(cx + door - 2, 0, cz - 1, cx + door + 1, 2, cz + 1, 0, AIR, true));
			if (i == 0) {
				chests.add(new int[]{cx, 0, cz});
			}
		}
		int columns = 4 + random.nextInt(5);
		double heading = random.nextDouble() * TAU;
		double cos = Math.cos(heading), sin = Math.sin(heading);
		for (int i = 0; i < columns; i++) {
			double along = (i - (columns - 1) / 2.0) * 4;
			int x = (int) Math.round(cos * along + sin * s * 0.6);
			int z = (int) Math.round(sin * along - cos * s * 0.6);
			int top = random.nextDouble() < 0.35 ? 1 + random.nextInt(3) : (int) (s * range(0.5, 0.9)) + 3;
			solids.add(new Solid.Box(x, -1, z, x + 1, top, z + 1, 0, ACCENT, true));
			if (top > 4) {
				solids.add(new Solid.Box(x, top + 1, z, x, top + 1, z, 0, LIGHT, false));
			}
		}
		int fx = (int) Math.round(-sin * s * 0.5), fz = (int) Math.round(cos * s * 0.5);
		int fallen = (int) (s * range(0.5, 0.8)) + 3;
		if (random.nextBoolean()) {
			solids.add(new Solid.Box(fx, 0, fz, fx + fallen, 1, fz + 1, 0, ACCENT, false));
		} else {
			solids.add(new Solid.Box(fx, 0, fz, fx + 1, 1, fz + fallen, 0, ACCENT, false));
		}
	}

	// A raised octagonal sanctum: pillars crowned with crystals, an altar with a chest, and a crystal floating above it.
	private void shrine() {
		double s = p.size();
		solids.add(new Solid.Prism(new V3(0, -4, 0), new V3(0, -1, 0), s + 3, 8, Math.PI / 8, 0, 0, BASE, BASE, BASE, BASE, true));
		solids.add(new Solid.Prism(new V3(0, -4, 0), new V3(0, 0, 0), s + 1.5, 8, Math.PI / 8, 0, 0, BASE, BASE, BASE, BASE, true));
		solids.add(new Solid.Prism(new V3(0, -1, 0), new V3(0, 0, 0), s * 0.45, 8, Math.PI / 8, 0, 0, ACCENT, ACCENT, ACCENT, ACCENT, true));
		int pillars = count(6, 8);
		double width = s > 7 ? 1.2 : 0.8;
		for (int i = 0; i < pillars; i++) {
			double az = (i + 0.5) * TAU / pillars;
			int x = (int) Math.round(Math.cos(az) * (s - 0.5)), z = (int) Math.round(Math.sin(az) * (s - 0.5));
			int top = (int) (s * range(0.9, 1.2)) + 3;
			solids.add(new Solid.Prism(new V3(x, 1, z), new V3(x, top, z), width, 4, SQUARE, 0, 0, BODY, BODY, BODY, BODY, true));
			solids.add(new Solid.Prism(new V3(x, top + 1, z), new V3(x, top + 2, z), 0.9, 6, random.nextDouble() * TAU, 2.5, 1, TIP, TIP, TIP, TIP, false));
		}
		solids.add(new Solid.Box(-1, 1, -1, 1, 1, 1, 0, ACCENT, true));
		chests.add(new int[]{0, 2, 0});
		double y = 6 + s * 0.3;
		double twist = random.nextDouble() * TAU;
		solids.add(new Solid.Prism(new V3(0, y, 0), new V3(0, y + s * 0.5, 0), 1.3, 6, twist, 3, 1, LIGHT, SHELL, SHELL, TIP, false));
		solids.add(new Solid.Prism(new V3(0, y + 0.5, 0), new V3(0, y - 0.5, 0), 1.3, 6, twist, 3, 1, LIGHT, SHELL, SHELL, TIP, false));
	}

	// Two pillars and the arch between them, with a light hanging under the keystone.
	private void gate() {
		double half = p.size();
		double h = half * range(1.5, 2.1);
		double w = Math.max(1, half * 0.2) * p.girth();
		boolean alongZ = random.nextBoolean();
		int ends = (int) half + 3;
		solids.add(alongZ ? new Solid.Box(-3, -1, -ends, 3, -1, ends, 0.15, BASE, true) : new Solid.Box(-ends, -1, -3, ends, -1, 3, 0.15, BASE, true));
		for (int sign = -1; sign <= 1; sign += 2) {
			solids.add(new Solid.Prism(across(sign * half, -3, alongZ), across(sign * half, h, alongZ), w + 0.4, 4, SQUARE, 0, 1,
					BODY, BODY, SHELL, SHELL, true));
		}
		V3 last = null;
		for (int k = 0; k <= 12; k++) {
			double phi = Math.PI * k / 12;
			V3 pt = across(half * Math.cos(phi), h + half * 0.8 * Math.sin(phi), alongZ);
			if (last != null) {
				solids.add(new Solid.Tube(last, pt, w, w, BODY, true));
			}
			last = pt;
		}
		double crown = h + half * 0.8;
		solids.add(new Solid.Ellipsoid(new V3(0, crown, 0), w * 1.4, w * 1.4, w * 1.4, 0.1, 0, ACCENT, null, INF, null, true));
		// A stub down from the keystone, so whatever hangs from it always has a block to hang from.
		int hang = (int) Math.floor(crown - w * 1.4) - 1;
		solids.add(new Solid.Box(0, hang + 1, 0, 0, (int) Math.floor(crown), 0, 0, ACCENT, true));
		solids.add(new Solid.Box(0, hang, 0, 0, hang, 0, 0, LIGHT, false));
		scatter(BASE, 3 + random.nextInt(4), half * 0.5, half * 1.5, 0.7, 1.5);
	}

	// A chunk of land adrift in the void, with glowing drips hanging under it and a few crystals on top.
	private void island() {
		double r = p.size();
		double t = r * range(0.9, 1.4);
		solids.add(new Solid.Ellipsoid(V3.ZERO, r, t, r * range(0.8, 1.2), 0.35, 0, BODY, null, 0, SHELL, false));
		int drips = 3 + random.nextInt(5);
		for (int i = 0; i < drips; i++) {
			double az = random.nextDouble() * TAU;
			double d = r * 0.55 * random.nextDouble();
			V3 top = new V3(Math.cos(az) * d, -t * 0.45, Math.sin(az) * d);
			V3 end = top.add(0, -range(2, 2 + r * 0.8), 0);
			solids.add(new Solid.Tube(top, end, range(1, 1.8), 0.4, BODY, false));
			if (random.nextBoolean()) {
				solids.add(new Solid.Ellipsoid(end, 0.8, 0.8, 0.8, 0, 0, LIGHT, null, INF, null, false));
			}
		}
		int crystals = random.nextInt(4);
		for (int i = 0; i < crystals; i++) {
			double az = random.nextDouble() * TAU;
			double d = r * 0.6 * random.nextDouble();
			double hs = range(2, 5);
			crystal(new V3(Math.cos(az) * d, 0, Math.sin(az) * d), tilted(az, range(0, 0.5)), hs * 0.7, 0.8, hs * 0.3 + 1);
		}
	}

	// A hollow floating geode in layers, cracked open on one side, with a crystal hanging in the hollow of the big ones.
	private void geode() {
		double r = p.size();
		solids.add(new Solid.Ellipsoid(V3.ZERO, r, r * 0.92, r, 0.1, 1.2, BASE, null, INF, null, true));
		solids.add(new Solid.Ellipsoid(V3.ZERO, r - 1.2, (r - 1.2) * 0.92, r - 1.2, 0.1, 1, SHELL, null, INF, null, true));
		solids.add(new Solid.Ellipsoid(V3.ZERO, r - 2.2, (r - 2.2) * 0.92, r - 2.2, 0.1, 1, BODY, AIR, INF, null, true));
		V3 out = V3.of(random.nextDouble() * TAU, range(-0.2, 0.6)).mul(r * 1.3);
		solids.add(new Solid.Tube(out, out.mul(0.25), r * 0.45, r * 0.3, AIR, true));
		if (r >= 5) {
			double twist = random.nextDouble() * TAU;
			solids.add(new Solid.Prism(new V3(0, -0.5, 0), new V3(0, 0.5, 0), 0.9, 6, twist, r * 0.35, 1, TIP, TIP, TIP, TIP, false));
			solids.add(new Solid.Prism(new V3(0, 0.5, 0), new V3(0, -0.5, 0), 0.9, 6, twist, r * 0.35, 1, TIP, TIP, TIP, TIP, false));
		}
	}

	// A little star: a glowing core in a glass halo, with rays, sometimes a ring and a couple of moons.
	private void star() {
		double r = p.size() * p.girth();
		solids.add(new Solid.Ellipsoid(V3.ZERO, r, r, r, 0.2, 0, LIGHT, null, INF, null, false));
		solids.add(new Solid.Ellipsoid(V3.ZERO, r + 1.6, r + 1.6, r + 1.6, 0.2, 1, SHELL, null, INF, null, false));
		int rays = 4 + random.nextInt(4);
		for (int i = 0; i < rays; i++) {
			V3 dir = V3.of(random.nextDouble() * TAU, range(-1.2, 1.2));
			solids.add(new Solid.Tube(dir.mul(r), dir.mul(r * range(2.2, 3.6) + 2), 0.9, 0.3, TIP, false));
		}
		if (random.nextBoolean()) {
			V3 normal = V3.of(random.nextDouble() * TAU, range(0.6, 1.4));
			V3 u = normal.cross(Math.abs(normal.y()) > 0.9 ? new V3(1, 0, 0) : V3.UP).normalize();
			V3 v = normal.cross(u);
			double orbit = r * 2.6 + 1;
			int beads = (int) (orbit * 3);
			for (int k = 0; k < beads; k++) {
				double a = TAU * k / beads;
				solids.add(new Solid.Ellipsoid(u.mul(Math.cos(a) * orbit).add(v.mul(Math.sin(a) * orbit)), 0.7, 0.7, 0.7, 0, 0, ACCENT, null, INF, null, false));
			}
		}
		int moons = random.nextInt(4);
		for (int i = 0; i < moons; i++) {
			double mr = range(0.8, 1.4);
			solids.add(new Solid.Ellipsoid(V3.of(random.nextDouble() * TAU, range(-1, 1)).mul(r * range(3, 5) + 2), mr, mr, mr, 0.2, 0, LIGHT, null, INF, null, false));
		}
	}

	// Branching stalks like a dried-out coral, every tip ending in a bulb.
	private void reef() {
		double h = p.size();
		solids.add(new Solid.Ellipsoid(new V3(0, -1, 0), h * 0.35 + 1, h * 0.2 + 1, h * 0.35 + 1, 0.3, 0, BASE, null, 0, null, true));
		int stalks = count(3, 6);
		for (int i = 0; i < stalks; i++) {
			double az = random.nextDouble() * TAU;
			V3 start = new V3(Math.cos(az) * h * 0.12, -1, Math.sin(az) * h * 0.12);
			branch(start, V3.of(az, range(1, 1.4)), h * range(0.35, 0.55), Math.max(0.8, h * 0.06) * p.girth(), 0);
		}
	}

	private void branch(V3 start, V3 dir, double len, double r, int depth) {
		V3 end = start.add(dir.mul(len));
		solids.add(new Solid.Tube(start, end, r, r * 0.75, BODY, true));
		if (depth < 2 && len > 3) {
			int kids = random.nextDouble() < 0.3 ? 3 : 2;
			double elevation = Math.asin(Math.max(-1, Math.min(1, dir.y())));
			for (int k = 0; k < kids; k++) {
				V3 child = V3.of(dir.azimuth() + range(-1.2, 1.2), Math.max(0.3, Math.min(1.5, elevation + range(-0.3, 0.25))));
				branch(end, child, len * range(0.55, 0.75), Math.max(0.6, r * 0.7), depth + 1);
			}
			return;
		}
		double bulb = range(1.1, 2.1);
		solids.add(new Solid.Ellipsoid(end, bulb, bulb, bulb, 0.2, 0, random.nextBoolean() ? LIGHT : ACCENT, null, INF, null, false));
	}

	private void crystal(V3 start, V3 dir, double length, double r, double tip) {
		solids.add(new Solid.Prism(start, start.add(dir.mul(length)), r, p.sides(), random.nextDouble() * TAU, tip, 1, LIGHT, BODY, SHELL, TIP, true));
	}

	private void scatter(Material material, int n, double near, double far, double small, double big) {
		for (int i = 0; i < n; i++) {
			double az = random.nextDouble() * TAU;
			double d = range(near, far);
			double size = range(small, big);
			solids.add(new Solid.Ellipsoid(new V3(Math.cos(az) * d, range(-0.5, 0.5), Math.sin(az) * d), size * 1.3, size, size * 1.3, 0.3, 0, material,
					null, INF, null, false));
		}
	}

	// Leaning away from vertical by tilt radians, toward the azimuth az.
	private static V3 tilted(double az, double tilt) {
		return new V3(Math.cos(az) * Math.sin(tilt), Math.cos(tilt), Math.sin(az) * Math.sin(tilt));
	}

	private static V3 across(double offset, double y, boolean alongZ) {
		return alongZ ? new V3(0, y, offset) : new V3(offset, y, 0);
	}

	private int count(int lo, int hi) {
		return p.count() >= 0 ? p.count() : lo + random.nextInt(hi - lo + 1);
	}

	private double range(double lo, double hi) {
		return lo + random.nextDouble() * (hi - lo);
	}
}
