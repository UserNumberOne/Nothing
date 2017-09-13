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
         return p_apply_1_.isEntityAlive();
      }
   };
   public static final Predicate IS_STANDALONE = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return p_apply_1_.isEntityAlive() && !p_apply_1_.isBeingRidden() && !p_apply_1_.isRiding();
      }
   };
   public static final Predicate HAS_INVENTORY = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return p_apply_1_ instanceof IInventory && p_apply_1_.isEntityAlive();
      }
   };
   public static final Predicate CAN_AI_TARGET = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(p_apply_1_ instanceof EntityPlayer) || !((EntityPlayer)p_apply_1_).isSpectator() && !((EntityPlayer)p_apply_1_).isCreative();
      }
   };
   public static final Predicate NOT_SPECTATING = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return !(p_apply_1_ instanceof EntityPlayer) || !((EntityPlayer)p_apply_1_).isSpectator();
      }
   };
   public static final Predicate IS_SHULKER = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return p_apply_1_ instanceof EntityShulker && p_apply_1_.isEntityAlive();
      }
   };

   public static Predicate withinRange(final double var0, final double var2, final double var4, double var6) {
      final double d0 = range * range;
      return new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return p_apply_1_ != null && p_apply_1_.getDistanceSq(x, y, z) <= d0;
         }
      };
   }

   public static Predicate getTeamCollisionPredicate(final Entity var0) {
      final Team team = entityIn.getTeam();
      final Team.CollisionRule team$collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
      Predicate ret = team$collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : Predicates.and(NOT_SPECTATING, new Predicate() {
         public boolean apply(@Nullable Entity var1x) {
            if (!p_apply_1_.canBePushed()) {
               return false;
            } else if (!entityIn.world.isRemote || p_apply_1_ instanceof EntityPlayer && ((EntityPlayer)p_apply_1_).isUser()) {
               Team team1 = p_apply_1_.getTeam();
               Team.CollisionRule team$collisionrule1 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();
               if (team$collisionrule1 == Team.CollisionRule.NEVER) {
                  return false;
               } else {
                  boolean flag = team != null && team.isSameTeam(team1);
                  return (team$collisionrule == Team.CollisionRule.HIDE_FOR_OWN_TEAM || team$collisionrule1 == Team.CollisionRule.HIDE_FOR_OWN_TEAM) && flag ? false : team$collisionrule != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS && team$collisionrule1 != Team.CollisionRule.HIDE_FOR_OTHER_TEAMS || flag;
               }
            } else {
               return false;
            }
         }
      });
      return ret;
   }

   public static class ArmoredMob implements Predicate {
      private final ItemStack armor;

      public ArmoredMob(ItemStack var1) {
         this.armor = armor;
      }

      public boolean apply(@Nullable Entity var1) {
         if (!p_apply_1_.isEntityAlive()) {
            return false;
         } else if (!(p_apply_1_ instanceof EntityLivingBase)) {
            return false;
         } else {
            EntityLivingBase entitylivingbase = (EntityLivingBase)p_apply_1_;
            return entitylivingbase.getItemStackFromSlot(EntityLiving.getSlotForItemStack(this.armor)) != null ? false : (entitylivingbase instanceof EntityLiving ? ((EntityLiving)entitylivingbase).canPickUpLoot() : (entitylivingbase instanceof EntityArmorStand ? true : entitylivingbase instanceof EntityPlayer));
         }
      }
   }
}
