package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.glu.GLU;

@SideOnly(Side.CLIENT)
public class ActiveRenderInfo {
   private static final IntBuffer VIEWPORT = GLAllocation.createDirectIntBuffer(16);
   private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer OBJECTCOORDS = GLAllocation.createDirectFloatBuffer(3);
   private static Vec3d position = new Vec3d(0.0D, 0.0D, 0.0D);
   private static float rotationX;
   private static float rotationXZ;
   private static float rotationZ;
   private static float rotationYZ;
   private static float rotationXY;

   public static void updateRenderInfo(EntityPlayer var0, boolean var1) {
      GlStateManager.getFloat(2982, MODELVIEW);
      GlStateManager.getFloat(2983, PROJECTION);
      GlStateManager.glGetInteger(2978, VIEWPORT);
      float var2 = (float)((VIEWPORT.get(0) + VIEWPORT.get(2)) / 2);
      float var3 = (float)((VIEWPORT.get(1) + VIEWPORT.get(3)) / 2);
      GLU.gluUnProject(var2, var3, 0.0F, MODELVIEW, PROJECTION, VIEWPORT, OBJECTCOORDS);
      position = new Vec3d((double)OBJECTCOORDS.get(0), (double)OBJECTCOORDS.get(1), (double)OBJECTCOORDS.get(2));
      int var4 = var1 ? 1 : 0;
      float var5 = var0.rotationPitch;
      float var6 = var0.rotationYaw;
      rotationX = MathHelper.cos(var6 * 0.017453292F) * (float)(1 - var4 * 2);
      rotationZ = MathHelper.sin(var6 * 0.017453292F) * (float)(1 - var4 * 2);
      rotationYZ = -rotationZ * MathHelper.sin(var5 * 0.017453292F) * (float)(1 - var4 * 2);
      rotationXY = rotationX * MathHelper.sin(var5 * 0.017453292F) * (float)(1 - var4 * 2);
      rotationXZ = MathHelper.cos(var5 * 0.017453292F);
   }

   public static Vec3d projectViewFromEntity(Entity var0, double var1) {
      double var3 = var0.prevPosX + (var0.posX - var0.prevPosX) * var1;
      double var5 = var0.prevPosY + (var0.posY - var0.prevPosY) * var1;
      double var7 = var0.prevPosZ + (var0.posZ - var0.prevPosZ) * var1;
      double var9 = var3 + position.xCoord;
      double var11 = var5 + position.yCoord;
      double var13 = var7 + position.zCoord;
      return new Vec3d(var9, var11, var13);
   }

   public static IBlockState getBlockStateAtEntityViewpoint(World var0, Entity var1, float var2) {
      Vec3d var3 = projectViewFromEntity(var1, (double)var2);
      BlockPos var4 = new BlockPos(var3);
      IBlockState var5 = var0.getBlockState(var4);
      if (var5.getMaterial().isLiquid()) {
         float var6 = 0.0F;
         if (var5.getBlock() instanceof BlockLiquid) {
            var6 = BlockLiquid.getLiquidHeightPercent(((Integer)var5.getValue(BlockLiquid.LEVEL)).intValue()) - 0.11111111F;
         }

         float var7 = (float)(var4.getY() + 1) - var6;
         if (var3.yCoord >= (double)var7) {
            var5 = var0.getBlockState(var4.up());
         }
      }

      return var5;
   }

   public static Vec3d getPosition() {
      return position;
   }

   public static float getRotationX() {
      return rotationX;
   }

   public static float getRotationXZ() {
      return rotationXZ;
   }

   public static float getRotationZ() {
      return rotationZ;
   }

   public static float getRotationYZ() {
      return rotationYZ;
   }

   public static float getRotationXY() {
      return rotationXY;
   }
}
