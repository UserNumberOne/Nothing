package net.minecraft.client.gui;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec4b;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MapItemRenderer {
   private static final ResourceLocation TEXTURE_MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");
   private final TextureManager textureManager;
   private final Map loadedMaps = Maps.newHashMap();

   public MapItemRenderer(TextureManager var1) {
      this.textureManager = var1;
   }

   public void updateMapTexture(MapData var1) {
      this.getMapRendererInstance(var1).updateMapTexture();
   }

   public void renderMap(MapData var1, boolean var2) {
      this.getMapRendererInstance(var1).render(var2);
   }

   private MapItemRenderer.Instance getMapRendererInstance(MapData var1) {
      MapItemRenderer.Instance var2 = (MapItemRenderer.Instance)this.loadedMaps.get(var1.mapName);
      if (var2 == null) {
         var2 = new MapItemRenderer.Instance(var1);
         this.loadedMaps.put(var1.mapName, var2);
      }

      return var2;
   }

   public void clearLoadedMaps() {
      for(MapItemRenderer.Instance var2 : this.loadedMaps.values()) {
         this.textureManager.deleteTexture(var2.location);
      }

      this.loadedMaps.clear();
   }

   @SideOnly(Side.CLIENT)
   class Instance {
      private final MapData mapData;
      private final DynamicTexture mapTexture;
      private final ResourceLocation location;
      private final int[] mapTextureData;

      private Instance(MapData var2) {
         this.mapData = var2;
         this.mapTexture = new DynamicTexture(128, 128);
         this.mapTextureData = this.mapTexture.getTextureData();
         this.location = MapItemRenderer.this.textureManager.getDynamicTextureLocation("map/" + var2.mapName, this.mapTexture);

         for(int var3 = 0; var3 < this.mapTextureData.length; ++var3) {
            this.mapTextureData[var3] = 0;
         }

      }

      private void updateMapTexture() {
         for(int var1 = 0; var1 < 16384; ++var1) {
            int var2 = this.mapData.colors[var1] & 255;
            if (var2 / 4 == 0) {
               this.mapTextureData[var1] = (var1 + var1 / 128 & 1) * 8 + 16 << 24;
            } else {
               this.mapTextureData[var1] = MapColor.COLORS[var2 / 4].getMapColor(var2 & 3);
            }
         }

         this.mapTexture.updateDynamicTexture();
      }

      private void render(boolean var1) {
         boolean var2 = false;
         boolean var3 = false;
         Tessellator var4 = Tessellator.getInstance();
         VertexBuffer var5 = var4.getBuffer();
         float var6 = 0.0F;
         MapItemRenderer.this.textureManager.bindTexture(this.location);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
         GlStateManager.disableAlpha();
         var5.begin(7, DefaultVertexFormats.POSITION_TEX);
         var5.pos(0.0D, 128.0D, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
         var5.pos(128.0D, 128.0D, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
         var5.pos(128.0D, 0.0D, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
         var5.pos(0.0D, 0.0D, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
         var4.draw();
         GlStateManager.enableAlpha();
         GlStateManager.disableBlend();
         MapItemRenderer.this.textureManager.bindTexture(MapItemRenderer.TEXTURE_MAP_ICONS);
         int var7 = 0;

         for(Vec4b var9 : this.mapData.mapDecorations.values()) {
            if (!var1 || var9.getType() == 1) {
               GlStateManager.pushMatrix();
               GlStateManager.translate(0.0F + (float)var9.getX() / 2.0F + 64.0F, 0.0F + (float)var9.getY() / 2.0F + 64.0F, -0.02F);
               GlStateManager.rotate((float)(var9.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
               GlStateManager.scale(4.0F, 4.0F, 3.0F);
               GlStateManager.translate(-0.125F, 0.125F, 0.0F);
               byte var10 = var9.getType();
               float var11 = (float)(var10 % 4 + 0) / 4.0F;
               float var12 = (float)(var10 / 4 + 0) / 4.0F;
               float var13 = (float)(var10 % 4 + 1) / 4.0F;
               float var14 = (float)(var10 / 4 + 1) / 4.0F;
               var5.begin(7, DefaultVertexFormats.POSITION_TEX);
               float var15 = -0.001F;
               var5.pos(-1.0D, 1.0D, (double)((float)var7 * -0.001F)).tex((double)var11, (double)var12).endVertex();
               var5.pos(1.0D, 1.0D, (double)((float)var7 * -0.001F)).tex((double)var13, (double)var12).endVertex();
               var5.pos(1.0D, -1.0D, (double)((float)var7 * -0.001F)).tex((double)var13, (double)var14).endVertex();
               var5.pos(-1.0D, -1.0D, (double)((float)var7 * -0.001F)).tex((double)var11, (double)var14).endVertex();
               var4.draw();
               GlStateManager.popMatrix();
               ++var7;
            }
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.0F, -0.04F);
         GlStateManager.scale(1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }
   }
}
