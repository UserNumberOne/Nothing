package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketSetSlot implements Packet {
   private int windowId;
   private int slot;
   private ItemStack item;

   public SPacketSetSlot() {
   }

   public SPacketSetSlot(int var1, int var2, @Nullable ItemStack var3) {
      this.windowId = windowIdIn;
      this.slot = slotIn;
      this.item = itemIn == null ? null : itemIn.copy();
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleSetSlot(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readByte();
      this.slot = buf.readShort();
      this.item = buf.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.slot);
      buf.writeItemStack(this.item);
   }

   @SideOnly(Side.CLIENT)
   public int getWindowId() {
      return this.windowId;
   }

   @SideOnly(Side.CLIENT)
   public int getSlot() {
      return this.slot;
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public ItemStack getStack() {
      return this.item;
   }
}
