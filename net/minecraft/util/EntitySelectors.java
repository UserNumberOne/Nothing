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

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   public static final Predicate IS_STANDALONE = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1.isEntityAlive() && !var1.isBeingRidden() && !var1.isRiding();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   public static final Predicate HAS_INVENTORY = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof IInventory && var1.isEntityAlive();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   public static final Predicate CAN_AI_TARGET = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator() && !((EntityPlayer)var1).isCreative();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   public static final Predicate NOT_SPECTATING = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(var1 instanceof EntityPlayer) || !((EntityPlayer)var1).isSpectator();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   public static final Predicate IS_SHULKER = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityShulker && var1.isEntityAlive();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };

   public static Predicate withinRange(final double var0, final double var2, final double var4, double var6) {
      final double var8 = var6 * var6;
      return new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return var1 != null && var1.getDistanceSq(var0, var2, var4) <= var8;
         }

         // $FF: synthetic method
         public boolean apply(Object var1) {
            return this.apply((Entity)var1);
         }
      };
   }

   public static Predicate getTeamCollisionPredicate(final Entity var0) {
      final Team var1 = var0.getTeam();
      final Team.CollisionRule var2 = var1 == null ? Team.CollisionRule.ALWAYS : var1.getCollisionRule();
      return var2 == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : Predicates.and(NOT_SPECTATING, new Predicate() {
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
                  if ((var2 == Team.CollisionRule.HIDE_FOR_OWN_TEAM || var3 == Team.CollisionRule.HIDE_FOR_OWN_TEAM) && var4) {
                     return false;
                  } else {
                     return var2 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS && var3 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || var4;
                  }
               }
            } else {
               return false;
            }
         }

         // $FF: synthetic method
         public boolean apply(Object var1x) {
            return this.apply((Entity)var1x);
         }
      });
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
            if (var2.getItemStackFromSlot(EntityLiving.getSlotForItemStack(this.armor)) != null) {
               return false;
            } else if (var2 instanceof EntityLiving) {
               return ((EntityLiving)var2).canPickUpLoot();
            } else if (var2 instanceof EntityArmorStand) {
               return true;
            } else {
               return var2 instanceof EntityPlayer;
            }
         }
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   }
}
