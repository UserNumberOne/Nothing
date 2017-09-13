package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelArmorStand;
import net.minecraft.client.model.ModelArmorStandArmor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderArmorStand extends RenderLivingBase {
   public static final ResourceLocation TEXTURE_ARMOR_STAND = new ResourceLocation("textures/entity/armorstand/wood.png");

   public RenderArmorStand(RenderManager var1) {
      super(var1, new ModelArmorStand(), 0.0F);
      LayerBipedArmor var2 = new LayerBipedArmor(this) {
         protected void initArmor() {
            this.modelLeggings = new ModelArmorStandArmor(0.5F);
            this.modelArmor = new ModelArmorStandArmor(1.0F);
         }
      };
      this.addLayer(var2);
      this.addLayer(new LayerHeldItem(this));
      this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
   }

   protected ResourceLocation getEntityTexture(EntityArmorStand var1) {
      return TEXTURE_ARMOR_STAND;
   }

   public ModelArmorStand getMainModel() {
      return (ModelArmorStand)super.getMainModel();
   }

   protected void applyRotations(EntityArmorStand var1, float var2, float var3, float var4) {
      GlStateManager.rotate(180.0F - var3, 0.0F, 1.0F, 0.0F);
      float var5 = (float)(var1.world.getTotalWorldTime() - var1.punchCooldown) + var4;
      if (var5 < 5.0F) {
         GlStateManager.rotate(MathHelper.sin(var5 / 1.5F * 3.1415927F) * 3.0F, 0.0F, 1.0F, 0.0F);
      }

   }

   protected boolean canRenderName(EntityArmorStand var1) {
      return var1.getAlwaysRenderNameTag();
   }

   public void doRender(EntityArmorStand var1, double var2, double var4, double var6, float var8, float var9) {
      if (var1.hasMarker()) {
         this.renderMarker = true;
      }

      super.doRender((EntityLivingBase)var1, var2, var4, var6, var8, var9);
      if (var1.hasMarker()) {
         this.renderMarker = false;
      }

   }
}
