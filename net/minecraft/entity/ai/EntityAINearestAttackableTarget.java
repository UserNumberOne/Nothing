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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

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

         public boolean apply(Object var1) {
            return this.apply((EntityLivingBase)var1);
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
            public Double apply(@Nullable EntityPlayer param1) {
               // $FF: Couldn't be decompiled
            }

            public Double apply(EntityPlayer param1) {
               // $FF: Couldn't be decompiled
            }
         }, this.targetEntitySelector);
         return this.targetEntity != null;
      }
   }

   protected AxisAlignedBB getTargetableArea(double var1) {
      return this.taskOwner.getEntityBoundingBox().expand(var1, 4.0D, var1);
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.targetEntity, this.targetEntity instanceof EntityPlayerMP ? TargetReason.CLOSEST_PLAYER : TargetReason.CLOSEST_ENTITY, true);
      super.startExecuting();
   }

   public static class Sorter implements Comparator {
      private final Entity theEntity;

      public Sorter(Entity var1) {
         this.theEntity = var1;
      }

      public int compare(Entity param1, Entity param2) {
         // $FF: Couldn't be decompiled
      }

      public int compare(Entity param1, Entity param2) {
         // $FF: Couldn't be decompiled
      }
   }
}
