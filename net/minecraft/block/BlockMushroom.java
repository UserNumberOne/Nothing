package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;

public class BlockMushroom extends BlockBush implements IGrowable {
   protected static final AxisAlignedBB MUSHROOM_AABB = new AxisAlignedBB(0.30000001192092896D, 0.0D, 0.30000001192092896D, 0.699999988079071D, 0.4000000059604645D, 0.699999988079071D);

   protected BlockMushroom() {
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return MUSHROOM_AABB;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var4.nextInt(25) == 0) {
         int var5 = 5;
         boolean var6 = true;

         for(BlockPos var8 : BlockPos.getAllInBoxMutable(var2.add(-4, -1, -4), var2.add(4, 1, 4))) {
            if (var1.getBlockState(var8).getBlock() == this) {
               --var5;
               if (var5 <= 0) {
                  return;
               }
            }
         }

         BlockPos var9 = var2.add(var4.nextInt(3) - 1, var4.nextInt(2) - var4.nextInt(2), var4.nextInt(3) - 1);

         for(int var10 = 0; var10 < 4; ++var10) {
            if (var1.isAirBlock(var9) && this.canBlockStay(var1, var9, this.getDefaultState())) {
               var2 = var9;
            }

            var9 = var2.add(var4.nextInt(3) - 1, var4.nextInt(2) - var4.nextInt(2), var4.nextInt(3) - 1);
         }

         if (var1.isAirBlock(var9) && this.canBlockStay(var1, var9, this.getDefaultState())) {
            var1.setBlockState(var9, this.getDefaultState(), 2);
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) && this.canBlockStay(var1, var2, this.getDefaultState());
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.isFullBlock();
   }

   public boolean canBlockStay(World var1, BlockPos var2, IBlockState var3) {
      if (var2.getY() >= 0 && var2.getY() < 256) {
         IBlockState var4 = var1.getBlockState(var2.down());
         return var4.getBlock() == Blocks.MYCELIUM ? true : (var4.getBlock() == Blocks.DIRT && var4.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL ? true : var1.getLight(var2) < 13 && var4.getBlock().canSustainPlant(var4, var1, var2.down(), EnumFacing.UP, this));
      } else {
         return false;
      }
   }

   public boolean generateBigMushroom(World var1, BlockPos var2, IBlockState var3, Random var4) {
      var1.setBlockToAir(var2);
      WorldGenBigMushroom var5 = null;
      if (this == Blocks.BROWN_MUSHROOM) {
         var5 = new WorldGenBigMushroom(Blocks.BROWN_MUSHROOM_BLOCK);
      } else if (this == Blocks.RED_MUSHROOM) {
         var5 = new WorldGenBigMushroom(Blocks.RED_MUSHROOM_BLOCK);
      }

      if (var5 != null && var5.generate(var1, var4, var2)) {
         return true;
      } else {
         var1.setBlockState(var2, var3, 3);
         return false;
      }
   }

   public boolean canGrow(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return true;
   }

   public boolean canUseBonemeal(World var1, Random var2, BlockPos var3, IBlockState var4) {
      return (double)var2.nextFloat() < 0.4D;
   }

   public void grow(World var1, Random var2, BlockPos var3, IBlockState var4) {
      this.generateBigMushroom(var1, var3, var4, var2);
   }
}
