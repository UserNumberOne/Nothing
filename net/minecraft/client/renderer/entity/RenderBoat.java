package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IMultipassModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBoat extends Render {
   private static final ResourceLocation[] BOAT_TEXTURES = new ResourceLocation[]{new ResourceLocation("textures/entity/boat/boat_oak.png"), new ResourceLocation("textures/entity/boat/boat_spruce.png"), new ResourceLocation("textures/entity/boat/boat_birch.png"), new ResourceLocation("textures/entity/boat/boat_jungle.png"), new ResourceLocation("textures/entity/boat/boat_acacia.png"), new ResourceLocation("textures/entity/boat/boat_darkoak.png")};
   protected ModelBase modelBoat = new ModelBoat();

   public RenderBoat(RenderManager var1) {
      super(var1);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityBoat var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.pushMatrix();
      this.setupTranslation(var2, var4, var6);
      this.setupRotation(var1, var8, var9);
      this.bindEntityTexture(var1);
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      this.modelBoat.render(var1, var9, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.popMatrix();
      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   public void setupRotation(EntityBoat var1, float var2, float var3) {
      GlStateManager.rotate(180.0F - var2, 0.0F, 1.0F, 0.0F);
      float var4 = (float)var1.getTimeSinceHit() - var3;
      float var5 = var1.getDamageTaken() - var3;
      if (var5 < 0.0F) {
         var5 = 0.0F;
      }

      if (var4 > 0.0F) {
         GlStateManager.rotate(MathHelper.sin(var4) * var4 * var5 / 10.0F * (float)var1.getForwardDirection(), 1.0F, 0.0F, 0.0F);
      }

      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
   }

   public void setupTranslation(double var1, double var3, double var5) {
      GlStateManager.translate((float)var1, (float)var3 + 0.375F, (float)var5);
   }

   protected ResourceLocation getEntityTexture(EntityBoat var1) {
      return BOAT_TEXTURES[var1.getBoatType().ordinal()];
   }

   public boolean isMultipass() {
      return true;
   }

   public void renderMultipass(EntityBoat var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.pushMatrix();
      this.setupTranslation(var2, var4, var6);
      this.setupRotation(var1, var8, var9);
      this.bindEntityTexture(var1);
      ((IMultipassModel)this.modelBoat).renderMultipass(var1, var9, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GlStateManager.popMatrix();
   }
}
