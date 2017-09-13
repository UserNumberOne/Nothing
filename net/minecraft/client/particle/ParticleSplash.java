package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSplash extends ParticleRain {
   protected ParticleSplash(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn);
      this.particleGravity = 0.04F;
      this.nextTextureIndexX();
      if (ySpeedIn == 0.0D && (xSpeedIn != 0.0D || zSpeedIn != 0.0D)) {
         this.motionX = xSpeedIn;
         this.motionY = ySpeedIn + 0.1D;
         this.motionZ = zSpeedIn;
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleSplash(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
