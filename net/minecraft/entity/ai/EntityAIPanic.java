package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAIPanic extends EntityAIBase {
   private final EntityCreature theEntityCreature;
   protected double speed;
   private double randPosX;
   private double randPosY;
   private double randPosZ;

   public EntityAIPanic(EntityCreature entitycreature, double d0) {
      this.theEntityCreature = entitycreature;
      this.speed = d0;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (this.theEntityCreature.getAITarget() == null && !this.theEntityCreature.isBurning()) {
         return false;
      } else if (!this.theEntityCreature.isBurning()) {
         Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.theEntityCreature, 5, 4);
         if (vec3d == null) {
            return false;
         } else {
            this.randPosX = vec3d.xCoord;
            this.randPosY = vec3d.yCoord;
            this.randPosZ = vec3d.zCoord;
            return true;
         }
      } else {
         BlockPos blockposition = this.getRandPos(this.theEntityCreature.world, this.theEntityCreature, 5, 4);
         if (blockposition == null) {
            return false;
         } else {
            this.randPosX = (double)blockposition.getX();
            this.randPosY = (double)blockposition.getY();
            this.randPosZ = (double)blockposition.getZ();
            return true;
         }
      }
   }

   public void startExecuting() {
      this.theEntityCreature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
   }

   public boolean continueExecuting() {
      if (this.theEntityCreature.ticksExisted - this.theEntityCreature.restoreWaterCost > 100) {
         this.theEntityCreature.onKillEntity((EntityLivingBase)null);
         return false;
      } else {
         return !this.theEntityCreature.getNavigator().noPath();
      }
   }

   private BlockPos getRandPos(World world, Entity entity, int i, int j) {
      BlockPos blockposition = new BlockPos(entity);
      BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
      int k = blockposition.getX();
      int l = blockposition.getY();
      int i1 = blockposition.getZ();
      float f = (float)(i * i * j * 2);
      BlockPos blockposition1 = null;

      for(int j1 = k - i; j1 <= k + i; ++j1) {
         for(int k1 = l - j; k1 <= l + j; ++k1) {
            for(int l1 = i1 - i; l1 <= i1 + i; ++l1) {
               blockposition_mutableblockposition.setPos(j1, k1, l1);
               IBlockState iblockdata = world.getBlockState(blockposition_mutableblockposition);
               Block block = iblockdata.getBlock();
               if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                  float f1 = (float)((j1 - k) * (j1 - k) + (k1 - l) * (k1 - l) + (l1 - i1) * (l1 - i1));
                  if (f1 < f) {
                     f = f1;
                     blockposition1 = new BlockPos(blockposition_mutableblockposition);
                  }
               }
            }
         }
      }

      return blockposition1;
   }
}
