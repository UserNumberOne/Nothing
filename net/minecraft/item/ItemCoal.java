package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;

public class ItemCoal extends Item {
   public ItemCoal() {
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(CreativeTabs.MATERIALS);
   }

   public String getUnlocalizedName(ItemStack var1) {
      return var1.getMetadata() == 1 ? "item.charcoal" : "item.coal";
   }
}
