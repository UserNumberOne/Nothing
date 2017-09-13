package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketClickWindow implements Packet {
   private int windowId;
   private int slotId;
   private int packedClickData;
   private short actionNumber;
   private ItemStack clickedItem;
   private ClickType mode;

   public CPacketClickWindow() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketClickWindow(int var1, int var2, int var3, ClickType var4, ItemStack var5, short var6) {
      this.windowId = windowIdIn;
      this.slotId = slotIdIn;
      this.packedClickData = usedButtonIn;
      this.clickedItem = clickedItemIn != null ? clickedItemIn.copy() : null;
      this.actionNumber = actionNumberIn;
      this.mode = modeIn;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processClickWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readByte();
      this.slotId = buf.readShort();
      this.packedClickData = buf.readByte();
      this.actionNumber = buf.readShort();
      this.mode = (ClickType)buf.readEnumValue(ClickType.class);
      this.clickedItem = buf.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.slotId);
      buf.writeByte(this.packedClickData);
      buf.writeShort(this.actionNumber);
      buf.writeEnumValue(this.mode);
      buf.writeItemStack(this.clickedItem);
   }

   public int getWindowId() {
      return this.windowId;
   }

   public int getSlotId() {
      return this.slotId;
   }

   public int getUsedButton() {
      return this.packedClickData;
   }

   public short getActionNumber() {
      return this.actionNumber;
   }

   public ItemStack getClickedItem() {
      return this.clickedItem;
   }

   public ClickType getClickType() {
      return this.mode;
   }
}
