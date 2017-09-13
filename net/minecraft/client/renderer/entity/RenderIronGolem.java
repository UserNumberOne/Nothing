package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerIronGolemFlower;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderIronGolem extends RenderLiving {
   private static final ResourceLocation IRON_GOLEM_TEXTURES = new ResourceLocation("textures/entity/iron_golem.png");

   public RenderIronGolem(RenderManager var1) {
      super(var1, new ModelIronGolem(), 0.5F);
      this.addLayer(new LayerIronGolemFlower(this));
   }

   protected ResourceLocation getEntityTexture(EntityIronGolem var1) {
      return IRON_GOLEM_TEXTURES;
   }

   protected void applyRotations(EntityIronGolem var1, float var2, float var3, float var4) {
      super.applyRotations(var1, var2, var3, var4);
      if ((double)var1.limbSwingAmount >= 0.01D) {
         float var5 = 13.0F;
         float var6 = var1.limbSwing - var1.limbSwingAmount * (1.0F - var4) + 6.0F;
         float var7 = (Math.abs(var6 % 13.0F - 6.5F) - 3.25F) / 3.25F;
         GlStateManager.rotate(6.5F * var7, 0.0F, 0.0F, 1.0F);
      }

   }
}
