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
      this.status = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.status = (CPacketClientStatus.State)var1.readEnumValue(CPacketClientStatus.State.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.status);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processClientStatus(this);
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
