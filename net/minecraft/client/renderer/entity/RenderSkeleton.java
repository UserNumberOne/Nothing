package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerSkeletonType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSkeleton extends RenderBiped {
   private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
   private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
   private static final ResourceLocation STRAY_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray.png");

   public RenderSkeleton(RenderManager var1) {
      super(renderManagerIn, new ModelSkeleton(), 0.5F);
      this.addLayer(new LayerHeldItem(this));
      this.addLayer(new LayerBipedArmor(this) {
         protected void initArmor() {
            this.modelLeggings = new ModelSkeleton(0.5F, true);
            this.modelArmor = new ModelSkeleton(1.0F, true);
         }
      });
      this.addLayer(new LayerSkeletonType(this));
   }

   protected void preRenderCallback(EntitySkeleton var1, float var2) {
      if (entitylivingbaseIn.getSkeletonType() == SkeletonType.WITHER) {
         GlStateManager.scale(1.2F, 1.2F, 1.2F);
      }

   }

   public void transformHeldFull3DItemLayer() {
      GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
   }

   protected ResourceLocation getEntityTexture(EntitySkeleton var1) {
      SkeletonType skeletontype = entity.getSkeletonType();
      return skeletontype == SkeletonType.WITHER ? WITHER_SKELETON_TEXTURES : (skeletontype == SkeletonType.STRAY ? STRAY_SKELETON_TEXTURES : SKELETON_TEXTURES);
   }
}
