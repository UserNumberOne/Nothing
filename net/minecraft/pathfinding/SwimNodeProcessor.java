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
      return this.openPoint(MathHelper.floor(var1 - (double)(this.entity.width / 2.0F)), MathHelper.floor(var3 + 0.5D), MathHelper.floor(var5 - (double)(this.entity.width / 2.0F)));
   }

   public int findPathOptions(PathPoint[] var1, PathPoint var2, PathPoint var3, float var4) {
      int var5 = 0;

      for(EnumFacing var9 : EnumFacing.values()) {
         PathPoint var10 = this.getWaterNode(var2.xCoord + var9.getFrontOffsetX(), var2.yCoord + var9.getFrontOffsetY(), var2.zCoord + var9.getFrontOffsetZ());
         if (var10 != null && !var10.visited && var10.distanceTo(var3) < var4) {
            var1[var5++] = var10;
         }
      }

      return var5;
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4, EntityLiving var5, int var6, int var7, int var8, boolean var9, boolean var10) {
      return PathNodeType.WATER;
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4) {
      return PathNodeType.WATER;
   }

   @Nullable
   private PathPoint getWaterNode(int var1, int var2, int var3) {
      PathNodeType var4 = this.isFree(var1, var2, var3);
      return var4 == PathNodeType.WATER ? this.openPoint(var1, var2, var3) : null;
   }

   private PathNodeType isFree(int var1, int var2, int var3) {
      BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

      for(int var5 = var1; var5 < var1 + this.entitySizeX; ++var5) {
         for(int var6 = var2; var6 < var2 + this.entitySizeY; ++var6) {
            for(int var7 = var3; var7 < var3 + this.entitySizeZ; ++var7) {
               IBlockState var8 = this.blockaccess.getBlockState(var4.setPos(var5, var6, var7));
               if (var8.getMaterial() != Material.WATER) {
                  return PathNodeType.BLOCKED;
               }
            }
         }
      }

      return PathNodeType.WATER;
   }
}
