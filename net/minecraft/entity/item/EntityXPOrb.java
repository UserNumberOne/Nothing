package net.minecraft.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityXPOrb extends Entity {
   public int xpColor;
   public int xpOrbAge;
   public int delayBeforeCanPickup;
   private int xpOrbHealth = 5;
   public int xpValue;
   private EntityPlayer closestPlayer;
   private int xpTargetColor;

   public EntityXPOrb(World world, double d0, double d1, double d2, int i) {
      super(world);
      this.setSize(0.5F, 0.5F);
      this.setPosition(d0, d1, d2);
      this.rotationYaw = (float)(Math.random() * 360.0D);
      this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
      this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
      this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
      this.xpValue = i;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public EntityXPOrb(World world) {
      super(world);
      this.setSize(0.25F, 0.25F);
   }

   protected void entityInit() {
   }

   public void onUpdate() {
      super.onUpdate();
      EntityPlayer prevTarget = this.closestPlayer;
      if (this.delayBeforeCanPickup > 0) {
         --this.delayBeforeCanPickup;
      }

      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (!this.hasNoGravity()) {
         this.motionY -= 0.029999999329447746D;
      }

      if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
         this.motionY = 0.20000000298023224D;
         this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
         this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
         this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
      }

      this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
      if (this.xpTargetColor < this.xpColor - 20 + this.getEntityId() % 100) {
         if (this.closestPlayer == null || this.closestPlayer.getDistanceSqToEntity(this) > 64.0D) {
            this.closestPlayer = this.world.getClosestPlayerToEntity(this, 8.0D);
         }

         this.xpTargetColor = this.xpColor;
      }

      if (this.closestPlayer != null && this.closestPlayer.isSpectator()) {
         this.closestPlayer = null;
      }

      if (this.closestPlayer != null) {
         boolean cancelled = false;
         if (this.closestPlayer != prevTarget) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this, this.closestPlayer, TargetReason.CLOSEST_PLAYER);
            EntityLivingBase target = event.getTarget() == null ? null : ((CraftLivingEntity)event.getTarget()).getHandle();
            this.closestPlayer = target instanceof EntityPlayer ? (EntityPlayer)target : null;
            cancelled = event.isCancelled();
         }

         if (!cancelled && this.closestPlayer != null) {
            double d1 = (this.closestPlayer.posX - this.posX) / 8.0D;
            double d2 = (this.closestPlayer.posY + (double)this.closestPlayer.getEyeHeight() / 2.0D - this.posY) / 8.0D;
            double d3 = (this.closestPlayer.posZ - this.posZ) / 8.0D;
            double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            double d5 = 1.0D - d4;
            if (d5 > 0.0D) {
               d5 = d5 * d5;
               this.motionX += d1 / d4 * d5 * 0.1D;
               this.motionY += d2 / d4 * d5 * 0.1D;
               this.motionZ += d3 / d4 * d5 * 0.1D;
            }
         }
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      float f = 0.98F;
      if (this.onGround) {
         f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.98F;
      }

      this.motionX *= (double)f;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= (double)f;
      if (this.onGround) {
         this.motionY *= -0.8999999761581421D;
      }

      ++this.xpColor;
      ++this.xpOrbAge;
      if (this.xpOrbAge >= 6000) {
         this.setDead();
      }

   }

   public boolean handleWaterMovement() {
      return this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
   }

   protected void dealFireDamage(int i) {
      this.attackEntityFrom(DamageSource.inFire, (float)i);
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else {
         this.setBeenAttacked();
         this.xpOrbHealth = (int)((float)this.xpOrbHealth - f);
         if (this.xpOrbHealth <= 0) {
            this.setDead();
         }

         return false;
      }
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("Health", (short)this.xpOrbHealth);
      nbttagcompound.setShort("Age", (short)this.xpOrbAge);
      nbttagcompound.setShort("Value", (short)this.xpValue);
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.xpOrbHealth = nbttagcompound.getShort("Health");
      this.xpOrbAge = nbttagcompound.getShort("Age");
      this.xpValue = nbttagcompound.getShort("Value");
   }

   public void onCollideWithPlayer(EntityPlayer entityhuman) {
      if (!this.world.isRemote && this.delayBeforeCanPickup == 0 && entityhuman.xpCooldown == 0) {
         entityhuman.xpCooldown = 2;
         this.world.playSound((EntityPlayer)null, entityhuman.posX, entityhuman.posY, entityhuman.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
         entityhuman.onItemPickup(this, 1);
         ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, entityhuman);
         if (itemstack != null && itemstack.isItemDamaged()) {
            int i = Math.min(this.xpToDurability(this.xpValue), itemstack.getItemDamage());
            this.xpValue -= this.durabilityToXp(i);
            itemstack.setItemDamage(itemstack.getItemDamage() - i);
         }

         if (this.xpValue > 0) {
            entityhuman.addExperience(CraftEventFactory.callPlayerExpChangeEvent(entityhuman, this.xpValue).getAmount());
         }

         this.setDead();
      }

   }

   private int durabilityToXp(int i) {
      return i / 2;
   }

   private int xpToDurability(int i) {
      return i * 2;
   }

   public int getXpValue() {
      return this.xpValue;
   }

   public static int getXPSplit(int i) {
      if (i > 162670129) {
         return i - 100000;
      } else if (i > 81335063) {
         return 81335063;
      } else if (i > 40667527) {
         return 40667527;
      } else if (i > 20333759) {
         return 20333759;
      } else if (i > 10166857) {
         return 10166857;
      } else if (i > 5083423) {
         return 5083423;
      } else if (i > 2541701) {
         return 2541701;
      } else if (i > 1270849) {
         return 1270849;
      } else if (i > 635413) {
         return 635413;
      } else if (i > 317701) {
         return 317701;
      } else if (i > 158849) {
         return 158849;
      } else if (i > 79423) {
         return 79423;
      } else if (i > 39709) {
         return 39709;
      } else if (i > 19853) {
         return 19853;
      } else if (i > 9923) {
         return 9923;
      } else if (i > 4957) {
         return 4957;
      } else {
         return i >= 2477 ? 2477 : (i >= 1237 ? 1237 : (i >= 617 ? 617 : (i >= 307 ? 307 : (i >= 149 ? 149 : (i >= 73 ? 73 : (i >= 37 ? 37 : (i >= 17 ? 17 : (i >= 7 ? 7 : (i >= 3 ? 3 : 1)))))))));
      }
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }
}
