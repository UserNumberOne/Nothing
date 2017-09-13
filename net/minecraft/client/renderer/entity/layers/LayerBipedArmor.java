package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBipedArmor extends LayerArmorBase {
   public LayerBipedArmor(RenderLivingBase var1) {
      super(rendererIn);
   }

   protected void initArmor() {
      this.modelLeggings = new ModelBiped(0.5F);
      this.modelArmor = new ModelBiped(1.0F);
   }

   protected void setModelSlotVisible(ModelBiped var1, EntityEquipmentSlot var2) {
      this.setModelVisible(p_188359_1_);
      switch(slotIn) {
      case HEAD:
         p_188359_1_.bipedHead.showModel = true;
         p_188359_1_.bipedHeadwear.showModel = true;
         break;
      case CHEST:
         p_188359_1_.bipedBody.showModel = true;
         p_188359_1_.bipedRightArm.showModel = true;
         p_188359_1_.bipedLeftArm.showModel = true;
         break;
      case LEGS:
         p_188359_1_.bipedBody.showModel = true;
         p_188359_1_.bipedRightLeg.showModel = true;
         p_188359_1_.bipedLeftLeg.showModel = true;
         break;
      case FEET:
         p_188359_1_.bipedRightLeg.showModel = true;
         p_188359_1_.bipedLeftLeg.showModel = true;
      }

   }

   protected void setModelVisible(ModelBiped var1) {
      model.setInvisible(false);
   }

   protected ModelBiped getArmorModelHook(EntityLivingBase var1, ItemStack var2, EntityEquipmentSlot var3, ModelBiped var4) {
      return ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
   }
}
