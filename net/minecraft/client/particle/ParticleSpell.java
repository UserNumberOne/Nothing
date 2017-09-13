package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSpell extends Particle {
   private static final Random RANDOM = new Random();
   private int baseSpellTextureIndex = 128;

   protected ParticleSpell(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, 0.5D - RANDOM.nextDouble(), var10, 0.5D - RANDOM.nextDouble());
      this.motionY *= 0.20000000298023224D;
      if (var8 == 0.0D && var12 == 0.0D) {
         this.motionX *= 0.10000000149011612D;
         this.motionZ *= 0.10000000149011612D;
      }

      this.particleScale *= 0.75F;
      this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
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

      this.setParticleTextureIndex(this.baseSpellTextureIndex + (7 - this.particleAge * 8 / this.particleMaxAge));
      this.motionY += 0.004D;
      this.move(this.motionX, this.motionY, this.motionZ);
      if (this.posY == this.prevPosY) {
         this.motionX *= 1.1D;
         this.motionZ *= 1.1D;
      }

      this.motionX *= 0.9599999785423279D;
      this.motionY *= 0.9599999785423279D;
      this.motionZ *= 0.9599999785423279D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   public void setBaseSpellTextureIndex(int var1) {
      this.baseSpellTextureIndex = var1;
   }

   @SideOnly(Side.CLIENT)
   public static class AmbientMobFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleSpell var16 = new ParticleSpell(var2, var3, var5, var7, var9, var11, var13);
         var16.setAlphaF(0.15F);
         var16.setRBGColorF((float)var9, (float)var11, (float)var13);
         return var16;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleSpell(var2, var3, var5, var7, var9, var11, var13);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class InstantFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleSpell var16 = new ParticleSpell(var2, var3, var5, var7, var9, var11, var13);
         ((ParticleSpell)var16).setBaseSpellTextureIndex(144);
         return var16;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class MobFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleSpell var16 = new ParticleSpell(var2, var3, var5, var7, var9, var11, var13);
         var16.setRBGColorF((float)var9, (float)var11, (float)var13);
         return var16;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class WitchFactory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         ParticleSpell var16 = new ParticleSpell(var2, var3, var5, var7, var9, var11, var13);
         ((ParticleSpell)var16).setBaseSpellTextureIndex(144);
         float var17 = var2.rand.nextFloat() * 0.5F + 0.35F;
         var16.setRBGColorF(1.0F * var17, 0.0F * var17, 1.0F * var17);
         return var16;
      }
   }
}
