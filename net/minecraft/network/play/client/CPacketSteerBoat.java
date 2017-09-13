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
      this.left = var1;
      this.right = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.left = var1.readBoolean();
      this.right = var1.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBoolean(this.left);
      var1.writeBoolean(this.right);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processSteerBoat(this);
   }

   public boolean getLeft() {
      return this.left;
   }

   public boolean getRight() {
      return this.right;
   }
}
