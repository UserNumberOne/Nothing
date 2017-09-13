package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketConfirmTransaction implements Packet {
   private int windowId;
   private short uid;
   private boolean accepted;

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processConfirmTransaction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readByte();
      this.uid = var1.readShort();
      this.accepted = var1.readByte() != 0;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeShort(this.uid);
      var1.writeByte(this.accepted ? 1 : 0);
   }

   public int getWindowId() {
      return this.windowId;
   }

   public short getUid() {
      return this.uid;
   }
}
