package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseStrafePlayer extends PhaseBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private int fireballCharge;
   private Path currentPath;
   private Vec3d targetLocation;
   private EntityLivingBase attackTarget;
   private boolean holdingPatternClockwise;

   public PhaseStrafePlayer(EntityDragon var1) {
      super(var1);
   }

   public void doLocalUpdate() {
      if (this.attackTarget == null) {
         LOGGER.warn("Skipping player strafe phase because no player was found");
         this.dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
      } else {
         if (this.currentPath != null && this.currentPath.isFinished()) {
            double var1 = this.attackTarget.posX;
            double var3 = this.attackTarget.posZ;
            double var5 = var1 - this.dragon.posX;
            double var7 = var3 - this.dragon.posZ;
            double var9 = (double)MathHelper.sqrt(var5 * var5 + var7 * var7);
            double var11 = Math.min(0.4000000059604645D + var9 / 80.0D - 1.0D, 10.0D);
            this.targetLocation = new Vec3d(var1, this.attackTarget.posY + var11, var3);
         }

         double var31 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
         if (var31 < 100.0D || var31 > 22500.0D) {
            this.findNewTarget();
         }

         double var32 = 64.0D;
         if (this.attackTarget.getDistanceSqToEntity(this.dragon) < 4096.0D) {
            if (this.dragon.canEntityBeSeen(this.attackTarget)) {
               ++this.fireballCharge;
               Vec3d var13 = (new Vec3d(this.attackTarget.posX - this.dragon.posX, 0.0D, this.attackTarget.posZ - this.dragon.posZ)).normalize();
               Vec3d var14 = (new Vec3d((double)MathHelper.sin(this.dragon.rotationYaw * 0.017453292F), 0.0D, (double)(-MathHelper.cos(this.dragon.rotationYaw * 0.017453292F)))).normalize();
               float var15 = (float)var14.dotProduct(var13);
               float var16 = (float)(Math.acos((double)var15) * 57.2957763671875D);
               var16 = var16 + 0.5F;
               if (this.fireballCharge >= 5 && var16 >= 0.0F && var16 < 10.0F) {
                  double var33 = 1.0D;
                  Vec3d var17 = this.dragon.getLook(1.0F);
                  double var18 = this.dragon.dragonPartHead.posX - var17.xCoord * 1.0D;
                  double var20 = this.dragon.dragonPartHead.posY + (double)(this.dragon.dragonPartHead.height / 2.0F) + 0.5D;
                  double var22 = this.dragon.dragonPartHead.posZ - var17.zCoord * 1.0D;
                  double var24 = this.attackTarget.posX - var18;
                  double var26 = this.attackTarget.posY + (double)(this.attackTarget.height / 2.0F) - (var20 + (double)(this.dragon.dragonPartHead.height / 2.0F));
                  double var28 = this.attackTarget.posZ - var22;
                  this.dragon.world.playEvent((EntityPlayer)null, 1017, new BlockPos(this.dragon), 0);
                  EntityDragonFireball var30 = new EntityDragonFireball(this.dragon.world, this.dragon, var24, var26, var28);
                  var30.posX = var18;
                  var30.posY = var20;
                  var30.posZ = var22;
                  this.dragon.world.spawnEntity(var30);
                  this.fireballCharge = 0;
                  if (this.currentPath != null) {
                     while(!this.currentPath.isFinished()) {
                        this.currentPath.incrementPathIndex();
                     }
                  }

                  this.dragon.getPhaseManager().setPhase(PhaseList.HOLDING_PATTERN);
               }
            } else if (this.fireballCharge > 0) {
               --this.fireballCharge;
            }
         } else if (this.fireballCharge > 0) {
            --this.fireballCharge;
         }

      }
   }

   private void findNewTarget() {
      if (this.currentPath == null || this.currentPath.isFinished()) {
         int var1 = this.dragon.initPathPoints();
         int var2 = var1;
         if (this.dragon.getRNG().nextInt(8) == 0) {
            this.holdingPatternClockwise = !this.holdingPatternClockwise;
            var2 = var1 + 6;
         }

         if (this.holdingPatternClockwise) {
            ++var2;
         } else {
            --var2;
         }

         if (this.dragon.getFightManager() != null && this.dragon.getFightManager().getNumAliveCrystals() >= 0) {
            var2 = var2 % 12;
            if (var2 < 0) {
               var2 += 12;
            }
         } else {
            var2 = var2 - 12;
            var2 = var2 & 7;
            var2 = var2 + 12;
         }

         this.currentPath = this.dragon.findPath(var1, var2, (PathPoint)null);
         if (this.currentPath != null) {
            this.currentPath.incrementPathIndex();
         }
      }

      this.navigateToNextPathNode();
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

   public void initPhase() {
      this.fireballCharge = 0;
      this.targetLocation = null;
      this.currentPath = null;
      this.attackTarget = null;
   }

   public void setTarget(EntityLivingBase var1) {
      this.attackTarget = var1;
      int var2 = this.dragon.initPathPoints();
      int var3 = this.dragon.getNearestPpIdx(this.attackTarget.posX, this.attackTarget.posY, this.attackTarget.posZ);
      int var4 = MathHelper.floor(this.attackTarget.posX);
      int var5 = MathHelper.floor(this.attackTarget.posZ);
      double var6 = (double)var4 - this.dragon.posX;
      double var8 = (double)var5 - this.dragon.posZ;
      double var10 = (double)MathHelper.sqrt(var6 * var6 + var8 * var8);
      double var12 = Math.min(0.4000000059604645D + var10 / 80.0D - 1.0D, 10.0D);
      int var14 = MathHelper.floor(this.attackTarget.posY + var12);
      PathPoint var15 = new PathPoint(var4, var14, var5);
      this.currentPath = this.dragon.findPath(var2, var3, var15);
      if (this.currentPath != null) {
         this.currentPath.incrementPathIndex();
         this.navigateToNextPathNode();
      }

   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   public PhaseList getPhaseList() {
      return PhaseList.STRAFE_PLAYER;
   }
}
