package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAir extends Block {
   protected BlockAir() {
      super(Material.AIR);
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.INVISIBLE;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canCollideCheck(IBlockState var1, boolean var2) {
      return false;
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }
}
