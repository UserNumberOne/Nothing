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
import net.minecraft.src.MinecraftServer;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer scoreboardMCServer;
   private final Set addedObjectives = Sets.newHashSet();
   private Runnable[] dirtyRunnables = new Runnable[0];

   public ServerScoreboard(MinecraftServer minecraftserver) {
      this.scoreboardMCServer = minecraftserver;
   }

   public void onScoreUpdated(Score scoreboardscore) {
      super.onScoreUpdated(scoreboardscore);
      if (this.addedObjectives.contains(scoreboardscore.getObjective())) {
         this.sendAll(new SPacketUpdateScore(scoreboardscore));
      }

      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String s) {
      super.broadcastScoreUpdate(s);
      this.sendAll(new SPacketUpdateScore(s));
      this.markSaveDataDirty();
   }

   public void broadcastScoreUpdate(String s, ScoreObjective scoreboardobjective) {
      super.broadcastScoreUpdate(s, scoreboardobjective);
      this.sendAll(new SPacketUpdateScore(s, scoreboardobjective));
      this.markSaveDataDirty();
   }

   public void setObjectiveInDisplaySlot(int i, ScoreObjective scoreboardobjective) {
      ScoreObjective scoreboardobjective1 = this.getObjectiveInDisplaySlot(i);
      super.setObjectiveInDisplaySlot(i, scoreboardobjective);
      if (scoreboardobjective1 != scoreboardobjective && scoreboardobjective1 != null) {
         if (this.getObjectiveDisplaySlotCount(scoreboardobjective1) > 0) {
            this.sendAll(new SPacketDisplayObjective(i, scoreboardobjective));
         } else {
            this.sendDisplaySlotRemovalPackets(scoreboardobjective1);
         }
      }

      if (scoreboardobjective != null) {
         if (this.addedObjectives.contains(scoreboardobjective)) {
            this.sendAll(new SPacketDisplayObjective(i, scoreboardobjective));
         } else {
            this.addObjective(scoreboardobjective);
         }
      }

      this.markSaveDataDirty();
   }

   public boolean addPlayerToTeam(String s, String s1) {
      if (super.addPlayerToTeam(s, s1)) {
         ScorePlayerTeam scoreboardteam = this.getTeam(s1);
         this.sendAll(new SPacketTeams(scoreboardteam, Arrays.asList(s), 3));
         this.markSaveDataDirty();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String s, ScorePlayerTeam scoreboardteam) {
      super.removePlayerFromTeam(s, scoreboardteam);
      this.sendAll(new SPacketTeams(scoreboardteam, Arrays.asList(s), 4));
      this.markSaveDataDirty();
   }

   public void onScoreObjectiveAdded(ScoreObjective scoreboardobjective) {
      super.onScoreObjectiveAdded(scoreboardobjective);
      this.markSaveDataDirty();
   }

   public void onObjectiveDisplayNameChanged(ScoreObjective scoreboardobjective) {
      super.onObjectiveDisplayNameChanged(scoreboardobjective);
      if (this.addedObjectives.contains(scoreboardobjective)) {
         this.sendAll(new SPacketScoreboardObjective(scoreboardobjective, 2));
      }

      this.markSaveDataDirty();
   }

   public void onScoreObjectiveRemoved(ScoreObjective scoreboardobjective) {
      super.onScoreObjectiveRemoved(scoreboardobjective);
      if (this.addedObjectives.contains(scoreboardobjective)) {
         this.sendDisplaySlotRemovalPackets(scoreboardobjective);
      }

      this.markSaveDataDirty();
   }

   public void broadcastTeamCreated(ScorePlayerTeam scoreboardteam) {
      super.broadcastTeamCreated(scoreboardteam);
      this.sendAll(new SPacketTeams(scoreboardteam, 0));
      this.markSaveDataDirty();
   }

   public void broadcastTeamInfoUpdate(ScorePlayerTeam scoreboardteam) {
      super.broadcastTeamInfoUpdate(scoreboardteam);
      this.sendAll(new SPacketTeams(scoreboardteam, 2));
      this.markSaveDataDirty();
   }

   public void broadcastTeamRemove(ScorePlayerTeam scoreboardteam) {
      super.broadcastTeamRemove(scoreboardteam);
      this.sendAll(new SPacketTeams(scoreboardteam, 1));
      this.markSaveDataDirty();
   }

   public void addDirtyRunnable(Runnable runnable) {
      this.dirtyRunnables = (Runnable[])Arrays.copyOf(this.dirtyRunnables, this.dirtyRunnables.length + 1);
      this.dirtyRunnables[this.dirtyRunnables.length - 1] = runnable;
   }

   protected void markSaveDataDirty() {
      for(Runnable runnable : this.dirtyRunnables) {
         runnable.run();
      }

   }

   public List getCreatePackets(ScoreObjective scoreboardobjective) {
      ArrayList arraylist = Lists.newArrayList();
      arraylist.add(new SPacketScoreboardObjective(scoreboardobjective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveInDisplaySlot(i) == scoreboardobjective) {
            arraylist.add(new SPacketDisplayObjective(i, scoreboardobjective));
         }
      }

      for(Score scoreboardscore : this.getSortedScores(scoreboardobjective)) {
         arraylist.add(new SPacketUpdateScore(scoreboardscore));
      }

      return arraylist;
   }

   public void addObjective(ScoreObjective scoreboardobjective) {
      List list = this.getCreatePackets(scoreboardobjective);

      for(EntityPlayerMP entityplayer : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         if (entityplayer.getBukkitEntity().getScoreboard().getHandle() == this) {
            for(Packet packet : list) {
               entityplayer.connection.sendPacket(packet);
            }
         }
      }

      this.addedObjectives.add(scoreboardobjective);
   }

   public List getDestroyPackets(ScoreObjective scoreboardobjective) {
      ArrayList arraylist = Lists.newArrayList();
      arraylist.add(new SPacketScoreboardObjective(scoreboardobjective, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveInDisplaySlot(i) == scoreboardobjective) {
            arraylist.add(new SPacketDisplayObjective(i, scoreboardobjective));
         }
      }

      return arraylist;
   }

   public void sendDisplaySlotRemovalPackets(ScoreObjective scoreboardobjective) {
      List list = this.getDestroyPackets(scoreboardobjective);

      for(EntityPlayerMP entityplayer : this.scoreboardMCServer.getPlayerList().getPlayers()) {
         if (entityplayer.getBukkitEntity().getScoreboard().getHandle() == this) {
            for(Packet packet : list) {
               entityplayer.connection.sendPacket(packet);
            }
         }
      }

      this.addedObjectives.remove(scoreboardobjective);
   }

   public int getObjectiveDisplaySlotCount(ScoreObjective scoreboardobjective) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getObjectiveInDisplaySlot(j) == scoreboardobjective) {
            ++i;
         }
      }

      return i;
   }

   private void sendAll(Packet packet) {
      for(EntityPlayerMP entityplayer : this.scoreboardMCServer.getPlayerList().playerEntityList) {
         if (entityplayer.getBukkitEntity().getScoreboard().getHandle() == this) {
            entityplayer.connection.sendPacket(packet);
         }
      }

   }
}
