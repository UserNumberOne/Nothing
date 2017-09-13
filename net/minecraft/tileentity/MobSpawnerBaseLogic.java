package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public abstract class MobSpawnerBaseLogic {
   public int spawnDelay = 20;
   private final List potentialSpawns = Lists.newArrayList();
   private WeightedSpawnerEntity randomEntity = new WeightedSpawnerEntity();
   private double mobRotation;
   private double prevMobRotation;
   private int minSpawnDelay = 200;
   private int maxSpawnDelay = 800;
   private int spawnCount = 4;
   private Entity cachedEntity;
   private int maxNearbyEntities = 6;
   private int activatingRangeFromPlayer = 16;
   private int spawnRange = 4;

   public String getEntityNameToSpawn() {
      return this.randomEntity.getNbt().getString("id");
   }

   public void setEntityName(String s) {
      this.randomEntity.getNbt().setString("id", s);
   }

   private boolean isActivated() {
      BlockPos blockposition = this.getSpawnerPosition();
      return this.getSpawnerWorld().isAnyPlayerWithinRangeAt((double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.5D, (double)blockposition.getZ() + 0.5D, (double)this.activatingRangeFromPlayer);
   }

   public void updateSpawner() {
      if (!this.isActivated()) {
         this.prevMobRotation = this.mobRotation;
      } else {
         BlockPos blockposition = this.getSpawnerPosition();
         if (this.getSpawnerWorld().isRemote) {
            double d0 = (double)((float)blockposition.getX() + this.getSpawnerWorld().rand.nextFloat());
            double d1 = (double)((float)blockposition.getY() + this.getSpawnerWorld().rand.nextFloat());
            double d2 = (double)((float)blockposition.getZ() + this.getSpawnerWorld().rand.nextFloat());
            this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            this.getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            if (this.spawnDelay > 0) {
               --this.spawnDelay;
            }

            this.prevMobRotation = this.mobRotation;
            this.mobRotation = (this.mobRotation + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0D;
         } else {
            if (this.spawnDelay == -1) {
               this.resetTimer();
            }

            if (this.spawnDelay > 0) {
               --this.spawnDelay;
               return;
            }

            boolean flag = false;

            for(int i = 0; i < this.spawnCount; ++i) {
               NBTTagCompound nbttagcompound = this.randomEntity.getNbt();
               NBTTagList nbttaglist = nbttagcompound.getTagList("Pos", 6);
               World world = this.getSpawnerWorld();
               int j = nbttaglist.tagCount();
               double d3 = j >= 1 ? nbttaglist.getDoubleAt(0) : (double)blockposition.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)this.spawnRange + 0.5D;
               double d4 = j >= 2 ? nbttaglist.getDoubleAt(1) : (double)(blockposition.getY() + world.rand.nextInt(3) - 1);
               double d5 = j >= 3 ? nbttaglist.getDoubleAt(2) : (double)blockposition.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * (double)this.spawnRange + 0.5D;
               Entity entity = AnvilChunkLoader.readWorldEntityPos(nbttagcompound, world, d3, d4, d5, false);
               if (entity == null) {
                  return;
               }

               int k = world.getEntitiesWithinAABB(entity.getClass(), (new AxisAlignedBB((double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ(), (double)(blockposition.getX() + 1), (double)(blockposition.getY() + 1), (double)(blockposition.getZ() + 1))).expandXyz((double)this.spawnRange)).size();
               if (k >= this.maxNearbyEntities) {
                  this.resetTimer();
                  return;
               }

               EntityLiving entityinsentient = entity instanceof EntityLiving ? (EntityLiving)entity : null;
               entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, world.rand.nextFloat() * 360.0F, 0.0F);
               if (entityinsentient == null || entityinsentient.getCanSpawnHere() && entityinsentient.isNotColliding()) {
                  if (this.randomEntity.getNbt().getSize() == 1 && this.randomEntity.getNbt().hasKey("id", 8) && entity instanceof EntityLiving) {
                     ((EntityLiving)entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData)null);
                  }

                  AnvilChunkLoader.a(entity, world, SpawnReason.SPAWNER);
                  world.playEvent(2004, blockposition, 0);
                  if (entityinsentient != null) {
                     entityinsentient.spawnExplosionParticle();
                  }

                  flag = true;
               }
            }

            if (flag) {
               this.resetTimer();
            }
         }
      }

   }

   private void resetTimer() {
      if (this.maxSpawnDelay <= this.minSpawnDelay) {
         this.spawnDelay = this.minSpawnDelay;
      } else {
         int i = this.maxSpawnDelay - this.minSpawnDelay;
         this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(i);
      }

      if (!this.potentialSpawns.isEmpty()) {
         this.setNextSpawnData((WeightedSpawnerEntity)WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.potentialSpawns));
      }

      this.broadcastEvent(1);
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      this.spawnDelay = nbttagcompound.getShort("Delay");
      this.potentialSpawns.clear();
      if (nbttagcompound.hasKey("SpawnPotentials", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("SpawnPotentials", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            this.potentialSpawns.add(new WeightedSpawnerEntity(nbttaglist.getCompoundTagAt(i)));
         }
      }

      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("SpawnData");
      if (!nbttagcompound1.hasKey("id", 8)) {
         nbttagcompound1.setString("id", "Pig");
      }

      this.setNextSpawnData(new WeightedSpawnerEntity(1, nbttagcompound1));
      if (nbttagcompound.hasKey("MinSpawnDelay", 99)) {
         this.minSpawnDelay = nbttagcompound.getShort("MinSpawnDelay");
         this.maxSpawnDelay = nbttagcompound.getShort("MaxSpawnDelay");
         this.spawnCount = nbttagcompound.getShort("SpawnCount");
      }

      if (nbttagcompound.hasKey("MaxNearbyEntities", 99)) {
         this.maxNearbyEntities = nbttagcompound.getShort("MaxNearbyEntities");
         this.activatingRangeFromPlayer = nbttagcompound.getShort("RequiredPlayerRange");
      }

      if (nbttagcompound.hasKey("SpawnRange", 99)) {
         this.spawnRange = nbttagcompound.getShort("SpawnRange");
      }

      if (this.getSpawnerWorld() != null) {
         this.cachedEntity = null;
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      String s = this.getEntityNameToSpawn();
      if (StringUtils.isNullOrEmpty(s)) {
         return nbttagcompound;
      } else {
         nbttagcompound.setShort("Delay", (short)this.spawnDelay);
         nbttagcompound.setShort("MinSpawnDelay", (short)this.minSpawnDelay);
         nbttagcompound.setShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
         nbttagcompound.setShort("SpawnCount", (short)this.spawnCount);
         nbttagcompound.setShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
         nbttagcompound.setShort("RequiredPlayerRange", (short)this.activatingRangeFromPlayer);
         nbttagcompound.setShort("SpawnRange", (short)this.spawnRange);
         nbttagcompound.setTag("SpawnData", this.randomEntity.getNbt().copy());
         NBTTagList nbttaglist = new NBTTagList();
         if (this.potentialSpawns.isEmpty()) {
            nbttaglist.appendTag(this.randomEntity.toCompoundTag());
         } else {
            for(WeightedSpawnerEntity mobspawnerdata : this.potentialSpawns) {
               nbttaglist.appendTag(mobspawnerdata.toCompoundTag());
            }
         }

         nbttagcompound.setTag("SpawnPotentials", nbttaglist);
         return nbttagcompound;
      }
   }

   public boolean setDelayToMin(int i) {
      if (i == 1 && this.getSpawnerWorld().isRemote) {
         this.spawnDelay = this.minSpawnDelay;
         return true;
      } else {
         return false;
      }
   }

   public void setNextSpawnData(WeightedSpawnerEntity mobspawnerdata) {
      this.randomEntity = mobspawnerdata;
   }

   public abstract void broadcastEvent(int var1);

   public abstract World getSpawnerWorld();

   public abstract BlockPos getSpawnerPosition();
}
