package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.vexira.mysticalterrain.formation.Formation;
import com.vexira.mysticalterrain.formation.FormationBuilder;
import com.vexira.mysticalterrain.formation.FormationParams;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FormationFeature extends Feature<FormationConfiguration> {
	// Features may write to the chunks around their own. From anywhere in this chunk a formation of this reach stays
	// inside them; wider ones are moved to its middle.
	private static final int LOCAL_REACH = 16;

	public FormationFeature(Codec<FormationConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<FormationConfiguration> context) {
		WorldGenLevel level = context.level();
		FormationConfiguration config = context.config();
		BlockPos origin = context.origin();
		ChunkPos chunk = new ChunkPos(origin);
		FormationParams params = config.sample(context.random());
		Formation formation = FormationBuilder.build(params);
		BlockPos base = origin;
		if (formation.reach() > LOCAL_REACH) {
			int x = chunk.getMiddleBlockX();
			int z = chunk.getMiddleBlockZ();
			if (config.anchor() == FormationConfiguration.Anchor.FLOAT) {
				base = new BlockPos(x, origin.getY(), z);
			} else if (!level.dimensionType().hasCeiling()) {
				base = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
			}
			if (!level.getBiome(base).equals(level.getBiome(origin))) {
				return false;
			}
		}
		// Nether placements count the bed of the lava sea as a floor, so ground has to be dry as well as solid.
		if (config.anchor() != FormationConfiguration.Anchor.FLOAT && (base.getY() <= level.getMinY() + 2
				|| level.getBlockState(base.below()).isAir() || !level.getFluidState(base).isEmpty())) {
			return false;
		}
		BoundingBox box = new BoundingBox(chunk.getMinBlockX() - 16, level.getMinY(), chunk.getMinBlockZ() - 16,
				chunk.getMaxBlockX() + 16, level.getMaxY(), chunk.getMaxBlockZ() + 16);
		FormationPlacer.place(level, config, formation, base, params.seed(), box);
		return true;
	}
}
