package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFirework {
   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleFirework.Spark var16 = new ParticleFirework.Spark(var2, var3, var5, var7, var9, var11, var13, Minecraft.getMinecraft().effectRenderer);
         var16.setAlphaF(0.99F);
         return var16;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Overlay extends Particle {
      protected Overlay(World var1, double var2, double var4, double var6) {
         super(var1, var2, var4, var6);
         this.particleMaxAge = 4;
      }

      public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
         float var9 = 0.25F;
         float var10 = 0.5F;
         float var11 = 0.125F;
         float var12 = 0.375F;
         float var13 = 7.1F * MathHelper.sin(((float)this.particleAge + var3 - 1.0F) * 0.25F * 3.1415927F);
         this.setAlphaF(0.6F - ((float)this.particleAge + var3 - 1.0F) * 0.25F * 0.5F);
         float var14 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)var3 - interpPosX);
         float var15 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)var3 - interpPosY);
         float var16 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var3 - interpPosZ);
         int var17 = this.getBrightnessForRender(var3);
         int var18 = var17 >> 16 & '\uffff';
         int var19 = var17 & '\uffff';
         var1.pos((double)(var14 - var4 * var13 - var7 * var13), (double)(var15 - var5 * var13), (double)(var16 - var6 * var13 - var8 * var13)).tex(0.5D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
         var1.pos((double)(var14 - var4 * var13 + var7 * var13), (double)(var15 + var5 * var13), (double)(var16 - var6 * var13 + var8 * var13)).tex(0.5D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
         var1.pos((double)(var14 + var4 * var13 + var7 * var13), (double)(var15 + var5 * var13), (double)(var16 + var6 * var13 + var8 * var13)).tex(0.25D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
         var1.pos((double)(var14 + var4 * var13 - var7 * var13), (double)(var15 - var5 * var13), (double)(var16 + var6 * var13 - var8 * var13)).tex(0.25D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Spark extends ParticleSimpleAnimated {
      private boolean trail;
      private boolean twinkle;
      private final ParticleManager effectRenderer;
      private float fadeColourRed;
      private float fadeColourGreen;
      private float fadeColourBlue;
      private boolean hasFadeColour;

      public Spark(World var1, double var2, double var4, double var6, double var8, double var10, double var12, ParticleManager var14) {
         super(var1, var2, var4, var6, 160, 8, -0.004F);
         this.motionX = var8;
         this.motionY = var10;
         this.motionZ = var12;
         this.effectRenderer = var14;
         this.particleScale *= 0.75F;
         this.particleMaxAge = 48 + this.rand.nextInt(12);
      }

      public void setTrail(boolean var1) {
         this.trail = var1;
      }

      public void setTwinkle(boolean var1) {
         this.twinkle = var1;
      }

      public boolean isTransparent() {
         return true;
      }

      public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
         if (!this.twinkle || this.particleAge < this.particleMaxAge / 3 || (this.particleAge + this.particleMaxAge) / 3 % 2 == 0) {
            super.renderParticle(var1, var2, var3, var4, var5, var6, var7, var8);
         }

      }

      public void onUpdate() {
         super.onUpdate();
         if (this.trail && this.particleAge < this.particleMaxAge / 2 && (this.particleAge + this.particleMaxAge) % 2 == 0) {
            ParticleFirework.Spark var1 = new ParticleFirework.Spark(this.world, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, this.effectRenderer);
            var1.setAlphaF(0.99F);
            var1.setRBGColorF(this.particleRed, this.particleGreen, this.particleBlue);
            var1.particleAge = var1.particleMaxAge / 2;
            if (this.hasFadeColour) {
               var1.hasFadeColour = true;
               var1.fadeColourRed = this.fadeColourRed;
               var1.fadeColourGreen = this.fadeColourGreen;
               var1.fadeColourBlue = this.fadeColourBlue;
            }

            var1.twinkle = this.twinkle;
            this.effectRenderer.addEffect(var1);
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class Starter extends Particle {
      private int fireworkAge;
      private final ParticleManager theEffectRenderer;
      private NBTTagList fireworkExplosions;
      boolean twinkle;

      public Starter(World var1, double var2, double var4, double var6, double var8, double var10, double var12, ParticleManager var14, NBTTagCompound var15) {
         super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
         this.motionX = var8;
         this.motionY = var10;
         this.motionZ = var12;
         this.theEffectRenderer = var14;
         this.particleMaxAge = 8;
         if (var15 != null) {
            this.fireworkExplosions = var15.getTagList("Explosions", 10);
            if (this.fireworkExplosions.hasNoTags()) {
               this.fireworkExplosions = null;
            } else {
               this.particleMaxAge = this.fireworkExplosions.tagCount() * 2 - 1;

               for(int var16 = 0; var16 < this.fireworkExplosions.tagCount(); ++var16) {
                  NBTTagCompound var17 = this.fireworkExplosions.getCompoundTagAt(var16);
                  if (var17.getBoolean("Flicker")) {
                     this.twinkle = true;
                     this.particleMaxAge += 15;
                     break;
                  }
               }
            }
         }

      }

      public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      }

      public void onUpdate() {
         if (this.fireworkAge == 0 && this.fireworkExplosions != null) {
            boolean var1 = this.isFarFromCamera();
            boolean var2 = false;
            if (this.fireworkExplosions.tagCount() >= 3) {
               var2 = true;
            } else {
               for(int var3 = 0; var3 < this.fireworkExplosions.tagCount(); ++var3) {
                  NBTTagCompound var4 = this.fireworkExplosions.getCompoundTagAt(var3);
                  if (var4.getByte("Type") == 1) {
                     var2 = true;
                     break;
                  }
               }
            }

            SoundEvent var17;
            if (var2) {
               var17 = var1 ? SoundEvents.ENTITY_FIREWORK_LARGE_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_LARGE_BLAST;
            } else {
               var17 = var1 ? SoundEvents.ENTITY_FIREWORK_BLAST_FAR : SoundEvents.ENTITY_FIREWORK_BLAST;
            }

            this.world.playSound(this.posX, this.posY, this.posZ, var17, SoundCategory.AMBIENT, 20.0F, 0.95F + this.rand.nextFloat() * 0.1F, true);
         }

         if (this.fireworkAge % 2 == 0 && this.fireworkExplosions != null && this.fireworkAge / 2 < this.fireworkExplosions.tagCount()) {
            int var13 = this.fireworkAge / 2;
            NBTTagCompound var15 = this.fireworkExplosions.getCompoundTagAt(var13);
            byte var18 = var15.getByte("Type");
            boolean var19 = var15.getBoolean("Trail");
            boolean var5 = var15.getBoolean("Flicker");
            int[] var6 = var15.getIntArray("Colors");
            int[] var7 = var15.getIntArray("FadeColors");
            if (var6.length == 0) {
               var6 = new int[]{ItemDye.DYE_COLORS[0]};
            }

            if (var18 == 1) {
               this.createBall(0.5D, 4, var6, var7, var19, var5);
            } else if (var18 == 2) {
               this.createShaped(0.5D, new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, var6, var7, var19, var5, false);
            } else if (var18 == 3) {
               this.createShaped(0.5D, new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, var6, var7, var19, var5, true);
            } else if (var18 == 4) {
               this.createBurst(var6, var7, var19, var5);
            } else {
               this.createBall(0.25D, 2, var6, var7, var19, var5);
            }

            int var8 = var6[0];
            float var9 = (float)((var8 & 16711680) >> 16) / 255.0F;
            float var10 = (float)((var8 & '\uff00') >> 8) / 255.0F;
            float var11 = (float)((var8 & 255) >> 0) / 255.0F;
            ParticleFirework.Overlay var12 = new ParticleFirework.Overlay(this.world, this.posX, this.posY, this.posZ);
            var12.setRBGColorF(var9, var10, var11);
            this.theEffectRenderer.addEffect(var12);
         }

         ++this.fireworkAge;
         if (this.fireworkAge > this.particleMaxAge) {
            if (this.twinkle) {
               boolean var14 = this.isFarFromCamera();
               SoundEvent var16 = var14 ? SoundEvents.ENTITY_FIREWORK_TWINKLE_FAR : SoundEvents.ENTITY_FIREWORK_TWINKLE;
               this.world.playSound(this.posX, this.posY, this.posZ, var16, SoundCategory.AMBIENT, 20.0F, 0.9F + this.rand.nextFloat() * 0.15F, true);
            }

            this.setExpired();
         }

      }

      private boolean isFarFromCamera() {
         Minecraft var1 = Minecraft.getMinecraft();
         return var1 == null || var1.getRenderViewEntity() == null || var1.getRenderViewEntity().getDistanceSq(this.posX, this.posY, this.posZ) >= 256.0D;
      }

      private void createParticle(double var1, double var3, double var5, double var7, double var9, double var11, int[] var13, int[] var14, boolean var15, boolean var16) {
         ParticleFirework.Spark var17 = new ParticleFirework.Spark(this.world, var1, var3, var5, var7, var9, var11, this.theEffectRenderer);
         var17.setAlphaF(0.99F);
         var17.setTrail(var15);
         var17.setTwinkle(var16);
         int var18 = this.rand.nextInt(var13.length);
         var17.setColor(var13[var18]);
         if (var14 != null && var14.length > 0) {
            var17.setColorFade(var14[this.rand.nextInt(var14.length)]);
         }

         this.theEffectRenderer.addEffect(var17);
      }

      private void createBall(double var1, int var3, int[] var4, int[] var5, boolean var6, boolean var7) {
         double var8 = this.posX;
         double var10 = this.posY;
         double var12 = this.posZ;

         for(int var14 = -var3; var14 <= var3; ++var14) {
            for(int var15 = -var3; var15 <= var3; ++var15) {
               for(int var16 = -var3; var16 <= var3; ++var16) {
                  double var17 = (double)var15 + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                  double var19 = (double)var14 + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                  double var21 = (double)var16 + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                  double var23 = (double)MathHelper.sqrt(var17 * var17 + var19 * var19 + var21 * var21) / var1 + this.rand.nextGaussian() * 0.05D;
                  this.createParticle(var8, var10, var12, var17 / var23, var19 / var23, var21 / var23, var4, var5, var6, var7);
                  if (var14 != -var3 && var14 != var3 && var15 != -var3 && var15 != var3) {
                     var16 += var3 * 2 - 1;
                  }
               }
            }
         }

      }

      private void createShaped(double var1, double[][] var3, int[] var4, int[] var5, boolean var6, boolean var7, boolean var8) {
         double var9 = var3[0][0];
         double var11 = var3[0][1];
         this.createParticle(this.posX, this.posY, this.posZ, var9 * var1, var11 * var1, 0.0D, var4, var5, var6, var7);
         float var13 = this.rand.nextFloat() * 3.1415927F;
         double var14 = var8 ? 0.034D : 0.34D;

         for(int var16 = 0; var16 < 3; ++var16) {
            double var17 = (double)var13 + (double)((float)var16 * 3.1415927F) * var14;
            double var19 = var9;
            double var21 = var11;

            for(int var23 = 1; var23 < var3.length; ++var23) {
               double var24 = var3[var23][0];
               double var26 = var3[var23][1];

               for(double var28 = 0.25D; var28 <= 1.0D; var28 += 0.25D) {
                  double var30 = (var19 + (var24 - var19) * var28) * var1;
                  double var32 = (var21 + (var26 - var21) * var28) * var1;
                  double var34 = var30 * Math.sin(var17);
                  var30 = var30 * Math.cos(var17);

                  for(double var36 = -1.0D; var36 <= 1.0D; var36 += 2.0D) {
                     this.createParticle(this.posX, this.posY, this.posZ, var30 * var36, var32, var34 * var36, var4, var5, var6, var7);
                  }
               }

               var19 = var24;
               var21 = var26;
            }
         }

      }

      private void createBurst(int[] var1, int[] var2, boolean var3, boolean var4) {
         double var5 = this.rand.nextGaussian() * 0.05D;
         double var7 = this.rand.nextGaussian() * 0.05D;

         for(int var9 = 0; var9 < 70; ++var9) {
            double var10 = this.motionX * 0.5D + this.rand.nextGaussian() * 0.15D + var5;
            double var12 = this.motionZ * 0.5D + this.rand.nextGaussian() * 0.15D + var7;
            double var14 = this.motionY * 0.5D + this.rand.nextDouble() * 0.5D;
            this.createParticle(this.posX, this.posY, this.posZ, var10, var14, var12, var1, var2, var3, var4);
         }

      }

      public int getFXLayer() {
         return 0;
      }
   }
}
