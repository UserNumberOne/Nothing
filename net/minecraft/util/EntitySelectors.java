package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;

public final class EntitySelectors {
   public static final Predicate IS_ALIVE = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1.isEntityAlive();
      }
   };
   public static final Predicate IS_STANDALONE = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1.isEntityAlive() && !var1.isBeingRidden() && !var1.isRiding();
      }
   };
   public static final Predicate HAS_INVENTORY = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof IInventory && var1.isEntityAlive();
      }
   };
   public static final Predicate CAN_AI_TARGET = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator() && !((EntityPlayer)var1).isCreative();
      }
   };
   public static final Predicate NOT_SPECTATING = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator();
      }
   };
   public static final Predicate IS_SHULKER = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityShulker && var1.isEntityAlive();
      }
   };

   public static Predicate withinRange(final double var0, final double var2, final double var4, double var6) {
      final double var8 = var6 * var6;
      return new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return var1 != null && var1.getDistanceSq(var0, var2, var4) <= var8;
         }
      };
   }

   public static Predicate getTeamCollisionPredicate(final Entity var0) {
      final Team var1 = var0.getTeam();
      final Team.CollisionRule var2 = var1 == null ? Team.CollisionRule.ALWAYS : var1.getCollisionRule();
      Predicate var3 = var2 == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : Predicates.and(NOT_SPECTATING, new Predicate() {
         public boolean apply(@Nullable Entity var1x) {
            if (!var1x.canBePushed()) {
               return false;
            } else if (!var0.world.isRemote || var1x instanceof EntityPlayer && ((EntityPlayer)var1x).isUser()) {
               Team var2x = var1x.getTeam();
               Team.CollisionRule var3 = var2x == null ? Team.CollisionRule.ALWAYS : var2x.getCollisionRule();
               if (var3 == Team.CollisionRule.NEVER) {
                  return false;
               } else {
                  boolean var4 = var1 != null && var1.isSameTeam(var2x);
                  return (var2 == Team.CollisionRule.HIDE_FOR_OWN_TEAM || var3 == Team.CollisionRule.HIDE_FOR_OWN_TEAM) && var4 ? false : var2 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS && var3 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || var4;
               }
            } else {
               return false;
            }
         }
      });
      return var3;
   }

   public static class ArmoredMob implements Predicate {
      private final ItemStack armor;

      public ArmoredMob(ItemStack var1) {
         this.armor = var1;
      }

      public boolean apply(@Nullable Entity var1) {
         if (!var1.isEntityAlive()) {
            return false;
         } else if (!(var1 instanceof EntityLivingBase)) {
            return false;
         } else {
            EntityLivingBase var2 = (EntityLivingBase)var1;
            return var2.getItemStackFromSlot(EntityLiving.getSlotForItemStack(this.armor)) != null ? false : (var2 instanceof EntityLiving ? ((EntityLiving)var2).canPickUpLoot() : (var2 instanceof EntityArmorStand ? true : var2 instanceof EntityPlayer));
         }
      }
   }
}
