package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketResourcePackStatus implements Packet {
   private CPacketResourcePackStatus.Action action;

   public CPacketResourcePackStatus() {
   }

   public CPacketResourcePackStatus(CPacketResourcePackStatus.Action var1) {
      this.action = p_i47156_1_;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.action = (CPacketResourcePackStatus.Action)buf.readEnumValue(CPacketResourcePackStatus.Action.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeEnumValue(this.action);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.handleResourcePackStatus(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED;
   }
}
