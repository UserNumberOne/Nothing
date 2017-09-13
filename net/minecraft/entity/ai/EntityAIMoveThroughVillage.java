package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveThroughVillage extends EntityAIBase {
   private final EntityCreature theEntity;
   private final double movementSpeed;
   private Path entityPathNavigate;
   private VillageDoorInfo doorInfo;
   private final boolean isNocturnal;
   private final List doorList = Lists.newArrayList();

   public EntityAIMoveThroughVillage(EntityCreature var1, double var2, boolean var4) {
      this.theEntity = var1;
      this.movementSpeed = var2;
      this.isNocturnal = var4;
      this.setMutexBits(1);
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
      }
   }

   public boolean shouldExecute() {
      this.resizeDoorList();
      if (this.isNocturnal && this.theEntity.world.isDaytime()) {
         return false;
      } else {
         Village var1 = this.theEntity.world.getVillageCollection().getNearestVillage(new BlockPos(this.theEntity), 0);
         if (var1 == null) {
            return false;
         } else {
            this.doorInfo = this.findNearestDoor(var1);
            if (this.doorInfo == null) {
               return false;
            } else {
               PathNavigateGround var2 = (PathNavigateGround)this.theEntity.getNavigator();
               boolean var3 = var2.getEnterDoors();
               var2.setBreakDoors(false);
               this.entityPathNavigate = var2.getPathToPos(this.doorInfo.getDoorBlockPos());
               var2.setBreakDoors(var3);
               if (this.entityPathNavigate != null) {
                  return true;
               } else {
                  Vec3d var4 = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 10, 7, new Vec3d((double)this.doorInfo.getDoorBlockPos().getX(), (double)this.doorInfo.getDoorBlockPos().getY(), (double)this.doorInfo.getDoorBlockPos().getZ()));
                  if (var4 == null) {
                     return false;
                  } else {
                     var2.setBreakDoors(false);
                     this.entityPathNavigate = this.theEntity.getNavigator().getPathToXYZ(var4.xCoord, var4.yCoord, var4.zCoord);
                     var2.setBreakDoors(var3);
                     return this.entityPathNavigate != null;
                  }
               }
            }
         }
      }
   }

   public boolean continueExecuting() {
      if (this.theEntity.getNavigator().noPath()) {
         return false;
      } else {
         float var1 = this.theEntity.width + 4.0F;
         return this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) > (double)(var1 * var1);
      }
   }

   public void startExecuting() {
      this.theEntity.getNavigator().setPath(this.entityPathNavigate, this.movementSpeed);
   }

   public void resetTask() {
      if (this.theEntity.getNavigator().noPath() || this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) < 16.0D) {
         this.doorList.add(this.doorInfo);
      }

   }

   private VillageDoorInfo findNearestDoor(Village var1) {
      VillageDoorInfo var2 = null;
      int var3 = Integer.MAX_VALUE;

      for(VillageDoorInfo var6 : var1.getVillageDoorInfoList()) {
         int var7 = var6.getDistanceSquared(MathHelper.floor(this.theEntity.posX), MathHelper.floor(this.theEntity.posY), MathHelper.floor(this.theEntity.posZ));
         if (var7 < var3 && !this.doesDoorListContain(var6)) {
            var2 = var6;
            var3 = var7;
         }
      }

      return var2;
   }

   private boolean doesDoorListContain(VillageDoorInfo var1) {
      for(VillageDoorInfo var3 : this.doorList) {
         if (var1.getDoorBlockPos().equals(var3.getDoorBlockPos())) {
            return true;
         }
      }

      return false;
   }

   private void resizeDoorList() {
      if (this.doorList.size() > 15) {
         this.doorList.remove(0);
      }

   }
}
