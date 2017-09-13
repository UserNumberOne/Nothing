package net.minecraft.client.particle;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleCloud extends Particle {
   float oSize;

   protected ParticleCloud(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
      float var14 = 2.5F;
      this.motionX *= 0.10000000149011612D;
      this.motionY *= 0.10000000149011612D;
      this.motionZ *= 0.10000000149011612D;
      this.motionX += var8;
      this.motionY += var10;
      this.motionZ += var12;
      float var15 = 1.0F - (float)(Math.random() * 0.30000001192092896D);
      this.particleRed = var15;
      this.particleGreen = var15;
      this.particleBlue = var15;
      this.particleScale *= 0.75F;
      this.particleScale *= 2.5F;
      this.oSize = this.particleScale;
      this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.3D));
      this.particleMaxAge = (int)((float)this.particleMaxAge * 2.5F);
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

      this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9599999785423279D;
      this.motionY *= 0.9599999785423279D;
      this.motionZ *= 0.9599999785423279D;
      EntityPlayer var1 = this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 2.0D, false);
      if (var1 != null) {
         AxisAlignedBB var2 = var1.getEntityBoundingBox();
         if (this.posY > var2.minY) {
            this.posY += (var2.minY - this.posY) * 0.2D;
            this.motionY += (var1.motionY - this.motionY) * 0.2D;
            this.setPosition(this.posX, this.posY, this.posZ);
         }
      }

      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleCloud(var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
