package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAIRestrictSun extends EntityAIBase {
   private final EntityCreature theEntity;

   public EntityAIRestrictSun(EntityCreature var1) {
      this.theEntity = var1;
   }

   public boolean shouldExecute() {
      return this.theEntity.world.isDaytime() && this.theEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null;
   }

   public void startExecuting() {
      ((PathNavigateGround)this.theEntity.getNavigator()).setAvoidSun(true);
   }

   public void resetTask() {
      ((PathNavigateGround)this.theEntity.getNavigator()).setAvoidSun(false);
   }
}
