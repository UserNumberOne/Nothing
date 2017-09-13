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

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      World worldMain = (World)world.getServer().getServer().worlds.get(0);
      ItemStack itemstack1 = new ItemStack(Items.FILLED_MAP, 1, worldMain.getUniqueDataId("map"));
      String s = "map_" + itemstack1.getMetadata();
      MapData worldmap = new MapData(s);
      worldMain.setData(s, worldmap);
      worldmap.scale = 0;
      worldmap.calculateMapCenter(entityhuman.posX, entityhuman.posZ, worldmap.scale);
      worldmap.dimension = (byte)((WorldServer)world).dimension;
      worldmap.trackingPosition = true;
      worldmap.markDirty();
      CraftEventFactory.callEvent(new MapInitializeEvent(worldmap.mapView));
      --itemstack.stackSize;
      if (itemstack.stackSize <= 0) {
         return new ActionResult(EnumActionResult.SUCCESS, itemstack1);
      } else {
         if (!entityhuman.inventory.addItemStackToInventory(itemstack1.copy())) {
            entityhuman.dropItem(itemstack1, false);
         }

         entityhuman.addStat(StatList.getObjectUseStats(this));
         return new ActionResult(EnumActionResult.SUCCESS, itemstack);
      }
   }
}
