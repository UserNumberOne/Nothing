package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketCloseWindow implements Packet {
   private int windowId;

   public CPacketCloseWindow() {
   }

   public CPacketCloseWindow(int id) {
      this.windowId = id;
   }

   public void processPacket(INetHandlerPlayServer packetlistenerplayin) {
      packetlistenerplayin.processCloseWindow(this);
   }

   public void readPacketData(PacketBuffer packetdataserializer) throws IOException {
      this.windowId = packetdataserializer.readByte();
   }

   public void writePacketData(PacketBuffer packetdataserializer) throws IOException {
      packetdataserializer.writeByte(this.windowId);
   }
}
