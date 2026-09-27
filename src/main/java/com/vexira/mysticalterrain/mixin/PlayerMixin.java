package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Player.class)
public class PlayerMixin {
	@ModifyConstant(method = "tick", constant = {@Constant(doubleValue = 2.9999999E7), @Constant(doubleValue = -2.9999999E7)})
	private double mysticalterrain$clampBounds(double limit) {
		return Math.copySign(WorldSize.LIMIT - 1.0, limit);
	}
}
