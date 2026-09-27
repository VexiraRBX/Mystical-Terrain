package com.vexira.mysticalterrain.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin {
	@Shadow
	public abstract int minY();

	@Shadow
	public abstract int height();

	// MIN_Y, MAX_Y and Y_SIZE come from the Y bits BlockPos packs, which the bigger world cut from 12 to 10. Codecs all over
	// worldgen use them as plain number ranges (Tectonic's gradients go to ±2048), so they keep vanilla's values and only a
	// dimension's real height is held to what actually packs, below.
	@Redirect(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/core/BlockPos;PACKED_Y_LENGTH:I"))
	private static int mysticalterrain$vanillaYBits() {
		return 12;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void mysticalterrain$checkHeight(CallbackInfo ci) {
		int limit = (1 << BlockPos.PACKED_Y_LENGTH - 1) - 16;
		if (minY() < -limit || minY() + height() > limit) {
			throw new IllegalStateException("Mystical Terrain's bigger world only has room for dimensions between y " + -limit + " and " + (limit - 1));
		}
	}
}
