package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBiped extends RenderLiving {
   private static final ResourceLocation DEFAULT_RES_LOC = new ResourceLocation("textures/entity/steve.png");
   public ModelBiped modelBipedMain;
   protected float scale;

   public RenderBiped(RenderManager var1, ModelBiped var2, float var3) {
      this(renderManagerIn, modelBipedIn, shadowSize, 1.0F);
      this.addLayer(new LayerHeldItem(this));
   }

   public RenderBiped(RenderManager var1, ModelBiped var2, float var3, float var4) {
      super(renderManagerIn, modelBipedIn, shadowSize);
      this.modelBipedMain = modelBipedIn;
      this.scale = p_i46169_4_;
      this.addLayer(new LayerCustomHead(modelBipedIn.bipedHead));
   }

   protected ResourceLocation getEntityTexture(EntityLiving var1) {
      return DEFAULT_RES_LOC;
   }

   public void transformHeldFull3DItemLayer() {
      GlStateManager.translate(0.0F, 0.1875F, 0.0F);
   }
}
