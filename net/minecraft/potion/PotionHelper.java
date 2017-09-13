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
      public boolean apply(@Nullable ItemStack p_apply_1_) {
         for(PotionHelper.ItemPredicateInstance potionhelper$itempredicateinstance : PotionHelper.POTION_ITEMS) {
            if (potionhelper$itempredicateinstance.apply(p_apply_1_)) {
               return true;
            }
         }

         return false;
      }
   };

   public static boolean isReagent(ItemStack stack) {
      return isItemConversionReagent(stack) || isTypeConversionReagent(stack);
   }

   protected static boolean isItemConversionReagent(ItemStack stack) {
      int i = 0;

      for(int j = POTION_ITEM_CONVERSIONS.size(); i < j; ++i) {
         if (((PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(i)).reagent.apply(stack)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean isTypeConversionReagent(ItemStack stack) {
      int i = 0;

      for(int j = POTION_TYPE_CONVERSIONS.size(); i < j; ++i) {
         if (((PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(i)).reagent.apply(stack)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasConversions(ItemStack input, ItemStack reagent) {
      return !IS_POTION_ITEM.apply(input) ? false : hasItemConversions(input, reagent) || hasTypeConversions(input, reagent);
   }

   protected static boolean hasItemConversions(ItemStack p_185206_0_, ItemStack p_185206_1_) {
      Item item = p_185206_0_.getItem();
      int i = 0;

      for(int j = POTION_ITEM_CONVERSIONS.size(); i < j; ++i) {
         PotionHelper.MixPredicate mixpredicate = (PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(i);
         if (mixpredicate.input == item && mixpredicate.reagent.apply(p_185206_1_)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean hasTypeConversions(ItemStack p_185209_0_, ItemStack p_185209_1_) {
      PotionType potiontype = PotionUtils.getPotionFromItem(p_185209_0_);
      int i = 0;

      for(int j = POTION_TYPE_CONVERSIONS.size(); i < j; ++i) {
         PotionHelper.MixPredicate mixpredicate = (PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(i);
         if (mixpredicate.input == potiontype && mixpredicate.reagent.apply(p_185209_1_)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static ItemStack doReaction(ItemStack reagent, @Nullable ItemStack potionIn) {
      if (potionIn != null) {
         PotionType potiontype = PotionUtils.getPotionFromItem(potionIn);
         Item item = potionIn.getItem();
         int i = 0;

         for(int j = POTION_ITEM_CONVERSIONS.size(); i < j; ++i) {
            PotionHelper.MixPredicate mixpredicate = (PotionHelper.MixPredicate)POTION_ITEM_CONVERSIONS.get(i);
            if (mixpredicate.input == item && mixpredicate.reagent.apply(reagent)) {
               return PotionUtils.addPotionToItemStack(new ItemStack((Item)mixpredicate.output), potiontype);
            }
         }

         i = 0;

         for(int k = POTION_TYPE_CONVERSIONS.size(); i < k; ++i) {
            PotionHelper.MixPredicate mixpredicate1 = (PotionHelper.MixPredicate)POTION_TYPE_CONVERSIONS.get(i);
            if (mixpredicate1.input == potiontype && mixpredicate1.reagent.apply(reagent)) {
               return PotionUtils.addPotionToItemStack(new ItemStack(item), (PotionType)mixpredicate1.output);
            }
         }
      }

      return potionIn;
   }

   public static void init() {
      Predicate predicate = new PotionHelper.ItemPredicateInstance(Items.NETHER_WART);
      Predicate predicate1 = new PotionHelper.ItemPredicateInstance(Items.GOLDEN_CARROT);
      Predicate predicate2 = new PotionHelper.ItemPredicateInstance(Items.REDSTONE);
      Predicate predicate3 = new PotionHelper.ItemPredicateInstance(Items.FERMENTED_SPIDER_EYE);
      Predicate predicate4 = new PotionHelper.ItemPredicateInstance(Items.RABBIT_FOOT);
      Predicate predicate5 = new PotionHelper.ItemPredicateInstance(Items.GLOWSTONE_DUST);
      Predicate predicate6 = new PotionHelper.ItemPredicateInstance(Items.MAGMA_CREAM);
      Predicate predicate7 = new PotionHelper.ItemPredicateInstance(Items.SUGAR);
      Predicate predicate8 = new PotionHelper.ItemPredicateInstance(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata());
      Predicate predicate9 = new PotionHelper.ItemPredicateInstance(Items.SPECKLED_MELON);
      Predicate predicate10 = new PotionHelper.ItemPredicateInstance(Items.SPIDER_EYE);
      Predicate predicate11 = new PotionHelper.ItemPredicateInstance(Items.GHAST_TEAR);
      Predicate predicate12 = new PotionHelper.ItemPredicateInstance(Items.BLAZE_POWDER);
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.POTIONITEM));
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.SPLASH_POTION));
      registerPotionItem(new PotionHelper.ItemPredicateInstance(Items.LINGERING_POTION));
      registerPotionItemConversion(Items.POTIONITEM, new PotionHelper.ItemPredicateInstance(Items.GUNPOWDER), Items.SPLASH_POTION);
      registerPotionItemConversion(Items.SPLASH_POTION, new PotionHelper.ItemPredicateInstance(Items.DRAGON_BREATH), Items.LINGERING_POTION);
      registerPotionTypeConversion(PotionTypes.WATER, predicate9, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate11, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate4, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate12, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate10, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate7, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate6, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate5, PotionTypes.THICK);
      registerPotionTypeConversion(PotionTypes.WATER, predicate2, PotionTypes.MUNDANE);
      registerPotionTypeConversion(PotionTypes.WATER, predicate, PotionTypes.AWKWARD);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate1, PotionTypes.NIGHT_VISION);
      registerPotionTypeConversion(PotionTypes.NIGHT_VISION, predicate2, PotionTypes.LONG_NIGHT_VISION);
      registerPotionTypeConversion(PotionTypes.NIGHT_VISION, predicate3, PotionTypes.INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.LONG_NIGHT_VISION, predicate3, PotionTypes.LONG_INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.INVISIBILITY, predicate2, PotionTypes.LONG_INVISIBILITY);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate6, PotionTypes.FIRE_RESISTANCE);
      registerPotionTypeConversion(PotionTypes.FIRE_RESISTANCE, predicate2, PotionTypes.LONG_FIRE_RESISTANCE);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate4, PotionTypes.LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, predicate2, PotionTypes.LONG_LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, predicate5, PotionTypes.STRONG_LEAPING);
      registerPotionTypeConversion(PotionTypes.LEAPING, predicate3, PotionTypes.SLOWNESS);
      registerPotionTypeConversion(PotionTypes.LONG_LEAPING, predicate3, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.SLOWNESS, predicate2, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, predicate3, PotionTypes.SLOWNESS);
      registerPotionTypeConversion(PotionTypes.LONG_SWIFTNESS, predicate3, PotionTypes.LONG_SLOWNESS);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate7, PotionTypes.SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, predicate2, PotionTypes.LONG_SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.SWIFTNESS, predicate5, PotionTypes.STRONG_SWIFTNESS);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate8, PotionTypes.WATER_BREATHING);
      registerPotionTypeConversion(PotionTypes.WATER_BREATHING, predicate2, PotionTypes.LONG_WATER_BREATHING);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate9, PotionTypes.HEALING);
      registerPotionTypeConversion(PotionTypes.HEALING, predicate5, PotionTypes.STRONG_HEALING);
      registerPotionTypeConversion(PotionTypes.HEALING, predicate3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.STRONG_HEALING, predicate3, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.HARMING, predicate5, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.POISON, predicate3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.LONG_POISON, predicate3, PotionTypes.HARMING);
      registerPotionTypeConversion(PotionTypes.STRONG_POISON, predicate3, PotionTypes.STRONG_HARMING);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate10, PotionTypes.POISON);
      registerPotionTypeConversion(PotionTypes.POISON, predicate2, PotionTypes.LONG_POISON);
      registerPotionTypeConversion(PotionTypes.POISON, predicate5, PotionTypes.STRONG_POISON);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate11, PotionTypes.REGENERATION);
      registerPotionTypeConversion(PotionTypes.REGENERATION, predicate2, PotionTypes.LONG_REGENERATION);
      registerPotionTypeConversion(PotionTypes.REGENERATION, predicate5, PotionTypes.STRONG_REGENERATION);
      registerPotionTypeConversion(PotionTypes.AWKWARD, predicate12, PotionTypes.STRENGTH);
      registerPotionTypeConversion(PotionTypes.STRENGTH, predicate2, PotionTypes.LONG_STRENGTH);
      registerPotionTypeConversion(PotionTypes.STRENGTH, predicate5, PotionTypes.STRONG_STRENGTH);
      registerPotionTypeConversion(PotionTypes.WATER, predicate3, PotionTypes.WEAKNESS);
      registerPotionTypeConversion(PotionTypes.WEAKNESS, predicate2, PotionTypes.LONG_WEAKNESS);
   }

   public static void registerPotionItemConversion(ItemPotion p_185201_0_, PotionHelper.ItemPredicateInstance p_185201_1_, ItemPotion p_185201_2_) {
      POTION_ITEM_CONVERSIONS.add(new PotionHelper.MixPredicate(p_185201_0_, p_185201_1_, p_185201_2_));
   }

   public static void registerPotionItem(PotionHelper.ItemPredicateInstance p_185202_0_) {
      POTION_ITEMS.add(p_185202_0_);
   }

   public static void registerPotionTypeConversion(PotionType input, Predicate reagentPredicate, PotionType output) {
      POTION_TYPE_CONVERSIONS.add(new PotionHelper.MixPredicate(input, reagentPredicate, output));
   }

   public static class ItemPredicateInstance implements Predicate {
      private final Item item;
      private final int meta;

      public ItemPredicateInstance(Item itemIn) {
         this(itemIn, -1);
      }

      public ItemPredicateInstance(Item itemIn, int metaIn) {
         this.item = itemIn;
         this.meta = metaIn;
      }

      public boolean apply(@Nullable ItemStack p_apply_1_) {
         return p_apply_1_ != null && p_apply_1_.getItem() == this.item && (this.meta == -1 || this.meta == p_apply_1_.getMetadata());
      }
   }

   public static class MixPredicate {
      final Object input;
      final Predicate reagent;
      final Object output;

      public MixPredicate(Object inputIn, Predicate reagentIn, Object outputIn) {
         this.input = inputIn;
         this.reagent = reagentIn;
         this.output = outputIn;
      }
   }
}
