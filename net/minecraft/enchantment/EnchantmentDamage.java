package net.minecraft.enchantment;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public class EnchantmentDamage extends Enchantment {
   private static final String[] PROTECTION_NAME = new String[]{"all", "undead", "arthropods"};
   private static final int[] BASE_ENCHANTABILITY = new int[]{1, 5, 5};
   private static final int[] LEVEL_ENCHANTABILITY = new int[]{11, 8, 8};
   private static final int[] THRESHOLD_ENCHANTABILITY = new int[]{20, 20, 20};
   public final int damageType;

   public EnchantmentDamage(Enchantment.Rarity var1, int var2, EntityEquipmentSlot... var3) {
      super(var1, EnumEnchantmentType.WEAPON, var3);
      this.damageType = var2;
   }

   public int getMinEnchantability(int var1) {
      return BASE_ENCHANTABILITY[this.damageType] + (var1 - 1) * LEVEL_ENCHANTABILITY[this.damageType];
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + THRESHOLD_ENCHANTABILITY[this.damageType];
   }

   public int getMaxLevel() {
      return 5;
   }

   public float calcDamageByCreature(int var1, EnumCreatureAttribute var2) {
      if (this.damageType == 0) {
         return 1.0F + (float)Math.max(0, var1 - 1) * 0.5F;
      } else if (this.damageType == 1 && var2 == EnumCreatureAttribute.UNDEAD) {
         return (float)var1 * 2.5F;
      } else {
         return this.damageType == 2 && var2 == EnumCreatureAttribute.ARTHROPOD ? (float)var1 * 2.5F : 0.0F;
      }
   }

   public String getName() {
      return "enchantment.damage." + PROTECTION_NAME[this.damageType];
   }

   public boolean canApplyTogether(Enchantment var1) {
      return !(var1 instanceof EnchantmentDamage);
   }

   public boolean canApply(ItemStack var1) {
      return var1.getItem() instanceof ItemAxe ? true : super.canApply(var1);
   }

   public void onEntityDamaged(EntityLivingBase var1, Entity var2, int var3) {
      if (var2 instanceof EntityLivingBase) {
         EntityLivingBase var4 = (EntityLivingBase)var2;
         if (this.damageType == 2 && var4.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
            int var5 = 20 + var1.getRNG().nextInt(10 * var3);
            var4.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, var5, 3));
         }
      }

   }
}
