package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class CombatTracker {
   private final List combatEntries = Lists.newArrayList();
   private final EntityLivingBase fighter;
   private int lastDamageTime;
   private int combatStartTime;
   private int combatEndTime;
   private boolean inCombat;
   private boolean takingDamage;
   private String fallSuffix;

   public CombatTracker(EntityLivingBase var1) {
      this.fighter = var1;
   }

   public void calculateFallSuffix() {
      this.resetFallSuffix();
      if (this.fighter.isOnLadder()) {
         Block var1 = this.fighter.world.getBlockState(new BlockPos(this.fighter.posX, this.fighter.getEntityBoundingBox().minY, this.fighter.posZ)).getBlock();
         if (var1 == Blocks.LADDER) {
            this.fallSuffix = "ladder";
         } else if (var1 == Blocks.VINE) {
            this.fallSuffix = "vines";
         }
      } else if (this.fighter.isInWater()) {
         this.fallSuffix = "water";
      }

   }

   public void trackDamage(DamageSource var1, float var2, float var3) {
      this.reset();
      this.calculateFallSuffix();
      CombatEntry var4 = new CombatEntry(var1, this.fighter.ticksExisted, var2, var3, this.fallSuffix, this.fighter.fallDistance);
      this.combatEntries.add(var4);
      this.lastDamageTime = this.fighter.ticksExisted;
      this.takingDamage = true;
      if (var4.isLivingDamageSrc() && !this.inCombat && this.fighter.isEntityAlive()) {
         this.inCombat = true;
         this.combatStartTime = this.fighter.ticksExisted;
         this.combatEndTime = this.combatStartTime;
         this.fighter.sendEnterCombat();
      }

   }

   public ITextComponent getDeathMessage() {
      if (this.combatEntries.isEmpty()) {
         return new TextComponentTranslation("death.attack.generic", new Object[]{this.fighter.getDisplayName()});
      } else {
         CombatEntry var1 = this.getBestCombatEntry();
         CombatEntry var2 = (CombatEntry)this.combatEntries.get(this.combatEntries.size() - 1);
         ITextComponent var3 = var2.getDamageSrcDisplayName();
         Entity var4 = var2.getDamageSrc().getEntity();
         Object var6;
         if (var1 != null && var2.getDamageSrc() == DamageSource.fall) {
            ITextComponent var5 = var1.getDamageSrcDisplayName();
            if (var1.getDamageSrc() != DamageSource.fall && var1.getDamageSrc() != DamageSource.outOfWorld) {
               if (var5 != null && (var3 == null || !var5.equals(var3))) {
                  Entity var9 = var1.getDamageSrc().getEntity();
                  ItemStack var8 = var9 instanceof EntityLivingBase ? ((EntityLivingBase)var9).getHeldItemMainhand() : null;
                  if (var8 != null && var8.hasDisplayName()) {
                     var6 = new TextComponentTranslation("death.fell.assist.item", new Object[]{this.fighter.getDisplayName(), var5, var8.getTextComponent()});
                  } else {
                     var6 = new TextComponentTranslation("death.fell.assist", new Object[]{this.fighter.getDisplayName(), var5});
                  }
               } else if (var3 != null) {
                  ItemStack var7 = var4 instanceof EntityLivingBase ? ((EntityLivingBase)var4).getHeldItemMainhand() : null;
                  if (var7 != null && var7.hasDisplayName()) {
                     var6 = new TextComponentTranslation("death.fell.finish.item", new Object[]{this.fighter.getDisplayName(), var3, var7.getTextComponent()});
                  } else {
                     var6 = new TextComponentTranslation("death.fell.finish", new Object[]{this.fighter.getDisplayName(), var3});
                  }
               } else {
                  var6 = new TextComponentTranslation("death.fell.killer", new Object[]{this.fighter.getDisplayName()});
               }
            } else {
               var6 = new TextComponentTranslation("death.fell.accident." + this.getFallSuffix(var1), new Object[]{this.fighter.getDisplayName()});
            }
         } else {
            var6 = var2.getDamageSrc().getDeathMessage(this.fighter);
         }

         return (ITextComponent)var6;
      }
   }

   @Nullable
   public EntityLivingBase getBestAttacker() {
      EntityLivingBase var1 = null;
      EntityPlayer var2 = null;
      float var3 = 0.0F;
      float var4 = 0.0F;

      for(CombatEntry var6 : this.combatEntries) {
         if (var6.getDamageSrc().getEntity() instanceof EntityPlayer && (var2 == null || var6.getDamage() > var4)) {
            var4 = var6.getDamage();
            var2 = (EntityPlayer)var6.getDamageSrc().getEntity();
         }

         if (var6.getDamageSrc().getEntity() instanceof EntityLivingBase && (var1 == null || var6.getDamage() > var3)) {
            var3 = var6.getDamage();
            var1 = (EntityLivingBase)var6.getDamageSrc().getEntity();
         }
      }

      if (var2 != null && var4 >= var3 / 3.0F) {
         return var2;
      } else {
         return var1;
      }
   }

   @Nullable
   private CombatEntry getBestCombatEntry() {
      CombatEntry var1 = null;
      CombatEntry var2 = null;
      float var3 = 0.0F;
      float var4 = 0.0F;

      for(int var5 = 0; var5 < this.combatEntries.size(); ++var5) {
         CombatEntry var6 = (CombatEntry)this.combatEntries.get(var5);
         CombatEntry var7 = var5 > 0 ? (CombatEntry)this.combatEntries.get(var5 - 1) : null;
         if ((var6.getDamageSrc() == DamageSource.fall || var6.getDamageSrc() == DamageSource.outOfWorld) && var6.getDamageAmount() > 0.0F && (var1 == null || var6.getDamageAmount() > var4)) {
            if (var5 > 0) {
               var1 = var7;
            } else {
               var1 = var6;
            }

            var4 = var6.getDamageAmount();
         }

         if (var6.getFallSuffix() != null && (var2 == null || var6.getDamage() > var3)) {
            var2 = var6;
            var3 = var6.getDamage();
         }
      }

      if (var4 > 5.0F && var1 != null) {
         return var1;
      } else if (var3 > 5.0F && var2 != null) {
         return var2;
      } else {
         return null;
      }
   }

   private String getFallSuffix(CombatEntry var1) {
      return var1.getFallSuffix() == null ? "generic" : var1.getFallSuffix();
   }

   public int getCombatDuration() {
      return this.inCombat ? this.fighter.ticksExisted - this.combatStartTime : this.combatEndTime - this.combatStartTime;
   }

   private void resetFallSuffix() {
      this.fallSuffix = null;
   }

   public void reset() {
      int var1 = this.inCombat ? 300 : 100;
      if (this.takingDamage && (!this.fighter.isEntityAlive() || this.fighter.ticksExisted - this.lastDamageTime > var1)) {
         boolean var2 = this.inCombat;
         this.takingDamage = false;
         this.inCombat = false;
         this.combatEndTime = this.fighter.ticksExisted;
         if (var2) {
            this.fighter.sendEndCombat();
         }

         this.combatEntries.clear();
      }

   }

   public EntityLivingBase getFighter() {
      return this.fighter;
   }
}
