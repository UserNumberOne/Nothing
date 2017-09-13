package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenShrub extends WorldGenTrees {
   private final IBlockState leavesMetadata;
   private final IBlockState woodMetadata;

   public WorldGenShrub(IBlockState var1, IBlockState var2) {
      super(false);
      this.woodMetadata = var1;
      this.leavesMetadata = var2;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(IBlockState var4 = var1.getBlockState(var3); (var4.getBlock().isAir(var4, var1, var3) || var4.getBlock().isLeaves(var4, var1, var3)) && var3.getY() > 0; var4 = var1.getBlockState(var3)) {
         var3 = var3.down();
      }

      IBlockState var14 = var1.getBlockState(var3);
      if (var14.getBlock().canSustainPlant(var14, var1, var3, EnumFacing.UP, (BlockSapling)Blocks.SAPLING)) {
         var3 = var3.up();
         this.setBlockAndNotifyAdequately(var1, var3, this.woodMetadata);

         for(int var5 = var3.getY(); var5 <= var3.getY() + 2; ++var5) {
            int var6 = var5 - var3.getY();
            int var7 = 2 - var6;

            for(int var8 = var3.getX() - var7; var8 <= var3.getX() + var7; ++var8) {
               int var9 = var8 - var3.getX();

               for(int var10 = var3.getZ() - var7; var10 <= var3.getZ() + var7; ++var10) {
                  int var11 = var10 - var3.getZ();
                  if (Math.abs(var9) != var7 || Math.abs(var11) != var7 || var2.nextInt(2) != 0) {
                     BlockPos var12 = new BlockPos(var8, var5, var10);
                     var14 = var1.getBlockState(var12);
                     if (var14.getBlock().canBeReplacedByLeaves(var14, var1, var12)) {
                        this.setBlockAndNotifyAdequately(var1, var12, this.leavesMetadata);
                     }
                  }
               }
            }
         }
      }

      return true;
   }
}
