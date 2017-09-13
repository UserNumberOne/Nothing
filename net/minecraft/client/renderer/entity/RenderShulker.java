package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderShulker extends RenderLiving {
   private static final ResourceLocation SHULKER_ENDERGOLEM_TEXTURE = new ResourceLocation("textures/entity/shulker/endergolem.png");
   private int modelVersion;

   public RenderShulker(RenderManager var1, ModelShulker var2) {
      super(var1, var2, 0.0F);
      this.addLayer(new RenderShulker.HeadLayer());
      this.modelVersion = var2.getModelVersion();
      this.shadowSize = 0.0F;
   }

   public void doRender(EntityShulker var1, double var2, double var4, double var6, float var8, float var9) {
      if (this.modelVersion != ((ModelShulker)this.mainModel).getModelVersion()) {
         this.mainModel = new ModelShulker();
         this.modelVersion = ((ModelShulker)this.mainModel).getModelVersion();
      }

      int var10 = var1.getClientTeleportInterp();
      if (var10 > 0 && var1.isAttachedToBlock()) {
         BlockPos var11 = var1.getAttachmentPos();
         BlockPos var12 = var1.getOldAttachPos();
         double var13 = (double)((float)var10 - var9) / 6.0D;
         var13 = var13 * var13;
         double var15 = (double)(var11.getX() - var12.getX()) * var13;
         double var17 = (double)(var11.getY() - var12.getY()) * var13;
         double var19 = (double)(var11.getZ() - var12.getZ()) * var13;
         super.doRender((EntityLiving)var1, var2 - var15, var4 - var17, var6 - var19, var8, var9);
      } else {
         super.doRender((EntityLiving)var1, var2, var4, var6, var8, var9);
      }

   }

   public boolean shouldRender(EntityShulker var1, ICamera var2, double var3, double var5, double var7) {
      if (super.shouldRender((EntityLiving)var1, var2, var3, var5, var7)) {
         return true;
      } else {
         if (var1.getClientTeleportInterp() > 0 && var1.isAttachedToBlock()) {
            BlockPos var9 = var1.getOldAttachPos();
            BlockPos var10 = var1.getAttachmentPos();
            Vec3d var11 = new Vec3d((double)var10.getX(), (double)var10.getY(), (double)var10.getZ());
            Vec3d var12 = new Vec3d((double)var9.getX(), (double)var9.getY(), (double)var9.getZ());
            if (var2.isBoundingBoxInFrustum(new AxisAlignedBB(var12.xCoord, var12.yCoord, var12.zCoord, var11.xCoord, var11.yCoord, var11.zCoord))) {
               return true;
            }
         }

         return false;
      }
   }

   protected ResourceLocation getEntityTexture(EntityShulker var1) {
      return SHULKER_ENDERGOLEM_TEXTURE;
   }

   protected void applyRotations(EntityShulker var1, float var2, float var3, float var4) {
      super.applyRotations(var1, var2, var3, var4);
      switch(var1.getAttachmentFacing()) {
      case DOWN:
      default:
         break;
      case EAST:
         GlStateManager.translate(0.5F, 0.5F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         break;
      case WEST:
         GlStateManager.translate(-0.5F, 0.5F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
         break;
      case NORTH:
         GlStateManager.translate(0.0F, 0.5F, -0.5F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         break;
      case SOUTH:
         GlStateManager.translate(0.0F, 0.5F, 0.5F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         break;
      case UP:
         GlStateManager.translate(0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      }

   }

   protected void preRenderCallback(EntityShulker var1, float var2) {
      float var3 = 0.999F;
      GlStateManager.scale(0.999F, 0.999F, 0.999F);
   }

   @SideOnly(Side.CLIENT)
   class HeadLayer implements LayerRenderer {
      private HeadLayer() {
      }

      public void doRenderLayer(EntityShulker var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
         GlStateManager.pushMatrix();
         switch(var1.getAttachmentFacing()) {
         case DOWN:
         default:
            break;
         case EAST:
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(1.0F, -1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            break;
         case WEST:
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(-1.0F, -1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            break;
         case NORTH:
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0F, -1.0F);
            break;
         case SOUTH:
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0F, 1.0F);
            break;
         case UP:
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -2.0F, 0.0F);
         }

         ModelRenderer var9 = ((ModelShulker)RenderShulker.this.getMainModel()).head;
         var9.rotateAngleY = var6 * 0.017453292F;
         var9.rotateAngleX = var7 * 0.017453292F;
         RenderShulker.this.bindTexture(RenderShulker.SHULKER_ENDERGOLEM_TEXTURE);
         var9.render(var8);
         GlStateManager.popMatrix();
      }

      public boolean shouldCombineTextures() {
         return false;
      }
   }
}
