package com.vexira.mysticalterrain.feature;

import com.vexira.mysticalterrain.formation.Formation;
import com.vexira.mysticalterrain.formation.FormationBuilder;
import com.vexira.mysticalterrain.formation.FormationKind;
import com.vexira.mysticalterrain.formation.FormationParams;
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

public class FormationPiece extends StructurePiece {
	public static final StructurePieceType TYPE = FormationPiece::new;

	private final ResourceKey<ConfiguredFeature<?, ?>> key;
	// Null when the formation has since been removed from the data; the piece then places nothing.
	private final FormationConfiguration config;
	private final BlockPos base;
	private final FormationParams params;
	private volatile Formation formation;

	FormationPiece(ResourceKey<ConfiguredFeature<?, ?>> key, FormationConfiguration config, BlockPos base, FormationParams params) {
		this(key, config, base, params, FormationBuilder.build(params));
	}

	private FormationPiece(ResourceKey<ConfiguredFeature<?, ?>> key, FormationConfiguration config, BlockPos base, FormationParams params,
			Formation formation) {
		super(TYPE, 0, new BoundingBox(base.getX() + formation.minX(), base.getY() + formation.minY(), base.getZ() + formation.minZ(),
				base.getX() + formation.maxX(), base.getY() + formation.maxY(), base.getZ() + formation.maxZ()));
		this.key = key;
		this.config = config;
		this.base = base;
		this.params = params;
		this.formation = formation;
	}

	public FormationPiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(TYPE, tag);
		key = ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.parse(tag.getStringOr("formation", "")));
		config = context.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).getOptional(key)
				.map(ConfiguredFeature::config)
				.filter(FormationConfiguration.class::isInstance)
				.map(FormationConfiguration.class::cast)
				.orElse(null);
		base = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
		FormationKind kind;
		try {
			kind = FormationKind.valueOf(tag.getStringOr("kind", "SPIRES"));
		} catch (IllegalArgumentException e) {
			kind = FormationKind.SPIRES;
		}
		params = new FormationParams(kind, tag.getIntOr("size", 8), tag.getIntOr("count", -1), tag.getDoubleOr("lean", 0.5),
				tag.getDoubleOr("girth", 1), tag.getIntOr("sides", 6), tag.getLongOr("seed", 0));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putString("formation", key.identifier().toString());
		tag.putInt("x", base.getX());
		tag.putInt("y", base.getY());
		tag.putInt("z", base.getZ());
		tag.putString("kind", params.kind().name());
		tag.putInt("size", params.size());
		tag.putInt("count", params.count());
		tag.putDouble("lean", params.lean());
		tag.putDouble("girth", params.girth());
		tag.putInt("sides", params.sides());
		tag.putLong("seed", params.seed());
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
			BoundingBox box, ChunkPos chunk, BlockPos pivot) {
		if (config == null) {
			return;
		}
		if (formation == null) {
			formation = FormationBuilder.build(params);
		}
		FormationPlacer.place(level, config, formation, base, params.seed(), box);
	}
}
