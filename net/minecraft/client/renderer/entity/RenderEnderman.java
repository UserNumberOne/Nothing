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
      super(renderManagerIn, new ModelEnderman(0.0F), 0.5F);
      this.endermanModel = (ModelEnderman)super.mainModel;
      this.addLayer(new LayerEndermanEyes(this));
      this.addLayer(new LayerHeldBlock(this));
   }

   public void doRender(EntityEnderman var1, double var2, double var4, double var6, float var8, float var9) {
      IBlockState iblockstate = entity.getHeldBlockState();
      this.endermanModel.isCarrying = iblockstate != null;
      this.endermanModel.isAttacking = entity.isScreaming();
      if (entity.isScreaming()) {
         double d0 = 0.02D;
         x += this.rnd.nextGaussian() * 0.02D;
         z += this.rnd.nextGaussian() * 0.02D;
      }

      super.doRender((EntityLiving)entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityEnderman var1) {
      return ENDERMAN_TEXTURES;
   }
}
