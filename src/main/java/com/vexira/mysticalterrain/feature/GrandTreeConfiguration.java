package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vexira.mysticalterrain.tree.Growth;
import com.vexira.mysticalterrain.tree.TreeParams;
import com.vexira.mysticalterrain.tree.TreeStyle;
import java.util.Locale;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * reach is how far the tree may spread sideways. As a feature anything over 15 is moved in toward the middle of the chunk and
 * capped at 23 so it stays inside the area a feature may write to; as a landmark structure it can go up to 120. growth only
 * matters to the grown styles, canopy and layered.
 */
public record GrandTreeConfiguration(
		TreeStyle style,
		IntProvider height,
		FloatProvider trunkRadius,
		FloatProvider crown,
		float lean,
		float gnarl,
		float droop,
		float shape,
		float density,
		IntProvider roots,
		IntProvider branches,
		int veins,
		int reach,
		boolean aquatic,
		TreeBlocks blocks,
		Growth growth
) implements FeatureConfiguration {
	private static final Codec<TreeStyle> STYLE = Codec.STRING.comapFlatMap(name -> {
		try {
			return DataResult.success(TreeStyle.valueOf(name.toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException e) {
			return DataResult.error(() -> "Unknown tree style: " + name);
		}
	}, style -> style.name().toLowerCase(Locale.ROOT));

	private static final Growth G = Growth.DEFAULT;
	private static final Codec<Growth> GROWTH = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(1, 24).optionalFieldOf("lobes", G.lobes()).forGetter(Growth::lobes),
			Codec.doubleRange(0, 1).optionalFieldOf("spread", G.spread()).forGetter(Growth::spread),
			Codec.doubleRange(0, 1).optionalFieldOf("flat", G.flat()).forGetter(Growth::flat),
			Codec.doubleRange(0, 1).optionalFieldOf("tiers", G.tiers()).forGetter(Growth::tiers),
			Codec.doubleRange(0, 1).optionalFieldOf("lopside", G.lopside()).forGetter(Growth::lopside),
			Codec.doubleRange(0, 4).optionalFieldOf("bends", G.bends()).forGetter(Growth::bends),
			Codec.doubleRange(0, 1).optionalFieldOf("swing", G.swing()).forGetter(Growth::swing),
			Codec.intRange(1, 5).optionalFieldOf("forks", G.forks()).forGetter(Growth::forks),
			Codec.doubleRange(0, 1).optionalFieldOf("split", G.split()).forGetter(Growth::split),
			Codec.doubleRange(0, 1).optionalFieldOf("sprout", G.sprout()).forGetter(Growth::sprout),
			Codec.doubleRange(-1, 1).optionalFieldOf("tropism", G.tropism()).forGetter(Growth::tropism),
			Codec.doubleRange(0.3, 3).optionalFieldOf("twig", G.twig()).forGetter(Growth::twig),
			Codec.doubleRange(0, 1.5).optionalFieldOf("drip", G.drip()).forGetter(Growth::drip),
			Codec.doubleRange(0, 1).optionalFieldOf("hollow", G.hollow()).forGetter(Growth::hollow),
			Codec.doubleRange(0, 1).optionalFieldOf("buttress", G.buttress()).forGetter(Growth::buttress),
			Codec.doubleRange(0, 1).optionalFieldOf("jitter", G.jitter()).forGetter(Growth::jitter)
	).apply(instance, Growth::new));

	public static final Codec<GrandTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			STYLE.fieldOf("style").forGetter(GrandTreeConfiguration::style),
			IntProvider.codec(4, 256).fieldOf("height").forGetter(GrandTreeConfiguration::height),
			FloatProvider.codec(0.5F, 24).fieldOf("trunk_radius").forGetter(GrandTreeConfiguration::trunkRadius),
			FloatProvider.codec(1, 100).fieldOf("crown").forGetter(GrandTreeConfiguration::crown),
			Codec.floatRange(0, 1.5F).optionalFieldOf("lean", 0.1F).forGetter(GrandTreeConfiguration::lean),
			Codec.floatRange(0, 1.5F).optionalFieldOf("gnarl", 0.35F).forGetter(GrandTreeConfiguration::gnarl),
			Codec.floatRange(0, 2).optionalFieldOf("droop", 0F).forGetter(GrandTreeConfiguration::droop),
			Codec.floatRange(0, 1).optionalFieldOf("shape", 0.5F).forGetter(GrandTreeConfiguration::shape),
			Codec.floatRange(0.1F, 1).optionalFieldOf("density", 0.85F).forGetter(GrandTreeConfiguration::density),
			IntProvider.codec(-1, 32).optionalFieldOf("roots", ConstantInt.of(-1)).forGetter(GrandTreeConfiguration::roots),
			IntProvider.codec(-1, 32).optionalFieldOf("branches", ConstantInt.of(-1)).forGetter(GrandTreeConfiguration::branches),
			Codec.intRange(0, 32).optionalFieldOf("veins", 0).forGetter(GrandTreeConfiguration::veins),
			Codec.intRange(4, 120).optionalFieldOf("reach", 14).forGetter(GrandTreeConfiguration::reach),
			Codec.BOOL.optionalFieldOf("aquatic", false).forGetter(GrandTreeConfiguration::aquatic),
			TreeBlocks.CODEC.fieldOf("blocks").forGetter(GrandTreeConfiguration::blocks),
			GROWTH.optionalFieldOf("growth", Growth.DEFAULT).forGetter(GrandTreeConfiguration::growth)
	).apply(instance, GrandTreeConfiguration::new));

	public TreeParams sample(RandomSource random, int reach, int ceiling) {
		return new TreeParams(style, height.sample(random), trunkRadius.sample(random), crown.sample(random), lean, gnarl,
				roots.sample(random), branches.sample(random), droop, shape, density, veins, reach, ceiling, random.nextLong(), growth);
	}
}
