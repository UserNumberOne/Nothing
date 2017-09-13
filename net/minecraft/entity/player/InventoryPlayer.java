package net.minecraft.entity.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryPlayer implements IInventory {
   public final ItemStack[] mainInventory = new ItemStack[36];
   public final ItemStack[] armorInventory = new ItemStack[4];
   public final ItemStack[] offHandInventory = new ItemStack[1];
   private final ItemStack[][] allInventories;
   public int currentItem;
   public EntityPlayer player;
   private ItemStack itemStack;
   public boolean inventoryChanged;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public ItemStack[] getContents() {
      ItemStack[] combined = new ItemStack[this.mainInventory.length + this.armorInventory.length + this.offHandInventory.length];
      System.arraycopy(this.mainInventory, 0, combined, 0, this.mainInventory.length);
      System.arraycopy(this.armorInventory, 0, combined, this.mainInventory.length, this.armorInventory.length);
      System.arraycopy(this.offHandInventory, 0, combined, this.mainInventory.length + this.armorInventory.length, this.offHandInventory.length);
      return combined;
   }

   public ItemStack[] getArmorContents() {
      return this.armorInventory;
   }

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return this.player.getBukkitEntity();
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public Location getLocation() {
      return this.player.getBukkitEntity().getLocation();
   }

   public InventoryPlayer(EntityPlayer entityhuman) {
      this.allInventories = new ItemStack[][]{this.mainInventory, this.armorInventory, this.offHandInventory};
      this.player = entityhuman;
   }

   @Nullable
   public ItemStack getCurrentItem() {
      return isHotbar(this.currentItem) ? this.mainInventory[this.currentItem] : null;
   }

   public static int getHotbarSize() {
      return 9;
   }

   private boolean canMergeStacks(@Nullable ItemStack itemstack, ItemStack itemstack1) {
      return itemstack != null && this.stackEqualExact(itemstack, itemstack1) && itemstack.isStackable() && itemstack.stackSize < itemstack.getMaxStackSize() && itemstack.stackSize < this.getInventoryStackLimit();
   }

   private boolean stackEqualExact(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack.getItem() == itemstack1.getItem() && (!itemstack.getHasSubtypes() || itemstack.getMetadata() == itemstack1.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1);
   }

   public int canHold(ItemStack itemstack) {
      int remains = itemstack.stackSize;

      for(int i = 0; i < this.mainInventory.length; ++i) {
         if (this.mainInventory[i] == null) {
            return itemstack.stackSize;
         }

         if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemstack.getItem() && this.mainInventory[i].isStackable() && this.mainInventory[i].stackSize < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(this.mainInventory[i], itemstack)) {
            remains -= (this.mainInventory[i].getMaxStackSize() < this.getInventoryStackLimit() ? this.mainInventory[i].getMaxStackSize() : this.getInventoryStackLimit()) - this.mainInventory[i].stackSize;
         }

         if (remains <= 0) {
            return itemstack.stackSize;
         }
      }

      return itemstack.stackSize - remains;
   }

   public int getFirstEmptyStack() {
      for(int i = 0; i < this.mainInventory.length; ++i) {
         if (this.mainInventory[i] == null) {
            return i;
         }
      }

      return -1;
   }

   public void pickItem(int i) {
      this.currentItem = this.getBestHotbarSlot();
      ItemStack itemstack = this.mainInventory[this.currentItem];
      this.mainInventory[this.currentItem] = this.mainInventory[i];
      this.mainInventory[i] = itemstack;
   }

   public static boolean isHotbar(int i) {
      return i >= 0 && i < 9;
   }

   public int getBestHotbarSlot() {
      for(int i = 0; i < 9; ++i) {
         int j = (this.currentItem + i) % 9;
         if (this.mainInventory[j] == null) {
            return j;
         }
      }

      for(int var3 = 0; var3 < 9; ++var3) {
         int j = (this.currentItem + var3) % 9;
         if (!this.mainInventory[j].isItemEnchanted()) {
            return j;
         }
      }

      return this.currentItem;
   }

   public int clearMatchingItems(@Nullable Item item, int i, int j, @Nullable NBTTagCompound nbttagcompound) {
      int k = 0;

      for(int l = 0; l < this.getSizeInventory(); ++l) {
         ItemStack itemstack = this.getStackInSlot(l);
         if (itemstack != null && (item == null || itemstack.getItem() == item) && (i <= -1 || itemstack.getMetadata() == i) && (nbttagcompound == null || NBTUtil.areNBTEquals(nbttagcompound, itemstack.getTagCompound(), true))) {
            int i1 = j <= 0 ? itemstack.stackSize : Math.min(j - k, itemstack.stackSize);
            k += i1;
            if (j != 0) {
               itemstack.stackSize -= i1;
               if (itemstack.stackSize == 0) {
                  this.setInventorySlotContents(l, (ItemStack)null);
               }

               if (j > 0 && k >= j) {
                  return k;
               }
            }
         }
      }

      if (this.itemStack != null) {
         if (item != null && this.itemStack.getItem() != item) {
            return k;
         }

         if (i > -1 && this.itemStack.getMetadata() != i) {
            return k;
         }

         if (nbttagcompound != null && !NBTUtil.areNBTEquals(nbttagcompound, this.itemStack.getTagCompound(), true)) {
            return k;
         }

         int var9 = j <= 0 ? this.itemStack.stackSize : Math.min(j - k, this.itemStack.stackSize);
         k += var9;
         if (j != 0) {
            this.itemStack.stackSize -= var9;
            if (this.itemStack.stackSize == 0) {
               this.itemStack = null;
            }

            if (j > 0 && k >= j) {
               return k;
            }
         }
      }

      return k;
   }

   private int storePartialItemStack(ItemStack itemstack) {
      Item item = itemstack.getItem();
      int i = itemstack.stackSize;
      int j = this.storeItemStack(itemstack);
      if (j == -1) {
         j = this.getFirstEmptyStack();
      }

      if (j == -1) {
         return i;
      } else {
         ItemStack itemstack1 = this.getStackInSlot(j);
         if (itemstack1 == null) {
            itemstack1 = new ItemStack(item, 0, itemstack.getMetadata());
            if (itemstack.hasTagCompound()) {
               itemstack1.setTagCompound(itemstack.getTagCompound().copy());
            }

            this.setInventorySlotContents(j, itemstack1);
         }

         int k = i;
         if (i > itemstack1.getMaxStackSize() - itemstack1.stackSize) {
            k = itemstack1.getMaxStackSize() - itemstack1.stackSize;
         }

         if (k > this.getInventoryStackLimit() - itemstack1.stackSize) {
            k = this.getInventoryStackLimit() - itemstack1.stackSize;
         }

         if (k == 0) {
            return i;
         } else {
            i = i - k;
            itemstack1.stackSize += k;
            itemstack1.animationsToGo = 5;
            return i;
         }
      }
   }

   private int storeItemStack(ItemStack itemstack) {
      if (this.canMergeStacks(this.getStackInSlot(this.currentItem), itemstack)) {
         return this.currentItem;
      } else if (this.canMergeStacks(this.getStackInSlot(40), itemstack)) {
         return 40;
      } else {
         for(int i = 0; i < this.mainInventory.length; ++i) {
            if (this.canMergeStacks(this.mainInventory[i], itemstack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void decrementAnimations() {
      for(ItemStack[] aitemstack1 : this.allInventories) {
         for(int k = 0; k < aitemstack1.length; ++k) {
            if (aitemstack1[k] != null) {
               aitemstack1[k].updateAnimation(this.player.world, this.player, k, this.currentItem == k);
            }
         }
      }

   }

   public boolean addItemStackToInventory(@Nullable final ItemStack itemstack) {
      if (itemstack != null && itemstack.stackSize != 0 && itemstack.getItem() != null) {
         try {
            if (itemstack.isItemDamaged()) {
               int i = this.getFirstEmptyStack();
               if (i >= 0) {
                  this.mainInventory[i] = ItemStack.copyItemStack(itemstack);
                  this.mainInventory[i].animationsToGo = 5;
                  itemstack.stackSize = 0;
                  return true;
               } else if (this.player.capabilities.isCreativeMode) {
                  itemstack.stackSize = 0;
                  return true;
               } else {
                  return false;
               }
            } else {
               int i;
               while(true) {
                  i = itemstack.stackSize;
                  itemstack.stackSize = this.storePartialItemStack(itemstack);
                  if (itemstack.stackSize <= 0 || itemstack.stackSize >= i) {
                     break;
                  }
               }

               if (itemstack.stackSize == i && this.player.capabilities.isCreativeMode) {
                  itemstack.stackSize = 0;
                  return true;
               } else {
                  return itemstack.stackSize < i;
               }
            }
         } catch (Throwable var5) {
            CrashReport crashreport = CrashReport.makeCrashReport(var5, "Adding item to inventory");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Item being added");
            crashreportsystemdetails.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(itemstack.getItem())));
            crashreportsystemdetails.addCrashSection("Item data", Integer.valueOf(itemstack.getMetadata()));
            crashreportsystemdetails.setDetail("Item name", new ICrashReportDetail() {
               public String call() throws Exception {
                  return itemstack.getDisplayName();
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(crashreport);
         }
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack2 : this.allInventories) {
         if (i < aitemstack2.length) {
            aitemstack = aitemstack2;
            break;
         }

         i -= aitemstack2.length;
      }

      return aitemstack != null && aitemstack[i] != null ? ItemStackHelper.getAndSplit(aitemstack, i, j) : null;
   }

   public void deleteStack(ItemStack itemstack) {
      for(ItemStack[] aitemstack1 : this.allInventories) {
         for(int k = 0; k < aitemstack1.length; ++k) {
            if (aitemstack1[k] == itemstack) {
               aitemstack1[k] = null;
               break;
            }
         }
      }

   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack2 : this.allInventories) {
         if (i < aitemstack2.length) {
            aitemstack = aitemstack2;
            break;
         }

         i -= aitemstack2.length;
      }

      if (aitemstack != null && aitemstack[i] != null) {
         Object object = aitemstack[i];
         aitemstack[i] = null;
         return (ItemStack)object;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack2 : this.allInventories) {
         if (i < aitemstack2.length) {
            aitemstack = aitemstack2;
            break;
         }

         i -= aitemstack2.length;
      }

      if (aitemstack != null) {
         aitemstack[i] = itemstack;
      }

   }

   public float getStrVsBlock(IBlockState iblockdata) {
      float f = 1.0F;
      if (this.mainInventory[this.currentItem] != null) {
         f *= this.mainInventory[this.currentItem].getStrVsBlock(iblockdata);
      }

      return f;
   }

   public NBTTagList writeToNBT(NBTTagList nbttaglist) {
      for(int i = 0; i < this.mainInventory.length; ++i) {
         if (this.mainInventory[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.mainInventory[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      for(int var4 = 0; var4 < this.armorInventory.length; ++var4) {
         if (this.armorInventory[var4] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)(var4 + 100));
            this.armorInventory[var4].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      for(int var5 = 0; var5 < this.offHandInventory.length; ++var5) {
         if (this.offHandInventory[var5] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)(var5 + 150));
            this.offHandInventory[var5].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      return nbttaglist;
   }

   public void readFromNBT(NBTTagList nbttaglist) {
      Arrays.fill(this.mainInventory, (Object)null);
      Arrays.fill(this.armorInventory, (Object)null);
      Arrays.fill(this.offHandInventory, (Object)null);

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot") & 255;
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
         if (itemstack != null) {
            if (j >= 0 && j < this.mainInventory.length) {
               this.mainInventory[j] = itemstack;
            } else if (j >= 100 && j < this.armorInventory.length + 100) {
               this.armorInventory[j - 100] = itemstack;
            } else if (j >= 150 && j < this.offHandInventory.length + 150) {
               this.offHandInventory[j - 150] = itemstack;
            }
         }
      }

   }

   public int getSizeInventory() {
      return this.mainInventory.length + this.armorInventory.length + this.offHandInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack2 : this.allInventories) {
         if (i < aitemstack2.length) {
            aitemstack = aitemstack2;
            break;
         }

         i -= aitemstack2.length;
      }

      return aitemstack == null ? null : aitemstack[i];
   }

   public String getName() {
      return "container.inventory";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean canHarvestBlock(IBlockState iblockdata) {
      if (iblockdata.getMaterial().isToolNotRequired()) {
         return true;
      } else {
         ItemStack itemstack = this.getStackInSlot(this.currentItem);
         return itemstack != null ? itemstack.canHarvestBlock(iblockdata) : false;
      }
   }

   public void damageArmor(float f) {
      f = f / 4.0F;
      if (f < 1.0F) {
         f = 1.0F;
      }

      for(int i = 0; i < this.armorInventory.length; ++i) {
         if (this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
            this.armorInventory[i].damageItem((int)f, this.player);
            if (this.armorInventory[i].stackSize == 0) {
               this.armorInventory[i] = null;
            }
         }
      }

   }

   public void dropAllItems() {
      for(ItemStack[] aitemstack1 : this.allInventories) {
         for(int k = 0; k < aitemstack1.length; ++k) {
            if (aitemstack1[k] != null) {
               this.player.dropItem(aitemstack1[k], true, false);
               aitemstack1[k] = null;
            }
         }
      }

   }

   public void markDirty() {
      this.inventoryChanged = true;
   }

   public void setItemStack(@Nullable ItemStack itemstack) {
      this.itemStack = itemstack;
   }

   @Nullable
   public ItemStack getItemStack() {
      if (this.itemStack != null && this.itemStack.stackSize == 0) {
         this.setItemStack((ItemStack)null);
      }

      return this.itemStack;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.player.isDead ? false : entityhuman.getDistanceSqToEntity(this.player) <= 64.0D;
   }

   public boolean hasItemStack(ItemStack itemstack) {
      for(ItemStack[] aitemstack1 : this.allInventories) {
         for(ItemStack itemstack1 : aitemstack1) {
            if (itemstack1 != null && itemstack1.isItemEqual(itemstack)) {
               return true;
            }
         }
      }

      return false;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public void copyInventory(InventoryPlayer playerinventory) {
      for(int i = 0; i < this.getSizeInventory(); ++i) {
         this.setInventorySlotContents(i, playerinventory.getStackInSlot(i));
      }

      this.currentItem = playerinventory.currentItem;
   }

   public int getField(int i) {
      return 0;
   }

   public void setField(int i, int j) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(ItemStack[] aitemstack1 : this.allInventories) {
         for(int k = 0; k < aitemstack1.length; ++k) {
            aitemstack1[k] = null;
         }
      }

   }
}
