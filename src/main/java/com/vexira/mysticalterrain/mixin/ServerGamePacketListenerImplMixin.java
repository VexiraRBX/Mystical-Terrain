package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@ModifyConstant(method = "clampHorizontal", constant = {@Constant(doubleValue = 3.0E7), @Constant(doubleValue = -3.0E7)})
	private static double mysticalterrain$moveBounds(double limit) {
		return Math.copySign(WorldSize.LIMIT, limit);
	}
}
