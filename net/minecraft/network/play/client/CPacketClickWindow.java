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
      this.windowId = var1;
      this.slotId = var2;
      this.packedClickData = var3;
      this.clickedItem = var5 != null ? var5.copy() : null;
      this.actionNumber = var6;
      this.mode = var4;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processClickWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readByte();
      this.slotId = var1.readShort();
      this.packedClickData = var1.readByte();
      this.actionNumber = var1.readShort();
      this.mode = (ClickType)var1.readEnumValue(ClickType.class);
      this.clickedItem = var1.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeShort(this.slotId);
      var1.writeByte(this.packedClickData);
      var1.writeShort(this.actionNumber);
      var1.writeEnumValue(this.mode);
      var1.writeItemStack(this.clickedItem);
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
