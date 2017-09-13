package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.village.Village;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class EntityAIDefendVillage extends EntityAITarget {
   EntityIronGolem irongolem;
   EntityLivingBase villageAgressorTarget;

   public EntityAIDefendVillage(EntityIronGolem entityirongolem) {
      super(entityirongolem, false, true);
      this.irongolem = entityirongolem;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      Village village = this.irongolem.getVillage();
      if (village == null) {
         return false;
      } else {
         this.villageAgressorTarget = village.findNearestVillageAggressor(this.irongolem);
         if (this.villageAgressorTarget instanceof EntityCreeper) {
            return false;
         } else if (this.isSuitableTarget(this.villageAgressorTarget, false)) {
            return true;
         } else if (this.taskOwner.getRNG().nextInt(20) == 0) {
            this.villageAgressorTarget = village.getNearestTargetPlayer(this.irongolem);
            return this.isSuitableTarget(this.villageAgressorTarget, false);
         } else {
            return false;
         }
      }
   }

   public void startExecuting() {
      this.irongolem.setGoalTarget(this.villageAgressorTarget, TargetReason.DEFEND_VILLAGE, true);
      super.startExecuting();
   }
}
