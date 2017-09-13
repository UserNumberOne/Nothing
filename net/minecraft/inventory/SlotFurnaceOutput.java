package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SlotFurnaceOutput extends Slot {
   private final EntityPlayer player;
   private int removeCount;

   public SlotFurnaceOutput(EntityPlayer var1, IInventory var2, int var3, int var4, int var5) {
      super(inventoryIn, slotIndex, xPosition, yPosition);
      this.player = player;
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
      return false;
   }

   public ItemStack decrStackSize(int var1) {
      if (this.getHasStack()) {
         this.removeCount += Math.min(amount, this.getStack().stackSize);
      }

      return super.decrStackSize(amount);
   }

   public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
      this.onCrafting(stack);
      super.onPickupFromSlot(playerIn, stack);
   }

   protected void onCrafting(ItemStack var1, int var2) {
      this.removeCount += amount;
      this.onCrafting(stack);
   }

   protected void onCrafting(ItemStack var1) {
      stack.onCrafting(this.player.world, this.player, this.removeCount);
      if (!this.player.world.isRemote) {
         int i = this.removeCount;
         float f = FurnaceRecipes.instance().getSmeltingExperience(stack);
         if (f == 0.0F) {
            i = 0;
         } else if (f < 1.0F) {
            int j = MathHelper.floor((float)i * f);
            if (j < MathHelper.ceil((float)i * f) && Math.random() < (double)((float)i * f - (float)j)) {
               ++j;
            }

            i = j;
         }

         while(i > 0) {
            int k = EntityXPOrb.getXPSplit(i);
            i -= k;
            this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, k));
         }
      }

      this.removeCount = 0;
      FMLCommonHandler.instance().firePlayerSmeltedEvent(this.player, stack);
      if (stack.getItem() == Items.IRON_INGOT) {
         this.player.addStat(AchievementList.ACQUIRE_IRON);
      }

      if (stack.getItem() == Items.COOKED_FISH) {
         this.player.addStat(AchievementList.COOK_FISH);
      }

   }
}
