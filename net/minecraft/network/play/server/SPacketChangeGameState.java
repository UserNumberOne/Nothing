package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketChangeGameState implements Packet {
   public static final String[] MESSAGE_NAMES = new String[]{"tile.bed.notValid"};
   private int state;
   private float value;

   public SPacketChangeGameState() {
   }

   public SPacketChangeGameState(int var1, float var2) {
      this.state = stateIn;
      this.value = valueIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.state = buf.readUnsignedByte();
      this.value = buf.readFloat();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.state);
      buf.writeFloat(this.value);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleChangeGameState(this);
   }

   @SideOnly(Side.CLIENT)
   public int getGameState() {
      return this.state;
   }

   @SideOnly(Side.CLIENT)
   public float getValue() {
      return this.value;
   }
}
