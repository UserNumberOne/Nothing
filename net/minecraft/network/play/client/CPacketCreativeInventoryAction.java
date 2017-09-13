package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketCreativeInventoryAction implements Packet {
   private int slotId;
   private ItemStack stack;

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processCreativeInventoryAction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.slotId = var1.readShort();
      this.stack = var1.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeShort(this.slotId);
      var1.writeItemStack(this.stack);
   }

   public int getSlotId() {
      return this.slotId;
   }

   public ItemStack getStack() {
      return this.stack;
   }
}
