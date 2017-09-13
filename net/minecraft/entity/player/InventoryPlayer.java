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
      ItemStack[] var1 = new ItemStack[this.mainInventory.length + this.armorInventory.length + this.offHandInventory.length];
      System.arraycopy(this.mainInventory, 0, var1, 0, this.mainInventory.length);
      System.arraycopy(this.armorInventory, 0, var1, this.mainInventory.length, this.armorInventory.length);
      System.arraycopy(this.offHandInventory, 0, var1, this.mainInventory.length + this.armorInventory.length, this.offHandInventory.length);
      return var1;
   }

   public ItemStack[] getArmorContents() {
      return this.armorInventory;
   }

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return this.player.getBukkitEntity();
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public Location getLocation() {
      return this.player.getBukkitEntity().getLocation();
   }

   public InventoryPlayer(EntityPlayer var1) {
      this.allInventories = new ItemStack[][]{this.mainInventory, this.armorInventory, this.offHandInventory};
      this.player = var1;
   }

   @Nullable
   public ItemStack getCurrentItem() {
      return isHotbar(this.currentItem) ? this.mainInventory[this.currentItem] : null;
   }

   public static int getHotbarSize() {
      return 9;
   }

   private boolean canMergeStacks(@Nullable ItemStack var1, ItemStack var2) {
      return var1 != null && this.stackEqualExact(var1, var2) && var1.isStackable() && var1.stackSize < var1.getMaxStackSize() && var1.stackSize < this.getInventoryStackLimit();
   }

   private boolean stackEqualExact(ItemStack var1, ItemStack var2) {
      return var1.getItem() == var2.getItem() && (!var1.getHasSubtypes() || var1.getMetadata() == var2.getMetadata()) && ItemStack.areItemStackTagsEqual(var1, var2);
   }

   public int canHold(ItemStack var1) {
      int var2 = var1.stackSize;

      for(int var3 = 0; var3 < this.mainInventory.length; ++var3) {
         if (this.mainInventory[var3] == null) {
            return var1.stackSize;
         }

         if (this.mainInventory[var3] != null && this.mainInventory[var3].getItem() == var1.getItem() && this.mainInventory[var3].isStackable() && this.mainInventory[var3].stackSize < this.mainInventory[var3].getMaxStackSize() && this.mainInventory[var3].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[var3].getHasSubtypes() || this.mainInventory[var3].getMetadata() == var1.getMetadata()) && ItemStack.areItemStackTagsEqual(this.mainInventory[var3], var1)) {
            var2 -= (this.mainInventory[var3].getMaxStackSize() < this.getInventoryStackLimit() ? this.mainInventory[var3].getMaxStackSize() : this.getInventoryStackLimit()) - this.mainInventory[var3].stackSize;
         }

         if (var2 <= 0) {
            return var1.stackSize;
         }
      }

      return var1.stackSize - var2;
   }

   public int getFirstEmptyStack() {
      for(int var1 = 0; var1 < this.mainInventory.length; ++var1) {
         if (this.mainInventory[var1] == null) {
            return var1;
         }
      }

      return -1;
   }

   public void pickItem(int var1) {
      this.currentItem = this.getBestHotbarSlot();
      ItemStack var2 = this.mainInventory[this.currentItem];
      this.mainInventory[this.currentItem] = this.mainInventory[var1];
      this.mainInventory[var1] = var2;
   }

   public static boolean isHotbar(int var0) {
      return var0 >= 0 && var0 < 9;
   }

   public int getBestHotbarSlot() {
      for(int var1 = 0; var1 < 9; ++var1) {
         int var2 = (this.currentItem + var1) % 9;
         if (this.mainInventory[var2] == null) {
            return var2;
         }
      }

      for(int var3 = 0; var3 < 9; ++var3) {
         int var4 = (this.currentItem + var3) % 9;
         if (!this.mainInventory[var4].isItemEnchanted()) {
            return var4;
         }
      }

      return this.currentItem;
   }

   public int clearMatchingItems(@Nullable Item var1, int var2, int var3, @Nullable NBTTagCompound var4) {
      int var5 = 0;

      for(int var6 = 0; var6 < this.getSizeInventory(); ++var6) {
         ItemStack var7 = this.getStackInSlot(var6);
         if (var7 != null && (var1 == null || var7.getItem() == var1) && (var2 <= -1 || var7.getMetadata() == var2) && (var4 == null || NBTUtil.areNBTEquals(var4, var7.getTagCompound(), true))) {
            int var8 = var3 <= 0 ? var7.stackSize : Math.min(var3 - var5, var7.stackSize);
            var5 += var8;
            if (var3 != 0) {
               var7.stackSize -= var8;
               if (var7.stackSize == 0) {
                  this.setInventorySlotContents(var6, (ItemStack)null);
               }

               if (var3 > 0 && var5 >= var3) {
                  return var5;
               }
            }
         }
      }

      if (this.itemStack != null) {
         if (var1 != null && this.itemStack.getItem() != var1) {
            return var5;
         }

         if (var2 > -1 && this.itemStack.getMetadata() != var2) {
            return var5;
         }

         if (var4 != null && !NBTUtil.areNBTEquals(var4, this.itemStack.getTagCompound(), true)) {
            return var5;
         }

         int var9 = var3 <= 0 ? this.itemStack.stackSize : Math.min(var3 - var5, this.itemStack.stackSize);
         var5 += var9;
         if (var3 != 0) {
            this.itemStack.stackSize -= var9;
            if (this.itemStack.stackSize == 0) {
               this.itemStack = null;
            }

            if (var3 > 0 && var5 >= var3) {
               return var5;
            }
         }
      }

      return var5;
   }

   private int storePartialItemStack(ItemStack var1) {
      Item var2 = var1.getItem();
      int var3 = var1.stackSize;
      int var4 = this.storeItemStack(var1);
      if (var4 == -1) {
         var4 = this.getFirstEmptyStack();
      }

      if (var4 == -1) {
         return var3;
      } else {
         ItemStack var5 = this.getStackInSlot(var4);
         if (var5 == null) {
            var5 = new ItemStack(var2, 0, var1.getMetadata());
            if (var1.hasTagCompound()) {
               var5.setTagCompound(var1.getTagCompound().copy());
            }

            this.setInventorySlotContents(var4, var5);
         }

         int var6 = var3;
         if (var3 > var5.getMaxStackSize() - var5.stackSize) {
            var6 = var5.getMaxStackSize() - var5.stackSize;
         }

         if (var6 > this.getInventoryStackLimit() - var5.stackSize) {
            var6 = this.getInventoryStackLimit() - var5.stackSize;
         }

         if (var6 == 0) {
            return var3;
         } else {
            var3 = var3 - var6;
            var5.stackSize += var6;
            var5.animationsToGo = 5;
            return var3;
         }
      }
   }

   private int storeItemStack(ItemStack var1) {
      if (this.canMergeStacks(this.getStackInSlot(this.currentItem), var1)) {
         return this.currentItem;
      } else if (this.canMergeStacks(this.getStackInSlot(40), var1)) {
         return 40;
      } else {
         for(int var2 = 0; var2 < this.mainInventory.length; ++var2) {
            if (this.canMergeStacks(this.mainInventory[var2], var1)) {
               return var2;
            }
         }

         return -1;
      }
   }

   public void decrementAnimations() {
      for(ItemStack[] var4 : this.allInventories) {
         for(int var5 = 0; var5 < var4.length; ++var5) {
            if (var4[var5] != null) {
               var4[var5].updateAnimation(this.player.world, this.player, var5, this.currentItem == var5);
            }
         }
      }

   }

   public boolean addItemStackToInventory(@Nullable final ItemStack var1) {
      if (var1 != null && var1.stackSize != 0 && var1.getItem() != null) {
         try {
            if (var1.isItemDamaged()) {
               int var6 = this.getFirstEmptyStack();
               if (var6 >= 0) {
                  this.mainInventory[var6] = ItemStack.copyItemStack(var1);
                  this.mainInventory[var6].animationsToGo = 5;
                  var1.stackSize = 0;
                  return true;
               } else if (this.player.capabilities.isCreativeMode) {
                  var1.stackSize = 0;
                  return true;
               } else {
                  return false;
               }
            } else {
               int var2;
               while(true) {
                  var2 = var1.stackSize;
                  var1.stackSize = this.storePartialItemStack(var1);
                  if (var1.stackSize <= 0 || var1.stackSize >= var2) {
                     break;
                  }
               }

               if (var1.stackSize == var2 && this.player.capabilities.isCreativeMode) {
                  var1.stackSize = 0;
                  return true;
               } else {
                  return var1.stackSize < var2;
               }
            }
         } catch (Throwable var5) {
            CrashReport var3 = CrashReport.makeCrashReport(var5, "Adding item to inventory");
            CrashReportCategory var4 = var3.makeCategory("Item being added");
            var4.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(var1.getItem())));
            var4.addCrashSection("Item data", Integer.valueOf(var1.getMetadata()));
            var4.setDetail("Item name", new ICrashReportDetail() {
               public String call() throws Exception {
                  return var1.getDisplayName();
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(var3);
         }
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      ItemStack[] var3 = null;

      for(ItemStack[] var7 : this.allInventories) {
         if (var1 < var7.length) {
            var3 = var7;
            break;
         }

         var1 -= var7.length;
      }

      return var3 != null && var3[var1] != null ? ItemStackHelper.getAndSplit(var3, var1, var2) : null;
   }

   public void deleteStack(ItemStack var1) {
      for(ItemStack[] var5 : this.allInventories) {
         for(int var6 = 0; var6 < var5.length; ++var6) {
            if (var5[var6] == var1) {
               var5[var6] = null;
               break;
            }
         }
      }

   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      ItemStack[] var2 = null;

      for(ItemStack[] var6 : this.allInventories) {
         if (var1 < var6.length) {
            var2 = var6;
            break;
         }

         var1 -= var6.length;
      }

      if (var2 != null && var2[var1] != null) {
         Object var7 = var2[var1];
         var2[var1] = null;
         return (ItemStack)var7;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      ItemStack[] var3 = null;

      for(ItemStack[] var7 : this.allInventories) {
         if (var1 < var7.length) {
            var3 = var7;
            break;
         }

         var1 -= var7.length;
      }

      if (var3 != null) {
         var3[var1] = var2;
      }

   }

   public float getStrVsBlock(IBlockState var1) {
      float var2 = 1.0F;
      if (this.mainInventory[this.currentItem] != null) {
         var2 *= this.mainInventory[this.currentItem].getStrVsBlock(var1);
      }

      return var2;
   }

   public NBTTagList writeToNBT(NBTTagList var1) {
      for(int var2 = 0; var2 < this.mainInventory.length; ++var2) {
         if (this.mainInventory[var2] != null) {
            NBTTagCompound var3 = new NBTTagCompound();
            var3.setByte("Slot", (byte)var2);
            this.mainInventory[var2].writeToNBT(var3);
            var1.appendTag(var3);
         }
      }

      for(int var4 = 0; var4 < this.armorInventory.length; ++var4) {
         if (this.armorInventory[var4] != null) {
            NBTTagCompound var6 = new NBTTagCompound();
            var6.setByte("Slot", (byte)(var4 + 100));
            this.armorInventory[var4].writeToNBT(var6);
            var1.appendTag(var6);
         }
      }

      for(int var5 = 0; var5 < this.offHandInventory.length; ++var5) {
         if (this.offHandInventory[var5] != null) {
            NBTTagCompound var7 = new NBTTagCompound();
            var7.setByte("Slot", (byte)(var5 + 150));
            this.offHandInventory[var5].writeToNBT(var7);
            var1.appendTag(var7);
         }
      }

      return var1;
   }

   public void readFromNBT(NBTTagList var1) {
      Arrays.fill(this.mainInventory, (Object)null);
      Arrays.fill(this.armorInventory, (Object)null);
      Arrays.fill(this.offHandInventory, (Object)null);

      for(int var2 = 0; var2 < var1.tagCount(); ++var2) {
         NBTTagCompound var3 = var1.getCompoundTagAt(var2);
         int var4 = var3.getByte("Slot") & 255;
         ItemStack var5 = ItemStack.loadItemStackFromNBT(var3);
         if (var5 != null) {
            if (var4 >= 0 && var4 < this.mainInventory.length) {
               this.mainInventory[var4] = var5;
            } else if (var4 >= 100 && var4 < this.armorInventory.length + 100) {
               this.armorInventory[var4 - 100] = var5;
            } else if (var4 >= 150 && var4 < this.offHandInventory.length + 150) {
               this.offHandInventory[var4 - 150] = var5;
            }
         }
      }

   }

   public int getSizeInventory() {
      return this.mainInventory.length + this.armorInventory.length + this.offHandInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      ItemStack[] var2 = null;

      for(ItemStack[] var6 : this.allInventories) {
         if (var1 < var6.length) {
            var2 = var6;
            break;
         }

         var1 -= var6.length;
      }

      return var2 == null ? null : var2[var1];
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

   public boolean canHarvestBlock(IBlockState var1) {
      if (var1.getMaterial().isToolNotRequired()) {
         return true;
      } else {
         ItemStack var2 = this.getStackInSlot(this.currentItem);
         return var2 != null ? var2.canHarvestBlock(var1) : false;
      }
   }

   public void damageArmor(float var1) {
      var1 = var1 / 4.0F;
      if (var1 < 1.0F) {
         var1 = 1.0F;
      }

      for(int var2 = 0; var2 < this.armorInventory.length; ++var2) {
         if (this.armorInventory[var2] != null && this.armorInventory[var2].getItem() instanceof ItemArmor) {
            this.armorInventory[var2].damageItem((int)var1, this.player);
            if (this.armorInventory[var2].stackSize == 0) {
               this.armorInventory[var2] = null;
            }
         }
      }

   }

   public void dropAllItems() {
      for(ItemStack[] var4 : this.allInventories) {
         for(int var5 = 0; var5 < var4.length; ++var5) {
            if (var4[var5] != null) {
               this.player.dropItem(var4[var5], true, false);
               var4[var5] = null;
            }
         }
      }

   }

   public void markDirty() {
      this.inventoryChanged = true;
   }

   public void setItemStack(@Nullable ItemStack var1) {
      this.itemStack = var1;
   }

   @Nullable
   public ItemStack getItemStack() {
      if (this.itemStack != null && this.itemStack.stackSize == 0) {
         this.setItemStack((ItemStack)null);
      }

      return this.itemStack;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.player.isDead ? false : var1.getDistanceSqToEntity(this.player) <= 64.0D;
   }

   public boolean hasItemStack(ItemStack var1) {
      for(ItemStack[] var5 : this.allInventories) {
         for(ItemStack var9 : var5) {
            if (var9 != null && var9.isItemEqual(var1)) {
               return true;
            }
         }
      }

      return false;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public void copyInventory(InventoryPlayer var1) {
      for(int var2 = 0; var2 < this.getSizeInventory(); ++var2) {
         this.setInventorySlotContents(var2, var1.getStackInSlot(var2));
      }

      this.currentItem = var1.currentItem;
   }

   public int getField(int var1) {
      return 0;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(ItemStack[] var4 : this.allInventories) {
         for(int var5 = 0; var5 < var4.length; ++var5) {
            var4[var5] = null;
         }
      }

   }
}
