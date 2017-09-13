package net.minecraft.inventory;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerRepair extends Container {
   private static final Logger LOGGER = LogManager.getLogger();
   private final IInventory outputSlot = new InventoryCraftResult();
   private final IInventory inputSlots = new InventoryBasic("Repair", true, 2) {
      public void markDirty() {
         super.markDirty();
         ContainerRepair.this.onCraftMatrixChanged(this);
      }
   };
   private final World world;
   private final BlockPos selfPosition;
   public int maximumCost;
   private int materialCost;
   private String repairedItemName;
   private final EntityPlayer player;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerRepair(InventoryPlayer playerinventory, final World world, final BlockPos blockposition, EntityPlayer entityhuman) {
      this.player = playerinventory;
      this.selfPosition = blockposition;
      this.world = world;
      this.player = entityhuman;
      this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
      this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
      this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return false;
         }

         public boolean canTakeStack(EntityPlayer entityhuman) {
            return (entityhuman.capabilities.isCreativeMode || entityhuman.experienceLevel >= ContainerRepair.this.maximumCost) && ContainerRepair.this.maximumCost > 0 && this.getHasStack();
         }

         public void onPickupFromSlot(EntityPlayer entityhuman, ItemStack itemstack) {
            if (!entityhuman.capabilities.isCreativeMode) {
               entityhuman.addExperienceLevel(-ContainerRepair.this.maximumCost);
            }

            ContainerRepair.this.inputSlots.setInventorySlotContents(0, (ItemStack)null);
            if (ContainerRepair.this.materialCost > 0) {
               ItemStack itemstack1 = ContainerRepair.this.inputSlots.getStackInSlot(1);
               if (itemstack1 != null && itemstack1.stackSize > ContainerRepair.this.materialCost) {
                  itemstack1.stackSize -= ContainerRepair.this.materialCost;
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, itemstack1);
               } else {
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
               }
            } else {
               ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
            }

            ContainerRepair.this.maximumCost = 0;
            IBlockState iblockdata = world.getBlockState(blockposition);
            if (!entityhuman.capabilities.isCreativeMode && !world.isRemote && iblockdata.getBlock() == Blocks.ANVIL && entityhuman.getRNG().nextFloat() < 0.12F) {
               int i = ((Integer)iblockdata.getValue(BlockAnvil.DAMAGE)).intValue();
               ++i;
               if (i > 2) {
                  world.setBlockToAir(blockposition);
                  world.playEvent(1029, blockposition, 0);
               } else {
                  world.setBlockState(blockposition, iblockdata.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(i)), 2);
                  world.playEvent(1030, blockposition, 0);
               }
            } else if (!world.isRemote) {
               world.playEvent(1030, blockposition, 0);
            }

         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(playerinventory, var7, 8 + var7 * 18, 142));
      }

   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      super.onCraftMatrixChanged(iinventory);
      if (iinventory == this.inputSlots) {
         this.updateRepairOutput();
      }

   }

   public void updateRepairOutput() {
      ItemStack itemstack = this.inputSlots.getStackInSlot(0);
      this.maximumCost = 1;
      int i = 0;
      byte b0 = 0;
      byte b1 = 0;
      if (itemstack == null) {
         CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
         this.maximumCost = 0;
      } else {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
         Map map = EnchantmentHelper.getEnchantments(itemstack1);
         int j = b0 + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
         this.materialCost = 0;
         if (itemstack2 != null) {
            boolean flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.getEnchantments(itemstack2).hasNoTags();
            if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
               int k = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
               if (k <= 0) {
                  CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               int l;
               for(l = 0; k > 0 && l < itemstack2.stackSize; ++l) {
                  int i1 = itemstack1.getItemDamage() - k;
                  itemstack1.setItemDamage(i1);
                  ++i;
                  k = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
               }

               this.materialCost = l;
            } else {
               if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                  CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               if (itemstack1.isItemStackDamageable() && !flag) {
                  int k = itemstack.getMaxDamage() - itemstack.getItemDamage();
                  int l = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                  int i1 = l + itemstack1.getMaxDamage() * 12 / 100;
                  int j1 = k + i1;
                  int k1 = itemstack1.getMaxDamage() - j1;
                  if (k1 < 0) {
                     k1 = 0;
                  }

                  if (k1 < itemstack1.getMetadata()) {
                     itemstack1.setItemDamage(k1);
                     i += 2;
                  }
               }

               Map map1 = EnchantmentHelper.getEnchantments(itemstack2);

               for(Enchantment enchantment : map1.keySet()) {
                  if (enchantment != null) {
                     int j1 = map.containsKey(enchantment) ? ((Integer)map.get(enchantment)).intValue() : 0;
                     int k1 = ((Integer)map1.get(enchantment)).intValue();
                     k1 = j1 == k1 ? k1 + 1 : Math.max(k1, j1);
                     boolean flag1 = enchantment.canApply(itemstack);
                     if (this.player.capabilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                        flag1 = true;
                     }

                     for(Enchantment enchantment1 : map.keySet()) {
                        if (enchantment1 != enchantment && !enchantment.canApplyTogether(enchantment1)) {
                           flag1 = false;
                           ++i;
                        }
                     }

                     if (flag1) {
                        if (k1 > enchantment.getMaxLevel()) {
                           k1 = enchantment.getMaxLevel();
                        }

                        map.put(enchantment, Integer.valueOf(k1));
                        int l1 = 0;
                        switch(ContainerRepair.SyntheticClass_1.a[enchantment.getRarity().ordinal()]) {
                        case 1:
                           l1 = 1;
                           break;
                        case 2:
                           l1 = 2;
                           break;
                        case 3:
                           l1 = 4;
                           break;
                        case 4:
                           l1 = 8;
                        }

                        if (flag) {
                           l1 = Math.max(1, l1 / 2);
                        }

                        i += l1 * k1;
                     }
                  }
               }
            }
         }

         if (StringUtils.isBlank(this.repairedItemName)) {
            if (itemstack.hasDisplayName()) {
               b1 = 1;
               i += b1;
               itemstack1.clearCustomName();
            }
         } else if (!this.repairedItemName.equals(itemstack.getDisplayName())) {
            b1 = 1;
            i += b1;
            itemstack1.setStackDisplayName(this.repairedItemName);
         }

         this.maximumCost = j + i;
         if (i <= 0) {
            itemstack1 = null;
         }

         if (b1 == i && b1 > 0 && this.maximumCost >= 40) {
            this.maximumCost = 39;
         }

         if (this.maximumCost >= 40 && !this.player.capabilities.isCreativeMode) {
            itemstack1 = null;
         }

         if (itemstack1 != null) {
            int i2 = itemstack1.getRepairCost();
            if (itemstack2 != null && i2 < itemstack2.getRepairCost()) {
               i2 = itemstack2.getRepairCost();
            }

            if (b1 != i || b1 == 0) {
               i2 = i2 * 2 + 1;
            }

            itemstack1.setRepairCost(i2);
            EnchantmentHelper.setEnchantments(map, itemstack1);
         }

         CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), itemstack1);
         this.detectAndSendChanges();
      }

   }

   public void addListener(IContainerListener icrafting) {
      super.addListener(icrafting);
      icrafting.sendProgressBarUpdate(this, 0, this.maximumCost);
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      if (!this.world.isRemote) {
         for(int i = 0; i < this.inputSlots.getSizeInventory(); ++i) {
            ItemStack itemstack = this.inputSlots.removeStackFromSlot(i);
            if (itemstack != null) {
               entityhuman.dropItem(itemstack, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      if (!this.checkReachable) {
         return true;
      } else {
         return this.world.getBlockState(this.selfPosition).getBlock() != Blocks.ANVIL ? false : entityhuman.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
      }
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i == 2) {
            if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (i != 0 && i != 1) {
            if (i >= 3 && i < 39 && !this.mergeItemStack(itemstack1, 0, 2, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
            return null;
         }

         if (itemstack1.stackSize == 0) {
            slot.putStack((ItemStack)null);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.stackSize == itemstack.stackSize) {
            return null;
         }

         slot.onPickupFromSlot(entityhuman, itemstack1);
      }

      return itemstack;
   }

   public void updateItemName(String s) {
      this.repairedItemName = s;
      if (this.getSlot(2).getHasStack()) {
         ItemStack itemstack = this.getSlot(2).getStack();
         if (StringUtils.isBlank(s)) {
            itemstack.clearCustomName();
         } else {
            itemstack.setStackDisplayName(this.repairedItemName);
         }
      }

      this.updateRepairOutput();
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory inventory = new CraftInventoryAnvil(new Location(this.world.getWorld(), (double)this.selfPosition.getX(), (double)this.selfPosition.getY(), (double)this.selfPosition.getZ()), this.inputSlots, this.outputSlot);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[Enchantment.Rarity.values().length];

      static {
         try {
            a[Enchantment.Rarity.COMMON.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[Enchantment.Rarity.UNCOMMON.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[Enchantment.Rarity.RARE.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[Enchantment.Rarity.VERY_RARE.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
