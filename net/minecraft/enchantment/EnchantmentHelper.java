package net.minecraft.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
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
      if (stack == null) {
         return 0;
      } else {
         NBTTagList nbttaglist = stack.getEnchantmentTagList();
         if (nbttaglist == null) {
            return 0;
         } else {
            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Enchantment enchantment = Enchantment.getEnchantmentByID(nbttaglist.getCompoundTagAt(i).getShort("id"));
               int j = nbttaglist.getCompoundTagAt(i).getShort("lvl");
               if (enchantment == enchID) {
                  return j;
               }
            }

            return 0;
         }
      }
   }

   public static Map getEnchantments(ItemStack var0) {
      Map map = Maps.newLinkedHashMap();
      NBTTagList nbttaglist = stack.getItem() == Items.ENCHANTED_BOOK ? Items.ENCHANTED_BOOK.getEnchantments(stack) : stack.getEnchantmentTagList();
      if (nbttaglist != null) {
         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            Enchantment enchantment = Enchantment.getEnchantmentByID(nbttaglist.getCompoundTagAt(i).getShort("id"));
            int j = nbttaglist.getCompoundTagAt(i).getShort("lvl");
            map.put(enchantment, Integer.valueOf(j));
         }
      }

      return map;
   }

   public static void setEnchantments(Map var0, ItemStack var1) {
      NBTTagList nbttaglist = new NBTTagList();

      for(Entry entry : enchMap.entrySet()) {
         Enchantment enchantment = (Enchantment)entry.getKey();
         if (enchantment != null) {
            int i = ((Integer)entry.getValue()).intValue();
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setShort("id", (short)Enchantment.getEnchantmentID(enchantment));
            nbttagcompound.setShort("lvl", (short)i);
            nbttaglist.appendTag(nbttagcompound);
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
               Items.ENCHANTED_BOOK.addEnchantment(stack, new EnchantmentData(enchantment, i));
            }
         }
      }

      if (nbttaglist.hasNoTags()) {
         if (stack.hasTagCompound()) {
            stack.getTagCompound().removeTag("ench");
         }
      } else if (stack.getItem() != Items.ENCHANTED_BOOK) {
         stack.setTagInfo("ench", nbttaglist);
      }

   }

   private static void applyEnchantmentModifier(EnchantmentHelper.IModifier var0, ItemStack var1) {
      if (stack != null) {
         NBTTagList nbttaglist = stack.getEnchantmentTagList();
         if (nbttaglist != null) {
            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               int j = nbttaglist.getCompoundTagAt(i).getShort("id");
               int k = nbttaglist.getCompoundTagAt(i).getShort("lvl");
               if (Enchantment.getEnchantmentByID(j) != null) {
                  modifier.calculateModifier(Enchantment.getEnchantmentByID(j), k);
               }
            }
         }
      }

   }

   private static void applyEnchantmentModifierArray(EnchantmentHelper.IModifier var0, Iterable var1) {
      for(ItemStack itemstack : stacks) {
         applyEnchantmentModifier(modifier, itemstack);
      }

   }

   public static int getEnchantmentModifierDamage(Iterable var0, DamageSource var1) {
      ENCHANTMENT_MODIFIER_DAMAGE.damageModifier = 0;
      ENCHANTMENT_MODIFIER_DAMAGE.source = source;
      applyEnchantmentModifierArray(ENCHANTMENT_MODIFIER_DAMAGE, stacks);
      return ENCHANTMENT_MODIFIER_DAMAGE.damageModifier;
   }

   public static float getModifierForCreature(ItemStack var0, EnumCreatureAttribute var1) {
      ENCHANTMENT_MODIFIER_LIVING.livingModifier = 0.0F;
      ENCHANTMENT_MODIFIER_LIVING.entityLiving = creatureAttribute;
      applyEnchantmentModifier(ENCHANTMENT_MODIFIER_LIVING, stack);
      return ENCHANTMENT_MODIFIER_LIVING.livingModifier;
   }

   public static void applyThornEnchantments(EntityLivingBase var0, Entity var1) {
      ENCHANTMENT_ITERATOR_HURT.attacker = p_151384_1_;
      ENCHANTMENT_ITERATOR_HURT.user = p_151384_0_;
      if (p_151384_0_ != null) {
         applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getEquipmentAndArmor());
      }

      if (p_151384_1_ instanceof EntityPlayer) {
         applyEnchantmentModifier(ENCHANTMENT_ITERATOR_HURT, p_151384_0_.getHeldItemMainhand());
      }

   }

   public static void applyArthropodEnchantments(EntityLivingBase var0, Entity var1) {
      ENCHANTMENT_ITERATOR_DAMAGE.user = p_151385_0_;
      ENCHANTMENT_ITERATOR_DAMAGE.target = p_151385_1_;
      if (p_151385_0_ != null) {
         applyEnchantmentModifierArray(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getEquipmentAndArmor());
      }

      if (p_151385_0_ instanceof EntityPlayer) {
         applyEnchantmentModifier(ENCHANTMENT_ITERATOR_DAMAGE, p_151385_0_.getHeldItemMainhand());
      }

   }

   public static int getMaxEnchantmentLevel(Enchantment var0, EntityLivingBase var1) {
      Iterable iterable = p_185284_0_.getEntityEquipment(p_185284_1_);
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;

         for(ItemStack itemstack : iterable) {
            int j = getEnchantmentLevel(p_185284_0_, itemstack);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   public static int getKnockbackModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.KNOCKBACK, player);
   }

   public static int getFireAspectModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.FIRE_ASPECT, player);
   }

   public static int getRespirationModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.RESPIRATION, p_185292_0_);
   }

   public static int getDepthStriderModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.DEPTH_STRIDER, p_185294_0_);
   }

   public static int getEfficiencyModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.EFFICIENCY, p_185293_0_);
   }

   public static int getLuckOfSeaModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, player);
   }

   public static int getLureModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LURE, player);
   }

   public static int getLootingModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.LOOTING, p_185283_0_);
   }

   public static boolean getAquaAffinityModifier(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.AQUA_AFFINITY, p_185287_0_) > 0;
   }

   public static boolean hasFrostWalkerEnchantment(EntityLivingBase var0) {
      return getMaxEnchantmentLevel(Enchantments.FROST_WALKER, player) > 0;
   }

   @Nullable
   public static ItemStack getEnchantedItem(Enchantment var0, EntityLivingBase var1) {
      Iterable iterable = p_92099_0_.getEntityEquipment(p_92099_1_);
      if (iterable == null) {
         return null;
      } else {
         List list = Lists.newArrayList();

         for(ItemStack itemstack : iterable) {
            if (itemstack != null && getEnchantmentLevel(p_92099_0_, itemstack) > 0) {
               list.add(itemstack);
            }
         }

         return list.isEmpty() ? null : (ItemStack)list.get(p_92099_1_.getRNG().nextInt(list.size()));
      }
   }

   public static int calcItemStackEnchantability(Random var0, int var1, int var2, ItemStack var3) {
      Item item = stack.getItem();
      int i = item.getItemEnchantability(stack);
      if (i <= 0) {
         return 0;
      } else {
         if (power > 15) {
            power = 15;
         }

         int j = rand.nextInt(8) + 1 + (power >> 1) + rand.nextInt(power + 1);
         return enchantNum == 0 ? Math.max(j / 3, 1) : (enchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, power * 2));
      }
   }

   public static ItemStack addRandomEnchantment(Random var0, ItemStack var1, int var2, boolean var3) {
      boolean flag = p_77504_1_.getItem() == Items.BOOK;
      List list = buildEnchantmentList(random, p_77504_1_, p_77504_2_, allowTreasure);
      if (flag) {
         p_77504_1_.setItem(Items.ENCHANTED_BOOK);
      }

      for(EnchantmentData enchantmentdata : list) {
         if (flag) {
            Items.ENCHANTED_BOOK.addEnchantment(p_77504_1_, enchantmentdata);
         } else {
            p_77504_1_.addEnchantment(enchantmentdata.enchantmentobj, enchantmentdata.enchantmentLevel);
         }
      }

      return p_77504_1_;
   }

   public static List buildEnchantmentList(Random var0, ItemStack var1, int var2, boolean var3) {
      List list = Lists.newArrayList();
      Item item = itemStackIn.getItem();
      int i = item.getItemEnchantability(itemStackIn);
      if (i <= 0) {
         return list;
      } else {
         p_77513_2_ = p_77513_2_ + 1 + randomIn.nextInt(i / 4 + 1) + randomIn.nextInt(i / 4 + 1);
         float f = (randomIn.nextFloat() + randomIn.nextFloat() - 1.0F) * 0.15F;
         p_77513_2_ = MathHelper.clamp(Math.round((float)p_77513_2_ + (float)p_77513_2_ * f), 1, Integer.MAX_VALUE);
         List list1 = getEnchantmentDatas(p_77513_2_, itemStackIn, allowTreasure);
         if (!list1.isEmpty()) {
            list.add(WeightedRandom.getRandomItem(randomIn, list1));

            while(randomIn.nextInt(50) <= p_77513_2_) {
               removeIncompatible(list1, (EnchantmentData)Util.getLastElement(list));
               if (list1.isEmpty()) {
                  break;
               }

               list.add(WeightedRandom.getRandomItem(randomIn, list1));
               p_77513_2_ /= 2;
            }
         }

         return list;
      }
   }

   public static void removeIncompatible(List var0, EnchantmentData var1) {
      Iterator iterator = p_185282_0_.iterator();

      while(iterator.hasNext()) {
         Enchantment e2 = ((EnchantmentData)iterator.next()).enchantmentobj;
         if (!p_185282_1_.enchantmentobj.canApplyTogether(e2) || !e2.canApplyTogether(p_185282_1_.enchantmentobj)) {
            iterator.remove();
         }
      }

   }

   public static List getEnchantmentDatas(int var0, ItemStack var1, boolean var2) {
      List list = Lists.newArrayList();
      Item item = p_185291_1_.getItem();
      boolean flag = p_185291_1_.getItem() == Items.BOOK;

      for(Enchantment enchantment : Enchantment.REGISTRY) {
         if ((!enchantment.isTreasureEnchantment() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(p_185291_1_) || flag && enchantment.isAllowedOnBooks())) {
            for(int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
               if (p_185291_0_ >= enchantment.getMinEnchantability(i) && p_185291_0_ <= enchantment.getMaxEnchantability(i)) {
                  list.add(new EnchantmentData(enchantment, i));
                  break;
               }
            }
         }
      }

      return list;
   }

   static final class DamageIterator implements EnchantmentHelper.IModifier {
      public EntityLivingBase user;
      public Entity target;

      private DamageIterator() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         enchantmentIn.onEntityDamaged(this.user, this.target, enchantmentLevel);
      }
   }

   static final class HurtIterator implements EnchantmentHelper.IModifier {
      public EntityLivingBase user;
      public Entity attacker;

      private HurtIterator() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         enchantmentIn.onUserHurt(this.user, this.attacker, enchantmentLevel);
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
         this.damageModifier += enchantmentIn.calcModifierDamage(enchantmentLevel, this.source);
      }
   }

   static final class ModifierLiving implements EnchantmentHelper.IModifier {
      public float livingModifier;
      public EnumCreatureAttribute entityLiving;

      private ModifierLiving() {
      }

      public void calculateModifier(Enchantment var1, int var2) {
         this.livingModifier += enchantmentIn.calcDamageByCreature(enchantmentLevel, this.entityLiving);
      }
   }
}
