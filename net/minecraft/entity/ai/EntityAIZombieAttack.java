package net.minecraft.entity.ai;

import net.minecraft.entity.monster.EntityZombie;

public class EntityAIZombieAttack extends EntityAIAttackMelee {
   private final EntityZombie zombie;
   private int raiseArmTicks;

   public EntityAIZombieAttack(EntityZombie var1, double var2, boolean var4) {
      super(var1, var2, var4);
      this.zombie = var1;
   }

   public void startExecuting() {
      super.startExecuting();
      this.raiseArmTicks = 0;
   }

   public void resetTask() {
      super.resetTask();
      this.zombie.setArmsRaised(false);
   }

   public void updateTask() {
      super.updateTask();
      ++this.raiseArmTicks;
      if (this.raiseArmTicks >= 5 && this.attackTick < 10) {
         this.zombie.setArmsRaised(true);
      } else {
         this.zombie.setArmsRaised(false);
      }

   }
}
