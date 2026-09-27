package com.vexira.mysticalterrain.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Grasses, ferns and small mushrooms: they grow on anything in #minecraft:dirt and don't spread or turn into vanilla plants.
public class PlantBlock extends VegetationBlock {
	public static final MapCodec<PlantBlock> CODEC = simpleCodec(PlantBlock::new);
	private static final VoxelShape SHAPE = Block.column(12.0, 0.0, 13.0);

	public PlantBlock(Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE.move(state.getOffset(pos));
	}
}
