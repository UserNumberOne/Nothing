package net.minecraft.world;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.craftbukkit.v1_10_R1.util.LongHash;
import org.bukkit.craftbukkit.v1_10_R1.util.LongHashSet;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public final class WorldEntitySpawner {
   private static final int MOB_COUNT_DIV = (int)Math.pow(17.0D, 2.0D);
   private final LongHashSet eligibleChunksForSpawning = new LongHashSet();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$server$EnumCreatureType;

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

               for(int var10 = -8; var10 <= 8; ++var10) {
                  for(int var11 = -8; var11 <= 8; ++var11) {
                     boolean var12 = var10 == -8 || var10 == 8 || var11 == -8 || var11 == 8;
                     long var13 = LongHash.toLong(var10 + var8, var11 + var9);
                     if (!this.eligibleChunksForSpawning.contains(var13)) {
                        ++var5;
                        if (!var12 && var1.getWorldBorder().isInBounds(var10 + var8, var11 + var9)) {
                           PlayerChunkMapEntry var15 = var1.getPlayerChunkMap().getEntry(var10 + var8, var11 + var9);
                           if (var15 != null && var15.isSentToPlayers()) {
                              this.eligibleChunksForSpawning.add(var13);
                           }
                        }
                     }
                  }
               }
            }
         }

         int var41 = 0;
         BlockPos var42 = var1.getSpawnPoint();

         for(EnumCreatureType var16 : EnumCreatureType.values()) {
            int var17 = var16.getMaxNumberOfCreature();
            switch($SWITCH_TABLE$net$minecraft$server$EnumCreatureType()[var16.ordinal()]) {
            case 1:
               var17 = var1.getWorld().getMonsterSpawnLimit();
               break;
            case 2:
               var17 = var1.getWorld().getAnimalSpawnLimit();
               break;
            case 3:
               var17 = var1.getWorld().getAmbientSpawnLimit();
               break;
            case 4:
               var17 = var1.getWorld().getWaterAnimalSpawnLimit();
            }

            if (var17 != 0 && (!var16.getPeacefulCreature() || var3) && (var16.getPeacefulCreature() || var2) && (!var16.getAnimal() || var4)) {
               int var45 = var1.countEntities(var16.getCreatureClass());
               int var47 = var17 * var5 / MOB_COUNT_DIV;
               if (var45 <= var47) {
                  BlockPos.MutableBlockPos var18 = new BlockPos.MutableBlockPos();
                  Iterator var19 = this.eligibleChunksForSpawning.iterator();

                  label137:
                  while(var19.hasNext()) {
                     long var20 = ((Long)var19.next()).longValue();
                     BlockPos var22 = getRandomChunkPosition(var1, LongHash.msw(var20), LongHash.lsw(var20));
                     int var23 = var22.getX();
                     int var24 = var22.getY();
                     int var25 = var22.getZ();
                     IBlockState var26 = var1.getBlockState(var22);
                     if (!var26.isNormalCube()) {
                        int var27 = 0;

                        for(int var28 = 0; var28 < 3; ++var28) {
                           int var29 = var23;
                           int var30 = var24;
                           int var31 = var25;
                           Biome.SpawnListEntry var32 = null;
                           IEntityLivingData var33 = null;
                           int var34 = MathHelper.ceil(Math.random() * 4.0D);

                           for(int var35 = 0; var35 < var34; ++var35) {
                              var29 += var1.rand.nextInt(6) - var1.rand.nextInt(6);
                              var30 += var1.rand.nextInt(1) - var1.rand.nextInt(1);
                              var31 += var1.rand.nextInt(6) - var1.rand.nextInt(6);
                              var18.setPos(var29, var30, var31);
                              float var36 = (float)var29 + 0.5F;
                              float var37 = (float)var31 + 0.5F;
                              if (!var1.isAnyPlayerWithinRangeAt((double)var36, (double)var30, (double)var37, 24.0D) && var42.distanceSq((double)var36, (double)var30, (double)var37) >= 576.0D) {
                                 if (var32 == null) {
                                    var32 = var1.getSpawnListEntryForTypeAt(var16, var18);
                                    if (var32 == null) {
                                       break;
                                    }
                                 }

                                 if (var1.canCreatureTypeSpawnHere(var16, var32, var18) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(var32.entityClass), var1, var18)) {
                                    EntityLiving var38;
                                    try {
                                       var38 = (EntityLiving)var32.entityClass.getConstructor(World.class).newInstance(var1);
                                    } catch (Exception var40) {
                                       var40.printStackTrace();
                                       return var41;
                                    }

                                    var38.setLocationAndAngles((double)var36, (double)var30, (double)var37, var1.rand.nextFloat() * 360.0F, 0.0F);
                                    if (var38.getCanSpawnHere() && var38.isNotColliding()) {
                                       var33 = var38.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var38)), var33);
                                       if (var38.isNotColliding()) {
                                          ++var27;
                                          var1.addEntity(var38, SpawnReason.NATURAL);
                                       } else {
                                          var38.setDead();
                                       }

                                       if (var27 >= var38.getMaxSpawnedInChunk()) {
                                          continue label137;
                                       }
                                    }

                                    var41 += var27;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return var41;
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
            if (!var1.getBlockState(var4).isFullyOpaque()) {
               return false;
            } else {
               Block var5 = var1.getBlockState(var4).getBlock();
               boolean var6 = var5 != Blocks.BEDROCK && var5 != Blocks.BARRIER;
               return var6 && isValidEmptySpawnBlock(var3) && isValidEmptySpawnBlock(var1.getBlockState(var2.up()));
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
                     var10 = var19.onInitialSpawn(var0.getDifficultyForLocation(new BlockPos(var19)), var10);
                     var0.addEntity(var19, SpawnReason.CHUNK_GEN);
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

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$server$EnumCreatureType() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$server$EnumCreatureType;
      if ($SWITCH_TABLE$net$minecraft$server$EnumCreatureType != null) {
         return var10000;
      } else {
         int[] var0 = new int[EnumCreatureType.values().length];

         try {
            var0[EnumCreatureType.AMBIENT.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            var0[EnumCreatureType.CREATURE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[EnumCreatureType.MONSTER.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[EnumCreatureType.WATER_CREATURE.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$net$minecraft$server$EnumCreatureType = var0;
         return var0;
      }
   }
}
