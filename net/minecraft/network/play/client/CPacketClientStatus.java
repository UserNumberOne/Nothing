package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketClientStatus implements Packet {
   private CPacketClientStatus.State status;

   public CPacketClientStatus() {
   }

   public CPacketClientStatus(CPacketClientStatus.State var1) {
      this.status = p_i46886_1_;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.status = (CPacketClientStatus.State)buf.readEnumValue(CPacketClientStatus.State.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeEnumValue(this.status);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processClientStatus(this);
   }

   public CPacketClientStatus.State getStatus() {
      return this.status;
   }

   public static enum State {
      PERFORM_RESPAWN,
      REQUEST_STATS,
      OPEN_INVENTORY_ACHIEVEMENT;
   }
}
