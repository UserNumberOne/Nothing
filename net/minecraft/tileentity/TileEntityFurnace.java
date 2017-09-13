package net.minecraft.tileentity;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.MathHelper;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
   private static final int[] SLOTS_TOP = new int[1];
   private static final int[] SLOTS_BOTTOM = new int[]{2, 1};
   private static final int[] SLOTS_SIDES = new int[]{1};
   private ItemStack[] furnaceItemStacks = new ItemStack[3];
   private int furnaceBurnTime;
   private int currentItemBurnTime;
   private int cookTime;
   private int totalCookTime;
   private String furnaceCustomName;
   private int lastTick = MinecraftServer.currentTick;
   private int maxStack = 64;
   public List transaction = new ArrayList();

   public ItemStack[] getContents() {
      return this.furnaceItemStacks;
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

   public int getSizeInventory() {
      return this.furnaceItemStacks.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return this.furnaceItemStacks[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      return ItemStackHelper.getAndSplit(this.furnaceItemStacks, i, j);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      return ItemStackHelper.getAndRemove(this.furnaceItemStacks, i);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      boolean flag = itemstack != null && itemstack.isItemEqual(this.furnaceItemStacks[i]) && ItemStack.areItemStackTagsEqual(itemstack, this.furnaceItemStacks[i]);
      this.furnaceItemStacks[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

      if (i == 0 && !flag) {
         this.totalCookTime = this.getCookTime(itemstack);
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

   public void setCustomInventoryName(String s) {
      this.furnaceCustomName = s;
   }

   public static void registerFixesFurnace(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Furnace", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte b0 = nbttagcompound1.getByte("Slot");
         if (b0 >= 0 && b0 < this.furnaceItemStacks.length) {
            this.furnaceItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         }
      }

      this.furnaceBurnTime = nbttagcompound.getShort("BurnTime");
      this.cookTime = nbttagcompound.getShort("CookTime");
      this.totalCookTime = nbttagcompound.getShort("CookTimeTotal");
      this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.furnaceCustomName = nbttagcompound.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setShort("BurnTime", (short)this.furnaceBurnTime);
      nbttagcompound.setShort("CookTime", (short)this.cookTime);
      nbttagcompound.setShort("CookTimeTotal", (short)this.totalCookTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         if (this.furnaceItemStacks[i] != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.furnaceItemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Items", nbttaglist);
      if (this.hasCustomName()) {
         nbttagcompound.setString("CustomName", this.furnaceCustomName);
      }

      return nbttagcompound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   public void update() {
      boolean flag = this.getBlockType() == Blocks.LIT_FURNACE;
      boolean flag1 = false;
      int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
      this.lastTick = MinecraftServer.currentTick;
      if (this.isBurning() && this.canSmelt()) {
         this.cookTime += elapsedTicks;
         if (this.cookTime >= this.totalCookTime) {
            this.cookTime = 0;
            this.totalCookTime = this.getCookTime(this.furnaceItemStacks[0]);
            this.smeltItem();
            flag1 = true;
         }
      } else {
         this.cookTime = 0;
      }

      if (this.isBurning()) {
         this.furnaceBurnTime -= elapsedTicks;
      }

      if (!this.world.isRemote) {
         if (!this.isBurning() && (this.furnaceItemStacks[1] == null || this.furnaceItemStacks[0] == null)) {
            if (!this.isBurning() && this.cookTime > 0) {
               this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
            }
         } else if (this.furnaceBurnTime <= 0 && this.canSmelt()) {
            CraftItemStack fuel = CraftItemStack.asCraftMirror(this.furnaceItemStacks[1]);
            FurnaceBurnEvent furnaceBurnEvent = new FurnaceBurnEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), fuel, getItemBurnTime(this.furnaceItemStacks[1]));
            this.world.getServer().getPluginManager().callEvent(furnaceBurnEvent);
            if (furnaceBurnEvent.isCancelled()) {
               return;
            }

            this.currentItemBurnTime = furnaceBurnEvent.getBurnTime();
            this.furnaceBurnTime += this.currentItemBurnTime;
            if (this.furnaceBurnTime > 0 && furnaceBurnEvent.isBurning()) {
               flag1 = true;
               if (this.furnaceItemStacks[1] != null) {
                  --this.furnaceItemStacks[1].stackSize;
                  if (this.furnaceItemStacks[1].stackSize == 0) {
                     Item item = this.furnaceItemStacks[1].getItem().getContainerItem();
                     this.furnaceItemStacks[1] = item != null ? new ItemStack(item) : null;
                  }
               }
            }
         }

         if (flag != this.isBurning()) {
            flag1 = true;
            BlockFurnace.setState(this.isBurning(), this.world, this.pos);
            this.updateContainingBlockInfo();
         }
      }

      if (flag1) {
         this.markDirty();
      }

   }

   public int getCookTime(@Nullable ItemStack itemstack) {
      return 200;
   }

   private boolean canSmelt() {
      if (this.furnaceItemStacks[0] == null) {
         return false;
      } else {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         return itemstack == null ? false : (this.furnaceItemStacks[2] == null ? true : (!this.furnaceItemStacks[2].isItemEqual(itemstack) ? false : (this.furnaceItemStacks[2].stackSize + itemstack.stackSize <= this.getInventoryStackLimit() && this.furnaceItemStacks[2].stackSize < this.furnaceItemStacks[2].getMaxStackSize() ? true : this.furnaceItemStacks[2].stackSize + itemstack.stackSize <= itemstack.getMaxStackSize())));
      }
   }

   public void smeltItem() {
      if (this.canSmelt()) {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         CraftItemStack source = CraftItemStack.asCraftMirror(this.furnaceItemStacks[0]);
         org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack);
         FurnaceSmeltEvent furnaceSmeltEvent = new FurnaceSmeltEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), source, result);
         this.world.getServer().getPluginManager().callEvent(furnaceSmeltEvent);
         if (furnaceSmeltEvent.isCancelled()) {
            return;
         }

         result = furnaceSmeltEvent.getResult();
         itemstack = CraftItemStack.asNMSCopy(result);
         if (itemstack != null) {
            if (this.furnaceItemStacks[2] == null) {
               this.furnaceItemStacks[2] = itemstack;
            } else {
               if (!CraftItemStack.asCraftMirror(this.furnaceItemStacks[2]).isSimilar(result)) {
                  return;
               }

               this.furnaceItemStacks[2].stackSize += itemstack.stackSize;
            }
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

   public static int getItemBurnTime(ItemStack itemstack) {
      if (itemstack == null) {
         return 0;
      } else {
         Item item = itemstack.getItem();
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

         return item instanceof ItemTool && "WOOD".equals(((ItemTool)item).getToolMaterialName()) ? 200 : (item instanceof ItemSword && "WOOD".equals(((ItemSword)item).getToolMaterialName()) ? 200 : (item instanceof ItemHoe && "WOOD".equals(((ItemHoe)item).getMaterialName()) ? 200 : (item == Items.STICK ? 100 : (item == Items.COAL ? 1600 : (item == Items.LAVA_BUCKET ? 20000 : (item == Item.getItemFromBlock(Blocks.SAPLING) ? 100 : (item == Items.BLAZE_ROD ? 2400 : 0)))))));
      }
   }

   public static boolean isItemFuel(ItemStack itemstack) {
      return getItemBurnTime(itemstack) > 0;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.world.getTileEntity(this.pos) != this ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      if (i == 2) {
         return false;
      } else if (i != 1) {
         return true;
      } else {
         ItemStack itemstack1 = this.furnaceItemStacks[1];
         return isItemFuel(itemstack) || SlotFurnaceFuel.isBucket(itemstack) && (itemstack1 == null || itemstack1.getItem() != Items.BUCKET);
      }
   }

   public int[] getSlotsForFace(EnumFacing enumdirection) {
      return enumdirection == EnumFacing.DOWN ? SLOTS_BOTTOM : (enumdirection == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
   }

   public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      return this.isItemValidForSlot(i, itemstack);
   }

   public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      if (enumdirection == EnumFacing.DOWN && i == 1) {
         Item item = itemstack.getItem();
         if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
            return false;
         }
      }

      return true;
   }

   public String getGuiID() {
      return "minecraft:furnace";
   }

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      return new ContainerFurnace(playerinventory, this);
   }

   public int getField(int i) {
      switch(i) {
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

   public void setField(int i, int j) {
      switch(i) {
      case 0:
         this.furnaceBurnTime = j;
         break;
      case 1:
         this.currentItemBurnTime = j;
         break;
      case 2:
         this.cookTime = j;
         break;
      case 3:
         this.totalCookTime = j;
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
}
