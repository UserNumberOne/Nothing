package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelShulker extends ModelBase {
   private final ModelRenderer base;
   private final ModelRenderer lid;
   public ModelRenderer head;

   public ModelShulker() {
      this.textureHeight = 64;
      this.textureWidth = 64;
      this.lid = new ModelRenderer(this);
      this.base = new ModelRenderer(this);
      this.head = new ModelRenderer(this);
      this.lid.setTextureOffset(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16, 12, 16);
      this.lid.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.base.setTextureOffset(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16, 8, 16);
      this.base.setRotationPoint(0.0F, 24.0F, 0.0F);
      this.head.setTextureOffset(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6, 6, 6);
      this.head.setRotationPoint(0.0F, 12.0F, 0.0F);
   }

   public int getModelVersion() {
      return 28;
   }

   public void setLivingAnimations(EntityLivingBase var1, float var2, float var3, float var4) {
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      EntityShulker var8 = (EntityShulker)var7;
      float var9 = var3 - (float)var8.ticksExisted;
      float var10 = (0.5F + var8.getClientPeekAmount(var9)) * 3.1415927F;
      float var11 = -1.0F + MathHelper.sin(var10);
      float var12 = 0.0F;
      if (var10 > 3.1415927F) {
         var12 = MathHelper.sin(var3 * 0.1F) * 0.7F;
      }

      this.lid.setRotationPoint(0.0F, 16.0F + MathHelper.sin(var10) * 8.0F + var12, 0.0F);
      if (var8.getClientPeekAmount(var9) > 0.3F) {
         this.lid.rotateAngleY = var11 * var11 * var11 * var11 * 3.1415927F * 0.125F;
      } else {
         this.lid.rotateAngleY = 0.0F;
      }

      this.head.rotateAngleX = var5 * 0.017453292F;
      this.head.rotateAngleY = var4 * 0.017453292F;
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.base.render(var7);
      this.lid.render(var7);
   }
}
