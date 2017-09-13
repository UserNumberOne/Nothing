package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAITargetNonTamed extends EntityAINearestAttackableTarget {
   private final EntityTameable theTameable;

   public EntityAITargetNonTamed(EntityTameable var1, Class var2, boolean var3, Predicate var4) {
      super(entityIn, classTarget, 10, checkSight, false, targetSelector);
      this.theTameable = entityIn;
   }

   public boolean shouldExecute() {
      return !this.theTameable.isTamed() && super.shouldExecute();
   }
}
