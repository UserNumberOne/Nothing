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
      super(rarityIn, EnumEnchantmentType.WEAPON, slots);
      this.damageType = damageTypeIn;
   }

   public int getMinEnchantability(int var1) {
      return BASE_ENCHANTABILITY[this.damageType] + (enchantmentLevel - 1) * LEVEL_ENCHANTABILITY[this.damageType];
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + THRESHOLD_ENCHANTABILITY[this.damageType];
   }

   public int getMaxLevel() {
      return 5;
   }

   public float calcDamageByCreature(int var1, EnumCreatureAttribute var2) {
      return this.damageType == 0 ? 1.0F + (float)Math.max(0, level - 1) * 0.5F : (this.damageType == 1 && creatureType == EnumCreatureAttribute.UNDEAD ? (float)level * 2.5F : (this.damageType == 2 && creatureType == EnumCreatureAttribute.ARTHROPOD ? (float)level * 2.5F : 0.0F));
   }

   public String getName() {
      return "enchantment.damage." + PROTECTION_NAME[this.damageType];
   }

   public boolean canApplyTogether(Enchantment var1) {
      return !(ench instanceof EnchantmentDamage);
   }

   public boolean canApply(ItemStack var1) {
      return stack.getItem() instanceof ItemAxe ? true : super.canApply(stack);
   }

   public void onEntityDamaged(EntityLivingBase var1, Entity var2, int var3) {
      if (target instanceof EntityLivingBase) {
         EntityLivingBase entitylivingbase = (EntityLivingBase)target;
         if (this.damageType == 2 && entitylivingbase.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
            int i = 20 + user.getRNG().nextInt(10 * level);
            entitylivingbase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, i, 3));
         }
      }

   }
}
