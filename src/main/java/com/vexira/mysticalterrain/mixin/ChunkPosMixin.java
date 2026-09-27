package com.vexira.mysticalterrain.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChunkPos.class)
public class ChunkPosMixin {
	// INVALID_CHUNK_POS sits just past vanilla's edge, which is now a real chunk. Move it past ours.
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 1875066))
	private static int mysticalterrain$invalidChunk(int chunk) {
		return 1 << 22;
	}
}
