package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.vexira.mysticalterrain.tree.Noise;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * Surface blocks for dimensions whose surface rules we can't add to, like the End. Runs once per chunk and paints every
 * exposed top of its biome, so the undersides of floating islands and the faces of cliffs keep their stone.
 */
public class GroundCoverFeature extends Feature<GroundCoverConfiguration> {
	public GroundCoverFeature(Codec<GroundCoverConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<GroundCoverConfiguration> context) {
		WorldGenLevel level = context.level();
		GroundCoverConfiguration config = context.config();
		RandomSource random = context.random();
		ChunkPos chunk = new ChunkPos(context.origin());
		long seed = level.getSeed();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		boolean painted = false;
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				boolean open = true;
				int under = 0;
				for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1; y > level.getMinY(); y--) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) {
						open = true;
						under = 0;
						continue;
					}
					boolean replaceable = state.is(config.replaceable());
					if (open && replaceable && level.getBiome(pos).is(config.biome())) {
						level.setBlock(pos, top(config, seed, x, z).getState(random, pos), Block.UPDATE_CLIENTS);
						under = config.depth();
						painted = true;
					} else if (under > 0 && replaceable) {
						level.setBlock(pos, config.under().getState(random, pos), Block.UPDATE_CLIENTS);
						under--;
					} else {
						under = 0;
					}
					open = false;
				}
			}
		}
		return painted;
	}

	private static BlockStateProvider top(GroundCoverConfiguration config, long seed, int x, int z) {
		if (config.accent().isPresent() && Noise.smooth(seed, x / config.accentScale(), 0, z / config.accentScale()) > config.accentThreshold()) {
			return config.accent().get();
		}
		return config.top();
	}
}
