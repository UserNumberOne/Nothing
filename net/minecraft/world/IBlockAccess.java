package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface IBlockAccess {
   @Nullable
   TileEntity getTileEntity(BlockPos var1);

   IBlockState getBlockState(BlockPos var1);

   boolean isAirBlock(BlockPos var1);

   int getStrongPower(BlockPos var1, EnumFacing var2);
}
