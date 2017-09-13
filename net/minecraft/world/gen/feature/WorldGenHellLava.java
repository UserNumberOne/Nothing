package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenHellLava extends WorldGenerator {
   private final Block block;
   private final boolean insideRock;

   public WorldGenHellLava(Block var1, boolean var2) {
      this.block = var1;
      this.insideRock = var2;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      if (var1.getBlockState(var3.up()).getBlock() != Blocks.NETHERRACK) {
         return false;
      } else if (var1.getBlockState(var3).getMaterial() != Material.AIR && var1.getBlockState(var3).getBlock() != Blocks.NETHERRACK) {
         return false;
      } else {
         int var4 = 0;
         if (var1.getBlockState(var3.west()).getBlock() == Blocks.NETHERRACK) {
            ++var4;
         }

         if (var1.getBlockState(var3.east()).getBlock() == Blocks.NETHERRACK) {
            ++var4;
         }

         if (var1.getBlockState(var3.north()).getBlock() == Blocks.NETHERRACK) {
            ++var4;
         }

         if (var1.getBlockState(var3.south()).getBlock() == Blocks.NETHERRACK) {
            ++var4;
         }

         if (var1.getBlockState(var3.down()).getBlock() == Blocks.NETHERRACK) {
            ++var4;
         }

         int var5 = 0;
         if (var1.isAirBlock(var3.west())) {
            ++var5;
         }

         if (var1.isAirBlock(var3.east())) {
            ++var5;
         }

         if (var1.isAirBlock(var3.north())) {
            ++var5;
         }

         if (var1.isAirBlock(var3.south())) {
            ++var5;
         }

         if (var1.isAirBlock(var3.down())) {
            ++var5;
         }

         if (!this.insideRock && var4 == 4 && var5 == 1 || var4 == 5) {
            IBlockState var6 = this.block.getDefaultState();
            var1.setBlockState(var3, var6, 2);
            var1.immediateBlockTick(var3, var6, var2);
         }

         return true;
      }
   }
}
