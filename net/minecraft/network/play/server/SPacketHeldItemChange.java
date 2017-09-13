package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketHeldItemChange implements Packet {
   private int heldItemHotbarIndex;

   public SPacketHeldItemChange() {
   }

   public SPacketHeldItemChange(int var1) {
      this.heldItemHotbarIndex = hotbarIndexIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.heldItemHotbarIndex = buf.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.heldItemHotbarIndex);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleHeldItemChange(this);
   }

   @SideOnly(Side.CLIENT)
   public int getHeldItemHotbarIndex() {
      return this.heldItemHotbarIndex;
   }
}
