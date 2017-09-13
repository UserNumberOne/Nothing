package net.minecraft.entity.ai;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIFleeSun extends EntityAIBase {
   private final EntityCreature theCreature;
   private double shelterX;
   private double shelterY;
   private double shelterZ;
   private final double movementSpeed;
   private final World world;

   public EntityAIFleeSun(EntityCreature var1, double var2) {
      this.theCreature = var1;
      this.movementSpeed = var2;
      this.world = var1.world;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (!this.world.isDaytime()) {
         return false;
      } else if (!this.theCreature.isBurning()) {
         return false;
      } else if (!this.world.canSeeSky(new BlockPos(this.theCreature.posX, this.theCreature.getEntityBoundingBox().minY, this.theCreature.posZ))) {
         return false;
      } else if (this.theCreature.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
         return false;
      } else {
         Vec3d var1 = this.findPossibleShelter();
         if (var1 == null) {
            return false;
         } else {
            this.shelterX = var1.xCoord;
            this.shelterY = var1.yCoord;
            this.shelterZ = var1.zCoord;
            return true;
         }
      }
   }

   public boolean continueExecuting() {
      return !this.theCreature.getNavigator().noPath();
   }

   public void startExecuting() {
      this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
   }

   @Nullable
   private Vec3d findPossibleShelter() {
      Random var1 = this.theCreature.getRNG();
      BlockPos var2 = new BlockPos(this.theCreature.posX, this.theCreature.getEntityBoundingBox().minY, this.theCreature.posZ);

      for(int var3 = 0; var3 < 10; ++var3) {
         BlockPos var4 = var2.add(var1.nextInt(20) - 10, var1.nextInt(6) - 3, var1.nextInt(20) - 10);
         if (!this.world.canSeeSky(var4) && this.theCreature.getBlockPathWeight(var4) < 0.0F) {
            return new Vec3d((double)var4.getX(), (double)var4.getY(), (double)var4.getZ());
         }
      }

      return null;
   }
}
