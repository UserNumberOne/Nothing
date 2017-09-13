package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketKeepAlive implements Packet {
   private int id;

   public SPacketKeepAlive() {
   }

   public SPacketKeepAlive(int var1) {
      this.id = idIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleKeepAlive(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.id = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.id);
   }

   @SideOnly(Side.CLIENT)
   public int getId() {
      return this.id;
   }
}
