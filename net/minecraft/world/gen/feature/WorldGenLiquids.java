package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenLiquids extends WorldGenerator {
   private final Block block;

   public WorldGenLiquids(Block var1) {
      this.block = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      if (var1.getBlockState(var3.up()).getBlock() != Blocks.STONE) {
         return false;
      } else if (var1.getBlockState(var3.down()).getBlock() != Blocks.STONE) {
         return false;
      } else {
         IBlockState var4 = var1.getBlockState(var3);
         if (var4.getMaterial() != Material.AIR && var4.getBlock() != Blocks.STONE) {
            return false;
         } else {
            int var5 = 0;
            if (var1.getBlockState(var3.west()).getBlock() == Blocks.STONE) {
               ++var5;
            }

            if (var1.getBlockState(var3.east()).getBlock() == Blocks.STONE) {
               ++var5;
            }

            if (var1.getBlockState(var3.north()).getBlock() == Blocks.STONE) {
               ++var5;
            }

            if (var1.getBlockState(var3.south()).getBlock() == Blocks.STONE) {
               ++var5;
            }

            int var6 = 0;
            if (var1.isAirBlock(var3.west())) {
               ++var6;
            }

            if (var1.isAirBlock(var3.east())) {
               ++var6;
            }

            if (var1.isAirBlock(var3.north())) {
               ++var6;
            }

            if (var1.isAirBlock(var3.south())) {
               ++var6;
            }

            if (var5 == 3 && var6 == 1) {
               IBlockState var7 = this.block.getDefaultState();
               var1.setBlockState(var3, var7, 2);
               var1.immediateBlockTick(var3, var7, var2);
            }

            return true;
         }
      }
   }
}
