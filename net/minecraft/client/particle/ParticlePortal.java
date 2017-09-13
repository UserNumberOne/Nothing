package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticlePortal extends Particle {
   private final float portalParticleScale;
   private final double portalPosX;
   private final double portalPosY;
   private final double portalPosZ;

   protected ParticlePortal(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      this.motionX = var8;
      this.motionY = var10;
      this.motionZ = var12;
      this.posX = var2;
      this.posY = var4;
      this.posZ = var6;
      this.portalPosX = this.posX;
      this.portalPosY = this.posY;
      this.portalPosZ = this.posZ;
      float var14 = this.rand.nextFloat() * 0.6F + 0.4F;
      this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F;
      this.portalParticleScale = this.particleScale;
      this.particleRed = var14 * 0.9F;
      this.particleGreen = var14 * 0.3F;
      this.particleBlue = var14;
      this.particleMaxAge = (int)(Math.random() * 10.0D) + 40;
      this.setParticleTextureIndex((int)(Math.random() * 8.0D));
   }

   public void move(double var1, double var3, double var5) {
      this.setBoundingBox(this.getBoundingBox().offset(var1, var3, var5));
      this.resetPositionToBB();
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = ((float)this.particleAge + var3) / (float)this.particleMaxAge;
      var9 = 1.0F - var9;
      var9 = var9 * var9;
      var9 = 1.0F - var9;
      this.particleScale = this.portalParticleScale * var9;
      super.renderParticle(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public int getBrightnessForRender(float var1) {
      int var2 = super.getBrightnessForRender(var1);
      float var3 = (float)this.particleAge / (float)this.particleMaxAge;
      var3 = var3 * var3;
      var3 = var3 * var3;
      int var4 = var2 & 255;
      int var5 = var2 >> 16 & 255;
      var5 = var5 + (int)(var3 * 15.0F * 16.0F);
      if (var5 > 240) {
         var5 = 240;
      }

      return var4 | var5 << 16;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      float var1 = (float)this.particleAge / (float)this.particleMaxAge;
      float var2 = -var1 + var1 * var1 * 2.0F;
      float var3 = 1.0F - var2;
      this.posX = this.portalPosX + this.motionX * (double)var3;
      this.posY = this.portalPosY + this.motionY * (double)var3 + (double)(1.0F - var1);
      this.posZ = this.portalPosZ + this.motionZ * (double)var3;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticlePortal(var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
