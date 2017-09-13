package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import java.util.List;
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
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl;

public abstract class Enchantment extends Impl {
   public static final RegistryNamespaced REGISTRY = GameData.getEnchantmentRegistry();
   private final EntityEquipmentSlot[] applicableEquipmentTypes;
   private final Enchantment.Rarity rarity;
   public EnumEnchantmentType type;
   protected String name;

   @Nullable
   public static Enchantment getEnchantmentByID(int var0) {
      return (Enchantment)REGISTRY.getObjectById(id);
   }

   public static int getEnchantmentID(Enchantment var0) {
      return REGISTRY.getIDForObject(enchantmentIn);
   }

   @Nullable
   public static Enchantment getEnchantmentByLocation(String var0) {
      return (Enchantment)REGISTRY.getObject(new ResourceLocation(location));
   }

   protected Enchantment(Enchantment.Rarity var1, EnumEnchantmentType var2, EntityEquipmentSlot[] var3) {
      this.rarity = rarityIn;
      this.type = typeIn;
      this.applicableEquipmentTypes = slots;
   }

   @Nullable
   public Iterable getEntityEquipment(EntityLivingBase var1) {
      List list = Lists.newArrayList();

      for(EntityEquipmentSlot entityequipmentslot : this.applicableEquipmentTypes) {
         ItemStack itemstack = entityIn.getItemStackFromSlot(entityequipmentslot);
         if (itemstack != null) {
            list.add(itemstack);
         }
      }

      return list.size() > 0 ? list : null;
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
      return 1 + enchantmentLevel * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 5;
   }

   public int calcModifierDamage(int var1, DamageSource var2) {
      return 0;
   }

   public float calcDamageByCreature(int var1, EnumCreatureAttribute var2) {
      return 0.0F;
   }

   public boolean canApplyTogether(Enchantment var1) {
      return this != ench;
   }

   public Enchantment setName(String var1) {
      this.name = enchName;
      return this;
   }

   public String getName() {
      return "enchantment." + this.name;
   }

   public String getTranslatedName(int var1) {
      String s = I18n.translateToLocal(this.getName());
      return level == 1 && this.getMaxLevel() == 1 ? s : s + " " + I18n.translateToLocal("enchantment.level." + level);
   }

   public boolean canApply(ItemStack var1) {
      return this.canApplyAtEnchantingTable(stack);
   }

   public void onEntityDamaged(EntityLivingBase var1, Entity var2, int var3) {
   }

   public void onUserHurt(EntityLivingBase var1, Entity var2, int var3) {
   }

   public boolean isTreasureEnchantment() {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack var1) {
      return this.type.canEnchantItem(stack.getItem());
   }

   public boolean isAllowedOnBooks() {
      return true;
   }

   public static void registerEnchantments() {
      EntityEquipmentSlot[] aentityequipmentslot = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
      REGISTRY.register(0, new ResourceLocation("protection"), new EnchantmentProtection(Enchantment.Rarity.COMMON, EnchantmentProtection.Type.ALL, aentityequipmentslot));
      REGISTRY.register(1, new ResourceLocation("fire_protection"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.FIRE, aentityequipmentslot));
      REGISTRY.register(2, new ResourceLocation("feather_falling"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.FALL, aentityequipmentslot));
      REGISTRY.register(3, new ResourceLocation("blast_protection"), new EnchantmentProtection(Enchantment.Rarity.RARE, EnchantmentProtection.Type.EXPLOSION, aentityequipmentslot));
      REGISTRY.register(4, new ResourceLocation("projectile_protection"), new EnchantmentProtection(Enchantment.Rarity.UNCOMMON, EnchantmentProtection.Type.PROJECTILE, aentityequipmentslot));
      REGISTRY.register(5, new ResourceLocation("respiration"), new EnchantmentOxygen(Enchantment.Rarity.RARE, aentityequipmentslot));
      REGISTRY.register(6, new ResourceLocation("aqua_affinity"), new EnchantmentWaterWorker(Enchantment.Rarity.RARE, aentityequipmentslot));
      REGISTRY.register(7, new ResourceLocation("thorns"), new EnchantmentThorns(Enchantment.Rarity.VERY_RARE, aentityequipmentslot));
      REGISTRY.register(8, new ResourceLocation("depth_strider"), new EnchantmentWaterWalker(Enchantment.Rarity.RARE, aentityequipmentslot));
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
   }

   public static enum Rarity {
      COMMON(10),
      UNCOMMON(5),
      RARE(2),
      VERY_RARE(1);

      private final int weight;

      private Rarity(int var3) {
         this.weight = rarityWeight;
      }

      public int getWeight() {
         return this.weight;
      }
   }
}
