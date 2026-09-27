package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// max-world-size: its default, and the lambda in the constructor that clamps whatever server.properties says.
@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 29999984))
	private int mysticalterrain$defaultWorldSize(int size) {
		return WorldSize.BORDER_RADIUS;
	}

	@ModifyConstant(method = "method_16715", constant = @Constant(intValue = 29999984))
	private static int mysticalterrain$maxWorldSize(int size) {
		return WorldSize.BORDER_RADIUS;
	}

	// Existing server.properties files already have vanilla's default written out; treat it as the new default.
	@ModifyArg(method = "method_16715", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"), index = 0)
	private static int mysticalterrain$upgradeVanillaDefault(int size) {
		return size == 29999984 ? WorldSize.BORDER_RADIUS : size;
	}
}
