package com.vexira.mysticalterrain.biome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

public class MysticalRegion extends Region {
	private static final Codec<ResourceKey<Biome>> BIOME = ResourceKey.codec(Registries.BIOME);

	public static final Codec<MysticalRegion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("name").forGetter(Region::getName),
			Codec.INT.fieldOf("weight").forGetter(Region::getWeight),
			Codec.unboundedMap(BIOME, ExtraCodecs.compactListCodec(BIOME)).fieldOf("replacements").forGetter(region -> region.replacements),
			ClimateBiome.CODEC.listOf().fieldOf("caves").forGetter(region -> region.caves)
	).apply(instance, MysticalRegion::new));

	private final Map<ResourceKey<Biome>, List<ResourceKey<Biome>>> replacements;
	private final List<ClimateBiome> caves;

	public MysticalRegion(Identifier name, int weight, Map<ResourceKey<Biome>, List<ResourceKey<Biome>>> replacements, List<ClimateBiome> caves) {
		super(name, RegionType.OVERWORLD, weight);
		this.replacements = replacements;
		this.caves = caves;
	}

	@Override
	public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
		addModifiedVanillaOverworldBiomes(point -> mapper.accept(Pair.of(point.getFirst(), pick(point))), builder -> {
		});
		caves.forEach(cave -> addBiome(mapper, cave.parameters(), cave.biome()));
	}

	// A slot with two replacements is split the way vanilla picks its own variants: the second one takes positive weirdness.
	private ResourceKey<Biome> pick(Pair<Climate.ParameterPoint, ResourceKey<Biome>> point) {
		List<ResourceKey<Biome>> options = replacements.get(point.getSecond());
		if (options == null) {
			return point.getSecond();
		}
		Climate.Parameter weirdness = point.getFirst().weirdness();
		return options.get(options.size() > 1 && weirdness.min() + weirdness.max() > 0 ? 1 : 0);
	}
}
