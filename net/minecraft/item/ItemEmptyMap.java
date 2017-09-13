package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapData;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.server.MapInitializeEvent;

public class ItemEmptyMap extends ItemMapBase {
   protected ItemEmptyMap() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      World var5 = (World)var2.getServer().getServer().worlds.get(0);
      ItemStack var6 = new ItemStack(Items.FILLED_MAP, 1, var5.getUniqueDataId("map"));
      String var7 = "map_" + var6.getMetadata();
      MapData var8 = new MapData(var7);
      var5.setData(var7, var8);
      var8.scale = 0;
      var8.calculateMapCenter(var3.posX, var3.posZ, var8.scale);
      var8.dimension = (byte)((WorldServer)var2).dimension;
      var8.trackingPosition = true;
      var8.markDirty();
      CraftEventFactory.callEvent(new MapInitializeEvent(var8.mapView));
      --var1.stackSize;
      if (var1.stackSize <= 0) {
         return new ActionResult(EnumActionResult.SUCCESS, var6);
      } else {
         if (!var3.inventory.addItemStackToInventory(var6.copy())) {
            var3.dropItem(var6, false);
         }

         var3.addStat(StatList.getObjectUseStats(this));
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      }
   }
}
