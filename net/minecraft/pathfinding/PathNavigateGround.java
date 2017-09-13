package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateGround extends PathNavigate {
   private boolean shouldAvoidSun;

   public PathNavigateGround(EntityLiving var1, World var2) {
      super(var1, var2);
   }

   protected PathFinder getPathFinder() {
      this.nodeProcessor = new WalkNodeProcessor();
      this.nodeProcessor.setCanEnterDoors(true);
      return new PathFinder(this.nodeProcessor);
   }

   protected boolean canNavigate() {
      return this.theEntity.onGround || this.getCanSwim() && this.isInLiquid() || this.theEntity.isRiding();
   }

   protected Vec3d getEntityPosition() {
      return new Vec3d(this.theEntity.posX, (double)this.getPathablePosY(), this.theEntity.posZ);
   }

   public Path getPathToPos(BlockPos var1) {
      if (this.world.getBlockState(var1).getMaterial() == Material.AIR) {
         BlockPos var2;
         for(var2 = var1.down(); var2.getY() > 0 && this.world.getBlockState(var2).getMaterial() == Material.AIR; var2 = var2.down()) {
            ;
         }

         if (var2.getY() > 0) {
            return super.getPathToPos(var2.up());
         }

         while(var2.getY() < this.world.getHeight() && this.world.getBlockState(var2).getMaterial() == Material.AIR) {
            var2 = var2.up();
         }

         var1 = var2;
      }

      if (!this.world.getBlockState(var1).getMaterial().isSolid()) {
         return super.getPathToPos(var1);
      } else {
         BlockPos var3;
         for(var3 = var1.up(); var3.getY() < this.world.getHeight() && this.world.getBlockState(var3).getMaterial().isSolid(); var3 = var3.up()) {
            ;
         }

         return super.getPathToPos(var3);
      }
   }

   public Path getPathToEntityLiving(Entity var1) {
      BlockPos var2 = new BlockPos(var1);
      return this.getPathToPos(var2);
   }

   private int getPathablePosY() {
      if (this.theEntity.isInWater() && this.getCanSwim()) {
         int var1 = (int)this.theEntity.getEntityBoundingBox().minY;
         Block var2 = this.world.getBlockState(new BlockPos(MathHelper.floor(this.theEntity.posX), var1, MathHelper.floor(this.theEntity.posZ))).getBlock();
         int var3 = 0;

         while(var2 == Blocks.FLOWING_WATER || var2 == Blocks.WATER) {
            ++var1;
            var2 = this.world.getBlockState(new BlockPos(MathHelper.floor(this.theEntity.posX), var1, MathHelper.floor(this.theEntity.posZ))).getBlock();
            ++var3;
            if (var3 > 16) {
               return (int)this.theEntity.getEntityBoundingBox().minY;
            }
         }

         return var1;
      } else {
         return (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D);
      }
   }

   protected void removeSunnyPath() {
      super.removeSunnyPath();

      for(int var1 = 0; var1 < this.currentPath.getCurrentPathLength(); ++var1) {
         PathPoint var2 = this.currentPath.getPathPointFromIndex(var1);
         PathPoint var3 = var1 + 1 < this.currentPath.getCurrentPathLength() ? this.currentPath.getPathPointFromIndex(var1 + 1) : null;
         IBlockState var4 = this.world.getBlockState(new BlockPos(var2.xCoord, var2.yCoord, var2.zCoord));
         Block var5 = var4.getBlock();
         if (var5 == Blocks.CAULDRON) {
            this.currentPath.setPoint(var1, var2.cloneMove(var2.xCoord, var2.yCoord + 1, var2.zCoord));
            if (var3 != null && var2.yCoord >= var3.yCoord) {
               this.currentPath.setPoint(var1 + 1, var3.cloneMove(var3.xCoord, var2.yCoord + 1, var3.zCoord));
            }
         }
      }

      if (this.shouldAvoidSun) {
         if (this.world.canSeeSky(new BlockPos(MathHelper.floor(this.theEntity.posX), (int)(this.theEntity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor(this.theEntity.posZ)))) {
            return;
         }

         for(int var6 = 0; var6 < this.currentPath.getCurrentPathLength(); ++var6) {
            PathPoint var7 = this.currentPath.getPathPointFromIndex(var6);
            if (this.world.canSeeSky(new BlockPos(var7.xCoord, var7.yCoord, var7.zCoord))) {
               this.currentPath.setCurrentPathLength(var6 - 1);
               return;
            }
         }
      }

   }

   protected boolean isDirectPathBetweenPoints(Vec3d var1, Vec3d var2, int var3, int var4, int var5) {
      int var6 = MathHelper.floor(var1.xCoord);
      int var7 = MathHelper.floor(var1.zCoord);
      double var8 = var2.xCoord - var1.xCoord;
      double var10 = var2.zCoord - var1.zCoord;
      double var12 = var8 * var8 + var10 * var10;
      if (var12 < 1.0E-8D) {
         return false;
      } else {
         double var14 = 1.0D / Math.sqrt(var12);
         var8 = var8 * var14;
         var10 = var10 * var14;
         var3 = var3 + 2;
         var5 = var5 + 2;
         if (!this.isSafeToStandAt(var6, (int)var1.yCoord, var7, var3, var4, var5, var1, var8, var10)) {
            return false;
         } else {
            var3 = var3 - 2;
            var5 = var5 - 2;
            double var16 = 1.0D / Math.abs(var8);
            double var18 = 1.0D / Math.abs(var10);
            double var20 = (double)var6 - var1.xCoord;
            double var22 = (double)var7 - var1.zCoord;
            if (var8 >= 0.0D) {
               ++var20;
            }

            if (var10 >= 0.0D) {
               ++var22;
            }

            var20 = var20 / var8;
            var22 = var22 / var10;
            int var24 = var8 < 0.0D ? -1 : 1;
            int var25 = var10 < 0.0D ? -1 : 1;
            int var26 = MathHelper.floor(var2.xCoord);
            int var27 = MathHelper.floor(var2.zCoord);
            int var28 = var26 - var6;
            int var29 = var27 - var7;

            while(var28 * var24 > 0 || var29 * var25 > 0) {
               if (var20 < var22) {
                  var20 += var16;
                  var6 += var24;
                  var28 = var26 - var6;
               } else {
                  var22 += var18;
                  var7 += var25;
                  var29 = var27 - var7;
               }

               if (!this.isSafeToStandAt(var6, (int)var1.yCoord, var7, var3, var4, var5, var1, var8, var10)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private boolean isSafeToStandAt(int var1, int var2, int var3, int var4, int var5, int var6, Vec3d var7, double var8, double var10) {
      int var12 = var1 - var4 / 2;
      int var13 = var3 - var6 / 2;
      if (!this.isPositionClear(var12, var2, var13, var4, var5, var6, var7, var8, var10)) {
         return false;
      } else {
         for(int var14 = var12; var14 < var12 + var4; ++var14) {
            for(int var15 = var13; var15 < var13 + var6; ++var15) {
               double var16 = (double)var14 + 0.5D - var7.xCoord;
               double var18 = (double)var15 + 0.5D - var7.zCoord;
               if (var16 * var8 + var18 * var10 >= 0.0D) {
                  PathNodeType var20 = this.nodeProcessor.getPathNodeType(this.world, var14, var2 - 1, var15, this.theEntity, var4, var5, var6, true, true);
                  if (var20 == PathNodeType.WATER) {
                     return false;
                  }

                  if (var20 == PathNodeType.LAVA) {
                     return false;
                  }

                  if (var20 == PathNodeType.OPEN) {
                     return false;
                  }

                  var20 = this.nodeProcessor.getPathNodeType(this.world, var14, var2, var15, this.theEntity, var4, var5, var6, true, true);
                  float var21 = this.theEntity.getPathPriority(var20);
                  if (var21 < 0.0F || var21 >= 8.0F) {
                     return false;
                  }

                  if (var20 == PathNodeType.DAMAGE_FIRE || var20 == PathNodeType.DANGER_FIRE || var20 == PathNodeType.DAMAGE_OTHER) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private boolean isPositionClear(int var1, int var2, int var3, int var4, int var5, int var6, Vec3d var7, double var8, double var10) {
      for(BlockPos var13 : BlockPos.getAllInBox(new BlockPos(var1, var2, var3), new BlockPos(var1 + var4 - 1, var2 + var5 - 1, var3 + var6 - 1))) {
         double var14 = (double)var13.getX() + 0.5D - var7.xCoord;
         double var16 = (double)var13.getZ() + 0.5D - var7.zCoord;
         if (var14 * var8 + var16 * var10 >= 0.0D) {
            Block var18 = this.world.getBlockState(var13).getBlock();
            if (!var18.isPassable(this.world, var13)) {
               return false;
            }
         }
      }

      return true;
   }

   public void setBreakDoors(boolean var1) {
      this.nodeProcessor.setCanBreakDoors(var1);
   }

   public void setEnterDoors(boolean var1) {
      this.nodeProcessor.setCanEnterDoors(var1);
   }

   public boolean getEnterDoors() {
      return this.nodeProcessor.getCanEnterDoors();
   }

   public void setCanSwim(boolean var1) {
      this.nodeProcessor.setCanSwim(var1);
   }

   public boolean getCanSwim() {
      return this.nodeProcessor.getCanSwim();
   }

   public void setAvoidSun(boolean var1) {
      this.shouldAvoidSun = var1;
   }
}
