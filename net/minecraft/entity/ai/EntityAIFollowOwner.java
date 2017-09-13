package net.minecraft.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase {
   private final EntityTameable thePet;
   private EntityLivingBase theOwner;
   World world;
   private final double followSpeed;
   private final PathNavigate petPathfinder;
   private int timeToRecalcPath;
   float maxDist;
   float minDist;
   private float oldWaterCost;

   public EntityAIFollowOwner(EntityTameable var1, double var2, float var4, float var5) {
      this.thePet = var1;
      this.world = var1.world;
      this.followSpeed = var2;
      this.petPathfinder = var1.getNavigator();
      this.minDist = var4;
      this.maxDist = var5;
      this.setMutexBits(3);
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   public boolean shouldExecute() {
      EntityLivingBase var1 = this.thePet.getOwner();
      if (var1 == null) {
         return false;
      } else if (var1 instanceof EntityPlayer && ((EntityPlayer)var1).isSpectator()) {
         return false;
      } else if (this.thePet.isSitting()) {
         return false;
      } else if (this.thePet.getDistanceSqToEntity(var1) < (double)(this.minDist * this.minDist)) {
         return false;
      } else {
         this.theOwner = var1;
         return true;
      }
   }

   public boolean continueExecuting() {
      return !this.petPathfinder.noPath() && this.thePet.getDistanceSqToEntity(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.isSitting();
   }

   public void startExecuting() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.thePet.getPathPriority(PathNodeType.WATER);
      this.thePet.setPathPriority(PathNodeType.WATER, 0.0F);
   }

   public void resetTask() {
      this.theOwner = null;
      this.petPathfinder.clearPathEntity();
      this.thePet.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
   }

   private boolean isEmptyBlock(BlockPos var1) {
      IBlockState var2 = this.world.getBlockState(var1);
      return var2.getMaterial() == Material.AIR ? true : !var2.isFullCube();
   }

   public void updateTask() {
      this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());
      if (!this.thePet.isSitting() && --this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         if (!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.followSpeed) && !this.thePet.getLeashed() && this.thePet.getDistanceSqToEntity(this.theOwner) >= 144.0D) {
            int var1 = MathHelper.floor(this.theOwner.posX) - 2;
            int var2 = MathHelper.floor(this.theOwner.posZ) - 2;
            int var3 = MathHelper.floor(this.theOwner.getEntityBoundingBox().minY);

            for(int var4 = 0; var4 <= 4; ++var4) {
               for(int var5 = 0; var5 <= 4; ++var5) {
                  if ((var4 < 1 || var5 < 1 || var4 > 3 || var5 > 3) && this.world.getBlockState(new BlockPos(var1 + var4, var3 - 1, var2 + var5)).isFullyOpaque() && this.isEmptyBlock(new BlockPos(var1 + var4, var3, var2 + var5)) && this.isEmptyBlock(new BlockPos(var1 + var4, var3 + 1, var2 + var5))) {
                     this.thePet.setLocationAndAngles((double)((float)(var1 + var4) + 0.5F), (double)var3, (double)((float)(var2 + var5) + 0.5F), this.thePet.rotationYaw, this.thePet.rotationPitch);
                     this.petPathfinder.clearPathEntity();
                     return;
                  }
               }
            }
         }
      }

   }
}
