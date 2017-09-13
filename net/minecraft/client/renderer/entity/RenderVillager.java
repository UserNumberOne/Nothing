package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVillager extends RenderLiving {
   private static final ResourceLocation VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/villager.png");
   private static final ResourceLocation FARMER_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/farmer.png");
   private static final ResourceLocation LIBRARIAN_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/librarian.png");
   private static final ResourceLocation PRIEST_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/priest.png");
   private static final ResourceLocation SMITH_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/smith.png");
   private static final ResourceLocation BUTCHER_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/villager/butcher.png");

   public RenderVillager(RenderManager var1) {
      super(var1, new ModelVillager(0.0F), 0.5F);
      this.addLayer(new LayerCustomHead(this.getMainModel().villagerHead));
   }

   public ModelVillager getMainModel() {
      return (ModelVillager)super.getMainModel();
   }

   protected ResourceLocation getEntityTexture(EntityVillager var1) {
      return var1.getProfessionForge().getSkin();
   }

   protected void preRenderCallback(EntityVillager var1, float var2) {
      float var3 = 0.9375F;
      if (var1.getGrowingAge() < 0) {
         var3 = (float)((double)var3 * 0.5D);
         this.shadowSize = 0.25F;
      } else {
         this.shadowSize = 0.5F;
      }

      GlStateManager.scale(var3, var3, var3);
   }
}
