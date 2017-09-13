package net.minecraft.pathfinding;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class PathWorldListener implements IWorldEventListener {
   private final List navigations = Lists.newArrayList();

   public void notifyBlockUpdate(World var1, BlockPos var2, IBlockState var3, IBlockState var4, int var5) {
      if (this.didBlockChange(worldIn, pos, oldState, newState)) {
         int i = 0;

         for(int j = this.navigations.size(); i < j; ++i) {
            PathNavigate pathnavigate = (PathNavigate)this.navigations.get(i);
            if (pathnavigate != null && !pathnavigate.canUpdatePathOnTimeout()) {
               Path path = pathnavigate.getPath();
               if (path != null && !path.isFinished() && path.getCurrentPathLength() != 0) {
                  PathPoint pathpoint = pathnavigate.currentPath.getFinalPathPoint();
                  double d0 = pos.distanceSq(((double)pathpoint.xCoord + pathnavigate.theEntity.posX) / 2.0D, ((double)pathpoint.yCoord + pathnavigate.theEntity.posY) / 2.0D, ((double)pathpoint.zCoord + pathnavigate.theEntity.posZ) / 2.0D);
                  int k = (path.getCurrentPathLength() - path.getCurrentPathIndex()) * (path.getCurrentPathLength() - path.getCurrentPathIndex());
                  if (d0 < (double)k) {
                     pathnavigate.updatePath();
                  }
               }
            }
         }
      }

   }

   protected boolean didBlockChange(World var1, BlockPos var2, IBlockState var3, IBlockState var4) {
      AxisAlignedBB axisalignedbb = oldState.getCollisionBoundingBox(worldIn, pos);
      AxisAlignedBB axisalignedbb1 = newState.getCollisionBoundingBox(worldIn, pos);
      return axisalignedbb != axisalignedbb1 && (axisalignedbb == null || !axisalignedbb.equals(axisalignedbb1));
   }

   public void notifyLightSet(BlockPos var1) {
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer var1, SoundEvent var2, SoundCategory var3, double var4, double var6, double var8, float var10, float var11) {
   }

   public void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
   }

   public void onEntityAdded(Entity var1) {
      if (entityIn instanceof EntityLiving) {
         this.navigations.add(((EntityLiving)entityIn).getNavigator());
      }

   }

   public void onEntityRemoved(Entity var1) {
      if (entityIn instanceof EntityLiving) {
         this.navigations.remove(((EntityLiving)entityIn).getNavigator());
      }

   }

   public void playRecord(SoundEvent var1, BlockPos var2) {
   }

   public void broadcastSound(int var1, BlockPos var2, int var3) {
   }

   public void playEvent(EntityPlayer var1, int var2, BlockPos var3, int var4) {
   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
   }
}
