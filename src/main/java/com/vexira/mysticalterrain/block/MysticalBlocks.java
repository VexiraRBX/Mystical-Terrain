package com.vexira.mysticalterrain.block;

import com.vexira.mysticalterrain.MysticalTerrain;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

// Registration order is the creative tab's order: realm by realm.
public final class MysticalBlocks {
	private static final List<Item> TAB = new ArrayList<>();
	public static final List<WoodSet> WOOD = new ArrayList<>();
	public static final List<Block> LEAVES = new ArrayList<>();
	public static final List<Block> PLANTS = new ArrayList<>();
	private static final List<Block> STONES = new ArrayList<>();
	private static final List<Block> GROUND = new ArrayList<>();

	public static final WoodSet MYSTWOOD = wood("mystwood", MapColor.TERRACOTTA_RED, MapColor.COLOR_BROWN, SoundType.WOOD, true);
	public static final Block MYSTWOOD_LEAVES = leaves("mystwood_leaves", MapColor.COLOR_CYAN, SoundType.GRASS, 0x3F7068, 0);
	public static final Block STARBLOOM_BLOSSOMS = leaves("starbloom_blossoms", MapColor.COLOR_PINK, SoundType.CHERRY_LEAVES, 0xF0C8DA, 0);
	public static final Block RUNESTONE = stone("runestone", MapColor.TERRACOTTA_CYAN, SoundType.STONE);
	public static final Block RUNESTONE_BRICKS = bricks("runestone_bricks", MapColor.TERRACOTTA_CYAN, SoundType.STONE);

	public static final WoodSet MAPLE = wood("maple", MapColor.TERRACOTTA_ORANGE, MapColor.TERRACOTTA_GRAY, SoundType.WOOD, true);
	public static final Block MAPLE_LEAVES = leaves("maple_leaves", MapColor.COLOR_RED, SoundType.GRASS, 0xB83420, 0);
	public static final Block GOLDEN_MAPLE_LEAVES = leaves("golden_maple_leaves", MapColor.COLOR_YELLOW, SoundType.GRASS, 0xDCA430, 0);
	public static final Block RUSSET_GRANITE = stone("russet_granite", MapColor.TERRACOTTA_RED, SoundType.STONE);
	public static final Block RUSSET_GRANITE_BRICKS = bricks("russet_granite_bricks", MapColor.TERRACOTTA_RED, SoundType.STONE);
	public static final Block LEAF_MOLD = soil("leaf_mold", Blocks.ROOTED_DIRT, MapColor.PODZOL);
	public static final Block GOLDENROD = flower("goldenrod", MobEffects.SATURATION, 0.35F, 0);
	public static final Block AUTUMN_FERN = plant("autumn_fern", Blocks.FERN, 0);

	public static final WoodSet RIMEWOOD = wood("rimewood", MapColor.COLOR_LIGHT_GRAY, MapColor.TERRACOTTA_WHITE, SoundType.WOOD, true);
	public static final Block RIMEWOOD_LEAVES = leaves("rimewood_leaves", MapColor.COLOR_LIGHT_BLUE, SoundType.GRASS, 0x9CC0C6, 0);
	public static final Block RIMESTONE = stone("rimestone", MapColor.ICE, SoundType.CALCITE);
	public static final Block RIMESTONE_BRICKS = bricks("rimestone_bricks", MapColor.ICE, SoundType.CALCITE);
	public static final Block PERMAFROST = soil("permafrost", Blocks.DIRT, MapColor.TERRACOTTA_LIGHT_GRAY);
	public static final Block FROST_FERN = plant("frost_fern", Blocks.FERN, 0);
	public static final Block SNOWBELL = flower("snowbell", MobEffects.SLOWNESS, 8.0F, 0);

	public static final WoodSet CHARWOOD = wood("charwood", MapColor.COLOR_BLACK, MapColor.COLOR_BLACK, SoundType.NETHER_WOOD, false);
	public static final Block EMBER_LEAVES = leaves("ember_leaves", MapColor.TERRACOTTA_RED, SoundType.GRASS, 0xC2481F, 4);
	public static final Block ASHSTONE = stone("ashstone", MapColor.DEEPSLATE, SoundType.BASALT);
	public static final Block ASHSTONE_BRICKS = bricks("ashstone_bricks", MapColor.DEEPSLATE, SoundType.DEEPSLATE_BRICKS);
	public static final Block ASH_SOIL = soil("ash_soil", Blocks.DIRT, MapColor.COLOR_GRAY);
	public static final Block FIRE_LILY = flower("fire_lily", MobEffects.FIRE_RESISTANCE, 4.0F, 6);
	public static final Block ASH_GRASS = plant("ash_grass", Blocks.SHORT_GRASS, 0);

	public static final WoodSet SILVERBARK = wood("silverbark", MapColor.TERRACOTTA_MAGENTA, MapColor.TERRACOTTA_WHITE, SoundType.CHERRY_WOOD, true);
	public static final Block SILVERBARK_LEAVES = leaves("silverbark_leaves", MapColor.COLOR_PURPLE, SoundType.AZALEA_LEAVES, 0xA585C2, 0);
	public static final Block WISTERIA_BLOSSOMS = leaves("wisteria_blossoms", MapColor.COLOR_PURPLE, SoundType.CHERRY_LEAVES, 0xA27ED4, 0);
	public static final Block MOONSTONE = stone("moonstone", MapColor.QUARTZ, SoundType.CALCITE);
	public static final Block MOONSTONE_BRICKS = bricks("moonstone_bricks", MapColor.QUARTZ, SoundType.CALCITE);
	public static final Block FAE_MOSS = soil("fae_moss", Blocks.MOSS_BLOCK, MapColor.COLOR_PURPLE);
	public static final Block MOONPETAL = flower("moonpetal", MobEffects.NIGHT_VISION, 5.0F, 0);
	public static final Block GLIMMERCAP = plant("glimmercap", Blocks.BROWN_MUSHROOM, 5);

	public static final WoodSet GINKGO = wood("ginkgo", MapColor.SAND, MapColor.TERRACOTTA_GRAY, SoundType.WOOD, true);
	public static final Block GINKGO_LEAVES = leaves("ginkgo_leaves", MapColor.PLANT, SoundType.GRASS, 0x6AA332, 0);
	public static final Block FOSSIL_STONE = stone("fossil_stone", MapColor.SAND, SoundType.STONE);
	public static final Block FOSSIL_STONE_BRICKS = bricks("fossil_stone_bricks", MapColor.SAND, SoundType.STONE);
	public static final Block PRIMAL_MOSS = soil("primal_moss", Blocks.MOSS_BLOCK, MapColor.PLANT);
	public static final Block HORSETAIL = plant("horsetail", Blocks.SHORT_GRASS, 0);
	public static final Block ROYAL_FERN = plant("royal_fern", Blocks.FERN, 0);

	public static final WoodSet GLOAMWOOD = wood("gloamwood", MapColor.TERRACOTTA_BLUE, MapColor.COLOR_BLACK, SoundType.WOOD, true);
	public static final Block GLOAMWOOD_LEAVES = leaves("gloamwood_leaves", MapColor.COLOR_BLUE, SoundType.GRASS, 0x43508F, 0);
	public static final Block GLOOMSTONE = stone("gloomstone", MapColor.TERRACOTTA_BLUE, SoundType.DEEPSLATE);
	public static final Block GLOOMSTONE_BRICKS = bricks("gloomstone_bricks", MapColor.TERRACOTTA_BLUE, SoundType.DEEPSLATE_BRICKS);
	public static final Block GLOAM_MOSS = soil("gloam_moss", Blocks.MOSS_BLOCK, MapColor.COLOR_BLUE);
	public static final Block NIGHTSHADE = flower("nightshade", MobEffects.WEAKNESS, 9.0F, 0);
	public static final Block DUSKBELL = flower("duskbell", MobEffects.BLINDNESS, 8.0F, 0);

	public static final WoodSet PALM = wood("palm", MapColor.SAND, MapColor.TERRACOTTA_BROWN, SoundType.BAMBOO_WOOD, true);
	public static final Block PALM_FRONDS = leaves("palm_fronds", MapColor.PLANT, SoundType.GRASS, 0x4A8F2A, 0);
	public static final Block MANGO_LEAVES = leaves("mango_leaves", MapColor.PLANT, SoundType.GRASS, 0x357A28, 0);
	public static final Block HIBISCUS = flower("hibiscus", MobEffects.REGENERATION, 8.0F, 0);
	public static final Block BIRD_OF_PARADISE = flower("bird_of_paradise", MobEffects.SPEED, 5.0F, 0);

	public static final WoodSet CYPRESS = wood("cypress", MapColor.TERRACOTTA_BROWN, MapColor.TERRACOTTA_GRAY, SoundType.WOOD, true);
	public static final Block CYPRESS_LEAVES = leaves("cypress_leaves", MapColor.TERRACOTTA_GREEN, SoundType.GRASS, 0x607436, 0);
	public static final Block MIRESTONE = stone("mirestone", MapColor.TERRACOTTA_GREEN, SoundType.TUFF);
	public static final Block MIRESTONE_BRICKS = bricks("mirestone_bricks", MapColor.TERRACOTTA_GREEN, SoundType.TUFF_BRICKS);
	public static final Block PEAT = soil("peat", Blocks.MUD, MapColor.TERRACOTTA_BLACK);
	public static final Block CATTAIL = plant("cattail", Blocks.SHORT_GRASS, 0);
	public static final Block MARSH_GRASS = plant("marsh_grass", Blocks.SHORT_GRASS, 0);

	public static final WoodSet OLIVE = wood("olive", MapColor.SAND, MapColor.COLOR_GRAY, SoundType.WOOD, true);
	public static final Block OLIVE_LEAVES = leaves("olive_leaves", MapColor.TERRACOTTA_GREEN, SoundType.GRASS, 0x7A8A68, 0);
	public static final Block SANDROCK = stone("sandrock", MapColor.TERRACOTTA_ORANGE, SoundType.STONE);
	public static final Block SANDROCK_BRICKS = bricks("sandrock_bricks", MapColor.TERRACOTTA_ORANGE, SoundType.STONE);
	public static final Block SUNBAKED_CLAY = soil("sunbaked_clay", Blocks.PACKED_MUD, MapColor.TERRACOTTA_ORANGE);
	public static final Block DESERT_SAGE = flower("desert_sage", MobEffects.JUMP_BOOST, 6.0F, 0);
	public static final Block DUNE_GRASS = plant("dune_grass", Blocks.SHORT_GRASS, 0);

	public static final WoodSet SPOREWOOD = wood("sporewood", MapColor.TERRACOTTA_WHITE, MapColor.TERRACOTTA_WHITE, SoundType.NETHER_WOOD, false);
	public static final Block SPORECAP_BLOCK = cap("sporecap_block", MapColor.COLOR_CYAN, 0);
	public static final Block GLOWCAP_BLOCK = cap("glowcap_block", MapColor.COLOR_ORANGE, 12);
	public static final Block SPORE_SOIL = soil("spore_soil", Blocks.ROOTED_DIRT, MapColor.TERRACOTTA_GRAY);
	public static final Block INKCAP = plant("inkcap", Blocks.BROWN_MUSHROOM, 0);
	public static final Block VIOLET_PUFFBALL = plant("violet_puffball", Blocks.BROWN_MUSHROOM, 0);

	public static final WoodSet STARBARK = wood("starbark", MapColor.COLOR_PURPLE, MapColor.COLOR_BLACK, SoundType.WOOD, true);
	public static final Block STARBARK_LEAVES = leaves("starbark_leaves", MapColor.COLOR_PURPLE, SoundType.AZALEA_LEAVES, 0x8048B8, 0);
	public static final Block STARSTONE = stone("starstone", MapColor.TERRACOTTA_PURPLE, SoundType.DEEPSLATE);
	public static final Block STARSTONE_BRICKS = bricks("starstone_bricks", MapColor.TERRACOTTA_PURPLE, SoundType.DEEPSLATE_BRICKS);
	public static final Block STARFLOWER = flower("starflower", MobEffects.GLOWING, 10.0F, 7);
	public static final Block VOIDBLOOM = flower("voidbloom", MobEffects.LEVITATION, 4.0F, 4);

	private MysticalBlocks() {
	}

	public static void register() {
		FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
		for (WoodSet set : WOOD) {
			StrippableBlockRegistry.register(set.log(), set.strippedLog());
			StrippableBlockRegistry.register(set.wood(), set.strippedWood());
			if (set.flammable()) {
				set.logs().forEach(block -> flammable.add(block, 5, 5));
				set.building().forEach(block -> flammable.add(block, 5, 20));
			}
		}
		LEAVES.stream().filter(block -> block != EMBER_LEAVES).forEach(block -> flammable.add(block, 30, 60));
		PLANTS.stream().filter(block -> block != FIRE_LILY && block != ASH_GRASS).forEach(block -> flammable.add(block, 60, 100));

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MysticalTerrain.id("blocks"), FabricItemGroup.builder()
				.title(Component.translatable("itemGroup.mysticalterrain.blocks"))
				.icon(() -> new ItemStack(MAPLE_LEAVES))
				.displayItems((parameters, output) -> TAB.forEach(output::accept))
				.build());
		// Also in vanilla's tabs: with many mods installed, the creative inventory pushes our tab onto a later page.
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
			for (WoodSet set : WOOD) {
				set.logs().forEach(entries::accept);
				set.building().forEach(entries::accept);
				entries.accept(set.door());
				entries.accept(set.trapdoor());
			}
			STONES.forEach(entries::accept);
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			WOOD.forEach(set -> entries.accept(set.log()));
			LEAVES.forEach(entries::accept);
			GROUND.forEach(entries::accept);
			PLANTS.forEach(entries::accept);
		});
	}

	private static WoodSet wood(String name, MapColor inside, MapColor bark, SoundType sound, boolean flammable) {
		Block log = register(name + "_log", RotatedPillarBlock::new, log(inside, bark, sound));
		Block wood = register(name + "_wood", RotatedPillarBlock::new, log(bark, bark, sound));
		Block strippedLog = register("stripped_" + name + "_log", RotatedPillarBlock::new, log(inside, inside, sound));
		Block strippedWood = register("stripped_" + name + "_wood", RotatedPillarBlock::new, log(inside, inside, sound));
		Block planks = register(name + "_planks", Block::new, copy(Blocks.OAK_PLANKS, inside, sound));
		Block stairs = register(name + "_stairs", p -> new StairBlock(planks.defaultBlockState(), p), copy(Blocks.OAK_STAIRS, inside, sound));
		Block slab = register(name + "_slab", SlabBlock::new, copy(Blocks.OAK_SLAB, inside, sound));
		Block fence = register(name + "_fence", FenceBlock::new, copy(Blocks.OAK_FENCE, inside, sound));
		Block gate = register(name + "_fence_gate", p -> new FenceGateBlock(WoodType.OAK, p), copy(Blocks.OAK_FENCE_GATE, inside, sound));
		Block door = register(name + "_door", p -> new DoorBlock(BlockSetType.OAK, p), copy(Blocks.OAK_DOOR, inside, sound), DoubleHighBlockItem::new);
		Block trapdoor = register(name + "_trapdoor", p -> new TrapDoorBlock(BlockSetType.OAK, p), copy(Blocks.OAK_TRAPDOOR, inside, sound));
		WoodSet set = new WoodSet(log, wood, strippedLog, strippedWood, planks, stairs, slab, fence, gate, door, trapdoor, flammable);
		WOOD.add(set);
		return set;
	}

	private static Block leaves(String name, MapColor color, SoundType sound, int particle, int light) {
		ColorParticleOption falling = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, 0xFF000000 | particle);
		Block block = register(name, p -> new UntintedParticleLeavesBlock(0.01F, falling, p),
				copy(Blocks.OAK_LEAVES, color, sound).lightLevel(state -> light));
		LEAVES.add(block);
		return block;
	}

	private static Block cap(String name, MapColor color, int light) {
		Block block = register(name, Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK).mapColor(color).lightLevel(state -> light));
		GROUND.add(block);
		return block;
	}

	private static Block stone(String name, MapColor color, SoundType sound) {
		Block block = register(name, Block::new, copy(Blocks.STONE, color, sound));
		STONES.add(block);
		return block;
	}

	private static Block bricks(String name, MapColor color, SoundType sound) {
		Block block = register(name, Block::new, copy(Blocks.STONE_BRICKS, color, sound));
		STONES.add(block);
		return block;
	}

	private static Block soil(String name, Block like, MapColor color) {
		Block block = register(name, Block::new, BlockBehaviour.Properties.ofFullCopy(like).mapColor(color));
		GROUND.add(block);
		return block;
	}

	private static Block flower(String name, Holder<MobEffect> effect, float seconds, int light) {
		Block block = register(name, p -> new FlowerBlock(effect, seconds, p), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY).lightLevel(state -> light));
		PLANTS.add(block);
		return block;
	}

	private static Block plant(String name, Block like, int light) {
		Block block = register(name, PlantBlock::new, BlockBehaviour.Properties.ofFullCopy(like).lightLevel(state -> light));
		PLANTS.add(block);
		return block;
	}

	private static BlockBehaviour.Properties log(MapColor top, MapColor side, SoundType sound) {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)
				.mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? top : side)
				.sound(sound);
	}

	private static BlockBehaviour.Properties copy(Block base, MapColor color, SoundType sound) {
		return BlockBehaviour.Properties.ofFullCopy(base).mapColor(color).sound(sound);
	}

	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		return register(name, factory, properties, BlockItem::new);
	}

	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties,
			BiFunction<Block, Item.Properties, Item> item) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, MysticalTerrain.id(name));
		T block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MysticalTerrain.id(name));
		TAB.add(Registry.register(BuiltInRegistries.ITEM, itemKey, item.apply(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix())));
		return block;
	}
}
