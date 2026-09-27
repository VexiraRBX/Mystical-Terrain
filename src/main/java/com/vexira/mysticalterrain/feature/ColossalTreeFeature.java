package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ColossalTreeFeature extends Feature<ColossalTreeConfiguration> {
	// Features may only write into the 3x3 chunk area around the one being decorated.
	private static final int MAX_REACH = 14;

	public ColossalTreeFeature(Codec<ColossalTreeConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<ColossalTreeConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		ColossalTreeConfiguration config = context.config();
		BlockPos origin = context.origin();

		if (!level.getBlockState(origin.below()).is(BlockTags.DIRT)) {
			return false;
		}

		int height = config.height().sample(random);
		int radius = config.trunkRadius().sample(random);
		int crown = config.crownRadius().sample(random);
		if (origin.getY() + height + crown > level.getMinY() + level.getHeight() - 1) {
			return false;
		}
		for (int y = 1; y <= Math.min(height, 10); y++) {
			if (!isFree(level, origin.above(y))) {
				return false;
			}
		}

		placeTrunk(level, random, config, origin, height, radius);

		int branches = config.branchCount().sample(random);
		double angle = random.nextDouble() * Math.PI * 2;
		for (int i = 0; i < branches; i++) {
			angle += Math.PI * 2 / Math.max(1, branches) + (random.nextDouble() - 0.5) * 0.9;
			int startY = Mth.floor(height * (0.45 + 0.4 * random.nextDouble()));
			int leafRadius = 2 + random.nextInt(2);
			int length = Math.min(crown - random.nextInt(3), MAX_REACH - leafRadius - 1);
			placeBranch(level, random, config, origin.above(startY), angle, Math.max(radius + 2, length), leafRadius);
		}

		int topRadius = Math.max(3, crown / 2 + 1);
		placeFoliage(level, random, config, origin.above(height), topRadius, Math.max(2, topRadius * 2 / 3));
		placeFoliage(level, random, config, origin.above(height - topRadius / 2), topRadius + 1, 2);
		return true;
	}

	private void placeTrunk(WorldGenLevel level, RandomSource random, ColossalTreeConfiguration config, BlockPos origin, int height, int radius) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = -3; y < height; y++) {
			double taper = 1.0 - 0.45 * Math.max(0, y) / height;
			double flare = y < 4 ? (4 - y) * 0.55 : 0;
			double r = (radius + 0.5) * taper + flare;
			int reach = Mth.ceil(r);
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dz = -reach; dz <= reach; dz++) {
					if (dx * dx + dz * dz > r * r) {
						continue;
					}
					pos.setWithOffset(origin, dx, y, dz);
					BlockState existing = level.getBlockState(pos);
					if (y < 0 ? isSoil(existing) || isFree(level, pos) : isFree(level, pos)) {
						setBlock(level, pos, log(config, random, pos, Direction.Axis.Y));
					}
				}
			}
		}
	}

	private void placeBranch(WorldGenLevel level, RandomSource random, ColossalTreeConfiguration config, BlockPos start,
			double angle, int length, int leafRadius) {
		double dx = Math.cos(angle);
		double dz = Math.sin(angle);
		double rise = 0.35 + random.nextDouble() * 0.3;
		Direction.Axis axis = Math.abs(dx) > Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos end = start;
		for (int step = 0; step <= length; step++) {
			pos.setWithOffset(start, (int) Math.round(dx * step), (int) Math.round(rise * step), (int) Math.round(dz * step));
			if (isFree(level, pos)) {
				setBlock(level, pos, log(config, random, pos, axis));
			}
			end = pos.immutable();
		}
		placeFoliage(level, random, config, end, leafRadius + 1, leafRadius);
	}

	private void placeFoliage(WorldGenLevel level, RandomSource random, ColossalTreeConfiguration config, BlockPos center,
			int horizontal, int vertical) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -horizontal; dx <= horizontal; dx++) {
			for (int dy = -vertical; dy <= vertical; dy++) {
				for (int dz = -horizontal; dz <= horizontal; dz++) {
					double d = (double) (dx * dx + dz * dz) / (horizontal * horizontal) + (double) (dy * dy) / (vertical * vertical);
					if (d > 1.0 || d > 0.7 && random.nextFloat() < 0.35) {
						continue;
					}
					pos.setWithOffset(center, dx, dy, dz);
					if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(BlockTags.REPLACEABLE_BY_TREES)) {
						BlockState leaves = config.foliageProvider().getState(random, pos);
						if (leaves.hasProperty(BlockStateProperties.PERSISTENT)) {
							// The trunk can sit far from the outer leaves, so they must never decay.
							leaves = leaves.setValue(BlockStateProperties.PERSISTENT, true);
						}
						setBlock(level, pos, leaves);
					}
				}
			}
		}
	}

	private static BlockState log(ColossalTreeConfiguration config, RandomSource random, BlockPos pos, Direction.Axis axis) {
		BlockState state = config.trunkProvider().getState(random, pos);
		return state.hasProperty(BlockStateProperties.AXIS) ? state.setValue(BlockStateProperties.AXIS, axis) : state;
	}

	private static boolean isFree(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE_BY_TREES);
	}

	private static boolean isSoil(BlockState state) {
		return state.is(BlockTags.DIRT) || state.is(BlockTags.BASE_STONE_OVERWORLD);
	}
}
