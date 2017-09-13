package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.layers.LayerEndermanEyes;
import net.minecraft.client.renderer.entity.layers.LayerHeldBlock;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnderman extends RenderLiving {
   private static final ResourceLocation ENDERMAN_TEXTURES = new ResourceLocation("textures/entity/enderman/enderman.png");
   private final ModelEnderman endermanModel;
   private final Random rnd = new Random();

   public RenderEnderman(RenderManager var1) {
      super(var1, new ModelEnderman(0.0F), 0.5F);
      this.endermanModel = (ModelEnderman)super.mainModel;
      this.addLayer(new LayerEndermanEyes(this));
      this.addLayer(new LayerHeldBlock(this));
   }

   public void doRender(EntityEnderman var1, double var2, double var4, double var6, float var8, float var9) {
      IBlockState var10 = var1.getHeldBlockState();
      this.endermanModel.isCarrying = var10 != null;
      this.endermanModel.isAttacking = var1.isScreaming();
      if (var1.isScreaming()) {
         double var11 = 0.02D;
         var2 += this.rnd.nextGaussian() * 0.02D;
         var6 += this.rnd.nextGaussian() * 0.02D;
      }

      super.doRender((EntityLiving)var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityEnderman var1) {
      return ENDERMAN_TEXTURES;
   }
}
