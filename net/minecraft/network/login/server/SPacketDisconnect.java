package net.minecraft.network.login.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
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
      this.reason = ITextComponent.Serializer.fromJsonLenient(var1.readString(32767));
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeTextComponent(this.reason);
   }

   public void processPacket(INetHandlerLoginClient var1) {
      var1.handleDisconnect(this);
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getReason() {
      return this.reason;
   }
}
