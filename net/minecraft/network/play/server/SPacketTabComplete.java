package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketTabComplete implements Packet {
   private String[] matches;

   public SPacketTabComplete() {
   }

   public SPacketTabComplete(String[] var1) {
      this.matches = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.matches = new String[var1.readVarInt()];

      for(int var2 = 0; var2 < this.matches.length; ++var2) {
         this.matches[var2] = var1.readString(32767);
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.matches.length);

      for(String var5 : this.matches) {
         var1.writeString(var5);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleTabComplete(this);
   }

   @SideOnly(Side.CLIENT)
   public String[] getMatches() {
      return this.matches;
   }
}
