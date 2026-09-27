package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class SpireFeature extends Feature<SpireConfiguration> {
	private static final int MAX_REACH = 14;

	public SpireFeature(Codec<SpireConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SpireConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		SpireConfiguration config = context.config();
		BlockPos origin = context.origin();
		boolean hanging = config.hanging();

		BlockState anchor = level.getBlockState(origin.relative(hanging ? Direction.UP : Direction.DOWN));
		if (anchor.isAir() || !anchor.getFluidState().isEmpty()) {
			return false;
		}

		int radius = config.radius().sample(random);
		int height = hanging
				? Math.min(config.height().sample(random), origin.getY() - level.getMinY() - 2)
				: Math.min(config.height().sample(random), level.getMinY() + level.getHeight() - 2 - origin.getY());
		if (height < 3) {
			return false;
		}

		double maxLean = Math.max(0, MAX_REACH - radius - 1) / (double) height;
		double leanX = Mth.clamp((random.nextDouble() - 0.5) * 0.5, -maxLean, maxLean);
		double leanZ = Mth.clamp((random.nextDouble() - 0.5) * 0.5, -maxLean, maxLean);
		int dir = hanging ? -1 : 1;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		// Start a few blocks inside the ground/ceiling so the spire never floats on a thin edge. That buried footing is wider
		// than the spire, so it only replaces solid rock; on a slope it would otherwise stick out into the air or water.
		for (int i = -3; i < height; i++) {
			double t = Math.max(0, i) / (double) height;
			double r = (radius + 0.3) * Math.pow(1.0 - t, 1.2) + (i < 0 ? 0.6 : 0);
			if (r < 0.15) {
				break;
			}
			int ox = (int) Math.round(leanX * Math.max(0, i));
			int oz = (int) Math.round(leanZ * Math.max(0, i));
			int reach = Mth.ceil(r);
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dz = -reach; dz <= reach; dz++) {
					double d = Math.sqrt(dx * dx + dz * dz) + (random.nextFloat() - 0.5f) * 0.5;
					if (d > Math.max(r, 0.5)) {
						continue;
					}
					pos.setWithOffset(origin, ox + dx, dir * i, oz + dz);
					BlockState existing = level.getBlockState(pos);
					boolean allowed = i < 0 ? !existing.is(BlockTags.FEATURES_CANNOT_REPLACE) : canReplace(existing);
					if (allowed) {
						BlockState state = d > r - 1.2 ? config.shell().getState(random, pos) : config.core().getState(random, pos);
						setBlock(level, pos, state);
					}
				}
			}
		}
		return true;
	}

	private static boolean isRock(BlockState state) {
		return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced() && !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
	}

	private static boolean canReplace(BlockState state) {
		return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
	}
}
