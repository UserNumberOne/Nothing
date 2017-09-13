package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public abstract class EntityAgeable extends EntityCreature {
   private static final DataParameter BABY = EntityDataManager.createKey(EntityAgeable.class, DataSerializers.BOOLEAN);
   protected int growingAge;
   protected int forcedAge;
   protected int forcedAgeTimer;
   private float ageWidth = -1.0F;
   private float ageHeight;

   public EntityAgeable(World var1) {
      super(var1);
   }

   public abstract EntityAgeable createChild(EntityAgeable var1);

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (var3 != null && var3.getItem() == Items.SPAWN_EGG) {
         if (!this.world.isRemote) {
            Class var4 = (Class)EntityList.NAME_TO_CLASS.get(ItemMonsterPlacer.getEntityIdFromItem(var3));
            if (var4 != null && this.getClass() == var4) {
               EntityAgeable var5 = this.createChild(this);
               if (var5 != null) {
                  var5.setGrowingAge(-24000);
                  var5.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
                  this.world.spawnEntity(var5);
                  if (var3.hasDisplayName()) {
                     var5.setCustomNameTag(var3.getDisplayName());
                  }

                  if (!var1.capabilities.isCreativeMode) {
                     --var3.stackSize;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(BABY, Boolean.valueOf(false));
   }

   public int getGrowingAge() {
      return this.world.isRemote ? (((Boolean)this.dataManager.get(BABY)).booleanValue() ? -1 : 1) : this.growingAge;
   }

   public void ageUp(int var1, boolean var2) {
      int var3 = this.getGrowingAge();
      int var4 = var3;
      var3 = var3 + var1 * 20;
      if (var3 > 0) {
         var3 = 0;
         if (var4 < 0) {
            this.onGrowingAdult();
         }
      }

      int var5 = var3 - var4;
      this.setGrowingAge(var3);
      if (var2) {
         this.forcedAge += var5;
         if (this.forcedAgeTimer == 0) {
            this.forcedAgeTimer = 40;
         }
      }

      if (this.getGrowingAge() == 0) {
         this.setGrowingAge(this.forcedAge);
      }

   }

   public void addGrowth(int var1) {
      this.ageUp(var1, false);
   }

   public void setGrowingAge(int var1) {
      this.dataManager.set(BABY, Boolean.valueOf(var1 < 0));
      this.growingAge = var1;
      this.setScaleForAge(this.isChild());
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Age", this.getGrowingAge());
      var1.setInteger("ForcedAge", this.forcedAge);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setGrowingAge(var1.getInteger("Age"));
      this.forcedAge = var1.getInteger("ForcedAge");
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (BABY.equals(var1)) {
         this.setScaleForAge(this.isChild());
      }

      super.notifyDataManagerChange(var1);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.world.isRemote) {
         if (this.forcedAgeTimer > 0) {
            if (this.forcedAgeTimer % 4 == 0) {
               this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, 0.0D, 0.0D, 0.0D);
            }

            --this.forcedAgeTimer;
         }
      } else {
         int var1 = this.getGrowingAge();
         if (var1 < 0) {
            ++var1;
            this.setGrowingAge(var1);
            if (var1 == 0) {
               this.onGrowingAdult();
            }
         } else if (var1 > 0) {
            --var1;
            this.setGrowingAge(var1);
         }
      }

   }

   protected void onGrowingAdult() {
   }

   public boolean isChild() {
      return this.getGrowingAge() < 0;
   }

   public void setScaleForAge(boolean var1) {
      this.setScale(var1 ? 0.5F : 1.0F);
   }

   protected final void setSize(float var1, float var2) {
      boolean var3 = this.ageWidth > 0.0F;
      this.ageWidth = var1;
      this.ageHeight = var2;
      if (!var3) {
         this.setScale(1.0F);
      }

   }

   protected final void setScale(float var1) {
      super.setSize(this.ageWidth * var1, this.ageHeight * var1);
   }
}
