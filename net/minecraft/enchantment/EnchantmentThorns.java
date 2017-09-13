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
   public EnchantmentThorns(Enchantment.Rarity rarityIn, EntityEquipmentSlot... slots) {
      super(rarityIn, EnumEnchantmentType.ARMOR_CHEST, slots);
      this.setName("thorns");
   }

   public int getMinEnchantability(int enchantmentLevel) {
      return 10 + 20 * (enchantmentLevel - 1);
   }

   public int getMaxEnchantability(int enchantmentLevel) {
      return super.getMinEnchantability(enchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canApply(ItemStack stack) {
      return stack.getItem() instanceof ItemArmor ? true : super.canApply(stack);
   }

   public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
      Random random = user.getRNG();
      ItemStack itemstack = EnchantmentHelper.getEnchantedItem(Enchantments.THORNS, user);
      if (shouldHit(level, random)) {
         if (attacker != null) {
            attacker.attackEntityFrom(DamageSource.causeThornsDamage(user), (float)getDamage(level, random));
         }

         if (itemstack != null) {
            this.damageArmor(itemstack, 3, user);
         }
      } else if (itemstack != null) {
         this.damageArmor(itemstack, 1, user);
      }

   }

   public static boolean shouldHit(int level, Random rnd) {
      return level <= 0 ? false : rnd.nextFloat() < 0.15F * (float)level;
   }

   public static int getDamage(int level, Random rnd) {
      return level > 10 ? level - 10 : 1 + rnd.nextInt(4);
   }

   private void damageArmor(ItemStack stack, int amount, EntityLivingBase entity) {
      int slot = -1;
      int x = 0;

      for(ItemStack i : entity.getArmorInventoryList()) {
         if (i == stack) {
            slot = x;
            break;
         }

         ++x;
      }

      if (slot != -1 && stack.getItem() instanceof ISpecialArmor) {
         ISpecialArmor armor = (ISpecialArmor)stack.getItem();
         armor.damageArmor(entity, stack, DamageSource.causeThornsDamage(entity), amount, slot);
      } else {
         stack.damageItem(1, entity);
      }
   }
}
