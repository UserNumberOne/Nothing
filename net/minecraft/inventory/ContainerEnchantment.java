package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

public class ContainerEnchantment extends Container {
   public InventoryBasic tableInventory = new InventoryBasic("Enchant", true, 2) {
      public int getInventoryStackLimit() {
         return 64;
      }

      public void markDirty() {
         super.markDirty();
         ContainerEnchantment.this.onCraftMatrixChanged(this);
      }

      public Location getLocation() {
         return new Location(ContainerEnchantment.this.worldPointer.getWorld(), (double)ContainerEnchantment.this.position.getX(), (double)ContainerEnchantment.this.position.getY(), (double)ContainerEnchantment.this.position.getZ());
      }
   };
   public World worldPointer;
   private final BlockPos position;
   private final Random rand = new Random();
   public int xpSeed;
   public int[] enchantLevels = new int[3];
   public int[] enchantClue = new int[]{-1, -1, -1};
   public int[] worldClue = new int[]{-1, -1, -1};
   private CraftInventoryView bukkitEntity = null;
   private Player player;

   public ContainerEnchantment(InventoryPlayer var1, World var2, BlockPos var3) {
      this.worldPointer = var2;
      this.position = var3;
      this.xpSeed = var1.player.getXPSeed();
      this.addSlotToContainer(new Slot(this.tableInventory, 0, 15, 47) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return true;
         }

         public int getSlotStackLimit() {
            return 1;
         }
      });
      this.addSlotToContainer(new Slot(this.tableInventory, 1, 35, 47) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return var1.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(var1.getMetadata()) == EnumDyeColor.BLUE;
         }
      });

      for(int var4 = 0; var4 < 3; ++var4) {
         for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlotToContainer(new Slot(var1, var5 + var4 * 9 + 9, 8 + var5 * 18, 84 + var4 * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(var1, var6, 8 + var6 * 18, 142));
      }

      this.player = (Player)var1.player.getBukkitEntity();
   }

   protected void broadcastData(IContainerListener var1) {
      var1.sendProgressBarUpdate(this, 0, this.enchantLevels[0]);
      var1.sendProgressBarUpdate(this, 1, this.enchantLevels[1]);
      var1.sendProgressBarUpdate(this, 2, this.enchantLevels[2]);
      var1.sendProgressBarUpdate(this, 3, this.xpSeed & -16);
      var1.sendProgressBarUpdate(this, 4, this.enchantClue[0]);
      var1.sendProgressBarUpdate(this, 5, this.enchantClue[1]);
      var1.sendProgressBarUpdate(this, 6, this.enchantClue[2]);
      var1.sendProgressBarUpdate(this, 7, this.worldClue[0]);
      var1.sendProgressBarUpdate(this, 8, this.worldClue[1]);
      var1.sendProgressBarUpdate(this, 9, this.worldClue[2]);
   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      this.broadcastData(var1);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int var1 = 0; var1 < this.listeners.size(); ++var1) {
         IContainerListener var2 = (IContainerListener)this.listeners.get(var1);
         this.broadcastData(var2);
      }

   }

   public void onCraftMatrixChanged(IInventory var1) {
      if (var1 == this.tableInventory) {
         ItemStack var2 = var1.getStackInSlot(0);
         if (var2 != null) {
            if (!this.worldPointer.isRemote) {
               int var3 = 0;

               for(int var4 = -1; var4 <= 1; ++var4) {
                  for(int var5 = -1; var5 <= 1; ++var5) {
                     if ((var4 != 0 || var5 != 0) && this.worldPointer.isAirBlock(this.position.add(var5, 0, var4)) && this.worldPointer.isAirBlock(this.position.add(var5, 1, var4))) {
                        if (this.worldPointer.getBlockState(this.position.add(var5 * 2, 0, var4 * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++var3;
                        }

                        if (this.worldPointer.getBlockState(this.position.add(var5 * 2, 1, var4 * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++var3;
                        }

                        if (var5 != 0 && var4 != 0) {
                           if (this.worldPointer.getBlockState(this.position.add(var5 * 2, 0, var4)).getBlock() == Blocks.BOOKSHELF) {
                              ++var3;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(var5 * 2, 1, var4)).getBlock() == Blocks.BOOKSHELF) {
                              ++var3;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(var5, 0, var4 * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++var3;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(var5, 1, var4 * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++var3;
                           }
                        }
                     }
                  }
               }

               this.rand.setSeed((long)this.xpSeed);

               for(int var11 = 0; var11 < 3; ++var11) {
                  this.enchantLevels[var11] = EnchantmentHelper.calcItemStackEnchantability(this.rand, var11, var3, var2);
                  this.enchantClue[var11] = -1;
                  this.worldClue[var11] = -1;
                  if (this.enchantLevels[var11] < var11 + 1) {
                     this.enchantLevels[var11] = 0;
                  }
               }

               CraftItemStack var13 = CraftItemStack.asCraftMirror(var2);
               PrepareItemEnchantEvent var6 = new PrepareItemEnchantEvent(this.player, this.getBukkitView(), this.worldPointer.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), var13, this.enchantLevels, var3);
               var6.setCancelled(!var2.isItemEnchantable());
               this.worldPointer.getServer().getPluginManager().callEvent(var6);
               if (var6.isCancelled()) {
                  for(int var9 = 0; var9 < 3; ++var9) {
                     this.enchantLevels[var9] = 0;
                  }

                  return;
               }

               for(int var12 = 0; var12 < 3; ++var12) {
                  if (this.enchantLevels[var12] > 0) {
                     List var7 = this.getEnchantmentList(var2, var12, this.enchantLevels[var12]);
                     if (var7 != null && !var7.isEmpty()) {
                        EnchantmentData var8 = (EnchantmentData)var7.get(this.rand.nextInt(var7.size()));
                        this.enchantClue[var12] = Enchantment.getEnchantmentID(var8.enchantmentobj);
                        this.worldClue[var12] = var8.enchantmentLevel;
                     }
                  }
               }

               this.detectAndSendChanges();
            }
         } else {
            for(int var10 = 0; var10 < 3; ++var10) {
               this.enchantLevels[var10] = 0;
               this.enchantClue[var10] = -1;
               this.worldClue[var10] = -1;
            }
         }
      }

   }

   public boolean enchantItem(EntityPlayer var1, int var2) {
      ItemStack var3 = this.tableInventory.getStackInSlot(0);
      ItemStack var4 = this.tableInventory.getStackInSlot(1);
      int var5 = var2 + 1;
      if ((var4 == null || var4.stackSize < var5) && !var1.capabilities.isCreativeMode) {
         return false;
      } else if (this.enchantLevels[var2] > 0 && var3 != null && (var1.experienceLevel >= var5 && var1.experienceLevel >= this.enchantLevels[var2] || var1.capabilities.isCreativeMode)) {
         if (!this.worldPointer.isRemote) {
            Object var6 = this.getEnchantmentList(var3, var2, this.enchantLevels[var2]);
            if (var6 == null) {
               var6 = new ArrayList();
            }

            boolean var7 = var3.getItem() == Items.BOOK;
            if (var6 != null) {
               HashMap var8 = new HashMap();

               for(Object var10 : var6) {
                  EnchantmentData var11 = (EnchantmentData)var10;
                  var8.put(org.bukkit.enchantments.Enchantment.getById(Enchantment.getEnchantmentID(var11.enchantmentobj)), Integer.valueOf(var11.enchantmentLevel));
               }

               CraftItemStack var18 = CraftItemStack.asCraftMirror(var3);
               EnchantItemEvent var17 = new EnchantItemEvent((Player)var1.getBukkitEntity(), this.getBukkitView(), this.worldPointer.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), var18, this.enchantLevels[var2], var8, var2);
               this.worldPointer.getServer().getPluginManager().callEvent(var17);
               int var19 = var17.getExpLevelCost();
               if (var17.isCancelled() || var19 > var1.experienceLevel && !var1.capabilities.isCreativeMode || var17.getEnchantsToAdd().isEmpty()) {
                  return false;
               }

               if (var7) {
                  var3.setItem(Items.ENCHANTED_BOOK);
               }

               for(Entry var13 : var17.getEnchantsToAdd().entrySet()) {
                  try {
                     if (var7) {
                        int var14 = ((org.bukkit.enchantments.Enchantment)var13.getKey()).getId();
                        if (Enchantment.getEnchantmentByID(var14) != null) {
                           EnchantmentData var15 = new EnchantmentData(Enchantment.getEnchantmentByID(var14), ((Integer)var13.getValue()).intValue());
                           Items.ENCHANTED_BOOK.addEnchantment(var3, var15);
                        }
                     } else {
                        var18.addUnsafeEnchantment((org.bukkit.enchantments.Enchantment)var13.getKey(), ((Integer)var13.getValue()).intValue());
                     }
                  } catch (IllegalArgumentException var16) {
                     ;
                  }
               }

               var1.removeExperienceLevel(var5);
               if (!var1.capabilities.isCreativeMode) {
                  var4.stackSize -= var5;
                  if (var4.stackSize <= 0) {
                     this.tableInventory.setInventorySlotContents(1, (ItemStack)null);
                  }
               }

               var1.addStat(StatList.ITEM_ENCHANTED);
               this.tableInventory.markDirty();
               this.xpSeed = var1.getXPSeed();
               this.onCraftMatrixChanged(this.tableInventory);
               this.worldPointer.playSound((EntityPlayer)null, this.position, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, this.worldPointer.rand.nextFloat() * 0.1F + 0.9F);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private List getEnchantmentList(ItemStack var1, int var2, int var3) {
      this.rand.setSeed((long)(this.xpSeed + var2));
      List var4 = EnchantmentHelper.buildEnchantmentList(this.rand, var1, var3, false);
      if (var1.getItem() == Items.BOOK && var4.size() > 1) {
         var4.remove(this.rand.nextInt(var4.size()));
      }

      return var4;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      if (this.worldPointer == null) {
         this.worldPointer = var1.getEntityWorld();
      }

      if (!this.worldPointer.isRemote) {
         for(int var2 = 0; var2 < this.tableInventory.getSizeInventory(); ++var2) {
            ItemStack var3 = this.tableInventory.removeStackFromSlot(var2);
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
         return this.worldPointer.getBlockState(this.position).getBlock() != Blocks.ENCHANTING_TABLE ? false : var1.getDistanceSq((double)this.position.getX() + 0.5D, (double)this.position.getY() + 0.5D, (double)this.position.getZ() + 0.5D) <= 64.0D;
      }
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 == 0) {
            if (!this.mergeItemStack(var5, 2, 38, true)) {
               return null;
            }
         } else if (var2 == 1) {
            if (!this.mergeItemStack(var5, 2, 38, true)) {
               return null;
            }
         } else if (var5.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(var5.getMetadata()) == EnumDyeColor.BLUE) {
            if (!this.mergeItemStack(var5, 1, 2, true)) {
               return null;
            }
         } else {
            if (((Slot)this.inventorySlots.get(0)).getHasStack() || !((Slot)this.inventorySlots.get(0)).isItemValid(var5)) {
               return null;
            }

            if (var5.hasTagCompound() && var5.stackSize == 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(var5.copy());
               var5.stackSize = 0;
            } else if (var5.stackSize >= 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(new ItemStack(var5.getItem(), 1, var5.getMetadata()));
               --var5.stackSize;
            }
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

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryEnchanting var1 = new CraftInventoryEnchanting(this.tableInventory);
         this.bukkitEntity = new CraftInventoryView(this.player, var1, this);
         return this.bukkitEntity;
      }
   }
}
