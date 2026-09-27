package com.vexira.mysticalterrain.biome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

public class NetherRegion extends Region {
	public static final Codec<NetherRegion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("name").forGetter(Region::getName),
			Codec.INT.fieldOf("weight").forGetter(Region::getWeight),
			ClimateBiome.CODEC.listOf().fieldOf("biomes").forGetter(region -> region.biomes)
	).apply(instance, NetherRegion::new));

	private final List<ClimateBiome> biomes;

	public NetherRegion(Identifier name, int weight, List<ClimateBiome> biomes) {
		super(name, RegionType.NETHER, weight);
		this.biomes = biomes;
	}

	@Override
	public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
		biomes.forEach(biome -> addBiome(mapper, biome.parameters(), biome.biome()));
	}
}
