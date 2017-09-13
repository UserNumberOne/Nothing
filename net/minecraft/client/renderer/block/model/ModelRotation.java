package net.minecraft.client.renderer.block.model;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public enum ModelRotation implements IModelState, ITransformation {
   X0_Y0(0, 0),
   X0_Y90(0, 90),
   X0_Y180(0, 180),
   X0_Y270(0, 270),
   X90_Y0(90, 0),
   X90_Y90(90, 90),
   X90_Y180(90, 180),
   X90_Y270(90, 270),
   X180_Y0(180, 0),
   X180_Y90(180, 90),
   X180_Y180(180, 180),
   X180_Y270(180, 270),
   X270_Y0(270, 0),
   X270_Y90(270, 90),
   X270_Y180(270, 180),
   X270_Y270(270, 270);

   private static final Map MAP_ROTATIONS = Maps.newHashMap();
   private final int combinedXY;
   private final Matrix4f matrix4d;
   private final int quartersX;
   private final int quartersY;

   private static int combineXY(int var0, int var1) {
      return var0 * 360 + var1;
   }

   private ModelRotation(int var3, int var4) {
      this.combinedXY = combineXY(var3, var4);
      this.matrix4d = new Matrix4f();
      Matrix4f var5 = new Matrix4f();
      var5.setIdentity();
      Matrix4f.rotate((float)(-var3) * 0.017453292F, new Vector3f(1.0F, 0.0F, 0.0F), var5, var5);
      this.quartersX = MathHelper.abs(var3 / 90);
      Matrix4f var6 = new Matrix4f();
      var6.setIdentity();
      Matrix4f.rotate((float)(-var4) * 0.017453292F, new Vector3f(0.0F, 1.0F, 0.0F), var6, var6);
      this.quartersY = MathHelper.abs(var4 / 90);
      Matrix4f.mul(var6, var5, this.matrix4d);
   }

   public Matrix4f getMatrix4d() {
      return this.matrix4d;
   }

   public EnumFacing rotateFace(EnumFacing var1) {
      EnumFacing var2 = var1;

      for(int var3 = 0; var3 < this.quartersX; ++var3) {
         var2 = var2.rotateAround(EnumFacing.Axis.X);
      }

      if (var2.getAxis() != EnumFacing.Axis.Y) {
         for(int var4 = 0; var4 < this.quartersY; ++var4) {
            var2 = var2.rotateAround(EnumFacing.Axis.Y);
         }
      }

      return var2;
   }

   public int rotateVertex(EnumFacing var1, int var2) {
      int var3 = var2;
      if (var1.getAxis() == EnumFacing.Axis.X) {
         var3 = (var2 + this.quartersX) % 4;
      }

      EnumFacing var4 = var1;

      for(int var5 = 0; var5 < this.quartersX; ++var5) {
         var4 = var4.rotateAround(EnumFacing.Axis.X);
      }

      if (var4.getAxis() == EnumFacing.Axis.Y) {
         var3 = (var3 + this.quartersY) % 4;
      }

      return var3;
   }

   public static ModelRotation getModelRotation(int var0, int var1) {
      return (ModelRotation)MAP_ROTATIONS.get(Integer.valueOf(combineXY(MathHelper.normalizeAngle(var0, 360), MathHelper.normalizeAngle(var1, 360))));
   }

   public Optional apply(Optional var1) {
      return ForgeHooksClient.applyTransform(this.getMatrix(), var1);
   }

   public javax.vecmath.Matrix4f getMatrix() {
      return ForgeHooksClient.getMatrix(this);
   }

   public EnumFacing rotate(EnumFacing var1) {
      return this.rotateFace(var1);
   }

   public int rotate(EnumFacing var1, int var2) {
      return this.rotateVertex(var1, var2);
   }

   static {
      for(ModelRotation var3 : values()) {
         MAP_ROTATIONS.put(Integer.valueOf(var3.combinedXY), var3);
      }

   }
}
