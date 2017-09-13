package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;

public class SPacketTeams implements Packet {
   private String name = "";
   private String displayName = "";
   private String prefix = "";
   private String suffix = "";
   private String nameTagVisibility;
   private String collisionRule;
   private int color;
   private final Collection players;
   private int action;
   private int friendlyFlags;

   public SPacketTeams() {
      this.nameTagVisibility = Team.EnumVisible.ALWAYS.internalName;
      this.collisionRule = Team.CollisionRule.ALWAYS.name;
      this.color = -1;
      this.players = Lists.newArrayList();
   }

   public SPacketTeams(ScorePlayerTeam var1, int var2) {
      this.nameTagVisibility = Team.EnumVisible.ALWAYS.internalName;
      this.collisionRule = Team.CollisionRule.ALWAYS.name;
      this.color = -1;
      this.players = Lists.newArrayList();
      this.name = var1.getRegisteredName();
      this.action = var2;
      if (var2 == 0 || var2 == 2) {
         this.displayName = var1.getTeamName();
         this.prefix = var1.getColorPrefix();
         this.suffix = var1.getColorSuffix();
         this.friendlyFlags = var1.getFriendlyFlags();
         this.nameTagVisibility = var1.getNameTagVisibility().internalName;
         this.collisionRule = var1.getCollisionRule().name;
         this.color = var1.getChatFormat().getColorIndex();
      }

      if (var2 == 0) {
         this.players.addAll(var1.getMembershipCollection());
      }

   }

   public SPacketTeams(ScorePlayerTeam var1, Collection var2, int var3) {
      this.nameTagVisibility = Team.EnumVisible.ALWAYS.internalName;
      this.collisionRule = Team.CollisionRule.ALWAYS.name;
      this.color = -1;
      this.players = Lists.newArrayList();
      if (var3 != 3 && var3 != 4) {
         throw new IllegalArgumentException("Method must be join or leave for player constructor");
      } else if (var2 != null && !var2.isEmpty()) {
         this.action = var3;
         this.name = var1.getRegisteredName();
         this.players.addAll(var2);
      } else {
         throw new IllegalArgumentException("Players cannot be null/empty");
      }
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.name = var1.readString(16);
      this.action = var1.readByte();
      if (this.action == 0 || this.action == 2) {
         this.displayName = var1.readString(32);
         this.prefix = var1.readString(16);
         this.suffix = var1.readString(16);
         this.friendlyFlags = var1.readByte();
         this.nameTagVisibility = var1.readString(32);
         this.collisionRule = var1.readString(32);
         this.color = var1.readByte();
      }

      if (this.action == 0 || this.action == 3 || this.action == 4) {
         int var2 = var1.readVarInt();

         for(int var3 = 0; var3 < var2; ++var3) {
            this.players.add(var1.readString(40));
         }
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.name);
      var1.writeByte(this.action);
      if (this.action == 0 || this.action == 2) {
         var1.writeString(this.displayName);
         var1.writeString(this.prefix);
         var1.writeString(this.suffix);
         var1.writeByte(this.friendlyFlags);
         var1.writeString(this.nameTagVisibility);
         var1.writeString(this.collisionRule);
         var1.writeByte(this.color);
      }

      if (this.action == 0 || this.action == 3 || this.action == 4) {
         var1.writeVarInt(this.players.size());

         for(String var3 : this.players) {
            var1.writeString(var3);
         }
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleTeams(this);
   }
}
