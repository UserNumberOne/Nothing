package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
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

   public EntityAIPanic(EntityCreature var1, double var2) {
      this.theEntityCreature = var1;
      this.speed = var2;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if (this.theEntityCreature.getAITarget() == null && !this.theEntityCreature.isBurning()) {
         return false;
      } else if (!this.theEntityCreature.isBurning()) {
         Vec3d var2 = RandomPositionGenerator.findRandomTarget(this.theEntityCreature, 5, 4);
         if (var2 == null) {
            return false;
         } else {
            this.randPosX = var2.xCoord;
            this.randPosY = var2.yCoord;
            this.randPosZ = var2.zCoord;
            return true;
         }
      } else {
         BlockPos var1 = this.getRandPos(this.theEntityCreature.world, this.theEntityCreature, 5, 4);
         if (var1 == null) {
            return false;
         } else {
            this.randPosX = (double)var1.getX();
            this.randPosY = (double)var1.getY();
            this.randPosZ = (double)var1.getZ();
            return true;
         }
      }
   }

   public void startExecuting() {
      this.theEntityCreature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
   }

   public boolean continueExecuting() {
      return !this.theEntityCreature.getNavigator().noPath();
   }

   private BlockPos getRandPos(World var1, Entity var2, int var3, int var4) {
      BlockPos var5 = new BlockPos(var2);
      BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();
      int var7 = var5.getX();
      int var8 = var5.getY();
      int var9 = var5.getZ();
      float var10 = (float)(var3 * var3 * var4 * 2);
      BlockPos var11 = null;

      for(int var12 = var7 - var3; var12 <= var7 + var3; ++var12) {
         for(int var13 = var8 - var4; var13 <= var8 + var4; ++var13) {
            for(int var14 = var9 - var3; var14 <= var9 + var3; ++var14) {
               var6.setPos(var12, var13, var14);
               IBlockState var15 = var1.getBlockState(var6);
               Block var16 = var15.getBlock();
               if (var16 == Blocks.WATER || var16 == Blocks.FLOWING_WATER) {
                  float var17 = (float)((var12 - var7) * (var12 - var7) + (var13 - var8) * (var13 - var8) + (var14 - var9) * (var14 - var9));
                  if (var17 < var10) {
                     var10 = var17;
                     var11 = new BlockPos(var6);
                  }
               }
            }
         }
      }

      return var11;
   }
}
