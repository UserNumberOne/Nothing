package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipeFireworks implements IRecipe {
   private ItemStack resultItem;

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
            NBTTagCompound var18 = new NBTTagCompound();
            if (var6 > 0) {
               NBTTagCompound var22 = new NBTTagCompound();
               NBTTagList var26 = new NBTTagList();

               for(int var27 = 0; var27 < var1.getSizeInventory(); ++var27) {
                  ItemStack var29 = var1.getStackInSlot(var27);
                  if (var29 != null && var29.getItem() == Items.FIREWORK_CHARGE && var29.hasTagCompound() && var29.getTagCompound().hasKey("Explosion", 10)) {
                     var26.appendTag(var29.getTagCompound().getCompoundTag("Explosion"));
                  }
               }

               var22.setTag("Explosions", var26);
               var22.setByte("Flight", (byte)var4);
               var18.setTag("Fireworks", var22);
            }

            this.resultItem.setTagCompound(var18);
            return true;
         } else if (var4 == 1 && var3 == 0 && var6 == 0 && var5 > 0 && var8 <= 1) {
            this.resultItem = new ItemStack(Items.FIREWORK_CHARGE);
            NBTTagCompound var17 = new NBTTagCompound();
            NBTTagCompound var21 = new NBTTagCompound();
            byte var25 = 0;
            ArrayList var12 = Lists.newArrayList();

            for(int var13 = 0; var13 < var1.getSizeInventory(); ++var13) {
               ItemStack var14 = var1.getStackInSlot(var13);
               if (var14 != null) {
                  if (var14.getItem() == Items.DYE) {
                     var12.add(Integer.valueOf(ItemDye.DYE_COLORS[var14.getMetadata() & 15]));
                  } else if (var14.getItem() == Items.GLOWSTONE_DUST) {
                     var21.setBoolean("Flicker", true);
                  } else if (var14.getItem() == Items.DIAMOND) {
                     var21.setBoolean("Trail", true);
                  } else if (var14.getItem() == Items.FIRE_CHARGE) {
                     var25 = 1;
                  } else if (var14.getItem() == Items.FEATHER) {
                     var25 = 4;
                  } else if (var14.getItem() == Items.GOLD_NUGGET) {
                     var25 = 2;
                  } else if (var14.getItem() == Items.SKULL) {
                     var25 = 3;
                  }
               }
            }

            int[] var28 = new int[var12.size()];

            for(int var30 = 0; var30 < var28.length; ++var30) {
               var28[var30] = ((Integer)var12.get(var30)).intValue();
            }

            var21.setIntArray("Colors", var28);
            var21.setByte("Type", var25);
            var17.setTag("Explosion", var21);
            this.resultItem.setTagCompound(var17);
            return true;
         } else if (var4 == 0 && var3 == 0 && var6 == 1 && var5 > 0 && var5 == var7) {
            ArrayList var16 = Lists.newArrayList();

            for(int var19 = 0; var19 < var1.getSizeInventory(); ++var19) {
               ItemStack var11 = var1.getStackInSlot(var19);
               if (var11 != null) {
                  if (var11.getItem() == Items.DYE) {
                     var16.add(Integer.valueOf(ItemDye.DYE_COLORS[var11.getMetadata() & 15]));
                  } else if (var11.getItem() == Items.FIREWORK_CHARGE) {
                     this.resultItem = var11.copy();
                     this.resultItem.stackSize = 1;
                  }
               }
            }

            int[] var20 = new int[var16.size()];

            for(int var23 = 0; var23 < var20.length; ++var23) {
               var20[var23] = ((Integer)var16.get(var23)).intValue();
            }

            if (this.resultItem != null && this.resultItem.hasTagCompound()) {
               NBTTagCompound var24 = this.resultItem.getTagCompound().getCompoundTag("Explosion");
               if (var24 == null) {
                  return false;
               } else {
                  var24.setIntArray("FadeColors", var20);
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
         var2[var3] = ForgeHooks.getContainerItem(var4);
      }

      return var2;
   }
}
