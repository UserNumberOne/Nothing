package net.minecraft.world.end;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.gen.feature.WorldGenSpikes;

public enum DragonSpawnManager {
   START {
      public void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5) {
         BlockPos var6 = new BlockPos(0, 128, 0);

         for(EntityEnderCrystal var8 : var3) {
            var8.setBeamTarget(var6);
         }

         var2.setRespawnState(PREPARING_TO_SUMMON_PILLARS);
      }
   },
   PREPARING_TO_SUMMON_PILLARS {
      public void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5) {
         if (var4 < 100) {
            if (var4 == 0 || var4 == 50 || var4 == 51 || var4 == 52 || var4 >= 95) {
               var1.playEvent(3001, new BlockPos(0, 128, 0), 0);
            }
         } else {
            var2.setRespawnState(SUMMONING_PILLARS);
         }

      }
   },
   SUMMONING_PILLARS {
      public void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5) {
         boolean var6 = true;
         boolean var7 = var4 % 40 == 0;
         boolean var8 = var4 % 40 == 39;
         if (var7 || var8) {
            WorldGenSpikes.EndSpike[] var9 = BiomeEndDecorator.getSpikesForWorld(var1);
            int var10 = var4 / 40;
            if (var10 < var9.length) {
               WorldGenSpikes.EndSpike var11 = var9[var10];
               if (var7) {
                  for(EntityEnderCrystal var13 : var3) {
                     var13.setBeamTarget(new BlockPos(var11.getCenterX(), var11.getHeight() + 1, var11.getCenterZ()));
                  }
               } else {
                  boolean var15 = true;

                  for(BlockPos.MutableBlockPos var14 : BlockPos.getAllInBoxMutable(new BlockPos(var11.getCenterX() - 10, var11.getHeight() - 10, var11.getCenterZ() - 10), new BlockPos(var11.getCenterX() + 10, var11.getHeight() + 10, var11.getCenterZ() + 10))) {
                     var1.setBlockToAir(var14);
                  }

                  var1.createExplosion((Entity)null, (double)((float)var11.getCenterX() + 0.5F), (double)var11.getHeight(), (double)((float)var11.getCenterZ() + 0.5F), 5.0F, true);
                  WorldGenSpikes var17 = new WorldGenSpikes();
                  var17.setSpike(var11);
                  var17.setCrystalInvulnerable(true);
                  var17.setBeamTarget(new BlockPos(0, 128, 0));
                  var17.generate(var1, new Random(), new BlockPos(var11.getCenterX(), 45, var11.getCenterZ()));
               }
            } else if (var7) {
               var2.setRespawnState(SUMMONING_DRAGON);
            }
         }

      }
   },
   SUMMONING_DRAGON {
      public void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5) {
         if (var4 >= 100) {
            var2.setRespawnState(END);
            var2.resetSpikeCrystals();

            for(EntityEnderCrystal var7 : var3) {
               var7.setBeamTarget((BlockPos)null);
               var1.createExplosion(var7, var7.posX, var7.posY, var7.posZ, 6.0F, false);
               var7.setDead();
            }
         } else if (var4 >= 80) {
            var1.playEvent(3001, new BlockPos(0, 128, 0), 0);
         } else if (var4 == 0) {
            for(EntityEnderCrystal var9 : var3) {
               var9.setBeamTarget(new BlockPos(0, 128, 0));
            }
         } else if (var4 < 5) {
            var1.playEvent(3001, new BlockPos(0, 128, 0), 0);
         }

      }
   },
   END {
      public void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5) {
      }
   };

   private DragonSpawnManager() {
   }

   public abstract void process(WorldServer var1, DragonFightManager var2, List var3, int var4, BlockPos var5);
}
