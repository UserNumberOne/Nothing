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
      this.protocolVersion = var1;
      this.ip = var2;
      this.port = var3;
      this.requestedState = var4;
   }

   public C00Handshake(int var1, String var2, int var3, EnumConnectionState var4, boolean var5) {
      this(var1, var2, var3, var4);
      this.hasFMLMarker = var5;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.protocolVersion = var1.readVarInt();
      this.ip = var1.readString(255);
      this.port = var1.readUnsignedShort();
      this.requestedState = EnumConnectionState.getById(var1.readVarInt());
      this.hasFMLMarker = this.ip.contains("\u0000FML\u0000");
      this.ip = this.ip.split("\u0000")[0];
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.protocolVersion);
      var1.writeString(this.ip + "\u0000FML\u0000");
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

   public boolean hasFMLMarker() {
      return this.hasFMLMarker;
   }
}
