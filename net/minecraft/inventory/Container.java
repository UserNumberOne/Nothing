package net.minecraft.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;

public abstract class Container {
   public List inventoryItemStacks = Lists.newArrayList();
   public List inventorySlots = Lists.newArrayList();
   public int windowId;
   private int dragMode = -1;
   private int dragEvent;
   private final Set dragSlots = Sets.newHashSet();
   protected List listeners = Lists.newArrayList();
   private final Set playerList = Sets.newHashSet();
   public boolean checkReachable = true;

   public abstract InventoryView getBukkitView();

   public void transferTo(Container var1, CraftHumanEntity var2) {
      InventoryView var3 = this.getBukkitView();
      InventoryView var4 = var1.getBukkitView();
      ((CraftInventory)var3.getTopInventory()).getInventory().onClose(var2);
      ((CraftInventory)var3.getBottomInventory()).getInventory().onClose(var2);
      ((CraftInventory)var4.getTopInventory()).getInventory().onOpen(var2);
      ((CraftInventory)var4.getBottomInventory()).getInventory().onOpen(var2);
   }

   protected Slot addSlotToContainer(Slot var1) {
      var1.slotNumber = this.inventorySlots.size();
      this.inventorySlots.add(var1);
      this.inventoryItemStacks.add((Object)null);
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
               ItemStack var9 = var6.getItemStack().copy();
               int var10 = var6.getItemStack().stackSize;
               Iterator var26 = this.dragSlots.iterator();
               HashMap var11 = new HashMap();

               while(var26.hasNext()) {
                  Slot var12 = (Slot)var26.next();
                  if (var12 != null && canAddItemToSlot(var12, var6.getItemStack(), true) && var12.isItemValid(var6.getItemStack()) && var6.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(var12)) {
                     ItemStack var13 = var9.copy();
                     int var14 = var12.getHasStack() ? var12.getStack().stackSize : 0;
                     computeStackSize(this.dragSlots, this.dragMode, var13, var14);
                     if (var13.stackSize > var13.getMaxStackSize()) {
                        var13.stackSize = var13.getMaxStackSize();
                     }

                     if (var13.stackSize > var12.getItemStackLimit(var13)) {
                        var13.stackSize = var12.getItemStackLimit(var13);
                     }

                     var10 -= var13.stackSize - var14;
                     var11.put(Integer.valueOf(var12.slotNumber), var13);
                  }
               }

               InventoryView var45 = this.getBukkitView();
               CraftItemStack var50 = CraftItemStack.asCraftMirror(var9);
               var50.setAmount(var10);
               HashMap var52 = new HashMap();

               for(Entry var16 : var11.entrySet()) {
                  var52.put((Integer)var16.getKey(), CraftItemStack.asBukkitCopy((ItemStack)var16.getValue()));
               }

               ItemStack var56 = var6.getItemStack();
               var6.setItemStack(CraftItemStack.asNMSCopy(var50));
               InventoryDragEvent var54 = new InventoryDragEvent(var45, var50.getType() != Material.AIR ? var50 : null, CraftItemStack.asBukkitCopy(var56), this.dragMode == 1, var52);
               var4.world.getServer().getPluginManager().callEvent(var54);
               boolean var17 = var54.getResult() != Result.DEFAULT;
               if (var54.getResult() != Result.DENY) {
                  for(Entry var19 : var11.entrySet()) {
                     var45.setItem(((Integer)var19.getKey()).intValue(), CraftItemStack.asBukkitCopy((ItemStack)var19.getValue()));
                  }

                  if (var6.getItemStack() != null) {
                     var6.setItemStack(CraftItemStack.asNMSCopy(var54.getCursor()));
                     var17 = true;
                  }
               } else {
                  var6.setItemStack(var56);
               }

               if (var17 && var4 instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)var4).sendContainerToPlayer(this);
               }
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
                  ItemStack var47 = var6.getItemStack();
                  if (var47.stackSize > 0) {
                     var4.dropItem(var47.splitStack(1), true);
                  }

                  if (var47.stackSize == 0) {
                     var6.setItemStack((ItemStack)null);
                  }
               }
            }
         } else if (var3 == ClickType.QUICK_MOVE) {
            if (var1 < 0) {
               return null;
            }

            Slot var24 = (Slot)this.inventorySlots.get(var1);
            if (var24 != null && var24.canTakeStack(var4)) {
               ItemStack var34 = var24.getStack();
               if (var34 != null && var34.stackSize <= 0) {
                  var5 = var34.copy();
                  var24.putStack((ItemStack)null);
               }

               ItemStack var28 = this.transferStackInSlot(var4, var1);
               if (var28 != null) {
                  Item var48 = var28.getItem();
                  var5 = var28.copy();
                  if (var24.getStack() != null && var24.getStack().getItem() == var48) {
                     this.retrySlotClick(var1, var2, true, var4);
                  }
               }
            }
         } else {
            if (var1 < 0) {
               return null;
            }

            Slot var25 = (Slot)this.inventorySlots.get(var1);
            if (var25 != null) {
               ItemStack var35 = var25.getStack();
               ItemStack var29 = var6.getItemStack();
               if (var35 != null) {
                  var5 = var35.copy();
               }

               if (var35 == null) {
                  if (var29 != null && var25.isItemValid(var29)) {
                     int var44 = var2 == 0 ? var29.stackSize : 1;
                     if (var44 > var25.getItemStackLimit(var29)) {
                        var44 = var25.getItemStackLimit(var29);
                     }

                     var25.putStack(var29.splitStack(var44));
                     if (var29.stackSize == 0) {
                        var6.setItemStack((ItemStack)null);
                     } else if (var4 instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)var4).connection.sendPacket(new SPacketSetSlot(-1, -1, var4.inventory.getItemStack()));
                     }
                  }
               } else if (var25.canTakeStack(var4)) {
                  if (var29 == null) {
                     if (var35.stackSize > 0) {
                        int var43 = var2 == 0 ? var35.stackSize : (var35.stackSize + 1) / 2;
                        var6.setItemStack(var25.decrStackSize(var43));
                        if (var35.stackSize <= 0) {
                           var25.putStack((ItemStack)null);
                        }

                        var25.onPickupFromSlot(var4, var6.getItemStack());
                     } else {
                        var25.putStack((ItemStack)null);
                        var6.setItemStack((ItemStack)null);
                     }
                  } else if (var25.isItemValid(var29)) {
                     if (var35.getItem() == var29.getItem() && var35.getMetadata() == var29.getMetadata() && ItemStack.areItemStackTagsEqual(var35, var29)) {
                        int var42 = var2 == 0 ? var29.stackSize : 1;
                        if (var42 > var25.getItemStackLimit(var29) - var35.stackSize) {
                           var42 = var25.getItemStackLimit(var29) - var35.stackSize;
                        }

                        if (var42 > var29.getMaxStackSize() - var35.stackSize) {
                           var42 = var29.getMaxStackSize() - var35.stackSize;
                        }

                        var29.splitStack(var42);
                        if (var29.stackSize == 0) {
                           var6.setItemStack((ItemStack)null);
                        } else if (var4 instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)var4).connection.sendPacket(new SPacketSetSlot(-1, -1, var4.inventory.getItemStack()));
                        }

                        var35.stackSize += var42;
                     } else if (var29.stackSize <= var25.getItemStackLimit(var29)) {
                        var25.putStack(var29);
                        var6.setItemStack(var35);
                     }
                  } else if (var35.getItem() == var29.getItem() && var29.getMaxStackSize() > 1 && (!var35.getHasSubtypes() || var35.getMetadata() == var29.getMetadata()) && ItemStack.areItemStackTagsEqual(var35, var29)) {
                     int var41 = var35.stackSize;
                     int var49 = Math.min(var29.getMaxStackSize(), var25.getSlotStackLimit());
                     if (var41 > 0 && var41 + var29.stackSize <= var49) {
                        var29.stackSize += var41;
                        var35 = var25.decrStackSize(var41);
                        if (var35.stackSize == 0) {
                           var25.putStack((ItemStack)null);
                        }

                        var25.onPickupFromSlot(var4, var6.getItemStack());
                     } else if (var4 instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)var4).connection.sendPacket(new SPacketSetSlot(-1, -1, var4.inventory.getItemStack()));
                     }
                  }
               }

               var25.onSlotChanged();
               if (var4 instanceof EntityPlayerMP && var25.getSlotStackLimit() != 64) {
                  ((EntityPlayerMP)var4).connection.sendPacket(new SPacketSetSlot(this.windowId, var25.slotNumber, var25.getStack()));
                  if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                     ((EntityPlayerMP)var4).connection.sendPacket(new SPacketSetSlot(this.windowId, 0, this.getSlot(0).getStack()));
                  }
               }
            }
         }
      } else if (var3 == ClickType.SWAP && var2 >= 0 && var2 < 9) {
         Slot var23 = (Slot)this.inventorySlots.get(var1);
         ItemStack var33 = var6.getStackInSlot(var2);
         if (var33 != null && var33.stackSize <= 0) {
            var33 = null;
            var6.setInventorySlotContents(var2, (ItemStack)null);
         }

         ItemStack var27 = var23.getStack();
         if (var33 != null || var27 != null) {
            if (var33 == null) {
               if (var23.canTakeStack(var4)) {
                  var6.setInventorySlotContents(var2, var27);
                  var23.putStack((ItemStack)null);
                  var23.onPickupFromSlot(var4, var27);
               }
            } else if (var27 == null) {
               if (var23.isItemValid(var33)) {
                  int var39 = var23.getItemStackLimit(var33);
                  if (var33.stackSize > var39) {
                     var23.putStack(var33.splitStack(var39));
                  } else {
                     var23.putStack(var33);
                     var6.setInventorySlotContents(var2, (ItemStack)null);
                  }
               }
            } else if (var23.canTakeStack(var4) && var23.isItemValid(var33)) {
               int var40 = var23.getItemStackLimit(var33);
               if (var33.stackSize > var40) {
                  var23.putStack(var33.splitStack(var40));
                  var23.onPickupFromSlot(var4, var27);
                  if (!var6.addItemStackToInventory(var27)) {
                     var4.dropItem(var27, true);
                  }
               } else {
                  var23.putStack(var33);
                  var6.setInventorySlotContents(var2, var27);
                  var23.onPickupFromSlot(var4, var27);
               }
            }
         }
      } else if (var3 == ClickType.CLONE && var4.capabilities.isCreativeMode && var6.getItemStack() == null && var1 >= 0) {
         Slot var22 = (Slot)this.inventorySlots.get(var1);
         if (var22 != null && var22.getHasStack()) {
            if (var22.getStack().stackSize > 0) {
               ItemStack var32 = var22.getStack().copy();
               var32.stackSize = var32.getMaxStackSize();
               var6.setItemStack(var32);
            } else {
               var22.putStack((ItemStack)null);
            }
         }
      } else if (var3 == ClickType.THROW && var6.getItemStack() == null && var1 >= 0) {
         Slot var21 = (Slot)this.inventorySlots.get(var1);
         if (var21 != null && var21.getHasStack() && var21.canTakeStack(var4)) {
            ItemStack var31 = var21.decrStackSize(var2 == 0 ? 1 : var21.getStack().stackSize);
            var21.onPickupFromSlot(var4, var31);
            var4.dropItem(var31, true);
         }
      } else if (var3 == ClickType.PICKUP_ALL && var1 >= 0) {
         Slot var20 = (Slot)this.inventorySlots.get(var1);
         ItemStack var30 = var6.getItemStack();
         if (var30 != null && (var20 == null || !var20.getHasStack() || !var20.canTakeStack(var4))) {
            int var37 = var2 == 0 ? 0 : this.inventorySlots.size() - 1;
            int var38 = var2 == 0 ? 1 : -1;

            for(int var46 = 0; var46 < 2; ++var46) {
               for(int var51 = var37; var51 >= 0 && var51 < this.inventorySlots.size() && var30.stackSize < var30.getMaxStackSize(); var51 += var38) {
                  Slot var53 = (Slot)this.inventorySlots.get(var51);
                  if (var53.getHasStack() && canAddItemToSlot(var53, var30, true) && var53.canTakeStack(var4) && this.canMergeSlot(var30, var53) && (var46 != 0 || var53.getStack().stackSize != var53.getStack().getMaxStackSize())) {
                     int var57 = Math.min(var30.getMaxStackSize() - var30.stackSize, var53.getStack().stackSize);
                     ItemStack var55 = var53.decrStackSize(var57);
                     var30.stackSize += var57;
                     if (var55.stackSize <= 0) {
                        var53.putStack((ItemStack)null);
                     }

                     var53.onPickupFromSlot(var4, var55);
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
               int var10 = Math.min(var1.getMaxStackSize(), var7.getSlotStackLimit());
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
            if (var13 == null) {
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
         var2.stackSize = var2.getItem().getItemStackLimit();
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
