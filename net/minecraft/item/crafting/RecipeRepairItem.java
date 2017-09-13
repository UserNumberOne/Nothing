package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class RecipeRepairItem extends ShapelessRecipes implements IRecipe {
   public RecipeRepairItem() {
      super(new ItemStack(Items.LEATHER_HELMET), Arrays.asList(new ItemStack(Items.LEATHER_HELMET)));
   }

   public boolean matches(InventoryCrafting inventorycrafting, World world) {
      ArrayList arraylist = Lists.newArrayList();

      for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
         ItemStack itemstack = inventorycrafting.getStackInSlot(i);
         if (itemstack != null) {
            arraylist.add(itemstack);
            if (arraylist.size() > 1) {
               ItemStack itemstack1 = (ItemStack)arraylist.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.stackSize != 1 || itemstack.stackSize != 1 || !itemstack1.getItem().isDamageable()) {
                  return false;
               }
            }
         }
      }

      if (arraylist.size() == 2) {
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      ArrayList arraylist = Lists.newArrayList();

      for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
         ItemStack itemstack = inventorycrafting.getStackInSlot(i);
         if (itemstack != null) {
            arraylist.add(itemstack);
            if (arraylist.size() > 1) {
               ItemStack itemstack1 = (ItemStack)arraylist.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.stackSize != 1 || itemstack.stackSize != 1 || !itemstack1.getItem().isDamageable()) {
                  return null;
               }
            }
         }
      }

      if (arraylist.size() == 2) {
         ItemStack itemstack2 = (ItemStack)arraylist.get(0);
         ItemStack itemstack = (ItemStack)arraylist.get(1);
         if (itemstack2.getItem() == itemstack.getItem() && itemstack2.stackSize == 1 && itemstack.stackSize == 1 && itemstack2.getItem().isDamageable()) {
            Item item = itemstack2.getItem();
            int j = item.getMaxDamage() - itemstack2.getItemDamage();
            int k = item.getMaxDamage() - itemstack.getItemDamage();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            int i1 = item.getMaxDamage() - l;
            if (i1 < 0) {
               i1 = 0;
            }

            ItemStack result = new ItemStack(itemstack.getItem(), 1, i1);
            List ingredients = new ArrayList();
            ingredients.add(itemstack2.copy());
            ingredients.add(itemstack.copy());
            ShapelessRecipes recipe = new ShapelessRecipes(result.copy(), ingredients);
            inventorycrafting.currentRecipe = recipe;
            result = CraftEventFactory.callPreCraftEvent(inventorycrafting, result, CraftingManager.getInstance().lastCraftView, true);
            return result;
         }
      }

      return null;
   }

   public int getRecipeSize() {
      return 4;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return null;
   }

   public ItemStack[] getRemainingItems(InventoryCrafting inventorycrafting) {
      ItemStack[] aitemstack = new ItemStack[inventorycrafting.getSizeInventory()];

      for(int i = 0; i < aitemstack.length; ++i) {
         ItemStack itemstack = inventorycrafting.getStackInSlot(i);
         if (itemstack != null && itemstack.getItem().hasContainerItem()) {
            aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
         }
      }

      return aitemstack;
   }
}
