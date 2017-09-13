package net.minecraft.entity.ai;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RandomPositionGenerator {
   private static Vec3d staticVector = Vec3d.ZERO;

   @Nullable
   public static Vec3d findRandomTarget(EntityCreature var0, int var1, int var2) {
      return findRandomTargetBlock(var0, var1, var2, (Vec3d)null);
   }

   @Nullable
   public static Vec3d findRandomTargetBlockTowards(EntityCreature var0, int var1, int var2, Vec3d var3) {
      staticVector = var3.subtract(var0.posX, var0.posY, var0.posZ);
      return findRandomTargetBlock(var0, var1, var2, staticVector);
   }

   @Nullable
   public static Vec3d findRandomTargetBlockAwayFrom(EntityCreature var0, int var1, int var2, Vec3d var3) {
      staticVector = (new Vec3d(var0.posX, var0.posY, var0.posZ)).subtract(var3);
      return findRandomTargetBlock(var0, var1, var2, staticVector);
   }

   @Nullable
   private static Vec3d findRandomTargetBlock(EntityCreature var0, int var1, int var2, @Nullable Vec3d var3) {
      PathNavigate var4 = var0.getNavigator();
      Random var5 = var0.getRNG();
      boolean var6 = false;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      float var10 = -99999.0F;
      boolean var11;
      if (var0.hasHome()) {
         double var12 = var0.getHomePosition().distanceSq((double)MathHelper.floor(var0.posX), (double)MathHelper.floor(var0.posY), (double)MathHelper.floor(var0.posZ)) + 4.0D;
         double var14 = (double)(var0.getMaximumHomeDistance() + (float)var1);
         var11 = var12 < var14 * var14;
      } else {
         var11 = false;
      }

      for(int var18 = 0; var18 < 10; ++var18) {
         int var13 = var5.nextInt(2 * var1 + 1) - var1;
         int var19 = var5.nextInt(2 * var2 + 1) - var2;
         int var15 = var5.nextInt(2 * var1 + 1) - var1;
         if (var3 == null || (double)var13 * var3.xCoord + (double)var15 * var3.zCoord >= 0.0D) {
            if (var0.hasHome() && var1 > 1) {
               BlockPos var16 = var0.getHomePosition();
               if (var0.posX > (double)var16.getX()) {
                  var13 -= var5.nextInt(var1 / 2);
               } else {
                  var13 += var5.nextInt(var1 / 2);
               }

               if (var0.posZ > (double)var16.getZ()) {
                  var15 -= var5.nextInt(var1 / 2);
               } else {
                  var15 += var5.nextInt(var1 / 2);
               }
            }

            BlockPos var20 = new BlockPos((double)var13 + var0.posX, (double)var19 + var0.posY, (double)var15 + var0.posZ);
            if ((!var11 || var0.isWithinHomeDistanceFromPosition(var20)) && var4.canEntityStandOnPos(var20)) {
               float var17 = var0.getBlockPathWeight(var20);
               if (var17 > var10) {
                  var10 = var17;
                  var7 = var13;
                  var8 = var19;
                  var9 = var15;
                  var6 = true;
               }
            }
         }
      }

      if (var6) {
         return new Vec3d((double)var7 + var0.posX, (double)var8 + var0.posY, (double)var9 + var0.posZ);
      } else {
         return null;
      }
   }
}
