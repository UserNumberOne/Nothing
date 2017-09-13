package net.minecraft.inventory;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ContainerEnchantment extends Container {
   public IInventory tableInventory;
   private final World worldPointer;
   private final BlockPos position;
   private final Random rand;
   public int xpSeed;
   public int[] enchantLevels;
   public int[] enchantClue;
   public int[] worldClue;

   @SideOnly(Side.CLIENT)
   public ContainerEnchantment(InventoryPlayer var1, World var2) {
      this(var1, var2, BlockPos.ORIGIN);
   }

   public ContainerEnchantment(InventoryPlayer var1, World var2, BlockPos var3) {
      this.tableInventory = new InventoryBasic("Enchant", true, 2) {
         public int getInventoryStackLimit() {
            return 64;
         }

         public void markDirty() {
            super.markDirty();
            ContainerEnchantment.this.onCraftMatrixChanged(this);
         }
      };
      this.rand = new Random();
      this.enchantLevels = new int[3];
      this.enchantClue = new int[]{-1, -1, -1};
      this.worldClue = new int[]{-1, -1, -1};
      this.worldPointer = var2;
      this.position = var3;
      this.xpSeed = var1.player.getXPSeed();
      this.addSlotToContainer(new Slot(this.tableInventory, 0, 15, 47) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return true;
         }

         public int getSlotStackLimit() {
            return 1;
         }
      });
      this.addSlotToContainer(new Slot(this.tableInventory, 1, 35, 47) {
         List ores = OreDictionary.getOres("gemLapis");

         public boolean isItemValid(@Nullable ItemStack var1) {
            for(ItemStack var3 : this.ores) {
               if (OreDictionary.itemMatches(var3, var1, false)) {
                  return true;
               }
            }

            return false;
         }
      });

      for(int var4 = 0; var4 < 3; ++var4) {
         for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlotToContainer(new Slot(var1, var5 + var4 * 9 + 9, 8 + var5 * 18, 84 + var4 * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(var1, var6, 8 + var6 * 18, 142));
      }

   }

   protected void broadcastData(IContainerListener var1) {
      var1.sendProgressBarUpdate(this, 0, this.enchantLevels[0]);
      var1.sendProgressBarUpdate(this, 1, this.enchantLevels[1]);
      var1.sendProgressBarUpdate(this, 2, this.enchantLevels[2]);
      var1.sendProgressBarUpdate(this, 3, this.xpSeed & -16);
      var1.sendProgressBarUpdate(this, 4, this.enchantClue[0]);
      var1.sendProgressBarUpdate(this, 5, this.enchantClue[1]);
      var1.sendProgressBarUpdate(this, 6, this.enchantClue[2]);
      var1.sendProgressBarUpdate(this, 7, this.worldClue[0]);
      var1.sendProgressBarUpdate(this, 8, this.worldClue[1]);
      var1.sendProgressBarUpdate(this, 9, this.worldClue[2]);
   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      this.broadcastData(var1);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int var1 = 0; var1 < this.listeners.size(); ++var1) {
         IContainerListener var2 = (IContainerListener)this.listeners.get(var1);
         this.broadcastData(var2);
      }

   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
      if (var1 >= 0 && var1 <= 2) {
         this.enchantLevels[var1] = var2;
      } else if (var1 == 3) {
         this.xpSeed = var2;
      } else if (var1 >= 4 && var1 <= 6) {
         this.enchantClue[var1 - 4] = var2;
      } else if (var1 >= 7 && var1 <= 9) {
         this.worldClue[var1 - 7] = var2;
      } else {
         super.updateProgressBar(var1, var2);
      }

   }

   public void onCraftMatrixChanged(IInventory var1) {
      if (var1 == this.tableInventory) {
         ItemStack var2 = var1.getStackInSlot(0);
         if (var2 != null && var2.isItemEnchantable()) {
            if (!this.worldPointer.isRemote) {
               boolean var8 = false;
               float var4 = 0.0F;

               for(int var5 = -1; var5 <= 1; ++var5) {
                  for(int var6 = -1; var6 <= 1; ++var6) {
                     if ((var5 != 0 || var6 != 0) && this.worldPointer.isAirBlock(this.position.add(var6, 0, var5)) && this.worldPointer.isAirBlock(this.position.add(var6, 1, var5))) {
                        var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6 * 2, 0, var5 * 2));
                        var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6 * 2, 1, var5 * 2));
                        if (var6 != 0 && var5 != 0) {
                           var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6 * 2, 0, var5));
                           var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6 * 2, 1, var5));
                           var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6, 0, var5 * 2));
                           var4 = var4 + ForgeHooks.getEnchantPower(this.worldPointer, this.position.add(var6, 1, var5 * 2));
                        }
                     }
                  }
               }

               this.rand.setSeed((long)this.xpSeed);

               for(int var13 = 0; var13 < 3; ++var13) {
                  this.enchantLevels[var13] = EnchantmentHelper.calcItemStackEnchantability(this.rand, var13, (int)var4, var2);
                  this.enchantClue[var13] = -1;
                  this.worldClue[var13] = -1;
                  if (this.enchantLevels[var13] < var13 + 1) {
                     this.enchantLevels[var13] = 0;
                  }
               }

               for(int var14 = 0; var14 < 3; ++var14) {
                  if (this.enchantLevels[var14] > 0) {
                     List var15 = this.getEnchantmentList(var2, var14, this.enchantLevels[var14]);
                     if (var15 != null && !var15.isEmpty()) {
                        EnchantmentData var7 = (EnchantmentData)var15.get(this.rand.nextInt(var15.size()));
                        this.enchantClue[var14] = Enchantment.getEnchantmentID(var7.enchantmentobj);
                        this.worldClue[var14] = var7.enchantmentLevel;
                     }
                  }
               }

               this.detectAndSendChanges();
            }
         } else {
            for(int var3 = 0; var3 < 3; ++var3) {
               this.enchantLevels[var3] = 0;
               this.enchantClue[var3] = -1;
               this.worldClue[var3] = -1;
            }
         }
      }

   }

   public boolean enchantItem(EntityPlayer var1, int var2) {
      ItemStack var3 = this.tableInventory.getStackInSlot(0);
      ItemStack var4 = this.tableInventory.getStackInSlot(1);
      int var5 = var2 + 1;
      if ((var4 == null || var4.stackSize < var5) && !var1.capabilities.isCreativeMode) {
         return false;
      } else if (this.enchantLevels[var2] > 0 && var3 != null && (var1.experienceLevel >= var5 && var1.experienceLevel >= this.enchantLevels[var2] || var1.capabilities.isCreativeMode)) {
         if (!this.worldPointer.isRemote) {
            List var6 = this.getEnchantmentList(var3, var2, this.enchantLevels[var2]);
            boolean var7 = var3.getItem() == Items.BOOK;
            if (var6 != null) {
               var1.removeExperienceLevel(var5);
               if (var7) {
                  var3.setItem(Items.ENCHANTED_BOOK);
               }

               for(int var8 = 0; var8 < var6.size(); ++var8) {
                  EnchantmentData var9 = (EnchantmentData)var6.get(var8);
                  if (var7) {
                     Items.ENCHANTED_BOOK.addEnchantment(var3, var9);
                  } else {
                     var3.addEnchantment(var9.enchantmentobj, var9.enchantmentLevel);
                  }
               }

               if (!var1.capabilities.isCreativeMode) {
                  var4.stackSize -= var5;
                  if (var4.stackSize <= 0) {
                     this.tableInventory.setInventorySlotContents(1, (ItemStack)null);
                  }
               }

               var1.addStat(StatList.ITEM_ENCHANTED);
               this.tableInventory.markDirty();
               this.xpSeed = var1.getXPSeed();
               this.onCraftMatrixChanged(this.tableInventory);
               this.worldPointer.playSound((EntityPlayer)null, this.position, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, this.worldPointer.rand.nextFloat() * 0.1F + 0.9F);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private List getEnchantmentList(ItemStack var1, int var2, int var3) {
      this.rand.setSeed((long)(this.xpSeed + var2));
      List var4 = EnchantmentHelper.buildEnchantmentList(this.rand, var1, var3, false);
      if (var1.getItem() == Items.BOOK && var4.size() > 1) {
         var4.remove(this.rand.nextInt(var4.size()));
      }

      return var4;
   }

   @SideOnly(Side.CLIENT)
   public int getLapisAmount() {
      ItemStack var1 = this.tableInventory.getStackInSlot(1);
      return var1 == null ? 0 : var1.stackSize;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      if (!this.worldPointer.isRemote) {
         for(int var2 = 0; var2 < this.tableInventory.getSizeInventory(); ++var2) {
            ItemStack var3 = this.tableInventory.removeStackFromSlot(var2);
            if (var3 != null) {
               var1.dropItem(var3, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.worldPointer.getBlockState(this.position).getBlock() != Blocks.ENCHANTING_TABLE ? false : var1.getDistanceSq((double)this.position.getX() + 0.5D, (double)this.position.getY() + 0.5D, (double)this.position.getZ() + 0.5D) <= 64.0D;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 == 0) {
            if (!this.mergeItemStack(var5, 2, 38, true)) {
               return null;
            }
         } else if (var2 == 1) {
            if (!this.mergeItemStack(var5, 2, 38, true)) {
               return null;
            }
         } else if (var5.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(var5.getMetadata()) == EnumDyeColor.BLUE) {
            if (!this.mergeItemStack(var5, 1, 2, true)) {
               return null;
            }
         } else {
            if (((Slot)this.inventorySlots.get(0)).getHasStack() || !((Slot)this.inventorySlots.get(0)).isItemValid(var5)) {
               return null;
            }

            if (var5.hasTagCompound() && var5.stackSize == 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(var5.copy());
               var5.stackSize = 0;
            } else if (var5.stackSize >= 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(new ItemStack(var5.getItem(), 1, var5.getMetadata()));
               --var5.stackSize;
            }
         }

         if (var5.stackSize == 0) {
            var4.putStack((ItemStack)null);
         } else {
            var4.onSlotChanged();
         }

         if (var5.stackSize == var3.stackSize) {
            return null;
         }

         var4.onPickupFromSlot(var1, var5);
      }

      return var3;
   }
}
