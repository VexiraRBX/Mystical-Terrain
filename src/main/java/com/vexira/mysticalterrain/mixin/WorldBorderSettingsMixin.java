package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// The border a new world starts with, and how far out a saved border's centre may be (a lambda building the codec).
@Mixin(WorldBorder.Settings.class)
public class WorldBorderSettingsMixin {
	@ModifyConstant(method = "<clinit>", constant = @Constant(doubleValue = 5.9999968E7))
	private static double mysticalterrain$defaultSize(double size) {
		return WorldSize.BORDER_SIZE;
	}

	@ModifyConstant(method = "method_74150", constant = {@Constant(doubleValue = 2.9999984E7), @Constant(doubleValue = -2.9999984E7)})
	private static double mysticalterrain$centerBounds(double limit) {
		return Math.copySign(WorldSize.BORDER_RADIUS, limit);
	}
}
