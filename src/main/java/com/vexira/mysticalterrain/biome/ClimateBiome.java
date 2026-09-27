package com.vexira.mysticalterrain.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

// A biome pinned to its own spot in the climate, rather than taking over one of vanilla's.
public record ClimateBiome(ResourceKey<Biome> biome, Climate.ParameterPoint parameters) {
	public static final Codec<ClimateBiome> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ClimateBiome::biome),
			Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(ClimateBiome::parameters)
	).apply(instance, ClimateBiome::new));
}
