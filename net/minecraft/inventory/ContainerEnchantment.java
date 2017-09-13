package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
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
import org.bukkit.enchantments.Enchantment;
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

   public ContainerEnchantment(InventoryPlayer playerinventory, World world, BlockPos blockposition) {
      this.worldPointer = world;
      this.position = blockposition;
      this.xpSeed = playerinventory.player.getXPSeed();
      this.addSlotToContainer(new Slot(this.tableInventory, 0, 15, 47) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return true;
         }

         public int getSlotStackLimit() {
            return 1;
         }
      });
      this.addSlotToContainer(new Slot(this.tableInventory, 1, 35, 47) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return itemstack.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(itemstack.getMetadata()) == EnumDyeColor.BLUE;
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(playerinventory, var6, 8 + var6 * 18, 142));
      }

      this.player = (Player)playerinventory.player.getBukkitEntity();
   }

   protected void broadcastData(IContainerListener icrafting) {
      icrafting.sendProgressBarUpdate(this, 0, this.enchantLevels[0]);
      icrafting.sendProgressBarUpdate(this, 1, this.enchantLevels[1]);
      icrafting.sendProgressBarUpdate(this, 2, this.enchantLevels[2]);
      icrafting.sendProgressBarUpdate(this, 3, this.xpSeed & -16);
      icrafting.sendProgressBarUpdate(this, 4, this.enchantClue[0]);
      icrafting.sendProgressBarUpdate(this, 5, this.enchantClue[1]);
      icrafting.sendProgressBarUpdate(this, 6, this.enchantClue[2]);
      icrafting.sendProgressBarUpdate(this, 7, this.worldClue[0]);
      icrafting.sendProgressBarUpdate(this, 8, this.worldClue[1]);
      icrafting.sendProgressBarUpdate(this, 9, this.worldClue[2]);
   }

   public void addListener(IContainerListener icrafting) {
      super.addListener(icrafting);
      this.broadcastData(icrafting);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int i = 0; i < this.listeners.size(); ++i) {
         IContainerListener icrafting = (IContainerListener)this.listeners.get(i);
         this.broadcastData(icrafting);
      }

   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      if (iinventory == this.tableInventory) {
         ItemStack itemstack = iinventory.getStackInSlot(0);
         if (itemstack != null) {
            if (!this.worldPointer.isRemote) {
               int i = 0;

               for(int j = -1; j <= 1; ++j) {
                  for(int k = -1; k <= 1; ++k) {
                     if ((j != 0 || k != 0) && this.worldPointer.isAirBlock(this.position.add(k, 0, j)) && this.worldPointer.isAirBlock(this.position.add(k, 1, j))) {
                        if (this.worldPointer.getBlockState(this.position.add(k * 2, 0, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++i;
                        }

                        if (this.worldPointer.getBlockState(this.position.add(k * 2, 1, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                           ++i;
                        }

                        if (k != 0 && j != 0) {
                           if (this.worldPointer.getBlockState(this.position.add(k * 2, 0, j)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(k * 2, 1, j)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(k, 0, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }

                           if (this.worldPointer.getBlockState(this.position.add(k, 1, j * 2)).getBlock() == Blocks.BOOKSHELF) {
                              ++i;
                           }
                        }
                     }
                  }
               }

               this.rand.setSeed((long)this.xpSeed);

               for(int var11 = 0; var11 < 3; ++var11) {
                  this.enchantLevels[var11] = EnchantmentHelper.calcItemStackEnchantability(this.rand, var11, i, itemstack);
                  this.enchantClue[var11] = -1;
                  this.worldClue[var11] = -1;
                  if (this.enchantLevels[var11] < var11 + 1) {
                     this.enchantLevels[var11] = 0;
                  }
               }

               CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
               PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(this.player, this.getBukkitView(), this.worldPointer.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), item, this.enchantLevels, i);
               event.setCancelled(!itemstack.isItemEnchantable());
               this.worldPointer.getServer().getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  for(int var9 = 0; var9 < 3; ++var9) {
                     this.enchantLevels[var9] = 0;
                  }

                  return;
               }

               for(int var12 = 0; var12 < 3; ++var12) {
                  if (this.enchantLevels[var12] > 0) {
                     List list = this.getEnchantmentList(itemstack, var12, this.enchantLevels[var12]);
                     if (list != null && !list.isEmpty()) {
                        EnchantmentData weightedrandomenchant = (EnchantmentData)list.get(this.rand.nextInt(list.size()));
                        this.enchantClue[var12] = net.minecraft.enchantment.Enchantment.getEnchantmentID(weightedrandomenchant.enchantmentobj);
                        this.worldClue[var12] = weightedrandomenchant.enchantmentLevel;
                     }
                  }
               }

               this.detectAndSendChanges();
            }
         } else {
            for(int i = 0; i < 3; ++i) {
               this.enchantLevels[i] = 0;
               this.enchantClue[i] = -1;
               this.worldClue[i] = -1;
            }
         }
      }

   }

   public boolean enchantItem(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = this.tableInventory.getStackInSlot(0);
      ItemStack itemstack1 = this.tableInventory.getStackInSlot(1);
      int j = i + 1;
      if ((itemstack1 == null || itemstack1.stackSize < j) && !entityhuman.capabilities.isCreativeMode) {
         return false;
      } else if (this.enchantLevels[i] > 0 && itemstack != null && (entityhuman.experienceLevel >= j && entityhuman.experienceLevel >= this.enchantLevels[i] || entityhuman.capabilities.isCreativeMode)) {
         if (!this.worldPointer.isRemote) {
            List list = this.getEnchantmentList(itemstack, i, this.enchantLevels[i]);
            if (list == null) {
               list = new ArrayList();
            }

            boolean flag = itemstack.getItem() == Items.BOOK;
            if (list != null) {
               Map enchants = new HashMap();

               for(Object obj : list) {
                  EnchantmentData instance = (EnchantmentData)obj;
                  enchants.put(Enchantment.getById(net.minecraft.enchantment.Enchantment.getEnchantmentID(instance.enchantmentobj)), Integer.valueOf(instance.enchantmentLevel));
               }

               CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
               EnchantItemEvent event = new EnchantItemEvent((Player)entityhuman.getBukkitEntity(), this.getBukkitView(), this.worldPointer.getWorld().getBlockAt(this.position.getX(), this.position.getY(), this.position.getZ()), item, this.enchantLevels[i], enchants, i);
               this.worldPointer.getServer().getPluginManager().callEvent(event);
               int level = event.getExpLevelCost();
               if (event.isCancelled() || level > entityhuman.experienceLevel && !entityhuman.capabilities.isCreativeMode || event.getEnchantsToAdd().isEmpty()) {
                  return false;
               }

               if (flag) {
                  itemstack.setItem(Items.ENCHANTED_BOOK);
               }

               for(Entry entry : event.getEnchantsToAdd().entrySet()) {
                  try {
                     if (flag) {
                        int enchantId = ((Enchantment)entry.getKey()).getId();
                        if (net.minecraft.enchantment.Enchantment.getEnchantmentByID(enchantId) != null) {
                           EnchantmentData weightedrandomenchant = new EnchantmentData(net.minecraft.enchantment.Enchantment.getEnchantmentByID(enchantId), ((Integer)entry.getValue()).intValue());
                           Items.ENCHANTED_BOOK.addEnchantment(itemstack, weightedrandomenchant);
                        }
                     } else {
                        item.addUnsafeEnchantment((Enchantment)entry.getKey(), ((Integer)entry.getValue()).intValue());
                     }
                  } catch (IllegalArgumentException var16) {
                     ;
                  }
               }

               entityhuman.removeExperienceLevel(j);
               if (!entityhuman.capabilities.isCreativeMode) {
                  itemstack1.stackSize -= j;
                  if (itemstack1.stackSize <= 0) {
                     this.tableInventory.setInventorySlotContents(1, (ItemStack)null);
                  }
               }

               entityhuman.addStat(StatList.ITEM_ENCHANTED);
               this.tableInventory.markDirty();
               this.xpSeed = entityhuman.getXPSeed();
               this.onCraftMatrixChanged(this.tableInventory);
               this.worldPointer.playSound((EntityPlayer)null, this.position, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, this.worldPointer.rand.nextFloat() * 0.1F + 0.9F);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private List getEnchantmentList(ItemStack itemstack, int i, int j) {
      this.rand.setSeed((long)(this.xpSeed + i));
      List list = EnchantmentHelper.buildEnchantmentList(this.rand, itemstack, j, false);
      if (itemstack.getItem() == Items.BOOK && list.size() > 1) {
         list.remove(this.rand.nextInt(list.size()));
      }

      return list;
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      if (this.worldPointer == null) {
         this.worldPointer = entityhuman.getEntityWorld();
      }

      if (!this.worldPointer.isRemote) {
         for(int i = 0; i < this.tableInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.tableInventory.removeStackFromSlot(i);
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
         return this.worldPointer.getBlockState(this.position).getBlock() != Blocks.ENCHANTING_TABLE ? false : entityhuman.getDistanceSq((double)this.position.getX() + 0.5D, (double)this.position.getY() + 0.5D, (double)this.position.getZ() + 0.5D) <= 64.0D;
      }
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i == 0) {
            if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
               return null;
            }
         } else if (i == 1) {
            if (!this.mergeItemStack(itemstack1, 2, 38, true)) {
               return null;
            }
         } else if (itemstack1.getItem() == Items.DYE && EnumDyeColor.byDyeDamage(itemstack1.getMetadata()) == EnumDyeColor.BLUE) {
            if (!this.mergeItemStack(itemstack1, 1, 2, true)) {
               return null;
            }
         } else {
            if (((Slot)this.inventorySlots.get(0)).getHasStack() || !((Slot)this.inventorySlots.get(0)).isItemValid(itemstack1)) {
               return null;
            }

            if (itemstack1.hasTagCompound() && itemstack1.stackSize == 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(itemstack1.copy());
               itemstack1.stackSize = 0;
            } else if (itemstack1.stackSize >= 1) {
               ((Slot)this.inventorySlots.get(0)).putStack(new ItemStack(itemstack1.getItem(), 1, itemstack1.getMetadata()));
               --itemstack1.stackSize;
            }
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

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryEnchanting inventory = new CraftInventoryEnchanting(this.tableInventory);
         this.bukkitEntity = new CraftInventoryView(this.player, inventory, this);
         return this.bukkitEntity;
      }
   }
}
