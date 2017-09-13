package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class SlotFurnaceOutput extends Slot {
   private final EntityPlayer player;
   private int removeCount;

   public SlotFurnaceOutput(EntityPlayer entityhuman, IInventory iinventory, int i, int j, int k) {
      super(iinventory, i, j, k);
      this.player = entityhuman;
   }

   public boolean isItemValid(@Nullable ItemStack itemstack) {
      return false;
   }

   public ItemStack decrStackSize(int i) {
      if (this.getHasStack()) {
         this.removeCount += Math.min(i, this.getStack().stackSize);
      }

      return super.decrStackSize(i);
   }

   public void onPickupFromSlot(EntityPlayer entityhuman, ItemStack itemstack) {
      this.onCrafting(itemstack);
      super.onPickupFromSlot(entityhuman, itemstack);
   }

   protected void onCrafting(ItemStack itemstack, int i) {
      this.removeCount += i;
      this.onCrafting(itemstack);
   }

   protected void onCrafting(ItemStack itemstack) {
      itemstack.onCrafting(this.player.world, this.player, this.removeCount);
      if (!this.player.world.isRemote) {
         int i = this.removeCount;
         float f = FurnaceRecipes.instance().getSmeltingExperience(itemstack);
         if (f == 0.0F) {
            i = 0;
         } else if (f < 1.0F) {
            int j = MathHelper.floor((float)i * f);
            if (j < MathHelper.ceil((float)i * f) && Math.random() < (double)((float)i * f - (float)j)) {
               ++j;
            }

            i = j;
         }

         Player player = (Player)this.player.getBukkitEntity();
         TileEntityFurnace furnace = (TileEntityFurnace)this.inventory;
         Block block = this.player.world.getWorld().getBlockAt(furnace.pos.getX(), furnace.pos.getY(), furnace.pos.getZ());
         if (this.removeCount != 0) {
            FurnaceExtractEvent event = new FurnaceExtractEvent(player, block, CraftMagicNumbers.getMaterial(itemstack.getItem()), this.removeCount, i);
            this.player.world.getServer().getPluginManager().callEvent(event);
            i = event.getExpToDrop();
         }

         while(i > 0) {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, j));
         }
      }

      this.removeCount = 0;
      if (itemstack.getItem() == Items.IRON_INGOT) {
         this.player.addStat(AchievementList.ACQUIRE_IRON);
      }

      if (itemstack.getItem() == Items.COOKED_FISH) {
         this.player.addStat(AchievementList.COOK_FISH);
      }

   }
}
