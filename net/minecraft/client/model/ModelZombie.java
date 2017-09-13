package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelZombie extends ModelBiped {
   public ModelZombie() {
      this(0.0F, false);
   }

   public ModelZombie(float var1, boolean var2) {
      super(var1, 0.0F, 64, var2 ? 32 : 64);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(var1, var2, var3, var4, var5, var6, var7);
      boolean var8 = var7 instanceof EntityZombie && ((EntityZombie)var7).isArmsRaised();
      float var9 = MathHelper.sin(this.swingProgress * 3.1415927F);
      float var10 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * 3.1415927F);
      this.bipedRightArm.rotateAngleZ = 0.0F;
      this.bipedLeftArm.rotateAngleZ = 0.0F;
      this.bipedRightArm.rotateAngleY = -(0.1F - var9 * 0.6F);
      this.bipedLeftArm.rotateAngleY = 0.1F - var9 * 0.6F;
      float var11 = -3.1415927F / (var8 ? 1.5F : 2.25F);
      this.bipedRightArm.rotateAngleX = var11;
      this.bipedLeftArm.rotateAngleX = var11;
      this.bipedRightArm.rotateAngleX += var9 * 1.2F - var10 * 0.4F;
      this.bipedLeftArm.rotateAngleX += var9 * 1.2F - var10 * 0.4F;
      this.bipedRightArm.rotateAngleZ += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
      this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
      this.bipedRightArm.rotateAngleX += MathHelper.sin(var3 * 0.067F) * 0.05F;
      this.bipedLeftArm.rotateAngleX -= MathHelper.sin(var3 * 0.067F) * 0.05F;
   }
}
