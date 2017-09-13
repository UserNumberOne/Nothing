package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenMegaPineTree extends WorldGenHugeTrees {
   private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
   private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
   private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
   private final boolean useBaseHeight;

   public WorldGenMegaPineTree(boolean var1, boolean var2) {
      super(var1, 13, 15, TRUNK, LEAF);
      this.useBaseHeight = var2;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = this.getHeight(var2);
      if (!this.ensureGrowable(var1, var2, var3, var4)) {
         return false;
      } else {
         this.createCrown(var1, var3.getX(), var3.getZ(), var3.getY() + var4, 0, var2);

         for(int var5 = 0; var5 < var4; ++var5) {
            if (this.isAirLeaves(var1, var3.up(var5))) {
               this.setBlockAndNotifyAdequately(var1, var3.up(var5), this.woodMetadata);
            }

            if (var5 < var4 - 1) {
               if (this.isAirLeaves(var1, var3.add(1, var5, 0))) {
                  this.setBlockAndNotifyAdequately(var1, var3.add(1, var5, 0), this.woodMetadata);
               }

               if (this.isAirLeaves(var1, var3.add(1, var5, 1))) {
                  this.setBlockAndNotifyAdequately(var1, var3.add(1, var5, 1), this.woodMetadata);
               }

               if (this.isAirLeaves(var1, var3.add(0, var5, 1))) {
                  this.setBlockAndNotifyAdequately(var1, var3.add(0, var5, 1), this.woodMetadata);
               }
            }
         }

         return true;
      }
   }

   private void createCrown(World var1, int var2, int var3, int var4, int var5, Random var6) {
      int var7 = var6.nextInt(5) + (this.useBaseHeight ? this.baseHeight : 3);
      int var8 = 0;

      for(int var9 = var4 - var7; var9 <= var4; ++var9) {
         int var10 = var4 - var9;
         int var11 = var5 + MathHelper.floor((float)var10 / (float)var7 * 3.5F);
         this.growLeavesLayerStrict(var1, new BlockPos(var2, var9, var3), var11 + (var10 > 0 && var11 == var8 && (var9 & 1) == 0 ? 1 : 0));
         var8 = var11;
      }

   }

   public void generateSaplings(World var1, Random var2, BlockPos var3) {
      this.placePodzolCircle(var1, var3.west().north());
      this.placePodzolCircle(var1, var3.east(2).north());
      this.placePodzolCircle(var1, var3.west().south(2));
      this.placePodzolCircle(var1, var3.east(2).south(2));

      for(int var4 = 0; var4 < 5; ++var4) {
         int var5 = var2.nextInt(64);
         int var6 = var5 % 8;
         int var7 = var5 / 8;
         if (var6 == 0 || var6 == 7 || var7 == 0 || var7 == 7) {
            this.placePodzolCircle(var1, var3.add(-3 + var6, 0, -3 + var7));
         }
      }

   }

   private void placePodzolCircle(World var1, BlockPos var2) {
      for(int var3 = -2; var3 <= 2; ++var3) {
         for(int var4 = -2; var4 <= 2; ++var4) {
            if (Math.abs(var3) != 2 || Math.abs(var4) != 2) {
               this.placePodzolAt(var1, var2.add(var3, 0, var4));
            }
         }
      }

   }

   private void placePodzolAt(World var1, BlockPos var2) {
      for(int var3 = 2; var3 >= -3; --var3) {
         BlockPos var4 = var2.up(var3);
         IBlockState var5 = var1.getBlockState(var4);
         Block var6 = var5.getBlock();
         if (var6.canSustainPlant(var5, var1, var4, EnumFacing.UP, (BlockSapling)Blocks.SAPLING)) {
            this.setBlockAndNotifyAdequately(var1, var4, PODZOL);
            break;
         }

         if (var5.getMaterial() != Material.AIR && var3 < 0) {
            break;
         }
      }

   }

   private boolean isAirLeaves(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      return var3.getBlock().isAir(var3, var1, var2) || var3.getBlock().isLeaves(var3, var1, var2);
   }
}
