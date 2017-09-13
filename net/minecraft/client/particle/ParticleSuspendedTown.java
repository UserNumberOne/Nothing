package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSuspendedTown extends Particle {
   protected ParticleSuspendedTown(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      float var14 = this.rand.nextFloat() * 0.1F + 0.2F;
      this.particleRed = var14;
      this.particleGreen = var14;
      this.particleBlue = var14;
      this.setParticleTextureIndex(0);
      this.setSize(0.02F, 0.02F);
      this.particleScale *= this.rand.nextFloat() * 0.6F + 0.5F;
      this.motionX *= 0.019999999552965164D;
      this.motionY *= 0.019999999552965164D;
      this.motionZ *= 0.019999999552965164D;
      this.particleMaxAge = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
   }

   public void move(double var1, double var3, double var5) {
      this.setBoundingBox(this.getBoundingBox().offset(var1, var3, var5));
      this.resetPositionToBB();
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.99D;
      this.motionY *= 0.99D;
      this.motionZ *= 0.99D;
      if (this.particleMaxAge-- <= 0) {
         this.setExpired();
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleSuspendedTown(var2, var3, var5, var7, var9, var11, var13);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class HappyVillagerFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleSuspendedTown var16 = new ParticleSuspendedTown(var2, var3, var5, var7, var9, var11, var13);
         var16.setParticleTextureIndex(82);
         var16.setRBGColorF(1.0F, 1.0F, 1.0F);
         return var16;
      }
   }
}
