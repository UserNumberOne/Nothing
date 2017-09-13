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

      for(ItemFishFood.FishType itemfish_enumfish : ItemFishFood.FishType.values()) {
         if (itemfish_enumfish.canCook()) {
            this.addSmeltingRecipe(new ItemStack(Items.FISH, 1, itemfish_enumfish.getMetadata()), new ItemStack(Items.COOKED_FISH, 1, itemfish_enumfish.getMetadata()), 0.35F);
         }
      }

      this.addSmeltingRecipeForBlock(Blocks.COAL_ORE, new ItemStack(Items.COAL), 0.1F);
      this.addSmeltingRecipeForBlock(Blocks.REDSTONE_ORE, new ItemStack(Items.REDSTONE), 0.7F);
      this.addSmeltingRecipeForBlock(Blocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), 0.2F);
      this.addSmeltingRecipeForBlock(Blocks.QUARTZ_ORE, new ItemStack(Items.QUARTZ), 0.2F);
   }

   public void registerRecipe(ItemStack itemstack, ItemStack itemstack1, float f) {
      this.customRecipes.put(itemstack, itemstack1);
   }

   public void addSmeltingRecipeForBlock(Block block, ItemStack itemstack, float f) {
      this.addSmelting(Item.getItemFromBlock(block), itemstack, f);
   }

   public void addSmelting(Item item, ItemStack itemstack, float f) {
      this.addSmeltingRecipe(new ItemStack(item, 1, 32767), itemstack, f);
   }

   public void addSmeltingRecipe(ItemStack itemstack, ItemStack itemstack1, float f) {
      this.smeltingList.put(itemstack, itemstack1);
      this.experienceList.put(itemstack1, Float.valueOf(f));
   }

   @Nullable
   public ItemStack getSmeltingResult(ItemStack itemstack) {
      boolean vanilla = false;
      Iterator iterator = this.customRecipes.entrySet().iterator();

      Entry entry;
      while(true) {
         if (!iterator.hasNext()) {
            if (vanilla || this.smeltingList.isEmpty()) {
               return null;
            }

            iterator = this.smeltingList.entrySet().iterator();
            vanilla = true;
         }

         entry = (Entry)iterator.next();
         if (this.compareItemStacks(itemstack, (ItemStack)entry.getKey())) {
            break;
         }
      }

      return (ItemStack)entry.getValue();
   }

   private boolean compareItemStacks(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack1.getItem() == itemstack.getItem() && (itemstack1.getMetadata() == 32767 || itemstack1.getMetadata() == itemstack.getMetadata());
   }

   public Map getSmeltingList() {
      return this.smeltingList;
   }

   public float getSmeltingExperience(ItemStack itemstack) {
      boolean vanilla = false;
      Iterator iterator = this.customExperience.entrySet().iterator();

      Entry entry;
      while(true) {
         if (!iterator.hasNext()) {
            if (vanilla || this.experienceList.isEmpty()) {
               return 0.0F;
            }

            iterator = this.experienceList.entrySet().iterator();
            vanilla = true;
         }

         entry = (Entry)iterator.next();
         if (this.compareItemStacks(itemstack, (ItemStack)entry.getKey())) {
            break;
         }
      }

      return ((Float)entry.getValue()).floatValue();
   }
}
