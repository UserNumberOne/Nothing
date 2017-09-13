package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAIHarvestFarmland extends EntityAIMoveToBlock {
   private final EntityVillager theVillager;
   private boolean hasFarmItem;
   private boolean wantsToReapStuff;
   private int currentTask;

   public EntityAIHarvestFarmland(EntityVillager var1, double var2) {
      super(var1, var2, 16);
      this.theVillager = var1;
   }

   public boolean shouldExecute() {
      if (this.runDelay <= 0) {
         if (!this.theVillager.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
         }

         this.currentTask = -1;
         this.hasFarmItem = this.theVillager.isFarmItemInInventory();
         this.wantsToReapStuff = this.theVillager.wantsMoreFood();
      }

      return super.shouldExecute();
   }

   public boolean continueExecuting() {
      return this.currentTask >= 0 && super.continueExecuting();
   }

   public void startExecuting() {
      super.startExecuting();
   }

   public void resetTask() {
      super.resetTask();
   }

   public void updateTask() {
      super.updateTask();
      this.theVillager.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5D, 10.0F, (float)this.theVillager.getVerticalFaceSpeed());
      if (this.getIsAboveDestination()) {
         World var1 = this.theVillager.world;
         BlockPos var2 = this.destinationBlock.up();
         IBlockState var3 = var1.getBlockState(var2);
         Block var4 = var3.getBlock();
         if (this.currentTask == 0 && var4 instanceof BlockCrops && ((BlockCrops)var4).isMaxAge(var3)) {
            var1.destroyBlock(var2, true);
         } else if (this.currentTask == 1 && var3.getMaterial() == Material.AIR) {
            InventoryBasic var5 = this.theVillager.getVillagerInventory();

            for(int var6 = 0; var6 < var5.getSizeInventory(); ++var6) {
               ItemStack var7 = var5.getStackInSlot(var6);
               boolean var8 = false;
               if (var7 != null) {
                  if (var7.getItem() == Items.WHEAT_SEEDS) {
                     var1.setBlockState(var2, Blocks.WHEAT.getDefaultState(), 3);
                     var8 = true;
                  } else if (var7.getItem() == Items.POTATO) {
                     var1.setBlockState(var2, Blocks.POTATOES.getDefaultState(), 3);
                     var8 = true;
                  } else if (var7.getItem() == Items.CARROT) {
                     var1.setBlockState(var2, Blocks.CARROTS.getDefaultState(), 3);
                     var8 = true;
                  } else if (var7.getItem() == Items.BEETROOT_SEEDS) {
                     var1.setBlockState(var2, Blocks.BEETROOTS.getDefaultState(), 3);
                     var8 = true;
                  }
               }

               if (var8) {
                  --var7.stackSize;
                  if (var7.stackSize <= 0) {
                     var5.setInventorySlotContents(var6, (ItemStack)null);
                  }
                  break;
               }
            }
         }

         this.currentTask = -1;
         this.runDelay = 10;
      }

   }

   protected boolean shouldMoveTo(World var1, BlockPos var2) {
      Block var3 = var1.getBlockState(var2).getBlock();
      if (var3 == Blocks.FARMLAND) {
         var2 = var2.up();
         IBlockState var4 = var1.getBlockState(var2);
         var3 = var4.getBlock();
         if (var3 instanceof BlockCrops && ((BlockCrops)var3).isMaxAge(var4) && this.wantsToReapStuff && (this.currentTask == 0 || this.currentTask < 0)) {
            this.currentTask = 0;
            return true;
         }

         if (var4.getMaterial() == Material.AIR && this.hasFarmItem && (this.currentTask == 1 || this.currentTask < 0)) {
            this.currentTask = 1;
            return true;
         }
      }

      return false;
   }
}
