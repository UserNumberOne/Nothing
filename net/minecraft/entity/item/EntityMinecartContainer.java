package net.minecraft.entity.item;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class EntityMinecartContainer extends EntityMinecart implements ILockableContainer, ILootContainer {
   private ItemStack[] minecartContainerItems = new ItemStack[36];
   public boolean dropContentsWhenDead = true;
   private ResourceLocation lootTable;
   private long lootTableSeed;
   public IItemHandler itemHandler = new InvWrapper(this);

   public EntityMinecartContainer(World worldIn) {
      super(worldIn);
   }

   public EntityMinecartContainer(World worldIn, double x, double y, double z) {
      super(worldIn, x, y, z);
   }

   public void killMinecart(DamageSource source) {
      super.killMinecart(source);
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      this.addLoot((EntityPlayer)null);
      return this.minecartContainerItems[index];
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      this.addLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.minecartContainerItems, index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      this.addLoot((EntityPlayer)null);
      if (this.minecartContainerItems[index] != null) {
         ItemStack itemstack = this.minecartContainerItems[index];
         this.minecartContainerItems[index] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      this.addLoot((EntityPlayer)null);
      this.minecartContainerItems[index] = stack;
      if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return this.isDead ? false : player.getDistanceSqToEntity(this) <= 64.0D;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   @Nullable
   public Entity changeDimension(int dimensionIn) {
      this.dropContentsWhenDead = false;
      return super.changeDimension(dimensionIn);
   }

   public void setDead() {
      if (this.dropContentsWhenDead) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

      super.setDead();
   }

   public void setDropItemsWhenDead(boolean dropWhenDead) {
      this.dropContentsWhenDead = dropWhenDead;
   }

   public static void registerFixesMinecartContainer(DataFixer fixer, String name) {
      EntityMinecart.registerFixesMinecart(fixer, name);
      fixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(name, new String[]{"Items"}));
   }

   protected void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      if (this.lootTable != null) {
         compound.setString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            compound.setLong("LootTableSeed", this.lootTableSeed);
         }
      } else {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.minecartContainerItems.length; ++i) {
            if (this.minecartContainerItems[i] != null) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               nbttagcompound.setByte("Slot", (byte)i);
               this.minecartContainerItems[i].writeToNBT(nbttagcompound);
               nbttaglist.appendTag(nbttagcompound);
            }
         }

         compound.setTag("Items", nbttaglist);
      }

   }

   protected void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      this.minecartContainerItems = new ItemStack[this.getSizeInventory()];
      if (compound.hasKey("LootTable", 8)) {
         this.lootTable = new ResourceLocation(compound.getString("LootTable"));
         this.lootTableSeed = compound.getLong("LootTableSeed");
      } else {
         NBTTagList nbttaglist = compound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if (j >= 0 && j < this.minecartContainerItems.length) {
               this.minecartContainerItems[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
         }
      }

   }

   public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
      if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player, stack, hand))) {
         return true;
      } else {
         if (!this.world.isRemote) {
            player.displayGUIChest(this);
         }

         return true;
      }
   }

   protected void applyDrag() {
      float f = 0.98F;
      if (this.lootTable == null) {
         int i = 15 - Container.calcRedstoneFromInventory(this);
         f += (float)i * 0.001F;
      }

      this.motionX *= (double)f;
      this.motionY *= 0.0D;
      this.motionZ *= (double)f;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public boolean isLocked() {
      return false;
   }

   public void setLockCode(LockCode code) {
   }

   public LockCode getLockCode() {
      return LockCode.EMPTY_CODE;
   }

   public void addLoot(@Nullable EntityPlayer player) {
      if (this.lootTable != null) {
         LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
         this.lootTable = null;
         Random random;
         if (this.lootTableSeed == 0L) {
            random = new Random();
         } else {
            random = new Random(this.lootTableSeed);
         }

         LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer)this.world);
         if (player != null) {
            lootcontext$builder.withLuck(player.getLuck());
         }

         loottable.fillInventory(this, random, lootcontext$builder.build());
      }

   }

   public Object getCapability(Capability capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.itemHandler : super.getCapability(capability, facing);
   }

   public boolean hasCapability(Capability capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }

   public void clear() {
      this.addLoot((EntityPlayer)null);

      for(int i = 0; i < this.minecartContainerItems.length; ++i) {
         this.minecartContainerItems[i] = null;
      }

   }

   public void setLootTable(ResourceLocation lootTableIn, long lootTableSeedIn) {
      this.lootTable = lootTableIn;
      this.lootTableSeed = lootTableSeedIn;
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }
}
