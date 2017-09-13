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
      this(var1, var2, BlockPos.ORIGIN, var3);
   }

   public ContainerRepair(InventoryPlayer var1, final World var2, final BlockPos var3, EntityPlayer var4) {
      this.outputSlot = new InventoryCraftResult();
      this.inputSlots = new InventoryBasic("Repair", true, 2) {
         public void markDirty() {
            super.markDirty();
            ContainerRepair.this.onCraftMatrixChanged(this);
         }
      };
      this.selfPosition = var3;
      this.world = var2;
      this.player = var4;
      this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
      this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47));
      this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return false;
         }

         public boolean canTakeStack(EntityPlayer var1) {
            return (var1.capabilities.isCreativeMode || var1.experienceLevel >= ContainerRepair.this.maximumCost) && ContainerRepair.this.maximumCost > 0 && this.getHasStack();
         }

         public void onPickupFromSlot(EntityPlayer var1, ItemStack var2x) {
            if (!var1.capabilities.isCreativeMode) {
               var1.addExperienceLevel(-ContainerRepair.this.maximumCost);
            }

            float var3x = ForgeHooks.onAnvilRepair(var1, var2x, ContainerRepair.this.inputSlots.getStackInSlot(0), ContainerRepair.this.inputSlots.getStackInSlot(1));
            ContainerRepair.this.inputSlots.setInventorySlotContents(0, (ItemStack)null);
            if (ContainerRepair.this.materialCost > 0) {
               ItemStack var4 = ContainerRepair.this.inputSlots.getStackInSlot(1);
               if (var4 != null && var4.stackSize > ContainerRepair.this.materialCost) {
                  var4.stackSize -= ContainerRepair.this.materialCost;
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, var4);
               } else {
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
               }
            } else {
               ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
            }

            ContainerRepair.this.maximumCost = 0;
            IBlockState var6 = var2.getBlockState(var3);
            if (!var1.capabilities.isCreativeMode && !var2.isRemote && var6.getBlock() == Blocks.ANVIL && var1.getRNG().nextFloat() < var3x) {
               int var5 = ((Integer)var6.getValue(BlockAnvil.DAMAGE)).intValue();
               ++var5;
               if (var5 > 2) {
                  var2.setBlockToAir(var3);
                  var2.playEvent(1029, var3, 0);
               } else {
                  var2.setBlockState(var3, var6.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(var5)), 2);
                  var2.playEvent(1030, var3, 0);
               }
            } else if (!var2.isRemote) {
               var2.playEvent(1030, var3, 0);
            }

         }
      });

      for(int var5 = 0; var5 < 3; ++var5) {
         for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlotToContainer(new Slot(var1, var6 + var5 * 9 + 9, 8 + var6 * 18, 84 + var5 * 18));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(var1, var7, 8 + var7 * 18, 142));
      }

   }

   public void onCraftMatrixChanged(IInventory var1) {
      super.onCraftMatrixChanged(var1);
      if (var1 == this.inputSlots) {
         this.updateRepairOutput();
      }

   }

   public void updateRepairOutput() {
      ItemStack var1 = this.inputSlots.getStackInSlot(0);
      this.maximumCost = 1;
      int var2 = 0;
      int var3 = 0;
      byte var4 = 0;
      if (var1 == null) {
         this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
         this.maximumCost = 0;
      } else {
         ItemStack var5 = var1.copy();
         ItemStack var6 = this.inputSlots.getStackInSlot(1);
         Map var7 = EnchantmentHelper.getEnchantments(var5);
         var3 = var3 + var1.getRepairCost() + (var6 == null ? 0 : var6.getRepairCost());
         this.materialCost = 0;
         boolean var8 = false;
         if (var6 != null) {
            if (!ForgeHooks.onAnvilChange(this, var1, var6, this.outputSlot, this.repairedItemName, var3)) {
               return;
            }

            var8 = var6.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.getEnchantments(var6).hasNoTags();
            if (var5.isItemStackDamageable() && var5.getItem().getIsRepairable(var1, var6)) {
               int var19 = Math.min(var5.getItemDamage(), var5.getMaxDamage() / 4);
               if (var19 <= 0) {
                  this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               int var22;
               for(var22 = 0; var19 > 0 && var22 < var6.stackSize; ++var22) {
                  int var24 = var5.getItemDamage() - var19;
                  var5.setItemDamage(var24);
                  ++var2;
                  var19 = Math.min(var5.getItemDamage(), var5.getMaxDamage() / 4);
               }

               this.materialCost = var22;
            } else {
               if (!var8 && (var5.getItem() != var6.getItem() || !var5.isItemStackDamageable())) {
                  this.outputSlot.setInventorySlotContents(0, (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               if (var5.isItemStackDamageable() && !var8) {
                  int var9 = var1.getMaxDamage() - var1.getItemDamage();
                  int var10 = var6.getMaxDamage() - var6.getItemDamage();
                  int var11 = var10 + var5.getMaxDamage() * 12 / 100;
                  int var12 = var9 + var11;
                  int var13 = var5.getMaxDamage() - var12;
                  if (var13 < 0) {
                     var13 = 0;
                  }

                  if (var13 < var5.getMetadata()) {
                     var5.setItemDamage(var13);
                     var2 += 2;
                  }
               }

               Map var18 = EnchantmentHelper.getEnchantments(var6);

               for(Enchantment var23 : var18.keySet()) {
                  if (var23 != null) {
                     int var25 = var7.containsKey(var23) ? ((Integer)var7.get(var23)).intValue() : 0;
                     int var26 = ((Integer)var18.get(var23)).intValue();
                     var26 = var25 == var26 ? var26 + 1 : Math.max(var26, var25);
                     boolean var14 = var23.canApply(var1);
                     if (this.player.capabilities.isCreativeMode || var1.getItem() == Items.ENCHANTED_BOOK) {
                        var14 = true;
                     }

                     for(Enchantment var16 : var7.keySet()) {
                        if (var16 != var23 && (!var23.canApplyTogether(var16) || !var16.canApplyTogether(var23))) {
                           var14 = false;
                           ++var2;
                        }
                     }

                     if (var14) {
                        if (var26 > var23.getMaxLevel()) {
                           var26 = var23.getMaxLevel();
                        }

                        var7.put(var23, Integer.valueOf(var26));
                        int var28 = 0;
                        switch(var23.getRarity()) {
                        case COMMON:
                           var28 = 1;
                           break;
                        case UNCOMMON:
                           var28 = 2;
                           break;
                        case RARE:
                           var28 = 4;
                           break;
                        case VERY_RARE:
                           var28 = 8;
                        }

                        if (var8) {
                           var28 = Math.max(1, var28 / 2);
                        }

                        var2 += var28 * var26;
                     }
                  }
               }
            }
         }

         if (var8 && !var5.getItem().isBookEnchantable(var5, var6)) {
            var5 = null;
         }

         if (StringUtils.isBlank(this.repairedItemName)) {
            if (var1.hasDisplayName()) {
               var4 = 1;
               var2 += var4;
               var5.clearCustomName();
            }
         } else if (!this.repairedItemName.equals(var1.getDisplayName())) {
            var4 = 1;
            var2 += var4;
            var5.setStackDisplayName(this.repairedItemName);
         }

         this.maximumCost = var3 + var2;
         if (var2 <= 0) {
            var5 = null;
         }

         if (var4 == var2 && var4 > 0 && this.maximumCost >= 40) {
            this.maximumCost = 39;
         }

         if (this.maximumCost >= 40 && !this.player.capabilities.isCreativeMode) {
            var5 = null;
         }

         if (var5 != null) {
            int var20 = var5.getRepairCost();
            if (var6 != null && var20 < var6.getRepairCost()) {
               var20 = var6.getRepairCost();
            }

            if (var4 != var2 || var4 == 0) {
               var20 = var20 * 2 + 1;
            }

            var5.setRepairCost(var20);
            EnchantmentHelper.setEnchantments(var7, var5);
         }

         this.outputSlot.setInventorySlotContents(0, var5);
         this.detectAndSendChanges();
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      var1.sendProgressBarUpdate(this, 0, this.maximumCost);
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
      if (var1 == 0) {
         this.maximumCost = var2;
      }

   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      if (!this.world.isRemote) {
         for(int var2 = 0; var2 < this.inputSlots.getSizeInventory(); ++var2) {
            ItemStack var3 = this.inputSlots.removeStackFromSlot(var2);
            if (var3 != null) {
               var1.dropItem(var3, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.world.getBlockState(this.selfPosition).getBlock() != Blocks.ANVIL ? false : var1.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 == 2) {
            if (!this.mergeItemStack(var5, 3, 39, true)) {
               return null;
            }

            var4.onSlotChange(var5, var3);
         } else if (var2 != 0 && var2 != 1) {
            if (var2 >= 3 && var2 < 39 && !this.mergeItemStack(var5, 0, 2, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 3, 39, false)) {
            return null;
         }

         if (var5.stackSize == 0) {
            var4.putStack((ItemStack)null);
         } else {
            var4.onSlotChanged();
         }

         if (var5.stackSize == var3.stackSize) {
            return null;
         }

         var4.onPickupFromSlot(var1, var5);
      }

      return var3;
   }

   public void updateItemName(String var1) {
      this.repairedItemName = var1;
      if (this.getSlot(2).getHasStack()) {
         ItemStack var2 = this.getSlot(2).getStack();
         if (StringUtils.isBlank(var1)) {
            var2.clearCustomName();
         } else {
            var2.setStackDisplayName(this.repairedItemName);
         }
      }

      this.updateRepairOutput();
   }
}
