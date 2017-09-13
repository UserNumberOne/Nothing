package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSilverfish extends ModelBase {
   private final ModelRenderer[] silverfishBodyParts = new ModelRenderer[7];
   private final ModelRenderer[] silverfishWings;
   private final float[] zPlacement = new float[7];
   private static final int[][] SILVERFISH_BOX_LENGTH = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
   private static final int[][] SILVERFISH_TEXTURE_POSITIONS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

   public ModelSilverfish() {
      float var1 = -3.5F;

      for(int var2 = 0; var2 < this.silverfishBodyParts.length; ++var2) {
         this.silverfishBodyParts[var2] = new ModelRenderer(this, SILVERFISH_TEXTURE_POSITIONS[var2][0], SILVERFISH_TEXTURE_POSITIONS[var2][1]);
         this.silverfishBodyParts[var2].addBox((float)SILVERFISH_BOX_LENGTH[var2][0] * -0.5F, 0.0F, (float)SILVERFISH_BOX_LENGTH[var2][2] * -0.5F, SILVERFISH_BOX_LENGTH[var2][0], SILVERFISH_BOX_LENGTH[var2][1], SILVERFISH_BOX_LENGTH[var2][2]);
         this.silverfishBodyParts[var2].setRotationPoint(0.0F, (float)(24 - SILVERFISH_BOX_LENGTH[var2][1]), var1);
         this.zPlacement[var2] = var1;
         if (var2 < this.silverfishBodyParts.length - 1) {
            var1 += (float)(SILVERFISH_BOX_LENGTH[var2][2] + SILVERFISH_BOX_LENGTH[var2 + 1][2]) * 0.5F;
         }
      }

      this.silverfishWings = new ModelRenderer[3];
      this.silverfishWings[0] = new ModelRenderer(this, 20, 0);
      this.silverfishWings[0].addBox(-5.0F, 0.0F, (float)SILVERFISH_BOX_LENGTH[2][2] * -0.5F, 10, 8, SILVERFISH_BOX_LENGTH[2][2]);
      this.silverfishWings[0].setRotationPoint(0.0F, 16.0F, this.zPlacement[2]);
      this.silverfishWings[1] = new ModelRenderer(this, 20, 11);
      this.silverfishWings[1].addBox(-3.0F, 0.0F, (float)SILVERFISH_BOX_LENGTH[4][2] * -0.5F, 6, 4, SILVERFISH_BOX_LENGTH[4][2]);
      this.silverfishWings[1].setRotationPoint(0.0F, 20.0F, this.zPlacement[4]);
      this.silverfishWings[2] = new ModelRenderer(this, 20, 18);
      this.silverfishWings[2].addBox(-3.0F, 0.0F, (float)SILVERFISH_BOX_LENGTH[4][2] * -0.5F, 6, 5, SILVERFISH_BOX_LENGTH[1][2]);
      this.silverfishWings[2].setRotationPoint(0.0F, 19.0F, this.zPlacement[1]);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.setRotationAngles(var2, var3, var4, var5, var6, var7, var1);

      for(ModelRenderer var11 : this.silverfishBodyParts) {
         var11.render(var7);
      }

      for(ModelRenderer var15 : this.silverfishWings) {
         var15.render(var7);
      }

   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      for(int var8 = 0; var8 < this.silverfishBodyParts.length; ++var8) {
         this.silverfishBodyParts[var8].rotateAngleY = MathHelper.cos(var3 * 0.9F + (float)var8 * 0.15F * 3.1415927F) * 3.1415927F * 0.05F * (float)(1 + Math.abs(var8 - 2));
         this.silverfishBodyParts[var8].rotationPointX = MathHelper.sin(var3 * 0.9F + (float)var8 * 0.15F * 3.1415927F) * 3.1415927F * 0.2F * (float)Math.abs(var8 - 2);
      }

      this.silverfishWings[0].rotateAngleY = this.silverfishBodyParts[2].rotateAngleY;
      this.silverfishWings[1].rotateAngleY = this.silverfishBodyParts[4].rotateAngleY;
      this.silverfishWings[1].rotationPointX = this.silverfishBodyParts[4].rotationPointX;
      this.silverfishWings[2].rotateAngleY = this.silverfishBodyParts[1].rotateAngleY;
      this.silverfishWings[2].rotationPointX = this.silverfishBodyParts[1].rotationPointX;
   }
}
