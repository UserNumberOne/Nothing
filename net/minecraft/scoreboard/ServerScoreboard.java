package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
      this.scoreboardMCServer = mcServer;
   }

   public void onScoreUpdated(Score var1) {
      super.onScoreUpdated(scoreIn);
      if (this.addedObjectives.contains(scoreIn.getObjective())) {
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(scoreIn));
      }

      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String var1) {
      super.broadcastScoreUpdate(scoreName);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(scoreName));
      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String var1, ScoreObjective var2) {
      super.broadcastScoreUpdate(scoreName, objective);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(scoreName, objective));
      this.markSaveDataDirty();
   }

   public void setObjectiveInDisplaySlot(int var1, ScoreObjective var2) {
      ScoreObjective scoreobjective = this.getObjectiveInDisplaySlot(objectiveSlot);
      super.setObjectiveInDisplaySlot(objectiveSlot, objective);
      if (scoreobjective != objective && scoreobjective != null) {
         if (this.getObjectiveDisplaySlotCount(scoreobjective) > 0) {
            this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketDisplayObjective(objectiveSlot, objective));
         } else {
            this.sendDisplaySlotRemovalPackets(scoreobjective);
         }
      }

      if (objective != null) {
         if (this.addedObjectives.contains(objective)) {
            this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketDisplayObjective(objectiveSlot, objective));
         } else {
            this.addObjective(objective);
         }
      }

      this.markSaveDataDirty();
   }

   public boolean addPlayerToTeam(String var1, String var2) {
      if (super.addPlayerToTeam(player, newTeam)) {
         ScorePlayerTeam scoreplayerteam = this.getTeam(newTeam);
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(scoreplayerteam, Arrays.asList(player), 3));
         this.markSaveDataDirty();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String var1, ScorePlayerTeam var2) {
      super.removePlayerFromTeam(username, playerTeam);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(playerTeam, Arrays.asList(username), 4));
      this.markSaveDataDirty();
   }

   public void onScoreObjectiveAdded(ScoreObjective var1) {
      super.onScoreObjectiveAdded(scoreObjectiveIn);
      this.markSaveDataDirty();
   }

   public void onObjectiveDisplayNameChanged(ScoreObjective var1) {
      super.onObjectiveDisplayNameChanged(objective);
      if (this.addedObjectives.contains(objective)) {
         this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketScoreboardObjective(objective, 2));
      }

      this.markSaveDataDirty();
   }

   public void onScoreObjectiveRemoved(ScoreObjective var1) {
      super.onScoreObjectiveRemoved(objective);
      if (this.addedObjectives.contains(objective)) {
         this.sendDisplaySlotRemovalPackets(objective);
      }

      this.markSaveDataDirty();
   }

   public void broadcastTeamCreated(ScorePlayerTeam var1) {
      super.broadcastTeamCreated(playerTeam);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(playerTeam, 0));
      this.markSaveDataDirty();
   }

   public void broadcastTeamInfoUpdate(ScorePlayerTeam var1) {
      super.broadcastTeamInfoUpdate(playerTeam);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(playerTeam, 2));
      this.markSaveDataDirty();
   }

   public void broadcastTeamRemove(ScorePlayerTeam var1) {
      super.broadcastTeamRemove(playerTeam);
      this.scoreboardMCServer.getPlayerList().sendPacketToAllPlayers(new SPacketTeams(playerTeam, 1));
      this.markSaveDataDirty();
   }

   public void addDirtyRunnable(Runnable var1) {
      this.dirtyRunnables = (Runnable[])Arrays.copyOf(this.dirtyRunnables, this.dirtyRunnables.length + 1);
      this.dirtyRunnables[this.dirtyRunnables.length - 1] = runnable;
   }

   protected void markSaveDataDirty() {
      for(Runnable runnable : this.dirtyRunnables) {
         runnable.run();
      }

   }

   public List getCreatePackets(ScoreObjective var1) {
      List list = Lists.newArrayList();
      list.add(new SPacketScoreboardObjective(objective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveInDisplaySlot(i) == objective) {
            list.add(new SPacketDisplayObjective(i, objective));
         }
      }

      for(Score score : this.getSortedScores(objective)) {
         list.add(new SPacketUpdateScore(score));
      }

      return list;
   }

   public void addObjective(ScoreObjective var1) {
      List list = this.getCreatePackets(objective);

      for(EntityPlayerMP entityplayermp : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         for(Packet packet : list) {
            entityplayermp.connection.sendPacket(packet);
         }
      }

      this.addedObjectives.add(objective);
   }

   public List getDestroyPackets(ScoreObjective var1) {
      List list = Lists.newArrayList();
      list.add(new SPacketScoreboardObjective(p_96548_1_, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveInDisplaySlot(i) == p_96548_1_) {
            list.add(new SPacketDisplayObjective(i, p_96548_1_));
         }
      }

      return list;
   }

   public void sendDisplaySlotRemovalPackets(ScoreObjective var1) {
      List list = this.getDestroyPackets(p_96546_1_);

      for(EntityPlayerMP entityplayermp : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         for(Packet packet : list) {
            entityplayermp.connection.sendPacket(packet);
         }
      }

      this.addedObjectives.remove(p_96546_1_);
   }

   public int getObjectiveDisplaySlotCount(ScoreObjective var1) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getObjectiveInDisplaySlot(j) == p_96552_1_) {
            ++i;
         }
      }

      return i;
   }
}
