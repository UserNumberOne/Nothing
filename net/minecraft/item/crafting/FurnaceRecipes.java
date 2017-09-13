package net.minecraft.item.crafting;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;

public class FurnaceRecipes {
   private static final FurnaceRecipes SMELTING_BASE = new FurnaceRecipes();
   public Map smeltingList = Maps.newHashMap();
   private final Map experienceList = Maps.newHashMap();
   public Map customRecipes = Maps.newHashMap();
   public Map customExperience = Maps.newHashMap();

   public static FurnaceRecipes instance() {
      return SMELTING_BASE;
   }

   public FurnaceRecipes() {
      this.addSmeltingRecipeForBlock(Blocks.IRON_ORE, new ItemStack(Items.IRON_INGOT), 0.7F);
      this.addSmeltingRecipeForBlock(Blocks.GOLD_ORE, new ItemStack(Items.GOLD_INGOT), 1.0F);
      this.addSmeltingRecipeForBlock(Blocks.DIAMOND_ORE, new ItemStack(Items.DIAMOND), 1.0F);
      this.addSmeltingRecipeForBlock(Blocks.SAND, new ItemStack(Blocks.GLASS), 0.1F);
      this.addSmelting(Items.PORKCHOP, new ItemStack(Items.COOKED_PORKCHOP), 0.35F);
      this.addSmelting(Items.BEEF, new ItemStack(Items.COOKED_BEEF), 0.35F);
      this.addSmelting(Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), 0.35F);
      this.addSmelting(Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), 0.35F);
      this.addSmelting(Items.MUTTON, new ItemStack(Items.COOKED_MUTTON), 0.35F);
      this.addSmeltingRecipeForBlock(Blocks.COBBLESTONE, new ItemStack(Blocks.STONE), 0.1F);
      this.addSmeltingRecipe(new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.DEFAULT_META), new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.CRACKED_META), 0.1F);
      this.addSmelting(Items.CLAY_BALL, new ItemStack(Items.BRICK), 0.3F);
      this.addSmeltingRecipeForBlock(Blocks.CLAY, new ItemStack(Blocks.HARDENED_CLAY), 0.35F);
      this.addSmeltingRecipeForBlock(Blocks.CACTUS, new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()), 0.2F);
      this.addSmeltingRecipeForBlock(Blocks.LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);
      this.addSmeltingRecipeForBlock(Blocks.LOG2, new ItemStack(Items.COAL, 1, 1), 0.15F);
      this.addSmeltingRecipeForBlock(Blocks.EMERALD_ORE, new ItemStack(Items.EMERALD), 1.0F);
      this.addSmelting(Items.POTATO, new ItemStack(Items.BAKED_POTATO), 0.35F);
      this.addSmeltingRecipeForBlock(Blocks.NETHERRACK, new ItemStack(Items.NETHERBRICK), 0.1F);
      this.addSmeltingRecipe(new ItemStack(Blocks.SPONGE, 1, 1), new ItemStack(Blocks.SPONGE, 1, 0), 0.15F);
      this.addSmelting(Items.CHORUS_FRUIT, new ItemStack(Items.CHORUS_FRUIT_POPPED), 0.1F);

      for(ItemFishFood.FishType var4 : ItemFishFood.FishType.values()) {
         if (var4.canCook()) {
            this.addSmeltingRecipe(new ItemStack(Items.FISH, 1, var4.getMetadata()), new ItemStack(Items.COOKED_FISH, 1, var4.getMetadata()), 0.35F);
         }
      }

      this.addSmeltingRecipeForBlock(Blocks.COAL_ORE, new ItemStack(Items.COAL), 0.1F);
      this.addSmeltingRecipeForBlock(Blocks.REDSTONE_ORE, new ItemStack(Items.REDSTONE), 0.7F);
      this.addSmeltingRecipeForBlock(Blocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), 0.2F);
      this.addSmeltingRecipeForBlock(Blocks.QUARTZ_ORE, new ItemStack(Items.QUARTZ), 0.2F);
   }

   public void registerRecipe(ItemStack var1, ItemStack var2, float var3) {
      this.customRecipes.put(var1, var2);
   }

   public void addSmeltingRecipeForBlock(Block var1, ItemStack var2, float var3) {
      this.addSmelting(Item.getItemFromBlock(var1), var2, var3);
   }

   public void addSmelting(Item var1, ItemStack var2, float var3) {
      this.addSmeltingRecipe(new ItemStack(var1, 1, 32767), var2, var3);
   }

   public void addSmeltingRecipe(ItemStack var1, ItemStack var2, float var3) {
      this.smeltingList.put(var1, var2);
      this.experienceList.put(var2, Float.valueOf(var3));
   }

   @Nullable
   public ItemStack getSmeltingResult(ItemStack var1) {
      boolean var2 = false;
      Iterator var3 = this.customRecipes.entrySet().iterator();

      Entry var4;
      while(true) {
         if (!var3.hasNext()) {
            if (var2 || this.smeltingList.isEmpty()) {
               return null;
            }

            var3 = this.smeltingList.entrySet().iterator();
            var2 = true;
         }

         var4 = (Entry)var3.next();
         if (this.compareItemStacks(var1, (ItemStack)var4.getKey())) {
            break;
         }
      }

      return (ItemStack)var4.getValue();
   }

   private boolean compareItemStacks(ItemStack var1, ItemStack var2) {
      return var2.getItem() == var1.getItem() && (var2.getMetadata() == 32767 || var2.getMetadata() == var1.getMetadata());
   }

   public Map getSmeltingList() {
      return this.smeltingList;
   }

   public float getSmeltingExperience(ItemStack var1) {
      boolean var2 = false;
      Iterator var3 = this.customExperience.entrySet().iterator();

      Entry var4;
      while(true) {
         if (!var3.hasNext()) {
            if (var2 || this.experienceList.isEmpty()) {
               return 0.0F;
            }

            var3 = this.experienceList.entrySet().iterator();
            var2 = true;
         }

         var4 = (Entry)var3.next();
         if (this.compareItemStacks(var1, (ItemStack)var4.getKey())) {
            break;
         }
      }

      return ((Float)var4.getValue()).floatValue();
   }
}
