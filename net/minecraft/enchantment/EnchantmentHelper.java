package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;

public class EnchantmentHelper {
   private static final Random RANDOM = new Random();
   private static final EnchantmentHelper.ModifierDamage ENCHANTMENT_MODIFIER_DAMAGE = new EnchantmentHelper.ModifierDamage();
   private static final EnchantmentHelper.ModifierLiving ENCHANTMENT_MODIFIER_LIVING = new EnchantmentHelper.ModifierLiving();
   private static final EnchantmentHelper.HurtIterator ENCHANTMENT_ITERATOR_HURT = new EnchantmentHelper.HurtIterator();
   private static final EnchantmentHelper.DamageIterator ENCHANTMENT_ITERATOR_DAMAGE = new EnchantmentHelper.DamageIterator();

   public static int getEnchantmentLevel(Enchantment var0, @Nullable ItemStack var1) {
      if (var1 == null) {
         return 0;
      } else {
         NBTTagList var2 = var1.getEnchantmentTagList();
         if (var2 == null) {
            return 0;
         } else {
            for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
               Enchantment var4 = Enchantment.getEnchantmentByID(var2.getCompoundTagAt(var3).getShort("id"));
               short var5 = var2.getCompoundTagAt(var3).getShort("lvl");
               if (var4 == var0) {
                  return var5;
               }
            }

            return 0;
         }
      }
   }

   public static Map getEnchantments(ItemStack var0) {
      LinkedHashMap var1 = Maps.newLinkedHashMap();
      NBTTagList var2 = var0.getItem() == Items.ENCHANTED_BOOK ? Items.ENCHANTED_BOOK.getEnchantments(var0) : var0.getEnchantmentTagList();
      if (var2 != null) {
         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            Enchantment var4 = Enchantment.getEnchantmentByID(var2.getCompoundTagAt(var3).getShort("id"));
            short var5 = var2.getCompoundTagAt(var3).getShort("lvl");
            var1.put(var4, Integer.valueOf(var5));
         }
      }

      return var1;
   }

   public static void setEnchantments(Map var0, ItemStack var1) {
      NBTTagList var2 = new NBTTagList();

      for(Entry var4 : var0.entrySet()) {
         Enchantment var5 = (Enchantment)var4.getKey();
         if (var5 != null) {
            int var6 = ((Integer)var4.getValue()).intValue();
            NBTTagCompound var7 = new NBTTagCompound();
            var7.setShort("id", (short)Enchantment.getEnchantmentID(var5));
            var7.setShort("lvl", (short)var6);
            var2.appendTag(var7);
            if (var1.getItem() == Items.ENCHANTED_BOOK) {
               Items.ENCHANTED_BOOK.addEnchantment(var1, new EnchantmentData(var5, var6));
            }
         }
      }

      if (var2.hasNoTags()) {
         if (var1.hasTagCompound()) {
            var1.getTagCompound().removeTag("ench");
         }
      } else if (var1.getItem() != Items.ENCHANTED_BOOK) {
         var1.setTagInfo("ench", var2);
      }

   }

   private static void applyEnchantmentModifier(EnchantmentHelper.IModifier var0, ItemStack var1) {
      if (var1 != null) {
         NBTTagList var2 = var1.getEnchantmentTagList();
         if (var2 != null) {
            for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
               short var4 = var2.getCompoundTagAt(var3).getShort("id");
               short var5 = var2.getCompoundTagAt(var3).getShort("lvl");
               if (Enchantment.getEnchantmentByID(var4) != null) {
                  var0.calculateModifier(Enchantment.getEnchantmentByID(var4), var5);
               }
            }
         }
      }

   }

   private static void applyEnchantmentModifierArray(EnchantmentHelper.IModifier var0, Iterable var1) {
      for(ItemStack var3 : var1) {
         applyEnchantmentModifier(var0, var3);
      }

   }

   public static int getEnchantmentModifierDamage(Iterable var0, DamageSource var1) {
      ENCHANTMENT_MODIFIER_DAMAGE.damageModifier = 0;
      ENCHANTMENT_MODIFIER_DAMAGE.source = var1;
      applyEnchantmentModifierArray(ENCHANTMENT_MODIFIER_DAMAGE, var0);
      return ENCHANTMENT_MODIFIER_DAMAGE.damageModifier;
   }

   public static float getModifierForCreature(ItemStack var0, EnumCreatureAttribute var1) {
      ENCHANTMENT_MODIFIER_LIVING.livingModifier = 0.0F;
      ENCHANTMENT_MODIFIER_LIVING.entityLiving = var1;
      applyEnchantmentModifier(ENCHANTMENT_MODIFIER_LIVING, var0);
      return ENCHANTMENT_MODIFIER_LIVING.livingModifier;
   }

   public static void applyThornEnchantments(EntityLivingBase var0, Entity var1) {
      ENCHANTMENT_ITERATOR_HURT.attacker = var1;
      ENCHANTMENT_ITERATOR_HURT.user = var0;
      if (var0 != null) {
         applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_HURT, var0.getEquipmentAndArmor());
      }

      if (var1 instanceof EntityPlayer) {
         applyEnchantmentModifier(ENCHANTMENT_ITERATOR_HURT, var0.getHeldItemMainhand());
      }

   }

   public static void applyArthropodEnchantments(EntityLivingBase var0, Entity var1) {
      ENCHANTMENT_ITERATOR_DAMAGE.user = var0;
      ENCHANTMENT_ITERATOR_DAMAGE.target = var1;
      if (var0 != null) {
         applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_DAMAGE, var0.getEquipmentAndArmor());
      }

      if (var0 instanceof EntityPlayer) {
         applyEnchantmentModifier(ENCHANTMENT_ITERATOR_DAMAGE, var0.getHeldItemMainhand());
      }

   }

   public static int getMaxEnchantmentLevel(Enchantment var0, EntityLivingBase var1) {
      Iterable var2 = var0.getEntityEquipment(var1);
      if (var2 == null) {
         return 0;
      } else {
         int var3 = 0;

         for(ItemStack var5 : var2) {
            int var6 = getEnchantmentLevel(var0, var5);
            if (var6 > var3) {
               var3 = var6;
            }
         }

         return var3;
      }
   }

   public static int getKnockbackModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.KNOCKBACK, var0);
   }

   public static int getFireAspectModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.FIRE_ASPECT, var0);
   }

   public static int getRespirationModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.RESPIRATION, var0);
   }

   public static int getDepthStriderModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.DEPTH_STRIDER, var0);
   }

   public static int getEfficiencyModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.EFFICIENCY, var0);
   }

   public static int getLuckOfSeaModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, var0);
   }

   public static int getLureModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LURE, var0);
   }

   public static int getLootingModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LOOTING, var0);
   }

   public static boolean getAquaAffinityModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.AQUA_AFFINITY, var0) > 0;
   }

   public static boolean hasFrostWalkerEnchantment(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.FROST_WALKER, var0) > 0;
   }

   @Nullable
   public static ItemStack getEnchantedItem(Enchantment var0, EntityLivingBase var1) {
      Iterable var2 = var0.getEntityEquipment(var1);
      if (var2 == null) {
         return null;
      } else {
         ArrayList var3 = Lists.newArrayList();

         for(ItemStack var5 : var2) {
            if (var5 != null && getEnchantmentLevel(var0, var5) > 0) {
               var3.add(var5);
            }
         }

         return var3.isEmpty() ? null : (ItemStack)var3.get(var1.getRNG().nextInt(var3.size()));
      }
   }

   public static int calcItemStackEnchantability(Random var0, int var1, int var2, ItemStack var3) {
      Item var4 = var3.getItem();
      int var5 = var4.getItemEnchantability(var3);
      if (var5 <= 0) {
         return 0;
      } else {
         if (var2 > 15) {
            var2 = 15;
         }

         int var6 = var0.nextInt(8) + 1 + (var2 >> 1) + var0.nextInt(var2 + 1);
         return var1 == 0 ? Math.max(var6 / 3, 1) : (var1 == 1 ? var6 * 2 / 3 + 1 : Math.max(var6, var2 * 2));
      }
   }

   public static ItemStack addRandomEnchantment(Random var0, ItemStack var1, int var2, boolean var3) {
      boolean var4 = var1.getItem() == Items.BOOK;
      List var5 = buildEnchantmentList(var0, var1, var2, var3);
      if (var4) {
         var1.setItem(Items.ENCHANTED_BOOK);
      }

      for(EnchantmentData var7 : var5) {
         if (var4) {
            Items.ENCHANTED_BOOK.addEnchantment(var1, var7);
         } else {
            var1.addEnchantment(var7.enchantmentobj, var7.enchantmentLevel);
         }
      }

      return var1;
   }

   public static List buildEnchantmentList(Random var0, ItemStack var1, int var2, boolean var3) {
      ArrayList var4 = Lists.newArrayList();
      Item var5 = var1.getItem();
      int var6 = var5.getItemEnchantability(var1);
      if (var6 <= 0) {
         return var4;
      } else {
         var2 = var2 + 1 + var0.nextInt(var6 / 4 + 1) + var0.nextInt(var6 / 4 + 1);
         float var7 = (var0.nextFloat() + var0.nextFloat() - 1.0F) * 0.15F;
         var2 = MathHelper.clamp(Math.round((float)var2 + (float)var2 * var7), 1, Integer.MAX_VALUE);
         List var8 = getEnchantmentDatas(var2, var1, var3);
         if (!var8.isEmpty()) {
            var4.add(WeightedRandom.getRandomItem(var0, var8));

            while(var0.nextInt(50) <= var2) {
               removeIncompatible(var8, (EnchantmentData)Util.getLastElement(var4));
               if (var8.isEmpty()) {
                  break;
               }

               var4.add(WeightedRandom.getRandomItem(var0, var8));
               var2 /= 2;
            }
         }

         return var4;
      }
   }

   public static void removeIncompatible(List var0, EnchantmentData var1) {
      Iterator var2 = var0.iterator();

      while(var2.hasNext()) {
         Enchantment var3 = ((EnchantmentData)var2.next()).enchantmentobj;
         if (!var1.enchantmentobj.canApplyTogether(var3) || !var3.canApplyTogether(var1.enchantmentobj)) {
            var2.remove();
         }
      }

   }

   public static List getEnchantmentDatas(int var0, ItemStack var1, boolean var2) {
      ArrayList var3 = Lists.newArrayList();
      Item var4 = var1.getItem();
      boolean var5 = var1.getItem() == Items.BOOK;

      for(Enchantment var7 : Enchantment.REGISTRY) {
         if ((!var7.isTreasureEnchantment() || var2) && (var7.canApplyAtEnchantingTable(var1) || var5 && var7.isAllowedOnBooks())) {
            for(int var8 = var7.getMaxLevel(); var8 > var7.getMinLevel() - 1; --var8) {
               if (var0 >= var7.getMinEnchantability(var8) && var0 <= var7.getMaxEnchantability(var8)) {
                  var3.add(new EnchantmentData(var7, var8));
                  break;
               }
            }
         }
      }

      return var3;
   }

   static final class DamageIterator implements EnchantmentHelper.IModifier {
      public EntityLivingBase user;
      public Entity target;

      private DamageIterator() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         var1.onEntityDamaged(this.user, this.target, var2);
      }
   }

   static final class HurtIterator implements EnchantmentHelper.IModifier {
      public EntityLivingBase user;
      public Entity attacker;

      private HurtIterator() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         var1.onUserHurt(this.user, this.attacker, var2);
      }
   }

   interface IModifier {
      void calculateModifier(Enchantment var1, int var2);
   }

   static final class ModifierDamage implements EnchantmentHelper.IModifier {
      public int damageModifier;
      public DamageSource source;

      private ModifierDamage() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         this.damageModifier += var1.calcModifierDamage(var2, this.source);
      }
   }

   static final class ModifierLiving implements EnchantmentHelper.IModifier {
      public float livingModifier;
      public EnumCreatureAttribute entityLiving;

      private ModifierLiving() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         this.livingModifier += var1.calcDamageByCreature(var2, this.entityLiving);
      }
   }
}
