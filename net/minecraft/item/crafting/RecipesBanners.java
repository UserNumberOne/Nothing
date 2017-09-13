package net.minecraft.item.crafting;

import java.util.List;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

public class RecipesBanners {
   void addRecipes(CraftingManager var1) {
      for(EnumDyeColor var5 : EnumDyeColor.values()) {
         var1.addRecipe(new ItemStack(Items.BANNER, 1, var5.getDyeDamage()), "###", "###", " | ", '#', new ItemStack(Blocks.WOOL, 1, var5.getMetadata()), '|', Items.STICK);
      }

      var1.addRecipe(new RecipesBanners.RecipeDuplicatePattern());
      var1.addRecipe(new RecipesBanners.RecipeAddPattern());
   }

   public static class RecipeAddPattern implements IRecipe {
      private static String[] colors = new String[]{"Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"};
      private static List[] colored = new List[colors.length];
      private static List dyes;
      private static boolean hasInit = false;

      private RecipeAddPattern() {
      }

      public boolean matches(InventoryCrafting var1, World var2) {
         boolean var3 = false;

         for(int var4 = 0; var4 < var1.getSizeInventory(); ++var4) {
            ItemStack var5 = var1.getStackInSlot(var4);
            if (var5 != null && var5.getItem() == Items.BANNER) {
               if (var3) {
                  return false;
               }

               if (TileEntityBanner.getPatterns(var5) >= 6) {
                  return false;
               }

               var3 = true;
            }
         }

         if (!var3) {
            return false;
         } else {
            return this.matchPatterns(var1) != null;
         }
      }

      @Nullable
      public ItemStack getCraftingResult(InventoryCrafting var1) {
         ItemStack var2 = null;

         for(int var3 = 0; var3 < var1.getSizeInventory(); ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            if (var4 != null && var4.getItem() == Items.BANNER) {
               var2 = var4.copy();
               var2.stackSize = 1;
               break;
            }
         }

         TileEntityBanner.EnumBannerPattern var8 = this.matchPatterns(var1);
         if (var8 != null) {
            int var9 = 0;

            for(int var5 = 0; var5 < var1.getSizeInventory(); ++var5) {
               ItemStack var6 = var1.getStackInSlot(var5);
               int var7 = this.getColor(var6);
               if (var7 != -1) {
                  var9 = var7;
                  break;
               }
            }

            NBTTagCompound var10 = var2.getSubCompound("BlockEntityTag", true);
            NBTTagList var11;
            if (var10.hasKey("Patterns", 9)) {
               var11 = var10.getTagList("Patterns", 10);
            } else {
               var11 = new NBTTagList();
               var10.setTag("Patterns", var11);
            }

            NBTTagCompound var12 = new NBTTagCompound();
            var12.setString("Pattern", var8.getPatternID());
            var12.setInteger("Color", var9);
            var11.appendTag(var12);
         }

         return var2;
      }

      public int getRecipeSize() {
         return 10;
      }

      @Nullable
      public ItemStack getRecipeOutput() {
         return null;
      }

      public ItemStack[] getRemainingItems(InventoryCrafting var1) {
         ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

         for(int var3 = 0; var3 < var2.length; ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            var2[var3] = ForgeHooks.getContainerItem(var4);
         }

         return var2;
      }

      @Nullable
      private TileEntityBanner.EnumBannerPattern matchPatterns(InventoryCrafting var1) {
         for(TileEntityBanner.EnumBannerPattern var5 : TileEntityBanner.EnumBannerPattern.values()) {
            if (var5.hasValidCrafting()) {
               boolean var6 = true;
               if (var5.hasCraftingStack()) {
                  boolean var7 = false;
                  boolean var8 = false;

                  for(int var9 = 0; var9 < var1.getSizeInventory() && var6; ++var9) {
                     ItemStack var10 = var1.getStackInSlot(var9);
                     if (var10 != null && var10.getItem() != Items.BANNER) {
                        if (this.isDye(var10)) {
                           if (var8) {
                              var6 = false;
                              break;
                           }

                           var8 = true;
                        } else {
                           if (var7 || !var10.isItemEqual(var5.getCraftingStack())) {
                              var6 = false;
                              break;
                           }

                           var7 = true;
                        }
                     }
                  }

                  if (!var7) {
                     var6 = false;
                  }
               } else if (var1.getSizeInventory() == var5.getCraftingLayers().length * var5.getCraftingLayers()[0].length()) {
                  int var12 = -1;

                  for(int var13 = 0; var13 < var1.getSizeInventory() && var6; ++var13) {
                     int var14 = var13 / 3;
                     int var15 = var13 % 3;
                     ItemStack var11 = var1.getStackInSlot(var13);
                     if (var11 != null && var11.getItem() != Items.BANNER) {
                        if (!this.isDye(var11)) {
                           var6 = false;
                           break;
                        }

                        if (var12 != -1 && var12 != var11.getMetadata()) {
                           var6 = false;
                           break;
                        }

                        if (var5.getCraftingLayers()[var14].charAt(var15) == ' ') {
                           var6 = false;
                           break;
                        }

                        var12 = var11.getMetadata();
                     } else if (var5.getCraftingLayers()[var14].charAt(var15) != ' ') {
                        var6 = false;
                        break;
                     }
                  }
               } else {
                  var6 = false;
               }

               if (var6) {
                  return var5;
               }
            }
         }

         return null;
      }

      private static void init() {
         if (!hasInit) {
            for(int var0 = 0; var0 < colors.length; ++var0) {
               colored[var0] = OreDictionary.getOres("dye" + colors[var0]);
            }

            dyes = OreDictionary.getOres("dye");
            hasInit = true;
         }
      }

      private boolean isDye(ItemStack var1) {
         init();

         for(ItemStack var3 : dyes) {
            if (OreDictionary.itemMatches(var3, var1, false)) {
               return true;
            }
         }

         return false;
      }

      private int getColor(ItemStack var1) {
         init();
         if (var1 == null) {
            return -1;
         } else {
            for(int var2 = 0; var2 < colored.length; ++var2) {
               for(ItemStack var4 : colored[var2]) {
                  if (OreDictionary.itemMatches(var4, var1, true)) {
                     return var2;
                  }
               }
            }

            return -1;
         }
      }
   }

   public static class RecipeDuplicatePattern implements IRecipe {
      private RecipeDuplicatePattern() {
      }

      public boolean matches(InventoryCrafting var1, World var2) {
         ItemStack var3 = null;
         ItemStack var4 = null;

         for(int var5 = 0; var5 < var1.getSizeInventory(); ++var5) {
            ItemStack var6 = var1.getStackInSlot(var5);
            if (var6 != null) {
               if (var6.getItem() != Items.BANNER) {
                  return false;
               }

               if (var3 != null && var4 != null) {
                  return false;
               }

               int var7 = TileEntityBanner.getBaseColor(var6);
               boolean var8 = TileEntityBanner.getPatterns(var6) > 0;
               if (var3 != null) {
                  if (var8) {
                     return false;
                  }

                  if (var7 != TileEntityBanner.getBaseColor(var3)) {
                     return false;
                  }

                  var4 = var6;
               } else if (var4 != null) {
                  if (!var8) {
                     return false;
                  }

                  if (var7 != TileEntityBanner.getBaseColor(var4)) {
                     return false;
                  }

                  var3 = var6;
               } else if (var8) {
                  var3 = var6;
               } else {
                  var4 = var6;
               }
            }
         }

         return var3 != null && var4 != null;
      }

      @Nullable
      public ItemStack getCraftingResult(InventoryCrafting var1) {
         for(int var2 = 0; var2 < var1.getSizeInventory(); ++var2) {
            ItemStack var3 = var1.getStackInSlot(var2);
            if (var3 != null && TileEntityBanner.getPatterns(var3) > 0) {
               ItemStack var4 = var3.copy();
               var4.stackSize = 1;
               return var4;
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

      public ItemStack[] getRemainingItems(InventoryCrafting var1) {
         ItemStack[] var2 = new ItemStack[var1.getSizeInventory()];

         for(int var3 = 0; var3 < var2.length; ++var3) {
            ItemStack var4 = var1.getStackInSlot(var3);
            if (var4 != null) {
               if (var4.getItem().hasContainerItem(var4)) {
                  var2[var3] = ForgeHooks.getContainerItem(var4);
               } else if (var4.hasTagCompound() && TileEntityBanner.getPatterns(var4) > 0) {
                  var2[var3] = var4.copy();
                  var2[var3].stackSize = 1;
               }
            }
         }

         return var2;
      }
   }
}
