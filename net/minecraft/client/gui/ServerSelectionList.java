package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerSelectionList extends GuiListExtended {
   private final GuiMultiplayer owner;
   private final List serverListInternet = Lists.newArrayList();
   private final List serverListLan = Lists.newArrayList();
   private final GuiListExtended.IGuiListEntry lanScanEntry = new ServerListEntryLanScan();
   private int selectedSlotIndex = -1;

   public ServerSelectionList(GuiMultiplayer var1, Minecraft var2, int var3, int var4, int var5, int var6, int var7) {
      super(var2, var3, var4, var5, var6, var7);
      this.owner = var1;
   }

   public GuiListExtended.IGuiListEntry getListEntry(int var1) {
      if (var1 < this.serverListInternet.size()) {
         return (GuiListExtended.IGuiListEntry)this.serverListInternet.get(var1);
      } else {
         var1 = var1 - this.serverListInternet.size();
         if (var1 == 0) {
            return this.lanScanEntry;
         } else {
            --var1;
            return (GuiListExtended.IGuiListEntry)this.serverListLan.get(var1);
         }
      }
   }

   protected int getSize() {
      return this.serverListInternet.size() + 1 + this.serverListLan.size();
   }

   public void setSelectedSlotIndex(int var1) {
      this.selectedSlotIndex = var1;
   }

   protected boolean isSelected(int var1) {
      return var1 == this.selectedSlotIndex;
   }

   public int getSelected() {
      return this.selectedSlotIndex;
   }

   public void updateOnlineServers(ServerList var1) {
      this.serverListInternet.clear();

      for(int var2 = 0; var2 < var1.countServers(); ++var2) {
         this.serverListInternet.add(new ServerListEntryNormal(this.owner, var1.getServerData(var2)));
      }

   }

   public void updateNetworkServers(List var1) {
      this.serverListLan.clear();

      for(LanServerInfo var3 : var1) {
         this.serverListLan.add(new ServerListEntryLanDetected(this.owner, var3));
      }

   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 30;
   }

   public int getListWidth() {
      return super.getListWidth() + 85;
   }
}
