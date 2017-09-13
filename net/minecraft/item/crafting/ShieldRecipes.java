package net.minecraft.item.crafting;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.world.World;

public class ShieldRecipes {
   public void addRecipes(CraftingManager var1) {
      var1.addRecipe(new ItemStack(Items.SHIELD), "WoW", "WWW", " W ", 'W', Blocks.PLANKS, 'o', Items.IRON_INGOT);
      var1.addRecipe(new ShieldRecipes.Decoration((ShieldRecipes.SyntheticClass_1)null));
   }

   static class Decoration extends ShapelessRecipes implements IRecipe {
      private Decoration() {
         super(new ItemStack(Items.SHIELD, 0, 0), Arrays.asList(new ItemStack(Items.BANNER, 0, 0)));
      }

      public boolean matches(InventoryCrafting var1, World var2) {
         ItemStack var3 = null;
         ItemStack var4 = null;

         for(int var5 = 0; var5 < var1.getSizeInventory(); ++var5) {
            ItemStack var6 = var1.getStackInSlot(var5);
            if (var6 != null) {
               if (var6.getItem() == Items.BANNER) {
                  if (var4 != null) {
                     return false;
                  }

                  var4 = var6;
               } else {
                  if (var6.getItem() != Items.SHIELD) {
                     return false;
                  }

                  if (var3 != null) {
                     return false;
                  }

                  if (var6.getSubCompound("BlockEntityTag", false) != null) {
                     return false;
                  }

                  var3 = var6;
               }
            }
         }

         if (var3 != null && var4 != null) {
            return true;
         } else {
            return false;
         }
      }

      @Nullable
      public ItemStack getCraftingResult(InventoryCrafting var1) {
         ItemStack var2 = null;

         for(int var3 = 0; var3 < var1.getSizeInventory(); ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            if (var4 != null && var4.getItem() == Items.BANNER) {
               var2 = var4;
            }
         }

         ItemStack var6 = new ItemStack(Items.SHIELD, 1, 0);
         NBTTagCompound var5;
         EnumDyeColor var7;
         if (var2.hasTagCompound()) {
            var5 = var2.getTagCompound().copy();
            var7 = EnumDyeColor.byDyeDamage(TileEntityBanner.getBaseColor(var2));
         } else {
            var5 = new NBTTagCompound();
            var7 = EnumDyeColor.byDyeDamage(var2.getItemDamage());
         }

         var6.setTagCompound(var5);
         TileEntityBanner.addBaseColorTag(var6, var7);
         return var6;
      }

      public int getRecipeSize() {
         return 2;
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

      Decoration(ShieldRecipes.SyntheticClass_1 var1) {
         this();
      }
   }

   static class SyntheticClass_1 {
   }
}
