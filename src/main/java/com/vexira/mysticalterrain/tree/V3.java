package com.vexira.mysticalterrain.tree;

public record V3(double x, double y, double z) {
	public static final V3 ZERO = new V3(0, 0, 0);
	public static final V3 UP = new V3(0, 1, 0);

	public static V3 of(double azimuth, double elevation) {
		double h = StrictMath.cos(elevation);
		return new V3(StrictMath.cos(azimuth) * h, StrictMath.sin(elevation), StrictMath.sin(azimuth) * h);
	}

	public V3 add(V3 o) {
		return new V3(x + o.x, y + o.y, z + o.z);
	}

	public V3 add(double dx, double dy, double dz) {
		return new V3(x + dx, y + dy, z + dz);
	}

	public V3 sub(V3 o) {
		return new V3(x - o.x, y - o.y, z - o.z);
	}

	public V3 mul(double s) {
		return new V3(x * s, y * s, z * s);
	}

	public double dot(V3 o) {
		return x * o.x + y * o.y + z * o.z;
	}

	public V3 cross(V3 o) {
		return new V3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x);
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public double horizontal() {
		return Math.sqrt(x * x + z * z);
	}

	public double azimuth() {
		return StrictMath.atan2(z, x);
	}

	public V3 normalize() {
		double l = length();
		return l < 1e-9 ? UP : mul(1 / l);
	}

	public V3 lerp(V3 o, double t) {
		return new V3(x + (o.x - x) * t, y + (o.y - y) * t, z + (o.z - z) * t);
	}
}
