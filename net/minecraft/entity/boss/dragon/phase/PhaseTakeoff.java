package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseTakeoff extends PhaseBase {
   private boolean firstTick;
   private Path currentPath;
   private Vec3d targetLocation;

   public PhaseTakeoff(EntityDragon var1) {
      super(var1);
   }

   public void doLocalUpdate() {
      if (this.firstTick) {
         this.firstTick = false;
         this.findNewTarget();
      } else {
         BlockPos var1 = this.dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION);
         double var2 = this.dragon.getDistanceSqToCenter(var1);
         if (var2 > 100.0D) {
            this.dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
         }
      }

   }

   public void initPhase() {
      this.firstTick = true;
      this.currentPath = null;
      this.targetLocation = null;
   }

   private void findNewTarget() {
      int var1 = this.dragon.initPathPoints();
      Vec3d var2 = this.dragon.getHeadLookVec(1.0F);
      int var3 = this.dragon.getNearestPpIdx(-var2.xCoord * 40.0D, 105.0D, -var2.zCoord * 40.0D);
      if (this.dragon.getFightManager() != null && this.dragon.getFightManager().getNumAliveCrystals() >= 0) {
         var3 = var3 % 12;
         if (var3 < 0) {
            var3 += 12;
         }
      } else {
         var3 = var3 - 12;
         var3 = var3 & 7;
         var3 = var3 + 12;
      }

      this.currentPath = this.dragon.findPath(var1, var3, (PathPoint)null);
      if (this.currentPath != null) {
         this.currentPath.incrementPathIndex();
         this.navigateToNextPathNode();
      }

   }

   private void navigateToNextPathNode() {
      Vec3d var1 = this.currentPath.getCurrentPos();
      this.currentPath.incrementPathIndex();

      double var2;
      while(true) {
         var2 = var1.yCoord + (double)(this.dragon.getRNG().nextFloat() * 20.0F);
         if (var2 >= var1.yCoord) {
            break;
         }
      }

      this.targetLocation = new Vec3d(var1.xCoord, var2, var1.zCoord);
   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   public PhaseList getPhaseList() {
      return PhaseList.TAKEOFF;
   }
}
