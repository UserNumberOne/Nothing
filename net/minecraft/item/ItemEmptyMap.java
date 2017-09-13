package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class ItemEmptyMap extends ItemMapBase {
   protected ItemEmptyMap() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      ItemStack var5 = new ItemStack(Items.FILLED_MAP, 1, var2.getUniqueDataId("map"));
      String var6 = "map_" + var5.getMetadata();
      MapData var7 = new MapData(var6);
      var2.setData(var6, var7);
      var7.scale = 0;
      var7.calculateMapCenter(var3.posX, var3.posZ, var7.scale);
      var7.dimension = var2.provider.getDimension();
      var7.trackingPosition = true;
      var7.markDirty();
      --var1.stackSize;
      if (var1.stackSize <= 0) {
         return new ActionResult(EnumActionResult.SUCCESS, var5);
      } else {
         if (!var3.inventory.addItemStackToInventory(var5.copy())) {
            var3.dropItem(var5, false);
         }

         var3.addStat(StatList.getObjectUseStats(this));
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      }
   }
}
