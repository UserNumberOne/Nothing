package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSimpleAnimated extends Particle {
   private final int textureIdx;
   private final int numAgingFrames;
   private final float yAccel;
   private float fadeTargetRed;
   private float fadeTargetGreen;
   private float fadeTargetBlue;
   private boolean fadingColor;

   public ParticleSimpleAnimated(World var1, double var2, double var4, double var6, int var8, int var9, float var10) {
      super(var1, var2, var4, var6);
      this.textureIdx = var8;
      this.numAgingFrames = var9;
      this.yAccel = var10;
   }

   public void setColor(int var1) {
      float var2 = (float)((var1 & 16711680) >> 16) / 255.0F;
      float var3 = (float)((var1 & '\uff00') >> 8) / 255.0F;
      float var4 = (float)((var1 & 255) >> 0) / 255.0F;
      float var5 = 1.0F;
      this.setRBGColorF(var2 * 1.0F, var3 * 1.0F, var4 * 1.0F);
   }

   public void setColorFade(int var1) {
      this.fadeTargetRed = (float)((var1 & 16711680) >> 16) / 255.0F;
      this.fadeTargetGreen = (float)((var1 & '\uff00') >> 8) / 255.0F;
      this.fadeTargetBlue = (float)((var1 & 255) >> 0) / 255.0F;
      this.fadingColor = true;
   }

   public boolean isTransparent() {
      return true;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      if (this.particleAge > this.particleMaxAge / 2) {
         this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
         if (this.fadingColor) {
            this.particleRed += (this.fadeTargetRed - this.particleRed) * 0.2F;
            this.particleGreen += (this.fadeTargetGreen - this.particleGreen) * 0.2F;
            this.particleBlue += (this.fadeTargetBlue - this.particleBlue) * 0.2F;
         }
      }

      this.setParticleTextureIndex(this.textureIdx + (this.numAgingFrames - 1 - this.particleAge * this.numAgingFrames / this.particleMaxAge));
      this.motionY += (double)this.yAccel;
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9100000262260437D;
      this.motionY *= 0.9100000262260437D;
      this.motionZ *= 0.9100000262260437D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   public int getBrightnessForRender(float var1) {
      return 15728880;
   }
}
