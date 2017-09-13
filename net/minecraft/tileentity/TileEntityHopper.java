package net.minecraft.tileentity;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
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
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;

public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable {
   private ItemStack[] inventory = new ItemStack[5];
   private String customName;
   private int transferCooldown = -1;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.inventory;
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

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public static void registerFixesHopper(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Hopper", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.inventory = new ItemStack[this.getSizeInventory()];
      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.customName = nbttagcompound.getString("CustomName");
      }

      this.transferCooldown = nbttagcompound.getInteger("TransferCooldown");
      if (!this.checkLootAndRead(nbttagcompound)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");
            if (b0 >= 0 && b0 < this.inventory.length) {
               this.inventory[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      if (!this.checkLootAndWrite(nbttagcompound)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.inventory.length; ++i) {
            if (this.inventory[i] != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.setByte("Slot", (byte)i);
               this.inventory[i].writeToNBT(nbttagcompound1);
               nbttaglist.appendTag(nbttagcompound1);
            }
         }

         nbttagcompound.setTag("Items", nbttaglist);
      }

      nbttagcompound.setInteger("TransferCooldown", this.transferCooldown);
      if (this.hasCustomName()) {
         nbttagcompound.setString("CustomName", this.customName);
      }

      return nbttagcompound;
   }

   public int getSizeInventory() {
      return this.inventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return this.inventory[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.inventory, i, j);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.inventory, i);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.fillWithLoot((EntityPlayer)null);
      this.inventory[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.hopper";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String s) {
      this.customName = s;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.world.getTileEntity(this.pos) != this ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
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
            boolean flag = false;
            if (!this.isEmpty()) {
               flag = this.transferItemsOut();
            }

            if (!this.isFull()) {
               flag = captureDroppedItems(this) || flag;
            }

            if (flag) {
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
      for(ItemStack itemstack : this.inventory) {
         if (itemstack != null) {
            return false;
         }
      }

      return true;
   }

   private boolean isFull() {
      for(ItemStack itemstack : this.inventory) {
         if (itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private boolean transferItemsOut() {
      IInventory iinventory = this.getInventoryForHopperTransfer();
      if (iinventory == null) {
         return false;
      } else {
         EnumFacing enumdirection = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();
         if (this.isInventoryFull(iinventory, enumdirection)) {
            return false;
         } else {
            for(int i = 0; i < this.getSizeInventory(); ++i) {
               if (this.getStackInSlot(i) != null) {
                  ItemStack itemstack = this.getStackInSlot(i).copy();
                  CraftItemStack oitemstack = CraftItemStack.asCraftMirror(this.decrStackSize(i, 1));
                  Inventory destinationInventory;
                  if (iinventory instanceof InventoryLargeChest) {
                     destinationInventory = new CraftInventoryDoubleChest((InventoryLargeChest)iinventory);
                  } else {
                     destinationInventory = iinventory.getOwner().getInventory();
                  }

                  InventoryMoveItemEvent event = new InventoryMoveItemEvent(this.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
                  this.getWorld().getServer().getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     this.setInventorySlotContents(i, itemstack);
                     this.setTransferCooldown(8);
                     return false;
                  }

                  ItemStack itemstack1 = putStackInInventoryAllSlots(iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection);
                  if (itemstack1 == null || itemstack1.stackSize == 0) {
                     if (event.getItem().equals(oitemstack)) {
                        iinventory.markDirty();
                     } else {
                        this.setInventorySlotContents(i, itemstack);
                     }

                     return true;
                  }

                  this.setInventorySlotContents(i, itemstack);
               }
            }

            return false;
         }
      }
   }

   private boolean isInventoryFull(IInventory iinventory, EnumFacing enumdirection) {
      if (iinventory instanceof ISidedInventory) {
         ISidedInventory iworldinventory = (ISidedInventory)iinventory;
         int[] aint = iworldinventory.getSlotsForFace(enumdirection);

         for(int k : aint) {
            ItemStack itemstack = iworldinventory.getStackInSlot(k);
            if (itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
               return false;
            }
         }
      } else {
         int l = iinventory.getSizeInventory();

         for(int i1 = 0; i1 < l; ++i1) {
            ItemStack itemstack1 = iinventory.getStackInSlot(i1);
            if (itemstack1 == null || itemstack1.stackSize != itemstack1.getMaxStackSize()) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean isInventoryEmpty(IInventory iinventory, EnumFacing enumdirection) {
      if (iinventory instanceof ISidedInventory) {
         ISidedInventory iworldinventory = (ISidedInventory)iinventory;
         int[] aint = iworldinventory.getSlotsForFace(enumdirection);

         for(int k : aint) {
            if (iworldinventory.getStackInSlot(k) != null) {
               return false;
            }
         }
      } else {
         int l = iinventory.getSizeInventory();

         for(int i1 = 0; i1 < l; ++i1) {
            if (iinventory.getStackInSlot(i1) != null) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean captureDroppedItems(IHopper ihopper) {
      IInventory iinventory = getHopperInventory(ihopper);
      if (iinventory != null) {
         EnumFacing enumdirection = EnumFacing.DOWN;
         if (isInventoryEmpty(iinventory, enumdirection)) {
            return false;
         }

         if (iinventory instanceof ISidedInventory) {
            ISidedInventory iworldinventory = (ISidedInventory)iinventory;
            int[] aint = iworldinventory.getSlotsForFace(enumdirection);

            for(int k : aint) {
               if (pullItemFromSlot(ihopper, iinventory, k, enumdirection)) {
                  return true;
               }
            }
         } else {
            int l = iinventory.getSizeInventory();

            for(int i1 = 0; i1 < l; ++i1) {
               if (pullItemFromSlot(ihopper, iinventory, i1, enumdirection)) {
                  return true;
               }
            }
         }
      } else {
         for(EntityItem entityitem : getCaptureItems(ihopper.getWorld(), ihopper.getXPos(), ihopper.getYPos(), ihopper.getZPos())) {
            if (putDropInInventoryAllSlots(ihopper, entityitem)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean pullItemFromSlot(IHopper ihopper, IInventory iinventory, int i, EnumFacing enumdirection) {
      ItemStack itemstack = iinventory.getStackInSlot(i);
      if (itemstack != null && canExtractItemFromSlot(iinventory, itemstack, i, enumdirection)) {
         ItemStack itemstack1 = itemstack.copy();
         CraftItemStack oitemstack = CraftItemStack.asCraftMirror(iinventory.decrStackSize(i, 1));
         Inventory sourceInventory;
         if (iinventory instanceof InventoryLargeChest) {
            sourceInventory = new CraftInventoryDoubleChest((InventoryLargeChest)iinventory);
         } else {
            sourceInventory = iinventory.getOwner().getInventory();
         }

         InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), ihopper.getOwner().getInventory(), false);
         ihopper.getWorld().getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            iinventory.setInventorySlotContents(i, itemstack1);
            if (ihopper instanceof TileEntityHopper) {
               ((TileEntityHopper)ihopper).setTransferCooldown(8);
            } else if (ihopper instanceof EntityMinecartHopper) {
               ((EntityMinecartHopper)ihopper).setTransferTicker(4);
            }

            return false;
         }

         ItemStack itemstack2 = putStackInInventoryAllSlots(ihopper, CraftItemStack.asNMSCopy(event.getItem()), (EnumFacing)null);
         if (itemstack2 == null || itemstack2.stackSize == 0) {
            if (event.getItem().equals(oitemstack)) {
               iinventory.markDirty();
            } else {
               iinventory.setInventorySlotContents(i, itemstack1);
            }

            return true;
         }

         iinventory.setInventorySlotContents(i, itemstack1);
      }

      return false;
   }

   public static boolean putDropInInventoryAllSlots(IInventory iinventory, EntityItem entityitem) {
      boolean flag = false;
      if (entityitem == null) {
         return false;
      } else {
         InventoryPickupItemEvent event = new InventoryPickupItemEvent(iinventory.getOwner().getInventory(), (Item)entityitem.getBukkitEntity());
         entityitem.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return false;
         } else {
            ItemStack itemstack = entityitem.getEntityItem().copy();
            ItemStack itemstack1 = putStackInInventoryAllSlots(iinventory, itemstack, (EnumFacing)null);
            if (itemstack1 != null && itemstack1.stackSize != 0) {
               entityitem.setEntityItemStack(itemstack1);
            } else {
               flag = true;
               entityitem.setDead();
            }

            return flag;
         }
      }
   }

   public static ItemStack putStackInInventoryAllSlots(IInventory iinventory, ItemStack itemstack, @Nullable EnumFacing enumdirection) {
      if (iinventory instanceof ISidedInventory && enumdirection != null) {
         ISidedInventory iworldinventory = (ISidedInventory)iinventory;
         int[] aint = iworldinventory.getSlotsForFace(enumdirection);

         for(int i = 0; i < aint.length && itemstack != null && itemstack.stackSize > 0; ++i) {
            itemstack = insertStack(iinventory, itemstack, aint[i], enumdirection);
         }
      } else {
         int j = iinventory.getSizeInventory();

         for(int k = 0; k < j && itemstack != null && itemstack.stackSize > 0; ++k) {
            itemstack = insertStack(iinventory, itemstack, k, enumdirection);
         }
      }

      if (itemstack != null && itemstack.stackSize == 0) {
         itemstack = null;
      }

      return itemstack;
   }

   private static boolean canInsertItemInSlot(IInventory iinventory, ItemStack itemstack, int i, EnumFacing enumdirection) {
      return !iinventory.isItemValidForSlot(i, itemstack) ? false : !(iinventory instanceof ISidedInventory) || ((ISidedInventory)iinventory).canInsertItem(i, itemstack, enumdirection);
   }

   private static boolean canExtractItemFromSlot(IInventory iinventory, ItemStack itemstack, int i, EnumFacing enumdirection) {
      return !(iinventory instanceof ISidedInventory) || ((ISidedInventory)iinventory).canExtractItem(i, itemstack, enumdirection);
   }

   private static ItemStack insertStack(IInventory iinventory, ItemStack itemstack, int i, EnumFacing enumdirection) {
      ItemStack itemstack1 = iinventory.getStackInSlot(i);
      if (canInsertItemInSlot(iinventory, itemstack, i, enumdirection)) {
         boolean flag = false;
         if (itemstack1 == null) {
            iinventory.setInventorySlotContents(i, itemstack);
            itemstack = null;
            flag = true;
         } else if (canCombine(itemstack1, itemstack)) {
            int j = itemstack.getMaxStackSize() - itemstack1.stackSize;
            int k = Math.min(itemstack.stackSize, j);
            itemstack.stackSize -= k;
            itemstack1.stackSize += k;
            flag = k > 0;
         }

         if (flag) {
            if (iinventory instanceof TileEntityHopper) {
               TileEntityHopper tileentityhopper = (TileEntityHopper)iinventory;
               if (tileentityhopper.mayTransfer()) {
                  tileentityhopper.setTransferCooldown(8);
               }

               iinventory.markDirty();
            }

            iinventory.markDirty();
         }
      }

      return itemstack;
   }

   private IInventory getInventoryForHopperTransfer() {
      EnumFacing enumdirection = BlockHopper.getFacing(this.getBlockMetadata());
      return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)enumdirection.getFrontOffsetX(), this.getYPos() + (double)enumdirection.getFrontOffsetY(), this.getZPos() + (double)enumdirection.getFrontOffsetZ());
   }

   public static IInventory getHopperInventory(IHopper ihopper) {
      return getInventoryAtPosition(ihopper.getWorld(), ihopper.getXPos(), ihopper.getYPos() + 1.0D, ihopper.getZPos());
   }

   public static List getCaptureItems(World world, double d0, double d1, double d2) {
      return world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(d0 - 0.5D, d1, d2 - 0.5D, d0 + 0.5D, d1 + 1.5D, d2 + 0.5D), EntitySelectors.IS_ALIVE);
   }

   public static IInventory getInventoryAtPosition(World world, double d0, double d1, double d2) {
      Object object = null;
      int i = MathHelper.floor(d0);
      int j = MathHelper.floor(d1);
      int k = MathHelper.floor(d2);
      BlockPos blockposition = new BlockPos(i, j, k);
      Block block = world.getBlockState(blockposition).getBlock();
      if (block.hasTileEntity()) {
         TileEntity tileentity = world.getTileEntity(blockposition);
         if (tileentity instanceof IInventory) {
            object = (IInventory)tileentity;
            if (object instanceof TileEntityChest && block instanceof BlockChest) {
               object = ((BlockChest)block).getContainer(world, blockposition, true);
            }
         }
      }

      if (object == null) {
         List list = world.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, d0 + 0.5D, d1 + 0.5D, d2 + 0.5D), EntitySelectors.HAS_INVENTORY);
         if (!list.isEmpty()) {
            object = (IInventory)list.get(world.rand.nextInt(list.size()));
         }
      }

      return (IInventory)object;
   }

   private static boolean canCombine(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack.getItem() != itemstack1.getItem() ? false : (itemstack.getMetadata() != itemstack1.getMetadata() ? false : (itemstack.stackSize > itemstack.getMaxStackSize() ? false : ItemStack.areItemStackTagsEqual(itemstack, itemstack1)));
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

   public void setTransferCooldown(int i) {
      this.transferCooldown = i;
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

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      this.fillWithLoot(entityhuman);
      return new ContainerHopper(playerinventory, this, entityhuman);
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
      this.fillWithLoot((EntityPlayer)null);

      for(int i = 0; i < this.inventory.length; ++i) {
         this.inventory[i] = null;
      }

   }
}
