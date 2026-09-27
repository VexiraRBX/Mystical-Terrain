package com.vexira.mysticalterrain.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

// Branches and roots fall back to the trunk; everything else is skipped when missing.
public record TreeBlocks(
		BlockStateProvider trunk,
		Optional<BlockStateProvider> branch,
		Optional<BlockStateProvider> root,
		Optional<BlockStateProvider> leaves,
		Optional<BlockStateProvider> vein,
		Optional<BlockStateProvider> fruit,
		float fruitChance,
		Optional<BlockStateProvider> hanging,
		float hangingChance,
		IntProvider hangingLength,
		Optional<BlockStateProvider> cover,
		float coverChance
) {
	public static final Codec<TreeBlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.CODEC.fieldOf("trunk").forGetter(TreeBlocks::trunk),
			BlockStateProvider.CODEC.optionalFieldOf("branch").forGetter(TreeBlocks::branch),
			BlockStateProvider.CODEC.optionalFieldOf("root").forGetter(TreeBlocks::root),
			BlockStateProvider.CODEC.optionalFieldOf("leaves").forGetter(TreeBlocks::leaves),
			BlockStateProvider.CODEC.optionalFieldOf("vein").forGetter(TreeBlocks::vein),
			BlockStateProvider.CODEC.optionalFieldOf("fruit").forGetter(TreeBlocks::fruit),
			Codec.floatRange(0, 1).optionalFieldOf("fruit_chance", 0.03F).forGetter(TreeBlocks::fruitChance),
			BlockStateProvider.CODEC.optionalFieldOf("hanging").forGetter(TreeBlocks::hanging),
			Codec.floatRange(0, 1).optionalFieldOf("hanging_chance", 0.08F).forGetter(TreeBlocks::hangingChance),
			IntProvider.codec(1, 32).optionalFieldOf("hanging_length", UniformInt.of(1, 4)).forGetter(TreeBlocks::hangingLength),
			BlockStateProvider.CODEC.optionalFieldOf("cover").forGetter(TreeBlocks::cover),
			Codec.floatRange(0, 1).optionalFieldOf("cover_chance", 0.25F).forGetter(TreeBlocks::coverChance)
	).apply(instance, TreeBlocks::new));

	public BlockStateProvider branchOrTrunk() {
		return branch.orElse(trunk);
	}

	public BlockStateProvider rootOrTrunk() {
		return root.orElse(trunk);
	}
}
