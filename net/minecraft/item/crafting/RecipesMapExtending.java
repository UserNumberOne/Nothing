package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class RecipesMapExtending extends ShapedRecipes {
   public RecipesMapExtending() {
      super(3, 3, new ItemStack[]{new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.FILLED_MAP, 0, 32767), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.PAPER)}, new ItemStack(Items.MAP, 0, 0));
   }

   public boolean matches(InventoryCrafting var1, World var2) {
      if (!super.matches(var1, var2)) {
         return false;
      } else {
         ItemStack var3 = null;

         for(int var4 = 0; var4 < var1.getSizeInventory() && var3 == null; ++var4) {
            ItemStack var5 = var1.getStackInSlot(var4);
            if (var5 != null && var5.getItem() == Items.FILLED_MAP) {
               var3 = var5;
            }
         }

         if (var3 == null) {
            return false;
         } else {
            MapData var6 = Items.FILLED_MAP.getMapData(var3, var2);
            return var6 == null ? false : var6.scale < 4;
         }
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      ItemStack var2 = null;

      for(int var3 = 0; var3 < var1.getSizeInventory() && var2 == null; ++var3) {
         ItemStack var4 = var1.getStackInSlot(var3);
         if (var4 != null && var4.getItem() == Items.FILLED_MAP) {
            var2 = var4;
         }
      }

      var2 = var2.copy();
      var2.stackSize = 1;
      if (var2.getTagCompound() == null) {
         var2.setTagCompound(new NBTTagCompound());
      }

      var2.getTagCompound().setInteger("map_scale_direction", 1);
      return var2;
   }
}
