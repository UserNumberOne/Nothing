package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonDeath;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonEyes;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDragon extends RenderLiving {
   public static final ResourceLocation ENDERCRYSTAL_BEAM_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal_beam.png");
   private static final ResourceLocation DRAGON_EXPLODING_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
   private static final ResourceLocation DRAGON_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon.png");
   protected ModelDragon modelDragon;

   public RenderDragon(RenderManager var1) {
      super(var1, new ModelDragon(0.0F), 0.5F);
      this.modelDragon = (ModelDragon)this.mainModel;
      this.addLayer(new LayerEnderDragonEyes(this));
      this.addLayer(new LayerEnderDragonDeath());
   }

   protected void applyRotations(EntityDragon var1, float var2, float var3, float var4) {
      float var5 = (float)var1.getMovementOffsets(7, var4)[0];
      float var6 = (float)(var1.getMovementOffsets(5, var4)[1] - var1.getMovementOffsets(10, var4)[1]);
      GlStateManager.rotate(-var5, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(var6 * 10.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.0F, 1.0F);
      if (var1.deathTime > 0) {
         float var7 = ((float)var1.deathTime + var4 - 1.0F) / 20.0F * 1.6F;
         var7 = MathHelper.sqrt(var7);
         if (var7 > 1.0F) {
            var7 = 1.0F;
         }

         GlStateManager.rotate(var7 * this.getDeathMaxRotation(var1), 0.0F, 0.0F, 1.0F);
      }

   }

   protected void renderModel(EntityDragon var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      if (var1.deathTicks > 0) {
         float var8 = (float)var1.deathTicks / 200.0F;
         GlStateManager.depthFunc(515);
         GlStateManager.enableAlpha();
         GlStateManager.alphaFunc(516, var8);
         this.bindTexture(DRAGON_EXPLODING_TEXTURES);
         this.mainModel.render(var1, var2, var3, var4, var5, var6, var7);
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.depthFunc(514);
      }

      this.bindEntityTexture(var1);
      this.mainModel.render(var1, var2, var3, var4, var5, var6, var7);
      if (var1.hurtTime > 0) {
         GlStateManager.depthFunc(514);
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
         this.mainModel.render(var1, var2, var3, var4, var5, var6, var7);
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.depthFunc(515);
      }

   }

   public void doRender(EntityDragon var1, double var2, double var4, double var6, float var8, float var9) {
      super.doRender((EntityLiving)var1, var2, var4, var6, var8, var9);
      if (var1.healingEnderCrystal != null) {
         this.bindTexture(ENDERCRYSTAL_BEAM_TEXTURES);
         float var10 = MathHelper.sin(((float)var1.healingEnderCrystal.ticksExisted + var9) * 0.2F) / 2.0F + 0.5F;
         var10 = (var10 * var10 + var10) * 0.2F;
         renderCrystalBeams(var2, var4, var6, var9, var1.posX + (var1.prevPosX - var1.posX) * (double)(1.0F - var9), var1.posY + (var1.prevPosY - var1.posY) * (double)(1.0F - var9), var1.posZ + (var1.prevPosZ - var1.posZ) * (double)(1.0F - var9), var1.ticksExisted, var1.healingEnderCrystal.posX, (double)var10 + var1.healingEnderCrystal.posY, var1.healingEnderCrystal.posZ);
      }

   }

   public static void renderCrystalBeams(double var0, double var2, double var4, float var6, double var7, double var9, double var11, int var13, double var14, double var16, double var18) {
      float var20 = (float)(var14 - var7);
      float var21 = (float)(var16 - 1.0D - var9);
      float var22 = (float)(var18 - var11);
      float var23 = MathHelper.sqrt(var20 * var20 + var22 * var22);
      float var24 = MathHelper.sqrt(var20 * var20 + var21 * var21 + var22 * var22);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var0, (float)var2 + 2.0F, (float)var4);
      GlStateManager.rotate((float)(-Math.atan2((double)var22, (double)var20)) * 57.295776F - 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate((float)(-Math.atan2((double)var23, (double)var21)) * 57.295776F - 90.0F, 1.0F, 0.0F, 0.0F);
      Tessellator var25 = Tessellator.getInstance();
      VertexBuffer var26 = var25.getBuffer();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableCull();
      GlStateManager.shadeModel(7425);
      float var27 = 0.0F - ((float)var13 + var6) * 0.01F;
      float var28 = MathHelper.sqrt(var20 * var20 + var21 * var21 + var22 * var22) / 32.0F - ((float)var13 + var6) * 0.01F;
      var26.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);
      boolean var29 = true;

      for(int var30 = 0; var30 <= 8; ++var30) {
         float var31 = MathHelper.sin((float)(var30 % 8) * 6.2831855F / 8.0F) * 0.75F;
         float var32 = MathHelper.cos((float)(var30 % 8) * 6.2831855F / 8.0F) * 0.75F;
         float var33 = (float)(var30 % 8) / 8.0F;
         var26.pos((double)(var31 * 0.2F), (double)(var32 * 0.2F), 0.0D).tex((double)var33, (double)var27).color(0, 0, 0, 255).endVertex();
         var26.pos((double)var31, (double)var32, (double)var24).tex((double)var33, (double)var28).color(255, 255, 255, 255).endVertex();
      }

      var25.draw();
      GlStateManager.enableCull();
      GlStateManager.shadeModel(7424);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   protected ResourceLocation getEntityTexture(EntityDragon var1) {
      return DRAGON_TEXTURES;
   }
}
