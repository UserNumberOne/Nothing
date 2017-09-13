package net.minecraft.pathfinding;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class PathFinder {
   private final PathHeap path = new PathHeap();
   private final Set closedSet = Sets.newHashSet();
   private final PathPoint[] pathOptions = new PathPoint[32];
   private final NodeProcessor nodeProcessor;

   public PathFinder(NodeProcessor var1) {
      this.nodeProcessor = var1;
   }

   @Nullable
   public Path findPath(IBlockAccess var1, EntityLiving var2, Entity var3, float var4) {
      return this.findPath(var1, var2, var3.posX, var3.getEntityBoundingBox().minY, var3.posZ, var4);
   }

   @Nullable
   public Path findPath(IBlockAccess var1, EntityLiving var2, BlockPos var3, float var4) {
      return this.findPath(var1, var2, (double)((float)var3.getX() + 0.5F), (double)((float)var3.getY() + 0.5F), (double)((float)var3.getZ() + 0.5F), var4);
   }

   @Nullable
   private Path findPath(IBlockAccess var1, EntityLiving var2, double var3, double var5, double var7, float var9) {
      this.path.clearPath();
      this.nodeProcessor.initProcessor(var1, var2);
      PathPoint var10 = this.nodeProcessor.getStart();
      PathPoint var11 = this.nodeProcessor.getPathPointToCoords(var3, var5, var7);
      Path var12 = this.findPath(var10, var11, var9);
      this.nodeProcessor.postProcess();
      return var12;
   }

   @Nullable
   private Path findPath(PathPoint var1, PathPoint var2, float var3) {
      var1.totalPathDistance = 0.0F;
      var1.distanceToNext = var1.distanceManhattan(var2);
      var1.distanceToTarget = var1.distanceToNext;
      this.path.clearPath();
      this.closedSet.clear();
      this.path.addPoint(var1);
      PathPoint var4 = var1;
      int var5 = 0;

      while(!this.path.isPathEmpty()) {
         ++var5;
         if (var5 >= 200) {
            break;
         }

         PathPoint var6 = this.path.dequeue();
         if (var6.equals(var2)) {
            var4 = var2;
            break;
         }

         if (var6.distanceManhattan(var2) < var4.distanceManhattan(var2)) {
            var4 = var6;
         }

         var6.visited = true;
         int var7 = this.nodeProcessor.findPathOptions(this.pathOptions, var6, var2, var3);

         for(int var8 = 0; var8 < var7; ++var8) {
            PathPoint var9 = this.pathOptions[var8];
            float var10 = var6.distanceManhattan(var9);
            var9.distanceFromOrigin = var6.distanceFromOrigin + var10;
            var9.cost = var10 + var9.costMalus;
            float var11 = var6.totalPathDistance + var9.cost;
            if (var9.distanceFromOrigin < var3 && (!var9.isAssigned() || var11 < var9.totalPathDistance)) {
               var9.previous = var6;
               var9.totalPathDistance = var11;
               var9.distanceToNext = var9.distanceManhattan(var2) + var9.costMalus;
               if (var9.isAssigned()) {
                  this.path.changeDistance(var9, var9.totalPathDistance + var9.distanceToNext);
               } else {
                  var9.distanceToTarget = var9.totalPathDistance + var9.distanceToNext;
                  this.path.addPoint(var9);
               }
            }
         }
      }

      if (var4 == var1) {
         return null;
      } else {
         Path var12 = this.createEntityPath(var1, var4);
         return var12;
      }
   }

   private Path createEntityPath(PathPoint var1, PathPoint var2) {
      int var3 = 1;

      for(PathPoint var4 = var2; var4.previous != null; var4 = var4.previous) {
         ++var3;
      }

      PathPoint[] var5 = new PathPoint[var3];
      PathPoint var7 = var2;
      --var3;

      for(var5[var3] = var2; var7.previous != null; var5[var3] = var7) {
         var7 = var7.previous;
         --var3;
      }

      return new Path(var5);
   }
}
