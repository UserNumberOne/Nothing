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
      super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
      this.owner = ownerIn;
   }

   public GuiListExtended.IGuiListEntry getListEntry(int var1) {
      if (index < this.serverListInternet.size()) {
         return (GuiListExtended.IGuiListEntry)this.serverListInternet.get(index);
      } else {
         index = index - this.serverListInternet.size();
         if (index == 0) {
            return this.lanScanEntry;
         } else {
            --index;
            return (GuiListExtended.IGuiListEntry)this.serverListLan.get(index);
         }
      }
   }

   protected int getSize() {
      return this.serverListInternet.size() + 1 + this.serverListLan.size();
   }

   public void setSelectedSlotIndex(int var1) {
      this.selectedSlotIndex = selectedSlotIndexIn;
   }

   protected boolean isSelected(int var1) {
      return slotIndex == this.selectedSlotIndex;
   }

   public int getSelected() {
      return this.selectedSlotIndex;
   }

   public void updateOnlineServers(ServerList var1) {
      this.serverListInternet.clear();

      for(int i = 0; i < p_148195_1_.countServers(); ++i) {
         this.serverListInternet.add(new ServerListEntryNormal(this.owner, p_148195_1_.getServerData(i)));
      }

   }

   public void updateNetworkServers(List var1) {
      this.serverListLan.clear();

      for(LanServerInfo lanserverinfo : p_148194_1_) {
         this.serverListLan.add(new ServerListEntryLanDetected(this.owner, lanserverinfo));
      }

   }

   protected int getScrollBarX() {
      return super.getScrollBarX() + 30;
   }

   public int getListWidth() {
      return super.getListWidth() + 85;
   }
}
