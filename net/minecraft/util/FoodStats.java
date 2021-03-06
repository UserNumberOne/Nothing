package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class FoodStats {
   public int foodLevel = 20;
   public float foodSaturationLevel = 5.0F;
   public float foodExhaustionLevel;
   private int foodTimer;
   private EntityPlayer entityhuman;
   private int prevFoodLevel = 20;

   public FoodStats() {
      throw new AssertionError("Whoopsie, we missed the bukkit.");
   }

   public FoodStats(EntityPlayer var1) {
      Validate.notNull(var1);
      this.entityhuman = var1;
   }

   public void addStats(int var1, float var2) {
      this.foodLevel = Math.min(var1 + this.foodLevel, 20);
      this.foodSaturationLevel = Math.min(this.foodSaturationLevel + (float)var1 * var2 * 2.0F, (float)this.foodLevel);
   }

   public void addStats(ItemFood var1, ItemStack var2) {
      int var3 = this.foodLevel;
      FoodLevelChangeEvent var4 = CraftEventFactory.callFoodLevelChangeEvent(this.entityhuman, var1.getHealAmount(var2) + var3);
      if (!var4.isCancelled()) {
         this.addStats(var4.getFoodLevel() - var3, var1.getSaturationModifier(var2));
      }

      ((EntityPlayerMP)this.entityhuman).getBukkitEntity().sendHealthUpdate();
   }

   public void onUpdate(EntityPlayer var1) {
      EnumDifficulty var2 = var1.world.getDifficulty();
      this.prevFoodLevel = this.foodLevel;
      if (this.foodExhaustionLevel > 4.0F) {
         this.foodExhaustionLevel -= 4.0F;
         if (this.foodSaturationLevel > 0.0F) {
            this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0F, 0.0F);
         } else if (var2 != EnumDifficulty.PEACEFUL) {
            FoodLevelChangeEvent var3 = CraftEventFactory.callFoodLevelChangeEvent(var1, Math.max(this.foodLevel - 1, 0));
            if (!var3.isCancelled()) {
               this.foodLevel = var3.getFoodLevel();
            }

            ((EntityPlayerMP)var1).connection.sendPacket(new SPacketUpdateHealth(((EntityPlayerMP)var1).getBukkitEntity().getScaledHealth(), this.foodLevel, this.foodSaturationLevel));
         }
      }

      boolean var5 = var1.world.getGameRules().getBoolean("naturalRegeneration");
      if (var5 && this.foodSaturationLevel > 0.0F && var1.shouldHeal() && this.foodLevel >= 20) {
         ++this.foodTimer;
         if (this.foodTimer >= 10) {
            float var4 = Math.min(this.foodSaturationLevel, 4.0F);
            var1.heal(var4 / 4.0F, RegainReason.SATIATED);
            this.addExhaustion(var4);
            this.foodTimer = 0;
         }
      } else if (var5 && this.foodLevel >= 18 && var1.shouldHeal()) {
         ++this.foodTimer;
         if (this.foodTimer >= 80) {
            var1.heal(1.0F, RegainReason.SATIATED);
            this.addExhaustion(4.0F);
            this.foodTimer = 0;
         }
      } else if (this.foodLevel <= 0) {
         ++this.foodTimer;
         if (this.foodTimer >= 80) {
            if (var1.getHealth() > 10.0F || var2 == EnumDifficulty.HARD || var1.getHealth() > 1.0F && var2 == EnumDifficulty.NORMAL) {
               var1.attackEntityFrom(DamageSource.starve, 1.0F);
            }

            this.foodTimer = 0;
         }
      } else {
         this.foodTimer = 0;
      }

   }

   public void readNBT(NBTTagCompound var1) {
      if (var1.hasKey("foodLevel", 99)) {
         this.foodLevel = var1.getInteger("foodLevel");
         this.foodTimer = var1.getInteger("foodTickTimer");
         this.foodSaturationLevel = var1.getFloat("foodSaturationLevel");
         this.foodExhaustionLevel = var1.getFloat("foodExhaustionLevel");
      }

   }

   public void writeNBT(NBTTagCompound var1) {
      var1.setInteger("foodLevel", this.foodLevel);
      var1.setInteger("foodTickTimer", this.foodTimer);
      var1.setFloat("foodSaturationLevel", this.foodSaturationLevel);
      var1.setFloat("foodExhaustionLevel", this.foodExhaustionLevel);
   }

   public int getFoodLevel() {
      return this.foodLevel;
   }

   public boolean needFood() {
      return this.foodLevel < 20;
   }

   public void addExhaustion(float var1) {
      this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + var1, 40.0F);
   }

   public float getSaturationLevel() {
      return this.foodSaturationLevel;
   }

   public void setFoodLevel(int var1) {
      this.foodLevel = var1;
   }
}
