package net.minecraft.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockLilyPad extends BlockBush {
   protected static final AxisAlignedBB LILY_PAD_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.09375D, 0.9375D);

   protected BlockLilyPad() {
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      if (!(entityIn instanceof EntityBoat)) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, LILY_PAD_AABB);
      }

   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
      if (entityIn instanceof EntityBoat) {
         worldIn.destroyBlock(new BlockPos(pos), true);
      }

   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return LILY_PAD_AABB;
   }

   protected boolean canSustainBush(IBlockState var1) {
      return state.getBlock() == Blocks.WATER || state.getMaterial() == Material.ICE;
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      if (pos.getY() >= 0 && pos.getY() < 256) {
         IBlockState iblockstate = worldIn.getBlockState(pos.down());
         Material material = iblockstate.getMaterial();
         return material == Material.WATER && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0 || material == Material.ICE;
      } else {
         return false;
      }
   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }
}
