package com.vexira.mysticalterrain.biome;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.vexira.mysticalterrain.MysticalTerrain;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

// grand_trees.json lists, for every biome that grows grand trees (vanilla ones included), the placed features to add to it.
public final class MysticalTrees {
	private static final String PLACEMENTS = "/data/" + MysticalTerrain.MOD_ID + "/grand_trees.json";
	private static final Codec<Map<String, List<ResourceKey<PlacedFeature>>>> CODEC =
			Codec.unboundedMap(Codec.STRING, ResourceKey.codec(Registries.PLACED_FEATURE).listOf());

	private MysticalTrees() {
	}

	public static void register() {
		Map<String, List<ResourceKey<PlacedFeature>>> byBiome;
		try (InputStream in = MysticalTrees.class.getResourceAsStream(PLACEMENTS)) {
			if (in == null) {
				throw new IllegalStateException("Missing " + PLACEMENTS);
			}
			byBiome = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
					.getOrThrow(error -> new IllegalStateException("Couldn't read " + PLACEMENTS + ": " + error));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		BiomeModifications.create(MysticalTerrain.id("grand_trees")).add(ModificationPhase.ADDITIONS,
				context -> byBiome.containsKey(context.getBiomeKey().identifier().toString()),
				(selection, modification) -> byBiome.get(selection.getBiomeKey().identifier().toString())
						.forEach(key -> modification.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, key)));
		long placements = byBiome.values().stream().flatMap(List::stream).distinct().count();
		MysticalTerrain.LOGGER.info("Planted grand trees in {} biomes with {} placements", byBiome.size(), placements);
		clearVanillaTrees();
	}

	// Only our trees grow in vanilla's biomes and ours: every placed feature that would grow a vanilla tree, a fallen log or
	// an azalea tree is taken out, apart from our own undergrowth (bushes and fallen logs). Other mods' biomes keep theirs, and
	// saplings still grow vanilla trees.
	private static void clearVanillaTrees() {
		BiomeModifications.create(MysticalTerrain.id("no_vanilla_trees")).add(ModificationPhase.REMOVALS, context -> {
			String namespace = context.getBiomeKey().identifier().getNamespace();
			return namespace.equals("minecraft") || namespace.equals(MysticalTerrain.MOD_ID);
		}, (selection, modification) -> {
			GenerationStep.Decoration[] steps = GenerationStep.Decoration.values();
			List<HolderSet<PlacedFeature>> features = selection.getBiome().getGenerationSettings().features();
			// Gathered first: removing while walking the biome's own lists would change them underneath us.
			List<Runnable> removals = new ArrayList<>();
			for (int i = 0; i < features.size() && i < steps.length; i++) {
				GenerationStep.Decoration step = steps[i];
				for (Holder<PlacedFeature> feature : features.get(i)) {
					if (feature.unwrapKey().isPresent() && !isUndergrowth(feature.unwrapKey().get()) && growsVanillaTree(feature.value())) {
						ResourceKey<PlacedFeature> key = feature.unwrapKey().get();
						removals.add(() -> modification.getGenerationSettings().removeFeature(step, key));
					}
				}
			}
			removals.forEach(Runnable::run);
		});
	}

	private static boolean isUndergrowth(ResourceKey<PlacedFeature> key) {
		return key.identifier().getNamespace().equals(MysticalTerrain.MOD_ID) && key.identifier().getPath().startsWith("undergrowth/");
	}

	private static boolean growsVanillaTree(PlacedFeature feature) {
		return feature.getFeatures().anyMatch(f -> f.feature() == Feature.TREE || f.feature() == Feature.FALLEN_TREE || f.feature() == Feature.ROOT_SYSTEM);
	}
}
