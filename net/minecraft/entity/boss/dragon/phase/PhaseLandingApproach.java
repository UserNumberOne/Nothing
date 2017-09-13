package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLandingApproach extends PhaseBase {
   private Path currentPath;
   private Vec3d targetLocation;

   public PhaseLandingApproach(EntityDragon var1) {
      super(var1);
   }

   public PhaseList getPhaseList() {
      return PhaseList.LANDING_APPROACH;
   }

   public void initPhase() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   public void doLocalUpdate() {
      double var1 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
      if (var1 < 100.0D || var1 > 22500.0D || this.dragon.isCollidedHorizontally || this.dragon.isCollidedVertically) {
         this.findNewTarget();
      }

   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget() {
      if (this.currentPath == null || this.currentPath.isFinished()) {
         int var1 = this.dragon.initPathPoints();
         BlockPos var2 = this.dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         EntityPlayer var3 = this.dragon.world.getNearestAttackablePlayer(var2, 128.0D, 128.0D);
         int var4;
         if (var3 != null) {
            Vec3d var5 = (new Vec3d(var3.posX, 0.0D, var3.posZ)).normalize();
            var4 = this.dragon.getNearestPpIdx(-var5.xCoord * 40.0D, 105.0D, -var5.zCoord * 40.0D);
         } else {
            var4 = this.dragon.getNearestPpIdx(40.0D, (double)var2.getY(), 0.0D);
         }

         PathPoint var6 = new PathPoint(var2.getX(), var2.getY(), var2.getZ());
         this.currentPath = this.dragon.findPath(var1, var4, var6);
         if (this.currentPath != null) {
            this.currentPath.incrementPathIndex();
         }
      }

      this.navigateToNextPathNode();
      if (this.currentPath != null && this.currentPath.isFinished()) {
         this.dragon.getPhaseManager().setPhase(PhaseList.LANDING);
      }

   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isFinished()) {
         Vec3d var1 = this.currentPath.getCurrentPos();
         this.currentPath.incrementPathIndex();
         double var2 = var1.xCoord;
         double var4 = var1.zCoord;

         double var6;
         while(true) {
            var6 = var1.yCoord + (double)(this.dragon.getRNG().nextFloat() * 20.0F);
            if (var6 >= var1.yCoord) {
               break;
            }
         }

         this.targetLocation = new Vec3d(var2, var6, var4);
      }

   }
}
