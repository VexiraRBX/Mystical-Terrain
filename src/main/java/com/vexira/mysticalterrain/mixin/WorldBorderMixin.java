package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldBorder.class)
public class WorldBorderMixin {
	@ModifyConstant(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder$Settings;)V", constant = @Constant(intValue = 29999984))
	private int mysticalterrain$absoluteMaxSize(int size) {
		return WorldSize.BORDER_RADIUS;
	}

	@ModifyConstant(method = "<init>(Lnet/minecraft/world/level/border/WorldBorder$Settings;)V", constant = @Constant(doubleValue = 5.9999968E7))
	private double mysticalterrain$initialSize(double size) {
		return WorldSize.BORDER_SIZE;
	}

	// A world saved with vanilla's full-size border (so one nobody resized) gets the new full size instead.
	@ModifyArg(method = "applyInitialSettings", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;setSize(D)V"))
	private double mysticalterrain$upgradeSavedSize(double size) {
		return size == 5.9999968E7 ? WorldSize.BORDER_SIZE : size;
	}
}
