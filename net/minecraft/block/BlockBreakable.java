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
      this(var1, var2, var1.getMaterialMapColor());
   }

   protected BlockBreakable(Material var1, boolean var2, MapColor var3) {
      super(var1, var3);
      this.ignoreSimilarity = var2;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      IBlockState var5 = var2.getBlockState(var3.offset(var4));
      Block var6 = var5.getBlock();
      if (this == Blocks.GLASS || this == Blocks.STAINED_GLASS) {
         if (var1 != var5) {
            return true;
         }

         if (var6 == this) {
            return false;
         }
      }

      return !this.ignoreSimilarity && var6 == this ? false : super.shouldSideBeRendered(var1, var2, var3, var4);
   }
}
