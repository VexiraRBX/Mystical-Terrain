package com.vexira.mysticalterrain.mixin;

import com.vexira.mysticalterrain.WorldSize;
import net.minecraft.server.commands.ForceLoadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ForceLoadCommand.class)
public class ForceLoadCommandMixin {
	@ModifyConstant(method = "changeForceLoad", constant = {@Constant(intValue = 30000000), @Constant(intValue = -30000000)})
	private static int mysticalterrain$bounds(int limit) {
		return limit < 0 ? -WorldSize.LIMIT : WorldSize.LIMIT;
	}
}
