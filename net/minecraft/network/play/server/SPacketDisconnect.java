package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketDisconnect implements Packet {
   private ITextComponent reason;

   public SPacketDisconnect() {
   }

   public SPacketDisconnect(ITextComponent var1) {
      this.reason = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.reason = var1.readTextComponent();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeTextComponent(this.reason);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleDisconnect(this);
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getReason() {
      return this.reason;
   }
}
