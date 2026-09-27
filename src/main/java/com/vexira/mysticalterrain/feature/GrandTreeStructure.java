package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexira.mysticalterrain.tree.TreeParams;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

// A landmark tree too big for a feature: as a structure it can span every chunk it reaches.
public class GrandTreeStructure extends Structure {
	public static final MapCodec<GrandTreeStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			settingsCodec(instance),
			ConfiguredFeature.CODEC.fieldOf("tree").forGetter(structure -> structure.tree)
	).apply(instance, GrandTreeStructure::new));
	public static final StructureType<GrandTreeStructure> TYPE = () -> CODEC;

	private final Holder<ConfiguredFeature<?, ?>> tree;

	public GrandTreeStructure(StructureSettings settings, Holder<ConfiguredFeature<?, ?>> tree) {
		super(settings);
		this.tree = tree;
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!(tree.value().config() instanceof GrandTreeConfiguration config) || tree.unwrapKey().isEmpty()) {
			return Optional.empty();
		}
		ChunkPos chunk = context.chunkPos();
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		ChunkGenerator generator = context.chunkGenerator();
		LevelHeightAccessor heights = context.heightAccessor();
		int floor = generator.getFirstFreeHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, context.randomState());
		int surface = generator.getFirstFreeHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heights, context.randomState());
		if (surface > floor && !config.aquatic() || heights.getMaxY() - floor < 16) {
			return Optional.empty();
		}
		BlockPos base = new BlockPos(x, floor, z);
		TreeParams params = config.sample(context.random(), config.reach(), heights.getMaxY() - floor);
		return Optional.of(new GenerationStub(base, builder -> builder.addPiece(new GrandTreePiece(tree.unwrapKey().get(), config, base, params))));
	}

	@Override
	public StructureType<?> type() {
		return TYPE;
	}
}
