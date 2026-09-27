package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexira.mysticalterrain.formation.Formation;
import com.vexira.mysticalterrain.formation.FormationBuilder;
import com.vexira.mysticalterrain.formation.FormationParams;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

// A formation too big for a feature: as a structure it can span every chunk it reaches.
public class FormationStructure extends Structure {
	public static final MapCodec<FormationStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			settingsCodec(instance),
			ConfiguredFeature.CODEC.fieldOf("formation").forGetter(structure -> structure.formation)
	).apply(instance, FormationStructure::new));
	public static final StructureType<FormationStructure> TYPE = () -> CODEC;

	// Nether floors below this are the lava sea.
	private static final int LAVA_SEA = 32;
	private static final int HEADROOM = 8;
	private static final int MAX_HEADROOM = 24;

	private final Holder<ConfiguredFeature<?, ?>> formation;

	public FormationStructure(StructureSettings settings, Holder<ConfiguredFeature<?, ?>> formation) {
		super(settings);
		this.formation = formation;
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (!(formation.value().config() instanceof FormationConfiguration config) || formation.unwrapKey().isEmpty()) {
			return Optional.empty();
		}
		ChunkPos chunk = context.chunkPos();
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		FormationParams params = config.sample(context.random());
		Formation shape = FormationBuilder.build(params);
		ChunkGenerator generator = context.chunkGenerator();
		LevelHeightAccessor heights = context.heightAccessor();
		RandomState randomState = context.randomState();
		Optional<Integer> y = switch (config.anchor()) {
			case SURFACE -> surface(generator, heights, randomState, x, z, shape.reach() / 2);
			case FLOOR -> floor(generator.getBaseColumn(x, z, heights, randomState), heights, config.elevation().sample(context.random()),
					Math.max(HEADROOM, Math.min(shape.maxY(), MAX_HEADROOM)));
			case FLOAT -> Optional.of(config.elevation().sample(context.random()));
		};
		if (y.isEmpty()) {
			return Optional.empty();
		}
		BlockPos base = new BlockPos(x, y.get(), z);
		return Optional.of(new GenerationStub(base, builder -> builder.addPiece(new FormationPiece(formation.unwrapKey().get(), config, base, params))));
	}

	// Island edges fall away into the void, so the ground has to reach out some way in every direction.
	private static Optional<Integer> surface(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState, int x, int z, int spread) {
		int y = generator.getFirstFreeHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heights, randomState);
		int floor = heights.getMinY() + 8;
		if (y <= floor) {
			return Optional.empty();
		}
		int[][] around = {{spread, 0}, {-spread, 0}, {0, spread}, {0, -spread}};
		for (int[] offset : around) {
			if (generator.getFirstFreeHeight(x + offset[0], z + offset[1], Heightmap.Types.WORLD_SURFACE_WG, heights, randomState) <= floor) {
				return Optional.empty();
			}
		}
		return Optional.of(y);
	}

	// The first open floor going down from top, with room above it for the formation and not in the lava sea.
	private static Optional<Integer> floor(NoiseColumn column, LevelHeightAccessor heights, int top, int headroom) {
		int start = Math.min(top, heights.getMaxY() - headroom);
		for (int y = start; y > Math.max(LAVA_SEA, heights.getMinY()); y--) {
			BlockState below = column.getBlock(y - 1);
			if (!column.getBlock(y).isAir() || below.isAir() || !below.getFluidState().isEmpty()) {
				continue;
			}
			boolean open = true;
			for (int dy = 1; dy < headroom && open; dy++) {
				open = column.getBlock(y + dy).isAir();
			}
			if (open) {
				return Optional.of(y);
			}
		}
		return Optional.empty();
	}

	@Override
	public StructureType<?> type() {
		return TYPE;
	}
}
