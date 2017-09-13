package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBreakable extends Block {
   private final boolean ignoreSimilarity;

   protected BlockBreakable(Material var1, boolean var2) {
      this(materialIn, ignoreSimilarityIn, materialIn.getMaterialMapColor());
   }

   protected BlockBreakable(Material var1, boolean var2, MapColor var3) {
      super(materialIn, mapColorIn);
      this.ignoreSimilarity = ignoreSimilarityIn;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
      Block block = iblockstate.getBlock();
      if (this == Blocks.GLASS || this == Blocks.STAINED_GLASS) {
         if (blockState != iblockstate) {
            return true;
         }

         if (block == this) {
            return false;
         }
      }

      return !this.ignoreSimilarity && block == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
   }
}
