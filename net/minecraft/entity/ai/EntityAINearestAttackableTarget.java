package net.minecraft.entity.ai;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAINearestAttackableTarget extends EntityAITarget {
   protected final Class targetClass;
   private final int targetChance;
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate targetEntitySelector;
   protected EntityLivingBase targetEntity;

   public EntityAINearestAttackableTarget(EntityCreature var1, Class var2, boolean var3) {
      this(var1, var2, var3, false);
   }

   public EntityAINearestAttackableTarget(EntityCreature var1, Class var2, boolean var3, boolean var4) {
      this(var1, var2, 10, var3, var4, (Predicate)null);
   }

   public EntityAINearestAttackableTarget(EntityCreature var1, Class var2, int var3, boolean var4, boolean var5, @Nullable final Predicate var6) {
      super(var1, var4, var5);
      this.targetClass = var2;
      this.targetChance = var3;
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(var1);
      this.setMutexBits(1);
      this.targetEntitySelector = new Predicate() {
         public boolean apply(@Nullable EntityLivingBase var1) {
            return var1 == null ? false : (var6 != null && !var6.apply(var1) ? false : (!EntitySelectors.NOT_SPECTATING.apply(var1) ? false : EntityAINearestAttackableTarget.this.isSuitableTarget(var1, false)));
         }
      };
   }

   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class) {
         List var1 = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
         if (var1.isEmpty()) {
            return false;
         } else {
            Collections.sort(var1, this.theNearestAttackableTargetSorter);
            this.targetEntity = (EntityLivingBase)var1.get(0);
            return true;
         }
      } else {
         this.targetEntity = this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double)this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(), new Function() {
            @Nullable
            public Double apply(@Nullable EntityPlayer var1) {
               ItemStack var2 = var1.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
               if (var2 != null && var2.getItem() == Items.SKULL) {
                  int var3 = var2.getItemDamage();
                  boolean var4 = EntityAINearestAttackableTarget.this.taskOwner instanceof EntitySkeleton && ((EntitySkeleton)EntityAINearestAttackableTarget.this.taskOwner).getSkeletonType() == SkeletonType.NORMAL && var3 == 0;
                  boolean var5 = EntityAINearestAttackableTarget.this.taskOwner instanceof EntityZombie && var3 == 2;
                  boolean var6 = EntityAINearestAttackableTarget.this.taskOwner instanceof EntityCreeper && var3 == 4;
                  if (var4 || var5 || var6) {
                     return 0.5D;
                  }
               }

               return 1.0D;
            }
         }, this.targetEntitySelector);
         return this.targetEntity != null;
      }
   }

   protected AxisAlignedBB getTargetableArea(double var1) {
      return this.taskOwner.getEntityBoundingBox().expand(var1, 4.0D, var1);
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.targetEntity);
      super.startExecuting();
   }

   public static class Sorter implements Comparator {
      private final Entity theEntity;

      public Sorter(Entity var1) {
         this.theEntity = var1;
      }

      public int compare(Entity var1, Entity var2) {
         double var3 = this.theEntity.getDistanceSqToEntity(var1);
         double var5 = this.theEntity.getDistanceSqToEntity(var2);
         return var3 < var5 ? -1 : (var3 > var5 ? 1 : 0);
      }
   }
}
