package net.minecraft.network.handshake.client;

import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;

public class C00Handshake implements Packet {
   private int protocolVersion;
   public String ip;
   public int port;
   private EnumConnectionState requestedState;

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.protocolVersion = var1.readVarInt();
      this.ip = var1.readString(255);
      this.port = var1.readUnsignedShort();
      this.requestedState = EnumConnectionState.getById(var1.readVarInt());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.protocolVersion);
      var1.writeString(this.ip);
      var1.writeShort(this.port);
      var1.writeVarInt(this.requestedState.getId());
   }

   public void processPacket(INetHandlerHandshakeServer var1) {
      var1.processHandshake(this);
   }

   public EnumConnectionState getRequestedState() {
      return this.requestedState;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }
}
