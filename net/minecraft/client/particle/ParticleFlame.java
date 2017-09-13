package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFlame extends Particle {
   private final float flameScale;

   protected ParticleFlame(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      this.motionX = this.motionX * 0.009999999776482582D + var8;
      this.motionY = this.motionY * 0.009999999776482582D + var10;
      this.motionZ = this.motionZ * 0.009999999776482582D + var12;
      this.posX += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
      this.posY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
      this.posZ += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F);
      this.flameScale = this.particleScale;
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
      this.setParticleTextureIndex(48);
   }

   public void move(double var1, double var3, double var5) {
      this.setBoundingBox(this.getBoundingBox().offset(var1, var3, var5));
      this.resetPositionToBB();
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = ((float)this.particleAge + var3) / (float)this.particleMaxAge;
      this.particleScale = this.flameScale * (1.0F - var9 * var9 * 0.5F);
      super.renderParticle(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public int getBrightnessForRender(float var1) {
      float var2 = ((float)this.particleAge + var1) / (float)this.particleMaxAge;
      var2 = MathHelper.clamp(var2, 0.0F, 1.0F);
      int var3 = super.getBrightnessForRender(var1);
      int var4 = var3 & 255;
      int var5 = var3 >> 16 & 255;
      var4 = var4 + (int)(var2 * 15.0F * 16.0F);
      if (var4 > 240) {
         var4 = 240;
      }

      return var4 | var5 << 16;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9599999785423279D;
      this.motionY *= 0.9599999785423279D;
      this.motionZ *= 0.9599999785423279D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleFlame(var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
