package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

// accent takes over the top block in noise patches; the higher accent_threshold is, the smaller and rarer they get.
public record GroundCoverConfiguration(
		ResourceKey<Biome> biome,
		HolderSet<Block> replaceable,
		BlockStateProvider top,
		BlockStateProvider under,
		int depth,
		Optional<BlockStateProvider> accent,
		float accentScale,
		float accentThreshold
) implements FeatureConfiguration {
	public static final Codec<GroundCoverConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(GroundCoverConfiguration::biome),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable").forGetter(GroundCoverConfiguration::replaceable),
			BlockStateProvider.CODEC.fieldOf("top").forGetter(GroundCoverConfiguration::top),
			BlockStateProvider.CODEC.fieldOf("under").forGetter(GroundCoverConfiguration::under),
			Codec.intRange(0, 8).optionalFieldOf("depth", 3).forGetter(GroundCoverConfiguration::depth),
			BlockStateProvider.CODEC.optionalFieldOf("accent").forGetter(GroundCoverConfiguration::accent),
			Codec.floatRange(1, 256).optionalFieldOf("accent_scale", 12F).forGetter(GroundCoverConfiguration::accentScale),
			Codec.floatRange(-1, 1).optionalFieldOf("accent_threshold", 0.3F).forGetter(GroundCoverConfiguration::accentThreshold)
	).apply(instance, GroundCoverConfiguration::new));
}
