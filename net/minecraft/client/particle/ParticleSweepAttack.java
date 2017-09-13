package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSweepAttack extends Particle {
   private static final ResourceLocation SWEEP_TEXTURE = new ResourceLocation("textures/entity/sweep.png");
   private static final VertexFormat VERTEX_FORMAT = (new VertexFormat()).addElement(DefaultVertexFormats.POSITION_3F).addElement(DefaultVertexFormats.TEX_2F).addElement(DefaultVertexFormats.COLOR_4UB).addElement(DefaultVertexFormats.TEX_2S).addElement(DefaultVertexFormats.NORMAL_3B).addElement(DefaultVertexFormats.PADDING_1B);
   private int life;
   private final int lifeTime;
   private final TextureManager textureManager;
   private final float size;

   protected ParticleSweepAttack(TextureManager var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13) {
      super(var2, var3, var5, var7, 0.0D, 0.0D, 0.0D);
      this.textureManager = var1;
      this.lifeTime = 4;
      float var15 = this.rand.nextFloat() * 0.6F + 0.4F;
      this.particleRed = var15;
      this.particleGreen = var15;
      this.particleBlue = var15;
      this.size = 1.0F - (float)var9 * 0.5F;
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      int var9 = (int)(((float)this.life + var3) * 3.0F / (float)this.lifeTime);
      if (var9 <= 7) {
         this.textureManager.bindTexture(SWEEP_TEXTURE);
         float var10 = (float)(var9 % 4) / 4.0F;
         float var11 = var10 + 0.24975F;
         float var12 = (float)(var9 / 2) / 2.0F;
         float var13 = var12 + 0.4995F;
         float var14 = 1.0F * this.size;
         float var15 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)var3 - interpPosX);
         float var16 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)var3 - interpPosY);
         float var17 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var3 - interpPosZ);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableLighting();
         RenderHelper.disableStandardItemLighting();
         var1.begin(7, VERTEX_FORMAT);
         var1.pos((double)(var15 - var4 * var14 - var7 * var14), (double)(var16 - var5 * var14 * 0.5F), (double)(var17 - var6 * var14 - var8 * var14)).tex((double)var11, (double)var13).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
         var1.pos((double)(var15 - var4 * var14 + var7 * var14), (double)(var16 + var5 * var14 * 0.5F), (double)(var17 - var6 * var14 + var8 * var14)).tex((double)var11, (double)var12).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
         var1.pos((double)(var15 + var4 * var14 + var7 * var14), (double)(var16 + var5 * var14 * 0.5F), (double)(var17 + var6 * var14 + var8 * var14)).tex((double)var10, (double)var12).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
         var1.pos((double)(var15 + var4 * var14 - var7 * var14), (double)(var16 - var5 * var14 * 0.5F), (double)(var17 + var6 * var14 - var8 * var14)).tex((double)var10, (double)var13).color(this.particleRed, this.particleGreen, this.particleBlue, 1.0F).lightmap(0, 240).normal(0.0F, 1.0F, 0.0F).endVertex();
         Tessellator.getInstance().draw();
         GlStateManager.enableLighting();
      }

   }

   public int getBrightnessForRender(float var1) {
      return 61680;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      ++this.life;
      if (this.life == this.lifeTime) {
         this.setExpired();
      }

   }

   public int getFXLayer() {
      return 3;
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleSweepAttack(Minecraft.getMinecraft().getTextureManager(), var2, var3, var5, var7, var9, var11, var13);
      }
   }
}
