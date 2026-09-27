package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexira.mysticalterrain.formation.FormationKind;
import com.vexira.mysticalterrain.formation.FormationParams;
import com.vexira.mysticalterrain.formation.Material;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * anchor only matters to the structure, which has to find its own spot: on the surface, on a cave floor at or below
 * elevation (the Nether), or floating at elevation. A feature takes whatever its placement gives it.
 */
public record FormationConfiguration(
		FormationKind kind,
		IntProvider size,
		IntProvider count,
		FloatProvider lean,
		FloatProvider girth,
		int sides,
		Map<Material, BlockStateProvider> materials,
		Optional<ResourceKey<LootTable>> loot,
		Anchor anchor,
		IntProvider elevation
) implements FeatureConfiguration {
	public static final Codec<FormationConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			named(FormationKind.class).fieldOf("kind").forGetter(FormationConfiguration::kind),
			IntProvider.codec(1, 160).fieldOf("size").forGetter(FormationConfiguration::size),
			IntProvider.codec(-1, 16).optionalFieldOf("count", ConstantInt.of(-1)).forGetter(FormationConfiguration::count),
			FloatProvider.codec(0, 1.5F).optionalFieldOf("lean", ConstantFloat.of(0.5F)).forGetter(FormationConfiguration::lean),
			FloatProvider.codec(0.3F, 3).optionalFieldOf("girth", ConstantFloat.of(1)).forGetter(FormationConfiguration::girth),
			Codec.intRange(3, 12).optionalFieldOf("sides", 6).forGetter(FormationConfiguration::sides),
			Codec.unboundedMap(named(Material.class), BlockStateProvider.CODEC).fieldOf("materials").forGetter(FormationConfiguration::materials),
			ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("loot").forGetter(FormationConfiguration::loot),
			named(Anchor.class).optionalFieldOf("anchor", Anchor.SURFACE).forGetter(FormationConfiguration::anchor),
			IntProvider.codec(-64, 320).optionalFieldOf("elevation", ConstantInt.of(100)).forGetter(FormationConfiguration::elevation)
	).apply(instance, FormationConfiguration::new));

	public enum Anchor {
		SURFACE,
		FLOOR,
		FLOAT
	}

	private static <E extends Enum<E>> Codec<E> named(Class<E> type) {
		return Codec.STRING.comapFlatMap(name -> {
			try {
				return DataResult.success(Enum.valueOf(type, name.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException e) {
				return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
			}
		}, value -> value.name().toLowerCase(Locale.ROOT));
	}

	public FormationParams sample(RandomSource random) {
		return new FormationParams(kind, size.sample(random), count.sample(random), lean.sample(random), girth.sample(random), sides,
				random.nextLong());
	}

	// Anything missing falls back to the body; with no body either, that part is left out.
	public BlockStateProvider material(Material material) {
		BlockStateProvider provider = materials.get(material);
		return provider != null ? provider : materials.get(Material.BODY);
	}
}
