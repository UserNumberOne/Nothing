package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.server.MinecraftServer;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer scoreboardMCServer;
   private final Set addedObjectives = Sets.newHashSet();
   private Runnable[] dirtyRunnables = new Runnable[0];

   public ServerScoreboard(MinecraftServer var1) {
      this.scoreboardMCServer = var1;
   }

   public void onScoreUpdated(Score var1) {
      super.onScoreUpdated(var1);
      if (this.addedObjectives.contains(var1.getObjective())) {
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(var1));
      }

      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String var1) {
      super.broadcastScoreUpdate(var1);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(var1));
      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String var1, ScoreObjective var2) {
      super.broadcastScoreUpdate(var1, var2);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(var1, var2));
      this.markSaveDataDirty();
   }

   public void setObjectiveInDisplaySlot(int var1, ScoreObjective var2) {
      ScoreObjective var3 = this.getObjectiveInDisplaySlot(var1);
      super.setObjectiveInDisplaySlot(var1, var2);
      if (var3 != var2 && var3 != null) {
         if (this.getObjectiveDisplaySlotCount(var3) > 0) {
            this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketDisplayObjective(var1, var2));
         } else {
            this.sendDisplaySlotRemovalPackets(var3);
         }
      }

      if (var2 != null) {
         if (this.addedObjectives.contains(var2)) {
            this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketDisplayObjective(var1, var2));
         } else {
            this.addObjective(var2);
         }
      }

      this.markSaveDataDirty();
   }

   public boolean addPlayerToTeam(String var1, String var2) {
      if (super.addPlayerToTeam(var1, var2)) {
         ScorePlayerTeam var3 = this.getTeam(var2);
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(var3, Arrays.asList(var1), 3));
         this.markSaveDataDirty();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String var1, ScorePlayerTeam var2) {
      super.removePlayerFromTeam(var1, var2);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(var2, Arrays.asList(var1), 4));
      this.markSaveDataDirty();
   }

   public void onScoreObjectiveAdded(ScoreObjective var1) {
      super.onScoreObjectiveAdded(var1);
      this.markSaveDataDirty();
   }

   public void onObjectiveDisplayNameChanged(ScoreObjective var1) {
      super.onObjectiveDisplayNameChanged(var1);
      if (this.addedObjectives.contains(var1)) {
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketScoreboardObjective(var1, 2));
      }

      this.markSaveDataDirty();
   }

   public void onScoreObjectiveRemoved(ScoreObjective var1) {
      super.onScoreObjectiveRemoved(var1);
      if (this.addedObjectives.contains(var1)) {
         this.sendDisplaySlotRemovalPackets(var1);
      }

      this.markSaveDataDirty();
   }

   public void broadcastTeamCreated(ScorePlayerTeam var1) {
      super.broadcastTeamCreated(var1);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(var1, 0));
      this.markSaveDataDirty();
   }

   public void broadcastTeamInfoUpdate(ScorePlayerTeam var1) {
      super.broadcastTeamInfoUpdate(var1);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(var1, 2));
      this.markSaveDataDirty();
   }

   public void broadcastTeamRemove(ScorePlayerTeam var1) {
      super.broadcastTeamRemove(var1);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(var1, 1));
      this.markSaveDataDirty();
   }

   public void addDirtyRunnable(Runnable var1) {
      this.dirtyRunnables = (Runnable[])Arrays.copyOf(this.dirtyRunnables, this.dirtyRunnables.length + 1);
      this.dirtyRunnables[this.dirtyRunnables.length - 1] = var1;
   }

   protected void markSaveDataDirty() {
      for(Runnable var4 : this.dirtyRunnables) {
         var4.run();
      }

   }

   public List getCreatePackets(ScoreObjective var1) {
      ArrayList var2 = Lists.newArrayList();
      var2.add(new SPacketScoreboardObjective(var1, 0));

      for(int var3 = 0; var3 < 19; ++var3) {
         if (this.getObjectiveInDisplaySlot(var3) == var1) {
            var2.add(new SPacketDisplayObjective(var3, var1));
         }
      }

      for(Score var4 : this.getSortedScores(var1)) {
         var2.add(new SPacketUpdateScore(var4));
      }

      return var2;
   }

   public void addObjective(ScoreObjective var1) {
      List var2 = this.getCreatePackets(var1);

      for(EntityPlayerMP var4 : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         for(Packet var6 : var2) {
            var4.connection.sendPacket(var6);
         }
      }

      this.addedObjectives.add(var1);
   }

   public List getDestroyPackets(ScoreObjective var1) {
      ArrayList var2 = Lists.newArrayList();
      var2.add(new SPacketScoreboardObjective(var1, 1));

      for(int var3 = 0; var3 < 19; ++var3) {
         if (this.getObjectiveInDisplaySlot(var3) == var1) {
            var2.add(new SPacketDisplayObjective(var3, var1));
         }
      }

      return var2;
   }

   public void sendDisplaySlotRemovalPackets(ScoreObjective var1) {
      List var2 = this.getDestroyPackets(var1);

      for(EntityPlayerMP var4 : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         for(Packet var6 : var2) {
            var4.connection.sendPacket(var6);
         }
      }

      this.addedObjectives.remove(var1);
   }

   public int getObjectiveDisplaySlotCount(ScoreObjective var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < 19; ++var3) {
         if (this.getObjectiveInDisplaySlot(var3) == var1) {
            ++var2;
         }
      }

      return var2;
   }
}
