package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketSteerBoat implements Packet {
   private boolean left;
   private boolean right;

   public CPacketSteerBoat() {
   }

   public CPacketSteerBoat(boolean var1, boolean var2) {
      this.left = p_i46873_1_;
      this.right = p_i46873_2_;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.left = buf.readBoolean();
      this.right = buf.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBoolean(this.left);
      buf.writeBoolean(this.right);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processSteerBoat(this);
   }

   public boolean getLeft() {
      return this.left;
   }

   public boolean getRight() {
      return this.right;
   }
}
