package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleMobAppearance extends Particle {
   private EntityLivingBase entity;

   protected ParticleMobAppearance(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.particleGravity = 0.0F;
      this.particleMaxAge = 30;
   }

   public int getFXLayer() {
      return 3;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.entity == null) {
         EntityGuardian var1 = new EntityGuardian(this.world);
         var1.setElder();
         this.entity = var1;
      }

   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (this.entity != null) {
         RenderManager var9 = Minecraft.getMinecraft().getRenderManager();
         var9.setRenderPosition(Particle.interpPosX, Particle.interpPosY, Particle.interpPosZ);
         float var10 = 0.42553192F;
         float var11 = ((float)this.particleAge + var3) / (float)this.particleMaxAge;
         GlStateManager.depthMask(true);
         GlStateManager.enableBlend();
         GlStateManager.enableDepth();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         float var12 = 240.0F;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.pushMatrix();
         float var13 = 0.05F + 0.5F * MathHelper.sin(var11 * 3.1415927F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, var13);
         GlStateManager.translate(0.0F, 1.8F, 0.0F);
         GlStateManager.rotate(180.0F - var2.rotationYaw, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(60.0F - 150.0F * var11 - var2.rotationPitch, 1.0F, 0.0F, 0.0F);
         GlStateManager.translate(0.0F, -0.4F, -1.5F);
         GlStateManager.scale(0.42553192F, 0.42553192F, 0.42553192F);
         this.entity.rotationYaw = 0.0F;
         this.entity.rotationYawHead = 0.0F;
         this.entity.prevRotationYaw = 0.0F;
         this.entity.prevRotationYawHead = 0.0F;
         var9.doRenderEntity(this.entity, 0.0D, 0.0D, 0.0D, 0.0F, var3, false);
         GlStateManager.popMatrix();
         GlStateManager.enableDepth();
      }

   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleMobAppearance(var2, var3, var5, var7);
      }
   }
}
