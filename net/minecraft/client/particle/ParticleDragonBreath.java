package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDragonBreath extends Particle {
   private final float oSize;
   private boolean hasHitGround;

   protected ParticleDragonBreath(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
      this.motionX = xSpeed;
      this.motionY = ySpeed;
      this.motionZ = zSpeed;
      this.particleRed = MathHelper.nextFloat(this.rand, 0.7176471F, 0.8745098F);
      this.particleGreen = MathHelper.nextFloat(this.rand, 0.0F, 0.0F);
      this.particleBlue = MathHelper.nextFloat(this.rand, 0.8235294F, 0.9764706F);
      this.particleScale *= 0.75F;
      this.oSize = this.particleScale;
      this.particleMaxAge = (int)(20.0D / ((double)this.rand.nextFloat() * 0.8D + 0.2D));
      this.hasHitGround = false;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      } else {
         this.setParticleTextureIndex(3 * this.particleAge / this.particleMaxAge + 5);
         if (this.isCollided) {
            this.motionY = 0.0D;
            this.hasHitGround = true;
         }

         if (this.hasHitGround) {
            this.motionY += 0.002D;
         }

         this.move(this.motionX, this.motionY, this.motionZ);
         if (this.posY == this.prevPosY) {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
         }

         this.motionX *= 0.9599999785423279D;
         this.motionZ *= 0.9599999785423279D;
         if (this.hasHitGround) {
            this.motionY *= 0.9599999785423279D;
         }
      }

   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.particleScale = this.oSize * MathHelper.clamp(((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F, 0.0F, 1.0F);
      super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleDragonBreath(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
      }
   }
}
