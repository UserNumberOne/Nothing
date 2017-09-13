package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public final class WorldEntitySpawner {
   private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
   private final Set eligibleChunksForSpawning = Sets.newHashSet();

   public int findChunksForSpawning(WorldServer var1, boolean var2, boolean var3, boolean var4) {
      if (!var2 && !var3) {
         return 0;
      } else {
         this.eligibleChunksForSpawning.clear();
         int var5 = 0;

         for(EntityPlayer var7 : var1.playerEntities) {
            if (!var7.isSpectator()) {
               int var8 = MathHelper.floor(var7.posX / 16.0D);
               int var9 = MathHelper.floor(var7.posZ / 16.0D);
               boolean var10 = true;

               for(int var11 = -8; var11 <= 8; ++var11) {
                  for(int var12 = -8; var12 <= 8; ++var12) {
                     boolean var13 = var11 == -8 || var11 == 8 || var12 == -8 || var12 == 8;
                     ChunkPos var14 = new ChunkPos(var11 + var8, var12 + var9);
                     if (!this.eligibleChunksForSpawning.contains(var14)) {
                        ++var5;
                        if (!var13 && var1.getWorldBorder().contains(var14)) {
                           PlayerChunkMapEntry var15 = var1.getPlayerChunkMap().getEntry(var14.chunkXPos, var14.chunkZPos);
                           if (var15 != null && var15.isSentToPlayers()) {
                              this.eligibleChunksForSpawning.add(var14);
                           }
                        }
                     }
                  }
               }
            }
         }

         int var38 = 0;
         BlockPos var39 = var1.getSpawnPoint();

         for(EnumCreatureType var43 : EnumCreatureType.values()) {
            if ((!var43.getPeacefulCreature() || var3) && (var43.getPeacefulCreature() || var2) && (!var43.getAnimal() || var4)) {
               int var44 = var1.countEntities(var43, true);
               int var45 = var43.getMaxNumberOfCreature() * var5 / MOB_COUNT_DIV;
               if (var44 <= var45) {
                  ArrayList var46 = Lists.newArrayList(this.eligibleChunksForSpawning);
                  Collections.shuffle(var46);
                  BlockPos.MutableBlockPos var47 = new BlockPos.MutableBlockPos();

                  label143:
                  for(ChunkPos var17 : var46) {
                     BlockPos var18 = getRandomChunkPosition(var1, var17.chunkXPos, var17.chunkZPos);
                     int var19 = var18.getX();
                     int var20 = var18.getY();
                     int var21 = var18.getZ();
                     IBlockState var22 = var1.getBlockState(var18);
                     if (!var22.isNormalCube()) {
                        int var23 = 0;

                        for(int var24 = 0; var24 < 3; ++var24) {
                           int var25 = var19;
                           int var26 = var20;
                           int var27 = var21;
                           boolean var28 = true;
                           Biome.SpawnListEntry var29 = null;
                           IEntityLivingData var30 = null;
                           int var31 = MathHelper.ceil(Math.random() * 4.0D);

                           for(int var32 = 0; var32 < var31; ++var32) {
                              var25 += var1.rand.nextInt(6) - var1.rand.nextInt(6);
                              var26 += var1.rand.nextInt(1) - var1.rand.nextInt(1);
                              var27 += var1.rand.nextInt(6) - var1.rand.nextInt(6);
                              var47.setPos(var25, var26, var27);
                              float var33 = (float)var25 + 0.5F;
                              float var34 = (float)var27 + 0.5F;
                              if (!var1.isAnyPlayerWithinRangeAt((double)var33, (double)var26, (double)var34, 24.0D) && var39.distanceSq((double)var33, (double)var26, (double)var34) >= 576.0D) {
                                 if (var29 == null) {
                                    var29 = var1.getSpawnListEntryForTypeAt(var43, var47);
                                    if (var29 == null) {
                                       break;
                                    }
                                 }

                                 if (var1.canCreatureTypeSpawnHere(var43, var29, var47) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(var29.entityClass), var1, var47)) {
                                    EntityLiving var35;
                                    try {
                                       var35 = (EntityLiving)var29.entityClass.getConstructor(World.class).newInstance(var1);
                                    } catch (Exception var37) {
                                       var37.printStackTrace();
                                       return var38;
                                    }

                                    var35.setLocationAndAngles((double)var33, (double)var26, (double)var34, var1.rand.nextFloat() * 360.0F, 0.0F);
                                    Result var36 = ForgeEventFactory.canEntitySpawn(var35, var1, var33, (float)var26, var34);
                                    if (var36 == Result.ALLOW || var36 == Result.DEFAULT && var35.getCanSpawnHere() && var35.isNotColliding()) {
                                       if (!ForgeEventFactory.doSpecialSpawn(var35, var1, var33, (float)var26, var34)) {
                                          var30 = var35.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var35)), var30);
                                       }

                                       if (var35.isNotColliding()) {
                                          ++var23;
                                          var1.spawnEntity(var35);
                                       } else {
                                          var35.setDead();
                                       }

                                       if (var23 >= ForgeEventFactory.getMaxSpawnPackSize(var35)) {
                                          continue label143;
                                       }
                                    }

                                    var38 += var23;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return var38;
      }
   }

   private static BlockPos getRandomChunkPosition(World var0, int var1, int var2) {
      Chunk var3 = var0.getChunkFromChunkCoords(var1, var2);
      int var4 = var1 * 16 + var0.rand.nextInt(16);
      int var5 = var2 * 16 + var0.rand.nextInt(16);
      int var6 = MathHelper.roundUp(var3.getHeight(new BlockPos(var4, 0, var5)) + 1, 16);
      int var7 = var0.rand.nextInt(var6 > 0 ? var6 : var3.getTopFilledSegment() + 16 - 1);
      return new BlockPos(var4, var7, var5);
   }

   public static boolean isValidEmptySpawnBlock(IBlockState var0) {
      return var0.isBlockNormalCube() ? false : (var0.canProvidePower() ? false : (var0.getMaterial().isLiquid() ? false : !BlockRailBase.isRailBlock(var0)));
   }

   public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType var0, World var1, BlockPos var2) {
      if (!var1.getWorldBorder().contains(var2)) {
         return false;
      } else {
         IBlockState var3 = var1.getBlockState(var2);
         if (var0 == EntityLiving.SpawnPlacementType.IN_WATER) {
            return var3.getMaterial().isLiquid() && var1.getBlockState(var2.down()).getMaterial().isLiquid() && !var1.getBlockState(var2.up()).isNormalCube();
         } else {
            BlockPos var4 = var2.down();
            IBlockState var5 = var1.getBlockState(var4);
            if (!var5.getBlock().canCreatureSpawn(var5, var1, var4, var0)) {
               return false;
            } else {
               Block var6 = var1.getBlockState(var4).getBlock();
               boolean var7 = var6 != Blocks.BEDROCK && var6 != Blocks.BARRIER;
               return var7 && isValidEmptySpawnBlock(var3) && isValidEmptySpawnBlock(var1.getBlockState(var2.up()));
            }
         }
      }
   }

   public static void performWorldGenSpawning(World var0, Biome var1, int var2, int var3, int var4, int var5, Random var6) {
      List var7 = var1.getSpawnableList(EnumCreatureType.CREATURE);
      if (!var7.isEmpty()) {
         while(var6.nextFloat() < var1.getSpawningChance()) {
            Biome.SpawnListEntry var8 = (Biome.SpawnListEntry)WeightedRandom.getRandomItem(var0.rand, var7);
            int var9 = var8.minGroupCount + var6.nextInt(1 + var8.maxGroupCount - var8.minGroupCount);
            IEntityLivingData var10 = null;
            int var11 = var2 + var6.nextInt(var4);
            int var12 = var3 + var6.nextInt(var5);
            int var13 = var11;
            int var14 = var12;

            for(int var15 = 0; var15 < var9; ++var15) {
               boolean var16 = false;

               for(int var17 = 0; !var16 && var17 < 4; ++var17) {
                  BlockPos var18 = var0.getTopSolidOrLiquidBlock(new BlockPos(var11, 0, var12));
                  if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, var0, var18)) {
                     EntityLiving var19;
                     try {
                        var19 = (EntityLiving)var8.entityClass.getConstructor(World.class).newInstance(var0);
                     } catch (Exception var21) {
                        var21.printStackTrace();
                        continue;
                     }

                     var19.setLocationAndAngles((double)((float)var11 + 0.5F), (double)var18.getY(), (double)((float)var12 + 0.5F), var6.nextFloat() * 360.0F, 0.0F);
                     var0.spawnEntity(var19);
                     var10 = var19.onInitialSpawn(var0.getDifficultyForLocation(new BlockPos(var19)), var10);
                     var16 = true;
                  }

                  var11 += var6.nextInt(5) - var6.nextInt(5);

                  for(var12 += var6.nextInt(5) - var6.nextInt(5); var11 < var2 || var11 >= var2 + var4 || var12 < var3 || var12 >= var3 + var4; var12 = var14 + var6.nextInt(5) - var6.nextInt(5)) {
                     var11 = var13 + var6.nextInt(5) - var6.nextInt(5);
                  }
               }
            }
         }
      }

   }
}
