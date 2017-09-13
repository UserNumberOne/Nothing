package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class WorldGeneratorBonusChest extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(IBlockState var4 = var1.getBlockState(var3); (var4.getBlock().isAir(var4, var1, var3) || var4.getBlock().isLeaves(var4, var1, var3)) && var3.getY() > 1; var4 = var1.getBlockState(var3)) {
         var3 = var3.down();
      }

      if (var3.getY() < 1) {
         return false;
      } else {
         var3 = var3.up();

         for(int var12 = 0; var12 < 4; ++var12) {
            BlockPos var5 = var3.add(var2.nextInt(4) - var2.nextInt(4), var2.nextInt(3) - var2.nextInt(3), var2.nextInt(4) - var2.nextInt(4));
            if (var1.isAirBlock(var5) && var1.getBlockState(var5.down()).isSideSolid(var1, var5.down(), EnumFacing.UP)) {
               var1.setBlockState(var5, Blocks.CHEST.getDefaultState(), 2);
               TileEntity var6 = var1.getTileEntity(var5);
               if (var6 instanceof TileEntityChest) {
                  ((TileEntityChest)var6).setLootTable(LootTableList.CHESTS_SPAWN_BONUS_CHEST, var2.nextLong());
               }

               BlockPos var7 = var5.east();
               BlockPos var8 = var5.west();
               BlockPos var9 = var5.north();
               BlockPos var10 = var5.south();
               if (var1.isAirBlock(var8) && var1.getBlockState(var8.down()).isSideSolid(var1, var8.down(), EnumFacing.UP)) {
                  var1.setBlockState(var8, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var7) && var1.getBlockState(var7.down()).isSideSolid(var1, var7.down(), EnumFacing.UP)) {
                  var1.setBlockState(var7, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var9) && var1.getBlockState(var9.down()).isSideSolid(var1, var9.down(), EnumFacing.UP)) {
                  var1.setBlockState(var9, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var10) && var1.getBlockState(var10.down()).isSideSolid(var1, var10.down(), EnumFacing.UP)) {
                  var1.setBlockState(var10, Blocks.TORCH.getDefaultState(), 2);
               }

               return true;
            }
         }

         return false;
      }
   }
}
