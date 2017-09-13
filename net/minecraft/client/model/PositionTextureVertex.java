package net.minecraft.client.model;

import net.minecraft.util.math.Vec3d;

public class PositionTextureVertex {
   public Vec3d vector3D;
   public float texturePositionX;
   public float texturePositionY;

   public PositionTextureVertex(float var1, float var2, float var3, float var4, float var5) {
      this(new Vec3d((double)p_i1158_1_, (double)p_i1158_2_, (double)p_i1158_3_), p_i1158_4_, p_i1158_5_);
   }

   public PositionTextureVertex setTexturePosition(float var1, float var2) {
      return new PositionTextureVertex(this, p_78240_1_, p_78240_2_);
   }

   public PositionTextureVertex(PositionTextureVertex var1, float var2, float var3) {
      this.vector3D = textureVertex.vector3D;
      this.texturePositionX = texturePositionXIn;
      this.texturePositionY = texturePositionYIn;
   }

   public PositionTextureVertex(Vec3d var1, float var2, float var3) {
      this.vector3D = p_i47091_1_;
      this.texturePositionX = p_i47091_2_;
      this.texturePositionY = p_i47091_3_;
   }
}
