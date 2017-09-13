package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class ItemNameTag extends Item {
   public ItemNameTag() {
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      if (!stack.hasDisplayName()) {
         return false;
      } else if (target instanceof EntityLiving) {
         EntityLiving entityliving = (EntityLiving)target;
         entityliving.setCustomNameTag(stack.getDisplayName());
         entityliving.enablePersistence();
         --stack.stackSize;
         return true;
      } else {
         return super.itemInteractionForEntity(stack, playerIn, target, hand);
      }
   }
}
