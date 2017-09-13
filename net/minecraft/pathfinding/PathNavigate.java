package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public abstract class PathNavigate {
   protected EntityLiving theEntity;
   protected World world;
   @Nullable
   protected Path currentPath;
   protected double speed;
   private final IAttributeInstance pathSearchRange;
   private int totalTicks;
   private int ticksAtLastPos;
   private Vec3d lastPosCheck = Vec3d.ZERO;
   private Vec3d timeoutCachedNode = Vec3d.ZERO;
   private long timeoutTimer;
   private long lastTimeoutCheck;
   private double timeoutLimit;
   private float maxDistanceToWaypoint = 0.5F;
   private boolean tryUpdatePath;
   private long lastTimeUpdated;
   protected NodeProcessor nodeProcessor;
   private BlockPos targetPos;
   private final PathFinder pathFinder;

   public PathNavigate(EntityLiving var1, World var2) {
      this.theEntity = var1;
      this.world = var2;
      this.pathSearchRange = var1.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      this.pathFinder = this.getPathFinder();
   }

   protected abstract PathFinder getPathFinder();

   public void setSpeed(double var1) {
      this.speed = var1;
   }

   public float getPathSearchRange() {
      return (float)this.pathSearchRange.getAttributeValue();
   }

   public boolean canUpdatePathOnTimeout() {
      return this.tryUpdatePath;
   }

   public void updatePath() {
      if (this.world.getTotalWorldTime() - this.lastTimeUpdated > 20L) {
         if (this.targetPos != null) {
            this.currentPath = null;
            this.currentPath = this.getPathToPos(this.targetPos);
            this.lastTimeUpdated = this.world.getTotalWorldTime();
            this.tryUpdatePath = false;
         }
      } else {
         this.tryUpdatePath = true;
      }

   }

   @Nullable
   public final Path getPathToXYZ(double var1, double var3, double var5) {
      return this.getPathToPos(new BlockPos(var1, var3, var5));
   }

   @Nullable
   public Path getPathToPos(BlockPos var1) {
      if (!this.canNavigate()) {
         return null;
      } else if (this.currentPath != null && !this.currentPath.isFinished() && var1.equals(this.targetPos)) {
         return this.currentPath;
      } else {
         this.targetPos = var1;
         float var2 = this.getPathSearchRange();
         this.world.theProfiler.startSection("pathfind");
         BlockPos var3 = new BlockPos(this.theEntity);
         int var4 = (int)(var2 + 8.0F);
         ChunkCache var5 = new ChunkCache(this.world, var3.add(-var4, -var4, -var4), var3.add(var4, var4, var4), 0);
         Path var6 = this.pathFinder.findPath(var5, this.theEntity, this.targetPos, var2);
         this.world.theProfiler.endSection();
         return var6;
      }
   }

   @Nullable
   public Path getPathToEntityLiving(Entity var1) {
      if (!this.canNavigate()) {
         return null;
      } else {
         BlockPos var2 = new BlockPos(var1);
         if (this.currentPath != null && !this.currentPath.isFinished() && var2.equals(this.targetPos)) {
            return this.currentPath;
         } else {
            this.targetPos = var2;
            float var3 = this.getPathSearchRange();
            this.world.theProfiler.startSection("pathfind");
            BlockPos var4 = (new BlockPos(this.theEntity)).up();
            int var5 = (int)(var3 + 16.0F);
            ChunkCache var6 = new ChunkCache(this.world, var4.add(-var5, -var5, -var5), var4.add(var5, var5, var5), 0);
            Path var7 = this.pathFinder.findPath(var6, this.theEntity, var1, var3);
            this.world.theProfiler.endSection();
            return var7;
         }
      }
   }

   public boolean tryMoveToXYZ(double var1, double var3, double var5, double var7) {
      return this.setPath(this.getPathToXYZ(var1, var3, var5), var7);
   }

   public boolean tryMoveToEntityLiving(Entity var1, double var2) {
      Path var4 = this.getPathToEntityLiving(var1);
      return var4 != null && this.setPath(var4, var2);
   }

   public boolean setPath(@Nullable Path var1, double var2) {
      if (var1 == null) {
         this.currentPath = null;
         return false;
      } else {
         if (!var1.isSamePath(this.currentPath)) {
            this.currentPath = var1;
         }

         this.removeSunnyPath();
         if (this.currentPath.getCurrentPathLength() == 0) {
            return false;
         } else {
            this.speed = var2;
            Vec3d var4 = this.getEntityPosition();
            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = var4;
            return true;
         }
      }
   }

   @Nullable
   public Path getPath() {
      return this.currentPath;
   }

   public void onUpdateNavigation() {
      ++this.totalTicks;
      if (this.tryUpdatePath) {
         this.updatePath();
      }

      if (!this.noPath()) {
         if (this.canNavigate()) {
            this.pathFollow();
         } else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
            Vec3d var1 = this.getEntityPosition();
            Vec3d var2 = this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex());
            if (var1.yCoord > var2.yCoord && !this.theEntity.onGround && MathHelper.floor(var1.xCoord) == MathHelper.floor(var2.xCoord) && MathHelper.floor(var1.zCoord) == MathHelper.floor(var2.zCoord)) {
               this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
            }
         }

         if (!this.noPath()) {
            Vec3d var4 = this.currentPath.getPosition(this.theEntity);
            if (var4 != null) {
               BlockPos var6 = (new BlockPos(var4)).down();
               AxisAlignedBB var3 = this.world.getBlockState(var6).getBoundingBox(this.world, var6);
               var4 = var4.subtract(0.0D, 1.0D - var3.maxY, 0.0D);
               this.theEntity.getMoveHelper().setMoveTo(var4.xCoord, var4.yCoord, var4.zCoord, this.speed);
            }
         }
      }
   }

   protected void pathFollow() {
      Vec3d var1 = this.getEntityPosition();
      int var2 = this.currentPath.getCurrentPathLength();

      for(int var3 = this.currentPath.getCurrentPathIndex(); var3 < this.currentPath.getCurrentPathLength(); ++var3) {
         if ((double)this.currentPath.getPathPointFromIndex(var3).yCoord != Math.floor(var1.yCoord)) {
            var2 = var3;
            break;
         }
      }

      this.maxDistanceToWaypoint = this.theEntity.width > 0.75F ? this.theEntity.width / 2.0F : 0.75F - this.theEntity.width / 2.0F;
      Vec3d var8 = this.currentPath.getCurrentPos();
      if (MathHelper.abs((float)(this.theEntity.posX - (var8.xCoord + 0.5D))) < this.maxDistanceToWaypoint && MathHelper.abs((float)(this.theEntity.posZ - (var8.zCoord + 0.5D))) < this.maxDistanceToWaypoint && Math.abs(this.theEntity.posY - var8.yCoord) < 1.0D) {
         this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
      }

      int var4 = MathHelper.ceil(this.theEntity.width);
      int var5 = MathHelper.ceil(this.theEntity.height);
      int var6 = var4;

      for(int var7 = var2 - 1; var7 >= this.currentPath.getCurrentPathIndex(); --var7) {
         if (this.isDirectPathBetweenPoints(var1, this.currentPath.getVectorFromIndex(this.theEntity, var7), var4, var5, var6)) {
            this.currentPath.setCurrentPathIndex(var7);
            break;
         }
      }

      this.checkForStuck(var1);
   }

   protected void checkForStuck(Vec3d var1) {
      if (this.totalTicks - this.ticksAtLastPos > 100) {
         if (var1.squareDistanceTo(this.lastPosCheck) < 2.25D) {
            this.clearPathEntity();
         }

         this.ticksAtLastPos = this.totalTicks;
         this.lastPosCheck = var1;
      }

      if (this.currentPath != null && !this.currentPath.isFinished()) {
         Vec3d var2 = this.currentPath.getCurrentPos();
         if (var2.equals(this.timeoutCachedNode)) {
            this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
         } else {
            this.timeoutCachedNode = var2;
            double var3 = var1.distanceTo(this.timeoutCachedNode);
            this.timeoutLimit = this.theEntity.getAIMoveSpeed() > 0.0F ? var3 / (double)this.theEntity.getAIMoveSpeed() * 1000.0D : 0.0D;
         }

         if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
            this.timeoutCachedNode = Vec3d.ZERO;
            this.timeoutTimer = 0L;
            this.timeoutLimit = 0.0D;
            this.clearPathEntity();
         }

         this.lastTimeoutCheck = System.currentTimeMillis();
      }

   }

   public boolean noPath() {
      return this.currentPath == null || this.currentPath.isFinished();
   }

   public void clearPathEntity() {
      this.currentPath = null;
   }

   protected abstract Vec3d getEntityPosition();

   protected abstract boolean canNavigate();

   protected boolean isInLiquid() {
      return this.theEntity.isInWater() || this.theEntity.isInLava();
   }

   protected void removeSunnyPath() {
   }

   protected abstract boolean isDirectPathBetweenPoints(Vec3d var1, Vec3d var2, int var3, int var4, int var5);

   public boolean canEntityStandOnPos(BlockPos var1) {
      return this.world.getBlockState(var1.down()).isFullBlock();
   }

   public NodeProcessor getNodeProcessor() {
      return this.nodeProcessor;
   }
}
