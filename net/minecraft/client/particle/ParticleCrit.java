package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCrit extends Particle {
   float oSize;

   protected ParticleCrit(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      this(var1, var2, var4, var6, var8, var10, var12, 1.0F);
   }

   protected ParticleCrit(World var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
      this.motionX *= 0.10000000149011612D;
      this.motionY *= 0.10000000149011612D;
      this.motionZ *= 0.10000000149011612D;
      this.motionX += var8 * 0.4D;
      this.motionY += var10 * 0.4D;
      this.motionZ += var12 * 0.4D;
      float var15 = (float)(Math.random() * 0.30000001192092896D + 0.6000000238418579D);
      this.particleRed = var15;
      this.particleGreen = var15;
      this.particleBlue = var15;
      this.particleScale *= 0.75F;
      this.particleScale *= var14;
      this.oSize = this.particleScale;
      this.particleMaxAge = (int)(6.0D / (Math.random() * 0.8D + 0.6D));
      this.particleMaxAge = (int)((float)this.particleMaxAge * var14);
      this.setParticleTextureIndex(65);
      this.onUpdate();
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = ((float)this.particleAge + var3) / (float)this.particleMaxAge * 32.0F;
      var9 = MathHelper.clamp(var9, 0.0F, 1.0F);
      this.particleScale = this.oSize * var9;
      super.renderParticle(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      this.particleGreen = (float)((double)this.particleGreen * 0.96D);
      this.particleBlue = (float)((double)this.particleBlue * 0.9D);
      this.motionX *= 0.699999988079071D;
      this.motionY *= 0.699999988079071D;
      this.motionZ *= 0.699999988079071D;
      this.motionY -= 0.019999999552965164D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   @SideOnly(Side.CLIENT)
   public static class DamageIndicatorFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleCrit var16 = new ParticleCrit(var2, var3, var5, var7, var9, var11 + 1.0D, var13, 1.0F);
         var16.setMaxAge(20);
         var16.setParticleTextureIndex(67);
         return var16;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleCrit(var2, var3, var5, var7, var9, var11, var13);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class MagicFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleCrit var16 = new ParticleCrit(var2, var3, var5, var7, var9, var11, var13);
         var16.setRBGColorF(var16.getRedColorF() * 0.3F, var16.getGreenColorF() * 0.8F, var16.getBlueColorF());
         var16.nextTextureIndexX();
         return var16;
      }
   }
}
