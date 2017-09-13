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
      this.renderer = rendererIn;
      this.initArmor();
   }

   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
      this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
      this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
      this.renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
   }

   public boolean shouldCombineTextures() {
      return false;
   }

   private void renderArmorLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, EntityEquipmentSlot var9) {
      ItemStack itemstack = this.getItemStackFromSlot(entityLivingBaseIn, slotIn);
      if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
         ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
         if (itemarmor.getEquipmentSlot() == slotIn) {
            ModelBase t = (T)this.getModelFromSlot(slotIn);
            t = (T)this.getArmorModelHook(entityLivingBaseIn, itemstack, slotIn, t);
            t.setModelAttributes(this.renderer.getMainModel());
            t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);
            this.setModelSlotVisible(t, slotIn);
            this.isLegSlot(slotIn);
            this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, (String)null));
            if (itemarmor.hasOverlay(itemstack)) {
               int i = itemarmor.getColor(itemstack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               GlStateManager.color(this.colorR * f, this.colorG * f1, this.colorB * f2, this.alpha);
               t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
               this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, "overlay"));
            }

            GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
            t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (!this.skipRenderGlint && itemstack.hasEffect()) {
               renderEnchantedGlint(this.renderer, entityLivingBaseIn, t, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
         }
      }

   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityLivingBase var1, EntityEquipmentSlot var2) {
      return living.getItemStackFromSlot(slotIn);
   }

   public ModelBase getModelFromSlot(EntityEquipmentSlot var1) {
      return this.isLegSlot(slotIn) ? this.modelLeggings : this.modelArmor;
   }

   private boolean isLegSlot(EntityEquipmentSlot var1) {
      return slotIn == EntityEquipmentSlot.LEGS;
   }

   public static void renderEnchantedGlint(RenderLivingBase var0, EntityLivingBase var1, ModelBase var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      float f = (float)p_188364_1_.ticksExisted + p_188364_5_;
      p_188364_0_.bindTexture(ENCHANTED_ITEM_GLINT_RES);
      GlStateManager.enableBlend();
      GlStateManager.depthFunc(514);
      GlStateManager.depthMask(false);
      float f1 = 0.5F;
      GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

      for(int i = 0; i < 2; ++i) {
         GlStateManager.disableLighting();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
         float f2 = 0.76F;
         GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
         GlStateManager.matrixMode(5890);
         GlStateManager.loadIdentity();
         float f3 = 0.33333334F;
         GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
         GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
         GlStateManager.matrixMode(5888);
         model.render(p_188364_1_, p_188364_3_, p_188364_4_, p_188364_6_, p_188364_7_, p_188364_8_, p_188364_9_);
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
      return this.getArmorResource(armor, p_177181_2_, (String)null);
   }

   /** @deprecated */
   @Deprecated
   private ResourceLocation getArmorResource(ItemArmor var1, boolean var2, String var3) {
      String s = String.format("textures/models/armor/%s_layer_%d%s.png", armor.getArmorMaterial().getName(), p_177178_2_ ? 2 : 1, p_177178_3_ == null ? "" : String.format("_%s", p_177178_3_));
      ResourceLocation resourcelocation = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(s);
      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s);
         ARMOR_TEXTURE_RES_MAP.put(s, resourcelocation);
      }

      return resourcelocation;
   }

   protected abstract void initArmor();

   protected abstract void setModelSlotVisible(ModelBase var1, EntityEquipmentSlot var2);

   protected ModelBase getArmorModelHook(EntityLivingBase var1, ItemStack var2, EntityEquipmentSlot var3, ModelBase var4) {
      return model;
   }

   public ResourceLocation getArmorResource(Entity var1, ItemStack var2, EntityEquipmentSlot var3, String var4) {
      ItemArmor item = (ItemArmor)stack.getItem();
      String texture = item.getArmorMaterial().getName();
      String domain = "minecraft";
      int idx = texture.indexOf(58);
      if (idx != -1) {
         domain = texture.substring(0, idx);
         texture = texture.substring(idx + 1);
      }

      String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, this.isLegSlot(slot) ? 2 : 1, type == null ? "" : String.format("_%s", type));
      s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
      ResourceLocation resourcelocation = (ResourceLocation)ARMOR_TEXTURE_RES_MAP.get(s1);
      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s1);
         ARMOR_TEXTURE_RES_MAP.put(s1, resourcelocation);
      }

      return resourcelocation;
   }
}
