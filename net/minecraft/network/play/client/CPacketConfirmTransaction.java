package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketConfirmTransaction implements Packet {
   private int windowId;
   private short uid;
   private boolean accepted;

   public CPacketConfirmTransaction() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketConfirmTransaction(int var1, short var2, boolean var3) {
      this.windowId = windowIdIn;
      this.uid = uidIn;
      this.accepted = acceptedIn;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processConfirmTransaction(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readByte();
      this.uid = buf.readShort();
      this.accepted = buf.readByte() != 0;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeShort(this.uid);
      buf.writeByte(this.accepted ? 1 : 0);
   }

   public int getWindowId() {
      return this.windowId;
   }

   public short getUid() {
      return this.uid;
   }
}
