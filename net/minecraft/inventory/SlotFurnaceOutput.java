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

   public SlotFurnaceOutput(EntityPlayer var1, IInventory var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.player = var1;
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
      return false;
   }

   public ItemStack decrStackSize(int var1) {
      if (this.getHasStack()) {
         this.removeCount += Math.min(var1, this.getStack().stackSize);
      }

      return super.decrStackSize(var1);
   }

   public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
      this.onCrafting(var2);
      super.onPickupFromSlot(var1, var2);
   }

   protected void onCrafting(ItemStack var1, int var2) {
      this.removeCount += var2;
      this.onCrafting(var1);
   }

   protected void onCrafting(ItemStack var1) {
      var1.onCrafting(this.player.world, this.player, this.removeCount);
      if (!this.player.world.isRemote) {
         int var2 = this.removeCount;
         float var3 = FurnaceRecipes.instance().getSmeltingExperience(var1);
         if (var3 == 0.0F) {
            var2 = 0;
         } else if (var3 < 1.0F) {
            int var4 = MathHelper.floor((float)var2 * var3);
            if (var4 < MathHelper.ceil((float)var2 * var3) && Math.random() < (double)((float)var2 * var3 - (float)var4)) {
               ++var4;
            }

            var2 = var4;
         }

         Player var5 = (Player)this.player.getBukkitEntity();
         TileEntityFurnace var6 = (TileEntityFurnace)this.inventory;
         Block var7 = this.player.world.getWorld().getBlockAt(var6.pos.getX(), var6.pos.getY(), var6.pos.getZ());
         if (this.removeCount != 0) {
            FurnaceExtractEvent var8 = new FurnaceExtractEvent(var5, var7, CraftMagicNumbers.getMaterial(var1.getItem()), this.removeCount, var2);
            this.player.world.getServer().getPluginManager().callEvent(var8);
            var2 = var8.getExpToDrop();
         }

         while(var2 > 0) {
            int var9 = EntityXPOrb.getXPSplit(var2);
            var2 -= var9;
            this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, var9));
         }
      }

      this.removeCount = 0;
      if (var1.getItem() == Items.IRON_INGOT) {
         this.player.addStat(AchievementList.ACQUIRE_IRON);
      }

      if (var1.getItem() == Items.COOKED_FISH) {
         this.player.addStat(AchievementList.COOK_FISH);
      }

   }
}
