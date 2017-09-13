package net.minecraft.tileentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaHopperItemHandler;
import net.minecraftforge.items.VanillaInventoryCodeHooks;

public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable {
   private ItemStack[] inventory = new ItemStack[5];
   private String customName;
   private int transferCooldown = -1;

   public static void registerFixesHopper(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Hopper", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.inventory = new ItemStack[this.getSizeInventory()];
      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

      this.transferCooldown = var1.getInteger("TransferCooldown");
      if (!this.checkLootAndRead(var1)) {
         NBTTagList var2 = var1.getTagList("Items", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            byte var5 = var4.getByte("Slot");
            if (var5 >= 0 && var5 < this.inventory.length) {
               this.inventory[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      if (!this.checkLootAndWrite(var1)) {
         NBTTagList var2 = new NBTTagList();

         for(int var3 = 0; var3 < this.inventory.length; ++var3) {
            if (this.inventory[var3] != null) {
               NBTTagCompound var4 = new NBTTagCompound();
               var4.setByte("Slot", (byte)var3);
               this.inventory[var3].writeToNBT(var4);
               var2.appendTag(var4);
            }
         }

         var1.setTag("Items", var2);
      }

      var1.setInteger("TransferCooldown", this.transferCooldown);
      if (this.hasCustomName()) {
         var1.setString("CustomName", this.customName);
      }

      return var1;
   }

   public int getSizeInventory() {
      return this.inventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return this.inventory[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.inventory, var1, var2);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.inventory, var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.fillWithLoot((EntityPlayer)null);
      this.inventory[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.hopper";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String var1) {
      this.customName = var1;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public void update() {
      if (this.world != null && !this.world.isRemote) {
         --this.transferCooldown;
         if (!this.isOnTransferCooldown()) {
            this.setTransferCooldown(0);
            this.updateHopper();
         }
      }

   }

   public boolean updateHopper() {
      if (this.world != null && !this.world.isRemote) {
         if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata())) {
            boolean var1 = false;
            if (!this.isEmpty()) {
               var1 = this.transferItemsOut();
            }

            if (!this.isFull()) {
               var1 = captureDroppedItems(this) || var1;
            }

            if (var1) {
               this.setTransferCooldown(8);
               this.markDirty();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isEmpty() {
      for(ItemStack var4 : this.inventory) {
         if (var4 != null) {
            return false;
         }
      }

      return true;
   }

   private boolean isFull() {
      for(ItemStack var4 : this.inventory) {
         if (var4 == null || var4.stackSize != var4.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private boolean transferItemsOut() {
      if (VanillaInventoryCodeHooks.insertHook(this)) {
         return true;
      } else {
         IInventory var1 = this.getInventoryForHopperTransfer();
         if (var1 == null) {
            return false;
         } else {
            EnumFacing var2 = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();
            if (this.isInventoryFull(var1, var2)) {
               return false;
            } else {
               for(int var3 = 0; var3 < this.getSizeInventory(); ++var3) {
                  if (this.getStackInSlot(var3) != null) {
                     ItemStack var4 = this.getStackInSlot(var3).copy();
                     ItemStack var5 = putStackInInventoryAllSlots(var1, this.decrStackSize(var3, 1), var2);
                     if (var5 == null || var5.stackSize == 0) {
                        var1.markDirty();
                        return true;
                     }

                     this.setInventorySlotContents(var3, var4);
                  }
               }

               return false;
            }
         }
      }
   }

   private boolean isInventoryFull(IInventory var1, EnumFacing var2) {
      if (var1 instanceof ISidedInventory) {
         ISidedInventory var10 = (ISidedInventory)var1;
         int[] var11 = var10.getSlotsForFace(var2);

         for(int var8 : var11) {
            ItemStack var9 = var10.getStackInSlot(var8);
            if (var9 == null || var9.stackSize != var9.getMaxStackSize()) {
               return false;
            }
         }
      } else {
         int var3 = var1.getSizeInventory();

         for(int var4 = 0; var4 < var3; ++var4) {
            ItemStack var5 = var1.getStackInSlot(var4);
            if (var5 == null || var5.stackSize != var5.getMaxStackSize()) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean isInventoryEmpty(IInventory var0, EnumFacing var1) {
      if (var0 instanceof ISidedInventory) {
         ISidedInventory var2 = (ISidedInventory)var0;
         int[] var3 = var2.getSlotsForFace(var1);

         for(int var7 : var3) {
            if (var2.getStackInSlot(var7) != null) {
               return false;
            }
         }
      } else {
         int var8 = var0.getSizeInventory();

         for(int var9 = 0; var9 < var8; ++var9) {
            if (var0.getStackInSlot(var9) != null) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean captureDroppedItems(IHopper var0) {
      Boolean var1 = VanillaInventoryCodeHooks.extractHook(var0);
      if (var1 != null) {
         return var1.booleanValue();
      } else {
         IInventory var2 = getHopperInventory(var0);
         if (var2 != null) {
            EnumFacing var3 = EnumFacing.DOWN;
            if (isInventoryEmpty(var2, var3)) {
               return false;
            }

            if (var2 instanceof ISidedInventory) {
               ISidedInventory var4 = (ISidedInventory)var2;
               int[] var5 = var4.getSlotsForFace(var3);

               for(int var9 : var5) {
                  if (pullItemFromSlot(var0, var2, var9, var3)) {
                     return true;
                  }
               }
            } else {
               int var11 = var2.getSizeInventory();

               for(int var13 = 0; var13 < var11; ++var13) {
                  if (pullItemFromSlot(var0, var2, var13, var3)) {
                     return true;
                  }
               }
            }
         } else {
            for(EntityItem var12 : getCaptureItems(var0.getWorld(), var0.getXPos(), var0.getYPos(), var0.getZPos())) {
               if (putDropInInventoryAllSlots(var0, var12)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean pullItemFromSlot(IHopper var0, IInventory var1, int var2, EnumFacing var3) {
      ItemStack var4 = var1.getStackInSlot(var2);
      if (var4 != null && canExtractItemFromSlot(var1, var4, var2, var3)) {
         ItemStack var5 = var4.copy();
         ItemStack var6 = putStackInInventoryAllSlots(var0, var1.decrStackSize(var2, 1), (EnumFacing)null);
         if (var6 == null || var6.stackSize == 0) {
            var1.markDirty();
            return true;
         }

         var1.setInventorySlotContents(var2, var5);
      }

      return false;
   }

   public static boolean putDropInInventoryAllSlots(IInventory var0, EntityItem var1) {
      boolean var2 = false;
      if (var1 == null) {
         return false;
      } else {
         ItemStack var3 = var1.getEntityItem().copy();
         ItemStack var4 = putStackInInventoryAllSlots(var0, var3, (EnumFacing)null);
         if (var4 != null && var4.stackSize != 0) {
            var1.setEntityItemStack(var4);
         } else {
            var2 = true;
            var1.setDead();
         }

         return var2;
      }
   }

   public static ItemStack putStackInInventoryAllSlots(IInventory var0, ItemStack var1, @Nullable EnumFacing var2) {
      if (var0 instanceof ISidedInventory && var2 != null) {
         ISidedInventory var6 = (ISidedInventory)var0;
         int[] var7 = var6.getSlotsForFace(var2);

         for(int var5 = 0; var5 < var7.length && var1 != null && var1.stackSize > 0; ++var5) {
            var1 = insertStack(var0, var1, var7[var5], var2);
         }
      } else {
         int var3 = var0.getSizeInventory();

         for(int var4 = 0; var4 < var3 && var1 != null && var1.stackSize > 0; ++var4) {
            var1 = insertStack(var0, var1, var4, var2);
         }
      }

      if (var1 != null && var1.stackSize == 0) {
         var1 = null;
      }

      return var1;
   }

   private static boolean canInsertItemInSlot(IInventory var0, ItemStack var1, int var2, EnumFacing var3) {
      return !var0.isItemValidForSlot(var2, var1) ? false : !(var0 instanceof ISidedInventory) || ((ISidedInventory)var0).canInsertItem(var2, var1, var3);
   }

   private static boolean canExtractItemFromSlot(IInventory var0, ItemStack var1, int var2, EnumFacing var3) {
      return !(var0 instanceof ISidedInventory) || ((ISidedInventory)var0).canExtractItem(var2, var1, var3);
   }

   private static ItemStack insertStack(IInventory var0, ItemStack var1, int var2, EnumFacing var3) {
      ItemStack var4 = var0.getStackInSlot(var2);
      if (canInsertItemInSlot(var0, var1, var2, var3)) {
         boolean var5 = false;
         if (var4 == null) {
            int var6 = Math.min(var1.getMaxStackSize(), var0.getInventoryStackLimit());
            if (var6 >= var1.stackSize) {
               var0.setInventorySlotContents(var2, var1);
               var1 = null;
            } else {
               var0.setInventorySlotContents(var2, var1.splitStack(var6));
            }

            var5 = true;
         } else if (canCombine(var4, var1)) {
            int var9 = Math.min(var1.getMaxStackSize(), var0.getInventoryStackLimit());
            if (var9 > var4.stackSize) {
               int var7 = var9 - var4.stackSize;
               int var8 = Math.min(var1.stackSize, var7);
               var1.stackSize -= var8;
               var4.stackSize += var8;
               var5 = var8 > 0;
            }
         }

         if (var5) {
            if (var0 instanceof TileEntityHopper) {
               TileEntityHopper var10 = (TileEntityHopper)var0;
               if (var10.mayTransfer()) {
                  var10.setTransferCooldown(8);
               }

               var0.markDirty();
            }

            var0.markDirty();
         }
      }

      return var1;
   }

   private IInventory getInventoryForHopperTransfer() {
      EnumFacing var1 = BlockHopper.getFacing(this.getBlockMetadata());
      return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)var1.getFrontOffsetX(), this.getYPos() + (double)var1.getFrontOffsetY(), this.getZPos() + (double)var1.getFrontOffsetZ());
   }

   public static IInventory getHopperInventory(IHopper var0) {
      return getInventoryAtPosition(var0.getWorld(), var0.getXPos(), var0.getYPos() + 1.0D, var0.getZPos());
   }

   public static List getCaptureItems(World var0, double var1, double var3, double var5) {
      return var0.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(var1 - 0.5D, var3, var5 - 0.5D, var1 + 0.5D, var3 + 1.5D, var5 + 0.5D), EntitySelectors.IS_ALIVE);
   }

   public static IInventory getInventoryAtPosition(World var0, double var1, double var3, double var5) {
      Object var7 = null;
      int var8 = MathHelper.floor(var1);
      int var9 = MathHelper.floor(var3);
      int var10 = MathHelper.floor(var5);
      BlockPos var11 = new BlockPos(var8, var9, var10);
      Block var12 = var0.getBlockState(var11).getBlock();
      if (var12.hasTileEntity()) {
         TileEntity var13 = var0.getTileEntity(var11);
         if (var13 instanceof IInventory) {
            var7 = (IInventory)var13;
            if (var7 instanceof TileEntityChest && var12 instanceof BlockChest) {
               var7 = ((BlockChest)var12).getContainer(var0, var11, true);
            }
         }
      }

      if (var7 == null) {
         List var14 = var0.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(var1 - 0.5D, var3 - 0.5D, var5 - 0.5D, var1 + 0.5D, var3 + 0.5D, var5 + 0.5D), EntitySelectors.HAS_INVENTORY);
         if (!var14.isEmpty()) {
            var7 = (IInventory)var14.get(var0.rand.nextInt(var14.size()));
         }
      }

      return (IInventory)var7;
   }

   private static boolean canCombine(ItemStack var0, ItemStack var1) {
      return var0.getItem() != var1.getItem() ? false : (var0.getMetadata() != var1.getMetadata() ? false : (var0.stackSize > var0.getMaxStackSize() ? false : ItemStack.areItemStackTagsEqual(var0, var1)));
   }

   public double getXPos() {
      return (double)this.pos.getX() + 0.5D;
   }

   public double getYPos() {
      return (double)this.pos.getY() + 0.5D;
   }

   public double getZPos() {
      return (double)this.pos.getZ() + 0.5D;
   }

   public void setTransferCooldown(int var1) {
      this.transferCooldown = var1;
   }

   public boolean isOnTransferCooldown() {
      return this.transferCooldown > 0;
   }

   public boolean mayTransfer() {
      return this.transferCooldown <= 1;
   }

   public String getGuiID() {
      return "minecraft:hopper";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      this.fillWithLoot(var2);
      return new ContainerHopper(var1, this, var2);
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
      this.fillWithLoot((EntityPlayer)null);

      for(int var1 = 0; var1 < this.inventory.length; ++var1) {
         this.inventory[var1] = null;
      }

   }

   protected IItemHandler createUnSidedHandler() {
      return new VanillaHopperItemHandler(this);
   }
}
