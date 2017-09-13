package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class RecipeFireworks extends ShapelessRecipes implements IRecipe {
   private ItemStack resultItem;

   public RecipeFireworks() {
      super(new ItemStack(Items.FIREWORKS, 0, 0), Arrays.asList(new ItemStack(Items.GUNPOWDER, 0, 5)));
   }

   public boolean matches(InventoryCrafting var1, World var2) {
      this.resultItem = null;
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      int var7 = 0;
      int var8 = 0;

      for(int var9 = 0; var9 < var1.getSizeInventory(); ++var9) {
         ItemStack var10 = var1.getStackInSlot(var9);
         if (var10 != null) {
            if (var10.getItem() == Items.GUNPOWDER) {
               ++var4;
            } else if (var10.getItem() == Items.FIREWORK_CHARGE) {
               ++var6;
            } else if (var10.getItem() == Items.DYE) {
               ++var5;
            } else if (var10.getItem() == Items.PAPER) {
               ++var3;
            } else if (var10.getItem() == Items.GLOWSTONE_DUST) {
               ++var7;
            } else if (var10.getItem() == Items.DIAMOND) {
               ++var7;
            } else if (var10.getItem() == Items.FIRE_CHARGE) {
               ++var8;
            } else if (var10.getItem() == Items.FEATHER) {
               ++var8;
            } else if (var10.getItem() == Items.GOLD_NUGGET) {
               ++var8;
            } else {
               if (var10.getItem() != Items.SKULL) {
                  return false;
               }

               ++var8;
            }
         }
      }

      var7 = var7 + var5 + var8;
      if (var4 <= 3 && var3 <= 1) {
         if (var4 >= 1 && var3 == 1 && var7 == 0) {
            this.resultItem = new ItemStack(Items.FIREWORKS, 3);
            if (var6 > 0) {
               NBTTagCompound var17 = new NBTTagCompound();
               NBTTagCompound var19 = new NBTTagCompound();
               NBTTagList var21 = new NBTTagList();

               for(int var24 = 0; var24 < var1.getSizeInventory(); ++var24) {
                  ItemStack var29 = var1.getStackInSlot(var24);
                  if (var29 != null && var29.getItem() == Items.FIREWORK_CHARGE && var29.hasTagCompound() && var29.getTagCompound().hasKey("Explosion", 10)) {
                     var21.appendTag(var29.getTagCompound().getCompoundTag("Explosion"));
                  }
               }

               var19.setTag("Explosions", var21);
               var19.setByte("Flight", (byte)var4);
               var17.setTag("Fireworks", var19);
               this.resultItem.setTagCompound(var17);
            }

            return true;
         } else if (var4 == 1 && var3 == 0 && var6 == 0 && var5 > 0 && var8 <= 1) {
            this.resultItem = new ItemStack(Items.FIREWORK_CHARGE);
            NBTTagCompound var16 = new NBTTagCompound();
            NBTTagCompound var18 = new NBTTagCompound();
            byte var20 = 0;
            ArrayList var23 = Lists.newArrayList();

            for(int var27 = 0; var27 < var1.getSizeInventory(); ++var27) {
               ItemStack var14 = var1.getStackInSlot(var27);
               if (var14 != null) {
                  if (var14.getItem() == Items.DYE) {
                     var23.add(Integer.valueOf(ItemDye.DYE_COLORS[var14.getMetadata() & 15]));
                  } else if (var14.getItem() == Items.GLOWSTONE_DUST) {
                     var18.setBoolean("Flicker", true);
                  } else if (var14.getItem() == Items.DIAMOND) {
                     var18.setBoolean("Trail", true);
                  } else if (var14.getItem() == Items.FIRE_CHARGE) {
                     var20 = 1;
                  } else if (var14.getItem() == Items.FEATHER) {
                     var20 = 4;
                  } else if (var14.getItem() == Items.GOLD_NUGGET) {
                     var20 = 2;
                  } else if (var14.getItem() == Items.SKULL) {
                     var20 = 3;
                  }
               }
            }

            int[] var28 = new int[var23.size()];

            for(int var30 = 0; var30 < var28.length; ++var30) {
               var28[var30] = ((Integer)var23.get(var30)).intValue();
            }

            var18.setIntArray("Colors", var28);
            var18.setByte("Type", var20);
            var16.setTag("Explosion", var18);
            this.resultItem.setTagCompound(var16);
            return true;
         } else if (var4 == 0 && var3 == 0 && var6 == 1 && var5 > 0 && var5 == var7) {
            ArrayList var11 = Lists.newArrayList();

            for(int var12 = 0; var12 < var1.getSizeInventory(); ++var12) {
               ItemStack var13 = var1.getStackInSlot(var12);
               if (var13 != null) {
                  if (var13.getItem() == Items.DYE) {
                     var11.add(Integer.valueOf(ItemDye.DYE_COLORS[var13.getMetadata() & 15]));
                  } else if (var13.getItem() == Items.FIREWORK_CHARGE) {
                     this.resultItem = var13.copy();
                     this.resultItem.stackSize = 1;
                  }
               }
            }

            int[] var22 = new int[var11.size()];

            for(int var25 = 0; var25 < var22.length; ++var25) {
               var22[var25] = ((Integer)var11.get(var25)).intValue();
            }

            if (this.resultItem != null && this.resultItem.hasTagCompound()) {
               NBTTagCompound var26 = this.resultItem.getTagCompound().getCompoundTag("Explosion");
               if (var26 == null) {
                  return false;
               } else {
                  var26.setIntArray("FadeColors", var22);
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack getCraftingResult(InventoryCrafting var1) {
      return this.resultItem.copy();
   }

   public int getRecipeSize() {
      return 10;
   }

   @Nullable
   public ItemStack getRecipeOutput() {
      return this.resultItem;
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
