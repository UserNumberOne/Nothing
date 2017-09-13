package net.minecraft.enchantment;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class EnchantmentProtection extends Enchantment {
   public final EnchantmentProtection.Type protectionType;

   public EnchantmentProtection(Enchantment.Rarity var1, EnchantmentProtection.Type var2, EntityEquipmentSlot... var3) {
      super(rarityIn, EnumEnchantmentType.ARMOR, slots);
      this.protectionType = protectionTypeIn;
      if (protectionTypeIn == EnchantmentProtection.Type.FALL) {
         this.type = EnumEnchantmentType.ARMOR_FEET;
      }

   }

   public int getMinEnchantability(int var1) {
      return this.protectionType.getMinimalEnchantability() + (enchantmentLevel - 1) * this.protectionType.getEnchantIncreasePerLevel();
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + this.protectionType.getEnchantIncreasePerLevel();
   }

   public int getMaxLevel() {
      return 4;
   }

   public int calcModifierDamage(int var1, DamageSource var2) {
      return source.canHarmInCreative() ? 0 : (this.protectionType == EnchantmentProtection.Type.ALL ? level : (this.protectionType == EnchantmentProtection.Type.FIRE && source.isFireDamage() ? level * 2 : (this.protectionType == EnchantmentProtection.Type.FALL && source == DamageSource.fall ? level * 3 : (this.protectionType == EnchantmentProtection.Type.EXPLOSION && source.isExplosion() ? level * 2 : (this.protectionType == EnchantmentProtection.Type.PROJECTILE && source.isProjectile() ? level * 2 : 0)))));
   }

   public String getName() {
      return "enchantment.protect." + this.protectionType.getTypeName();
   }

   public boolean canApplyTogether(Enchantment var1) {
      if (!(ench instanceof EnchantmentProtection)) {
         return super.canApplyTogether(ench);
      } else {
         EnchantmentProtection enchantmentprotection = (EnchantmentProtection)ench;
         return this.protectionType == enchantmentprotection.protectionType ? false : this.protectionType == EnchantmentProtection.Type.FALL || enchantmentprotection.protectionType == EnchantmentProtection.Type.FALL;
      }
   }

   public static int getFireTimeForEntity(EntityLivingBase var0, int var1) {
      int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FIRE_PROTECTION, p_92093_0_);
      if (i > 0) {
         p_92093_1_ -= MathHelper.floor((float)p_92093_1_ * (float)i * 0.15F);
      }

      return p_92093_1_;
   }

   public static double getBlastDamageReduction(EntityLivingBase var0, double var1) {
      int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.BLAST_PROTECTION, entityLivingBaseIn);
      if (i > 0) {
         damage -= (double)MathHelper.floor(damage * (double)((float)i * 0.15F));
      }

      return damage;
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
         this.typeName = name;
         this.minEnchantability = minimal;
         this.levelCost = perLevelEnchantability;
         this.levelCostSpan = p_i47051_6_;
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
