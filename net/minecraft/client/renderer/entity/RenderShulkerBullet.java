package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelShulkerBullet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderShulkerBullet extends Render {
   private static final ResourceLocation SHULKER_SPARK_TEXTURE = new ResourceLocation("textures/entity/shulker/spark.png");
   private final ModelShulkerBullet model = new ModelShulkerBullet();

   public RenderShulkerBullet(RenderManager var1) {
      super(var1);
   }

   private float rotLerp(float var1, float var2, float var3) {
      float var4;
      for(var4 = var2 - var1; var4 < -180.0F; var4 += 360.0F) {
         ;
      }

      while(var4 >= 180.0F) {
         var4 -= 360.0F;
      }

      return var1 + var3 * var4;
   }

   public void doRender(EntityShulkerBullet var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.pushMatrix();
      float var10 = this.rotLerp(var1.prevRotationYaw, var1.rotationYaw, var9);
      float var11 = var1.prevRotationPitch + (var1.rotationPitch - var1.prevRotationPitch) * var9;
      float var12 = (float)var1.ticksExisted + var9;
      GlStateManager.translate((float)var2, (float)var4 + 0.15F, (float)var6);
      GlStateManager.rotate(MathHelper.sin(var12 * 0.1F) * 180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(MathHelper.cos(var12 * 0.1F) * 180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(MathHelper.sin(var12 * 0.15F) * 360.0F, 0.0F, 0.0F, 1.0F);
      float var13 = 0.03125F;
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
      this.bindEntityTexture(var1);
      this.model.render(var1, 0.0F, 0.0F, 0.0F, var10, var11, 0.03125F);
      GlStateManager.enableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
      GlStateManager.scale(1.5F, 1.5F, 1.5F);
      this.model.render(var1, 0.0F, 0.0F, 0.0F, var10, var11, 0.03125F);
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityShulkerBullet var1) {
      return SHULKER_SPARK_TEXTURE;
   }
}
