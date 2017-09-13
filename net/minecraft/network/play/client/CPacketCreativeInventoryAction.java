package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketCreativeInventoryAction implements Packet {
   private int slotId;
   private ItemStack stack;

   public CPacketCreativeInventoryAction() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketCreativeInventoryAction(int var1, ItemStack var2) {
      this.slotId = slotIdIn;
      this.stack = stackIn != null ? stackIn.copy() : null;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processCreativeInventoryAction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.slotId = buf.readShort();
      this.stack = buf.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeShort(this.slotId);
      buf.writeItemStack(this.stack);
   }

   public int getSlotId() {
      return this.slotId;
   }

   public ItemStack getStack() {
      return this.stack;
   }
}
