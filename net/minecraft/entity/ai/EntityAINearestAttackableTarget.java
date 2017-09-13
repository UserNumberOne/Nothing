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

   public EntityAINearestAttackableTarget(EntityCreature entitycreature, Class oclass, boolean flag) {
      this(entitycreature, oclass, flag, false);
   }

   public EntityAINearestAttackableTarget(EntityCreature entitycreature, Class oclass, boolean flag, boolean flag1) {
      this(entitycreature, oclass, 10, flag, flag1, (Predicate)null);
   }

   public EntityAINearestAttackableTarget(EntityCreature entitycreature, Class oclass, int i, boolean flag, boolean flag1, @Nullable final Predicate predicate) {
      super(entitycreature, flag, flag1);
      this.targetClass = oclass;
      this.targetChance = i;
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(entitycreature);
      this.setMutexBits(1);
      this.targetEntitySelector = new Predicate() {
         public boolean apply(@Nullable EntityLivingBase t0) {
            return t0 == null ? false : (predicate != null && !predicate.apply(t0) ? false : (!EntitySelectors.NOT_SPECTATING.apply(t0) ? false : EntityAINearestAttackableTarget.this.isSuitableTarget(t0, false)));
         }

         public boolean apply(Object object) {
            return this.apply((EntityLivingBase)object);
         }
      };
   }

   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
         return false;
      } else if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class) {
         List list = this.taskOwner.world.getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
         if (list.isEmpty()) {
            return false;
         } else {
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntity = (EntityLivingBase)list.get(0);
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

   protected AxisAlignedBB getTargetableArea(double d0) {
      return this.taskOwner.getEntityBoundingBox().expand(d0, 4.0D, d0);
   }

   public void startExecuting() {
      this.taskOwner.setGoalTarget(this.targetEntity, this.targetEntity instanceof EntityPlayerMP ? TargetReason.CLOSEST_PLAYER : TargetReason.CLOSEST_ENTITY, true);
      super.startExecuting();
   }

   public static class Sorter implements Comparator {
      private final Entity theEntity;

      public Sorter(Entity entity) {
         this.theEntity = entity;
      }

      public int compare(Entity param1, Entity param2) {
         // $FF: Couldn't be decompiled
      }

      public int compare(Entity param1, Entity param2) {
         // $FF: Couldn't be decompiled
      }
   }
}
