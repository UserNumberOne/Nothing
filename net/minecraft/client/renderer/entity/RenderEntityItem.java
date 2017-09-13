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
      super(renderManagerIn);
      this.itemRenderer = p_i46167_2_;
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   private int transformModelCount(EntityItem var1, double var2, double var4, double var6, float var8, IBakedModel var9) {
      ItemStack itemstack = itemIn.getEntityItem();
      Item item = itemstack.getItem();
      if (item == null) {
         return 0;
      } else {
         boolean flag = p_177077_9_.isGui3d();
         int i = this.getModelCount(itemstack);
         float f = 0.25F;
         float f1 = this.shouldBob() ? MathHelper.sin(((float)itemIn.getAge() + p_177077_8_) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F : 0.0F;
         float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
         GlStateManager.translate((float)p_177077_2_, (float)p_177077_4_ + f1 + 0.25F * f2, (float)p_177077_6_);
         if (flag || this.renderManager.options != null) {
            float f3 = (((float)itemIn.getAge() + p_177077_8_) / 20.0F + itemIn.hoverStart) * 57.295776F;
            GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         return i;
      }
   }

   protected int getModelCount(ItemStack var1) {
      int i = 1;
      if (stack.stackSize > 48) {
         i = 5;
      } else if (stack.stackSize > 32) {
         i = 4;
      } else if (stack.stackSize > 16) {
         i = 3;
      } else if (stack.stackSize > 1) {
         i = 2;
      }

      return i;
   }

   public void doRender(EntityItem var1, double var2, double var4, double var6, float var8, float var9) {
      ItemStack itemstack = entity.getEntityItem();
      int i;
      if (itemstack != null && itemstack.getItem() != null) {
         i = Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata();
      } else {
         i = 187;
      }

      this.random.setSeed((long)i);
      boolean flag = false;
      if (this.bindEntityTexture(entity)) {
         this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
         flag = true;
      }

      GlStateManager.enableRescaleNormal();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.enableBlend();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.pushMatrix();
      IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, (EntityLivingBase)null);
      int j = this.transformModelCount(entity, x, y, z, partialTicks, ibakedmodel);
      boolean flag1 = ibakedmodel.isGui3d();
      if (!flag1) {
         float f3 = -0.0F * (float)(j - 1) * 0.5F;
         float f4 = -0.0F * (float)(j - 1) * 0.5F;
         float f5 = -0.09375F * (float)(j - 1) * 0.5F;
         GlStateManager.translate(f3, f4, f5);
      }

      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(entity));
      }

      for(int k = 0; k < j; ++k) {
         if (flag1) {
            GlStateManager.pushMatrix();
            if (k > 0) {
               float f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               GlStateManager.translate(this.shouldSpreadItems() ? f7 : 0.0F, this.shouldSpreadItems() ? f9 : 0.0F, f6);
            }

            ibakedmodel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
            this.itemRenderer.renderItem(itemstack, ibakedmodel);
            GlStateManager.popMatrix();
         } else {
            GlStateManager.pushMatrix();
            if (k > 0) {
               float f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               GlStateManager.translate(f8, f10, 0.0F);
            }

            ibakedmodel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
            this.itemRenderer.renderItem(itemstack, ibakedmodel);
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
      this.bindEntityTexture(entity);
      if (flag) {
         this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
      }

      super.doRender(entity, x, y, z, entityYaw, partialTicks);
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
