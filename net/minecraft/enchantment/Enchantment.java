package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.translation.I18n;
import org.bukkit.craftbukkit.v1_10_R1.enchantments.CraftEnchantment;

public abstract class Enchantment {
   public static final RegistryNamespaced REGISTRY = new RegistryNamespaced();
   private final EntityEquipmentSlot[] applicableEquipmentTypes;
   private final Enchantment.Rarity rarity;
   public EnumEnchantmentType type;
   protected String name;

   @Nullable
   public static Enchantment getEnchantmentByID(int var0) {
      return (Enchantment)REGISTRY.getObjectById(var0);
   }

   public static int getEnchantmentID(Enchantment var0) {
      return REGISTRY.getIDForObject(var0);
   }

   @Nullable
   public static Enchantment getEnchantmentByLocation(String var0) {
      return (Enchantment)REGISTRY.getObject(new ResourceLocation(var0));
   }

   protected Enchantment(Enchantment.Rarity var1, EnumEnchantmentType var2, EntityEquipmentSlot[] var3) {
      this.rarity = var1;
      this.type = var2;
      this.applicableEquipmentTypes = var3;
   }

   @Nullable
   public Iterable getEntityEquipment(EntityLivingBase var1) {
      ArrayList var2 = Lists.newArrayList();

      for(EntityEquipmentSlot var6 : this.applicableEquipmentTypes) {
         ItemStack var7 = var1.getItemStackFromSlot(var6);
         if (var7 != null) {
            var2.add(var7);
         }
      }

      return var2.size() > 0 ? var2 : null;
   }

   public Enchantment.Rarity getRarity() {
      return this.rarity;
   }

   public int getMinLevel() {
      return 1;
   }

   public int getMaxLevel() {
      return 1;
   }

   public int getMinEnchantability(int var1) {
      return 1 + var1 * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 5;
   }

   public int calcModifierDamage(int var1, DamageSource var2) {
      return 0;
   }

   public float calcDamageByCreature(int var1, EnumCreatureAttribute var2) {
      return 0.0F;
   }

   public boolean canApplyTogether(Enchantment var1) {
      return this != var1;
   }

   public Enchantment setName(String var1) {
      this.name = var1;
      return this;
   }

   public String getName() {
      return "enchantment." + this.name;
   }

   public String getTranslatedName(int var1) {
      String var2 = I18n.translateToLocal(this.getName());
      return var1 == 1 && this.getMaxLevel() == 1 ? var2 : var2 + " " + I18n.translateToLocal("enchantment.level." + var1);
   }

   public boolean canApply(ItemStack var1) {
      return this.type.canEnchantItem(var1.getItem());
   }

   public void onEntityDamaged(EntityLivingBase var1, Entity var2, int var3) {
   }

   public void onUserHurt(EntityLivingBase var1, Entity var2, int var3) {
   }

   public boolean isTreasureEnchantment() {
      return false;
   }

   public static void registerEnchantments() {
      EntityEquipmentSlot[] var0 = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
      REGISTRY.register(0, new ResourceLocation("protection"), new EnchantmentProtection(Enchantment.Rarity.COMMON, EnchantmentProtection.Type.ALL, var0));
      REGISTRY.register(1, new ResourceLocation("fire_protection"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.FIRE, var0));
      REGISTRY.register(2, new ResourceLocation("feather_falling"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.FALL, var0));
      REGISTRY.register(3, new ResourceLocation("blast_protection"), new EnchantmentProtection(Enchantment.Rarity.RARE, EnchantmentProtection.Type.EXPLOSION, var0));
      REGISTRY.register(4, new ResourceLocation("projectile_protection"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.PROJECTILE, var0));
      REGISTRY.register(5, new ResourceLocation("respiration"), new EnchantmentOxygen(Enchantment.Rarity.RARE, var0));
      REGISTRY.register(6, new ResourceLocation("aqua_affinity"), new EnchantmentWaterWorker(Enchantment.Rarity.RARE, var0));
      REGISTRY.register(7, new ResourceLocation("thorns"), new EnchantmentThorns(Enchantment.Rarity.VERY_RARE, var0));
      REGISTRY.register(8, new ResourceLocation("depth_strider"), new EnchantmentWaterWalker(Enchantment.Rarity.RARE, var0));
      REGISTRY.register(9, new ResourceLocation("frost_walker"), new EnchantmentFrostWalker(Enchantment.Rarity.RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET}));
      REGISTRY.register(16, new ResourceLocation("sharpness"), new EnchantmentDamage(Enchantment.Rarity.COMMON, 0, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(17, new ResourceLocation("smite"), new EnchantmentDamage(Enchantment.Rarity.UNCOMMON, 1, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(18, new ResourceLocation("bane_of_arthropods"), new EnchantmentDamage(Enchantment.Rarity.UNCOMMON, 2, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(19, new ResourceLocation("knockback"), new EnchantmentKnockback(Enchantment.Rarity.UNCOMMON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(20, new ResourceLocation("fire_aspect"), new EnchantmentFireAspect(Enchantment.Rarity.RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(21, new ResourceLocation("looting"), new EnchantmentLootBonus(Enchantment.Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(32, new ResourceLocation("efficiency"), new EnchantmentDigging(Enchantment.Rarity.COMMON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(33, new ResourceLocation("silk_touch"), new EnchantmentUntouching(Enchantment.Rarity.VERY_RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(34, new ResourceLocation("unbreaking"), new EnchantmentDurability(Enchantment.Rarity.UNCOMMON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(35, new ResourceLocation("fortune"), new EnchantmentLootBonus(Enchantment.Rarity.RARE, EnumEnchantmentType.DIGGER, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(48, new ResourceLocation("power"), new EnchantmentArrowDamage(Enchantment.Rarity.COMMON, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(49, new ResourceLocation("punch"), new EnchantmentArrowKnockback(Enchantment.Rarity.RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(50, new ResourceLocation("flame"), new EnchantmentArrowFire(Enchantment.Rarity.RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(51, new ResourceLocation("infinity"), new EnchantmentArrowInfinite(Enchantment.Rarity.VERY_RARE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(61, new ResourceLocation("luck_of_the_sea"), new EnchantmentLootBonus(Enchantment.Rarity.RARE, EnumEnchantmentType.FISHING_ROD, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(62, new ResourceLocation("lure"), new EnchantmentFishingSpeed(Enchantment.Rarity.RARE, EnumEnchantmentType.FISHING_ROD, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND}));
      REGISTRY.register(70, new ResourceLocation("mending"), new EnchantmentMending(Enchantment.Rarity.RARE, EntityEquipmentSlot.values()));

      for(Object var2 : REGISTRY) {
         org.bukkit.enchantments.Enchantment.registerEnchantment(new CraftEnchantment((Enchantment)var2));
      }

   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int var3) {
         this.weight = var3;
      }

      public int getWeight() {
         return this.weight;
      }
   }
}
