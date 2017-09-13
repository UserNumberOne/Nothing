package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class EntityAIWatchClosest2 extends EntityAIWatchClosest {
   public EntityAIWatchClosest2(EntityLiving var1, Class var2, float var3, float var4) {
      super(entitylivingIn, watchTargetClass, maxDistance, chanceIn);
      this.setMutexBits(3);
   }
}
