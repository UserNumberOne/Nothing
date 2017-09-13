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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class MobSpawnerBaseLogic {
   private int spawnDelay = 20;
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

   private String getEntityNameToSpawn() {
      return this.randomEntity.getNbt().getString("id");
   }

   public void setEntityName(String var1) {
      this.randomEntity.getNbt().setString("id", var1);
   }

   private boolean isActivated() {
      BlockPos var1 = this.getSpawnerPosition();
      return this.getSpawnerWorld().isAnyPlayerWithinRangeAt((double)var1.getX() + 0.5D, (double)var1.getY() + 0.5D, (double)var1.getZ() + 0.5D, (double)this.activatingRangeFromPlayer);
   }

   public void updateSpawner() {
      if (!this.isActivated()) {
         this.prevMobRotation = this.mobRotation;
      } else {
         BlockPos var1 = this.getSpawnerPosition();
         if (this.getSpawnerWorld().isRemote) {
            double var17 = (double)((float)var1.getX() + this.getSpawnerWorld().rand.nextFloat());
            double var18 = (double)((float)var1.getY() + this.getSpawnerWorld().rand.nextFloat());
            double var19 = (double)((float)var1.getZ() + this.getSpawnerWorld().rand.nextFloat());
            this.getSpawnerWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var17, var18, var19, 0.0D, 0.0D, 0.0D);
            this.getSpawnerWorld().spawnParticle(EnumParticleTypes.FLAME, var17, var18, var19, 0.0D, 0.0D, 0.0D);
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

            boolean var2 = false;

            for(int var3 = 0; var3 < this.spawnCount; ++var3) {
               NBTTagCompound var4 = this.randomEntity.getNbt();
               NBTTagList var5 = var4.getTagList("Pos", 6);
               World var6 = this.getSpawnerWorld();
               int var7 = var5.tagCount();
               double var8 = var7 >= 1 ? var5.getDoubleAt(0) : (double)var1.getX() + (var6.rand.nextDouble() - var6.rand.nextDouble()) * (double)this.spawnRange + 0.5D;
               double var10 = var7 >= 2 ? var5.getDoubleAt(1) : (double)(var1.getY() + var6.rand.nextInt(3) - 1);
               double var12 = var7 >= 3 ? var5.getDoubleAt(2) : (double)var1.getZ() + (var6.rand.nextDouble() - var6.rand.nextDouble()) * (double)this.spawnRange + 0.5D;
               Entity var14 = AnvilChunkLoader.readWorldEntityPos(var4, var6, var8, var10, var12, false);
               if (var14 == null) {
                  return;
               }

               int var15 = var6.getEntitiesWithinAABB(var14.getClass(), (new AxisAlignedBB((double)var1.getX(), (double)var1.getY(), (double)var1.getZ(), (double)(var1.getX() + 1), (double)(var1.getY() + 1), (double)(var1.getZ() + 1))).expandXyz((double)this.spawnRange)).size();
               if (var15 >= this.maxNearbyEntities) {
                  this.resetTimer();
                  return;
               }

               EntityLiving var16 = var14 instanceof EntityLiving ? (EntityLiving)var14 : null;
               var14.setLocationAndAngles(var14.posX, var14.posY, var14.posZ, var6.rand.nextFloat() * 360.0F, 0.0F);
               if (var16 == null || ForgeEventFactory.canEntitySpawnSpawner(var16, this.getSpawnerWorld(), (float)var14.posX, (float)var14.posY, (float)var14.posZ)) {
                  if (this.randomEntity.getNbt().getSize() == 1 && this.randomEntity.getNbt().hasKey("id", 8) && var14 instanceof EntityLiving && !ForgeEventFactory.doSpecialSpawn(var16, this.getSpawnerWorld(), (float)var14.posX, (float)var14.posY, (float)var14.posZ)) {
                     ((EntityLiving)var14).onInitialSpawn(var6.getDifficultyForLocation(new BlockPos(var14)), (IEntityLivingData)null);
                  }

                  AnvilChunkLoader.spawnEntity(var14, var6);
                  var6.playEvent(2004, var1, 0);
                  if (var16 != null) {
                     var16.spawnExplosionParticle();
                  }

                  var2 = true;
               }
            }

            if (var2) {
               this.resetTimer();
            }
         }
      }

   }

   private void resetTimer() {
      if (this.maxSpawnDelay <= this.minSpawnDelay) {
         this.spawnDelay = this.minSpawnDelay;
      } else {
         int var1 = this.maxSpawnDelay - this.minSpawnDelay;
         this.spawnDelay = this.minSpawnDelay + this.getSpawnerWorld().rand.nextInt(var1);
      }

      if (!this.potentialSpawns.isEmpty()) {
         this.setNextSpawnData((WeightedSpawnerEntity)WeightedRandom.getRandomItem(this.getSpawnerWorld().rand, this.potentialSpawns));
      }

      this.broadcastEvent(1);
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.spawnDelay = var1.getShort("Delay");
      this.potentialSpawns.clear();
      if (var1.hasKey("SpawnPotentials", 9)) {
         NBTTagList var2 = var1.getTagList("SpawnPotentials", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            this.potentialSpawns.add(new WeightedSpawnerEntity(var2.getCompoundTagAt(var3)));
         }
      }

      NBTTagCompound var4 = var1.getCompoundTag("SpawnData");
      if (!var4.hasKey("id", 8)) {
         var4.setString("id", "Pig");
      }

      this.setNextSpawnData(new WeightedSpawnerEntity(1, var4));
      if (var1.hasKey("MinSpawnDelay", 99)) {
         this.minSpawnDelay = var1.getShort("MinSpawnDelay");
         this.maxSpawnDelay = var1.getShort("MaxSpawnDelay");
         this.spawnCount = var1.getShort("SpawnCount");
      }

      if (var1.hasKey("MaxNearbyEntities", 99)) {
         this.maxNearbyEntities = var1.getShort("MaxNearbyEntities");
         this.activatingRangeFromPlayer = var1.getShort("RequiredPlayerRange");
      }

      if (var1.hasKey("SpawnRange", 99)) {
         this.spawnRange = var1.getShort("SpawnRange");
      }

      if (this.getSpawnerWorld() != null) {
         this.cachedEntity = null;
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      String var2 = this.getEntityNameToSpawn();
      if (StringUtils.isNullOrEmpty(var2)) {
         return var1;
      } else {
         var1.setShort("Delay", (short)this.spawnDelay);
         var1.setShort("MinSpawnDelay", (short)this.minSpawnDelay);
         var1.setShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
         var1.setShort("SpawnCount", (short)this.spawnCount);
         var1.setShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
         var1.setShort("RequiredPlayerRange", (short)this.activatingRangeFromPlayer);
         var1.setShort("SpawnRange", (short)this.spawnRange);
         var1.setTag("SpawnData", this.randomEntity.getNbt().copy());
         NBTTagList var3 = new NBTTagList();
         if (this.potentialSpawns.isEmpty()) {
            var3.appendTag(this.randomEntity.toCompoundTag());
         } else {
            for(WeightedSpawnerEntity var5 : this.potentialSpawns) {
               var3.appendTag(var5.toCompoundTag());
            }
         }

         var1.setTag("SpawnPotentials", var3);
         return var1;
      }
   }

   public boolean setDelayToMin(int var1) {
      if (var1 == 1 && this.getSpawnerWorld().isRemote) {
         this.spawnDelay = this.minSpawnDelay;
         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public Entity getCachedEntity() {
      if (this.cachedEntity == null) {
         this.cachedEntity = AnvilChunkLoader.readWorldEntity(this.randomEntity.getNbt(), this.getSpawnerWorld(), false);
         if (this.randomEntity.getNbt().getSize() == 1 && this.randomEntity.getNbt().hasKey("id", 8) && this.cachedEntity instanceof EntityLiving) {
            ((EntityLiving)this.cachedEntity).onInitialSpawn(this.getSpawnerWorld().getDifficultyForLocation(new BlockPos(this.cachedEntity)), (IEntityLivingData)null);
         }
      }

      return this.cachedEntity;
   }

   public void setNextSpawnData(WeightedSpawnerEntity var1) {
      this.randomEntity = var1;
   }

   public abstract void broadcastEvent(int var1);

   public abstract World getSpawnerWorld();

   public abstract BlockPos getSpawnerPosition();

   @SideOnly(Side.CLIENT)
   public double getMobRotation() {
      return this.mobRotation;
   }

   @SideOnly(Side.CLIENT)
   public double getPrevMobRotation() {
      return this.prevMobRotation;
   }
}
