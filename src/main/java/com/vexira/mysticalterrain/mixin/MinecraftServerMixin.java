package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@ModifyConstant(method = "getAbsoluteMaxWorldSize", constant = @Constant(intValue = 29999984))
	private int mysticalterrain$maxWorldSize(int size) {
		return WorldSize.BORDER_RADIUS;
	}
}
