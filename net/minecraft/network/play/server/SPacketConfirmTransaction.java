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
      this.windowId = var1;
      this.actionNumber = var2;
      this.accepted = var3;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleConfirmTransaction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readUnsignedByte();
      this.actionNumber = var1.readShort();
      this.accepted = var1.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeShort(this.actionNumber);
      var1.writeBoolean(this.accepted);
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
