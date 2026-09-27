package com.vexira.mysticalterrain;

import com.vexira.mysticalterrain.biome.MysticalBiomes;
import com.vexira.mysticalterrain.block.MysticalBlocks;
import com.vexira.mysticalterrain.biome.MysticalTrees;
import com.vexira.mysticalterrain.feature.BoulderConfiguration;
import com.vexira.mysticalterrain.feature.BoulderFeature;
import com.vexira.mysticalterrain.feature.ColossalTreeConfiguration;
import com.vexira.mysticalterrain.feature.ColossalTreeFeature;
import com.vexira.mysticalterrain.feature.FormationConfiguration;
import com.vexira.mysticalterrain.feature.FormationFeature;
import com.vexira.mysticalterrain.feature.FormationPiece;
import com.vexira.mysticalterrain.feature.FormationStructure;
import com.vexira.mysticalterrain.feature.GrandTreeConfiguration;
import com.vexira.mysticalterrain.feature.GrandTreeCount;
import com.vexira.mysticalterrain.feature.GrandTreeFeature;
import com.vexira.mysticalterrain.feature.GrandTreePiece;
import com.vexira.mysticalterrain.feature.GrandTreeStructure;
import com.vexira.mysticalterrain.feature.GroundCoverConfiguration;
import com.vexira.mysticalterrain.feature.GroundCoverFeature;
import com.vexira.mysticalterrain.feature.SpireConfiguration;
import com.vexira.mysticalterrain.feature.SpireFeature;
import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import terrablender.api.TerraBlenderApi;

public class MysticalTerrain implements ModInitializer, TerraBlenderApi {
	public static final String MOD_ID = "mysticalterrain";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Biomes and features are data-driven (see data/mysticalterrain); these are the custom types they use.
		Registry.register(BuiltInRegistries.FEATURE, id("colossal_tree"), new ColossalTreeFeature(ColossalTreeConfiguration.CODEC));
		Registry.register(BuiltInRegistries.FEATURE, id("spire"), new SpireFeature(SpireConfiguration.CODEC));
		Registry.register(BuiltInRegistries.FEATURE, id("boulder"), new BoulderFeature(BoulderConfiguration.CODEC));
		Registry.register(BuiltInRegistries.FEATURE, id("grand_tree"), new GrandTreeFeature(GrandTreeConfiguration.CODEC));
		Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, id("grand_tree_count"), GrandTreeCount.TYPE);
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id("grand_tree"), GrandTreeStructure.TYPE);
		Registry.register(BuiltInRegistries.STRUCTURE_PIECE, id("grand_tree"), GrandTreePiece.TYPE);
		Registry.register(BuiltInRegistries.FEATURE, id("formation"), new FormationFeature(FormationConfiguration.CODEC));
		Registry.register(BuiltInRegistries.FEATURE, id("ground_cover"), new GroundCoverFeature(GroundCoverConfiguration.CODEC));
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id("formation"), FormationStructure.TYPE);
		Registry.register(BuiltInRegistries.STRUCTURE_PIECE, id("formation"), FormationPiece.TYPE);
		MysticalBlocks.register();
		MysticalTrees.register();
		WorldSizeCheck.register();
		LOGGER.info("Mystical Terrain features registered");
	}

	@Override
	public void onTerraBlenderInitialized() {
		MysticalBiomes.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
