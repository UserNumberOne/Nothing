package net.minecraft.enchantment;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;

public class EnchantmentThorns extends Enchantment {
   public EnchantmentThorns(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.ARMOR_CHEST, var2);
      this.setName("thorns");
   }

   public int getMinEnchantability(int var1) {
      return 10 + 20 * (var1 - 1);
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(var1) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canApply(ItemStack var1) {
      return var1.getItem() instanceof ItemArmor ? true : super.canApply(var1);
   }

   public void onUserHurt(EntityLivingBase var1, Entity var2, int var3) {
      Random var4 = var1.getRNG();
      ItemStack var5 = EnchantmentHelper.getEnchantedItem(Enchantments.THORNS, var1);
      if (shouldHit(var3, var4)) {
         if (var2 != null) {
            var2.attackEntityFrom(DamageSource.causeThornsDamage(var1), (float)getDamage(var3, var4));
         }

         if (var5 != null) {
            this.damageArmor(var5, 3, var1);
         }
      } else if (var5 != null) {
         this.damageArmor(var5, 1, var1);
      }

   }

   public static boolean shouldHit(int var0, Random var1) {
      return var0 <= 0 ? false : var1.nextFloat() < 0.15F * (float)var0;
   }

   public static int getDamage(int var0, Random var1) {
      return var0 > 10 ? var0 - 10 : 1 + var1.nextInt(4);
   }

   private void damageArmor(ItemStack var1, int var2, EntityLivingBase var3) {
      int var4 = -1;
      int var5 = 0;

      for(ItemStack var7 : var3.getArmorInventoryList()) {
         if (var7 == var1) {
            var4 = var5;
            break;
         }

         ++var5;
      }

      if (var4 != -1 && var1.getItem() instanceof ISpecialArmor) {
         ISpecialArmor var8 = (ISpecialArmor)var1.getItem();
         var8.damageArmor(var3, var1, DamageSource.causeThornsDamage(var3), var2, var4);
      } else {
         var1.damageItem(1, var3);
      }
   }
}
