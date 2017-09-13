package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketWindowProperty implements Packet {
   private int windowId;
   private int property;
   private int value;

   public SPacketWindowProperty() {
   }

   public SPacketWindowProperty(int var1, int var2, int var3) {
      this.windowId = var1;
      this.property = var2;
      this.value = var3;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleWindowProperty(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readUnsignedByte();
      this.property = var1.readShort();
      this.value = var1.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeShort(this.property);
      var1.writeShort(this.value);
   }
}
