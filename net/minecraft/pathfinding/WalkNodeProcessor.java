package net.minecraft.pathfinding;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class WalkNodeProcessor extends NodeProcessor {
   private float avoidsWater;

   public void initProcessor(IBlockAccess var1, EntityLiving var2) {
      super.initProcessor(var1, var2);
      this.avoidsWater = var2.getPathPriority(PathNodeType.WATER);
   }

   public void postProcess() {
      this.entity.setPathPriority(PathNodeType.WATER, this.avoidsWater);
      super.postProcess();
   }

   public PathPoint getStart() {
      int var1;
      if (this.getCanSwim() && this.entity.isInWater()) {
         var1 = (int)this.entity.getEntityBoundingBox().minY;
         BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos(MathHelper.floor(this.entity.posX), var1, MathHelper.floor(this.entity.posZ));

         for(Block var3 = this.blockaccess.getBlockState(var8).getBlock(); var3 == Blocks.FLOWING_WATER || var3 == Blocks.WATER; var3 = this.blockaccess.getBlockState(var8).getBlock()) {
            ++var1;
            var8.setPos(MathHelper.floor(this.entity.posX), var1, MathHelper.floor(this.entity.posZ));
         }
      } else if (this.entity.onGround) {
         var1 = MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D);
      } else {
         BlockPos var2;
         for(var2 = new BlockPos(this.entity); (this.blockaccess.getBlockState(var2).getMaterial() == Material.AIR || this.blockaccess.getBlockState(var2).getBlock().isPassable(this.blockaccess, var2)) && var2.getY() > 0; var2 = var2.down()) {
            ;
         }

         var1 = var2.up().getY();
      }

      BlockPos var9 = new BlockPos(this.entity);
      PathNodeType var10 = this.getPathNodeType(this.entity, var9.getX(), var1, var9.getZ());
      if (this.entity.getPathPriority(var10) < 0.0F) {
         HashSet var4 = Sets.newHashSet();
         var4.add(new BlockPos(this.entity.getEntityBoundingBox().minX, (double)var1, this.entity.getEntityBoundingBox().minZ));
         var4.add(new BlockPos(this.entity.getEntityBoundingBox().minX, (double)var1, this.entity.getEntityBoundingBox().maxZ));
         var4.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, (double)var1, this.entity.getEntityBoundingBox().minZ));
         var4.add(new BlockPos(this.entity.getEntityBoundingBox().maxX, (double)var1, this.entity.getEntityBoundingBox().maxZ));

         for(BlockPos var6 : var4) {
            PathNodeType var7 = this.getPathNodeType(this.entity, var6);
            if (this.entity.getPathPriority(var7) >= 0.0F) {
               return this.openPoint(var6.getX(), var6.getY(), var6.getZ());
            }
         }
      }

      return this.openPoint(var9.getX(), var1, var9.getZ());
   }

   public PathPoint getPathPointToCoords(double var1, double var3, double var5) {
      return this.openPoint(MathHelper.floor(var1 - (double)(this.entity.width / 2.0F)), MathHelper.floor(var3), MathHelper.floor(var5 - (double)(this.entity.width / 2.0F)));
   }

   public int findPathOptions(PathPoint[] var1, PathPoint var2, PathPoint var3, float var4) {
      int var5 = 0;
      int var6 = 0;
      PathNodeType var7 = this.getPathNodeType(this.entity, var2.xCoord, var2.yCoord + 1, var2.zCoord);
      if (this.entity.getPathPriority(var7) >= 0.0F) {
         var6 = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
      }

      BlockPos var8 = (new BlockPos(var2.xCoord, var2.yCoord, var2.zCoord)).down();
      double var9 = (double)var2.yCoord - (1.0D - this.blockaccess.getBlockState(var8).getBoundingBox(this.blockaccess, var8).maxY);
      PathPoint var11 = this.getSafePoint(var2.xCoord, var2.yCoord, var2.zCoord + 1, var6, var9, EnumFacing.SOUTH);
      PathPoint var12 = this.getSafePoint(var2.xCoord - 1, var2.yCoord, var2.zCoord, var6, var9, EnumFacing.WEST);
      PathPoint var13 = this.getSafePoint(var2.xCoord + 1, var2.yCoord, var2.zCoord, var6, var9, EnumFacing.EAST);
      PathPoint var14 = this.getSafePoint(var2.xCoord, var2.yCoord, var2.zCoord - 1, var6, var9, EnumFacing.NORTH);
      if (var11 != null && !var11.visited && var11.distanceTo(var3) < var4) {
         var1[var5++] = var11;
      }

      if (var12 != null && !var12.visited && var12.distanceTo(var3) < var4) {
         var1[var5++] = var12;
      }

      if (var13 != null && !var13.visited && var13.distanceTo(var3) < var4) {
         var1[var5++] = var13;
      }

      if (var14 != null && !var14.visited && var14.distanceTo(var3) < var4) {
         var1[var5++] = var14;
      }

      boolean var15 = var14 == null || var14.nodeType == PathNodeType.OPEN || var14.costMalus != 0.0F;
      boolean var16 = var11 == null || var11.nodeType == PathNodeType.OPEN || var11.costMalus != 0.0F;
      boolean var17 = var13 == null || var13.nodeType == PathNodeType.OPEN || var13.costMalus != 0.0F;
      boolean var18 = var12 == null || var12.nodeType == PathNodeType.OPEN || var12.costMalus != 0.0F;
      if (var15 && var18) {
         PathPoint var19 = this.getSafePoint(var2.xCoord - 1, var2.yCoord, var2.zCoord - 1, var6, var9, EnumFacing.NORTH);
         if (var19 != null && !var19.visited && var19.distanceTo(var3) < var4) {
            var1[var5++] = var19;
         }
      }

      if (var15 && var17) {
         PathPoint var20 = this.getSafePoint(var2.xCoord + 1, var2.yCoord, var2.zCoord - 1, var6, var9, EnumFacing.NORTH);
         if (var20 != null && !var20.visited && var20.distanceTo(var3) < var4) {
            var1[var5++] = var20;
         }
      }

      if (var16 && var18) {
         PathPoint var21 = this.getSafePoint(var2.xCoord - 1, var2.yCoord, var2.zCoord + 1, var6, var9, EnumFacing.SOUTH);
         if (var21 != null && !var21.visited && var21.distanceTo(var3) < var4) {
            var1[var5++] = var21;
         }
      }

      if (var16 && var17) {
         PathPoint var22 = this.getSafePoint(var2.xCoord + 1, var2.yCoord, var2.zCoord + 1, var6, var9, EnumFacing.SOUTH);
         if (var22 != null && !var22.visited && var22.distanceTo(var3) < var4) {
            var1[var5++] = var22;
         }
      }

      return var5;
   }

   @Nullable
   private PathPoint getSafePoint(int var1, int var2, int var3, int var4, double var5, EnumFacing var7) {
      PathPoint var8 = null;
      BlockPos var9 = new BlockPos(var1, var2, var3);
      BlockPos var10 = var9.down();
      double var11 = (double)var2 - (1.0D - this.blockaccess.getBlockState(var10).getBoundingBox(this.blockaccess, var10).maxY);
      if (var11 - var5 > 1.125D) {
         return null;
      } else {
         PathNodeType var13 = this.getPathNodeType(this.entity, var1, var2, var3);
         float var14 = this.entity.getPathPriority(var13);
         double var15 = (double)this.entity.width / 2.0D;
         if (var14 >= 0.0F) {
            var8 = this.openPoint(var1, var2, var3);
            var8.nodeType = var13;
            var8.costMalus = Math.max(var8.costMalus, var14);
         }

         if (var13 == PathNodeType.WALKABLE) {
            return var8;
         } else {
            if (var8 == null && var4 > 0 && var13 != PathNodeType.FENCE && var13 != PathNodeType.TRAPDOOR) {
               var8 = this.getSafePoint(var1, var2 + 1, var3, var4 - 1, var5, var7);
               if (var8 != null && (var8.nodeType == PathNodeType.OPEN || var8.nodeType == PathNodeType.WALKABLE) && this.entity.width < 1.0F) {
                  double var17 = (double)(var1 - var7.getFrontOffsetX()) + 0.5D;
                  double var19 = (double)(var3 - var7.getFrontOffsetZ()) + 0.5D;
                  AxisAlignedBB var21 = new AxisAlignedBB(var17 - var15, (double)var2 + 0.001D, var19 - var15, var17 + var15, (double)((float)var2 + this.entity.height), var19 + var15);
                  AxisAlignedBB var22 = this.blockaccess.getBlockState(var9).getBoundingBox(this.blockaccess, var9);
                  AxisAlignedBB var23 = var21.addCoord(0.0D, var22.maxY - 0.002D, 0.0D);
                  if (this.entity.world.collidesWithAnyBlock(var23)) {
                     var8 = null;
                  }
               }
            }

            if (var13 == PathNodeType.OPEN) {
               AxisAlignedBB var24 = new AxisAlignedBB((double)var1 - var15 + 0.5D, (double)var2 + 0.001D, (double)var3 - var15 + 0.5D, (double)var1 + var15 + 0.5D, (double)((float)var2 + this.entity.height), (double)var3 + var15 + 0.5D);
               if (this.entity.world.collidesWithAnyBlock(var24)) {
                  return null;
               }

               if (this.entity.width >= 1.0F) {
                  PathNodeType var25 = this.getPathNodeType(this.entity, var1, var2 - 1, var3);
                  if (var25 == PathNodeType.BLOCKED) {
                     var8 = this.openPoint(var1, var2, var3);
                     var8.nodeType = PathNodeType.WALKABLE;
                     var8.costMalus = Math.max(var8.costMalus, var14);
                     return var8;
                  }
               }

               int var28 = 0;

               while(var2 > 0 && var13 == PathNodeType.OPEN) {
                  --var2;
                  if (var28++ >= this.entity.getMaxFallHeight()) {
                     return null;
                  }

                  var13 = this.getPathNodeType(this.entity, var1, var2, var3);
                  var14 = this.entity.getPathPriority(var13);
                  if (var13 != PathNodeType.OPEN && var14 >= 0.0F) {
                     var8 = this.openPoint(var1, var2, var3);
                     var8.nodeType = var13;
                     var8.costMalus = Math.max(var8.costMalus, var14);
                     break;
                  }

                  if (var14 < 0.0F) {
                     return null;
                  }
               }
            }

            return var8;
         }
      }
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4, EntityLiving var5, int var6, int var7, int var8, boolean var9, boolean var10) {
      EnumSet var11 = EnumSet.noneOf(PathNodeType.class);
      PathNodeType var12 = PathNodeType.BLOCKED;
      double var13 = (double)var5.width / 2.0D;
      BlockPos var15 = new BlockPos(var5);

      for(int var16 = 0; var16 < var6; ++var16) {
         for(int var17 = 0; var17 < var7; ++var17) {
            for(int var18 = 0; var18 < var8; ++var18) {
               int var19 = var16 + var2;
               int var20 = var17 + var3;
               int var21 = var18 + var4;
               PathNodeType var22 = this.getPathNodeType(var1, var19, var20, var21);
               if (var22 == PathNodeType.DOOR_WOOD_CLOSED && var9 && var10) {
                  var22 = PathNodeType.WALKABLE;
               }

               if (var22 == PathNodeType.DOOR_OPEN && !var10) {
                  var22 = PathNodeType.BLOCKED;
               }

               if (var22 == PathNodeType.RAIL && !(var1.getBlockState(var15).getBlock() instanceof BlockRailBase) && !(var1.getBlockState(var15.down()).getBlock() instanceof BlockRailBase)) {
                  var22 = PathNodeType.FENCE;
               }

               if (var16 == 0 && var17 == 0 && var18 == 0) {
                  var12 = var22;
               }

               var11.add(var22);
            }
         }
      }

      if (var11.contains(PathNodeType.FENCE)) {
         return PathNodeType.FENCE;
      } else {
         PathNodeType var23 = PathNodeType.BLOCKED;

         for(PathNodeType var25 : var11) {
            if (var5.getPathPriority(var25) < 0.0F) {
               return var25;
            }

            if (var5.getPathPriority(var25) >= var5.getPathPriority(var23)) {
               var23 = var25;
            }
         }

         if (var12 == PathNodeType.OPEN && var5.getPathPriority(var23) == 0.0F) {
            return PathNodeType.OPEN;
         } else {
            return var23;
         }
      }
   }

   private PathNodeType getPathNodeType(EntityLiving var1, BlockPos var2) {
      return this.getPathNodeType(var1, var2.getX(), var2.getY(), var2.getZ());
   }

   private PathNodeType getPathNodeType(EntityLiving var1, int var2, int var3, int var4) {
      return this.getPathNodeType(this.blockaccess, var2, var3, var4, var1, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.getCanBreakDoors(), this.getCanEnterDoors());
   }

   public PathNodeType getPathNodeType(IBlockAccess var1, int var2, int var3, int var4) {
      PathNodeType var5 = this.getPathNodeTypeRaw(var1, var2, var3, var4);
      if (var5 == PathNodeType.OPEN && var3 >= 1) {
         Block var6 = var1.getBlockState(new BlockPos(var2, var3 - 1, var4)).getBlock();
         PathNodeType var7 = this.getPathNodeTypeRaw(var1, var2, var3 - 1, var4);
         var5 = var7 != PathNodeType.WALKABLE && var7 != PathNodeType.OPEN && var7 != PathNodeType.WATER && var7 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         if (var7 == PathNodeType.DAMAGE_FIRE || var6 == Blocks.MAGMA) {
            var5 = PathNodeType.DAMAGE_FIRE;
         }

         if (var7 == PathNodeType.DAMAGE_CACTUS) {
            var5 = PathNodeType.DAMAGE_CACTUS;
         }
      }

      BlockPos.PooledMutableBlockPos var10 = BlockPos.PooledMutableBlockPos.retain();
      if (var5 == PathNodeType.WALKABLE) {
         for(int var11 = -1; var11 <= 1; ++var11) {
            for(int var8 = -1; var8 <= 1; ++var8) {
               if (var11 != 0 || var8 != 0) {
                  Block var9 = var1.getBlockState(var10.setPos(var11 + var2, var3, var8 + var4)).getBlock();
                  if (var9 == Blocks.CACTUS) {
                     var5 = PathNodeType.DANGER_CACTUS;
                  } else if (var9 == Blocks.FIRE) {
                     var5 = PathNodeType.DANGER_FIRE;
                  }
               }
            }
         }
      }

      var10.release();
      return var5;
   }

   private PathNodeType getPathNodeTypeRaw(IBlockAccess var1, int var2, int var3, int var4) {
      BlockPos var5 = new BlockPos(var2, var3, var4);
      IBlockState var6 = var1.getBlockState(var5);
      Block var7 = var6.getBlock();
      Material var8 = var6.getMaterial();
      if (var8 == Material.AIR) {
         return PathNodeType.OPEN;
      } else if (var7 != Blocks.TRAPDOOR && var7 != Blocks.IRON_TRAPDOOR && var7 != Blocks.WATERLILY) {
         if (var7 == Blocks.FIRE) {
            return PathNodeType.DAMAGE_FIRE;
         } else if (var7 == Blocks.CACTUS) {
            return PathNodeType.DAMAGE_CACTUS;
         } else if (var7 instanceof BlockDoor && var8 == Material.WOOD && !((Boolean)var6.getValue(BlockDoor.OPEN)).booleanValue()) {
            return PathNodeType.DOOR_WOOD_CLOSED;
         } else if (var7 instanceof BlockDoor && var8 == Material.IRON && !((Boolean)var6.getValue(BlockDoor.OPEN)).booleanValue()) {
            return PathNodeType.DOOR_IRON_CLOSED;
         } else if (var7 instanceof BlockDoor && ((Boolean)var6.getValue(BlockDoor.OPEN)).booleanValue()) {
            return PathNodeType.DOOR_OPEN;
         } else if (var7 instanceof BlockRailBase) {
            return PathNodeType.RAIL;
         } else if (!(var7 instanceof BlockFence) && !(var7 instanceof BlockWall) && (!(var7 instanceof BlockFenceGate) || ((Boolean)var6.getValue(BlockFenceGate.OPEN)).booleanValue())) {
            if (var8 == Material.WATER) {
               return PathNodeType.WATER;
            } else if (var8 == Material.LAVA) {
               return PathNodeType.LAVA;
            } else {
               return var7.isPassable(var1, var5) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
            }
         } else {
            return PathNodeType.FENCE;
         }
      } else {
         return PathNodeType.TRAPDOOR;
      }
   }
}
