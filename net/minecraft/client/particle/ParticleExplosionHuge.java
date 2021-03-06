package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleExplosionHuge extends Particle {
   private int timeSinceStart;
   private final int maximumTime = 8;

   protected ParticleExplosionHuge(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
   }

   public void onUpdate() {
      for(int var1 = 0; var1 < 6; ++var1) {
         double var2 = this.posX + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
         double var4 = this.posY + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
         double var6 = this.posZ + (this.rand.nextDouble() - this.rand.nextDouble()) * 4.0D;
         World var10000 = this.world;
         EnumParticleTypes var10001 = EnumParticleTypes.EXPLOSION_LARGE;
         float var10005 = (float)this.timeSinceStart;
         this.getClass();
         var10000.spawnParticle(var10001, var2, var4, var6, (double)(var10005 / 8.0F), 0.0D, 0.0D);
      }

      ++this.timeSinceStart;
      int var8 = this.timeSinceStart;
      this.getClass();
      if (var8 == 8) {
         this.setExpired();
      }

   }

   public int getFXLayer() {
      return 1;
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleExplosionHuge(var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
