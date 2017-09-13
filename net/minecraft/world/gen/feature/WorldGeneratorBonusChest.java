package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class WorldGeneratorBonusChest extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(IBlockState var4 = var1.getBlockState(var3); (var4.getMaterial() == Material.AIR || var4.getMaterial() == Material.LEAVES) && var3.getY() > 1; var4 = var1.getBlockState(var3)) {
         var3 = var3.down();
      }

      if (var3.getY() < 1) {
         return false;
      } else {
         var3 = var3.up();

         for(int var5 = 0; var5 < 4; ++var5) {
            BlockPos var6 = var3.add(var2.nextInt(4) - var2.nextInt(4), var2.nextInt(3) - var2.nextInt(3), var2.nextInt(4) - var2.nextInt(4));
            if (var1.isAirBlock(var6) && var1.getBlockState(var6.down()).isFullyOpaque()) {
               var1.setBlockState(var6, Blocks.CHEST.getDefaultState(), 2);
               TileEntity var7 = var1.getTileEntity(var6);
               if (var7 instanceof TileEntityChest) {
                  ((TileEntityChest)var7).setLootTable(LootTableList.CHESTS_SPAWN_BONUS_CHEST, var2.nextLong());
               }

               BlockPos var8 = var6.east();
               BlockPos var9 = var6.west();
               BlockPos var10 = var6.north();
               BlockPos var11 = var6.south();
               if (var1.isAirBlock(var9) && var1.getBlockState(var9.down()).isFullyOpaque()) {
                  var1.setBlockState(var9, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var8) && var1.getBlockState(var8.down()).isFullyOpaque()) {
                  var1.setBlockState(var8, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var10) && var1.getBlockState(var10.down()).isFullyOpaque()) {
                  var1.setBlockState(var10, Blocks.TORCH.getDefaultState(), 2);
               }

               if (var1.isAirBlock(var11) && var1.getBlockState(var11.down()).isFullyOpaque()) {
                  var1.setBlockState(var11, Blocks.TORCH.getDefaultState(), 2);
               }

               return true;
            }
         }

         return false;
      }
   }
}
