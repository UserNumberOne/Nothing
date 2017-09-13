package net.minecraft.item.crafting;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.world.World;

public class RecipesBanners {
   void addRecipes(CraftingManager craftingmanager) {
      for(EnumDyeColor enumcolor : EnumDyeColor.values()) {
         craftingmanager.addRecipe(new ItemStack(Items.BANNER, 1, enumcolor.getDyeDamage()), "###", "###", " | ", '#', new ItemStack(Blocks.WOOL, 1, enumcolor.getMetadata()), '|', Items.STICK);
      }

      craftingmanager.addRecipe(new RecipesBanners.RecipeDuplicatePattern((RecipesBanners.SyntheticClass_1)null));
      craftingmanager.addRecipe(new RecipesBanners.RecipeAddPattern((RecipesBanners.SyntheticClass_1)null));
   }

   static class RecipeAddPattern extends ShapelessRecipes implements IRecipe {
      private RecipeAddPattern() {
         super(new ItemStack(Items.BANNER, 0, 0), Arrays.asList(new ItemStack(Items.BANNER)));
      }

      public boolean matches(InventoryCrafting inventorycrafting, World world) {
         boolean flag = false;

         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack = inventorycrafting.getStackInSlot(i);
            if (itemstack != null && itemstack.getItem() == Items.BANNER) {
               if (flag) {
                  return false;
               }

               if (TileEntityBanner.getPatterns(itemstack) >= 6) {
                  return false;
               }

               flag = true;
            }
         }

         if (!flag) {
            return false;
         } else if (this.matchPatterns(inventorycrafting) != null) {
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
               itemstack = itemstack1.copy();
               itemstack.stackSize = 1;
               break;
            }
         }

         TileEntityBanner.EnumBannerPattern tileentitybanner_enumbannerpatterntype = this.matchPatterns(inventorycrafting);
         if (tileentitybanner_enumbannerpatterntype != null) {
            int j = 0;

            for(int k = 0; k < inventorycrafting.getSizeInventory(); ++k) {
               ItemStack itemstack2 = inventorycrafting.getStackInSlot(k);
               if (itemstack2 != null && itemstack2.getItem() == Items.DYE) {
                  j = itemstack2.getMetadata();
                  break;
               }
            }

            NBTTagCompound nbttagcompound = itemstack.getSubCompound("BlockEntityTag", true);
            NBTTagList nbttaglist;
            if (nbttagcompound.hasKey("Patterns", 9)) {
               nbttaglist = nbttagcompound.getTagList("Patterns", 10);
            } else {
               nbttaglist = new NBTTagList();
               nbttagcompound.setTag("Patterns", nbttaglist);
            }

            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setString("Pattern", tileentitybanner_enumbannerpatterntype.getPatternID());
            nbttagcompound1.setInteger("Color", j);
            nbttaglist.appendTag(nbttagcompound1);
         }

         return itemstack;
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

      @Nullable
      private TileEntityBanner.EnumBannerPattern matchPatterns(InventoryCrafting inventorycrafting) {
         for(TileEntityBanner.EnumBannerPattern tileentitybanner_enumbannerpatterntype : TileEntityBanner.EnumBannerPattern.values()) {
            if (tileentitybanner_enumbannerpatterntype.hasValidCrafting()) {
               boolean flag = true;
               if (tileentitybanner_enumbannerpatterntype.hasCraftingStack()) {
                  boolean flag1 = false;
                  boolean flag2 = false;

                  for(int k = 0; k < inventorycrafting.getSizeInventory() && flag; ++k) {
                     ItemStack itemstack = inventorycrafting.getStackInSlot(k);
                     if (itemstack != null && itemstack.getItem() != Items.BANNER) {
                        if (itemstack.getItem() == Items.DYE) {
                           if (flag2) {
                              flag = false;
                              break;
                           }

                           flag2 = true;
                        } else {
                           if (flag1 || !itemstack.isItemEqual(tileentitybanner_enumbannerpatterntype.getCraftingStack())) {
                              flag = false;
                              break;
                           }

                           flag1 = true;
                        }
                     }
                  }

                  if (!flag1) {
                     flag = false;
                  }
               } else if (inventorycrafting.getSizeInventory() != tileentitybanner_enumbannerpatterntype.getCraftingLayers().length * tileentitybanner_enumbannerpatterntype.getCraftingLayers()[0].length()) {
                  flag = false;
               } else {
                  int l = -1;

                  for(int i1 = 0; i1 < inventorycrafting.getSizeInventory() && flag; ++i1) {
                     int k = i1 / 3;
                     int j1 = i1 % 3;
                     ItemStack itemstack1 = inventorycrafting.getStackInSlot(i1);
                     if (itemstack1 != null && itemstack1.getItem() != Items.BANNER) {
                        if (itemstack1.getItem() != Items.DYE) {
                           flag = false;
                           break;
                        }

                        if (l != -1 && l != itemstack1.getMetadata()) {
                           flag = false;
                           break;
                        }

                        if (tileentitybanner_enumbannerpatterntype.getCraftingLayers()[k].charAt(j1) == ' ') {
                           flag = false;
                           break;
                        }

                        l = itemstack1.getMetadata();
                     } else if (tileentitybanner_enumbannerpatterntype.getCraftingLayers()[k].charAt(j1) != ' ') {
                        flag = false;
                        break;
                     }
                  }
               }

               if (flag) {
                  return tileentitybanner_enumbannerpatterntype;
               }
            }
         }

         return null;
      }

      RecipeAddPattern(RecipesBanners.SyntheticClass_1 recipesbanner_syntheticclass_1) {
         this();
      }
   }

   static class RecipeDuplicatePattern extends ShapelessRecipes implements IRecipe {
      private RecipeDuplicatePattern() {
         super(new ItemStack(Items.BANNER, 0, 0), Arrays.asList(new ItemStack(Items.DYE, 0, 5)));
      }

      public boolean matches(InventoryCrafting inventorycrafting, World world) {
         ItemStack itemstack = null;
         ItemStack itemstack1 = null;

         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack2 = inventorycrafting.getStackInSlot(i);
            if (itemstack2 != null) {
               if (itemstack2.getItem() != Items.BANNER) {
                  return false;
               }

               if (itemstack != null && itemstack1 != null) {
                  return false;
               }

               int j = TileEntityBanner.getBaseColor(itemstack2);
               boolean flag = TileEntityBanner.getPatterns(itemstack2) > 0;
               if (itemstack != null) {
                  if (flag) {
                     return false;
                  }

                  if (j != TileEntityBanner.getBaseColor(itemstack)) {
                     return false;
                  }

                  itemstack1 = itemstack2;
               } else if (itemstack1 != null) {
                  if (!flag) {
                     return false;
                  }

                  if (j != TileEntityBanner.getBaseColor(itemstack1)) {
                     return false;
                  }

                  itemstack = itemstack2;
               } else if (flag) {
                  itemstack = itemstack2;
               } else {
                  itemstack1 = itemstack2;
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
         for(int i = 0; i < inventorycrafting.getSizeInventory(); ++i) {
            ItemStack itemstack = inventorycrafting.getStackInSlot(i);
            if (itemstack != null && TileEntityBanner.getPatterns(itemstack) > 0) {
               ItemStack itemstack1 = itemstack.copy();
               itemstack1.stackSize = 1;
               return itemstack1;
            }
         }

         return null;
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
            if (itemstack != null) {
               if (itemstack.getItem().hasContainerItem()) {
                  aitemstack[i] = new ItemStack(itemstack.getItem().getContainerItem());
               } else if (itemstack.hasTagCompound() && TileEntityBanner.getPatterns(itemstack) > 0) {
                  aitemstack[i] = itemstack.copy();
                  aitemstack[i].stackSize = 1;
               }
            }
         }

         return aitemstack;
      }

      RecipeDuplicatePattern(RecipesBanners.SyntheticClass_1 recipesbanner_syntheticclass_1) {
         this();
      }
   }

   static class SyntheticClass_1 {
   }
}
