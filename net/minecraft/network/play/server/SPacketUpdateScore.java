package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUpdateScore implements Packet {
   private String name = "";
   private String objective = "";
   private int value;
   private SPacketUpdateScore.Action action;

   public SPacketUpdateScore() {
   }

   public SPacketUpdateScore(Score var1) {
      this.name = var1.getPlayerName();
      this.objective = var1.getObjective().getName();
      this.value = var1.getScorePoints();
      this.action = SPacketUpdateScore.Action.CHANGE;
   }

   public SPacketUpdateScore(String var1) {
      this.name = var1;
      this.objective = "";
      this.value = 0;
      this.action = SPacketUpdateScore.Action.REMOVE;
   }

   public SPacketUpdateScore(String var1, ScoreObjective var2) {
      this.name = var1;
      this.objective = var2.getName();
      this.value = 0;
      this.action = SPacketUpdateScore.Action.REMOVE;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.name = var1.readString(40);
      this.action = (SPacketUpdateScore.Action)var1.readEnumValue(SPacketUpdateScore.Action.class);
      this.objective = var1.readString(16);
      if (this.action != SPacketUpdateScore.Action.REMOVE) {
         this.value = var1.readVarInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.name);
      var1.writeEnumValue(this.action);
      var1.writeString(this.objective);
      if (this.action != SPacketUpdateScore.Action.REMOVE) {
         var1.writeVarInt(this.value);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleUpdateScore(this);
   }

   @SideOnly(Side.CLIENT)
   public String getPlayerName() {
      return this.name;
   }

   @SideOnly(Side.CLIENT)
   public String getObjectiveName() {
      return this.objective;
   }

   @SideOnly(Side.CLIENT)
   public int getScoreValue() {
      return this.value;
   }

   @SideOnly(Side.CLIENT)
   public SPacketUpdateScore.Action getScoreAction() {
      return this.action;
   }

   public static enum Action {
      CHANGE,
      REMOVE;
   }
}
