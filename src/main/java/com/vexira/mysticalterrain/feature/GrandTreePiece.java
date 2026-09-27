package com.vexira.mysticalterrain.feature;

import com.vexira.mysticalterrain.tree.Growth;
import com.vexira.mysticalterrain.tree.Tree;
import com.vexira.mysticalterrain.tree.TreeBuilder;
import com.vexira.mysticalterrain.tree.TreeParams;
import com.vexira.mysticalterrain.tree.TreeStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class GrandTreePiece extends StructurePiece {
	public static final StructurePieceType TYPE = GrandTreePiece::new;

	private final ResourceKey<ConfiguredFeature<?, ?>> key;
	// Null when the tree has since been removed from the data; the piece then places nothing.
	private final GrandTreeConfiguration config;
	private final BlockPos base;
	private final TreeParams params;
	private volatile Tree tree;

	GrandTreePiece(ResourceKey<ConfiguredFeature<?, ?>> key, GrandTreeConfiguration config, BlockPos base, TreeParams params) {
		this(key, config, base, params, TreeBuilder.build(params));
	}

	private GrandTreePiece(ResourceKey<ConfiguredFeature<?, ?>> key, GrandTreeConfiguration config, BlockPos base, TreeParams params, Tree tree) {
		super(TYPE, 0, new BoundingBox(base.getX() + tree.minX(), base.getY() + tree.minY(), base.getZ() + tree.minZ(),
				base.getX() + tree.maxX(), base.getY() + tree.maxY(), base.getZ() + tree.maxZ()));
		this.key = key;
		this.config = config;
		this.base = base;
		this.params = params;
		this.tree = tree;
	}

	public GrandTreePiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(TYPE, tag);
		key = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.parse(tag.getStringOr("tree", "")));
		config = context.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getOptional(key)
				.map(ConfiguredFeature::config)
				.filter(GrandTreeConfiguration.class::isInstance)
				.map(GrandTreeConfiguration.class::cast)
				.orElse(null);
		base = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
		params = new TreeParams(TreeStyle.valueOf(tag.getStringOr("style", "BROADLEAF")), tag.getIntOr("height", 20),
				tag.getDoubleOr("radius", 2), tag.getDoubleOr("crown", 8), tag.getDoubleOr("lean", 0), tag.getDoubleOr("gnarl", 0),
				tag.getIntOr("roots", -1), tag.getIntOr("branches", -1), tag.getDoubleOr("droop", 0), tag.getDoubleOr("shape", 0.5),
				tag.getDoubleOr("density", 0.85), tag.getIntOr("veins", 0), tag.getIntOr("reach", 14), tag.getIntOr("ceiling", 100),
				tag.getLongOr("seed", 0), config == null ? Growth.DEFAULT : config.growth());
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putString("tree", key.identifier().toString());
		tag.putInt("x", base.getX());
		tag.putInt("y", base.getY());
		tag.putInt("z", base.getZ());
		tag.putString("style", params.style().name());
		tag.putInt("height", params.height());
		tag.putDouble("radius", params.radius());
		tag.putDouble("crown", params.crown());
		tag.putDouble("lean", params.lean());
		tag.putDouble("gnarl", params.gnarl());
		tag.putInt("roots", params.roots());
		tag.putInt("branches", params.branches());
		tag.putDouble("droop", params.droop());
		tag.putDouble("shape", params.shape());
		tag.putDouble("density", params.density());
		tag.putInt("veins", params.veins());
		tag.putInt("reach", params.reach());
		tag.putInt("ceiling", params.ceiling());
		tag.putLong("seed", params.seed());
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
			BoundingBox box, ChunkPos chunk, BlockPos pivot) {
		if (config == null) {
			return;
		}
		if (tree == null) {
			tree = TreeBuilder.build(params);
		}
		TreePlacer.place(level, config, tree, base, params.seed(), box);
	}
}
