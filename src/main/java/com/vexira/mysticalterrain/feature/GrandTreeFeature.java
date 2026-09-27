package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.vexira.mysticalterrain.tree.TreeBuilder;
import com.vexira.mysticalterrain.tree.TreeParams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GrandTreeFeature extends Feature<GrandTreeConfiguration> {
	private static final int LOCAL_REACH = 15;
	private static final int MAX_REACH = 23;

	public GrandTreeFeature(Codec<GrandTreeConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<GrandTreeConfiguration> context) {
		WorldGenLevel level = context.level();
		GrandTreeConfiguration config = context.config();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		ChunkPos chunk = new ChunkPos(origin);
		int reach = Math.min(config.reach(), MAX_REACH);
		if (reach > LOCAL_REACH) {
			// Features may only write to the 3x3 chunks around this one, so the wider the tree the closer to the middle it has to grow.
			int lo = reach - 16, hi = 31 - reach;
			int x = chunk.getMinBlockX() + lo + random.nextInt(hi - lo + 1);
			int z = chunk.getMinBlockZ() + lo + random.nextInt(hi - lo + 1);
			BlockPos moved = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z);
			if (!level.getBiome(moved).equals(level.getBiome(origin))) {
				return false;
			}
			origin = moved;
		}
		BlockPos base = ground(level, origin);
		if (base == null || !config.aquatic() && !level.getFluidState(base).isEmpty() || crowded(level, base)) {
			return false;
		}
		int ceiling = level.getMaxY() - base.getY();
		if (level.dimensionType().hasCeiling()) {
			ceiling = Math.min(ceiling, headroom(level, base));
		}
		if (ceiling < 8) {
			return false;
		}
		BoundingBox box = new BoundingBox(chunk.getMinBlockX() - 16, level.getMinY(), chunk.getMinBlockZ() - 16,
				chunk.getMaxBlockX() + 16, level.getMaxY(), chunk.getMaxBlockZ() + 16);
		TreeParams params = config.sample(random, reach, ceiling);
		TreePlacer.place(level, config, TreeBuilder.build(params), base, params.seed(), box);
		return true;
	}

	// Another trunk this close means a giant already grew here; two would fuse into one lump. Only wood standing two blocks
	// high counts, so a bush's stub of a trunk or a fallen log doesn't keep a tree away.
	private static boolean crowded(WorldGenLevel level, BlockPos base) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -4; dx <= 4; dx += 2) {
			for (int dz = -4; dz <= 4; dz += 2) {
				if (isWood(level.getBlockState(pos.setWithOffset(base, dx, 1, dz))) && isWood(level.getBlockState(pos.setWithOffset(base, dx, 2, dz)))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isWood(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(TreePlacer.WOOD);
	}

	// Under a roof like the Nether's, how far the tree can grow before it would bury its crown in the rock above.
	private static int headroom(WorldGenLevel level, BlockPos base) {
		BlockPos.MutableBlockPos pos = base.mutable();
		for (int dy = 0; dy < 96; dy++) {
			BlockState state = level.getBlockState(pos.setWithOffset(base, 0, dy, 0));
			if (!state.isAir() && !state.canBeReplaced() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
				return dy - 2;
			}
		}
		return 96;
	}

	// Steps down through canopy and undergrowth to the soil. Coming down onto wood means the spot is taken by another tree, and
	// the tree would end up growing out of its branches.
	private static BlockPos ground(WorldGenLevel level, BlockPos start) {
		BlockPos.MutableBlockPos pos = start.mutable();
		for (int i = 0; i < 40 && pos.getY() > level.getMinY(); i++) {
			BlockPos below = pos.below();
			BlockState state = level.getBlockState(below);
			if (isWood(state)) {
				return null;
			}
			if (state.isAir() || state.is(BlockTags.LEAVES) || state.is(TreePlacer.CANOPY) || state.is(BlockTags.REPLACEABLE)
					|| state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.FLOWERS)) {
				pos.move(Direction.DOWN);
				continue;
			}
			return state.isFaceSturdy(level, below, Direction.UP) ? pos.immutable() : null;
		}
		return null;
	}
}
