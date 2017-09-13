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

   public int findChunksForSpawning(WorldServer worldserver, boolean flag, boolean flag1, boolean flag2) {
      if (!flag && !flag1) {
         return 0;
      } else {
         this.eligibleChunksForSpawning.clear();
         int i = 0;

         for(EntityPlayer entityhuman : worldserver.playerEntities) {
            if (!entityhuman.isSpectator()) {
               int l = MathHelper.floor(entityhuman.posX / 16.0D);
               int j = MathHelper.floor(entityhuman.posZ / 16.0D);

               for(int i1 = -8; i1 <= 8; ++i1) {
                  for(int k = -8; k <= 8; ++k) {
                     boolean flag4 = i1 == -8 || i1 == 8 || k == -8 || k == 8;
                     long chunkCoords = LongHash.toLong(i1 + l, k + j);
                     if (!this.eligibleChunksForSpawning.contains(chunkCoords)) {
                        ++i;
                        if (!flag4 && worldserver.getWorldBorder().isInBounds(i1 + l, k + j)) {
                           PlayerChunkMapEntry playerchunk = worldserver.getPlayerChunkMap().getEntry(i1 + l, k + j);
                           if (playerchunk != null && playerchunk.isSentToPlayers()) {
                              this.eligibleChunksForSpawning.add(chunkCoords);
                           }
                        }
                     }
                  }
               }
            }
         }

         int j1 = 0;
         BlockPos blockposition = worldserver.getSpawnPoint();

         for(EnumCreatureType enumcreaturetype : EnumCreatureType.values()) {
            int limit = enumcreaturetype.getMaxNumberOfCreature();
            switch($SWITCH_TABLE$net$minecraft$server$EnumCreatureType()[enumcreaturetype.ordinal()]) {
            case 1:
               limit = worldserver.getWorld().getMonsterSpawnLimit();
               break;
            case 2:
               limit = worldserver.getWorld().getAnimalSpawnLimit();
               break;
            case 3:
               limit = worldserver.getWorld().getAmbientSpawnLimit();
               break;
            case 4:
               limit = worldserver.getWorld().getWaterAnimalSpawnLimit();
            }

            if (limit != 0 && (!enumcreaturetype.getPeacefulCreature() || flag1) && (enumcreaturetype.getPeacefulCreature() || flag) && (!enumcreaturetype.getAnimal() || flag2)) {
               int k = worldserver.countEntities(enumcreaturetype.getCreatureClass());
               int l1 = limit * i / MOB_COUNT_DIV;
               if (k <= l1) {
                  BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
                  Iterator iterator1 = this.eligibleChunksForSpawning.iterator();

                  label137:
                  while(iterator1.hasNext()) {
                     long key = ((Long)iterator1.next()).longValue();
                     BlockPos blockposition1 = getRandomChunkPosition(worldserver, LongHash.msw(key), LongHash.lsw(key));
                     int i2 = blockposition1.getX();
                     int j2 = blockposition1.getY();
                     int k2 = blockposition1.getZ();
                     IBlockState iblockdata = worldserver.getBlockState(blockposition1);
                     if (!iblockdata.isNormalCube()) {
                        int l2 = 0;

                        for(int i3 = 0; i3 < 3; ++i3) {
                           int j3 = i2;
                           int k3 = j2;
                           int l3 = k2;
                           Biome.SpawnListEntry biomebase_biomemeta = null;
                           IEntityLivingData groupdataentity = null;
                           int i4 = MathHelper.ceil(Math.random() * 4.0D);

                           for(int j4 = 0; j4 < i4; ++j4) {
                              j3 += worldserver.rand.nextInt(6) - worldserver.rand.nextInt(6);
                              k3 += worldserver.rand.nextInt(1) - worldserver.rand.nextInt(1);
                              l3 += worldserver.rand.nextInt(6) - worldserver.rand.nextInt(6);
                              blockposition_mutableblockposition.setPos(j3, k3, l3);
                              float f = (float)j3 + 0.5F;
                              float f1 = (float)l3 + 0.5F;
                              if (!worldserver.isAnyPlayerWithinRangeAt((double)f, (double)k3, (double)f1, 24.0D) && blockposition.distanceSq((double)f, (double)k3, (double)f1) >= 576.0D) {
                                 if (biomebase_biomemeta == null) {
                                    biomebase_biomemeta = worldserver.getSpawnListEntryForTypeAt(enumcreaturetype, blockposition_mutableblockposition);
                                    if (biomebase_biomemeta == null) {
                                       break;
                                    }
                                 }

                                 if (worldserver.canCreatureTypeSpawnHere(enumcreaturetype, biomebase_biomemeta, blockposition_mutableblockposition) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(biomebase_biomemeta.entityClass), worldserver, blockposition_mutableblockposition)) {
                                    EntityLiving entityinsentient;
                                    try {
                                       entityinsentient = (EntityLiving)biomebase_biomemeta.entityClass.getConstructor(World.class).newInstance(worldserver);
                                    } catch (Exception var40) {
                                       var40.printStackTrace();
                                       return j1;
                                    }

                                    entityinsentient.setLocationAndAngles((double)f, (double)k3, (double)f1, worldserver.rand.nextFloat() * 360.0F, 0.0F);
                                    if (entityinsentient.getCanSpawnHere() && entityinsentient.isNotColliding()) {
                                       groupdataentity = entityinsentient.onInitialSpawn(worldserver.getDifficultyForLocation(new BlockPos(entityinsentient)), groupdataentity);
                                       if (entityinsentient.isNotColliding()) {
                                          ++l2;
                                          worldserver.addEntity(entityinsentient, SpawnReason.NATURAL);
                                       } else {
                                          entityinsentient.setDead();
                                       }

                                       if (l2 >= entityinsentient.getMaxSpawnedInChunk()) {
                                          continue label137;
                                       }
                                    }

                                    j1 += l2;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return j1;
      }
   }

   private static BlockPos getRandomChunkPosition(World world, int i, int j) {
      Chunk chunk = world.getChunkFromChunkCoords(i, j);
      int k = i * 16 + world.rand.nextInt(16);
      int l = j * 16 + world.rand.nextInt(16);
      int i1 = MathHelper.roundUp(chunk.getHeight(new BlockPos(k, 0, l)) + 1, 16);
      int j1 = world.rand.nextInt(i1 > 0 ? i1 : chunk.getTopFilledSegment() + 16 - 1);
      return new BlockPos(k, j1, l);
   }

   public static boolean isValidEmptySpawnBlock(IBlockState iblockdata) {
      return iblockdata.isBlockNormalCube() ? false : (iblockdata.canProvidePower() ? false : (iblockdata.getMaterial().isLiquid() ? false : !BlockRailBase.isRailBlock(iblockdata)));
   }

   public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType entityinsentient_enumentitypositiontype, World world, BlockPos blockposition) {
      if (!world.getWorldBorder().contains(blockposition)) {
         return false;
      } else {
         IBlockState iblockdata = world.getBlockState(blockposition);
         if (entityinsentient_enumentitypositiontype == EntityLiving.SpawnPlacementType.IN_WATER) {
            return iblockdata.getMaterial().isLiquid() && world.getBlockState(blockposition.down()).getMaterial().isLiquid() && !world.getBlockState(blockposition.up()).isNormalCube();
         } else {
            BlockPos blockposition1 = blockposition.down();
            if (!world.getBlockState(blockposition1).isFullyOpaque()) {
               return false;
            } else {
               Block block = world.getBlockState(blockposition1).getBlock();
               boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
               return flag && isValidEmptySpawnBlock(iblockdata) && isValidEmptySpawnBlock(world.getBlockState(blockposition.up()));
            }
         }
      }
   }

   public static void performWorldGenSpawning(World world, Biome biomebase, int i, int j, int k, int l, Random random) {
      List list = biomebase.getSpawnableList(EnumCreatureType.CREATURE);
      if (!list.isEmpty()) {
         while(random.nextFloat() < biomebase.getSpawningChance()) {
            Biome.SpawnListEntry biomebase_biomemeta = (Biome.SpawnListEntry)WeightedRandom.getRandomItem(world.rand, list);
            int i1 = biomebase_biomemeta.minGroupCount + random.nextInt(1 + biomebase_biomemeta.maxGroupCount - biomebase_biomemeta.minGroupCount);
            IEntityLivingData groupdataentity = null;
            int j1 = i + random.nextInt(k);
            int k1 = j + random.nextInt(l);
            int l1 = j1;
            int i2 = k1;

            for(int j2 = 0; j2 < i1; ++j2) {
               boolean flag = false;

               for(int k2 = 0; !flag && k2 < 4; ++k2) {
                  BlockPos blockposition = world.getTopSolidOrLiquidBlock(new BlockPos(j1, 0, k1));
                  if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, blockposition)) {
                     EntityLiving entityinsentient;
                     try {
                        entityinsentient = (EntityLiving)biomebase_biomemeta.entityClass.getConstructor(World.class).newInstance(world);
                     } catch (Exception var21) {
                        var21.printStackTrace();
                        continue;
                     }

                     entityinsentient.setLocationAndAngles((double)((float)j1 + 0.5F), (double)blockposition.getY(), (double)((float)k1 + 0.5F), random.nextFloat() * 360.0F, 0.0F);
                     groupdataentity = entityinsentient.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityinsentient)), groupdataentity);
                     world.addEntity(entityinsentient, SpawnReason.CHUNK_GEN);
                     flag = true;
                  }

                  j1 += random.nextInt(5) - random.nextInt(5);

                  for(k1 += random.nextInt(5) - random.nextInt(5); j1 < i || j1 >= i + k || k1 < j || k1 >= j + k; k1 = i2 + random.nextInt(5) - random.nextInt(5)) {
                     j1 = l1 + random.nextInt(5) - random.nextInt(5);
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
