package net.minecraft.enchantment;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class EnchantmentProtection extends Enchantment {
   public final EnchantmentProtection.Type protectionType;

   public EnchantmentProtection(Enchantment.Rarity var1, EnchantmentProtection.Type var2, EntityEquipmentSlot... var3) {
      super(var1, EnumEnchantmentType.ARMOR, var3);
      this.protectionType = var2;
      if (var2 == EnchantmentProtection.Type.FALL) {
         this.type = EnumEnchantmentType.ARMOR_FEET;
      }

   }

   public int getMinEnchantability(int var1) {
      return this.protectionType.getMinimalEnchantability() + (var1 - 1) * this.protectionType.getEnchantIncreasePerLevel();
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + this.protectionType.getEnchantIncreasePerLevel();
   }

   public int getMaxLevel() {
      return 4;
   }

   public int calcModifierDamage(int var1, DamageSource var2) {
      if (var2.canHarmInCreative()) {
         return 0;
      } else if (this.protectionType == EnchantmentProtection.Type.ALL) {
         return var1;
      } else if (this.protectionType == EnchantmentProtection.Type.FIRE && var2.isFireDamage()) {
         return var1 * 2;
      } else if (this.protectionType == EnchantmentProtection.Type.FALL && var2 == DamageSource.fall) {
         return var1 * 3;
      } else if (this.protectionType == EnchantmentProtection.Type.EXPLOSION && var2.isExplosion()) {
         return var1 * 2;
      } else {
         return this.protectionType == EnchantmentProtection.Type.PROJECTILE && var2.isProjectile() ? var1 * 2 : 0;
      }
   }

   public String getName() {
      return "enchantment.protect." + this.protectionType.getTypeName();
   }

   public boolean canApplyTogether(Enchantment var1) {
      if (var1 instanceof EnchantmentProtection) {
         EnchantmentProtection var2 = (EnchantmentProtection)var1;
         if (this.protectionType == var2.protectionType) {
            return false;
         } else {
            return this.protectionType == EnchantmentProtection.Type.FALL || var2.protectionType == EnchantmentProtection.Type.FALL;
         }
      } else {
         return super.canApplyTogether(var1);
      }
   }

   public static int getFireTimeForEntity(EntityLivingBase var0, int var1) {
      int var2 = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FIRE_PROTECTION, var0);
      if (var2 > 0) {
         var1 -= MathHelper.floor((float)var1 * (float)var2 * 0.15F);
      }

      return var1;
   }

   public static double getBlastDamageReduction(EntityLivingBase var0, double var1) {
      int var3 = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.BLAST_PROTECTION, var0);
      if (var3 > 0) {
         var1 -= (double)MathHelper.floor(var1 * (double)((float)var3 * 0.15F));
      }

      return var1;
   }

   public static enum Type {
      ALL("all", 1, 11, 20),
      FIRE("fire", 10, 8, 12),
      FALL("fall", 5, 6, 10),
      EXPLOSION("explosion", 5, 8, 12),
      PROJECTILE("projectile", 3, 6, 15);

      private final String typeName;
      private final int minEnchantability;
      private final int levelCost;
      private final int levelCostSpan;

      private Type(String var3, int var4, int var5, int var6) {
         this.typeName = var3;
         this.minEnchantability = var4;
         this.levelCost = var5;
         this.levelCostSpan = var6;
      }

      public String getTypeName() {
         return this.typeName;
      }

      public int getMinimalEnchantability() {
         return this.minEnchantability;
      }

      public int getEnchantIncreasePerLevel() {
         return this.levelCost;
      }
   }
}
