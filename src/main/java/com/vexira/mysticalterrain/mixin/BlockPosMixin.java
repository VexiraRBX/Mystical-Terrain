package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockPos.class)
public class BlockPosMixin {
	// The packed layout is sized from this: 67M needs 27 bits for X and Z, which leaves 10 for Y (-512 to 511).
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 30000000))
	private static int mysticalterrain$packWider(int limit) {
		return WorldSize.LIMIT;
	}
}
