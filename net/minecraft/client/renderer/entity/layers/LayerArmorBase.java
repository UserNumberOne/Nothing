package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class LayerArmorBase implements LayerRenderer {
   protected static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
   protected ModelBase modelLeggings;
   protected ModelBase modelArmor;
   private final RenderLivingBase renderer;
   private float alpha = 1.0F;
   private float colorR = 1.0F;
   private float colorG = 1.0F;
   private float colorB = 1.0F;
   private boolean skipRenderGlint;
   private static final Map ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();

   public LayerArmorBase(RenderLivingBase var1) {
      this.renderer = var1;
      this.initArmor();
   }

   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.renderArmorLayer(var1, var2, var3, var4, var5, var6, var7, var8, EntityEquipmentSlot.CHEST);
      this.renderArmorLayer(var1, var2, var3, var4, var5, var6, var7, var8, EntityEquipmentSlot.LEGS);
      this.renderArmorLayer(var1, var2, var3, var4, var5, var6, var7, var8, EntityEquipmentSlot.FEET);
      this.renderArmorLayer(var1, var2, var3, var4, var5, var6, var7, var8, EntityEquipmentSlot.HEAD);
   }

   public boolean shouldCombineTextures() {
      return false;
   }

   private void renderArmorLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, EntityEquipmentSlot var9) {
      ItemStack var10 = this.getItemStackFromSlot(var1, var9);
      if (var10 != null && var10.getItem() instanceof ItemArmor) {
         ItemArmor var11 = (ItemArmor)var10.getItem();
         if (var11.getEquipmentSlot() == var9) {
            ModelBase var12 = this.getModelFromSlot(var9);
            var12 = this.getArmorModelHook(var1, var10, var9, var12);
            var12.setModelAttributes(this.renderer.getMainModel());
            var12.setLivingAnimations(var1, var2, var3, var4);
            this.setModelSlotVisible(var12, var9);
            this.isLegSlot(var9);
            this.renderer.bindTexture(this.getArmorResource(var1, var10, var9, (String)null));
            if (var11.hasOverlay(var10)) {
               int var14 = var11.getColor(var10);
               float var15 = (float)(var14 >> 16 & 255) / 255.0F;
               float var16 = (float)(var14 >> 8 & 255) / 255.0F;
               float var17 = (float)(var14 & 255) / 255.0F;
               GlStateManager.color(this.colorR * var15, this.colorG * var16, this.colorB * var17, this.alpha);
               var12.render(var1, var2, var3, var5, var6, var7, var8);
               this.renderer.bindTexture(this.getArmorResource(var1, var10, var9, "overlay"));
            }

            GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
            var12.render(var1, var2, var3, var5, var6, var7, var8);
            if (!this.skipRenderGlint && var10.hasEffect()) {
               renderEnchantedGlint(this.renderer, var1, var12, var2, var3, var4, var5, var6, var7, var8);
            }
         }
      }

   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityLivingBase var1, EntityEquipmentSlot var2) {
      return var1.getItemStackFromSlot(var2);
   }

   public ModelBase getModelFromSlot(EntityEquipmentSlot var1) {
      return this.isLegSlot(var1) ? this.modelLeggings : this.modelArmor;
   }

   private boolean isLegSlot(EntityEquipmentSlot var1) {
      return var1 == EntityEquipmentSlot.LEGS;
   }

   public static void renderEnchantedGlint(RenderLivingBase var0, EntityLivingBase var1, ModelBase var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      float var10 = (float)var1.ticksExisted + var5;
      var0.bindTexture(ENCHANTED_ITEM_GLINT_RES);
      GlStateManager.enableBlend();
      GlStateManager.depthFunc(514);
      GlStateManager.depthMask(false);
      float var11 = 0.5F;
      GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

      for(int var12 = 0; var12 < 2; ++var12) {
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
         float var13 = 0.76F;
         GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         float var14 = 0.33333334F;
         GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
         GlStateManager.rotate(30.0F - (float)var12 * 60.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, var10 * (0.001F + (float)var12 * 0.003F) * 20.0F, 0.0F);
         GlStateManager.matrixMode(5888);
         var2.render(var1, var3, var4, var6, var7, var8, var9);
      }

      GlStateManager.matrixMode(5890);
      GlStateManager.loadIdentity();
      GlStateManager.matrixMode(5888);
      GlStateManager.enableLighting();
      GlStateManager.depthMask(true);
      GlStateManager.depthFunc(515);
      GlStateManager.disableBlend();
   }

   /** @deprecated */
   @Deprecated
   private ResourceLocation getArmorResource(ItemArmor var1, boolean var2) {
      return this.getArmorResource(var1, var2, (String)null);
   }

   /** @deprecated */
   @Deprecated
   private ResourceLocation getArmorResource(ItemArmor var1, boolean var2, String var3) {
      String var4 = String.format("textures/models/armor/%s_layer_%d%s.png", var1.getArmorMaterial().getName(), var2 ? 2 : 1, var3 == null ? "" : String.format("_%s", var3));
      ResourceLocation var5 = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(var4);
      if (var5 == null) {
         var5 = new ResourceLocation(var4);
         ARMOR_TEXTURE_RES_MAP.put(var4, var5);
      }

      return var5;
   }

   protected abstract void initArmor();

   protected abstract void setModelSlotVisible(ModelBase var1, EntityEquipmentSlot var2);

   protected ModelBase getArmorModelHook(EntityLivingBase var1, ItemStack var2, EntityEquipmentSlot var3, ModelBase var4) {
      return var4;
   }

   public ResourceLocation getArmorResource(Entity var1, ItemStack var2, EntityEquipmentSlot var3, String var4) {
      ItemArmor var5 = (ItemArmor)var2.getItem();
      String var6 = var5.getArmorMaterial().getName();
      String var7 = "minecraft";
      int var8 = var6.indexOf(58);
      if (var8 != -1) {
         var7 = var6.substring(0, var8);
         var6 = var6.substring(var8 + 1);
      }

      String var9 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", var7, var6, this.isLegSlot(var3) ? 2 : 1, var4 == null ? "" : String.format("_%s", var4));
      var9 = ForgeHooksClient.getArmorTexture(var1, var2, var9, var3, var4);
      ResourceLocation var10 = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(var9);
      if (var10 == null) {
         var10 = new ResourceLocation(var9);
         ARMOR_TEXTURE_RES_MAP.put(var9, var10);
      }

      return var10;
   }
}
