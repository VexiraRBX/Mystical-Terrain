package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelReader {
	@Shadow
	private static boolean isInWorldBoundsHorizontal(BlockPos pos) {
		throw new AssertionError();
	}

	@ModifyConstant(method = "isInWorldBoundsHorizontal", constant = {@Constant(intValue = 30000000), @Constant(intValue = -30000000)})
	private static int mysticalterrain$bounds(int limit) {
		return limit < 0 ? -WorldSize.LIMIT : WorldSize.LIMIT;
	}

	@ModifyConstant(method = "getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I", constant = {@Constant(intValue = 30000000), @Constant(intValue = -30000000)})
	private int mysticalterrain$heightBounds(int limit) {
		return limit < 0 ? -WorldSize.LIMIT : WorldSize.LIMIT;
	}

	// LevelReader's default reports full light past ±30M, which stops monsters spawning and saplings needing light out there.
	@Override
	public int getMaxLocalRawBrightness(BlockPos pos, int darkening) {
		return isInWorldBoundsHorizontal(pos) ? getRawBrightness(pos, darkening) : 15;
	}
}
