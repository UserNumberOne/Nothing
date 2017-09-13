package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAISit extends EntityAIBase {
   private final EntityTameable theEntity;
   private boolean isSitting;

   public EntityAISit(EntityTameable entitytameableanimal) {
      this.theEntity = entitytameableanimal;
      this.setMutexBits(5);
   }

   public boolean shouldExecute() {
      if (!this.theEntity.isTamed()) {
         return this.isSitting && this.theEntity.getAttackTarget() == null;
      } else if (this.theEntity.isInWater()) {
         return false;
      } else if (!this.theEntity.onGround) {
         return false;
      } else {
         EntityLivingBase entityliving = this.theEntity.getOwner();
         return entityliving == null ? true : (this.theEntity.getDistanceSqToEntity(entityliving) < 144.0D && entityliving.getAITarget() != null ? false : this.isSitting);
      }
   }

   public void startExecuting() {
      this.theEntity.getNavigator().clearPathEntity();
      this.theEntity.setSitting(true);
   }

   public void resetTask() {
      this.theEntity.setSitting(false);
   }

   public void setSitting(boolean flag) {
      this.isSitting = flag;
   }
}
