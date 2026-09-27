package com.vexira.mysticalterrain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import terrablender.worldgen.noise.LayeredNoiseUtil;

@Mixin(value = LayeredNoiseUtil.class, remap = false)
public class LayeredNoiseUtilMixin {
	// The climate noises are Large Biomes sized (data/minecraft/worldgen/noise), so TerraBlender's regions zoom twice more
	// to stay in proportion. Otherwise region edges would carve every big biome into several variants.
	@ModifyArg(method = "finalUniqueness", at = @At(value = "INVOKE", target = "Lterrablender/worldgen/noise/LayeredNoiseUtil;createZoomedArea(JILterrablender/worldgen/noise/AreaTransformer0;)Lterrablender/worldgen/noise/Area;"), index = 1)
	private static int mysticalterrain$widerRegions(int zooms) {
		return zooms + 2;
	}
}
