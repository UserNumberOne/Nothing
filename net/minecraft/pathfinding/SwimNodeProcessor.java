package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class SwimNodeProcessor extends NodeProcessor {
   public PathPoint getStart() {
      return this.openPoint(MathHelper.floor(this.entity.getEntityBoundingBox().minX), MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D), MathHelper.floor(this.entity.getEntityBoundingBox().minZ));
   }

   public PathPoint getPathPointToCoords(double var1, double var3, double var5) {
      return this.openPoint(MathHelper.floor(x - (double)(this.entity.width / 2.0F)), MathHelper.floor(y + 0.5D), MathHelper.floor(z - (double)(this.entity.width / 2.0F)));
   }

   public int findPathOptions(PathPoint[] var1, PathPoint var2, PathPoint var3, float var4) {
      int i = 0;

      for(EnumFacing enumfacing : EnumFacing.values()) {
         PathPoint pathpoint = this.getWaterNode(currentPoint.xCoord + enumfacing.getFrontOffsetX(), currentPoint.yCoord + enumfacing.getFrontOffsetY(), currentPoint.zCoord + enumfacing.getFrontOffsetZ());
         if (pathpoint != null && !pathpoint.visited && pathpoint.distanceTo(targetPoint) < maxDistance) {
            pathOptions[i++] = pathpoint;
         }
      }

      return i;
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4, EntityLiving var5, int var6, int var7, int var8, boolean var9, boolean var10) {
      return PathNodeType.WATER;
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4) {
      return PathNodeType.WATER;
   }

   @Nullable
   private PathPoint getWaterNode(int var1, int var2, int var3) {
      PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
      return pathnodetype == PathNodeType.WATER ? this.openPoint(p_186328_1_, p_186328_2_, p_186328_3_) : null;
   }

   private PathNodeType isFree(int var1, int var2, int var3) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = p_186327_1_; i < p_186327_1_ + this.entitySizeX; ++i) {
         for(int j = p_186327_2_; j < p_186327_2_ + this.entitySizeY; ++j) {
            for(int k = p_186327_3_; k < p_186327_3_ + this.entitySizeZ; ++k) {
               IBlockState iblockstate = this.blockaccess.getBlockState(blockpos$mutableblockpos.setPos(i, j, k));
               if (iblockstate.getMaterial() != Material.WATER) {
                  return PathNodeType.BLOCKED;
               }
            }
         }
      }

      return PathNodeType.WATER;
   }
}
