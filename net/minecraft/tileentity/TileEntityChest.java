package net.minecraft.tileentity;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class TileEntityChest extends TileEntityLockableLoot implements ITickable, IInventory {
   private ItemStack[] chestContents = new ItemStack[27];
   public boolean adjacentChestChecked;
   public TileEntityChest adjacentChestZNeg;
   public TileEntityChest adjacentChestXPos;
   public TileEntityChest adjacentChestXNeg;
   public TileEntityChest adjacentChestZPos;
   public float lidAngle;
   public float prevLidAngle;
   public int numPlayersUsing;
   private int ticksSinceSync;
   private BlockChest.Type cachedChestType;
   private String customName;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public TileEntityChest() {
   }

   public ItemStack[] getContents() {
      return this.chestContents;
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

   public TileEntityChest(BlockChest.Type blockchest_type) {
      this.cachedChestType = blockchest_type;
   }

   public int getSizeInventory() {
      return 27;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return this.chestContents[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.chestContents, i, j);
      if (itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.chestContents, i);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.fillWithLoot((EntityPlayer)null);
      this.chestContents[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.chest";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String s) {
      this.customName = s;
   }

   public static void registerFixesChest(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Chest", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.chestContents = new ItemStack[this.getSizeInventory()];
      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.customName = nbttagcompound.getString("CustomName");
      }

      if (!this.checkLootAndRead(nbttagcompound)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;
            if (j >= 0 && j < this.chestContents.length) {
               this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      if (!this.checkLootAndWrite(nbttagcompound)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.chestContents.length; ++i) {
            if (this.chestContents[i] != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.setByte("Slot", (byte)i);
               this.chestContents[i].writeToNBT(nbttagcompound1);
               nbttaglist.appendTag(nbttagcompound1);
            }
         }

         nbttagcompound.setTag("Items", nbttaglist);
      }

      if (this.hasCustomName()) {
         nbttagcompound.setString("CustomName", this.customName);
      }

      return nbttagcompound;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      if (this.world == null) {
         return true;
      } else {
         return this.world.getTileEntity(this.pos) != this ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
      }
   }

   public void updateContainingBlockInfo() {
      super.updateContainingBlockInfo();
      this.adjacentChestChecked = false;
   }

   private void setNeighbor(TileEntityChest tileentitychest, EnumFacing enumdirection) {
      if (tileentitychest.isInvalid()) {
         this.adjacentChestChecked = false;
      } else if (this.adjacentChestChecked) {
         switch(TileEntityChest.SyntheticClass_1.a[enumdirection.ordinal()]) {
         case 1:
            if (this.adjacentChestZNeg != tileentitychest) {
               this.adjacentChestChecked = false;
            }
            break;
         case 2:
            if (this.adjacentChestZPos != tileentitychest) {
               this.adjacentChestChecked = false;
            }
            break;
         case 3:
            if (this.adjacentChestXPos != tileentitychest) {
               this.adjacentChestChecked = false;
            }
            break;
         case 4:
            if (this.adjacentChestXNeg != tileentitychest) {
               this.adjacentChestChecked = false;
            }
         }
      }

   }

   public void checkForAdjacentChests() {
      if (!this.adjacentChestChecked) {
         this.adjacentChestChecked = true;
         this.adjacentChestXNeg = this.getAdjacentChest(EnumFacing.WEST);
         this.adjacentChestXPos = this.getAdjacentChest(EnumFacing.EAST);
         this.adjacentChestZNeg = this.getAdjacentChest(EnumFacing.NORTH);
         this.adjacentChestZPos = this.getAdjacentChest(EnumFacing.SOUTH);
      }

   }

   @Nullable
   protected TileEntityChest getAdjacentChest(EnumFacing enumdirection) {
      BlockPos blockposition = this.pos.offset(enumdirection);
      if (this.isChestAt(blockposition)) {
         TileEntity tileentity = this.world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityChest) {
            TileEntityChest tileentitychest = (TileEntityChest)tileentity;
            tileentitychest.setNeighbor(this, enumdirection.getOpposite());
            return tileentitychest;
         }
      }

      return null;
   }

   private boolean isChestAt(BlockPos blockposition) {
      if (this.world == null) {
         return false;
      } else {
         Block block = this.world.getBlockState(blockposition).getBlock();
         return block instanceof BlockChest && ((BlockChest)block).chestType == this.getChestType();
      }
   }

   public void update() {
      this.checkForAdjacentChests();
      int i = this.pos.getX();
      int j = this.pos.getY();
      int k = this.pos.getZ();
      ++this.ticksSinceSync;
      if (!this.world.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + i + j + k) % 200 == 0) {
         this.numPlayersUsing = 0;

         for(EntityPlayer entityhuman : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F), (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F)))) {
            if (entityhuman.openContainer instanceof ContainerChest) {
               IInventory iinventory = ((ContainerChest)entityhuman.openContainer).getLowerChestInventory();
               if (iinventory == this || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest)iinventory).isPartOfLargeChest(this)) {
                  ++this.numPlayersUsing;
               }
            }
         }
      }

      this.prevLidAngle = this.lidAngle;
      if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
         double d1 = (double)i + 0.5D;
         double d0 = (double)k + 0.5D;
         if (this.adjacentChestZPos != null) {
            d0 += 0.5D;
         }

         if (this.adjacentChestXPos != null) {
            d1 += 0.5D;
         }

         this.world.playSound((EntityPlayer)null, d1, (double)j + 0.5D, d0, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
      }

      if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
         float f1 = this.lidAngle;
         if (this.numPlayersUsing > 0) {
            this.lidAngle += 0.1F;
         } else {
            this.lidAngle -= 0.1F;
         }

         if (this.lidAngle > 1.0F) {
            this.lidAngle = 1.0F;
         }

         if (this.lidAngle < 0.5F && f1 >= 0.5F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            double d0 = (double)i + 0.5D;
            double d2 = (double)k + 0.5D;
            if (this.adjacentChestZPos != null) {
               d2 += 0.5D;
            }

            if (this.adjacentChestXPos != null) {
               d0 += 0.5D;
            }

            this.world.playSound((EntityPlayer)null, d0, (double)j + 0.5D, d2, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
         }

         if (this.lidAngle < 0.0F) {
            this.lidAngle = 0.0F;
         }
      }

   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 1) {
         this.numPlayersUsing = j;
         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   public void openInventory(EntityPlayer entityhuman) {
      if (!entityhuman.isSpectator()) {
         if (this.numPlayersUsing < 0) {
            this.numPlayersUsing = 0;
         }

         int oldPower = Math.max(0, Math.min(15, this.numPlayersUsing));
         ++this.numPlayersUsing;
         if (this.world == null) {
            return;
         }

         this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         if (this.getBlockType() == Blocks.TRAPPED_CHEST) {
            int newPower = Math.max(0, Math.min(15, this.numPlayersUsing));
            if (oldPower != newPower) {
               CraftEventFactory.callRedstoneChange(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), oldPower, newPower);
            }
         }

         this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }

   }

   public void closeInventory(EntityPlayer entityhuman) {
      if (!entityhuman.isSpectator() && this.getBlockType() instanceof BlockChest) {
         int oldPower = Math.max(0, Math.min(15, this.numPlayersUsing));
         --this.numPlayersUsing;
         if (this.world == null) {
            return;
         }

         this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         if (this.getBlockType() == Blocks.TRAPPED_CHEST) {
            int newPower = Math.max(0, Math.min(15, this.numPlayersUsing));
            if (oldPower != newPower) {
               CraftEventFactory.callRedstoneChange(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), oldPower, newPower);
            }
         }

         this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }

   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public void invalidate() {
      super.invalidate();
      this.updateContainingBlockInfo();
      this.checkForAdjacentChests();
   }

   public BlockChest.Type getChestType() {
      if (this.cachedChestType == null) {
         if (this.world == null || !(this.getBlockType() instanceof BlockChest)) {
            return BlockChest.Type.BASIC;
         }

         this.cachedChestType = ((BlockChest)this.getBlockType()).chestType;
      }

      return this.cachedChestType;
   }

   public String getGuiID() {
      return "minecraft:chest";
   }

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      this.fillWithLoot(entityhuman);
      return new ContainerChest(playerinventory, this, entityhuman);
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

      for(int i = 0; i < this.chestContents.length; ++i) {
         this.chestContents[i] = null;
      }

   }

   public boolean onlyOpsCanSetNbt() {
      return true;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.NORTH.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
