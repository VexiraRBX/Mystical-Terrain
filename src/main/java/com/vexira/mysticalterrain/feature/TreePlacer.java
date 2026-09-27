package com.vexira.mysticalterrain.feature;

import com.vexira.mysticalterrain.MysticalTerrain;
import com.vexira.mysticalterrain.tree.Noise;
import com.vexira.mysticalterrain.tree.Part;
import com.vexira.mysticalterrain.tree.Sink;
import com.vexira.mysticalterrain.tree.Tree;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

// Turns a rasterized tree into blocks, only ever writing inside the given box.
final class TreePlacer implements Sink {
	// Blocks the grand trees are built from that aren't tagged as logs or leaves: stone trunks, mushroom caps, froglight fruit...
	static final TagKey<Block> WOOD = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MysticalTerrain.MOD_ID, "tree_wood"));
	static final TagKey<Block> CANOPY = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MysticalTerrain.MOD_ID, "tree_canopy"));
	private static final Direction.Axis[] AXES = {Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z};
	private static final int NO_GROUND = Integer.MIN_VALUE;

	private final WorldGenLevel level;
	private final TreeBlocks blocks;
	private final BlockPos base;
	private final long seed;
	private final BoundingBox box;
	private final double foot;
	private final Map<Long, Integer> grounds = new HashMap<>();
	private final RandomSource random = RandomSource.create();
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private long[] leaves = new long[512];
	private int leafCount;

	private TreePlacer(WorldGenLevel level, TreeBlocks blocks, BlockPos base, long seed, BoundingBox box, double foot) {
		this.level = level;
		this.blocks = blocks;
		this.base = base;
		this.seed = seed;
		this.box = box;
		this.foot = foot;
	}

	static void place(WorldGenLevel level, GrandTreeConfiguration config, Tree tree, BlockPos base, long seed, BoundingBox box) {
		TreePlacer placer = new TreePlacer(level, config.blocks(), base, seed, box, tree.foot());
		tree.rasterize(box.minX() - base.getX(), box.minY() - base.getY(), box.minZ() - base.getZ(),
				box.maxX() - base.getX(), box.maxY() - base.getY(), box.maxZ() - base.getZ(), placer);
		placer.hang();
		placer.cover(tree);
	}

	@Override
	public void place(int x, int y, int z, Part part, int axis) {
		if (part == Part.ROOT) {
			int shift = drape(x, y, z);
			if (shift == NO_GROUND) {
				return;
			}
			y += shift;
		}
		pos.set(base.getX() + x, base.getY() + y, base.getZ() + z);
		if (level.isOutsideBuildHeight(pos)) {
			return;
		}
		BlockState current = level.getBlockState(pos);
		switch (part) {
			case ROOT, TRUNK, BRANCH -> {
				boolean buried = part == Part.ROOT || part == Part.TRUNK && y <= 1;
				if (isSoft(current) || current.is(BlockTags.LOGS) || buried && current.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) {
					BlockState state = wood(part);
					if (state.hasProperty(BlockStateProperties.AXIS)) {
						state = state.setValue(BlockStateProperties.AXIS, AXES[axis]);
					}
					set(state);
				}
			}
			case VEIN -> {
				if (blocks.vein().isPresent() && !current.isAir()
						&& (current.is(wood(Part.TRUNK).getBlock()) || current.is(wood(Part.ROOT).getBlock()) || current.is(wood(Part.BRANCH).getBlock()))) {
					set(pick(blocks.vein().get()));
				}
			}
			case LEAVES -> {
				if (blocks.leaves().isPresent() && isSoft(current)) {
					boolean fruit = blocks.fruit().isPresent() && Noise.unit(seed + 99, x, y, z) < blocks.fruitChance();
					set(settle(pick(fruit ? blocks.fruit().get() : blocks.leaves().get()), current));
					remember(pos);
				}
			}
			case FRUIT -> {
				if (blocks.fruit().isPresent() && isSoft(current)) {
					set(settle(pick(blocks.fruit().get()), current));
				}
			}
		}
	}

	// Roots are drawn for flat ground. Away from the trunk they're moved up or down with the terrain so they lie along a slope
	// instead of sticking out over it, and the bits that would still hang in the air are left out.
	private int drape(int x, int y, int z) {
		int ground = ground(base.getX() + x, base.getZ() + z);
		double w = Math.min(1, Math.max(0, (Math.sqrt(x * x + z * z) - foot) / 3));
		int dy = Math.max(-8, Math.min(5, ground - (base.getY() - 1)));
		int shift = (int) Math.round(dy * w);
		if (y <= 2 && base.getY() + y + shift - ground > 2) {
			return NO_GROUND;
		}
		return shift;
	}

	private int ground(int x, int z) {
		return grounds.computeIfAbsent(BlockPos.asLong(x, 0, z), key -> scan(x, z));
	}

	private int scan(int x, int z) {
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, base.getY() + 8, z);
		for (; at.getY() > base.getY() - 17; at.move(Direction.DOWN)) {
			BlockState state = level.getBlockState(at);
			if (!isSoft(state) && !isTree(state)) {
				return at.getY();
			}
		}
		return base.getY() - 17;
	}

	private BlockState wood(Part part) {
		return pick(switch (part) {
			case ROOT -> blocks.rootOrTrunk();
			case BRANCH -> blocks.branchOrTrunk();
			default -> blocks.trunk();
		});
	}

	// Seeded from the position, so every chunk a landmark spans picks the same blocks for it.
	private BlockState pick(BlockStateProvider provider) {
		random.setSeed(Noise.hash(seed, pos.getX(), pos.getY(), pos.getZ()));
		return provider.getState(random, pos);
	}

	private BlockState settle(BlockState state, BlockState current) {
		if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
			// Canopies sit far further from the trunk than leaves can survive on their own.
			state = state.setValue(BlockStateProperties.PERSISTENT, true);
		}
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, current.getFluidState().is(FluidTags.WATER) && current.getFluidState().isSource());
		}
		return state;
	}

	private void set(BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}

	private void remember(BlockPos leaf) {
		if (leafCount == leaves.length) {
			leaves = Arrays.copyOf(leaves, leafCount * 2);
		}
		leaves[leafCount++] = leaf.asLong();
	}

	private void hang() {
		if (blocks.hanging().isEmpty()) {
			return;
		}
		for (int i = 0; i < leafCount; i++) {
			pos.set(leaves[i]);
			if (Noise.unit(seed + 7, pos.getX(), pos.getY(), pos.getZ()) >= blocks.hangingChance()) {
				continue;
			}
			BlockState state = pick(blocks.hanging().get());
			int length = blocks.hangingLength().sample(random);
			if (state.is(Blocks.VINE)) {
				Direction side = Direction.from2DDataValue((int) (Noise.hash(seed + 8, pos.getX(), pos.getY(), pos.getZ()) & 3));
				chain(pos.relative(side), state.setValue(VineBlock.getPropertyForFace(side.getOpposite()), true), length);
			} else {
				chain(pos.below(), state, length);
			}
		}
	}

	private void chain(BlockPos start, BlockState state, int length) {
		BlockPos.MutableBlockPos at = start.mutable();
		int n = 0;
		while (n < length && box.isInside(at) && level.isEmptyBlock(at)) {
			n++;
			at.move(Direction.DOWN);
		}
		if (n == 0 || !(n == 1 ? tip(state) : body(state)).canSurvive(level, start)) {
			return;
		}
		at.set(start);
		for (int i = 0; i < n; i++) {
			level.setBlock(at, i == n - 1 ? tip(state) : body(state), Block.UPDATE_CLIENTS);
			at.move(Direction.DOWN);
		}
	}

	private static BlockState body(BlockState state) {
		if (state.is(Blocks.CAVE_VINES)) {
			return Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, state.getValue(CaveVines.BERRIES));
		}
		if (state.is(Blocks.WEEPING_VINES)) {
			return Blocks.WEEPING_VINES_PLANT.defaultBlockState();
		}
		return state.hasProperty(HangingMossBlock.TIP) ? state.setValue(HangingMossBlock.TIP, false) : state;
	}

	private static BlockState tip(BlockState state) {
		return state.hasProperty(HangingMossBlock.TIP) ? state.setValue(HangingMossBlock.TIP, true) : state;
	}

	// Scatters the cover block (petals, leaf litter, moss...) over the ground under the canopy.
	private void cover(Tree tree) {
		if (blocks.cover().isEmpty()) {
			return;
		}
		int reach = tree.reach();
		int x0 = Math.max(box.minX(), base.getX() - reach), x1 = Math.min(box.maxX(), base.getX() + reach);
		int z0 = Math.max(box.minZ(), base.getZ() - reach), z1 = Math.min(box.maxZ(), base.getZ() + reach);
		double limit = reach * 0.85;
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				double dx = x - base.getX(), dz = z - base.getZ();
				if (dx * dx + dz * dz > limit * limit || Noise.unit(seed + 11, x, 0, z) >= blocks.coverChance()) {
					continue;
				}
				for (int y = base.getY() + 6; y > base.getY() - 10; y--) {
					pos.set(x, y - 1, z);
					BlockState ground = level.getBlockState(pos);
					if (isSoft(ground)) {
						continue;
					}
					pos.set(x, y, z);
					BlockState here = level.getBlockState(pos);
					if (here.isAir() && !ground.is(BlockTags.LOGS)) {
						BlockState state = pick(blocks.cover().get());
						if (state.canSurvive(level, pos)) {
							set(state);
						}
					}
					break;
				}
			}
		}
	}

	static boolean isTree(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(WOOD) || state.is(CANOPY);
	}

	private static boolean isSoft(BlockState state) {
		return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.LEAVES)
				|| state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS);
	}
}
