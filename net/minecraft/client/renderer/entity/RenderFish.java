package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFish extends Render {
   private static final ResourceLocation FISH_PARTICLES = new ResourceLocation("textures/particle/particles.png");

   public RenderFish(RenderManager var1) {
      super(var1);
   }

   public void doRender(EntityFishHook var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var2, (float)var4, (float)var6);
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      this.bindEntityTexture(var1);
      Tessellator var10 = Tessellator.getInstance();
      VertexBuffer var11 = var10.getBuffer();
      boolean var12 = true;
      boolean var13 = true;
      float var14 = 0.0625F;
      float var15 = 0.125F;
      float var16 = 0.125F;
      float var17 = 0.1875F;
      float var18 = 1.0F;
      float var19 = 0.5F;
      float var20 = 0.5F;
      GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      var11.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
      var11.pos(-0.5D, -0.5D, 0.0D).tex(0.0625D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
      var11.pos(0.5D, -0.5D, 0.0D).tex(0.125D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
      var11.pos(0.5D, 0.5D, 0.0D).tex(0.125D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
      var11.pos(-0.5D, 0.5D, 0.0D).tex(0.0625D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
      var10.draw();
      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
      if (var1.angler != null && !this.renderOutlines) {
         int var21 = var1.angler.getPrimaryHand() == EnumHandSide.RIGHT ? 1 : -1;
         float var22 = var1.angler.getSwingProgress(var9);
         float var23 = MathHelper.sin(MathHelper.sqrt(var22) * 3.1415927F);
         float var24 = (var1.angler.prevRenderYawOffset + (var1.angler.renderYawOffset - var1.angler.prevRenderYawOffset) * var9) * 0.017453292F;
         double var25 = (double)MathHelper.sin(var24);
         double var27 = (double)MathHelper.cos(var24);
         double var29 = (double)var21 * 0.35D;
         double var31 = 0.8D;
         double var33;
         double var35;
         double var37;
         double var39;
         if ((this.renderManager.options == null || this.renderManager.options.thirdPersonView <= 0) && var1.angler == Minecraft.getMinecraft().player) {
            Vec3d var41 = new Vec3d((double)var21 * -0.36D, -0.05D, 0.4D);
            var41 = var41.rotatePitch(-(var1.angler.prevRotationPitch + (var1.angler.rotationPitch - var1.angler.prevRotationPitch) * var9) * 0.017453292F);
            var41 = var41.rotateYaw(-(var1.angler.prevRotationYaw + (var1.angler.rotationYaw - var1.angler.prevRotationYaw) * var9) * 0.017453292F);
            var41 = var41.rotateYaw(var23 * 0.5F);
            var41 = var41.rotatePitch(-var23 * 0.7F);
            var33 = var1.angler.prevPosX + (var1.angler.posX - var1.angler.prevPosX) * (double)var9 + var41.xCoord;
            var35 = var1.angler.prevPosY + (var1.angler.posY - var1.angler.prevPosY) * (double)var9 + var41.yCoord;
            var37 = var1.angler.prevPosZ + (var1.angler.posZ - var1.angler.prevPosZ) * (double)var9 + var41.zCoord;
            var39 = (double)var1.angler.getEyeHeight();
         } else {
            var33 = var1.angler.prevPosX + (var1.angler.posX - var1.angler.prevPosX) * (double)var9 - var27 * var29 - var25 * 0.8D;
            var35 = var1.angler.prevPosY + (double)var1.angler.getEyeHeight() + (var1.angler.posY - var1.angler.prevPosY) * (double)var9 - 0.45D;
            var37 = var1.angler.prevPosZ + (var1.angler.posZ - var1.angler.prevPosZ) * (double)var9 - var25 * var29 + var27 * 0.8D;
            var39 = var1.angler.isSneaking() ? -0.1875D : 0.0D;
         }

         double var60 = var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var9;
         double var43 = var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var9 + 0.25D;
         double var45 = var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var9;
         double var47 = (double)((float)(var33 - var60));
         double var49 = (double)((float)(var35 - var43)) + var39;
         double var51 = (double)((float)(var37 - var45));
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         var11.begin(3, DefaultVertexFormats.POSITION_COLOR);
         boolean var53 = true;

         for(int var54 = 0; var54 <= 16; ++var54) {
            float var55 = (float)var54 / 16.0F;
            var11.pos(var2 + var47 * (double)var55, var4 + var49 * (double)(var55 * var55 + var55) * 0.5D + 0.25D, var6 + var51 * (double)var55).color(0, 0, 0, 255).endVertex();
         }

         var10.draw();
         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
         super.doRender(var1, var2, var4, var6, var8, var9);
      }

   }

   protected ResourceLocation getEntityTexture(EntityFishHook var1) {
      return FISH_PARTICLES;
   }
}
