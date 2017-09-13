package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipeRepairItem implements IRecipe {
   public boolean matches(InventoryCrafting var1, World var2) {
      List list = Lists.newArrayList();

      for(int i = 0; i < inv.getSizeInventory(); ++i) {
         ItemStack itemstack = inv.getStackInSlot(i);
         if (itemstack != null) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = (ItemStack)list.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.stackSize != 1 || itemstack.stackSize != 1 || !itemstack1.getItem().isRepairable()) {
                  return false;
               }
            }
         }
      }

      return list.size() == 2;
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      List list = Lists.newArrayList();

      for(int i = 0; i < inv.getSizeInventory(); ++i) {
         ItemStack itemstack = inv.getStackInSlot(i);
         if (itemstack != null) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = (ItemStack)list.get(0);
               if (itemstack.getItem() != itemstack1.getItem() || itemstack1.stackSize != 1 || itemstack.stackSize != 1 || !itemstack1.getItem().isRepairable()) {
                  return null;
               }
            }
         }
      }

      if (list.size() == 2) {
         ItemStack itemstack2 = (ItemStack)list.get(0);
         ItemStack itemstack3 = (ItemStack)list.get(1);
         if (itemstack2.getItem() == itemstack3.getItem() && itemstack2.stackSize == 1 && itemstack3.stackSize == 1 && itemstack2.getItem().isRepairable()) {
            int j = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
            int k = itemstack2.getMaxDamage() - itemstack3.getItemDamage();
            int l = j + k + itemstack2.getMaxDamage() * 5 / 100;
            int i1 = itemstack2.getMaxDamage() - l;
            if (i1 < 0) {
               i1 = 0;
            }

            return new ItemStack(itemstack2.getItem(), 1, i1);
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
      ItemStack[] aitemstack = new ItemStack[inv.getSizeInventory()];

      for(int i = 0; i < aitemstack.length; ++i) {
         ItemStack itemstack = inv.getStackInSlot(i);
         aitemstack[i] = ForgeHooks.getContainerItem(itemstack);
      }

      return aitemstack;
   }
}
