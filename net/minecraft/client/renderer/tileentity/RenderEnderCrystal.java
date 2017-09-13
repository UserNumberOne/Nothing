package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnderCrystal extends Render {
   private static final ResourceLocation ENDER_CRYSTAL_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
   private final ModelBase modelEnderCrystal = new ModelEnderCrystal(0.0F, true);
   private final ModelBase modelEnderCrystalNoBase = new ModelEnderCrystal(0.0F, false);

   public RenderEnderCrystal(RenderManager var1) {
      super(var1);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityEnderCrystal var1, double var2, double var4, double var6, float var8, float var9) {
      float var10 = (float)var1.innerRotation + var9;
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var2, (float)var4, (float)var6);
      this.bindTexture(ENDER_CRYSTAL_TEXTURES);
      float var11 = MathHelper.sin(var10 * 0.2F) / 2.0F + 0.5F;
      var11 = var11 * var11 + var11;
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      if (var1.shouldShowBottom()) {
         this.modelEnderCrystal.render(var1, 0.0F, var10 * 3.0F, var11 * 0.2F, 0.0F, 0.0F, 0.0625F);
      } else {
         this.modelEnderCrystalNoBase.render(var1, 0.0F, var10 * 3.0F, var11 * 0.2F, 0.0F, 0.0F, 0.0625F);
      }

      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.popMatrix();
      BlockPos var12 = var1.getBeamTarget();
      if (var12 != null) {
         this.bindTexture(RenderDragon.ENDERCRYSTAL_BEAM_TEXTURES);
         float var13 = (float)var12.getX() + 0.5F;
         float var14 = (float)var12.getY() + 0.5F;
         float var15 = (float)var12.getZ() + 0.5F;
         double var16 = (double)var13 - var1.posX;
         double var18 = (double)var14 - var1.posY;
         double var20 = (double)var15 - var1.posZ;
         RenderDragon.renderCrystalBeams(var2 + var16, var4 - 0.3D + (double)(var11 * 0.4F) + var18, var6 + var20, var9, (double)var13, (double)var14, (double)var15, var1.innerRotation, var1.posX, var1.posY, var1.posZ);
      }

      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityEnderCrystal var1) {
      return ENDER_CRYSTAL_TEXTURES;
   }

   public boolean shouldRender(EntityEnderCrystal var1, ICamera var2, double var3, double var5, double var7) {
      return super.shouldRender(var1, var2, var3, var5, var7) || var1.getBeamTarget() != null;
   }
}
