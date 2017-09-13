package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
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

   public boolean matches(InventoryCrafting var1, World var2) {
      ArrayList var3 = Lists.newArrayList();

      for(int var4 = 0; var4 < var1.getSizeInventory(); ++var4) {
         ItemStack var5 = var1.getStackInSlot(var4);
         if (var5 != null) {
            var3.add(var5);
            if (var3.size() > 1) {
               ItemStack var6 = (ItemStack)var3.get(0);
               if (var5.getItem() != var6.getItem() || var6.stackSize != 1 || var5.stackSize != 1 || !var6.getItem().isDamageable()) {
                  return false;
               }
            }
         }
      }

      if (var3.size() == 2) {
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      ArrayList var2 = Lists.newArrayList();

      for(int var3 = 0; var3 < var1.getSizeInventory(); ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         if (var4 != null) {
            var2.add(var4);
            if (var2.size() > 1) {
               ItemStack var5 = (ItemStack)var2.get(0);
               if (var4.getItem() != var5.getItem() || var5.stackSize != 1 || var4.stackSize != 1 || !var5.getItem().isDamageable()) {
                  return null;
               }
            }
         }
      }

      if (var2.size() == 2) {
         ItemStack var13 = (ItemStack)var2.get(0);
         ItemStack var14 = (ItemStack)var2.get(1);
         if (var13.getItem() == var14.getItem() && var13.stackSize == 1 && var14.stackSize == 1 && var13.getItem().isDamageable()) {
            Item var15 = var13.getItem();
            int var6 = var15.getMaxDamage() - var13.getItemDamage();
            int var7 = var15.getMaxDamage() - var14.getItemDamage();
            int var8 = var6 + var7 + var15.getMaxDamage() * 5 / 100;
            int var9 = var15.getMaxDamage() - var8;
            if (var9 < 0) {
               var9 = 0;
            }

            ItemStack var10 = new ItemStack(var14.getItem(), 1, var9);
            ArrayList var11 = new ArrayList();
            var11.add(var13.copy());
            var11.add(var14.copy());
            ShapelessRecipes var12 = new ShapelessRecipes(var10.copy(), var11);
            var1.currentRecipe = var12;
            var10 = CraftEventFactory.callPreCraftEvent(var1, var10, CraftingManager.getInstance().lastCraftView, true);
            return var10;
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

   public ItemStack[] getRemainingItems(InventoryCrafting var1) {
      ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

      for(int var3 = 0; var3 < var2.length; ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         if (var4 != null && var4.getItem().hasContainerItem()) {
            var2[var3] = new ItemStack(var4.getItem().getContainerItem());
         }
      }

      return var2;
   }
}
