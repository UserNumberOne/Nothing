package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketChangeGameState implements Packet {
   public static final String[] MESSAGE_NAMES = new String[]{"tile.bed.notValid"};
   private int state;
   private float value;

   public SPacketChangeGameState() {
   }

   public SPacketChangeGameState(int var1, float var2) {
      this.state = var1;
      this.value = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.state = var1.readUnsignedByte();
      this.value = var1.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.state);
      var1.writeFloat(this.value);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleChangeGameState(this);
   }
}
