package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Entity.class)
public class EntityMixin {
	@ModifyConstant(method = "absSnapTo(DDD)V", constant = {@Constant(doubleValue = 3.0E7), @Constant(doubleValue = -3.0E7)})
	private double mysticalterrain$snapBounds(double limit) {
		return Math.copySign(WorldSize.LIMIT, limit);
	}

	@ModifyConstant(method = "load", constant = {@Constant(doubleValue = 3.0000512E7), @Constant(doubleValue = -3.0000512E7)})
	private double mysticalterrain$loadBounds(double limit) {
		return Math.copySign(WorldSize.LIMIT + 512.0, limit);
	}
}
