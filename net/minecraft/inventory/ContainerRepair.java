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

   public ContainerRepair(InventoryPlayer var1, final World var2, final BlockPos var3, EntityPlayer var4) {
      this.player = var1;
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

            ContainerRepair.this.inputSlots.setInventorySlotContents(0, (ItemStack)null);
            if (ContainerRepair.this.materialCost > 0) {
               ItemStack var3x = ContainerRepair.this.inputSlots.getStackInSlot(1);
               if (var3x != null && var3x.stackSize > ContainerRepair.this.materialCost) {
                  var3x.stackSize -= ContainerRepair.this.materialCost;
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, var3x);
               } else {
                  ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
               }
            } else {
               ContainerRepair.this.inputSlots.setInventorySlotContents(1, (ItemStack)null);
            }

            ContainerRepair.this.maximumCost = 0;
            IBlockState var5 = var2.getBlockState(var3);
            if (!var1.capabilities.isCreativeMode && !var2.isRemote && var5.getBlock() == Blocks.ANVIL && var1.getRNG().nextFloat() < 0.12F) {
               int var4 = ((Integer)var5.getValue(BlockAnvil.DAMAGE)).intValue();
               ++var4;
               if (var4 > 2) {
                  var2.setBlockToAir(var3);
                  var2.playEvent(1029, var3, 0);
               } else {
                  var2.setBlockState(var3, var5.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(var4)), 2);
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
      byte var3 = 0;
      byte var4 = 0;
      if (var1 == null) {
         CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
         this.maximumCost = 0;
      } else {
         ItemStack var5 = var1.copy();
         ItemStack var6 = this.inputSlots.getStackInSlot(1);
         Map var7 = EnchantmentHelper.getEnchantments(var5);
         int var8 = var3 + var1.getRepairCost() + (var6 == null ? 0 : var6.getRepairCost());
         this.materialCost = 0;
         if (var6 != null) {
            boolean var9 = var6.getItem() == Items.ENCHANTED_BOOK && !Items.ENCHANTED_BOOK.getEnchantments(var6).hasNoTags();
            if (var5.isItemStackDamageable() && var5.getItem().getIsRepairable(var1, var6)) {
               int var22 = Math.min(var5.getItemDamage(), var5.getMaxDamage() / 4);
               if (var22 <= 0) {
                  CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               int var23;
               for(var23 = 0; var22 > 0 && var23 < var6.stackSize; ++var23) {
                  int var24 = var5.getItemDamage() - var22;
                  var5.setItemDamage(var24);
                  ++var2;
                  var22 = Math.min(var5.getItemDamage(), var5.getMaxDamage() / 4);
               }

               this.materialCost = var23;
            } else {
               if (!var9 && (var5.getItem() != var6.getItem() || !var5.isItemStackDamageable())) {
                  CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), (ItemStack)null);
                  this.maximumCost = 0;
                  return;
               }

               if (var5.isItemStackDamageable() && !var9) {
                  int var10 = var1.getMaxDamage() - var1.getItemDamage();
                  int var11 = var6.getMaxDamage() - var6.getItemDamage();
                  int var12 = var11 + var5.getMaxDamage() * 12 / 100;
                  int var13 = var10 + var12;
                  int var14 = var5.getMaxDamage() - var13;
                  if (var14 < 0) {
                     var14 = 0;
                  }

                  if (var14 < var5.getMetadata()) {
                     var5.setItemDamage(var14);
                     var2 += 2;
                  }
               }

               Map var15 = EnchantmentHelper.getEnchantments(var6);

               for(Enchantment var17 : var15.keySet()) {
                  if (var17 != null) {
                     int var25 = var7.containsKey(var17) ? ((Integer)var7.get(var17)).intValue() : 0;
                     int var26 = ((Integer)var15.get(var17)).intValue();
                     var26 = var25 == var26 ? var26 + 1 : Math.max(var26, var25);
                     boolean var18 = var17.canApply(var1);
                     if (this.player.capabilities.isCreativeMode || var1.getItem() == Items.ENCHANTED_BOOK) {
                        var18 = true;
                     }

                     for(Enchantment var20 : var7.keySet()) {
                        if (var20 != var17 && !var17.canApplyTogether(var20)) {
                           var18 = false;
                           ++var2;
                        }
                     }

                     if (var18) {
                        if (var26 > var17.getMaxLevel()) {
                           var26 = var17.getMaxLevel();
                        }

                        var7.put(var17, Integer.valueOf(var26));
                        int var28 = 0;
                        switch(ContainerRepair.SyntheticClass_1.a[var17.getRarity().ordinal()]) {
                        case 1:
                           var28 = 1;
                           break;
                        case 2:
                           var28 = 2;
                           break;
                        case 3:
                           var28 = 4;
                           break;
                        case 4:
                           var28 = 8;
                        }

                        if (var9) {
                           var28 = Math.max(1, var28 / 2);
                        }

                        var2 += var28 * var26;
                     }
                  }
               }
            }
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

         this.maximumCost = var8 + var2;
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
            int var21 = var5.getRepairCost();
            if (var6 != null && var21 < var6.getRepairCost()) {
               var21 = var6.getRepairCost();
            }

            if (var4 != var2 || var4 == 0) {
               var21 = var21 * 2 + 1;
            }

            var5.setRepairCost(var21);
            EnchantmentHelper.setEnchantments(var7, var5);
         }

         CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), var5);
         this.detectAndSendChanges();
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      var1.sendProgressBarUpdate(this, 0, this.maximumCost);
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
      if (!this.checkReachable) {
         return true;
      } else {
         return this.world.getBlockState(this.selfPosition).getBlock() != Blocks.ANVIL ? false : var1.getDistanceSq((double)this.selfPosition.getX() + 0.5D, (double)this.selfPosition.getY() + 0.5D, (double)this.selfPosition.getZ() + 0.5D) <= 64.0D;
      }
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

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryAnvil var1 = new CraftInventoryAnvil(new Location(this.world.getWorld(), (double)this.selfPosition.getX(), (double)this.selfPosition.getY(), (double)this.selfPosition.getZ()), this.inputSlots, this.outputSlot);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), var1, this);
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
