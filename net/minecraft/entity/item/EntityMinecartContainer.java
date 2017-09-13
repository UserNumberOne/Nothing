package net.minecraft.entity.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
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
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;

public abstract class EntityMinecartContainer extends EntityMinecart implements ILockableContainer, ILootContainer {
   private ItemStack[] minecartContainerItems = new ItemStack[27];
   private boolean dropContentsWhenDead = true;
   private ResourceLocation lootTable;
   private long lootTableSeed;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.minecartContainerItems;
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

   public InventoryHolder getOwner() {
      Entity cart = this.getBukkitEntity();
      return cart instanceof InventoryHolder ? (InventoryHolder)cart : null;
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public Location getLocation() {
      return this.getBukkitEntity().getLocation();
   }

   public EntityMinecartContainer(World world) {
      super(world);
   }

   public EntityMinecartContainer(World world, double d0, double d1, double d2) {
      super(world, d0, d1, d2);
   }

   public void killMinecart(DamageSource damagesource) {
      super.killMinecart(damagesource);
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      this.addLoot((EntityPlayer)null);
      return this.minecartContainerItems[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      this.addLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.minecartContainerItems, i, j);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      this.addLoot((EntityPlayer)null);
      if (this.minecartContainerItems[i] != null) {
         ItemStack itemstack = this.minecartContainerItems[i];
         this.minecartContainerItems[i] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.addLoot((EntityPlayer)null);
      this.minecartContainerItems[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.isDead ? false : entityhuman.getDistanceSqToEntity(this) <= 64.0D;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   @Nullable
   public net.minecraft.entity.Entity changeDimension(int i) {
      this.dropContentsWhenDead = false;
      return super.changeDimension(i);
   }

   public void setDead() {
      if (this.dropContentsWhenDead) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

      super.setDead();
   }

   public void setDropItemsWhenDead(boolean flag) {
      this.dropContentsWhenDead = flag;
   }

   public static void registerFixesMinecartContainer(DataFixer dataconvertermanager, String s) {
      EntityMinecart.registerFixesMinecart(dataconvertermanager, s);
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(s, new String[]{"Items"}));
   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      if (this.lootTable != null) {
         nbttagcompound.setString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            nbttagcompound.setLong("LootTableSeed", this.lootTableSeed);
         }
      } else {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.minecartContainerItems.length; ++i) {
            if (this.minecartContainerItems[i] != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.setByte("Slot", (byte)i);
               this.minecartContainerItems[i].writeToNBT(nbttagcompound1);
               nbttaglist.appendTag(nbttagcompound1);
            }
         }

         nbttagcompound.setTag("Items", nbttaglist);
      }

   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.minecartContainerItems = new ItemStack[this.getSizeInventory()];
      if (nbttagcompound.hasKey("LootTable", 8)) {
         this.lootTable = new ResourceLocation(nbttagcompound.getString("LootTable"));
         this.lootTableSeed = nbttagcompound.getLong("LootTableSeed");
      } else {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;
            if (j >= 0 && j < this.minecartContainerItems.length) {
               this.minecartContainerItems[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
         }
      }

   }

   public boolean processInitialInteract(EntityPlayer entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (!this.world.isRemote) {
         entityhuman.displayGUIChest(this);
      }

      return true;
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

   public int getField(int i) {
      return 0;
   }

   public void setField(int i, int j) {
   }

   public int getFieldCount() {
      return 0;
   }

   public boolean isLocked() {
      return false;
   }

   public void setLockCode(LockCode chestlock) {
   }

   public LockCode getLockCode() {
      return LockCode.EMPTY_CODE;
   }

   public void addLoot(@Nullable EntityPlayer entityhuman) {
      if (this.lootTable != null) {
         LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
         this.lootTable = null;
         Random random;
         if (this.lootTableSeed == 0L) {
            random = new Random();
         } else {
            random = new Random(this.lootTableSeed);
         }

         LootContext.Builder loottableinfo_a = new LootContext.Builder((WorldServer)this.world);
         if (entityhuman != null) {
            loottableinfo_a.withLuck(entityhuman.getLuck());
         }

         loottable.fillInventory(this, random, loottableinfo_a.build());
      }

   }

   public void clear() {
      this.addLoot((EntityPlayer)null);

      for(int i = 0; i < this.minecartContainerItems.length; ++i) {
         this.minecartContainerItems[i] = null;
      }

   }

   public void setLootTable(ResourceLocation minecraftkey, long i) {
      this.lootTable = minecraftkey;
      this.lootTableSeed = i;
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }
}
