package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

public class SPacketChat implements Packet {
   private ITextComponent chatComponent;
   private byte type;

   public SPacketChat() {
   }

   public SPacketChat(ITextComponent var1) {
      this(var1, (byte)1);
   }

   public SPacketChat(ITextComponent var1, byte var2) {
      this.chatComponent = var1;
      this.type = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.chatComponent = var1.readTextComponent();
      this.type = var1.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeTextComponent(this.chatComponent);
      var1.writeByte(this.type);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleChat(this);
   }

   public boolean isSystem() {
      return this.type == 1 || this.type == 2;
   }
}
