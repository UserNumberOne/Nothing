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
      if (this.didBlockChange(var1, var2, var3, var4)) {
         int var6 = 0;

         for(int var7 = this.navigations.size(); var6 < var7; ++var6) {
            PathNavigate var8 = (PathNavigate)this.navigations.get(var6);
            if (var8 != null && !var8.canUpdatePathOnTimeout()) {
               Path var9 = var8.getPath();
               if (var9 != null && !var9.isFinished() && var9.getCurrentPathLength() != 0) {
                  PathPoint var10 = var8.currentPath.getFinalPathPoint();
                  double var11 = var2.distanceSq(((double)var10.xCoord + var8.theEntity.posX) / 2.0D, ((double)var10.yCoord + var8.theEntity.posY) / 2.0D, ((double)var10.zCoord + var8.theEntity.posZ) / 2.0D);
                  int var13 = (var9.getCurrentPathLength() - var9.getCurrentPathIndex()) * (var9.getCurrentPathLength() - var9.getCurrentPathIndex());
                  if (var11 < (double)var13) {
                     var8.updatePath();
                  }
               }
            }
         }
      }

   }

   protected boolean didBlockChange(World var1, BlockPos var2, IBlockState var3, IBlockState var4) {
      AxisAlignedBB var5 = var3.getCollisionBoundingBox(var1, var2);
      AxisAlignedBB var6 = var4.getCollisionBoundingBox(var1, var2);
      return var5 != var6 && (var5 == null || !var5.equals(var6));
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
      if (var1 instanceof EntityLiving) {
         this.navigations.add(((EntityLiving)var1).getNavigator());
      }

   }

   public void onEntityRemoved(Entity var1) {
      if (var1 instanceof EntityLiving) {
         this.navigations.remove(((EntityLiving)var1).getNavigator());
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
