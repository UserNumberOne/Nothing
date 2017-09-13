package net.minecraft.client.renderer.tileentity;

import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityEndGatewayRenderer extends TileEntitySpecialRenderer {
   private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
   private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
   private static final ResourceLocation END_GATEWAY_BEAM_TEXTURE = new ResourceLocation("textures/entity/end_gateway_beam.png");
   private static final Random RANDOM = new Random(31100L);
   private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
   private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
   FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

   public void renderTileEntityAt(TileEntityEndGateway var1, double var2, double var4, double var6, float var8, int var9) {
      GlStateManager.disableFog();
      if (var1.isSpawning() || var1.isCoolingDown()) {
         GlStateManager.alphaFunc(516, 0.1F);
         this.bindTexture(END_GATEWAY_BEAM_TEXTURE);
         float var10 = var1.isSpawning() ? var1.getSpawnPercent() : var1.getCooldownPercent();
         double var11 = var1.isSpawning() ? 256.0D - var4 : 25.0D;
         var10 = MathHelper.sin(var10 * 3.1415927F);
         int var13 = MathHelper.floor((double)var10 * var11);
         float[] var14 = EntitySheep.getDyeRgb(var1.isSpawning() ? EnumDyeColor.MAGENTA : EnumDyeColor.YELLOW);
         TileEntityBeaconRenderer.renderBeamSegment(var2, var4, var6, (double)var8, (double)var10, (double)var1.getWorld().getTotalWorldTime(), 0, var13, var14, 0.15D, 0.175D);
         TileEntityBeaconRenderer.renderBeamSegment(var2, var4, var6, (double)var8, (double)var10, (double)var1.getWorld().getTotalWorldTime(), 0, -var13, var14, 0.15D, 0.175D);
      }

      GlStateManager.disableLighting();
      RANDOM.setSeed(31100L);
      GlStateManager.getFloat(2982, MODELVIEW);
      GlStateManager.getFloat(2983, PROJECTION);
      double var22 = var2 * var2 + var4 * var4 + var6 * var6;
      byte var12;
      if (var22 > 36864.0D) {
         var12 = 2;
      } else if (var22 > 25600.0D) {
         var12 = 4;
      } else if (var22 > 16384.0D) {
         var12 = 6;
      } else if (var22 > 9216.0D) {
         var12 = 8;
      } else if (var22 > 4096.0D) {
         var12 = 10;
      } else if (var22 > 1024.0D) {
         var12 = 12;
      } else if (var22 > 576.0D) {
         var12 = 14;
      } else if (var22 > 256.0D) {
         var12 = 15;
      } else {
         var12 = 16;
      }

      for(int var23 = 0; var23 < var12; ++var23) {
         GlStateManager.pushMatrix();
         float var24 = 2.0F / (float)(18 - var23);
         if (var23 == 0) {
            this.bindTexture(END_SKY_TEXTURE);
            var24 = 0.15F;
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         }

         if (var23 >= 1) {
            this.bindTexture(END_PORTAL_TEXTURE);
         }

         if (var23 == 1) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
         }

         GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
         GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
         GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
         GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
         GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
         GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
         GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5890);
         GlStateManager.pushMatrix();
         GlStateManager.loadIdentity();
         GlStateManager.translate(0.5F, 0.5F, 0.0F);
         GlStateManager.scale(0.5F, 0.5F, 1.0F);
         float var15 = (float)(var23 + 1);
         GlStateManager.translate(17.0F / var15, (2.0F + var15 / 1.5F) * ((float)Minecraft.getSystemTime() % 800000.0F / 800000.0F), 0.0F);
         GlStateManager.rotate((var15 * var15 * 4321.0F + var15 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(4.5F - var15 / 4.0F, 4.5F - var15 / 4.0F, 1.0F);
         GlStateManager.multMatrix(PROJECTION);
         GlStateManager.multMatrix(MODELVIEW);
         Tessellator var16 = Tessellator.getInstance();
         VertexBuffer var17 = var16.getBuffer();
         var17.begin(7, DefaultVertexFormats.POSITION_COLOR);
         float var18 = (RANDOM.nextFloat() * 0.5F + 0.1F) * var24;
         float var19 = (RANDOM.nextFloat() * 0.5F + 0.4F) * var24;
         float var20 = (RANDOM.nextFloat() * 0.5F + 0.5F) * var24;
         if (var1.shouldRenderFace(EnumFacing.SOUTH)) {
            var17.pos(var2, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
         }

         if (var1.shouldRenderFace(EnumFacing.NORTH)) {
            var17.pos(var2, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
         }

         if (var1.shouldRenderFace(EnumFacing.EAST)) {
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
         }

         if (var1.shouldRenderFace(EnumFacing.WEST)) {
            var17.pos(var2, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
         }

         if (var1.shouldRenderFace(EnumFacing.DOWN)) {
            var17.pos(var2, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
         }

         if (var1.shouldRenderFace(EnumFacing.UP)) {
            var17.pos(var2, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6 + 1.0D).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2 + 1.0D, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
            var17.pos(var2, var4 + 1.0D, var6).color(var18, var19, var20, 1.0F).endVertex();
         }

         var16.draw();
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
         this.bindTexture(END_SKY_TEXTURE);
      }

      GlStateManager.disableBlend();
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
      GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
      GlStateManager.enableLighting();
      GlStateManager.enableFog();
   }

   private FloatBuffer getBuffer(float var1, float var2, float var3, float var4) {
      this.buffer.clear();
      this.buffer.put(var1).put(var2).put(var3).put(var4);
      this.buffer.flip();
      return this.buffer;
   }

   public boolean isGlobalRenderer(TileEntityEndGateway var1) {
      return var1.isSpawning() || var1.isCoolingDown();
   }
}
