package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketConfirmTransaction implements Packet {
   private int windowId;
   private short actionNumber;
   private boolean accepted;

   public SPacketConfirmTransaction() {
   }

   public SPacketConfirmTransaction(int var1, short var2, boolean var3) {
      this.windowId = windowIdIn;
      this.actionNumber = actionNumberIn;
      this.accepted = acceptedIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleConfirmTransaction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readUnsignedByte();
      this.actionNumber = buf.readShort();
      this.accepted = buf.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.actionNumber);
      buf.writeBoolean(this.accepted);
   }

   @SideOnly(Side.CLIENT)
   public int getWindowId() {
      return this.windowId;
   }

   @SideOnly(Side.CLIENT)
   public short getActionNumber() {
      return this.actionNumber;
   }

   @SideOnly(Side.CLIENT)
   public boolean wasAccepted() {
      return this.accepted;
   }
}
