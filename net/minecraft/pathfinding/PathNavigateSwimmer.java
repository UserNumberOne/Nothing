package net.minecraft.pathfinding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateSwimmer extends PathNavigate {
   public PathNavigateSwimmer(EntityLiving var1, World var2) {
      super(var1, var2);
   }

   protected PathFinder getPathFinder() {
      return new PathFinder(new SwimNodeProcessor());
   }

   protected boolean canNavigate() {
      return this.isInLiquid();
   }

   protected Vec3d getEntityPosition() {
      return new Vec3d(this.theEntity.posX, this.theEntity.posY + (double)this.theEntity.height * 0.5D, this.theEntity.posZ);
   }

   protected void pathFollow() {
      Vec3d var1 = this.getEntityPosition();
      float var2 = this.theEntity.width * this.theEntity.width;
      boolean var3 = true;
      if (var1.squareDistanceTo(this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex())) < (double)var2) {
         this.currentPath.incrementPathIndex();
      }

      for(int var4 = Math.min(this.currentPath.getCurrentPathIndex() + 6, this.currentPath.getCurrentPathLength() - 1); var4 > this.currentPath.getCurrentPathIndex(); --var4) {
         Vec3d var5 = this.currentPath.getVectorFromIndex(this.theEntity, var4);
         if (var5.squareDistanceTo(var1) <= 36.0D && this.isDirectPathBetweenPoints(var1, var5, 0, 0, 0)) {
            this.currentPath.setCurrentPathIndex(var4);
            break;
         }
      }

      this.checkForStuck(var1);
   }

   protected void removeSunnyPath() {
      super.removeSunnyPath();
   }

   protected boolean isDirectPathBetweenPoints(Vec3d var1, Vec3d var2, int var3, int var4, int var5) {
      RayTraceResult var6 = this.world.rayTraceBlocks(var1, new Vec3d(var2.xCoord, var2.yCoord + (double)this.theEntity.height * 0.5D, var2.zCoord), false, true, false);
      return var6 == null || var6.typeOfHit == RayTraceResult.Type.MISS;
   }

   public boolean canEntityStandOnPos(BlockPos var1) {
      return !this.world.getBlockState(var1).isFullBlock();
   }
}
