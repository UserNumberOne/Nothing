package net.minecraft.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSkeleton extends ModelBiped {
   public ModelSkeleton() {
      this(0.0F, false);
   }

   public ModelSkeleton(float var1, boolean var2) {
      super(var1, 0.0F, 64, 32);
      if (!var2) {
         this.bipedRightArm = new ModelRenderer(this, 40, 16);
         this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, var1);
         this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
         this.bipedLeftArm = new ModelRenderer(this, 40, 16);
         this.bipedLeftArm.mirror = true;
         this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, var1);
         this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
         this.bipedRightLeg = new ModelRenderer(this, 0, 16);
         this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, var1);
         this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
         this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
         this.bipedLeftLeg.mirror = true;
         this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, var1);
         this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
      }

   }

   public void setLivingAnimations(EntityLivingBase var1, float var2, float var3, float var4) {
      this.rightArmPose = ModelBiped.ArmPose.EMPTY;
      this.leftArmPose = ModelBiped.ArmPose.EMPTY;
      ItemStack var5 = var1.getHeldItem(EnumHand.MAIN_HAND);
      if (var5 != null && var5.getItem() == Items.BOW && ((EntitySkeleton)var1).isSwingingArms()) {
         if (var1.getPrimaryHand() == EnumHandSide.RIGHT) {
            this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
         } else {
            this.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
         }
      }

      super.setLivingAnimations(var1, var2, var3, var4);
   }

   public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity var7) {
      super.setRotationAngles(var1, var2, var3, var4, var5, var6, var7);
      ItemStack var8 = ((EntityLivingBase)var7).getHeldItemMainhand();
      EntitySkeleton var9 = (EntitySkeleton)var7;
      if (var9.isSwingingArms() && (var8 == null || var8.getItem() != Items.BOW)) {
         float var10 = MathHelper.sin(this.swingProgress * 3.1415927F);
         float var11 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * 3.1415927F);
         this.bipedRightArm.rotateAngleZ = 0.0F;
         this.bipedLeftArm.rotateAngleZ = 0.0F;
         this.bipedRightArm.rotateAngleY = -(0.1F - var10 * 0.6F);
         this.bipedLeftArm.rotateAngleY = 0.1F - var10 * 0.6F;
         this.bipedRightArm.rotateAngleX = -1.5707964F;
         this.bipedLeftArm.rotateAngleX = -1.5707964F;
         this.bipedRightArm.rotateAngleX -= var10 * 1.2F - var11 * 0.4F;
         this.bipedLeftArm.rotateAngleX -= var10 * 1.2F - var11 * 0.4F;
         this.bipedRightArm.rotateAngleZ += MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
         this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(var3 * 0.09F) * 0.05F + 0.05F;
         this.bipedRightArm.rotateAngleX += MathHelper.sin(var3 * 0.067F) * 0.05F;
         this.bipedLeftArm.rotateAngleX -= MathHelper.sin(var3 * 0.067F) * 0.05F;
      }

   }

   public void postRenderArm(float var1, EnumHandSide var2) {
      float var3 = var2 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      ModelRenderer var4 = this.getArmForSide(var2);
      var4.rotationPointX += var3;
      var4.postRender(var1);
      var4.rotationPointX -= var3;
   }
}
