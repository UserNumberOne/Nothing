package net.minecraft.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionHelper;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;

public class TileEntityBrewingStand extends TileEntityLockable implements ITickable, ISidedInventory {
   private static final int[] SLOTS_FOR_UP = new int[]{3};
   private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
   private static final int[] OUTPUT_SLOTS = new int[]{0, 1, 2, 4};
   private ItemStack[] brewingItemStacks = new ItemStack[5];
   private int brewTime;
   private boolean[] filledSlots;
   private Item ingredientID;
   private String customName;
   private int fuel;
   private int lastTick = MinecraftServer.currentTick;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public ItemStack[] getContents() {
      return this.brewingItemStacks;
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.brewing";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String s) {
      this.customName = s;
   }

   public int getSizeInventory() {
      return this.brewingItemStacks.length;
   }

   public void update() {
      if (this.fuel <= 0 && this.brewingItemStacks[4] != null && this.brewingItemStacks[4].getItem() == Items.BLAZE_POWDER) {
         this.fuel = 20;
         --this.brewingItemStacks[4].stackSize;
         if (this.brewingItemStacks[4].stackSize <= 0) {
            this.brewingItemStacks[4] = null;
         }

         this.markDirty();
      }

      boolean flag = this.canBrew();
      boolean flag1 = this.brewTime > 0;
      int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
      this.lastTick = MinecraftServer.currentTick;
      if (flag1) {
         this.brewTime -= elapsedTicks;
         boolean flag2 = this.brewTime <= 0;
         if (flag2 && flag) {
            this.brewPotions();
            this.markDirty();
         } else if (!flag) {
            this.brewTime = 0;
            this.markDirty();
         } else if (this.ingredientID != this.brewingItemStacks[3].getItem()) {
            this.brewTime = 0;
            this.markDirty();
         }
      } else if (flag && this.fuel > 0) {
         --this.fuel;
         this.brewTime = 400;
         this.ingredientID = this.brewingItemStacks[3].getItem();
         this.markDirty();
      }

      if (!this.world.isRemote) {
         boolean[] aboolean = this.createFilledSlotsArray();
         if (!Arrays.equals(aboolean, this.filledSlots)) {
            this.filledSlots = aboolean;
            IBlockState iblockdata = this.world.getBlockState(this.getPos());
            if (!(iblockdata.getBlock() instanceof BlockBrewingStand)) {
               return;
            }

            for(int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
               iblockdata = iblockdata.withProperty(BlockBrewingStand.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
            }

            this.world.setBlockState(this.pos, iblockdata, 2);
         }
      }

   }

   public boolean[] createFilledSlotsArray() {
      boolean[] aboolean = new boolean[3];

      for(int i = 0; i < 3; ++i) {
         if (this.brewingItemStacks[i] != null) {
            aboolean[i] = true;
         }
      }

      return aboolean;
   }

   private boolean canBrew() {
      if (this.brewingItemStacks[3] != null && this.brewingItemStacks[3].stackSize > 0) {
         ItemStack itemstack = this.brewingItemStacks[3];
         if (!PotionHelper.isReagent(itemstack)) {
            return false;
         } else {
            for(int i = 0; i < 3; ++i) {
               ItemStack itemstack1 = this.brewingItemStacks[i];
               if (itemstack1 != null && PotionHelper.hasConversions(itemstack1, itemstack)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private void brewPotions() {
      ItemStack itemstack = this.brewingItemStacks[3];
      if (this.getOwner() != null) {
         BrewEvent event = new BrewEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), (BrewerInventory)this.getOwner().getInventory());
         Bukkit.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }
      }

      for(int i = 0; i < 3; ++i) {
         this.brewingItemStacks[i] = PotionHelper.doReaction(itemstack, this.brewingItemStacks[i]);
      }

      --itemstack.stackSize;
      BlockPos blockposition = this.getPos();
      if (itemstack.getItem().hasContainerItem()) {
         ItemStack itemstack1 = new ItemStack(itemstack.getItem().getContainerItem());
         if (itemstack.stackSize <= 0) {
            itemstack = itemstack1;
         } else {
            InventoryHelper.spawnItemStack(this.world, (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ(), itemstack1);
         }
      }

      if (itemstack.stackSize <= 0) {
         itemstack = null;
      }

      this.brewingItemStacks[3] = itemstack;
      this.world.playEvent(1035, blockposition, 0);
   }

   public static void registerFixesBrewingStand(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Cauldron", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.brewingItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         if (b0 >= 0 && b0 < this.brewingItemStacks.length) {
            this.brewingItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         }
      }

      this.brewTime = nbttagcompound.getShort("BrewTime");
      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.customName = nbttagcompound.getString("CustomName");
      }

      this.fuel = nbttagcompound.getByte("Fuel");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setShort("BrewTime", (short)this.brewTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.brewingItemStacks.length; ++i) {
         if (this.brewingItemStacks[i] != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.brewingItemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Items", nbttaglist);
      if (this.hasCustomName()) {
         nbttagcompound.setString("CustomName", this.customName);
      }

      nbttagcompound.setByte("Fuel", (byte)this.fuel);
      return nbttagcompound;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return i >= 0 && i < this.brewingItemStacks.length ? this.brewingItemStacks[i] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      return ItemStackHelper.getAndSplit(this.brewingItemStacks, i, j);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      return ItemStackHelper.getAndRemove(this.brewingItemStacks, i);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      if (i >= 0 && i < this.brewingItemStacks.length) {
         this.brewingItemStacks[i] = itemstack;
      }

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
      if (i == 3) {
         return PotionHelper.isReagent(itemstack);
      } else {
         Item item = itemstack.getItem();
         return i == 4 ? item == Items.BLAZE_POWDER : item == Items.POTIONITEM || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE;
      }
   }

   public int[] getSlotsForFace(EnumFacing enumdirection) {
      return enumdirection == EnumFacing.UP ? SLOTS_FOR_UP : (enumdirection == EnumFacing.DOWN ? SLOTS_FOR_DOWN : OUTPUT_SLOTS);
   }

   public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      return this.isItemValidForSlot(i, itemstack);
   }

   public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      return i == 3 ? itemstack.getItem() == Items.GLASS_BOTTLE : true;
   }

   public String getGuiID() {
      return "minecraft:brewing_stand";
   }

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      return new ContainerBrewingStand(playerinventory, this);
   }

   public int getField(int i) {
      switch(i) {
      case 0:
         return this.brewTime;
      case 1:
         return this.fuel;
      default:
         return 0;
      }
   }

   public void setField(int i, int j) {
      switch(i) {
      case 0:
         this.brewTime = j;
         break;
      case 1:
         this.fuel = j;
      }

   }

   public int getFieldCount() {
      return 2;
   }

   public void clear() {
      Arrays.fill(this.brewingItemStacks, (Object)null);
   }
}
