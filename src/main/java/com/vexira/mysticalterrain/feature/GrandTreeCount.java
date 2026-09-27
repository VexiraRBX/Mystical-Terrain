package com.vexira.mysticalterrain.feature;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.vexira.mysticalterrain.MysticalTerrain;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

// How many of one tree a chunk grows, by the biome at the middle of the chunk, so a single placed feature can grow a tree at
// its own rate in every biome. Rates come from grand_tree_rates.json; below one they're the chance of a tree per chunk.
public class GrandTreeCount extends PlacementModifier {
	private static final String RATES = "/data/" + MysticalTerrain.MOD_ID + "/grand_tree_rates.json";
	private static final Map<String, Map<String, Double>> TABLE = load();
	public static final MapCodec<GrandTreeCount> CODEC = Identifier.CODEC.fieldOf("tree").xmap(GrandTreeCount::new, count -> count.tree);
	public static final PlacementModifierType<GrandTreeCount> TYPE = () -> CODEC;

	private final Identifier tree;
	private final Map<String, Double> rates;

	private GrandTreeCount(Identifier tree) {
		this.tree = tree;
		this.rates = TABLE.getOrDefault(tree.toString(), Map.of());
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
		int x = pos.getX() + 8, z = pos.getZ() + 8;
		BlockPos middle = new BlockPos(x, context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
		double rate = context.getLevel().getBiome(middle).unwrapKey().map(key -> rates.getOrDefault(key.identifier().toString(), 0.0)).orElse(0.0);
		int count = (int) rate + (random.nextDouble() < rate - Math.floor(rate) ? 1 : 0);
		return Stream.generate(() -> pos).limit(count);
	}

	@Override
	public PlacementModifierType<?> type() {
		return TYPE;
	}

	private static Map<String, Map<String, Double>> load() {
		try (InputStream in = GrandTreeCount.class.getResourceAsStream(RATES)) {
			if (in == null) {
				throw new IllegalStateException("Missing " + RATES);
			}
			return Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.DOUBLE))
					.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
					.getOrThrow(error -> new IllegalStateException("Couldn't read " + RATES + ": " + error));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
