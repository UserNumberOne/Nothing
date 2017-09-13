package net.minecraft.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleEndRod extends ParticleSimpleAnimated {
   public ParticleEndRod(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(p_i46580_1_, p_i46580_2_, p_i46580_4_, p_i46580_6_, 176, 8, -5.0E-4F);
      this.motionX = p_i46580_8_;
      this.motionY = p_i46580_10_;
      this.motionZ = p_i46580_12_;
      this.particleScale *= 0.75F;
      this.particleMaxAge = 60 + this.rand.nextInt(12);
      this.setColorFade(15916745);
   }

   public void move(double var1, double var3, double var5) {
      this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
      this.resetPositionToBB();
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleEndRod(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
