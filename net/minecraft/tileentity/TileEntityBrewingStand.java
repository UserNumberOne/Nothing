package net.minecraft.tileentity;

import java.util.Arrays;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

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
   IItemHandler handlerInput = new SidedInvWrapper(this, EnumFacing.UP);
   IItemHandler handlerOutput = new SidedInvWrapper(this, EnumFacing.DOWN);
   IItemHandler handlerSides = new SidedInvWrapper(this, EnumFacing.NORTH);

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.brewing";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String var1) {
      this.customName = name;
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
      if (flag1) {
         --this.brewTime;
         boolean flag2 = this.brewTime == 0;
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
            IBlockState iblockstate = this.world.getBlockState(this.getPos());
            if (!(iblockstate.getBlock() instanceof BlockBrewingStand)) {
               return;
            }

            for(int i = 0; i < BlockBrewingStand.HAS_BOTTLE.length; ++i) {
               iblockstate = iblockstate.withProperty(BlockBrewingStand.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
            }

            this.world.setBlockState(this.pos, iblockstate, 2);
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
         ;
      }

      return BrewingRecipeRegistry.canBrew(this.brewingItemStacks, this.brewingItemStacks[3], OUTPUT_SLOTS);
   }

   private void brewPotions() {
      if (!ForgeEventFactory.onPotionAttemptBrew(this.brewingItemStacks)) {
         ItemStack itemstack = this.brewingItemStacks[3];
         BrewingRecipeRegistry.brewPotions(this.brewingItemStacks, this.brewingItemStacks[3], OUTPUT_SLOTS);
         --itemstack.stackSize;
         BlockPos blockpos = this.getPos();
         if (itemstack.getItem().hasContainerItem(itemstack)) {
            ItemStack itemstack1 = itemstack.getItem().getContainerItem(itemstack);
            if (itemstack.stackSize <= 0) {
               itemstack = itemstack1;
            } else {
               InventoryHelper.spawnItemStack(this.world, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack1);
            }
         }

         if (itemstack.stackSize <= 0) {
            itemstack = null;
         }

         this.brewingItemStacks[3] = itemstack;
         this.world.playEvent(1035, blockpos, 0);
         ForgeEventFactory.onPotionBrewed(this.brewingItemStacks);
      }
   }

   public static void registerFixesBrewingStand(DataFixer var0) {
      fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Cauldron", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      NBTTagList nbttaglist = compound.getTagList("Items", 10);
      this.brewingItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot");
         if (j >= 0 && j < this.brewingItemStacks.length) {
            this.brewingItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
         }
      }

      this.brewTime = compound.getShort("BrewTime");
      if (compound.hasKey("CustomName", 8)) {
         this.customName = compound.getString("CustomName");
      }

      this.fuel = compound.getByte("Fuel");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      compound.setShort("BrewTime", (short)this.brewTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.brewingItemStacks.length; ++i) {
         if (this.brewingItemStacks[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.brewingItemStacks[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      compound.setTag("Items", nbttaglist);
      if (this.hasCustomName()) {
         compound.setString("CustomName", this.customName);
      }

      compound.setByte("Fuel", (byte)this.fuel);
      return compound;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return index >= 0 && index < this.brewingItemStacks.length ? this.brewingItemStacks[index] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return ItemStackHelper.getAndSplit(this.brewingItemStacks, index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.brewingItemStacks, index);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (index >= 0 && index < this.brewingItemStacks.length) {
         this.brewingItemStacks[index] = stack;
      }

   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      if (index == 3) {
         return BrewingRecipeRegistry.isValidIngredient(stack);
      } else {
         Item item = stack.getItem();
         return index == 4 ? item == Items.BLAZE_POWDER : BrewingRecipeRegistry.isValidInput(stack);
      }
   }

   public int[] getSlotsForFace(EnumFacing var1) {
      return side == EnumFacing.UP ? SLOTS_FOR_UP : (side == EnumFacing.DOWN ? SLOTS_FOR_DOWN : OUTPUT_SLOTS);
   }

   public boolean canInsertItem(int var1, ItemStack var2, EnumFacing var3) {
      return this.isItemValidForSlot(index, itemStackIn);
   }

   public boolean canExtractItem(int var1, ItemStack var2, EnumFacing var3) {
      return index == 3 ? stack.getItem() == Items.GLASS_BOTTLE : true;
   }

   public String getGuiID() {
      return "minecraft:brewing_stand";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerBrewingStand(playerInventory, this);
   }

   public int getField(int var1) {
      switch(id) {
      case 0:
         return this.brewTime;
      case 1:
         return this.fuel;
      default:
         return 0;
      }
   }

   public void setField(int var1, int var2) {
      switch(id) {
      case 0:
         this.brewTime = value;
         break;
      case 1:
         this.fuel = value;
      }

   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == EnumFacing.UP) {
            return this.handlerInput;
         } else {
            return facing == EnumFacing.DOWN ? this.handlerOutput : this.handlerSides;
         }
      } else {
         return super.getCapability(capability, facing);
      }
   }

   public int getFieldCount() {
      return 2;
   }

   public void clear() {
      Arrays.fill(this.brewingItemStacks, (Object)null);
   }
}
