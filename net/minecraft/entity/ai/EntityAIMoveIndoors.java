package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveIndoors extends EntityAIBase {
   private final EntityCreature entityObj;
   private VillageDoorInfo doorInfo;
   private int insidePosX = -1;
   private int insidePosZ = -1;

   public EntityAIMoveIndoors(EntityCreature var1) {
      this.entityObj = var1;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      BlockPos var1 = new BlockPos(this.entityObj);
      if ((!this.entityObj.world.isDaytime() || this.entityObj.world.isRaining() && !this.entityObj.world.getBiome(var1).canRain()) && !this.entityObj.world.provider.hasNoSky()) {
         if (this.entityObj.getRNG().nextInt(50) != 0) {
            return false;
         } else if (this.insidePosX != -1 && this.entityObj.getDistanceSq((double)this.insidePosX, this.entityObj.posY, (double)this.insidePosZ) < 4.0D) {
            return false;
         } else {
            Village var2 = this.entityObj.world.getVillageCollection().getNearestVillage(var1, 14);
            if (var2 == null) {
               return false;
            } else {
               this.doorInfo = var2.getDoorInfo(var1);
               return this.doorInfo != null;
            }
         }
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return !this.entityObj.getNavigator().noPath();
   }

   public void startExecuting() {
      this.insidePosX = -1;
      BlockPos var1 = this.doorInfo.getInsideBlockPos();
      int var2 = var1.getX();
      int var3 = var1.getY();
      int var4 = var1.getZ();
      if (this.entityObj.getDistanceSq(var1) > 256.0D) {
         Vec3d var5 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entityObj, 14, 3, new Vec3d((double)var2 + 0.5D, (double)var3, (double)var4 + 0.5D));
         if (var5 != null) {
            this.entityObj.getNavigator().tryMoveToXYZ(var5.xCoord, var5.yCoord, var5.zCoord, 1.0D);
         }
      } else {
         this.entityObj.getNavigator().tryMoveToXYZ((double)var2 + 0.5D, (double)var3, (double)var4 + 0.5D, 1.0D);
      }

   }

   public void resetTask() {
      this.insidePosX = this.doorInfo.getInsideBlockPos().getX();
      this.insidePosZ = this.doorInfo.getInsideBlockPos().getZ();
      this.doorInfo = null;
   }
}