package com.vexira.mysticalterrain.biome;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexira.mysticalterrain.MysticalTerrain;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.EndBiomeRegistry;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

// Regions and surface rules have to exist before any datapack loads, so they ship as plain files in the jar.
public final class MysticalBiomes {
	private static final String DIR = "/data/" + MysticalTerrain.MOD_ID + "/terrablender/";

	private MysticalBiomes() {
	}

	public static void register() {
		List<MysticalRegion> regions = read("regions.json", MysticalRegion.CODEC.listOf());
		regions.forEach(Regions::register);
		SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MysticalTerrain.MOD_ID,
				read("surface_rules.json", SurfaceRules.RuleSource.CODEC));

		Regions.register(read("nether_region.json", NetherRegion.CODEC));
		SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER, MysticalTerrain.MOD_ID,
				read("nether_surface_rules.json", SurfaceRules.RuleSource.CODEC));

		List<EndBiome> end = read("end_biomes.json", EndBiome.CODEC.listOf());
		for (EndBiome biome : end) {
			switch (biome.zone()) {
				case HIGHLANDS -> EndBiomeRegistry.registerHighlandsBiome(biome.biome(), biome.weight());
				case MIDLANDS -> EndBiomeRegistry.registerMidlandsBiome(biome.biome(), biome.weight());
				case EDGE -> EndBiomeRegistry.registerEdgeBiome(biome.biome(), biome.weight());
				case ISLANDS -> EndBiomeRegistry.registerIslandBiome(biome.biome(), biome.weight());
			}
		}
		MysticalTerrain.LOGGER.info("Registered {} Mystical Terrain overworld regions, a Nether region and {} End biomes", regions.size(), end.size());
	}

	private static <T> T read(String file, Codec<T> codec) {
		String path = DIR + file;
		try (InputStream in = MysticalBiomes.class.getResourceAsStream(path)) {
			if (in == null) {
				throw new IllegalStateException("Missing " + path);
			}
			return codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
					.getOrThrow(error -> new IllegalStateException("Couldn't read " + path + ": " + error));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// Where in the End a biome grows: the high middles of the big islands, their slopes, their edges, or the scattered islets.
	private enum Zone {
		HIGHLANDS,
		MIDLANDS,
		EDGE,
		ISLANDS;

		static final Codec<Zone> CODEC = Codec.STRING.comapFlatMap(name -> {
			try {
				return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException e) {
				return DataResult.error(() -> "Unknown End zone: " + name);
			}
		}, zone -> zone.name().toLowerCase(Locale.ROOT));
	}

	private record EndBiome(ResourceKey<Biome> biome, Zone zone, int weight) {
		static final Codec<EndBiome> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(EndBiome::biome),
				Zone.CODEC.fieldOf("zone").forGetter(EndBiome::zone),
				Codec.intRange(1, 100).fieldOf("weight").forGetter(EndBiome::weight)
		).apply(instance, EndBiome::new));
	}
}
