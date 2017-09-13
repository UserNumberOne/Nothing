package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketChatMessage implements Packet {
   private String message;

   public CPacketChatMessage() {
   }

   public CPacketChatMessage(String var1) {
      if (messageIn.length() > 100) {
         messageIn = messageIn.substring(0, 100);
      }

      this.message = messageIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.message = buf.readString(100);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.message);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processChatMessage(this);
   }

   public String getMessage() {
      return this.message;
   }
}
