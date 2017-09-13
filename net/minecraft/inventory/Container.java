package net.minecraft.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

   public void transferTo(Container other, CraftHumanEntity player) {
      InventoryView source = this.getBukkitView();
      InventoryView destination = other.getBukkitView();
      ((CraftInventory)source.getTopInventory()).getInventory().onClose(player);
      ((CraftInventory)source.getBottomInventory()).getInventory().onClose(player);
      ((CraftInventory)destination.getTopInventory()).getInventory().onOpen(player);
      ((CraftInventory)destination.getBottomInventory()).getInventory().onOpen(player);
   }

   protected Slot addSlotToContainer(Slot slot) {
      slot.slotNumber = this.inventorySlots.size();
      this.inventorySlots.add(slot);
      this.inventoryItemStacks.add((Object)null);
      return slot;
   }

   public void addListener(IContainerListener icrafting) {
      if (this.listeners.contains(icrafting)) {
         throw new IllegalArgumentException("Listener already listening");
      } else {
         this.listeners.add(icrafting);
         icrafting.updateCraftingInventory(this, this.getInventory());
         this.detectAndSendChanges();
      }
   }

   public List getInventory() {
      ArrayList arraylist = Lists.newArrayList();

      for(int i = 0; i < this.inventorySlots.size(); ++i) {
         arraylist.add(((Slot)this.inventorySlots.get(i)).getStack());
      }

      return arraylist;
   }

   public void detectAndSendChanges() {
      for(int i = 0; i < this.inventorySlots.size(); ++i) {
         ItemStack itemstack = ((Slot)this.inventorySlots.get(i)).getStack();
         ItemStack itemstack1 = (ItemStack)this.inventoryItemStacks.get(i);
         if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
            itemstack1 = itemstack == null ? null : itemstack.copy();
            this.inventoryItemStacks.set(i, itemstack1);

            for(int j = 0; j < this.listeners.size(); ++j) {
               ((IContainerListener)this.listeners.get(j)).sendSlotContents(this, i, itemstack1);
            }
         }
      }

   }

   public boolean enchantItem(EntityPlayer entityhuman, int i) {
      return false;
   }

   @Nullable
   public Slot getSlotFromInventory(IInventory iinventory, int i) {
      for(int j = 0; j < this.inventorySlots.size(); ++j) {
         Slot slot = (Slot)this.inventorySlots.get(j);
         if (slot.isHere(iinventory, i)) {
            return slot;
         }
      }

      return null;
   }

   public Slot getSlot(int i) {
      return (Slot)this.inventorySlots.get(i);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      Slot slot = (Slot)this.inventorySlots.get(i);
      return slot != null ? slot.getStack() : null;
   }

   @Nullable
   public ItemStack slotClick(int i, int j, ClickType inventoryclicktype, EntityPlayer entityhuman) {
      ItemStack itemstack = null;
      InventoryPlayer playerinventory = entityhuman.inventory;
      if (inventoryclicktype == ClickType.QUICK_CRAFT) {
         int l = this.dragEvent;
         this.dragEvent = getDragEvent(j);
         if ((l != 1 || this.dragEvent != 2) && l != this.dragEvent) {
            this.resetDrag();
         } else if (playerinventory.getItemStack() == null) {
            this.resetDrag();
         } else if (this.dragEvent == 0) {
            this.dragMode = extractDragMode(j);
            if (isValidDragMode(this.dragMode, entityhuman)) {
               this.dragEvent = 1;
               this.dragSlots.clear();
            } else {
               this.resetDrag();
            }
         } else if (this.dragEvent == 1) {
            Slot slot = (Slot)this.inventorySlots.get(i);
            if (slot != null && canAddItemToSlot(slot, playerinventory.getItemStack(), true) && slot.isItemValid(playerinventory.getItemStack()) && playerinventory.getItemStack().stackSize > this.dragSlots.size() && this.canDragIntoSlot(slot)) {
               this.dragSlots.add(slot);
            }
         } else if (this.dragEvent == 2) {
            if (!this.dragSlots.isEmpty()) {
               ItemStack itemstack1 = playerinventory.getItemStack().copy();
               int k = playerinventory.getItemStack().stackSize;
               Iterator iterator = this.dragSlots.iterator();
               Map draggedSlots = new HashMap();

               while(iterator.hasNext()) {
                  Slot slot1 = (Slot)iterator.next();
                  if (slot1 != null && canAddItemToSlot(slot1, playerinventory.getItemStack(), true) && slot1.isItemValid(playerinventory.getItemStack()) && playerinventory.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(slot1)) {
                     ItemStack itemstack2 = itemstack1.copy();
                     int i1 = slot1.getHasStack() ? slot1.getStack().stackSize : 0;
                     computeStackSize(this.dragSlots, this.dragMode, itemstack2, i1);
                     if (itemstack2.stackSize > itemstack2.getMaxStackSize()) {
                        itemstack2.stackSize = itemstack2.getMaxStackSize();
                     }

                     if (itemstack2.stackSize > slot1.getItemStackLimit(itemstack2)) {
                        itemstack2.stackSize = slot1.getItemStackLimit(itemstack2);
                     }

                     k -= itemstack2.stackSize - i1;
                     draggedSlots.put(Integer.valueOf(slot1.slotNumber), itemstack2);
                  }
               }

               InventoryView view = this.getBukkitView();
               org.bukkit.inventory.ItemStack newcursor = CraftItemStack.asCraftMirror(itemstack1);
               newcursor.setAmount(k);
               Map eventmap = new HashMap();

               for(Entry ditem : draggedSlots.entrySet()) {
                  eventmap.put((Integer)ditem.getKey(), CraftItemStack.asBukkitCopy((ItemStack)ditem.getValue()));
               }

               ItemStack oldCursor = playerinventory.getItemStack();
               playerinventory.setItemStack(CraftItemStack.asNMSCopy(newcursor));
               InventoryDragEvent event = new InventoryDragEvent(view, newcursor.getType() != Material.AIR ? newcursor : null, CraftItemStack.asBukkitCopy(oldCursor), this.dragMode == 1, eventmap);
               entityhuman.world.getServer().getPluginManager().callEvent(event);
               boolean needsUpdate = event.getResult() != Result.DEFAULT;
               if (event.getResult() != Result.DENY) {
                  for(Entry dslot : draggedSlots.entrySet()) {
                     view.setItem(((Integer)dslot.getKey()).intValue(), CraftItemStack.asBukkitCopy((ItemStack)dslot.getValue()));
                  }

                  if (playerinventory.getItemStack() != null) {
                     playerinventory.setItemStack(CraftItemStack.asNMSCopy(event.getCursor()));
                     needsUpdate = true;
                  }
               } else {
                  playerinventory.setItemStack(oldCursor);
               }

               if (needsUpdate && entityhuman instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)entityhuman).sendContainerToPlayer(this);
               }
            }

            this.resetDrag();
         } else {
            this.resetDrag();
         }
      } else if (this.dragEvent != 0) {
         this.resetDrag();
      } else if ((inventoryclicktype == ClickType.PICKUP || inventoryclicktype == ClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
         if (i == -999) {
            if (playerinventory.getItemStack() != null) {
               if (j == 0) {
                  entityhuman.dropItem(playerinventory.getItemStack(), true);
                  playerinventory.setItemStack((ItemStack)null);
               }

               if (j == 1) {
                  ItemStack carried = playerinventory.getItemStack();
                  if (carried.stackSize > 0) {
                     entityhuman.dropItem(carried.splitStack(1), true);
                  }

                  if (carried.stackSize == 0) {
                     playerinventory.setItemStack((ItemStack)null);
                  }
               }
            }
         } else if (inventoryclicktype == ClickType.QUICK_MOVE) {
            if (i < 0) {
               return null;
            }

            Slot slot2 = (Slot)this.inventorySlots.get(i);
            if (slot2 != null && slot2.canTakeStack(entityhuman)) {
               ItemStack itemstack1 = slot2.getStack();
               if (itemstack1 != null && itemstack1.stackSize <= 0) {
                  itemstack = itemstack1.copy();
                  slot2.putStack((ItemStack)null);
               }

               ItemStack itemstack3 = this.transferStackInSlot(entityhuman, i);
               if (itemstack3 != null) {
                  Item item = itemstack3.getItem();
                  itemstack = itemstack3.copy();
                  if (slot2.getStack() != null && slot2.getStack().getItem() == item) {
                     this.retrySlotClick(i, j, true, entityhuman);
                  }
               }
            }
         } else {
            if (i < 0) {
               return null;
            }

            Slot slot2 = (Slot)this.inventorySlots.get(i);
            if (slot2 != null) {
               ItemStack itemstack1 = slot2.getStack();
               ItemStack itemstack3 = playerinventory.getItemStack();
               if (itemstack1 != null) {
                  itemstack = itemstack1.copy();
               }

               if (itemstack1 == null) {
                  if (itemstack3 != null && slot2.isItemValid(itemstack3)) {
                     int j1 = j == 0 ? itemstack3.stackSize : 1;
                     if (j1 > slot2.getItemStackLimit(itemstack3)) {
                        j1 = slot2.getItemStackLimit(itemstack3);
                     }

                     slot2.putStack(itemstack3.splitStack(j1));
                     if (itemstack3.stackSize == 0) {
                        playerinventory.setItemStack((ItemStack)null);
                     } else if (entityhuman instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketSetSlot(-1, -1, entityhuman.inventory.getItemStack()));
                     }
                  }
               } else if (slot2.canTakeStack(entityhuman)) {
                  if (itemstack3 == null) {
                     if (itemstack1.stackSize > 0) {
                        int j1 = j == 0 ? itemstack1.stackSize : (itemstack1.stackSize + 1) / 2;
                        playerinventory.setItemStack(slot2.decrStackSize(j1));
                        if (itemstack1.stackSize <= 0) {
                           slot2.putStack((ItemStack)null);
                        }

                        slot2.onPickupFromSlot(entityhuman, playerinventory.getItemStack());
                     } else {
                        slot2.putStack((ItemStack)null);
                        playerinventory.setItemStack((ItemStack)null);
                     }
                  } else if (slot2.isItemValid(itemstack3)) {
                     if (itemstack1.getItem() == itemstack3.getItem() && itemstack1.getMetadata() == itemstack3.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack1, itemstack3)) {
                        int j1 = j == 0 ? itemstack3.stackSize : 1;
                        if (j1 > slot2.getItemStackLimit(itemstack3) - itemstack1.stackSize) {
                           j1 = slot2.getItemStackLimit(itemstack3) - itemstack1.stackSize;
                        }

                        if (j1 > itemstack3.getMaxStackSize() - itemstack1.stackSize) {
                           j1 = itemstack3.getMaxStackSize() - itemstack1.stackSize;
                        }

                        itemstack3.splitStack(j1);
                        if (itemstack3.stackSize == 0) {
                           playerinventory.setItemStack((ItemStack)null);
                        } else if (entityhuman instanceof EntityPlayerMP) {
                           ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketSetSlot(-1, -1, entityhuman.inventory.getItemStack()));
                        }

                        itemstack1.stackSize += j1;
                     } else if (itemstack3.stackSize <= slot2.getItemStackLimit(itemstack3)) {
                        slot2.putStack(itemstack3);
                        playerinventory.setItemStack(itemstack1);
                     }
                  } else if (itemstack1.getItem() == itemstack3.getItem() && itemstack3.getMaxStackSize() > 1 && (!itemstack1.getHasSubtypes() || itemstack1.getMetadata() == itemstack3.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack1, itemstack3)) {
                     int j1 = itemstack1.stackSize;
                     int maxStack = Math.min(itemstack3.getMaxStackSize(), slot2.getSlotStackLimit());
                     if (j1 > 0 && j1 + itemstack3.stackSize <= maxStack) {
                        itemstack3.stackSize += j1;
                        itemstack1 = slot2.decrStackSize(j1);
                        if (itemstack1.stackSize == 0) {
                           slot2.putStack((ItemStack)null);
                        }

                        slot2.onPickupFromSlot(entityhuman, playerinventory.getItemStack());
                     } else if (entityhuman instanceof EntityPlayerMP) {
                        ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketSetSlot(-1, -1, entityhuman.inventory.getItemStack()));
                     }
                  }
               }

               slot2.onSlotChanged();
               if (entityhuman instanceof EntityPlayerMP && slot2.getSlotStackLimit() != 64) {
                  ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketSetSlot(this.windowId, slot2.slotNumber, slot2.getStack()));
                  if (this.getBukkitView().getType() == InventoryType.WORKBENCH || this.getBukkitView().getType() == InventoryType.CRAFTING) {
                     ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketSetSlot(this.windowId, 0, this.getSlot(0).getStack()));
                  }
               }
            }
         }
      } else if (inventoryclicktype == ClickType.SWAP && j >= 0 && j < 9) {
         Slot slot2 = (Slot)this.inventorySlots.get(i);
         ItemStack itemstack1 = playerinventory.getStackInSlot(j);
         if (itemstack1 != null && itemstack1.stackSize <= 0) {
            itemstack1 = null;
            playerinventory.setInventorySlotContents(j, (ItemStack)null);
         }

         ItemStack itemstack3 = slot2.getStack();
         if (itemstack1 != null || itemstack3 != null) {
            if (itemstack1 == null) {
               if (slot2.canTakeStack(entityhuman)) {
                  playerinventory.setInventorySlotContents(j, itemstack3);
                  slot2.putStack((ItemStack)null);
                  slot2.onPickupFromSlot(entityhuman, itemstack3);
               }
            } else if (itemstack3 == null) {
               if (slot2.isItemValid(itemstack1)) {
                  int j1 = slot2.getItemStackLimit(itemstack1);
                  if (itemstack1.stackSize > j1) {
                     slot2.putStack(itemstack1.splitStack(j1));
                  } else {
                     slot2.putStack(itemstack1);
                     playerinventory.setInventorySlotContents(j, (ItemStack)null);
                  }
               }
            } else if (slot2.canTakeStack(entityhuman) && slot2.isItemValid(itemstack1)) {
               int j1 = slot2.getItemStackLimit(itemstack1);
               if (itemstack1.stackSize > j1) {
                  slot2.putStack(itemstack1.splitStack(j1));
                  slot2.onPickupFromSlot(entityhuman, itemstack3);
                  if (!playerinventory.addItemStackToInventory(itemstack3)) {
                     entityhuman.dropItem(itemstack3, true);
                  }
               } else {
                  slot2.putStack(itemstack1);
                  playerinventory.setInventorySlotContents(j, itemstack3);
                  slot2.onPickupFromSlot(entityhuman, itemstack3);
               }
            }
         }
      } else if (inventoryclicktype == ClickType.CLONE && entityhuman.capabilities.isCreativeMode && playerinventory.getItemStack() == null && i >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(i);
         if (slot2 != null && slot2.getHasStack()) {
            if (slot2.getStack().stackSize > 0) {
               ItemStack itemstack1 = slot2.getStack().copy();
               itemstack1.stackSize = itemstack1.getMaxStackSize();
               playerinventory.setItemStack(itemstack1);
            } else {
               slot2.putStack((ItemStack)null);
            }
         }
      } else if (inventoryclicktype == ClickType.THROW && playerinventory.getItemStack() == null && i >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(i);
         if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(entityhuman)) {
            ItemStack itemstack1 = slot2.decrStackSize(j == 0 ? 1 : slot2.getStack().stackSize);
            slot2.onPickupFromSlot(entityhuman, itemstack1);
            entityhuman.dropItem(itemstack1, true);
         }
      } else if (inventoryclicktype == ClickType.PICKUP_ALL && i >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(i);
         ItemStack itemstack1 = playerinventory.getItemStack();
         if (itemstack1 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(entityhuman))) {
            int k = j == 0 ? 0 : this.inventorySlots.size() - 1;
            int j1 = j == 0 ? 1 : -1;

            for(int k1 = 0; k1 < 2; ++k1) {
               for(int l1 = k; l1 >= 0 && l1 < this.inventorySlots.size() && itemstack1.stackSize < itemstack1.getMaxStackSize(); l1 += j1) {
                  Slot slot3 = (Slot)this.inventorySlots.get(l1);
                  if (slot3.getHasStack() && canAddItemToSlot(slot3, itemstack1, true) && slot3.canTakeStack(entityhuman) && this.canMergeSlot(itemstack1, slot3) && (k1 != 0 || slot3.getStack().stackSize != slot3.getStack().getMaxStackSize())) {
                     int i2 = Math.min(itemstack1.getMaxStackSize() - itemstack1.stackSize, slot3.getStack().stackSize);
                     ItemStack itemstack4 = slot3.decrStackSize(i2);
                     itemstack1.stackSize += i2;
                     if (itemstack4.stackSize <= 0) {
                        slot3.putStack((ItemStack)null);
                     }

                     slot3.onPickupFromSlot(entityhuman, itemstack4);
                  }
               }
            }
         }

         this.detectAndSendChanges();
      }

      return itemstack;
   }

   public boolean canMergeSlot(ItemStack itemstack, Slot slot) {
      return true;
   }

   protected void retrySlotClick(int i, int j, boolean flag, EntityPlayer entityhuman) {
      this.slotClick(i, j, ClickType.QUICK_MOVE, entityhuman);
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      InventoryPlayer playerinventory = entityhuman.inventory;
      if (playerinventory.getItemStack() != null) {
         entityhuman.dropItem(playerinventory.getItemStack(), false);
         playerinventory.setItemStack((ItemStack)null);
      }

   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      this.detectAndSendChanges();
   }

   public void putStackInSlot(int i, ItemStack itemstack) {
      this.getSlot(i).putStack(itemstack);
   }

   public boolean getCanCraft(EntityPlayer entityhuman) {
      return !this.playerList.contains(entityhuman);
   }

   public void setCanCraft(EntityPlayer entityhuman, boolean flag) {
      if (flag) {
         this.playerList.remove(entityhuman);
      } else {
         this.playerList.add(entityhuman);
      }

   }

   public abstract boolean canInteractWith(EntityPlayer var1);

   protected boolean mergeItemStack(ItemStack itemstack, int i, int j, boolean flag) {
      boolean flag1 = false;
      int k = i;
      if (flag) {
         k = j - 1;
      }

      if (itemstack.isStackable()) {
         while(itemstack.stackSize > 0 && (!flag && k < j || flag && k >= i)) {
            Slot slot = (Slot)this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();
            if (itemstack1 != null && areItemStacksEqual(itemstack, itemstack1)) {
               int l = itemstack1.stackSize + itemstack.stackSize;
               int maxStack = Math.min(itemstack.getMaxStackSize(), slot.getSlotStackLimit());
               if (l <= maxStack) {
                  itemstack.stackSize = 0;
                  itemstack1.stackSize = l;
                  slot.onSlotChanged();
                  flag1 = true;
               } else if (itemstack1.stackSize < maxStack) {
                  itemstack.stackSize -= maxStack - itemstack1.stackSize;
                  itemstack1.stackSize = maxStack;
                  slot.onSlotChanged();
                  flag1 = true;
               }
            }

            if (flag) {
               --k;
            } else {
               ++k;
            }
         }
      }

      if (itemstack.stackSize > 0) {
         if (flag) {
            k = j - 1;
         } else {
            k = i;
         }

         while(!flag && k < j || flag && k >= i) {
            Slot slot = (Slot)this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();
            if (itemstack1 == null) {
               slot.putStack(itemstack.copy());
               slot.onSlotChanged();
               itemstack.stackSize = 0;
               flag1 = true;
               break;
            }

            if (flag) {
               --k;
            } else {
               ++k;
            }
         }
      }

      return flag1;
   }

   private static boolean areItemStacksEqual(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack1.getItem() == itemstack.getItem() && (!itemstack.getHasSubtypes() || itemstack.getMetadata() == itemstack1.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1);
   }

   public static int extractDragMode(int i) {
      return i >> 2 & 3;
   }

   public static int getDragEvent(int i) {
      return i & 3;
   }

   public static boolean isValidDragMode(int i, EntityPlayer entityhuman) {
      return i == 0 ? true : (i == 1 ? true : i == 2 && entityhuman.capabilities.isCreativeMode);
   }

   protected void resetDrag() {
      this.dragEvent = 0;
      this.dragSlots.clear();
   }

   public static boolean canAddItemToSlot(Slot slot, ItemStack itemstack, boolean flag) {
      boolean flag1 = slot == null || !slot.getHasStack();
      if (slot != null && slot.getHasStack() && itemstack != null && itemstack.isItemEqual(slot.getStack()) && ItemStack.areItemStackTagsEqual(slot.getStack(), itemstack)) {
         flag1 |= slot.getStack().stackSize + (flag ? 0 : itemstack.stackSize) <= itemstack.getMaxStackSize();
      }

      return flag1;
   }

   public static void computeStackSize(Set set, int i, ItemStack itemstack, int j) {
      switch(i) {
      case 0:
         itemstack.stackSize = MathHelper.floor((float)itemstack.stackSize / (float)set.size());
         break;
      case 1:
         itemstack.stackSize = 1;
         break;
      case 2:
         itemstack.stackSize = itemstack.getItem().getItemStackLimit();
      }

      itemstack.stackSize += j;
   }

   public boolean canDragIntoSlot(Slot slot) {
      return true;
   }

   public static int calcRedstone(@Nullable TileEntity tileentity) {
      return tileentity instanceof IInventory ? calcRedstoneFromInventory((IInventory)tileentity) : 0;
   }

   public static int calcRedstoneFromInventory(@Nullable IInventory iinventory) {
      if (iinventory == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < iinventory.getSizeInventory(); ++j) {
            ItemStack itemstack = iinventory.getStackInSlot(j);
            if (itemstack != null) {
               f += (float)itemstack.stackSize / (float)Math.min(iinventory.getInventoryStackLimit(), itemstack.getMaxStackSize());
               ++i;
            }
         }

         f = f / (float)iinventory.getSizeInventory();
         return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }
}
