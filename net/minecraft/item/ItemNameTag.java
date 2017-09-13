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
      if (!var1.hasDisplayName()) {
         return false;
      } else if (var3 instanceof EntityLiving) {
         EntityLiving var5 = (EntityLiving)var3;
         var5.setCustomNameTag(var1.getDisplayName());
         var5.enablePersistence();
         --var1.stackSize;
         return true;
      } else {
         return super.itemInteractionForEntity(var1, var2, var3, var4);
      }
   }
}
