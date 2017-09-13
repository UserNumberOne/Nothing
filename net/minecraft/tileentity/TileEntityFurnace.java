package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
   private static final int[] SLOTS_TOP = new int[]{0};
   private static final int[] SLOTS_BOTTOM = new int[]{2, 1};
   private static final int[] SLOTS_SIDES = new int[]{1};
   private ItemStack[] furnaceItemStacks = new ItemStack[3];
   private int furnaceBurnTime;
   private int currentItemBurnTime;
   private int cookTime;
   private int totalCookTime;
   private String furnaceCustomName;
   IItemHandler handlerTop = new SidedInvWrapper(this, EnumFacing.UP);
   IItemHandler handlerBottom = new SidedInvWrapper(this, EnumFacing.DOWN);
   IItemHandler handlerSide = new SidedInvWrapper(this, EnumFacing.WEST);

   public int getSizeInventory() {
      return this.furnaceItemStacks.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return this.furnaceItemStacks[index];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      boolean flag = stack != null && stack.isItemEqual(this.furnaceItemStacks[index]) && ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks[index]);
      this.furnaceItemStacks[index] = stack;
      if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

      if (index == 0 && !flag) {
         this.totalCookTime = this.getCookTime(stack);
         this.cookTime = 0;
         this.markDirty();
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.furnaceCustomName : "container.furnace";
   }

   public boolean hasCustomName() {
      return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
   }

   public void setCustomInventoryName(String var1) {
      this.furnaceCustomName = p_145951_1_;
   }

   public static void registerFixesFurnace(DataFixer var0) {
      fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Furnace", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      NBTTagList nbttaglist = compound.getTagList("Items", 10);
      this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot");
         if (j >= 0 && j < this.furnaceItemStacks.length) {
            this.furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
         }
      }

      this.furnaceBurnTime = compound.getInteger("BurnTime");
      this.cookTime = compound.getInteger("CookTime");
      this.totalCookTime = compound.getInteger("CookTimeTotal");
      this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
      if (compound.hasKey("CustomName", 8)) {
         this.furnaceCustomName = compound.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      compound.setInteger("BurnTime", this.furnaceBurnTime);
      compound.setInteger("CookTime", this.cookTime);
      compound.setInteger("CookTimeTotal", this.totalCookTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         if (this.furnaceItemStacks[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.furnaceItemStacks[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      compound.setTag("Items", nbttaglist);
      if (this.hasCustomName()) {
         compound.setString("CustomName", this.furnaceCustomName);
      }

      return compound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   @SideOnly(Side.CLIENT)
   public static boolean isBurning(IInventory var0) {
      return inventory.getField(0) > 0;
   }

   public void update() {
      boolean flag = this.isBurning();
      boolean flag1 = false;
      if (this.isBurning()) {
         --this.furnaceBurnTime;
      }

      if (!this.world.isRemote) {
         if (this.isBurning() || this.furnaceItemStacks[1] != null && this.furnaceItemStacks[0] != null) {
            if (!this.isBurning() && this.canSmelt()) {
               this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
               this.currentItemBurnTime = this.furnaceBurnTime;
               if (this.isBurning()) {
                  flag1 = true;
                  if (this.furnaceItemStacks[1] != null) {
                     --this.furnaceItemStacks[1].stackSize;
                     if (this.furnaceItemStacks[1].stackSize == 0) {
                        this.furnaceItemStacks[1] = this.furnaceItemStacks[1].getItem().getContainerItem(this.furnaceItemStacks[1]);
                     }
                  }
               }
            }

            if (this.isBurning() && this.canSmelt()) {
               ++this.cookTime;
               if (this.cookTime == this.totalCookTime) {
                  this.cookTime = 0;
                  this.totalCookTime = this.getCookTime(this.furnaceItemStacks[0]);
                  this.smeltItem();
                  flag1 = true;
               }
            } else {
               this.cookTime = 0;
            }
         } else if (!this.isBurning() && this.cookTime > 0) {
            this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
         }

         if (flag != this.isBurning()) {
            flag1 = true;
            BlockFurnace.setState(this.isBurning(), this.world, this.pos);
         }
      }

      if (flag1) {
         this.markDirty();
      }

   }

   public int getCookTime(@Nullable ItemStack var1) {
      return 200;
   }

   private boolean canSmelt() {
      if (this.furnaceItemStacks[0] == null) {
         return false;
      } else {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         if (itemstack == null) {
            return false;
         } else if (this.furnaceItemStacks[2] == null) {
            return true;
         } else if (!this.furnaceItemStacks[2].isItemEqual(itemstack)) {
            return false;
         } else {
            int result = this.furnaceItemStacks[2].stackSize + itemstack.stackSize;
            return result <= this.getInventoryStackLimit() && result <= this.furnaceItemStacks[2].getMaxStackSize();
         }
      }
   }

   public void smeltItem() {
      if (this.canSmelt()) {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         if (this.furnaceItemStacks[2] == null) {
            this.furnaceItemStacks[2] = itemstack.copy();
         } else if (this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
            this.furnaceItemStacks[2].stackSize += itemstack.stackSize;
         }

         if (this.furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.SPONGE) && this.furnaceItemStacks[0].getMetadata() == 1 && this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].getItem() == Items.BUCKET) {
            this.furnaceItemStacks[1] = new ItemStack(Items.WATER_BUCKET);
         }

         --this.furnaceItemStacks[0].stackSize;
         if (this.furnaceItemStacks[0].stackSize <= 0) {
            this.furnaceItemStacks[0] = null;
         }
      }

   }

   public static int getItemBurnTime(ItemStack var0) {
      if (stack == null) {
         return 0;
      } else {
         Item item = stack.getItem();
         if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
            Block block = Block.getBlockFromItem(item);
            if (block == Blocks.WOODEN_SLAB) {
               return 150;
            }

            if (block.getDefaultState().getMaterial() == Material.WOOD) {
               return 300;
            }

            if (block == Blocks.COAL_BLOCK) {
               return 16000;
            }
         }

         if (item instanceof ItemTool && "WOOD".equals(((ItemTool)item).getToolMaterialName())) {
            return 200;
         } else if (item instanceof ItemSword && "WOOD".equals(((ItemSword)item).getToolMaterialName())) {
            return 200;
         } else if (item instanceof ItemHoe && "WOOD".equals(((ItemHoe)item).getMaterialName())) {
            return 200;
         } else if (item == Items.STICK) {
            return 100;
         } else if (item == Items.COAL) {
            return 1600;
         } else if (item == Items.LAVA_BUCKET) {
            return 20000;
         } else if (item == Item.getItemFromBlock(Blocks.SAPLING)) {
            return 100;
         } else {
            return item == Items.BLAZE_ROD ? 2400 : GameRegistry.getFuelValue(stack);
         }
      }
   }

   public static boolean isItemFuel(ItemStack var0) {
      return getItemBurnTime(stack) > 0;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      if (index == 2) {
         return false;
      } else if (index != 1) {
         return true;
      } else {
         ItemStack itemstack = this.furnaceItemStacks[1];
         return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && (itemstack == null || itemstack.getItem() != Items.BUCKET);
      }
   }

   public int[] getSlotsForFace(EnumFacing var1) {
      return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
   }

   public boolean canInsertItem(int var1, ItemStack var2, EnumFacing var3) {
      return this.isItemValidForSlot(index, itemStackIn);
   }

   public boolean canExtractItem(int var1, ItemStack var2, EnumFacing var3) {
      if (direction == EnumFacing.DOWN && index == 1) {
         Item item = stack.getItem();
         if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
            return false;
         }
      }

      return true;
   }

   public String getGuiID() {
      return "minecraft:furnace";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerFurnace(playerInventory, this);
   }

   public int getField(int var1) {
      switch(id) {
      case 0:
         return this.furnaceBurnTime;
      case 1:
         return this.currentItemBurnTime;
      case 2:
         return this.cookTime;
      case 3:
         return this.totalCookTime;
      default:
         return 0;
      }
   }

   public void setField(int var1, int var2) {
      switch(id) {
      case 0:
         this.furnaceBurnTime = value;
         break;
      case 1:
         this.currentItemBurnTime = value;
         break;
      case 2:
         this.cookTime = value;
         break;
      case 3:
         this.totalCookTime = value;
      }

   }

   public int getFieldCount() {
      return 4;
   }

   public void clear() {
      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         this.furnaceItemStacks[i] = null;
      }

   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == EnumFacing.DOWN) {
            return this.handlerBottom;
         } else {
            return facing == EnumFacing.UP ? this.handlerTop : this.handlerSide;
         }
      } else {
         return super.getCapability(capability, facing);
      }
   }
}
