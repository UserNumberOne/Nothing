package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketCloseWindow implements Packet {
   private int windowId;

   public SPacketCloseWindow() {
   }

   public SPacketCloseWindow(int var1) {
      this.windowId = windowIdIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleCloseWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readUnsignedByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
   }
}
