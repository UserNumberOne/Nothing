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

   public int getSizeInventory() {
      return this.furnaceItemStacks.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return this.furnaceItemStacks[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return ItemStackHelper.getAndSplit(this.furnaceItemStacks, var1, var2);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.furnaceItemStacks, var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      boolean var3 = var2 != null && var2.isItemEqual(this.furnaceItemStacks[var1]) && ItemStack.areItemStackTagsEqual(var2, this.furnaceItemStacks[var1]);
      this.furnaceItemStacks[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

      if (var1 == 0 && !var3) {
         this.totalCookTime = this.getCookTime(var2);
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
      this.furnaceCustomName = var1;
   }

   public static void registerFixesFurnace(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Furnace", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      NBTTagList var2 = var1.getTagList("Items", 10);
      this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         byte var5 = var4.getByte("Slot");
         if (var5 >= 0 && var5 < this.furnaceItemStacks.length) {
            this.furnaceItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
         }
      }

      this.furnaceBurnTime = var1.getShort("BurnTime");
      this.cookTime = var1.getShort("CookTime");
      this.totalCookTime = var1.getShort("CookTimeTotal");
      this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
      if (var1.hasKey("CustomName", 8)) {
         this.furnaceCustomName = var1.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setShort("BurnTime", (short)this.furnaceBurnTime);
      var1.setShort("CookTime", (short)this.cookTime);
      var1.setShort("CookTimeTotal", (short)this.totalCookTime);
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.furnaceItemStacks.length; ++var3) {
         if (this.furnaceItemStacks[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            this.furnaceItemStacks[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      var1.setTag("Items", var2);
      if (this.hasCustomName()) {
         var1.setString("CustomName", this.furnaceCustomName);
      }

      return var1;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   public void update() {
      boolean var1 = this.getBlockType() == Blocks.LIT_FURNACE;
      boolean var2 = false;
      int var3 = MinecraftServer.currentTick - this.lastTick;
      this.lastTick = MinecraftServer.currentTick;
      if (this.isBurning() && this.canSmelt()) {
         this.cookTime += var3;
         if (this.cookTime >= this.totalCookTime) {
            this.cookTime = 0;
            this.totalCookTime = this.getCookTime(this.furnaceItemStacks[0]);
            this.smeltItem();
            var2 = true;
         }
      } else {
         this.cookTime = 0;
      }

      if (this.isBurning()) {
         this.furnaceBurnTime -= var3;
      }

      if (!this.world.isRemote) {
         if (!this.isBurning() && (this.furnaceItemStacks[1] == null || this.furnaceItemStacks[0] == null)) {
            if (!this.isBurning() && this.cookTime > 0) {
               this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
            }
         } else if (this.furnaceBurnTime <= 0 && this.canSmelt()) {
            CraftItemStack var4 = CraftItemStack.asCraftMirror(this.furnaceItemStacks[1]);
            FurnaceBurnEvent var5 = new FurnaceBurnEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), var4, getItemBurnTime(this.furnaceItemStacks[1]));
            this.world.getServer().getPluginManager().callEvent(var5);
            if (var5.isCancelled()) {
               return;
            }

            this.currentItemBurnTime = var5.getBurnTime();
            this.furnaceBurnTime += this.currentItemBurnTime;
            if (this.furnaceBurnTime > 0 && var5.isBurning()) {
               var2 = true;
               if (this.furnaceItemStacks[1] != null) {
                  --this.furnaceItemStacks[1].stackSize;
                  if (this.furnaceItemStacks[1].stackSize == 0) {
                     Item var6 = this.furnaceItemStacks[1].getItem().getContainerItem();
                     this.furnaceItemStacks[1] = var6 != null ? new ItemStack(var6) : null;
                  }
               }
            }
         }

         if (var1 != this.isBurning()) {
            var2 = true;
            BlockFurnace.setState(this.isBurning(), this.world, this.pos);
            this.updateContainingBlockInfo();
         }
      }

      if (var2) {
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
         ItemStack var1 = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         return var1 == null ? false : (this.furnaceItemStacks[2] == null ? true : (!this.furnaceItemStacks[2].isItemEqual(var1) ? false : (this.furnaceItemStacks[2].stackSize + var1.stackSize <= this.getInventoryStackLimit() && this.furnaceItemStacks[2].stackSize < this.furnaceItemStacks[2].getMaxStackSize() ? true : this.furnaceItemStacks[2].stackSize + var1.stackSize <= var1.getMaxStackSize())));
      }
   }

   public void smeltItem() {
      if (this.canSmelt()) {
         ItemStack var1 = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         CraftItemStack var2 = CraftItemStack.asCraftMirror(this.furnaceItemStacks[0]);
         org.bukkit.inventory.ItemStack var3 = CraftItemStack.asBukkitCopy(var1);
         FurnaceSmeltEvent var4 = new FurnaceSmeltEvent(this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()), var2, var3);
         this.world.getServer().getPluginManager().callEvent(var4);
         if (var4.isCancelled()) {
            return;
         }

         var3 = var4.getResult();
         var1 = CraftItemStack.asNMSCopy(var3);
         if (var1 != null) {
            if (this.furnaceItemStacks[2] == null) {
               this.furnaceItemStacks[2] = var1;
            } else {
               if (!CraftItemStack.asCraftMirror(this.furnaceItemStacks[2]).isSimilar(var3)) {
                  return;
               }

               this.furnaceItemStacks[2].stackSize += var1.stackSize;
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

   public static int getItemBurnTime(ItemStack var0) {
      if (var0 == null) {
         return 0;
      } else {
         Item var1 = var0.getItem();
         if (var1 instanceof ItemBlock && Block.getBlockFromItem(var1) != Blocks.AIR) {
            Block var2 = Block.getBlockFromItem(var1);
            if (var2 == Blocks.WOODEN_SLAB) {
               return 150;
            }

            if (var2.getDefaultState().getMaterial() == Material.WOOD) {
               return 300;
            }

            if (var2 == Blocks.COAL_BLOCK) {
               return 16000;
            }
         }

         return var1 instanceof ItemTool && "WOOD".equals(((ItemTool)var1).getToolMaterialName()) ? 200 : (var1 instanceof ItemSword && "WOOD".equals(((ItemSword)var1).getToolMaterialName()) ? 200 : (var1 instanceof ItemHoe && "WOOD".equals(((ItemHoe)var1).getMaterialName()) ? 200 : (var1 == Items.STICK ? 100 : (var1 == Items.COAL ? 1600 : (var1 == Items.LAVA_BUCKET ? 20000 : (var1 == Item.getItemFromBlock(Blocks.SAPLING) ? 100 : (var1 == Items.BLAZE_ROD ? 2400 : 0)))))));
      }
   }

   public static boolean isItemFuel(ItemStack var0) {
      return getItemBurnTime(var0) > 0;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      if (var1 == 2) {
         return false;
      } else if (var1 != 1) {
         return true;
      } else {
         ItemStack var3 = this.furnaceItemStacks[1];
         return isItemFuel(var2) || SlotFurnaceFuel.isBucket(var2) && (var3 == null || var3.getItem() != Items.BUCKET);
      }
   }

   public int[] getSlotsForFace(EnumFacing var1) {
      return var1 == EnumFacing.DOWN ? SLOTS_BOTTOM : (var1 == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
   }

   public boolean canInsertItem(int var1, ItemStack var2, EnumFacing var3) {
      return this.isItemValidForSlot(var1, var2);
   }

   public boolean canExtractItem(int var1, ItemStack var2, EnumFacing var3) {
      if (var3 == EnumFacing.DOWN && var1 == 1) {
         Item var4 = var2.getItem();
         if (var4 != Items.WATER_BUCKET && var4 != Items.BUCKET) {
            return false;
         }
      }

      return true;
   }

   public String getGuiID() {
      return "minecraft:furnace";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerFurnace(var1, this);
   }

   public int getField(int var1) {
      switch(var1) {
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
      switch(var1) {
      case 0:
         this.furnaceBurnTime = var2;
         break;
      case 1:
         this.currentItemBurnTime = var2;
         break;
      case 2:
         this.cookTime = var2;
         break;
      case 3:
         this.totalCookTime = var2;
      }

   }

   public int getFieldCount() {
      return 4;
   }

   public void clear() {
      for(int var1 = 0; var1 < this.furnaceItemStacks.length; ++var1) {
         this.furnaceItemStacks[var1] = null;
      }

   }
}
