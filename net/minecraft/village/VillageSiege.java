package net.minecraft.village;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.ZombieType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class VillageSiege {
   private final World world;
   private boolean hasSetupSiege;
   private int siegeState = -1;
   private int siegeCount;
   private int nextSpawnTime;
   private Village theVillage;
   private int spawnX;
   private int spawnY;
   private int spawnZ;

   public VillageSiege(World var1) {
      this.world = var1;
   }

   public void tick() {
      if (this.world.isDaytime()) {
         this.siegeState = 0;
      } else if (this.siegeState != 2) {
         if (this.siegeState == 0) {
            float var1 = this.world.getCelestialAngle(0.0F);
            if ((double)var1 < 0.5D || (double)var1 > 0.501D) {
               return;
            }

            this.siegeState = this.world.rand.nextInt(10) == 0 ? 1 : 2;
            this.hasSetupSiege = false;
            if (this.siegeState == 2) {
               return;
            }
         }

         if (this.siegeState != -1) {
            if (!this.hasSetupSiege) {
               if (!this.trySetupSiege()) {
                  return;
               }

               this.hasSetupSiege = true;
            }

            if (this.nextSpawnTime > 0) {
               --this.nextSpawnTime;
            } else {
               this.nextSpawnTime = 2;
               if (this.siegeCount > 0) {
                  this.spawnZombie();
                  --this.siegeCount;
               } else {
                  this.siegeState = 2;
               }
            }
         }
      }

   }

   private boolean trySetupSiege() {
      List var1 = this.world.playerEntities;
      Iterator var2 = var1.iterator();

      while(true) {
         if (!var2.hasNext()) {
            return false;
         }

         EntityPlayer var3 = (EntityPlayer)var2.next();
         if (!var3.isSpectator()) {
            this.theVillage = this.world.getVillageCollection().getNearestVillage(new BlockPos(var3), 1);
            if (this.theVillage != null && this.theVillage.getNumVillageDoors() >= 10 && this.theVillage.getTicksSinceLastDoorAdding() >= 20 && this.theVillage.getNumVillagers() >= 20) {
               BlockPos var4 = this.theVillage.getCenter();
               float var5 = (float)this.theVillage.getVillageRadius();
               boolean var6 = false;

               for(int var7 = 0; var7 < 10; ++var7) {
                  float var8 = this.world.rand.nextFloat() * 6.2831855F;
                  this.spawnX = var4.getX() + (int)((double)(MathHelper.cos(var8) * var5) * 0.9D);
                  this.spawnY = var4.getY();
                  this.spawnZ = var4.getZ() + (int)((double)(MathHelper.sin(var8) * var5) * 0.9D);
                  var6 = false;

                  for(Village var10 : this.world.getVillageCollection().getVillageList()) {
                     if (var10 != this.theVillage && var10.isBlockPosWithinSqVillageRadius(new BlockPos(this.spawnX, this.spawnY, this.spawnZ))) {
                        var6 = true;
                        break;
                     }
                  }

                  if (!var6) {
                     break;
                  }
               }

               if (var6) {
                  return false;
               }

               Vec3d var11 = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
               if (var11 != null) {
                  break;
               }
            }
         }
      }

      this.nextSpawnTime = 0;
      this.siegeCount = 20;
      return true;
   }

   private boolean spawnZombie() {
      Vec3d var1 = this.findRandomSpawnPos(new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
      if (var1 == null) {
         return false;
      } else {
         EntityZombie var2;
         try {
            var2 = new EntityZombie(this.world);
            var2.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var2)), (IEntityLivingData)null);
            var2.setZombieType(ZombieType.NORMAL);
         } catch (Exception var4) {
            var4.printStackTrace();
            return false;
         }

         var2.setLocationAndAngles(var1.xCoord, var1.yCoord, var1.zCoord, this.world.rand.nextFloat() * 360.0F, 0.0F);
         this.world.addEntity(var2, SpawnReason.VILLAGE_INVASION);
         BlockPos var3 = this.theVillage.getCenter();
         var2.setHomePosAndDistance(var3, this.theVillage.getVillageRadius());
         return true;
      }
   }

   @Nullable
   private Vec3d findRandomSpawnPos(BlockPos var1) {
      for(int var2 = 0; var2 < 10; ++var2) {
         BlockPos var3 = var1.add(this.world.rand.nextInt(16) - 8, this.world.rand.nextInt(6) - 3, this.world.rand.nextInt(16) - 8);
         if (this.theVillage.isBlockPosWithinSqVillageRadius(var3) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, this.world, var3)) {
            return new Vec3d((double)var3.getX(), (double)var3.getY(), (double)var3.getZ());
         }
      }

      return null;
   }
}
