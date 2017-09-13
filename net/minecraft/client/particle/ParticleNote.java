package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleNote extends Particle {
   float noteParticleScale;

   protected ParticleNote(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      this(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i46353_8_, p_i46353_10_, p_i46353_12_, 2.0F);
   }

   protected ParticleNote(World var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
      this.motionX *= 0.009999999776482582D;
      this.motionY *= 0.009999999776482582D;
      this.motionZ *= 0.009999999776482582D;
      this.motionY += 0.2D;
      this.particleRed = MathHelper.sin(((float)p_i1217_8_ + 0.0F) * 6.2831855F) * 0.65F + 0.35F;
      this.particleGreen = MathHelper.sin(((float)p_i1217_8_ + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F;
      this.particleBlue = MathHelper.sin(((float)p_i1217_8_ + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F;
      this.particleScale *= 0.75F;
      this.particleScale *= p_i1217_14_;
      this.noteParticleScale = this.particleScale;
      this.particleMaxAge = 6;
      this.setParticleTextureIndex(64);
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      this.particleScale = this.noteParticleScale * f;
      super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      if (this.posY == this.prevPosY) {
         this.motionX *= 1.1D;
         this.motionZ *= 1.1D;
      }

      this.motionX *= 0.6600000262260437D;
      this.motionY *= 0.6600000262260437D;
      this.motionZ *= 0.6600000262260437D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleNote(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
