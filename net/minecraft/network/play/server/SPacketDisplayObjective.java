package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketDisplayObjective implements Packet {
   private int position;
   private String scoreName;

   public SPacketDisplayObjective() {
   }

   public SPacketDisplayObjective(int var1, ScoreObjective var2) {
      this.position = positionIn;
      if (objective == null) {
         this.scoreName = "";
      } else {
         this.scoreName = objective.getName();
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.position = buf.readByte();
      this.scoreName = buf.readString(16);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.position);
      buf.writeString(this.scoreName);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleDisplayObjective(this);
   }

   @SideOnly(Side.CLIENT)
   public int getPosition() {
      return this.position;
   }

   @SideOnly(Side.CLIENT)
   public String getName() {
      return this.scoreName;
   }
}
