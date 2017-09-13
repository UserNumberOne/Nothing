package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseHoldingPattern extends PhaseBase {
   private Path currentPath;
   private Vec3d targetLocation;
   private boolean clockwise;

   public PhaseHoldingPattern(EntityDragon var1) {
      super(var1);
   }

   public PhaseList getPhaseList() {
      return PhaseList.HOLDING_PATTERN;
   }

   public void doLocalUpdate() {
      double var1 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
      if (var1 < 100.0D || var1 > 22500.0D || this.dragon.isCollidedHorizontally || this.dragon.isCollidedVertically) {
         this.findNewTarget();
      }

   }

   public void initPhase() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget() {
      if (this.currentPath != null && this.currentPath.isFinished()) {
         BlockPos var1 = this.dragon.world.getTopSolidOrLiquidBlock(new BlockPos(WorldGenEndPodium.END_PODIUM_LOCATION));
         int var2 = this.dragon.getFightManager() == null ? 0 : this.dragon.getFightManager().getNumAliveCrystals();
         if (this.dragon.getRNG().nextInt(var2 + 3) == 0) {
            this.dragon.getPhaseManager().setPhase(PhaseList.LANDING_APPROACH);
            return;
         }

         double var3 = 64.0D;
         EntityPlayer var5 = this.dragon.world.getNearestAttackablePlayer(var1, var3, var3);
         if (var5 != null) {
            var3 = var5.getDistanceSqToCenter(var1) / 512.0D;
         }

         if (var5 != null && (this.dragon.getRNG().nextInt(MathHelper.abs((int)var3) + 2) == 0 || this.dragon.getRNG().nextInt(var2 + 2) == 0)) {
            this.strafePlayer(var5);
            return;
         }
      }

      if (this.currentPath == null || this.currentPath.isFinished()) {
         int var6 = this.dragon.initPathPoints();
         int var7 = var6;
         if (this.dragon.getRNG().nextInt(8) == 0) {
            this.clockwise = !this.clockwise;
            var7 = var6 + 6;
         }

         if (this.clockwise) {
            ++var7;
         } else {
            --var7;
         }

         if (this.dragon.getFightManager() != null && this.dragon.getFightManager().getNumAliveCrystals() >= 0) {
            var7 = var7 % 12;
            if (var7 < 0) {
               var7 += 12;
            }
         } else {
            var7 = var7 - 12;
            var7 = var7 & 7;
            var7 = var7 + 12;
         }

         this.currentPath = this.dragon.findPath(var6, var7, (PathPoint)null);
         if (this.currentPath != null) {
            this.currentPath.incrementPathIndex();
         }
      }

      this.navigateToNextPathNode();
   }

   private void strafePlayer(EntityPlayer var1) {
      this.dragon.getPhaseManager().setPhase(PhaseList.STRAFE_PLAYER);
      ((PhaseStrafePlayer)this.dragon.getPhaseManager().getPhase(PhaseList.STRAFE_PLAYER)).setTarget(var1);
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

   public void onCrystalDestroyed(EntityEnderCrystal var1, BlockPos var2, DamageSource var3, @Nullable EntityPlayer var4) {
      if (var4 != null) {
         this.strafePlayer(var4);
      }

   }
}
