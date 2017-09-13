package net.minecraft.client.shader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Matrix4f;

@SideOnly(Side.CLIENT)
public class ShaderDefault extends ShaderUniform {
   public ShaderDefault() {
      super("dummy", 4, 1, (ShaderManager)null);
   }

   public void set(float var1) {
   }

   public void set(float var1, float var2) {
   }

   public void set(float var1, float var2, float var3) {
   }

   public void set(float var1, float var2, float var3, float var4) {
   }

   public void setSafe(float var1, float var2, float var3, float var4) {
   }

   public void set(int var1, int var2, int var3, int var4) {
   }

   public void set(float[] var1) {
   }

   public void set(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15, float var16) {
   }

   public void set(Matrix4f var1) {
   }
}
