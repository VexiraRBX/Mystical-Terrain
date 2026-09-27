package com.vexira.mysticalterrain.mixin;

import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// Vanilla packs sections as 22 bits X, 22 bits Z and 20 bits Y, which stops at ±33.5M blocks.
// This uses 23/23/18: X in the top 23 bits, Z below it, Y in the low 18.
@Mixin(SectionPos.class)
public class SectionPosMixin {
	/**
	 * @author Vexira
	 * @reason Wider X and Z for the bigger world.
	 */
	@Overwrite
	public static long asLong(int x, int y, int z) {
		return (x & 0x7FFFFFL) << 41 | (z & 0x7FFFFFL) << 18 | y & 0x3FFFFL;
	}

	/**
	 * @author Vexira
	 * @reason Matches {@link #asLong(int, int, int)}.
	 */
	@Overwrite
	public static int x(long packed) {
		return (int) (packed >> 41);
	}

	/**
	 * @author Vexira
	 * @reason Matches {@link #asLong(int, int, int)}.
	 */
	@Overwrite
	public static int y(long packed) {
		return (int) (packed << 46 >> 46);
	}

	/**
	 * @author Vexira
	 * @reason Matches {@link #asLong(int, int, int)}.
	 */
	@Overwrite
	public static int z(long packed) {
		return (int) (packed << 23 >> 41);
	}

	/**
	 * @author Vexira
	 * @reason Matches {@link #asLong(int, int, int)}.
	 */
	@Overwrite
	public static long getZeroNode(long packed) {
		return packed & ~0x3FFFFL;
	}
}
