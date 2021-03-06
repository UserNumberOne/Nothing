package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

public class SPacketPlayerListHeaderFooter implements Packet {
   private ITextComponent header;
   private ITextComponent footer;

   public SPacketPlayerListHeaderFooter() {
   }

   public SPacketPlayerListHeaderFooter(ITextComponent var1) {
      this.header = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.header = var1.readTextComponent();
      this.footer = var1.readTextComponent();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeTextComponent(this.header);
      var1.writeTextComponent(this.footer);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handlePlayerListHeaderFooter(this);
   }
}
