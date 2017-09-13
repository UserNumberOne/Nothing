package net.minecraft.client.model;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBoat extends ModelBase implements IMultipassModel {
   public ModelRenderer[] boatSides = new ModelRenderer[5];
   public ModelRenderer[] paddles = new ModelRenderer[2];
   public ModelRenderer noWater;
   private final int patchList = GLAllocation.generateDisplayLists(1);

   public ModelBoat() {
      this.boatSides[0] = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
      this.boatSides[1] = (new ModelRenderer(this, 0, 19)).setTextureSize(128, 64);
      this.boatSides[2] = (new ModelRenderer(this, 0, 27)).setTextureSize(128, 64);
      this.boatSides[3] = (new ModelRenderer(this, 0, 35)).setTextureSize(128, 64);
      this.boatSides[4] = (new ModelRenderer(this, 0, 43)).setTextureSize(128, 64);
      boolean var1 = true;
      boolean var2 = true;
      boolean var3 = true;
      boolean var4 = true;
      boolean var5 = true;
      this.boatSides[0].addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
      this.boatSides[0].setRotationPoint(0.0F, 3.0F, 1.0F);
      this.boatSides[1].addBox(-13.0F, -7.0F, -1.0F, 18, 6, 2, 0.0F);
      this.boatSides[1].setRotationPoint(-15.0F, 4.0F, 4.0F);
      this.boatSides[2].addBox(-8.0F, -7.0F, -1.0F, 16, 6, 2, 0.0F);
      this.boatSides[2].setRotationPoint(15.0F, 4.0F, 0.0F);
      this.boatSides[3].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
      this.boatSides[3].setRotationPoint(0.0F, 4.0F, -9.0F);
      this.boatSides[4].addBox(-14.0F, -7.0F, -1.0F, 28, 6, 2, 0.0F);
      this.boatSides[4].setRotationPoint(0.0F, 4.0F, 9.0F);
      this.boatSides[0].rotateAngleX = 1.5707964F;
      this.boatSides[1].rotateAngleY = 4.712389F;
      this.boatSides[2].rotateAngleY = 1.5707964F;
      this.boatSides[3].rotateAngleY = 3.1415927F;
      this.paddles[0] = this.makePaddle(true);
      this.paddles[0].setRotationPoint(3.0F, -5.0F, 9.0F);
      this.paddles[1] = this.makePaddle(false);
      this.paddles[1].setRotationPoint(3.0F, -5.0F, -9.0F);
      this.paddles[1].rotateAngleY = 3.1415927F;
      this.paddles[0].rotateAngleZ = 0.19634955F;
      this.paddles[1].rotateAngleZ = 0.19634955F;
      this.noWater = (new ModelRenderer(this, 0, 0)).setTextureSize(128, 64);
      this.noWater.addBox(-14.0F, -9.0F, -3.0F, 28, 16, 3, 0.0F);
      this.noWater.setRotationPoint(0.0F, -3.0F, 1.0F);
      this.noWater.rotateAngleX = 1.5707964F;
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      EntityBoat var8 = (EntityBoat)var1;
      this.setRotationAngles(var2, var3, var4, var5, var6, var7, var1);

      for(int var9 = 0; var9 < 5; ++var9) {
         this.boatSides[var9].render(var7);
      }

      this.renderPaddle(var8, 0, var7, var2);
      this.renderPaddle(var8, 1, var7, var2);
   }

   public void renderMultipass(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.colorMask(false, false, false, false);
      this.noWater.render(var7);
      GlStateManager.colorMask(true, true, true, true);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
   }

   ModelRenderer makePaddle(boolean var1) {
      ModelRenderer var2 = (new ModelRenderer(this, 62, var1 ? 0 : 20)).setTextureSize(128, 64);
      boolean var3 = true;
      boolean var4 = true;
      boolean var5 = true;
      float var6 = -5.0F;
      var2.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18);
      var2.addBox(var1 ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
      return var2;
   }

   void renderPaddle(EntityBoat var1, int var2, float var3, float var4) {
      float var5 = 40.0F;
      float var6 = var1.getRowingTime(var2, var4) * 40.0F;
      ModelRenderer var7 = this.paddles[var2];
      var7.rotateAngleX = (float)MathHelper.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, (double)((MathHelper.sin(-var6) + 1.0F) / 2.0F));
      var7.rotateAngleY = (float)MathHelper.clampedLerp(-0.7853981633974483D, 0.7853981633974483D, (double)((MathHelper.sin(-var6 + 1.0F) + 1.0F) / 2.0F));
      if (var2 == 1) {
         var7.rotateAngleY = 3.1415927F - var7.rotateAngleY;
      }

      var7.render(var3);
   }
}
