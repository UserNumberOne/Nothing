package net.minecraft.network.handshake.client;

import java.io.IOException;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class C00Handshake implements Packet {
   private int protocolVersion;
   private String ip;
   private int port;
   private EnumConnectionState requestedState;
   private boolean hasFMLMarker;

   public C00Handshake() {
      this.hasFMLMarker = false;
   }

   @SideOnly(Side.CLIENT)
   public C00Handshake(int var1, String var2, int var3, EnumConnectionState var4) {
      this.hasFMLMarker = false;
      this.protocolVersion = version;
      this.ip = ip;
      this.port = port;
      this.requestedState = requestedState;
   }

   public C00Handshake(int var1, String var2, int var3, EnumConnectionState var4, boolean var5) {
      this(protocol, address, port, state);
      this.hasFMLMarker = addFMLMarker;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.protocolVersion = buf.readVarInt();
      this.ip = buf.readString(255);
      this.port = buf.readUnsignedShort();
      this.requestedState = EnumConnectionState.getById(buf.readVarInt());
      this.hasFMLMarker = this.ip.contains("\u0000FML\u0000");
      this.ip = this.ip.split("\u0000")[0];
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.protocolVersion);
      buf.writeString(this.ip + "\u0000FML\u0000");
      buf.writeShort(this.port);
      buf.writeVarInt(this.requestedState.getId());
   }

   public void processPacket(INetHandlerHandshakeServer var1) {
      handler.processHandshake(this);
   }

   public EnumConnectionState getRequestedState() {
      return this.requestedState;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public boolean hasFMLMarker() {
      return this.hasFMLMarker;
   }
}
