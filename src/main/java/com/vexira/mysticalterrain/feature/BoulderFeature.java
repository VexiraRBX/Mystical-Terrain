package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BoulderFeature extends Feature<BoulderConfiguration> {
	public BoulderFeature(Codec<BoulderConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BoulderConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BoulderConfiguration config = context.config();
		BlockPos origin = context.origin();

		BlockState ground = level.getBlockState(origin.below());
		if (ground.isAir() || !ground.getFluidState().isEmpty()) {
			return false;
		}

		int radius = config.radius().sample(random);
		double rx = radius * (0.8 + random.nextDouble() * 0.4);
		double ry = radius * (0.6 + random.nextDouble() * 0.3);
		double rz = radius * (0.8 + random.nextDouble() * 0.4);
		BlockPos center = origin.below(Math.max(1, radius / 3));

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int reachX = Mth.ceil(rx);
		int reachY = Mth.ceil(ry);
		int reachZ = Mth.ceil(rz);
		for (int dx = -reachX; dx <= reachX; dx++) {
			for (int dy = -reachY; dy <= reachY; dy++) {
				for (int dz = -reachZ; dz <= reachZ; dz++) {
					double d = dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz);
					if (d > 1.0 || d > 0.8 && random.nextFloat() < 0.4) {
						continue;
					}
					pos.setWithOffset(center, dx, dy, dz);
					if (!level.getBlockState(pos).is(BlockTags.FEATURES_CANNOT_REPLACE)) {
						setBlock(level, pos, config.stateProvider().getState(random, pos));
					}
				}
			}
		}
		return true;
	}
}
