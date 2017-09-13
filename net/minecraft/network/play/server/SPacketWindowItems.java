package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketWindowItems implements Packet {
   private int windowId;
   private ItemStack[] itemStacks;

   public SPacketWindowItems() {
   }

   public SPacketWindowItems(int windowIdIn, List stacks) {
      this.windowId = windowIdIn;
      this.itemStacks = new ItemStack[stacks.size()];

      for(int i = 0; i < this.itemStacks.length; ++i) {
         ItemStack itemstack = (ItemStack)stacks.get(i);
         this.itemStacks[i] = itemstack == null ? null : itemstack.copy();
      }

   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.windowId = buf.readUnsignedByte();
      int i = buf.readShort();
      this.itemStacks = new ItemStack[i];

      for(int j = 0; j < i; ++j) {
         this.itemStacks[j] = buf.readItemStack();
      }

   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.itemStacks.length);

      for(ItemStack itemstack : this.itemStacks) {
         buf.writeItemStack(itemstack);
      }

   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleWindowItems(this);
   }

   @SideOnly(Side.CLIENT)
   public int getWindowId() {
      return this.windowId;
   }

   @SideOnly(Side.CLIENT)
   public ItemStack[] getItemStacks() {
      return this.itemStacks;
   }
}
