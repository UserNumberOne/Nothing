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

   public EntityXPOrb(World var1, double var2, double var4, double var6, int var8) {
      super(var1);
      this.setSize(0.5F, 0.5F);
      this.setPosition(var2, var4, var6);
      this.rotationYaw = (float)(Math.random() * 360.0D);
      this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
      this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
      this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
      this.xpValue = var8;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public EntityXPOrb(World var1) {
      super(var1);
      this.setSize(0.25F, 0.25F);
   }

   protected void entityInit() {
   }

   public void onUpdate() {
      super.onUpdate();
      EntityPlayer var1 = this.closestPlayer;
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
         boolean var2 = false;
         if (this.closestPlayer != var1) {
            EntityTargetLivingEntityEvent var3 = CraftEventFactory.callEntityTargetLivingEvent(this, this.closestPlayer, TargetReason.CLOSEST_PLAYER);
            EntityLivingBase var4 = var3.getTarget() == null ? null : ((CraftLivingEntity)var3.getTarget()).getHandle();
            this.closestPlayer = var4 instanceof EntityPlayer ? (EntityPlayer)var4 : null;
            var2 = var3.isCancelled();
         }

         if (!var2 && this.closestPlayer != null) {
            double var5 = (this.closestPlayer.posX - this.posX) / 8.0D;
            double var7 = (this.closestPlayer.posY + (double)this.closestPlayer.getEyeHeight() / 2.0D - this.posY) / 8.0D;
            double var9 = (this.closestPlayer.posZ - this.posZ) / 8.0D;
            double var11 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
            double var13 = 1.0D - var11;
            if (var13 > 0.0D) {
               var13 = var13 * var13;
               this.motionX += var5 / var11 * var13 * 0.1D;
               this.motionY += var7 / var11 * var13 * 0.1D;
               this.motionZ += var9 / var11 * var13 * 0.1D;
            }
         }
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      float var15 = 0.98F;
      if (this.onGround) {
         var15 = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.98F;
      }

      this.motionX *= (double)var15;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= (double)var15;
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

   protected void dealFireDamage(int var1) {
      this.attackEntityFrom(DamageSource.inFire, (float)var1);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         this.setBeenAttacked();
         this.xpOrbHealth = (int)((float)this.xpOrbHealth - var2);
         if (this.xpOrbHealth <= 0) {
            this.setDead();
         }

         return false;
      }
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setShort("Health", (short)this.xpOrbHealth);
      var1.setShort("Age", (short)this.xpOrbAge);
      var1.setShort("Value", (short)this.xpValue);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.xpOrbHealth = var1.getShort("Health");
      this.xpOrbAge = var1.getShort("Age");
      this.xpValue = var1.getShort("Value");
   }

   public void onCollideWithPlayer(EntityPlayer var1) {
      if (!this.world.isRemote && this.delayBeforeCanPickup == 0 && var1.xpCooldown == 0) {
         var1.xpCooldown = 2;
         this.world.playSound((EntityPlayer)null, var1.posX, var1.posY, var1.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_TOUCH, SoundCategory.PLAYERS, 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
         var1.onItemPickup(this, 1);
         ItemStack var2 = EnchantmentHelper.getEnchantedItem(Enchantments.MENDING, var1);
         if (var2 != null && var2.isItemDamaged()) {
            int var3 = Math.min(this.xpToDurability(this.xpValue), var2.getItemDamage());
            this.xpValue -= this.durabilityToXp(var3);
            var2.setItemDamage(var2.getItemDamage() - var3);
         }

         if (this.xpValue > 0) {
            var1.addExperience(CraftEventFactory.callPlayerExpChangeEvent(var1, this.xpValue).getAmount());
         }

         this.setDead();
      }

   }

   private int durabilityToXp(int var1) {
      return var1 / 2;
   }

   private int xpToDurability(int var1) {
      return var1 * 2;
   }

   public int getXpValue() {
      return this.xpValue;
   }

   public static int getXPSplit(int var0) {
      if (var0 > 162670129) {
         return var0 - 100000;
      } else if (var0 > 81335063) {
         return 81335063;
      } else if (var0 > 40667527) {
         return 40667527;
      } else if (var0 > 20333759) {
         return 20333759;
      } else if (var0 > 10166857) {
         return 10166857;
      } else if (var0 > 5083423) {
         return 5083423;
      } else if (var0 > 2541701) {
         return 2541701;
      } else if (var0 > 1270849) {
         return 1270849;
      } else if (var0 > 635413) {
         return 635413;
      } else if (var0 > 317701) {
         return 317701;
      } else if (var0 > 158849) {
         return 158849;
      } else if (var0 > 79423) {
         return 79423;
      } else if (var0 > 39709) {
         return 39709;
      } else if (var0 > 19853) {
         return 19853;
      } else if (var0 > 9923) {
         return 9923;
      } else if (var0 > 4957) {
         return 4957;
      } else {
         return var0 >= 2477 ? 2477 : (var0 >= 1237 ? 1237 : (var0 >= 617 ? 617 : (var0 >= 307 ? 307 : (var0 >= 149 ? 149 : (var0 >= 73 ? 73 : (var0 >= 37 ? 37 : (var0 >= 17 ? 17 : (var0 >= 7 ? 7 : (var0 >= 3 ? 3 : 1)))))))));
      }
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }
}
