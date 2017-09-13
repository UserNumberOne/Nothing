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
      super(var1);
   }

   protected void initArmor() {
      this.modelLeggings = new ModelBiped(0.5F);
      this.modelArmor = new ModelBiped(1.0F);
   }

   protected void setModelSlotVisible(ModelBiped var1, EntityEquipmentSlot var2) {
      this.setModelVisible(var1);
      switch(var2) {
      case HEAD:
         var1.bipedHead.showModel = true;
         var1.bipedHeadwear.showModel = true;
         break;
      case CHEST:
         var1.bipedBody.showModel = true;
         var1.bipedRightArm.showModel = true;
         var1.bipedLeftArm.showModel = true;
         break;
      case LEGS:
         var1.bipedBody.showModel = true;
         var1.bipedRightLeg.showModel = true;
         var1.bipedLeftLeg.showModel = true;
         break;
      case FEET:
         var1.bipedRightLeg.showModel = true;
         var1.bipedLeftLeg.showModel = true;
      }

   }

   protected void setModelVisible(ModelBiped var1) {
      var1.setInvisible(false);
   }

   protected ModelBiped getArmorModelHook(EntityLivingBase var1, ItemStack var2, EntityEquipmentSlot var3, ModelBiped var4) {
      return ForgeHooksClient.getArmorModel(var1, var2, var3, var4);
   }
}
