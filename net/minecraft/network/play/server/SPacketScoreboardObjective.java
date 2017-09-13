package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketScoreboardObjective implements Packet {
   private String objectiveName;
   private String objectiveValue;
   private IScoreCriteria.EnumRenderType type;
   private int action;

   public SPacketScoreboardObjective() {
   }

   public SPacketScoreboardObjective(ScoreObjective var1, int var2) {
      this.objectiveName = var1.getName();
      this.objectiveValue = var1.getDisplayName();
      this.type = var1.getCriteria().getRenderType();
      this.action = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.objectiveName = var1.readString(16);
      this.action = var1.readByte();
      if (this.action == 0 || this.action == 2) {
         this.objectiveValue = var1.readString(32);
         this.type = IScoreCriteria.EnumRenderType.getByName(var1.readString(16));
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.objectiveName);
      var1.writeByte(this.action);
      if (this.action == 0 || this.action == 2) {
         var1.writeString(this.objectiveValue);
         var1.writeString(this.type.getRenderType());
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleScoreboardObjective(this);
   }

   @SideOnly(Side.CLIENT)
   public String getObjectiveName() {
      return this.objectiveName;
   }

   @SideOnly(Side.CLIENT)
   public String getObjectiveValue() {
      return this.objectiveValue;
   }

   @SideOnly(Side.CLIENT)
   public int getAction() {
      return this.action;
   }

   @SideOnly(Side.CLIENT)
   public IScoreCriteria.EnumRenderType getRenderType() {
      return this.type;
   }
}
