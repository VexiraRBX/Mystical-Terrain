package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ColossalTreeConfiguration(
		BlockStateProvider trunkProvider,
		BlockStateProvider foliageProvider,
		IntProvider height,
		IntProvider trunkRadius,
		IntProvider crownRadius,
		IntProvider branchCount
) implements FeatureConfiguration {
	public static final Codec<ColossalTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(ColossalTreeConfiguration::trunkProvider),
			BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(ColossalTreeConfiguration::foliageProvider),
			IntProvider.codec(8, 120).fieldOf("height").forGetter(ColossalTreeConfiguration::height),
			IntProvider.codec(1, 3).fieldOf("trunk_radius").forGetter(ColossalTreeConfiguration::trunkRadius),
			IntProvider.codec(3, 13).fieldOf("crown_radius").forGetter(ColossalTreeConfiguration::crownRadius),
			IntProvider.codec(0, 16).fieldOf("branch_count").forGetter(ColossalTreeConfiguration::branchCount)
	).apply(instance, ColossalTreeConfiguration::new));
}
