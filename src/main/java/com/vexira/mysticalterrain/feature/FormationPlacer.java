package com.vexira.mysticalterrain.feature;

import com.vexira.mysticalterrain.formation.Formation;
import com.vexira.mysticalterrain.formation.Material;
import com.vexira.mysticalterrain.formation.Sink;
import com.vexira.mysticalterrain.tree.Noise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

// Turns a formation into blocks, only ever writing inside the given box.
final class FormationPlacer implements Sink {
	private static final Direction.Axis[] AXES = {Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z};

	private final WorldGenLevel level;
	private final FormationConfiguration config;
	private final BlockPos base;
	private final long seed;
	private final RandomSource random = RandomSource.create();
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private FormationPlacer(WorldGenLevel level, FormationConfiguration config, BlockPos base, long seed) {
		this.level = level;
		this.config = config;
		this.base = base;
		this.seed = seed;
	}

	static void place(WorldGenLevel level, FormationConfiguration config, Formation formation, BlockPos base, long seed, BoundingBox box) {
		FormationPlacer placer = new FormationPlacer(level, config, base, seed);
		formation.rasterize(box.minX() - base.getX(), box.minY() - base.getY(), box.minZ() - base.getZ(),
				box.maxX() - base.getX(), box.maxY() - base.getY(), box.maxZ() - base.getZ(), placer);
		if (config.loot().isEmpty()) {
			return;
		}
		for (int[] chest : formation.chests()) {
			placer.pos.set(base.getX() + chest[0], base.getY() + chest[1], base.getZ() + chest[2]);
			if (box.isInside(placer.pos)) {
				placer.chest();
			}
		}
	}

	@Override
	public void place(int x, int y, int z, Material material, int axis, boolean force) {
		pos.set(base.getX() + x, base.getY() + y, base.getZ() + z);
		BlockState current = level.getBlockState(pos);
		if (current.is(BlockTags.FEATURES_CANNOT_REPLACE)) {
			return;
		}
		if (material == Material.AIR) {
			if (!current.isAir()) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			return;
		}
		if (!force && !isSoft(current)) {
			return;
		}
		BlockStateProvider provider = config.material(material);
		if (provider == null) {
			return;
		}
		BlockState state = pick(provider);
		// Air in a provider leaves gaps, as in a glass halo.
		if (state.isAir()) {
			return;
		}
		if (state.hasProperty(BlockStateProperties.AXIS)) {
			state = state.setValue(BlockStateProperties.AXIS, AXES[axis]);
		}
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}

	private void chest() {
		Direction facing = Direction.from2DDataValue((int) (Noise.hash(seed + 5, pos.getX(), pos.getY(), pos.getZ()) & 3));
		level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_CLIENTS);
		random.setSeed(Noise.hash(seed + 6, pos.getX(), pos.getY(), pos.getZ()));
		RandomizableContainer.setBlockEntityLootTable(level, random, pos, config.loot().get());
	}

	// Seeded from the position, so every chunk a formation spans picks the same blocks for it.
	private BlockState pick(BlockStateProvider provider) {
		random.setSeed(Noise.hash(seed, pos.getX(), pos.getY(), pos.getZ()));
		return provider.getState(random, pos);
	}

	private static boolean isSoft(BlockState state) {
		return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
	}
}
