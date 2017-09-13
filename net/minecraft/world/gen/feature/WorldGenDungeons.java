package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.DungeonHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenDungeons extends WorldGenerator {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String[] SPAWNERTYPES = new String[]{"Skeleton", "Zombie", "Zombie", "Spider"};

   public boolean generate(World var1, Random var2, BlockPos var3) {
      boolean var4 = true;
      int var5 = var2.nextInt(2) + 2;
      int var6 = -var5 - 1;
      int var7 = var5 + 1;
      boolean var8 = true;
      boolean var9 = true;
      int var10 = var2.nextInt(2) + 2;
      int var11 = -var10 - 1;
      int var12 = var10 + 1;
      int var13 = 0;

      for(int var14 = var6; var14 <= var7; ++var14) {
         for(int var15 = -1; var15 <= 4; ++var15) {
            for(int var16 = var11; var16 <= var12; ++var16) {
               BlockPos var17 = var3.add(var14, var15, var16);
               Material var18 = var1.getBlockState(var17).getMaterial();
               boolean var19 = var18.isSolid();
               if (var15 == -1 && !var19) {
                  return false;
               }

               if (var15 == 4 && !var19) {
                  return false;
               }

               if ((var14 == var6 || var14 == var7 || var16 == var11 || var16 == var12) && var15 == 0 && var1.isAirBlock(var17) && var1.isAirBlock(var17.up())) {
                  ++var13;
               }
            }
         }
      }

      if (var13 >= 1 && var13 <= 5) {
         for(int var23 = var6; var23 <= var7; ++var23) {
            for(int var26 = 3; var26 >= -1; --var26) {
               for(int var28 = var11; var28 <= var12; ++var28) {
                  BlockPos var30 = var3.add(var23, var26, var28);
                  if (var23 != var6 && var26 != -1 && var28 != var11 && var23 != var7 && var26 != 4 && var28 != var12) {
                     if (var1.getBlockState(var30).getBlock() != Blocks.CHEST) {
                        var1.setBlockToAir(var30);
                     }
                  } else if (var30.getY() >= 0 && !var1.getBlockState(var30.down()).getMaterial().isSolid()) {
                     var1.setBlockToAir(var30);
                  } else if (var1.getBlockState(var30).getMaterial().isSolid() && var1.getBlockState(var30).getBlock() != Blocks.CHEST) {
                     if (var26 == -1 && var2.nextInt(4) != 0) {
                        var1.setBlockState(var30, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 2);
                     } else {
                        var1.setBlockState(var30, Blocks.COBBLESTONE.getDefaultState(), 2);
                     }
                  }
               }
            }
         }

         for(int var24 = 0; var24 < 2; ++var24) {
            for(int var27 = 0; var27 < 3; ++var27) {
               int var29 = var3.getX() + var2.nextInt(var5 * 2 + 1) - var5;
               int var31 = var3.getY();
               int var32 = var3.getZ() + var2.nextInt(var10 * 2 + 1) - var10;
               BlockPos var33 = new BlockPos(var29, var31, var32);
               if (var1.isAirBlock(var33)) {
                  int var20 = 0;

                  for(EnumFacing var22 : EnumFacing.Plane.HORIZONTAL) {
                     if (var1.getBlockState(var33.offset(var22)).getMaterial().isSolid()) {
                        ++var20;
                     }
                  }

                  if (var20 == 1) {
                     var1.setBlockState(var33, Blocks.CHEST.correctFacing(var1, var33, Blocks.CHEST.getDefaultState()), 2);
                     TileEntity var34 = var1.getTileEntity(var33);
                     if (var34 instanceof TileEntityChest) {
                        ((TileEntityChest)var34).setLootTable(LootTableList.CHESTS_SIMPLE_DUNGEON, var2.nextLong());
                     }
                     break;
                  }
               }
            }
         }

         var1.setBlockState(var3, Blocks.MOB_SPAWNER.getDefaultState(), 2);
         TileEntity var25 = var1.getTileEntity(var3);
         if (var25 instanceof TileEntityMobSpawner) {
            ((TileEntityMobSpawner)var25).getSpawnerBaseLogic().setEntityName(this.pickMobSpawner(var2));
         } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", new Object[]{var3.getX(), var3.getY(), var3.getZ()});
         }

         return true;
      } else {
         return false;
      }
   }

   private String pickMobSpawner(Random var1) {
      return DungeonHooks.getRandomDungeonMob(var1);
   }
}
