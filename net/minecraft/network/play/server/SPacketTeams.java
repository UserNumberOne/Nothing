package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

   public SPacketTeams(ScorePlayerTeam teamIn, int actionIn) {
      this.nameTagVisibility = Team.EnumVisible.ALWAYS.internalName;
      this.collisionRule = Team.CollisionRule.ALWAYS.name;
      this.color = -1;
      this.players = Lists.newArrayList();
      this.name = teamIn.getRegisteredName();
      this.action = actionIn;
      if (actionIn == 0 || actionIn == 2) {
         this.displayName = teamIn.getTeamName();
         this.prefix = teamIn.getColorPrefix();
         this.suffix = teamIn.getColorSuffix();
         this.friendlyFlags = teamIn.getFriendlyFlags();
         this.nameTagVisibility = teamIn.getNameTagVisibility().internalName;
         this.collisionRule = teamIn.getCollisionRule().name;
         this.color = teamIn.getChatFormat().getColorIndex();
      }

      if (actionIn == 0) {
         this.players.addAll(teamIn.getMembershipCollection());
      }

   }

   public SPacketTeams(ScorePlayerTeam teamIn, Collection playersIn, int actionIn) {
      this.nameTagVisibility = Team.EnumVisible.ALWAYS.internalName;
      this.collisionRule = Team.CollisionRule.ALWAYS.name;
      this.color = -1;
      this.players = Lists.newArrayList();
      if (actionIn != 3 && actionIn != 4) {
         throw new IllegalArgumentException("Method must be join or leave for player constructor");
      } else if (playersIn != null && !playersIn.isEmpty()) {
         this.action = actionIn;
         this.name = teamIn.getRegisteredName();
         this.players.addAll(playersIn);
      } else {
         throw new IllegalArgumentException("Players cannot be null/empty");
      }
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.name = buf.readString(16);
      this.action = buf.readByte();
      if (this.action == 0 || this.action == 2) {
         this.displayName = buf.readString(32);
         this.prefix = buf.readString(16);
         this.suffix = buf.readString(16);
         this.friendlyFlags = buf.readByte();
         this.nameTagVisibility = buf.readString(32);
         this.collisionRule = buf.readString(32);
         this.color = buf.readByte();
      }

      if (this.action == 0 || this.action == 3 || this.action == 4) {
         int i = buf.readVarInt();

         for(int j = 0; j < i; ++j) {
            this.players.add(buf.readString(40));
         }
      }

   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeString(this.name);
      buf.writeByte(this.action);
      if (this.action == 0 || this.action == 2) {
         buf.writeString(this.displayName);
         buf.writeString(this.prefix);
         buf.writeString(this.suffix);
         buf.writeByte(this.friendlyFlags);
         buf.writeString(this.nameTagVisibility);
         buf.writeString(this.collisionRule);
         buf.writeByte(this.color);
      }

      if (this.action == 0 || this.action == 3 || this.action == 4) {
         buf.writeVarInt(this.players.size());

         for(String s : this.players) {
            buf.writeString(s);
         }
      }

   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleTeams(this);
   }

   @SideOnly(Side.CLIENT)
   public String getName() {
      return this.name;
   }

   @SideOnly(Side.CLIENT)
   public String getDisplayName() {
      return this.displayName;
   }

   @SideOnly(Side.CLIENT)
   public String getPrefix() {
      return this.prefix;
   }

   @SideOnly(Side.CLIENT)
   public String getSuffix() {
      return this.suffix;
   }

   @SideOnly(Side.CLIENT)
   public Collection getPlayers() {
      return this.players;
   }

   @SideOnly(Side.CLIENT)
   public int getAction() {
      return this.action;
   }

   @SideOnly(Side.CLIENT)
   public int getFriendlyFlags() {
      return this.friendlyFlags;
   }

   @SideOnly(Side.CLIENT)
   public int getColor() {
      return this.color;
   }

   @SideOnly(Side.CLIENT)
   public String getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   @SideOnly(Side.CLIENT)
   public String getCollisionRule() {
      return this.collisionRule;
   }
}