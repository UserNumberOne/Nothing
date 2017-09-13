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
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      this.motionX = this.motionX * 0.009999999776482582D + xSpeedIn;
      this.motionY = this.motionY * 0.009999999776482582D + ySpeedIn;
      this.motionZ = this.motionZ * 0.009999999776482582D + zSpeedIn;
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
      this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
      this.resetPositionToBB();
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
      this.particleScale = this.flameScale * (1.0F - f * f * 0.5F);
      super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
   }

   public int getBrightnessForRender(float var1) {
      float f = ((float)this.particleAge + p_189214_1_) / (float)this.particleMaxAge;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      int i = super.getBrightnessForRender(p_189214_1_);
      int j = i & 255;
      int k = i >> 16 & 255;
      j = j + (int)(f * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
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
         return new ParticleFlame(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
