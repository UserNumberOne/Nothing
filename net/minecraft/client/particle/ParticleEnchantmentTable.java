package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleEnchantmentTable extends Particle {
   private final float oSize;
   private final double coordX;
   private final double coordY;
   private final double coordZ;

   protected ParticleEnchantmentTable(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      this.motionX = var8;
      this.motionY = var10;
      this.motionZ = var12;
      this.coordX = var2;
      this.coordY = var4;
      this.coordZ = var6;
      this.prevPosX = var2 + var8;
      this.prevPosY = var4 + var10;
      this.prevPosZ = var6 + var12;
      this.posX = this.prevPosX;
      this.posY = this.prevPosY;
      this.posZ = this.prevPosZ;
      float var14 = this.rand.nextFloat() * 0.6F + 0.4F;
      this.particleScale = this.rand.nextFloat() * 0.5F + 0.2F;
      this.oSize = this.particleScale;
      this.particleRed = 0.9F * var14;
      this.particleGreen = 0.9F * var14;
      this.particleBlue = var14;
      this.particleMaxAge = (int)(Math.random() * 10.0D) + 30;
      this.setParticleTextureIndex((int)(Math.random() * 26.0D + 1.0D + 224.0D));
   }

   public void move(double var1, double var3, double var5) {
      this.setBoundingBox(this.getBoundingBox().offset(var1, var3, var5));
      this.resetPositionToBB();
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
      var1 = 1.0F - var1;
      float var2 = 1.0F - var1;
      var2 = var2 * var2;
      var2 = var2 * var2;
      this.posX = this.coordX + this.motionX * (double)var1;
      this.posY = this.coordY + this.motionY * (double)var1 - (double)(var2 * 1.2F);
      this.posZ = this.coordZ + this.motionZ * (double)var1;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

   }

   @SideOnly(Side.CLIENT)
   public static class EnchantmentTable implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleEnchantmentTable(var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
