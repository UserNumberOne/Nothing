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
   public void addRecipes(CraftingManager craftingmanager) {
      craftingmanager.addRecipe(new ItemStack(Items.SHIELD), "WoW", "WWW", " W ", 'W', Blocks.PLANKS, 'o', Items.IRON_INGOT);
      craftingmanager.addRecipe(new ShieldRecipes.Decoration((ShieldRecipes.SyntheticClass_1)null));
   }

   static class Decoration extends ShapelessRecipes implements IRecipe {
      private Decoration() {
         super(new ItemStack(Items.SHIELD, 0, 0), Arrays.asList(new ItemStack(Items.BANNER, 0, 0)));
      }

      public boolean matches(InventoryCrafting inventorycrafting, World world) {
         ItemStack itemstack = null;
         ItemStack itemstack1 = null;

         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack2 = inventorycrafting.getStackInSlot(i);
            if (itemstack2 != null) {
               if (itemstack2.getItem() == Items.BANNER) {
                  if (itemstack1 != null) {
                     return false;
                  }

                  itemstack1 = itemstack2;
               } else {
                  if (itemstack2.getItem() != Items.SHIELD) {
                     return false;
                  }

                  if (itemstack != null) {
                     return false;
                  }

                  if (itemstack2.getSubCompound("BlockEntityTag", false) != null) {
                     return false;
                  }

                  itemstack = itemstack2;
               }
            }
         }

         if (itemstack != null && itemstack1 != null) {
            return true;
         } else {
            return false;
         }
      }

      @Nullable
      public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
         ItemStack itemstack = null;

         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack1 = inventorycrafting.getStackInSlot(i);
            if (itemstack1 != null && itemstack1.getItem() == Items.BANNER) {
               itemstack = itemstack1;
            }
         }

         ItemStack itemstack2 = new ItemStack(Items.SHIELD, 1, 0);
         NBTTagCompound nbttagcompound;
         EnumDyeColor enumcolor;
         if (itemstack.hasTagCompound()) {
            nbttagcompound = itemstack.getTagCompound().copy();
            enumcolor = EnumDyeColor.byDyeDamage(TileEntityBanner.getBaseColor(itemstack));
         } else {
            nbttagcompound = new NBTTagCompound();
            enumcolor = EnumDyeColor.byDyeDamage(itemstack.getItemDamage());
         }

         itemstack2.setTagCompound(nbttagcompound);
         TileEntityBanner.addBaseColorTag(itemstack2, enumcolor);
         return itemstack2;
      }

      public int getRecipeSize() {
         return 2;
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

      Decoration(ShieldRecipes.SyntheticClass_1 recipiesshield_syntheticclass_1) {
         this();
      }
   }

   static class SyntheticClass_1 {
   }
}
