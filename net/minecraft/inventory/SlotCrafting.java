package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SlotCrafting extends Slot {
   private final InventoryCrafting craftMatrix;
   private final EntityPlayer player;
   private int amountCrafted;

   public SlotCrafting(EntityPlayer var1, InventoryCrafting var2, IInventory var3, int var4, int var5, int var6) {
      super(var3, var4, var5, var6);
      this.player = var1;
      this.craftMatrix = var2;
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
      return false;
   }

   public ItemStack decrStackSize(int var1) {
      if (this.getHasStack()) {
         this.amountCrafted += Math.min(var1, this.getStack().stackSize);
      }

      return super.decrStackSize(var1);
   }

   protected void onCrafting(ItemStack var1, int var2) {
      this.amountCrafted += var2;
      this.onCrafting(var1);
   }

   protected void onCrafting(ItemStack var1) {
      if (this.amountCrafted > 0) {
         var1.onCrafting(this.player.world, this.player, this.amountCrafted);
      }

      this.amountCrafted = 0;
      if (var1.getItem() == Item.getItemFromBlock(Blocks.CRAFTING_TABLE)) {
         this.player.addStat(AchievementList.BUILD_WORK_BENCH);
      }

      if (var1.getItem() instanceof ItemPickaxe) {
         this.player.addStat(AchievementList.BUILD_PICKAXE);
      }

      if (var1.getItem() == Item.getItemFromBlock(Blocks.FURNACE)) {
         this.player.addStat(AchievementList.BUILD_FURNACE);
      }

      if (var1.getItem() instanceof ItemHoe) {
         this.player.addStat(AchievementList.BUILD_HOE);
      }

      if (var1.getItem() == Items.BREAD) {
         this.player.addStat(AchievementList.MAKE_BREAD);
      }

      if (var1.getItem() == Items.CAKE) {
         this.player.addStat(AchievementList.BAKE_CAKE);
      }

      if (var1.getItem() instanceof ItemPickaxe && ((ItemPickaxe)var1.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD) {
         this.player.addStat(AchievementList.BUILD_BETTER_PICKAXE);
      }

      if (var1.getItem() instanceof ItemSword) {
         this.player.addStat(AchievementList.BUILD_SWORD);
      }

      if (var1.getItem() == Item.getItemFromBlock(Blocks.ENCHANTING_TABLE)) {
         this.player.addStat(AchievementList.ENCHANTMENTS);
      }

      if (var1.getItem() == Item.getItemFromBlock(Blocks.BOOKSHELF)) {
         this.player.addStat(AchievementList.BOOKCASE);
      }

   }

   public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
      FMLCommonHandler.instance().firePlayerCraftingEvent(var1, var2, this.craftMatrix);
      this.onCrafting(var2);
      ForgeHooks.setCraftingPlayer(var1);
      ItemStack[] var3 = CraftingManager.getInstance().getRemainingItems(this.craftMatrix, var1.world);
      ForgeHooks.setCraftingPlayer((EntityPlayer)null);

      for(int var4 = 0; var4 < var3.length; ++var4) {
         ItemStack var5 = this.craftMatrix.getStackInSlot(var4);
         ItemStack var6 = var3[var4];
         if (var5 != null) {
            this.craftMatrix.decrStackSize(var4, 1);
            var5 = this.craftMatrix.getStackInSlot(var4);
         }

         if (var6 != null) {
            if (var5 == null) {
               this.craftMatrix.setInventorySlotContents(var4, var6);
            } else if (ItemStack.areItemsEqual(var5, var6) && ItemStack.areItemStackTagsEqual(var5, var6)) {
               var6.stackSize += var5.stackSize;
               this.craftMatrix.setInventorySlotContents(var4, var6);
            } else if (!this.player.inventory.addItemStackToInventory(var6)) {
               this.player.dropItem(var6, false);
            }
         }
      }

   }
}
