package net.minecraft.potion;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

public class PotionHelper {
   private static final List POTION_TYPE_CONVERSIONS = Lists.newArrayList();
   private static final List POTION_ITEM_CONVERSIONS = Lists.newArrayList();
   private static final List POTION_ITEMS = Lists.newArrayList();
   private static final Predicate IS_POTION_ITEM = new Predicate() {
      public boolean apply(@Nullable ItemStack var1) {
         for(PotionHelper.ItemPredicateInstance var3 : PotionHelper.POTION_ITEMS) {
            if (var3.apply(var1)) {
               return true;
            }
         }

         return false;
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((ItemStack)var1);
      }
   };

   public static boolean isReagent(ItemStack var0) {
      return isItemConversionReagent(var0) || isTypeConversionReagent(var0);
   }

   protected static boolean isItemConversionReagent(ItemStack var0) {
      int var1 = 0;

      for(int var2 = POTION_ITEM_CONVERSIONS.size(); var1 < var2; ++var1) {
         if (((PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(var1)).reagent.apply(var0)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean isTypeConversionReagent(ItemStack var0) {
      int var1 = 0;

      for(int var2 = POTION_TYPE_CONVERSIONS.size(); var1 < var2; ++var1) {
         if (((PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(var1)).reagent.apply(var0)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasConversions(ItemStack var0, ItemStack var1) {
      if (!IS_POTION_ITEM.apply(var0)) {
         return false;
      } else {
         return hasItemConversions(var0, var1) || hasTypeConversions(var0, var1);
      }
   }

   protected static boolean hasItemConversions(ItemStack var0, ItemStack var1) {
      Item var2 = var0.getItem();
      int var3 = 0;

      for(int var4 = POTION_ITEM_CONVERSIONS.size(); var3 < var4; ++var3) {
         PotionHelper.MixPredicate var5 = (PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(var3);
         if (var5.input == var2 && var5.reagent.apply(var1)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean hasTypeConversions(ItemStack var0, ItemStack var1) {
      PotionType var2 = PotionUtils.getPotionFromItem(var0);
      int var3 = 0;

      for(int var4 = POTION_TYPE_CONVERSIONS.size(); var3 < var4; ++var3) {
         PotionHelper.MixPredicate var5 = (PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(var3);
         if (var5.input == var2 && var5.reagent.apply(var1)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static ItemStack doReaction(ItemStack var0, @Nullable ItemStack var1) {
      if (var1 != null) {
         PotionType var2 = PotionUtils.getPotionFromItem(var1);
         Item var3 = var1.getItem();
         int var4 = 0;

         for(int var5 = POTION_ITEM_CONVERSIONS.size(); var4 < var5; ++var4) {
            PotionHelper.MixPredicate var6 = (PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(var4);
            if (var6.input == var3 && var6.reagent.apply(var0)) {
               return PotionUtils.addPotionToItemStack(new ItemStack((Item)var6.output), var2);
            }
         }

         var4 = 0;

         for(int var8 = POTION_TYPE_CONVERSIONS.size(); var4 < var8; ++var4) {
            PotionHelper.MixPredicate var9 = (PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(var4);
            if (var9.input == var2 && var9.reagent.apply(var0)) {
               return PotionUtils.addPotionToItemStack(new ItemStack(var3), (PotionType)var9.output);
            }
         }
      }

      return var1;
   }

   public static void init() {
      PotionHelper.ItemPredicateInstance var0 = new PotionHelper.ItemPredicateInstance(Items.NETHER_WART);
      PotionHelper.ItemPredicateInstance var1 = new PotionHelper.ItemPredicateInstance(Items.GOLDEN_CARROT);
      PotionHelper.ItemPredicateInstance var2 = new PotionHelper.ItemPredicateInstance(Items.REDSTONE);
      PotionHelper.ItemPredicateInstance var3 = new PotionHelper.ItemPredicateInstance(Items.FERMENTED_SPIDER_EYE);
      PotionHelper.ItemPredicateInstance var4 = new PotionHelper.ItemPredicateInstance(Items.RABBIT_FOOT);
      PotionHelper.ItemPredicateInstance var5 = new PotionHelper.ItemPredicateInstance(Items.GLOWSTONE_DUST);
      PotionHelper.ItemPredicateInstance var6 = new PotionHelper.ItemPredicateInstance(Items.MAGMA_CREAM);
      PotionHelper.ItemPredicateInstance var7 = new PotionHelper.ItemPredicateInstance(Items.SUGAR);
      PotionHelper.ItemPredicateInstance var8 = new PotionHelper.ItemPredicateInstance(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata());
      PotionHelper.ItemPredicateInstance var9 = new PotionHelper.ItemPredicateInstance(Items.SPECKLED_MELON);
      PotionHelper.ItemPredicateInstance var10 = new PotionHelper.ItemPredicateInstance(Items.SPIDER_EYE);
      PotionHelper.ItemPredicateInstance var11 = new PotionHelper.ItemPredicateInstance(Items.GHAST_TEAR);
      PotionHelper.ItemPredicateInstance var12 = new PotionHelper.ItemPredicateInstance(Items.BLAZE_POWDER);
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.POTIONITEM));
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.SPLASH_POTION));
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.LINGERING_POTION));
      registerPotionItemConversion(Items.POTIONITEM, new PotionHelper.ItemPredicateInstance(Items.GUNPOWDER), Items.SPLASH_POTION);
      registerPotionItemConversion(Items.SPLASH_POTION, new PotionHelper.ItemPredicateInstance(Items.DRAGON_BREATH), Items.LINGERING_POTION);
      registerPotionTypeConversion(PotionTypes.WATER, var9, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var11, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var4, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var12, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var10, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var7, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var6, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var5, PotionTypes.THICK);
      registerPotionTypeConversion(PotionTypes.WATER, var2, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, var0, PotionTypes.AWKWARD);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var1, PotionTypes.NIGHT_VISION);
      registerPotionTypeConversion(PotionTypes.NIGHT_VISION, var2, PotionTypes.LONG_NIGHT_VISION);
      registerPotionTypeConversion(PotionTypes.NIGHT_VISION, var3, PotionTypes.INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.LONG_NIGHT_VISION, var3, PotionTypes.LONG_INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.INVISIBILITY, var2, PotionTypes.LONG_INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var6, PotionTypes.FIRE_RESISTANCE);
      registerPotionTypeConversion(PotionTypes.FIRE_RESISTANCE, var2, PotionTypes.LONG_FIRE_RESISTANCE);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var4, PotionTypes.LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, var2, PotionTypes.LONG_LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, var5, PotionTypes.STRONG_LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, var3, PotionTypes.SLOWNESS);
      registerPotionTypeConversion(PotionTypes.LONG_LEAPING, var3, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.SLOWNESS, var2, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, var3, PotionTypes.SLOWNESS);
      registerPotionTypeConversion(PotionTypes.LONG_SWIFTNESS, var3, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var7, PotionTypes.SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, var2, PotionTypes.LONG_SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, var5, PotionTypes.STRONG_SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var8, PotionTypes.WATER_BREATHING);
      registerPotionTypeConversion(PotionTypes.WATER_BREATHING, var2, PotionTypes.LONG_WATER_BREATHING);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var9, PotionTypes.HEALING);
      registerPotionTypeConversion(PotionTypes.HEALING, var5, PotionTypes.STRONG_HEALING);
      registerPotionTypeConversion(PotionTypes.HEALING, var3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.STRONG_HEALING, var3, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.HARMING, var5, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.POISON, var3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.LONG_POISON, var3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.STRONG_POISON, var3, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var10, PotionTypes.POISON);
      registerPotionTypeConversion(PotionTypes.POISON, var2, PotionTypes.LONG_POISON);
      registerPotionTypeConversion(PotionTypes.POISON, var5, PotionTypes.STRONG_POISON);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var11, PotionTypes.REGENERATION);
      registerPotionTypeConversion(PotionTypes.REGENERATION, var2, PotionTypes.LONG_REGENERATION);
      registerPotionTypeConversion(PotionTypes.REGENERATION, var5, PotionTypes.STRONG_REGENERATION);
      registerPotionTypeConversion(PotionTypes.AWKWARD, var12, PotionTypes.STRENGTH);
      registerPotionTypeConversion(PotionTypes.STRENGTH, var2, PotionTypes.LONG_STRENGTH);
      registerPotionTypeConversion(PotionTypes.STRENGTH, var5, PotionTypes.STRONG_STRENGTH);
      registerPotionTypeConversion(PotionTypes.WATER, var3, PotionTypes.WEAKNESS);
      registerPotionTypeConversion(PotionTypes.WEAKNESS, var2, PotionTypes.LONG_WEAKNESS);
   }

   private static void registerPotionItemConversion(ItemPotion var0, PotionHelper.ItemPredicateInstance var1, ItemPotion var2) {
      POTION_ITEM_CONVERSIONS.add(new PotionHelper.MixPredicate(var0, var1, var2));
   }

   private static void registerPotionItem(PotionHelper.ItemPredicateInstance var0) {
      POTION_ITEMS.add(var0);
   }

   private static void registerPotionTypeConversion(PotionType var0, Predicate var1, PotionType var2) {
      POTION_TYPE_CONVERSIONS.add(new PotionHelper.MixPredicate(var0, var1, var2));
   }

   static class ItemPredicateInstance implements Predicate {
      private final Item item;
      private final int meta;

      public ItemPredicateInstance(Item var1) {
         this(var1, -1);
      }

      public ItemPredicateInstance(Item var1, int var2) {
         this.item = var1;
         this.meta = var2;
      }

      public boolean apply(@Nullable ItemStack var1) {
         return var1 != null && var1.getItem() == this.item && (this.meta == -1 || this.meta == var1.getMetadata());
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((ItemStack)var1);
      }
   }

   static class MixPredicate {
      final Object input;
      final Predicate reagent;
      final Object output;

      public MixPredicate(Object var1, Predicate var2, Object var3) {
         this.input = var1;
         this.reagent = var2;
         this.output = var3;
      }
   }
}
