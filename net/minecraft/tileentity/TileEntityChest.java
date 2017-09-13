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

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public TileEntityChest(BlockChest.Type var1) {
      this.cachedChestType = var1;
   }

   public int getSizeInventory() {
      return 27;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return this.chestContents[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack var3 = ItemStackHelper.getAndSplit(this.chestContents, var1, var2);
      if (var3 != null) {
         this.markDirty();
      }

      return var3;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.chestContents, var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.fillWithLoot((EntityPlayer)null);
      this.chestContents[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.chest";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String var1) {
      this.customName = var1;
   }

   public static void registerFixesChest(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Chest", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.chestContents = new ItemStack[this.getSizeInventory()];
      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

      if (!this.checkLootAndRead(var1)) {
         NBTTagList var2 = var1.getTagList("Items", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;
            if (var5 >= 0 && var5 < this.chestContents.length) {
               this.chestContents[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      if (!this.checkLootAndWrite(var1)) {
         NBTTagList var2 = new NBTTagList();

         for(int var3 = 0; var3 < this.chestContents.length; ++var3) {
            if (this.chestContents[var3] != null) {
               NBTTagCompound var4 = new NBTTagCompound();
               var4.setByte("Slot", (byte)var3);
               this.chestContents[var3].writeToNBT(var4);
               var2.appendTag(var4);
            }
         }

         var1.setTag("Items", var2);
      }

      if (this.hasCustomName()) {
         var1.setString("CustomName", this.customName);
      }

      return var1;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      if (this.world == null) {
         return true;
      } else {
         return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
      }
   }

   public void updateContainingBlockInfo() {
      super.updateContainingBlockInfo();
      this.adjacentChestChecked = false;
   }

   private void setNeighbor(TileEntityChest var1, EnumFacing var2) {
      if (var1.isInvalid()) {
         this.adjacentChestChecked = false;
      } else if (this.adjacentChestChecked) {
         switch(TileEntityChest.SyntheticClass_1.a[var2.ordinal()]) {
         case 1:
            if (this.adjacentChestZNeg != var1) {
               this.adjacentChestChecked = false;
            }
            break;
         case 2:
            if (this.adjacentChestZPos != var1) {
               this.adjacentChestChecked = false;
            }
            break;
         case 3:
            if (this.adjacentChestXPos != var1) {
               this.adjacentChestChecked = false;
            }
            break;
         case 4:
            if (this.adjacentChestXNeg != var1) {
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
   protected TileEntityChest getAdjacentChest(EnumFacing var1) {
      BlockPos var2 = this.pos.offset(var1);
      if (this.isChestAt(var2)) {
         TileEntity var3 = this.world.getTileEntity(var2);
         if (var3 instanceof TileEntityChest) {
            TileEntityChest var4 = (TileEntityChest)var3;
            var4.setNeighbor(this, var1.getOpposite());
            return var4;
         }
      }

      return null;
   }

   private boolean isChestAt(BlockPos var1) {
      if (this.world == null) {
         return false;
      } else {
         Block var2 = this.world.getBlockState(var1).getBlock();
         return var2 instanceof BlockChest && ((BlockChest)var2).chestType == this.getChestType();
      }
   }

   public void update() {
      this.checkForAdjacentChests();
      int var1 = this.pos.getX();
      int var2 = this.pos.getY();
      int var3 = this.pos.getZ();
      ++this.ticksSinceSync;
      if (!this.world.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + var1 + var2 + var3) % 200 == 0) {
         this.numPlayersUsing = 0;

         for(EntityPlayer var6 : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double)((float)var1 - 5.0F), (double)((float)var2 - 5.0F), (double)((float)var3 - 5.0F), (double)((float)(var1 + 1) + 5.0F), (double)((float)(var2 + 1) + 5.0F), (double)((float)(var3 + 1) + 5.0F)))) {
            if (var6.openContainer instanceof ContainerChest) {
               IInventory var7 = ((ContainerChest)var6.openContainer).getLowerChestInventory();
               if (var7 == this || var7 instanceof InventoryLargeChest && ((InventoryLargeChest)var7).isPartOfLargeChest(this)) {
                  ++this.numPlayersUsing;
               }
            }
         }
      }

      this.prevLidAngle = this.lidAngle;
      if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
         double var8 = (double)var1 + 0.5D;
         double var10 = (double)var3 + 0.5D;
         if (this.adjacentChestZPos != null) {
            var10 += 0.5D;
         }

         if (this.adjacentChestXPos != null) {
            var8 += 0.5D;
         }

         this.world.playSound((EntityPlayer)null, var8, (double)var2 + 0.5D, var10, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
      }

      if (this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
         float var14 = this.lidAngle;
         if (this.numPlayersUsing > 0) {
            this.lidAngle += 0.1F;
         } else {
            this.lidAngle -= 0.1F;
         }

         if (this.lidAngle > 1.0F) {
            this.lidAngle = 1.0F;
         }

         if (this.lidAngle < 0.5F && var14 >= 0.5F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            double var15 = (double)var1 + 0.5D;
            double var12 = (double)var3 + 0.5D;
            if (this.adjacentChestZPos != null) {
               var12 += 0.5D;
            }

            if (this.adjacentChestXPos != null) {
               var15 += 0.5D;
            }

            this.world.playSound((EntityPlayer)null, var15, (double)var2 + 0.5D, var12, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
         }

         if (this.lidAngle < 0.0F) {
            this.lidAngle = 0.0F;
         }
      }

   }

   public boolean receiveClientEvent(int var1, int var2) {
      if (var1 == 1) {
         this.numPlayersUsing = var2;
         return true;
      } else {
         return super.receiveClientEvent(var1, var2);
      }
   }

   public void openInventory(EntityPlayer var1) {
      if (!var1.isSpectator()) {
         if (this.numPlayersUsing < 0) {
            this.numPlayersUsing = 0;
         }

         int var2 = Math.max(0, Math.min(15, this.numPlayersUsing));
         ++this.numPlayersUsing;
         if (this.world == null) {
            return;
         }

         this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         if (this.getBlockType() == Blocks.TRAPPED_CHEST) {
            int var3 = Math.max(0, Math.min(15, this.numPlayersUsing));
            if (var2 != var3) {
               CraftEventFactory.callRedstoneChange(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), var2, var3);
            }
         }

         this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }

   }

   public void closeInventory(EntityPlayer var1) {
      if (!var1.isSpectator() && this.getBlockType() instanceof BlockChest) {
         int var2 = Math.max(0, Math.min(15, this.numPlayersUsing));
         --this.numPlayersUsing;
         if (this.world == null) {
            return;
         }

         this.world.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         if (this.getBlockType() == Blocks.TRAPPED_CHEST) {
            int var3 = Math.max(0, Math.min(15, this.numPlayersUsing));
            if (var2 != var3) {
               CraftEventFactory.callRedstoneChange(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), var2, var3);
            }
         }

         this.world.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.world.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }

   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
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

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      this.fillWithLoot(var2);
      return new ContainerChest(var1, this, var2);
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

      for(int var1 = 0; var1 < this.chestContents.length; ++var1) {
         this.chestContents[var1] = null;
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
