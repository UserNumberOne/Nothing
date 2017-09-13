package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipesArmorDyes extends ShapelessRecipes implements IRecipe {
   public RecipesArmorDyes() {
      super(new ItemStack(Items.LEATHER_HELMET, 0, 0), Arrays.asList(new ItemStack(Items.DYE, 0, 5)));
   }

   public boolean matches(InventoryCrafting inventorycrafting, World world) {
      ItemStack itemstack = null;
      ArrayList arraylist = Lists.newArrayList();

      for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
         ItemStack itemstack1 = inventorycrafting.getStackInSlot(i);
         if (itemstack1 != null) {
            if (itemstack1.getItem() instanceof ItemArmor) {
               ItemArmor itemarmor = (ItemArmor)itemstack1.getItem();
               if (itemarmor.getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER || itemstack != null) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (itemstack1.getItem() != Items.DYE) {
                  return false;
               }

               arraylist.add(itemstack1);
            }
         }
      }

      if (itemstack != null && !arraylist.isEmpty()) {
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
      ItemStack itemstack = null;
      int[] aint = new int[3];
      int i = 0;
      int j = 0;
      ItemArmor itemarmor = null;

      for(int k = 0; k < inventorycrafting.getSizeInventory(); ++k) {
         ItemStack itemstack1 = inventorycrafting.getStackInSlot(k);
         if (itemstack1 != null) {
            if (itemstack1.getItem() instanceof ItemArmor) {
               itemarmor = (ItemArmor)itemstack1.getItem();
               if (itemarmor.getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER || itemstack != null) {
                  return null;
               }

               itemstack = itemstack1.copy();
               itemstack.stackSize = 1;
               if (itemarmor.hasColor(itemstack1)) {
                  int l = itemarmor.getColor(itemstack);
                  float f = (float)(l >> 16 & 255) / 255.0F;
                  float f1 = (float)(l >> 8 & 255) / 255.0F;
                  float f2 = (float)(l & 255) / 255.0F;
                  i = (int)((float)i + Math.max(f, Math.max(f1, f2)) * 255.0F);
                  aint[0] = (int)((float)aint[0] + f * 255.0F);
                  aint[1] = (int)((float)aint[1] + f1 * 255.0F);
                  aint[2] = (int)((float)aint[2] + f2 * 255.0F);
                  ++j;
               }
            } else {
               if (itemstack1.getItem() != Items.DYE) {
                  return null;
               }

               float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(itemstack1.getMetadata()));
               int j1 = (int)(afloat[0] * 255.0F);
               int k1 = (int)(afloat[1] * 255.0F);
               int i1 = (int)(afloat[2] * 255.0F);
               i += Math.max(j1, Math.max(k1, i1));
               aint[0] += j1;
               aint[1] += k1;
               aint[2] += i1;
               ++j;
            }
         }
      }

      if (itemarmor == null) {
         return null;
      } else {
         int var16 = aint[0] / j;
         int l1 = aint[1] / j;
         int l = aint[2] / j;
         float f = (float)i / (float)j;
         float f1 = (float)Math.max(var16, Math.max(l1, l));
         var16 = (int)((float)var16 * f / f1);
         l1 = (int)((float)l1 * f / f1);
         l = (int)((float)l * f / f1);
         int i1 = (var16 << 8) + l1;
         i1 = (i1 << 8) + l;
         itemarmor.setColor(itemstack, i1);
         return itemstack;
      }
   }

   public int getRecipeSize() {
      return 10;
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
