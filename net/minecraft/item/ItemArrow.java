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
      EntityTippedArrow entitytippedarrow = new EntityTippedArrow(worldIn, shooter);
      entitytippedarrow.setPotionEffect(stack);
      return entitytippedarrow;
   }

   public boolean isInfinite(ItemStack var1, ItemStack var2, EntityPlayer var3) {
      int enchant = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow);
      return enchant <= 0 ? false : this.getClass() == ItemArrow.class;
   }
}
