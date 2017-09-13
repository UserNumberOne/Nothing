package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;

public class SPacketDisplayObjective implements Packet {
   private int position;
   private String scoreName;

   public SPacketDisplayObjective() {
   }

   public SPacketDisplayObjective(int var1, ScoreObjective var2) {
      this.position = var1;
      if (var2 == null) {
         this.scoreName = "";
      } else {
         this.scoreName = var2.getName();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.position = var1.readByte();
      this.scoreName = var1.readString(16);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.position);
      var1.writeString(this.scoreName);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleDisplayObjective(this);
   }
}
