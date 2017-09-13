package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityItem extends Render {
   private final RenderItem itemRenderer;
   private final Random random = new Random();

   public RenderEntityItem(RenderManager var1, RenderItem var2) {
      super(var1);
      this.itemRenderer = var2;
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   private int transformModelCount(EntityItem var1, double var2, double var4, double var6, float var8, IBakedModel var9) {
      ItemStack var10 = var1.getEntityItem();
      Item var11 = var10.getItem();
      if (var11 == null) {
         return 0;
      } else {
         boolean var12 = var9.isGui3d();
         int var13 = this.getModelCount(var10);
         float var14 = 0.25F;
         float var15 = this.shouldBob() ? MathHelper.sin(((float)var1.getAge() + var8) / 10.0F + var1.hoverStart) * 0.1F + 0.1F : 0.0F;
         float var16 = var9.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
         GlStateManager.translate((float)var2, (float)var4 + var15 + 0.25F * var16, (float)var6);
         if (var12 || this.renderManager.options != null) {
            float var17 = (((float)var1.getAge() + var8) / 20.0F + var1.hoverStart) * 57.295776F;
            GlStateManager.rotate(var17, 0.0F, 1.0F, 0.0F);
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         return var13;
      }
   }

   protected int getModelCount(ItemStack var1) {
      byte var2 = 1;
      if (var1.stackSize > 48) {
         var2 = 5;
      } else if (var1.stackSize > 32) {
         var2 = 4;
      } else if (var1.stackSize > 16) {
         var2 = 3;
      } else if (var1.stackSize > 1) {
         var2 = 2;
      }

      return var2;
   }

   public void doRender(EntityItem var1, double var2, double var4, double var6, float var8, float var9) {
      ItemStack var10 = var1.getEntityItem();
      int var11;
      if (var10 != null && var10.getItem() != null) {
         var11 = Item.getIdFromItem(var10.getItem()) + var10.getMetadata();
      } else {
         var11 = 187;
      }

      this.random.setSeed((long)var11);
      boolean var12 = false;
      if (this.bindEntityTexture(var1)) {
         this.renderManager.renderEngine.getTexture(this.getEntityTexture(var1)).setBlurMipmap(false, false);
         var12 = true;
      }

      GlStateManager.enableRescaleNormal();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableBlend();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.pushMatrix();
      IBakedModel var13 = this.itemRenderer.getItemModelWithOverrides(var10, var1.world, (EntityLivingBase)null);
      int var14 = this.transformModelCount(var1, var2, var4, var6, var9, var13);
      boolean var15 = var13.isGui3d();
      if (!var15) {
         float var16 = -0.0F * (float)(var14 - 1) * 0.5F;
         float var17 = -0.0F * (float)(var14 - 1) * 0.5F;
         float var18 = -0.09375F * (float)(var14 - 1) * 0.5F;
         GlStateManager.translate(var16, var17, var18);
      }

      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      for(int var20 = 0; var20 < var14; ++var20) {
         if (var15) {
            GlStateManager.pushMatrix();
            if (var20 > 0) {
               float var21 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float var23 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float var19 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               GlStateManager.translate(this.shouldSpreadItems() ? var21 : 0.0F, this.shouldSpreadItems() ? var23 : 0.0F, var19);
            }

            var13 = ForgeHooksClient.handleCameraTransforms(var13, ItemCameraTransforms.TransformType.GROUND, false);
            this.itemRenderer.renderItem(var10, var13);
            GlStateManager.popMatrix();
         } else {
            GlStateManager.pushMatrix();
            if (var20 > 0) {
               float var22 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float var24 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               GlStateManager.translate(var22, var24, 0.0F);
            }

            var13 = ForgeHooksClient.handleCameraTransforms(var13, ItemCameraTransforms.TransformType.GROUND, false);
            this.itemRenderer.renderItem(var10, var13);
            GlStateManager.popMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.09375F);
         }
      }

      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.popMatrix();
      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      this.bindEntityTexture(var1);
      if (var12) {
         this.renderManager.renderEngine.getTexture(this.getEntityTexture(var1)).restoreLastBlurMipmap();
      }

      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityItem var1) {
      return TextureMap.LOCATION_BLOCKS_TEXTURE;
   }

   public boolean shouldSpreadItems() {
      return true;
   }

   public boolean shouldBob() {
      return true;
   }
}
