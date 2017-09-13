package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketSetSlot implements Packet {
   private int windowId;
   private int slot;
   private ItemStack item;

   public SPacketSetSlot() {
   }

   public SPacketSetSlot(int var1, int var2, @Nullable ItemStack var3) {
      this.windowId = var1;
      this.slot = var2;
      this.item = var3 == null ? null : var3.copy();
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleSetSlot(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readByte();
      this.slot = var1.readShort();
      this.item = var1.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeShort(this.slot);
      var1.writeItemStack(this.item);
   }
}
