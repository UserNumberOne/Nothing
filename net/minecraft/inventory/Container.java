package net.minecraft.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class Container {
   public List inventoryItemStacks = Lists.newArrayList();
   public List inventorySlots = Lists.newArrayList();
   public int windowId;
   @SideOnly(Side.CLIENT)
   private short transactionID;
   private int dragMode = -1;
   private int dragEvent;
   private final Set dragSlots = Sets.newHashSet();
   protected List listeners = Lists.newArrayList();
   private final Set playerList = Sets.newHashSet();

   protected Slot addSlotToContainer(Slot var1) {
      var1.slotNumber = this.inventorySlots.size();
      this.inventorySlots.add(var1);
      this.inventoryItemStacks.add((ItemStack)null);
      return var1;
   }

   public void addListener(IContainerListener var1) {
      if (this.listeners.contains(var1)) {
         throw new IllegalArgumentException("Listener already listening");
      } else {
         this.listeners.add(var1);
         var1.updateCraftingInventory(this, this.getInventory());
         this.detectAndSendChanges();
      }
   }

   public List getInventory() {
      ArrayList var1 = Lists.newArrayList();

      for(int var2 = 0; var2 < this.inventorySlots.size(); ++var2) {
         var1.add(((Slot)this.inventorySlots.get(var2)).getStack());
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   public void removeListener(IContainerListener var1) {
      this.listeners.remove(var1);
   }

   public void detectAndSendChanges() {
      for(int var1 = 0; var1 < this.inventorySlots.size(); ++var1) {
         ItemStack var2 = ((Slot)this.inventorySlots.get(var1)).getStack();
         ItemStack var3 = (ItemStack)this.inventoryItemStacks.get(var1);
         if (!ItemStack.areItemStacksEqual(var3, var2)) {
            var3 = var2 == null ? null : var2.copy();
            this.inventoryItemStacks.set(var1, var3);

            for(int var4 = 0; var4 < this.listeners.size(); ++var4) {
               ((IContainerListener)this.listeners.get(var4)).sendSlotContents(this, var1, var3);
            }
         }
      }

   }

   public boolean enchantItem(EntityPlayer var1, int var2) {
      return false;
   }

   @Nullable
   public Slot getSlotFromInventory(IInventory var1, int var2) {
      for(int var3 = 0; var3 < this.inventorySlots.size(); ++var3) {
         Slot var4 = (Slot)this.inventorySlots.get(var3);
         if (var4.isHere(var1, var2)) {
            return var4;
         }
      }

      return null;
   }

   public Slot getSlot(int var1) {
      return (Slot)this.inventorySlots.get(var1);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      Slot var3 = (Slot)this.inventorySlots.get(var2);
      return var3 != null ? var3.getStack() : null;
   }

   @Nullable
   public ItemStack slotClick(int var1, int var2, ClickType var3, EntityPlayer var4) {
      ItemStack var5 = null;
      InventoryPlayer var6 = var4.inventory;
      if (var3 == ClickType.QUICK_CRAFT) {
         int var7 = this.dragEvent;
         this.dragEvent = getDragEvent(var2);
         if ((var7 != 1 || this.dragEvent != 2) && var7 != this.dragEvent) {
            this.resetDrag();
         } else if (var6.getItemStack() == null) {
            this.resetDrag();
         } else if (this.dragEvent == 0) {
            this.dragMode = extractDragMode(var2);
            if (isValidDragMode(this.dragMode, var4)) {
               this.dragEvent = 1;
               this.dragSlots.clear();
            } else {
               this.resetDrag();
            }
         } else if (this.dragEvent == 1) {
            Slot var8 = (Slot)this.inventorySlots.get(var1);
            if (var8 != null && canAddItemToSlot(var8, var6.getItemStack(), true) && var8.isItemValid(var6.getItemStack()) && var6.getItemStack().stackSize > this.dragSlots.size() && this.canDragIntoSlot(var8)) {
               this.dragSlots.add(var8);
            }
         } else if (this.dragEvent == 2) {
            if (!this.dragSlots.isEmpty()) {
               ItemStack var22 = var6.getItemStack().copy();
               int var9 = var6.getItemStack().stackSize;

               for(Slot var11 : this.dragSlots) {
                  if (var11 != null && canAddItemToSlot(var11, var6.getItemStack(), true) && var11.isItemValid(var6.getItemStack()) && var6.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(var11)) {
                     ItemStack var12 = var22.copy();
                     int var13 = var11.getHasStack() ? var11.getStack().stackSize : 0;
                     computeStackSize(this.dragSlots, this.dragMode, var12, var13);
                     if (var12.stackSize > var12.getMaxStackSize()) {
                        var12.stackSize = var12.getMaxStackSize();
                     }

                     if (var12.stackSize > var11.getItemStackLimit(var12)) {
                        var12.stackSize = var11.getItemStackLimit(var12);
                     }

                     var9 -= var12.stackSize - var13;
                     var11.putStack(var12);
                  }
               }

               var22.stackSize = var9;
               if (var22.stackSize <= 0) {
                  var22 = null;
               }

               var6.setItemStack(var22);
            }

            this.resetDrag();
         } else {
            this.resetDrag();
         }
      } else if (this.dragEvent != 0) {
         this.resetDrag();
      } else if ((var3 == ClickType.PICKUP || var3 == ClickType.QUICK_MOVE) && (var2 == 0 || var2 == 1)) {
         if (var1 == -999) {
            if (var6.getItemStack() != null) {
               if (var2 == 0) {
                  var4.dropItem(var6.getItemStack(), true);
                  var6.setItemStack((ItemStack)null);
               }

               if (var2 == 1) {
                  var4.dropItem(var6.getItemStack().splitStack(1), true);
                  if (var6.getItemStack().stackSize == 0) {
                     var6.setItemStack((ItemStack)null);
                  }
               }
            }
         } else if (var3 == ClickType.QUICK_MOVE) {
            if (var1 < 0) {
               return null;
            }

            Slot var20 = (Slot)this.inventorySlots.get(var1);
            if (var20 != null && var20.canTakeStack(var4)) {
               ItemStack var27 = var20.getStack();
               if (var27 != null && var27.stackSize <= 0) {
                  var5 = var27.copy();
                  var20.putStack((ItemStack)null);
               }

               ItemStack var32 = this.transferStackInSlot(var4, var1);
               if (var32 != null) {
                  Item var37 = var32.getItem();
                  var5 = var32.copy();
                  if (var20.getStack() != null && var20.getStack().getItem() == var37) {
                     this.retrySlotClick(var1, var2, true, var4);
                  }
               }
            }
         } else {
            if (var1 < 0) {
               return null;
            }

            Slot var21 = (Slot)this.inventorySlots.get(var1);
            if (var21 != null) {
               ItemStack var28 = var21.getStack();
               ItemStack var33 = var6.getItemStack();
               if (var28 != null) {
                  var5 = var28.copy();
               }

               if (var28 == null) {
                  if (var33 != null && var21.isItemValid(var33)) {
                     int var41 = var2 == 0 ? var33.stackSize : 1;
                     if (var41 > var21.getItemStackLimit(var33)) {
                        var41 = var21.getItemStackLimit(var33);
                     }

                     var21.putStack(var33.splitStack(var41));
                     if (var33.stackSize == 0) {
                        var6.setItemStack((ItemStack)null);
                     }
                  }
               } else if (var21.canTakeStack(var4)) {
                  if (var33 == null) {
                     if (var28.stackSize > 0) {
                        int var40 = var2 == 0 ? var28.stackSize : (var28.stackSize + 1) / 2;
                        var6.setItemStack(var21.decrStackSize(var40));
                        if (var28.stackSize <= 0) {
                           var21.putStack((ItemStack)null);
                        }

                        var21.onPickupFromSlot(var4, var6.getItemStack());
                     } else {
                        var21.putStack((ItemStack)null);
                        var6.setItemStack((ItemStack)null);
                     }
                  } else if (var21.isItemValid(var33)) {
                     if (var28.getItem() == var33.getItem() && var28.getMetadata() == var33.getMetadata() && ItemStack.areItemStackTagsEqual(var28, var33)) {
                        int var39 = var2 == 0 ? var33.stackSize : 1;
                        if (var39 > var21.getItemStackLimit(var33) - var28.stackSize) {
                           var39 = var21.getItemStackLimit(var33) - var28.stackSize;
                        }

                        if (var39 > var33.getMaxStackSize() - var28.stackSize) {
                           var39 = var33.getMaxStackSize() - var28.stackSize;
                        }

                        var33.splitStack(var39);
                        if (var33.stackSize == 0) {
                           var6.setItemStack((ItemStack)null);
                        }

                        var28.stackSize += var39;
                     } else if (var33.stackSize <= var21.getItemStackLimit(var33)) {
                        var21.putStack(var33);
                        var6.setItemStack(var28);
                     }
                  } else if (var28.getItem() == var33.getItem() && var33.getMaxStackSize() > 1 && (!var28.getHasSubtypes() || var28.getMetadata() == var33.getMetadata()) && ItemStack.areItemStackTagsEqual(var28, var33)) {
                     int var38 = var28.stackSize;
                     if (var38 > 0 && var38 + var33.stackSize <= var33.getMaxStackSize()) {
                        var33.stackSize += var38;
                        var28 = var21.decrStackSize(var38);
                        if (var28.stackSize == 0) {
                           var21.putStack((ItemStack)null);
                        }

                        var21.onPickupFromSlot(var4, var6.getItemStack());
                     }
                  }
               }

               var21.onSlotChanged();
            }
         }
      } else if (var3 == ClickType.SWAP && var2 >= 0 && var2 < 9) {
         Slot var19 = (Slot)this.inventorySlots.get(var1);
         ItemStack var26 = var6.getStackInSlot(var2);
         if (var26 != null && var26.stackSize <= 0) {
            var26 = null;
            var6.setInventorySlotContents(var2, (ItemStack)null);
         }

         ItemStack var31 = var19.getStack();
         if (var26 != null || var31 != null) {
            if (var26 == null) {
               if (var19.canTakeStack(var4)) {
                  var6.setInventorySlotContents(var2, var31);
                  var19.putStack((ItemStack)null);
                  var19.onPickupFromSlot(var4, var31);
               }
            } else if (var31 == null) {
               if (var19.isItemValid(var26)) {
                  int var35 = var19.getItemStackLimit(var26);
                  if (var26.stackSize > var35) {
                     var19.putStack(var26.splitStack(var35));
                  } else {
                     var19.putStack(var26);
                     var6.setInventorySlotContents(var2, (ItemStack)null);
                  }
               }
            } else if (var19.canTakeStack(var4) && var19.isItemValid(var26)) {
               int var36 = var19.getItemStackLimit(var26);
               if (var26.stackSize > var36) {
                  var19.putStack(var26.splitStack(var36));
                  var19.onPickupFromSlot(var4, var31);
                  if (!var6.addItemStackToInventory(var31)) {
                     var4.dropItem(var31, true);
                  }
               } else {
                  var19.putStack(var26);
                  var6.setInventorySlotContents(var2, var31);
                  var19.onPickupFromSlot(var4, var31);
               }
            }
         }
      } else if (var3 == ClickType.CLONE && var4.capabilities.isCreativeMode && var6.getItemStack() == null && var1 >= 0) {
         Slot var18 = (Slot)this.inventorySlots.get(var1);
         if (var18 != null && var18.getHasStack()) {
            if (var18.getStack().stackSize > 0) {
               ItemStack var25 = var18.getStack().copy();
               var25.stackSize = var25.getMaxStackSize();
               var6.setItemStack(var25);
            } else {
               var18.putStack((ItemStack)null);
            }
         }
      } else if (var3 == ClickType.THROW && var6.getItemStack() == null && var1 >= 0) {
         Slot var17 = (Slot)this.inventorySlots.get(var1);
         if (var17 != null && var17.getHasStack() && var17.canTakeStack(var4)) {
            ItemStack var24 = var17.decrStackSize(var2 == 0 ? 1 : var17.getStack().stackSize);
            var17.onPickupFromSlot(var4, var24);
            var4.dropItem(var24, true);
         }
      } else if (var3 == ClickType.PICKUP_ALL && var1 >= 0) {
         Slot var16 = (Slot)this.inventorySlots.get(var1);
         ItemStack var23 = var6.getItemStack();
         if (var23 != null && (var16 == null || !var16.getHasStack() || !var16.canTakeStack(var4))) {
            int var30 = var2 == 0 ? 0 : this.inventorySlots.size() - 1;
            int var34 = var2 == 0 ? 1 : -1;

            for(int var42 = 0; var42 < 2; ++var42) {
               for(int var43 = var30; var43 >= 0 && var43 < this.inventorySlots.size() && var23.stackSize < var23.getMaxStackSize(); var43 += var34) {
                  Slot var44 = (Slot)this.inventorySlots.get(var43);
                  if (var44.getHasStack() && canAddItemToSlot(var44, var23, true) && var44.canTakeStack(var4) && this.canMergeSlot(var23, var44) && (var42 != 0 || var44.getStack().stackSize != var44.getStack().getMaxStackSize())) {
                     int var14 = Math.min(var23.getMaxStackSize() - var23.stackSize, var44.getStack().stackSize);
                     ItemStack var15 = var44.decrStackSize(var14);
                     var23.stackSize += var14;
                     if (var15.stackSize <= 0) {
                        var44.putStack((ItemStack)null);
                     }

                     var44.onPickupFromSlot(var4, var15);
                  }
               }
            }
         }

         this.detectAndSendChanges();
      }

      return var5;
   }

   public boolean canMergeSlot(ItemStack var1, Slot var2) {
      return true;
   }

   protected void retrySlotClick(int var1, int var2, boolean var3, EntityPlayer var4) {
      this.slotClick(var1, var2, ClickType.QUICK_MOVE, var4);
   }

   public void onContainerClosed(EntityPlayer var1) {
      InventoryPlayer var2 = var1.inventory;
      if (var2.getItemStack() != null) {
         var1.dropItem(var2.getItemStack(), false);
         var2.setItemStack((ItemStack)null);
      }

   }

   public void onCraftMatrixChanged(IInventory var1) {
      this.detectAndSendChanges();
   }

   public void putStackInSlot(int var1, ItemStack var2) {
      this.getSlot(var1).putStack(var2);
   }

   @SideOnly(Side.CLIENT)
   public void putStacksInSlots(ItemStack[] var1) {
      for(int var2 = 0; var2 < var1.length; ++var2) {
         this.getSlot(var2).putStack(var1[var2]);
      }

   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
   }

   @SideOnly(Side.CLIENT)
   public short getNextTransactionID(InventoryPlayer var1) {
      ++this.transactionID;
      return this.transactionID;
   }

   public boolean getCanCraft(EntityPlayer var1) {
      return !this.playerList.contains(var1);
   }

   public void setCanCraft(EntityPlayer var1, boolean var2) {
      if (var2) {
         this.playerList.remove(var1);
      } else {
         this.playerList.add(var1);
      }

   }

   public abstract boolean canInteractWith(EntityPlayer var1);

   protected boolean mergeItemStack(ItemStack var1, int var2, int var3, boolean var4) {
      boolean var5 = false;
      int var6 = var2;
      if (var4) {
         var6 = var3 - 1;
      }

      if (var1.isStackable()) {
         while(var1.stackSize > 0 && (!var4 && var6 < var3 || var4 && var6 >= var2)) {
            Slot var7 = (Slot)this.inventorySlots.get(var6);
            ItemStack var8 = var7.getStack();
            if (var8 != null && areItemStacksEqual(var1, var8)) {
               int var9 = var8.stackSize + var1.stackSize;
               int var10 = Math.min(var7.getSlotStackLimit(), var1.getMaxStackSize());
               if (var9 <= var10) {
                  var1.stackSize = 0;
                  var8.stackSize = var9;
                  var7.onSlotChanged();
                  var5 = true;
               } else if (var8.stackSize < var10) {
                  var1.stackSize -= var10 - var8.stackSize;
                  var8.stackSize = var10;
                  var7.onSlotChanged();
                  var5 = true;
               }
            }

            if (var4) {
               --var6;
            } else {
               ++var6;
            }
         }
      }

      if (var1.stackSize > 0) {
         if (var4) {
            var6 = var3 - 1;
         } else {
            var6 = var2;
         }

         while(!var4 && var6 < var3 || var4 && var6 >= var2) {
            Slot var12 = (Slot)this.inventorySlots.get(var6);
            ItemStack var13 = var12.getStack();
            if (var13 == null && var12.isItemValid(var1)) {
               var12.putStack(var1.copy());
               var12.onSlotChanged();
               var1.stackSize = 0;
               var5 = true;
               break;
            }

            if (var4) {
               --var6;
            } else {
               ++var6;
            }
         }
      }

      return var5;
   }

   private static boolean areItemStacksEqual(ItemStack var0, ItemStack var1) {
      return var1.getItem() == var0.getItem() && (!var0.getHasSubtypes() || var0.getMetadata() == var1.getMetadata()) && ItemStack.areItemStackTagsEqual(var0, var1);
   }

   public static int extractDragMode(int var0) {
      return var0 >> 2 & 3;
   }

   public static int getDragEvent(int var0) {
      return var0 & 3;
   }

   @SideOnly(Side.CLIENT)
   public static int getQuickcraftMask(int var0, int var1) {
      return var0 & 3 | (var1 & 3) << 2;
   }

   public static boolean isValidDragMode(int var0, EntityPlayer var1) {
      return var0 == 0 ? true : (var0 == 1 ? true : var0 == 2 && var1.capabilities.isCreativeMode);
   }

   protected void resetDrag() {
      this.dragEvent = 0;
      this.dragSlots.clear();
   }

   public static boolean canAddItemToSlot(Slot var0, ItemStack var1, boolean var2) {
      boolean var3 = var0 == null || !var0.getHasStack();
      if (var0 != null && var0.getHasStack() && var1 != null && var1.isItemEqual(var0.getStack()) && ItemStack.areItemStackTagsEqual(var0.getStack(), var1)) {
         var3 |= var0.getStack().stackSize + (var2 ? 0 : var1.stackSize) <= var1.getMaxStackSize();
      }

      return var3;
   }

   public static void computeStackSize(Set var0, int var1, ItemStack var2, int var3) {
      switch(var1) {
      case 0:
         var2.stackSize = MathHelper.floor((float)var2.stackSize / (float)var0.size());
         break;
      case 1:
         var2.stackSize = 1;
         break;
      case 2:
         var2.stackSize = var2.getMaxStackSize();
      }

      var2.stackSize += var3;
   }

   public boolean canDragIntoSlot(Slot var1) {
      return true;
   }

   public static int calcRedstone(@Nullable TileEntity var0) {
      return var0 instanceof IInventory ? calcRedstoneFromInventory((IInventory)var0) : 0;
   }

   public static int calcRedstoneFromInventory(@Nullable IInventory var0) {
      if (var0 == null) {
         return 0;
      } else {
         int var1 = 0;
         float var2 = 0.0F;

         for(int var3 = 0; var3 < var0.getSizeInventory(); ++var3) {
            ItemStack var4 = var0.getStackInSlot(var3);
            if (var4 != null) {
               var2 += (float)var4.stackSize / (float)Math.min(var0.getInventoryStackLimit(), var4.getMaxStackSize());
               ++var1;
            }
         }

         var2 = var2 / (float)var0.getSizeInventory();
         return MathHelper.floor(var2 * 14.0F) + (var1 > 0 ? 1 : 0);
      }
   }
}
