package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHand;

public class CPacketAnimation implements Packet {
   private EnumHand hand;

   public CPacketAnimation() {
   }

   public CPacketAnimation(EnumHand var1) {
      this.hand = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.hand = (EnumHand)var1.readEnumValue(EnumHand.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.hand);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.handleAnimation(this);
   }

   public EnumHand getHand() {
      return this.hand;
   }
}
