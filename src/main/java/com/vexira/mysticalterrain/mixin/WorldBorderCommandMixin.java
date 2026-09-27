package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.server.commands.WorldBorderCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Argument ranges, the checks in /worldborder center and set, and the numbers in their error messages.
@Mixin(WorldBorderCommand.class)
public class WorldBorderCommandMixin {
	@ModifyConstant(method = {"register", "setSize", "<clinit>"}, constant = {@Constant(doubleValue = 5.9999968E7), @Constant(doubleValue = -5.9999968E7)})
	private static double mysticalterrain$maxSize(double size) {
		return Math.copySign(WorldSize.BORDER_SIZE, size);
	}

	@ModifyConstant(method = {"setCenter", "<clinit>"}, constant = @Constant(doubleValue = 2.9999984E7))
	private static double mysticalterrain$maxCenter(double center) {
		return WorldSize.BORDER_RADIUS;
	}
}
