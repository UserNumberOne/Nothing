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

   public EntityMinecartContainer(World var1) {
      super(var1);
   }

   public EntityMinecartContainer(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public void killMinecart(DamageSource var1) {
      super.killMinecart(var1);
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      this.addLoot((EntityPlayer)null);
      return this.minecartContainerItems[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      this.addLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.minecartContainerItems, var1, var2);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      this.addLoot((EntityPlayer)null);
      if (this.minecartContainerItems[var1] != null) {
         ItemStack var2 = this.minecartContainerItems[var1];
         this.minecartContainerItems[var1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.addLoot((EntityPlayer)null);
      this.minecartContainerItems[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.isDead ? false : var1.getDistanceSqToEntity(this) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   @Nullable
   public Entity changeDimension(int var1) {
      this.dropContentsWhenDead = false;
      return super.changeDimension(var1);
   }

   public void setDead() {
      if (this.dropContentsWhenDead) {
         InventoryHelper.dropInventoryItems(this.world, this, this);
      }

      super.setDead();
   }

   public void setDropItemsWhenDead(boolean var1) {
      this.dropContentsWhenDead = var1;
   }

   public static void registerFixesMinecartContainer(DataFixer var0, String var1) {
      EntityMinecart.registerFixesMinecart(var0, var1);
      var0.registerWalker(FixTypes.ENTITY, new ItemStackDataLists(var1, new String[]{"Items"}));
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      if (this.lootTable != null) {
         var1.setString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            var1.setLong("LootTableSeed", this.lootTableSeed);
         }
      } else {
         NBTTagList var2 = new NBTTagList();

         for(int var3 = 0; var3 < this.minecartContainerItems.length; ++var3) {
            if (this.minecartContainerItems[var3] != null) {
               NBTTagCompound var4 = new NBTTagCompound();
               var4.setByte("Slot", (byte)var3);
               this.minecartContainerItems[var3].writeToNBT(var4);
               var2.appendTag(var4);
            }
         }

         var1.setTag("Items", var2);
      }

   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.minecartContainerItems = new ItemStack[this.getSizeInventory()];
      if (var1.hasKey("LootTable", 8)) {
         this.lootTable = new ResourceLocation(var1.getString("LootTable"));
         this.lootTableSeed = var1.getLong("LootTableSeed");
      } else {
         NBTTagList var2 = var1.getTagList("Items", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;
            if (var5 >= 0 && var5 < this.minecartContainerItems.length) {
               this.minecartContainerItems[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
         }
      }

   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, var1, var2, var3))) {
         return true;
      } else {
         if (!this.world.isRemote) {
            var1.displayGUIChest(this);
         }

         return true;
      }
   }

   protected void applyDrag() {
      float var1 = 0.98F;
      if (this.lootTable == null) {
         int var2 = 15 - Container.calcRedstoneFromInventory(this);
         var1 += (float)var2 * 0.001F;
      }

      this.motionX *= (double)var1;
      this.motionY *= 0.0D;
      this.motionZ *= (double)var1;
   }

   public int getField(int var1) {
      return 0;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public boolean isLocked() {
      return false;
   }

   public void setLockCode(LockCode var1) {
   }

   public LockCode getLockCode() {
      return LockCode.EMPTY_CODE;
   }

   public void addLoot(@Nullable EntityPlayer var1) {
      if (this.lootTable != null) {
         LootTable var2 = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
         this.lootTable = null;
         Random var3;
         if (this.lootTableSeed == 0L) {
            var3 = new Random();
         } else {
            var3 = new Random(this.lootTableSeed);
         }

         LootContext.Builder var4 = new LootContext.Builder((WorldServer)this.world);
         if (var1 != null) {
            var4.withLuck(var1.getLuck());
         }

         var2.fillInventory(this, var3, var4.build());
      }

   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.itemHandler : super.getCapability(var1, var2);
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return var1 == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(var1, var2);
   }

   public void clear() {
      this.addLoot((EntityPlayer)null);

      for(int var1 = 0; var1 < this.minecartContainerItems.length; ++var1) {
         this.minecartContainerItems[var1] = null;
      }

   }

   public void setLootTable(ResourceLocation var1, long var2) {
      this.lootTable = var1;
      this.lootTableSeed = var2;
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }
}
