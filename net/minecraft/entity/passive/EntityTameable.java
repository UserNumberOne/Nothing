package net.minecraft.entity.passive;

import com.google.common.base.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public abstract class EntityTameable extends EntityAnimal implements IEntityOwnable {
   protected static final DataParameter TAMED = EntityDataManager.createKey(EntityTameable.class, DataSerializers.BYTE);
   protected static final DataParameter OWNER_UNIQUE_ID = EntityDataManager.createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
   protected EntityAISit aiSit;

   public EntityTameable(World var1) {
      super(var1);
      this.setupTamedAI();
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(TAMED, Byte.valueOf((byte)0));
      this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      if (this.getOwnerId() == null) {
         var1.setString("OwnerUUID", "");
      } else {
         var1.setString("OwnerUUID", this.getOwnerId().toString());
      }

      var1.setBoolean("Sitting", this.isSitting());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      String var2;
      if (var1.hasKey("OwnerUUID", 8)) {
         var2 = var1.getString("OwnerUUID");
      } else {
         String var3 = var1.getString("Owner");
         var2 = PreYggdrasilConverter.a(this.h(), var3);
      }

      if (!var2.isEmpty()) {
         try {
            this.setOwnerId(UUID.fromString(var2));
            this.setTamed(true);
         } catch (Throwable var4) {
            this.setTamed(false);
         }
      }

      if (this.aiSit != null) {
         this.aiSit.setSitting(var1.getBoolean("Sitting"));
      }

      this.setSitting(var1.getBoolean("Sitting"));
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return this.isTamed() && this.isOwner(var1);
   }

   protected void playTameEffect(boolean var1) {
      EnumParticleTypes var2 = EnumParticleTypes.HEART;
      if (!var1) {
         var2 = EnumParticleTypes.SMOKE_NORMAL;
      }

      for(int var3 = 0; var3 < 7; ++var3) {
         double var4 = this.rand.nextGaussian() * 0.02D;
         double var6 = this.rand.nextGaussian() * 0.02D;
         double var8 = this.rand.nextGaussian() * 0.02D;
         this.world.spawnParticle(var2, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, var4, var6, var8);
      }

   }

   public boolean isTamed() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 4) != 0;
   }

   public void setTamed(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if (var1) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 | 4)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 & -5)));
      }

      this.setupTamedAI();
   }

   protected void setupTamedAI() {
   }

   public boolean isSitting() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 1) != 0;
   }

   public void setSitting(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if (var1) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 | 1)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(var2 & -2)));
      }

   }

   @Nullable
   public UUID getOwnerId() {
      return (UUID)((Optional)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
   }

   public void setOwnerId(@Nullable UUID var1) {
      this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(var1));
   }

   @Nullable
   public EntityLivingBase getOwner() {
      try {
         UUID var1 = this.getOwnerId();
         return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public boolean isOwner(EntityLivingBase var1) {
      return var1 == this.getOwner();
   }

   public EntityAISit getAISit() {
      return this.aiSit;
   }

   public boolean shouldAttackEntity(EntityLivingBase var1, EntityLivingBase var2) {
      return true;
   }

   public Team getTeam() {
      if (this.isTamed()) {
         EntityLivingBase var1 = this.getOwner();
         if (var1 != null) {
            return var1.getTeam();
         }
      }

      return super.getTeam();
   }

   public boolean isOnSameTeam(Entity var1) {
      if (this.isTamed()) {
         EntityLivingBase var2 = this.getOwner();
         if (var1 == var2) {
            return true;
         }

         if (var2 != null) {
            return var2.isOnSameTeam(var1);
         }
      }

      return super.isOnSameTeam(var1);
   }

   public void onDeath(DamageSource var1) {
      if (!this.world.isRemote && this.world.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
         this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
      }

      super.onDeath(var1);
   }

   // $FF: synthetic method
   public Entity getOwner() {
      return this.getOwner();
   }
}
