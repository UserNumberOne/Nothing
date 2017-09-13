package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleFootStep extends Particle {
   private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
   private int footstepAge;
   private final int footstepMaxAge;
   private final TextureManager currentFootSteps;

   protected ParticleFootStep(TextureManager var1, World var2, double var3, double var5, double var7) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
      this.currentFootSteps = currentFootStepsIn;
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.footstepMaxAge = 200;
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float f = ((float)this.footstepAge + partialTicks) / (float)this.footstepMaxAge;
      f = f * f;
      float f1 = 2.0F - f * 2.0F;
      if (f1 > 1.0F) {
         f1 = 1.0F;
      }

      f1 = f1 * 0.2F;
      GlStateManager.disableLighting();
      float f2 = 0.125F;
      float f3 = (float)(this.posX - interpPosX);
      float f4 = (float)(this.posY - interpPosY);
      float f5 = (float)(this.posZ - interpPosZ);
      float f6 = this.world.getLightBrightness(new BlockPos(this.posX, this.posY, this.posZ));
      this.currentFootSteps.bindTexture(FOOTPRINT_TEXTURE);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos((double)(f3 - 0.125F), (double)f4, (double)(f5 + 0.125F)).tex(0.0D, 1.0D).color(f6, f6, f6, f1).endVertex();
      buffer.pos((double)(f3 + 0.125F), (double)f4, (double)(f5 + 0.125F)).tex(1.0D, 1.0D).color(f6, f6, f6, f1).endVertex();
      buffer.pos((double)(f3 + 0.125F), (double)f4, (double)(f5 - 0.125F)).tex(1.0D, 0.0D).color(f6, f6, f6, f1).endVertex();
      buffer.pos((double)(f3 - 0.125F), (double)f4, (double)(f5 - 0.125F)).tex(0.0D, 0.0D).color(f6, f6, f6, f1).endVertex();
      Tessellator.getInstance().draw();
      GlStateManager.disableBlend();
      GlStateManager.enableLighting();
   }

   public void onUpdate() {
      ++this.footstepAge;
      if (this.footstepAge == this.footstepMaxAge) {
         this.setExpired();
      }

   }

   public int getFXLayer() {
      return 3;
   }

   @SideOnly(Side.CLIENT)
   public static class Factory implements IParticleFactory {
      public Particle createParticle(int var1, World var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
         return new ParticleFootStep(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn);
      }
   }
}
