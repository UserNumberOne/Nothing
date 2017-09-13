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

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public ItemStack[] getContents() {
      return this.brewingItemStacks;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.brewing";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String var1) {
      this.customName = var1;
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

      boolean var1 = this.canBrew();
      boolean var2 = this.brewTime > 0;
      int var3 = MinecraftServer.currentTick - this.lastTick;
      this.lastTick = MinecraftServer.currentTick;
      if (var2) {
         this.brewTime -= var3;
         boolean var4 = this.brewTime <= 0;
         if (var4 && var1) {
            this.brewPotions();
            this.markDirty();
         } else if (!var1) {
            this.brewTime = 0;
            this.markDirty();
         } else if (this.ingredientID != this.brewingItemStacks[3].getItem()) {
            this.brewTime = 0;
            this.markDirty();
         }
      } else if (var1 && this.fuel > 0) {
         --this.fuel;
         this.brewTime = 400;
         this.ingredientID = this.brewingItemStacks[3].getItem();
         this.markDirty();
      }

      if (!this.world.isRemote) {
         boolean[] var7 = this.createFilledSlotsArray();
         if (!Arrays.equals(var7, this.filledSlots)) {
            this.filledSlots = var7;
            IBlockState var5 = this.world.getBlockState(this.getPos());
            if (!(var5.getBlock() instanceof BlockBrewingStand)) {
               return;
            }

            for(int var6 = 0; var6 < BlockBrewingStand.HAS_BOTTLE.length; ++var6) {
               var5 = var5.withProperty(BlockBrewingStand.HAS_BOTTLE[var6], Boolean.valueOf(var7[var6]));
            }

            this.world.setBlockState(this.pos, var5, 2);
         }
      }

   }

   public boolean[] createFilledSlotsArray() {
      boolean[] var1 = new boolean[3];

      for(int var2 = 0; var2 < 3; ++var2) {
         if (this.brewingItemStacks[var2] != null) {
            var1[var2] = true;
         }
      }

      return var1;
   }

   private boolean canBrew() {
      if (this.brewingItemStacks[3] != null && this.brewingItemStacks[3].stackSize > 0) {
         ItemStack var1 = this.brewingItemStacks[3];
         if (!PotionHelper.isReagent(var1)) {
            return false;
         } else {
            for(int var2 = 0; var2 < 3; ++var2) {
               ItemStack var3 = this.brewingItemStacks[var2];
               if (var3 != null && PotionHelper.hasConversions(var3, var1)) {
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
      ItemStack var1 = this.brewingItemStacks[3];
      if (this.getOwner() != null) {
         BrewEvent var2 = new BrewEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), (BrewerInventory)this.getOwner().getInventory());
         Bukkit.getPluginManager().callEvent(var2);
         if (var2.isCancelled()) {
            return;
         }
      }

      for(int var4 = 0; var4 < 3; ++var4) {
         this.brewingItemStacks[var4] = PotionHelper.doReaction(var1, this.brewingItemStacks[var4]);
      }

      --var1.stackSize;
      BlockPos var5 = this.getPos();
      if (var1.getItem().hasContainerItem()) {
         ItemStack var3 = new ItemStack(var1.getItem().getContainerItem());
         if (var1.stackSize <= 0) {
            var1 = var3;
         } else {
            InventoryHelper.spawnItemStack(this.world, (double)var5.getX(), (double)var5.getY(), (double)var5.getZ(), var3);
         }
      }

      if (var1.stackSize <= 0) {
         var1 = null;
      }

      this.brewingItemStacks[3] = var1;
      this.world.playEvent(1035, var5, 0);
   }

   public static void registerFixesBrewingStand(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Cauldron", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      NBTTagList var2 = var1.getTagList("Items", 10);
      this.brewingItemStacks = new ItemStack[this.getSizeInventory()];

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         byte var5 = var4.getByte("Slot");
         if (var5 >= 0 && var5 < this.brewingItemStacks.length) {
            this.brewingItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
         }
      }

      this.brewTime = var1.getShort("BrewTime");
      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

      this.fuel = var1.getByte("Fuel");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setShort("BrewTime", (short)this.brewTime);
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.brewingItemStacks.length; ++var3) {
         if (this.brewingItemStacks[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            this.brewingItemStacks[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      var1.setTag("Items", var2);
      if (this.hasCustomName()) {
         var1.setString("CustomName", this.customName);
      }

      var1.setByte("Fuel", (byte)this.fuel);
      return var1;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return var1 >= 0 && var1 < this.brewingItemStacks.length ? this.brewingItemStacks[var1] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return ItemStackHelper.getAndSplit(this.brewingItemStacks, var1, var2);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.brewingItemStacks, var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (var1 >= 0 && var1 < this.brewingItemStacks.length) {
         this.brewingItemStacks[var1] = var2;
      }

   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      if (var1 == 3) {
         return PotionHelper.isReagent(var2);
      } else {
         Item var3 = var2.getItem();
         return var1 == 4 ? var3 == Items.BLAZE_POWDER : var3 == Items.POTIONITEM || var3 == Items.SPLASH_POTION || var3 == Items.LINGERING_POTION || var3 == Items.GLASS_BOTTLE;
      }
   }

   public int[] getSlotsForFace(EnumFacing var1) {
      return var1 == EnumFacing.UP ? SLOTS_FOR_UP : (var1 == EnumFacing.DOWN ? SLOTS_FOR_DOWN : OUTPUT_SLOTS);
   }

   public boolean canInsertItem(int var1, ItemStack var2, EnumFacing var3) {
      return this.isItemValidForSlot(var1, var2);
   }

   public boolean canExtractItem(int var1, ItemStack var2, EnumFacing var3) {
      return var1 == 3 ? var2.getItem() == Items.GLASS_BOTTLE : true;
   }

   public String getGuiID() {
      return "minecraft:brewing_stand";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerBrewingStand(var1, this);
   }

   public int getField(int var1) {
      switch(var1) {
      case 0:
         return this.brewTime;
      case 1:
         return this.fuel;
      default:
         return 0;
      }
   }

   public void setField(int var1, int var2) {
      switch(var1) {
      case 0:
         this.brewTime = var2;
         break;
      case 1:
         this.fuel = var2;
      }

   }

   public int getFieldCount() {
      return 2;
   }

   public void clear() {
      Arrays.fill(this.brewingItemStacks, (Object)null);
   }
}
