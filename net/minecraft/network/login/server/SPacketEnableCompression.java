package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEnableCompression implements Packet {
   private int compressionThreshold;

   public SPacketEnableCompression() {
   }

   public SPacketEnableCompression(int var1) {
      this.compressionThreshold = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.compressionThreshold = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.compressionThreshold);
   }

   public void processPacket(INetHandlerLoginClient var1) {
      var1.handleEnableCompression(this);
   }

   @SideOnly(Side.CLIENT)
   public int getCompressionThreshold() {
      return this.compressionThreshold;
   }
}
