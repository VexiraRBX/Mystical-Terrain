package com.vexira.mysticalterrain.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.vexira.mysticalterrain.MysticalTerrain;
import com.vexira.mysticalterrain.WorldSizeCheck;
import com.vexira.mysticalterrain.block.MysticalBlocks;
import com.vexira.mysticalterrain.block.WoodSet;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;

public class MysticalTerrainClient implements ClientModInitializer {
	// Built-in resource pack with every block and item texture at 16x; it sits over the 32x ones when selected.
	private static final Identifier SIXTEEN = MysticalTerrain.id("16x");

	@Override
	public void onInitializeClient() {
		ResourceLoader.registerBuiltinPack(SIXTEEN, FabricLoader.getInstance().getModContainer(MysticalTerrain.MOD_ID).orElseThrow(),
				Component.translatable("pack.mysticalterrain.16x"), PackActivationType.NORMAL);
		KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.mysticalterrain.toggle_16x", InputConstants.Type.KEYSYM,
				InputConstants.KEY_F7, KeyMapping.Category.register(MysticalTerrain.id("controls"))));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggle.consumeClick()) {
				toggleSixteen(client);
			}
		});
		for (WoodSet set : MysticalBlocks.WOOD) {
			BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, set.door(), set.trapdoor());
		}
		MysticalBlocks.PLANTS.forEach(block -> BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT));
		ClientConfigurationNetworking.registerGlobalReceiver(WorldSizeCheck.TYPE, (payload, context) -> {
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (!ClientPlayNetworking.canSend(WorldSizeCheck.TYPE)) {
				sender.disconnect(Component.literal("Mystical Terrain's bigger world sends block positions differently, so you can only join servers that also run Mystical Terrain."));
			}
		});
	}

	// Same as ticking the pack in the Resource Packs screen: saved to options.txt, then the textures reload.
	private static void toggleSixteen(Minecraft client) {
		PackRepository packs = client.getResourcePackRepository();
		String id = SIXTEEN.toString();
		boolean on = !packs.getSelectedIds().contains(id);
		if (on ? !packs.addPack(id) : !packs.removePack(id)) {
			return;
		}
		client.options.updateResourcePacks(packs);
		client.gui.setOverlayMessage(Component.translatable(on ? "message.mysticalterrain.16x_on" : "message.mysticalterrain.16x_off"), false);
	}
}
