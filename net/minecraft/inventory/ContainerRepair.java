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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContainerRepair extends Container {
   private static final Logger LOGGER = LogManager.getLogger();
   private final IInventory outputSlot;
   private final IInventory inputSlots;
   private final World world;
   private final BlockPos selfPosition;
   public int maximumCost;
   public int materialCost;
   private String repairedItemName;
   private final EntityPlayer player;

   @SideOnly(Side.CLIENT)
   public ContainerRepair(InventoryPlayer var1, World var2, EntityPlayer var3) {
      this(playerInventory, worldIn, BlockPos.ORIGIN, player);
   }

   public ContainerRepair(InventoryPlayer var1, final World var2, final BlockPos var3, EntityPlayer var4) {
      this.outputSlot = new InventoryCraftResult();
      this.inputSlots = new InventoryBasic("Repair", true, 2) {
         public void markDirty() {
            super.markDirty();
            ContainerRepair.this.onCraftMatrixChanged(this);
         }
      };
      this.selfPosition = blockPosIn;
      this.world = worldIn;
      this.player = player;
      this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
      this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
      this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return false;
         }

         public boolean canTakeStack(EntityPlayer var1) {
            return (playerIn.capabilities.isCreativeMode || playerIn.experienceLevel >= ContainerRepair.this.maximumCost) && ContainerRepair.this.maximumCost > 0 && this.getHasStack();
         }

         public void onPickupFromSlot(EntityPlayer var1, ItemStack var2x) {
            if (!playerIn.capabilities.isCreativeMode) {
               playerIn.addExperienceLevel(-ContainerRepair.this.maximumCost);
            }

            float breakChance = ForgeHooks.onAnvilRepair(playerIn, stack, ContainerRepair.this.inputSlots.getStackInSlot(0), ContainerRepair.this.inputSlots.getStackInSlot(1));
            ContainerRepair.this.inputSlots.setInventorySlotContents(0, (ItemStack)null);
            if (ContainerRepair.this.materialCost > 0) {
               ItemStack itemstack = ContainerRepair.this.inputSlots.getStackInSlot(1);
               if (itemstack != null && itemstack.stackSize > ContainerRepair.this.materialCost) {
                  itemstack.stackSize -= ContainerRepair.this.materialCost;
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, itemstack);
               } else {
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
               }
            } else {
               ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
            }

            ContainerRepair.this.maximumCost = 0;
            IBlockState iblockstate = worldIn.getBlockState(blockPosIn);
            if (!playerIn.capabilities.isCreativeMode && !worldIn.isRemote && iblockstate.getBlock() == Blocks.ANVIL && playerIn.getRNG().nextFloat() < breakChance) {
               int l = ((Integer)iblockstate.getValue(BlockAnvil.DAMAGE)).intValue();
               ++l;
               if (l > 2) {
                  worldIn.setBlockToAir(blockPosIn);
                  worldIn.playEvent(1029, blockPosIn, 0);
               } else {
                  worldIn.setBlockState(blockPosIn, iblockstate.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(l)), 2);
                  worldIn.playEvent(1030, blockPosIn, 0);
               }
            } else if (!worldIn.isRemote) {
               worldIn.playEvent(1030, blockPosIn, 0);
            }

         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
      }

   }

   public void onCraftMatrixChanged(IInventory var1) {
      super.onCraftMatrixChanged(inventoryIn);
      if (inventoryIn == this.inputSlots) {
         this.updateRepairOutput();
      }

   }

   public void updateRepairOutput() {
      ItemStack itemstack = this.inputSlots.getStackInSlot(0);
      this.maximumCost = 1;
      int i = 0;
      int j = 0;
      int k = 0;
      if (itemstack == null) {
         this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
         this.maximumCost = 0;
      } else {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
         Map map = EnchantmentHelper.getEnchantments(itemstack1);
         j = j + itemstack.getRepairCost() + (itemstack2 == null ? 0 : itemstack2.getRepairCost());
         this.materialCost = 0;
         boolean flag = false;
         if (itemstack2 != null) {
            if (!ForgeHooks.onAnvilChange(this, itemstack, itemstack2, this.outputSlot, this.repairedItemName, j)) {
               return;
            }

            flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.getEnchantments(itemstack2).hasNoTags();
            if (itemstack1.isItemStackDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
               int j2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
               if (j2 <= 0) {
                  this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               int k2;
               for(k2 = 0; j2 > 0 && k2 < itemstack2.stackSize; ++k2) {
                  int l2 = itemstack1.getItemDamage() - j2;
                  itemstack1.setItemDamage(l2);
                  ++i;
                  j2 = Math.min(itemstack1.getItemDamage(), itemstack1.getMaxDamage() / 4);
               }

               this.materialCost = k2;
            } else {
               if (!flag && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isItemStackDamageable())) {
                  this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               if (itemstack1.isItemStackDamageable() && !flag) {
                  int l = itemstack.getMaxDamage() - itemstack.getItemDamage();
                  int i1 = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
                  int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                  int k1 = l + j1;
                  int l1 = itemstack1.getMaxDamage() - k1;
                  if (l1 < 0) {
                     l1 = 0;
                  }

                  if (l1 < itemstack1.getMetadata()) {
                     itemstack1.setItemDamage(l1);
                     i += 2;
                  }
               }

               Map map1 = EnchantmentHelper.getEnchantments(itemstack2);

               for(Enchantment enchantment1 : map1.keySet()) {
                  if (enchantment1 != null) {
                     int i3 = map.containsKey(enchantment1) ? ((Integer)map.get(enchantment1)).intValue() : 0;
                     int j3 = ((Integer)map1.get(enchantment1)).intValue();
                     j3 = i3 == j3 ? j3 + 1 : Math.max(j3, i3);
                     boolean flag1 = enchantment1.canApply(itemstack);
                     if (this.player.capabilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                        flag1 = true;
                     }

                     for(Enchantment enchantment : map.keySet()) {
                        if (enchantment != enchantment1 && (!enchantment1.canApplyTogether(enchantment) || !enchantment.canApplyTogether(enchantment1))) {
                           flag1 = false;
                           ++i;
                        }
                     }

                     if (flag1) {
                        if (j3 > enchantment1.getMaxLevel()) {
                           j3 = enchantment1.getMaxLevel();
                        }

                        map.put(enchantment1, Integer.valueOf(j3));
                        int k3 = 0;
                        switch(enchantment1.getRarity()) {
                        case COMMON:
                           k3 = 1;
                           break;
                        case UNCOMMON:
                           k3 = 2;
                           break;
                        case RARE:
                           k3 = 4;
                           break;
                        case VERY_RARE:
                           k3 = 8;
                        }

                        if (flag) {
                           k3 = Math.max(1, k3 / 2);
                        }

                        i += k3 * j3;
                     }
                  }
               }
            }
         }

         if (flag && !itemstack1.getItem().isBookEnchantable(itemstack1, itemstack2)) {
            itemstack1 = null;
         }

         if (StringUtils.isBlank(this.repairedItemName)) {
            if (itemstack.hasDisplayName()) {
               k = 1;
               i += k;
               itemstack1.clearCustomName();
            }
         } else if (!this.repairedItemName.equals(itemstack.getDisplayName())) {
            k = 1;
            i += k;
            itemstack1.setStackDisplayName(this.repairedItemName);
         }

         this.maximumCost = j + i;
         if (i <= 0) {
            itemstack1 = null;
         }

         if (k == i && k > 0 && this.maximumCost >= 40) {
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

            if (k != i || k == 0) {
               i2 = i2 * 2 + 1;
            }

            itemstack1.setRepairCost(i2);
            EnchantmentHelper.setEnchantments(map, itemstack1);
         }

         this.outputSlot.setInventorySlotContents(0, itemstack1);
         this.detectAndSendChanges();
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(listener);
      listener.sendProgressBarUpdate(this, 0, this.maximumCost);
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
      if (id == 0) {
         this.maximumCost = data;
      }

   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(playerIn);
      if (!this.world.isRemote) {
         for(int i = 0; i < this.inputSlots.getSizeInventory(); ++i) {
            ItemStack itemstack = this.inputSlots.removeStackFromSlot(i);
            if (itemstack != null) {
               playerIn.dropItem(itemstack, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.world.getBlockState(this.selfPosition).getBlock() != Blocks.ANVIL ? false : playerIn.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (index == 2) {
            if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 39 && !this.mergeItemStack(itemstack1, 0, 2, false)) {
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

         slot.onPickupFromSlot(playerIn, itemstack1);
      }

      return itemstack;
   }

   public void updateItemName(String var1) {
      this.repairedItemName = newName;
      if (this.getSlot(2).getHasStack()) {
         ItemStack itemstack = this.getSlot(2).getStack();
         if (StringUtils.isBlank(newName)) {
            itemstack.clearCustomName();
         } else {
            itemstack.setStackDisplayName(this.repairedItemName);
         }
      }

      this.updateRepairOutput();
   }
}
