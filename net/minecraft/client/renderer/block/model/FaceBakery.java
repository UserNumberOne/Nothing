package net.minecraft.client.renderer.block.model;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

@SideOnly(Side.CLIENT)
public class FaceBakery {
   private static final float SCALE_ROTATION_22_5 = 1.0F / (float)Math.cos(0.39269909262657166D) - 1.0F;
   private static final float SCALE_ROTATION_GENERAL = 1.0F / (float)Math.cos(0.7853981633974483D) - 1.0F;
   private static final FaceBakery.Rotation[] UV_ROTATIONS = new FaceBakery.Rotation[ModelRotation.values().length * EnumFacing.values().length];
   private static final FaceBakery.Rotation UV_ROTATION_0 = new FaceBakery.Rotation() {
      BlockFaceUV makeRotatedUV(float var1, float var2, float var3, float var4) {
         return new BlockFaceUV(new float[]{var1, var2, var3, var4}, 0);
      }
   };
   private static final FaceBakery.Rotation UV_ROTATION_270 = new FaceBakery.Rotation() {
      BlockFaceUV makeRotatedUV(float var1, float var2, float var3, float var4) {
         return new BlockFaceUV(new float[]{var4, 16.0F - var1, var2, 16.0F - var3}, 270);
      }
   };
   private static final FaceBakery.Rotation UV_ROTATION_INVERSE = new FaceBakery.Rotation() {
      BlockFaceUV makeRotatedUV(float var1, float var2, float var3, float var4) {
         return new BlockFaceUV(new float[]{16.0F - var1, 16.0F - var2, 16.0F - var3, 16.0F - var4}, 0);
      }
   };
   private static final FaceBakery.Rotation UV_ROTATION_90 = new FaceBakery.Rotation() {
      BlockFaceUV makeRotatedUV(float var1, float var2, float var3, float var4) {
         return new BlockFaceUV(new float[]{16.0F - var2, var3, 16.0F - var4, var1}, 90);
      }
   };

   public BakedQuad makeBakedQuad(Vector3f var1, Vector3f var2, BlockPartFace var3, TextureAtlasSprite var4, EnumFacing var5, ModelRotation var6, @Nullable BlockPartRotation var7, boolean var8, boolean var9) {
      return this.makeBakedQuad(var1, var2, var3, var4, var5, (ITransformation)var6, var7, var8, var9);
   }

   public BakedQuad makeBakedQuad(Vector3f var1, Vector3f var2, BlockPartFace var3, TextureAtlasSprite var4, EnumFacing var5, ITransformation var6, BlockPartRotation var7, boolean var8, boolean var9) {
      BlockFaceUV var10 = var3.blockFaceUV;
      if (var8) {
         var10 = ForgeHooksClient.applyUVLock(var3.blockFaceUV, var5, var6);
      }

      int[] var11 = this.makeQuadVertexData(var10, var4, var5, this.getPositionsDiv16(var1, var2), var6, var7, false);
      EnumFacing var12 = getFacingFromVertexData(var11);
      if (var7 == null) {
         this.applyFacing(var11, var12);
      }

      ForgeHooksClient.fillNormal(var11, var12);
      return new BakedQuad(var11, var3.tintIndex, var12, var4, var9, DefaultVertexFormats.ITEM);
   }

   private BlockFaceUV applyUVLock(BlockFaceUV var1, EnumFacing var2, ModelRotation var3) {
      return UV_ROTATIONS[getIndex(var3, var2)].rotateUV(var1);
   }

   private int[] makeQuadVertexData(BlockFaceUV var1, TextureAtlasSprite var2, EnumFacing var3, float[] var4, ModelRotation var5, @Nullable BlockPartRotation var6, boolean var7) {
      return this.makeQuadVertexData(var1, var2, var3, var4, (ITransformation)var5, var6, var7);
   }

   private int[] makeQuadVertexData(BlockFaceUV var1, TextureAtlasSprite var2, EnumFacing var3, float[] var4, ITransformation var5, BlockPartRotation var6, boolean var7) {
      int[] var8 = new int[28];

      for(int var9 = 0; var9 < 4; ++var9) {
         this.fillVertexData(var8, var9, var3, var1, var4, var2, var5, var6, var7);
      }

      return var8;
   }

   private int getFaceShadeColor(EnumFacing var1) {
      float var2 = this.getFaceBrightness(var1);
      int var3 = MathHelper.clamp((int)(var2 * 255.0F), 0, 255);
      return -16777216 | var3 << 16 | var3 << 8 | var3;
   }

   private float getFaceBrightness(EnumFacing var1) {
      switch(var1) {
      case DOWN:
         return 0.5F;
      case UP:
         return 1.0F;
      case NORTH:
      case SOUTH:
         return 0.8F;
      case WEST:
      case EAST:
         return 0.6F;
      default:
         return 1.0F;
      }
   }

   private float[] getPositionsDiv16(Vector3f var1, Vector3f var2) {
      float[] var3 = new float[EnumFacing.values().length];
      var3[EnumFaceDirection.Constants.WEST_INDEX] = var1.x / 16.0F;
      var3[EnumFaceDirection.Constants.DOWN_INDEX] = var1.y / 16.0F;
      var3[EnumFaceDirection.Constants.NORTH_INDEX] = var1.z / 16.0F;
      var3[EnumFaceDirection.Constants.EAST_INDEX] = var2.x / 16.0F;
      var3[EnumFaceDirection.Constants.UP_INDEX] = var2.y / 16.0F;
      var3[EnumFaceDirection.Constants.SOUTH_INDEX] = var2.z / 16.0F;
      return var3;
   }

   private void fillVertexData(int[] var1, int var2, EnumFacing var3, BlockFaceUV var4, float[] var5, TextureAtlasSprite var6, ModelRotation var7, @Nullable BlockPartRotation var8, boolean var9) {
      this.fillVertexData(var1, var2, var3, var4, var5, var6, (ITransformation)var7, var8, var9);
   }

   private void fillVertexData(int[] var1, int var2, EnumFacing var3, BlockFaceUV var4, float[] var5, TextureAtlasSprite var6, ITransformation var7, BlockPartRotation var8, boolean var9) {
      EnumFacing var10 = var7.rotate(var3);
      int var11 = var9 ? this.getFaceShadeColor(var10) : -1;
      EnumFaceDirection.VertexInformation var12 = EnumFaceDirection.getFacing(var3).getVertexInformation(var2);
      Vector3f var13 = new Vector3f(var5[var12.xIndex], var5[var12.yIndex], var5[var12.zIndex]);
      this.rotatePart(var13, var8);
      int var14 = this.rotateVertex(var13, var3, var2, var7);
      this.storeVertexData(var1, var14, var2, var13, var11, var6, var4);
   }

   private void storeVertexData(int[] var1, int var2, int var3, Vector3f var4, int var5, TextureAtlasSprite var6, BlockFaceUV var7) {
      int var8 = var2 * 7;
      var1[var8] = Float.floatToRawIntBits(var4.x);
      var1[var8 + 1] = Float.floatToRawIntBits(var4.y);
      var1[var8 + 2] = Float.floatToRawIntBits(var4.z);
      var1[var8 + 3] = var5;
      var1[var8 + 4] = Float.floatToRawIntBits(var6.getInterpolatedU((double)var7.getVertexU(var3) * 0.999D + (double)var7.getVertexU((var3 + 2) % 4) * 0.001D));
      var1[var8 + 4 + 1] = Float.floatToRawIntBits(var6.getInterpolatedV((double)var7.getVertexV(var3) * 0.999D + (double)var7.getVertexV((var3 + 2) % 4) * 0.001D));
   }

   private void rotatePart(Vector3f var1, @Nullable BlockPartRotation var2) {
      if (var2 != null) {
         Matrix4f var3 = this.getMatrixIdentity();
         Vector3f var4 = new Vector3f(0.0F, 0.0F, 0.0F);
         switch(var2.axis) {
         case X:
            Matrix4f.rotate(var2.angle * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F), var3, var3);
            var4.set(0.0F, 1.0F, 1.0F);
            break;
         case Y:
            Matrix4f.rotate(var2.angle * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F), var3, var3);
            var4.set(1.0F, 0.0F, 1.0F);
            break;
         case Z:
            Matrix4f.rotate(var2.angle * 0.017453292F, new Vector3f(0.0F, 0.0F, 1.0F), var3, var3);
            var4.set(1.0F, 1.0F, 0.0F);
         }

         if (var2.rescale) {
            if (Math.abs(var2.angle) == 22.5F) {
               var4.scale(SCALE_ROTATION_22_5);
            } else {
               var4.scale(SCALE_ROTATION_GENERAL);
            }

            Vector3f.add(var4, new Vector3f(1.0F, 1.0F, 1.0F), var4);
         } else {
            var4.set(1.0F, 1.0F, 1.0F);
         }

         this.rotateScale(var1, new Vector3f(var2.origin), var3, var4);
      }

   }

   public int rotateVertex(Vector3f var1, EnumFacing var2, int var3, ModelRotation var4) {
      return this.rotateVertex(var1, var2, var3, (ITransformation)var4);
   }

   public int rotateVertex(Vector3f var1, EnumFacing var2, int var3, ITransformation var4) {
      if (var4 == ModelRotation.X0_Y0) {
         return var3;
      } else {
         ForgeHooksClient.transform(var1, var4.getMatrix());
         return var4.rotate(var2, var3);
      }
   }

   private void rotateScale(Vector3f var1, Vector3f var2, Matrix4f var3, Vector3f var4) {
      Vector4f var5 = new Vector4f(var1.x - var2.x, var1.y - var2.y, var1.z - var2.z, 1.0F);
      Matrix4f.transform(var3, var5, var5);
      var5.x *= var4.x;
      var5.y *= var4.y;
      var5.z *= var4.z;
      var1.set(var5.x + var2.x, var5.y + var2.y, var5.z + var2.z);
   }

   private Matrix4f getMatrixIdentity() {
      Matrix4f var1 = new Matrix4f();
      var1.setIdentity();
      return var1;
   }

   public static EnumFacing getFacingFromVertexData(int[] var0) {
      Vector3f var1 = new Vector3f(Float.intBitsToFloat(var0[0]), Float.intBitsToFloat(var0[1]), Float.intBitsToFloat(var0[2]));
      Vector3f var2 = new Vector3f(Float.intBitsToFloat(var0[7]), Float.intBitsToFloat(var0[8]), Float.intBitsToFloat(var0[9]));
      Vector3f var3 = new Vector3f(Float.intBitsToFloat(var0[14]), Float.intBitsToFloat(var0[15]), Float.intBitsToFloat(var0[16]));
      Vector3f var4 = new Vector3f();
      Vector3f var5 = new Vector3f();
      Vector3f var6 = new Vector3f();
      Vector3f.sub(var1, var2, var4);
      Vector3f.sub(var3, var2, var5);
      Vector3f.cross(var5, var4, var6);
      float var7 = (float)Math.sqrt((double)(var6.x * var6.x + var6.y * var6.y + var6.z * var6.z));
      var6.x /= var7;
      var6.y /= var7;
      var6.z /= var7;
      EnumFacing var8 = null;
      float var9 = 0.0F;

      for(EnumFacing var13 : EnumFacing.values()) {
         Vec3i var14 = var13.getDirectionVec();
         Vector3f var15 = new Vector3f((float)var14.getX(), (float)var14.getY(), (float)var14.getZ());
         float var16 = Vector3f.dot(var6, var15);
         if (var16 >= 0.0F && var16 > var9) {
            var9 = var16;
            var8 = var13;
         }
      }

      if (var8 == null) {
         return EnumFacing.UP;
      } else {
         return var8;
      }
   }

   private void applyFacing(int[] var1, EnumFacing var2) {
      int[] var3 = new int[var1.length];
      System.arraycopy(var1, 0, var3, 0, var1.length);
      float[] var4 = new float[EnumFacing.values().length];
      var4[EnumFaceDirection.Constants.WEST_INDEX] = 999.0F;
      var4[EnumFaceDirection.Constants.DOWN_INDEX] = 999.0F;
      var4[EnumFaceDirection.Constants.NORTH_INDEX] = 999.0F;
      var4[EnumFaceDirection.Constants.EAST_INDEX] = -999.0F;
      var4[EnumFaceDirection.Constants.UP_INDEX] = -999.0F;
      var4[EnumFaceDirection.Constants.SOUTH_INDEX] = -999.0F;

      for(int var5 = 0; var5 < 4; ++var5) {
         int var6 = 7 * var5;
         float var7 = Float.intBitsToFloat(var3[var6]);
         float var8 = Float.intBitsToFloat(var3[var6 + 1]);
         float var9 = Float.intBitsToFloat(var3[var6 + 2]);
         if (var7 < var4[EnumFaceDirection.Constants.WEST_INDEX]) {
            var4[EnumFaceDirection.Constants.WEST_INDEX] = var7;
         }

         if (var8 < var4[EnumFaceDirection.Constants.DOWN_INDEX]) {
            var4[EnumFaceDirection.Constants.DOWN_INDEX] = var8;
         }

         if (var9 < var4[EnumFaceDirection.Constants.NORTH_INDEX]) {
            var4[EnumFaceDirection.Constants.NORTH_INDEX] = var9;
         }

         if (var7 > var4[EnumFaceDirection.Constants.EAST_INDEX]) {
            var4[EnumFaceDirection.Constants.EAST_INDEX] = var7;
         }

         if (var8 > var4[EnumFaceDirection.Constants.UP_INDEX]) {
            var4[EnumFaceDirection.Constants.UP_INDEX] = var8;
         }

         if (var9 > var4[EnumFaceDirection.Constants.SOUTH_INDEX]) {
            var4[EnumFaceDirection.Constants.SOUTH_INDEX] = var9;
         }
      }

      EnumFaceDirection var17 = EnumFaceDirection.getFacing(var2);

      for(int var18 = 0; var18 < 4; ++var18) {
         int var19 = 7 * var18;
         EnumFaceDirection.VertexInformation var20 = var17.getVertexInformation(var18);
         float var21 = var4[var20.xIndex];
         float var10 = var4[var20.yIndex];
         float var11 = var4[var20.zIndex];
         var1[var19] = Float.floatToRawIntBits(var21);
         var1[var19 + 1] = Float.floatToRawIntBits(var10);
         var1[var19 + 2] = Float.floatToRawIntBits(var11);

         for(int var12 = 0; var12 < 4; ++var12) {
            int var13 = 7 * var12;
            float var14 = Float.intBitsToFloat(var3[var13]);
            float var15 = Float.intBitsToFloat(var3[var13 + 1]);
            float var16 = Float.intBitsToFloat(var3[var13 + 2]);
            if (MathHelper.epsilonEquals(var21, var14) && MathHelper.epsilonEquals(var10, var15) && MathHelper.epsilonEquals(var11, var16)) {
               var1[var19 + 4] = var3[var13 + 4];
               var1[var19 + 4 + 1] = var3[var13 + 4 + 1];
            }
         }
      }

   }

   private static void addUvRotation(ModelRotation var0, EnumFacing var1, FaceBakery.Rotation var2) {
      UV_ROTATIONS[getIndex(var0, var1)] = var2;
   }

   private static int getIndex(ModelRotation var0, EnumFacing var1) {
      return ModelRotation.values().length * var1.ordinal() + var0.ordinal();
   }

   static {
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.EAST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y0, EnumFacing.WEST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.EAST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.WEST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.EAST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.WEST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.EAST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.WEST, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.DOWN, UV_ROTATION_0);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.SOUTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.NORTH, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.UP, UV_ROTATION_0);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.UP, UV_ROTATION_270);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.DOWN, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.WEST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.WEST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.WEST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.NORTH, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.SOUTH, UV_ROTATION_270);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.WEST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.UP, UV_ROTATION_270);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.DOWN, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.EAST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.EAST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.NORTH, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.SOUTH, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.EAST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.EAST, UV_ROTATION_270);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X0_Y180, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.EAST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y0, EnumFacing.WEST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.EAST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.WEST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.EAST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.UP, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y180, EnumFacing.WEST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.EAST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.WEST, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.NORTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.SOUTH, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.DOWN, UV_ROTATION_INVERSE);
      addUvRotation(ModelRotation.X0_Y90, EnumFacing.UP, UV_ROTATION_90);
      addUvRotation(ModelRotation.X0_Y270, EnumFacing.DOWN, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y0, EnumFacing.EAST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.EAST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.NORTH, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y90, EnumFacing.SOUTH, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y180, EnumFacing.EAST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X90_Y270, EnumFacing.EAST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y0, EnumFacing.WEST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X180_Y90, EnumFacing.DOWN, UV_ROTATION_90);
      addUvRotation(ModelRotation.X180_Y270, EnumFacing.UP, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y90, EnumFacing.WEST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y180, EnumFacing.WEST, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.NORTH, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.SOUTH, UV_ROTATION_90);
      addUvRotation(ModelRotation.X270_Y270, EnumFacing.WEST, UV_ROTATION_90);
   }

   @SideOnly(Side.CLIENT)
   abstract static class Rotation {
      private Rotation() {
      }

      public BlockFaceUV rotateUV(BlockFaceUV var1) {
         float var2 = var1.getVertexU(var1.getVertexRotatedRev(0));
         float var3 = var1.getVertexV(var1.getVertexRotatedRev(0));
         float var4 = var1.getVertexU(var1.getVertexRotatedRev(2));
         float var5 = var1.getVertexV(var1.getVertexRotatedRev(2));
         return this.makeRotatedUV(var2, var3, var4, var5);
      }

      abstract BlockFaceUV makeRotatedUV(float var1, float var2, float var3, float var4);
   }
}
