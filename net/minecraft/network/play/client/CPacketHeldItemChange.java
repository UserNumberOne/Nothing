package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketHeldItemChange implements Packet {
   private int slotId;

   public CPacketHeldItemChange() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketHeldItemChange(int var1) {
      this.slotId = slotIdIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.slotId = buf.readShort();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeShort(this.slotId);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processHeldItemChange(this);
   }

   public int getSlotId() {
      return this.slotId;
   }
}
