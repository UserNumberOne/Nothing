package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.world.World;

public class ItemArrow extends Item {
   public ItemArrow() {
      this.setCreativeTab(CreativeTabs.COMBAT);
   }

   public EntityArrow createArrow(World var1, ItemStack var2, EntityLivingBase var3) {
      EntityTippedArrow var4 = new EntityTippedArrow(var1, var3);
      var4.setPotionEffect(var2);
      return var4;
   }

   public boolean isInfinite(ItemStack var1, ItemStack var2, EntityPlayer var3) {
      int var4 = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, var2);
      return var4 <= 0 ? false : this.getClass() == ItemArrow.class;
   }
}
