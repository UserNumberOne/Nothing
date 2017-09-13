package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

public class Scoreboard {
   private final Map scoreObjectives = Maps.newHashMap();
   private final Map scoreObjectiveCriterias = Maps.newHashMap();
   private final Map entitiesScoreObjectives = Maps.newHashMap();
   private final ScoreObjective[] objectiveDisplaySlots = new ScoreObjective[19];
   private final Map teams = Maps.newHashMap();
   private final Map teamMemberships = Maps.newHashMap();
   private static String[] displaySlots;

   @Nullable
   public ScoreObjective getObjective(String var1) {
      return (ScoreObjective)this.scoreObjectives.get(var1);
   }

   public ScoreObjective addScoreObjective(String var1, IScoreCriteria var2) {
      if (var1.length() > 16) {
         throw new IllegalArgumentException("The objective name '" + var1 + "' is too long!");
      } else {
         ScoreObjective var3 = this.getObjective(var1);
         if (var3 != null) {
            throw new IllegalArgumentException("An objective with the name '" + var1 + "' already exists!");
         } else {
            var3 = new ScoreObjective(this, var1, var2);
            Object var4 = (List)this.scoreObjectiveCriterias.get(var2);
            if (var4 == null) {
               var4 = Lists.newArrayList();
               this.scoreObjectiveCriterias.put(var2, var4);
            }

            ((List)var4).add(var3);
            this.scoreObjectives.put(var1, var3);
            this.onScoreObjectiveAdded(var3);
            return var3;
         }
      }
   }

   public Collection getObjectivesFromCriteria(IScoreCriteria var1) {
      Collection var2 = (Collection)this.scoreObjectiveCriterias.get(var1);
      return var2 == null ? Lists.newArrayList() : Lists.newArrayList(var2);
   }

   public boolean entityHasObjective(String var1, ScoreObjective var2) {
      Map var3 = (Map)this.entitiesScoreObjectives.get(var1);
      if (var3 == null) {
         return false;
      } else {
         Score var4 = (Score)var3.get(var2);
         return var4 != null;
      }
   }

   public Score getOrCreateScore(String var1, ScoreObjective var2) {
      if (var1.length() > 40) {
         throw new IllegalArgumentException("The player name '" + var1 + "' is too long!");
      } else {
         Object var3 = (Map)this.entitiesScoreObjectives.get(var1);
         if (var3 == null) {
            var3 = Maps.newHashMap();
            this.entitiesScoreObjectives.put(var1, var3);
         }

         Score var4 = (Score)((Map)var3).get(var2);
         if (var4 == null) {
            var4 = new Score(this, var2, var1);
            ((Map)var3).put(var2, var4);
         }

         return var4;
      }
   }

   public Collection getSortedScores(ScoreObjective var1) {
      ArrayList var2 = Lists.newArrayList();

      for(Map var4 : this.entitiesScoreObjectives.values()) {
         Score var5 = (Score)var4.get(var1);
         if (var5 != null) {
            var2.add(var5);
         }
      }

      Collections.sort(var2, Score.SCORE_COMPARATOR);
      return var2;
   }

   public Collection getScoreObjectives() {
      return this.scoreObjectives.values();
   }

   public Collection getObjectiveNames() {
      return this.entitiesScoreObjectives.keySet();
   }

   public void removeObjectiveFromEntity(String var1, ScoreObjective var2) {
      if (var2 == null) {
         Map var3 = (Map)this.entitiesScoreObjectives.remove(var1);
         if (var3 != null) {
            this.broadcastScoreUpdate(var1);
         }
      } else {
         Map var6 = (Map)this.entitiesScoreObjectives.get(var1);
         if (var6 != null) {
            Score var4 = (Score)var6.remove(var2);
            if (var6.size() < 1) {
               Map var5 = (Map)this.entitiesScoreObjectives.remove(var1);
               if (var5 != null) {
                  this.broadcastScoreUpdate(var1);
               }
            } else if (var4 != null) {
               this.broadcastScoreUpdate(var1, var2);
            }
         }
      }

   }

   public Collection getScores() {
      Collection var1 = this.entitiesScoreObjectives.values();
      ArrayList var2 = Lists.newArrayList();

      for(Map var4 : var1) {
         var2.addAll(var4.values());
      }

      return var2;
   }

   public Map getObjectivesForEntity(String var1) {
      Object var2 = (Map)this.entitiesScoreObjectives.get(var1);
      if (var2 == null) {
         var2 = Maps.newHashMap();
      }

      return (Map)var2;
   }

   public void removeObjective(ScoreObjective var1) {
      this.scoreObjectives.remove(var1.getName());

      for(int var2 = 0; var2 < 19; ++var2) {
         if (this.getObjectiveInDisplaySlot(var2) == var1) {
            this.setObjectiveInDisplaySlot(var2, (ScoreObjective)null);
         }
      }

      List var5 = (List)this.scoreObjectiveCriterias.get(var1.getCriteria());
      if (var5 != null) {
         var5.remove(var1);
      }

      for(Map var4 : this.entitiesScoreObjectives.values()) {
         var4.remove(var1);
      }

      this.onScoreObjectiveRemoved(var1);
   }

   public void setObjectiveInDisplaySlot(int var1, ScoreObjective var2) {
      this.objectiveDisplaySlots[var1] = var2;
   }

   @Nullable
   public ScoreObjective getObjectiveInDisplaySlot(int var1) {
      return this.objectiveDisplaySlots[var1];
   }

   public ScorePlayerTeam getTeam(String var1) {
      return (ScorePlayerTeam)this.teams.get(var1);
   }

   public ScorePlayerTeam createTeam(String var1) {
      if (var1.length() > 16) {
         throw new IllegalArgumentException("The team name '" + var1 + "' is too long!");
      } else {
         ScorePlayerTeam var2 = this.getTeam(var1);
         if (var2 != null) {
            throw new IllegalArgumentException("A team with the name '" + var1 + "' already exists!");
         } else {
            var2 = new ScorePlayerTeam(this, var1);
            this.teams.put(var1, var2);
            this.broadcastTeamCreated(var2);
            return var2;
         }
      }
   }

   public void removeTeam(ScorePlayerTeam var1) {
      this.teams.remove(var1.getRegisteredName());

      for(String var3 : var1.getMembershipCollection()) {
         this.teamMemberships.remove(var3);
      }

      this.broadcastTeamRemove(var1);
   }

   public boolean addPlayerToTeam(String var1, String var2) {
      if (var1.length() > 40) {
         throw new IllegalArgumentException("The player name '" + var1 + "' is too long!");
      } else if (!this.teams.containsKey(var2)) {
         return false;
      } else {
         ScorePlayerTeam var3 = this.getTeam(var2);
         if (this.getPlayersTeam(var1) != null) {
            this.removePlayerFromTeams(var1);
         }

         this.teamMemberships.put(var1, var3);
         var3.getMembershipCollection().add(var1);
         return true;
      }
   }

   public boolean removePlayerFromTeams(String var1) {
      ScorePlayerTeam var2 = this.getPlayersTeam(var1);
      if (var2 != null) {
         this.removePlayerFromTeam(var1, var2);
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String var1, ScorePlayerTeam var2) {
      if (this.getPlayersTeam(var1) != var2) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + var2.getRegisteredName() + "'.");
      } else {
         this.teamMemberships.remove(var1);
         var2.getMembershipCollection().remove(var1);
      }
   }

   public Collection getTeamNames() {
      return this.teams.keySet();
   }

   public Collection getTeams() {
      return this.teams.values();
   }

   @Nullable
   public ScorePlayerTeam getPlayersTeam(String var1) {
      return (ScorePlayerTeam)this.teamMemberships.get(var1);
   }

   public void onScoreObjectiveAdded(ScoreObjective var1) {
   }

   public void onObjectiveDisplayNameChanged(ScoreObjective var1) {
   }

   public void onScoreObjectiveRemoved(ScoreObjective var1) {
   }

   public void onScoreUpdated(Score var1) {
   }

   public void broadcastScoreUpdate(String var1) {
   }

   public void broadcastScoreUpdate(String var1, ScoreObjective var2) {
   }

   public void broadcastTeamCreated(ScorePlayerTeam var1) {
   }

   public void broadcastTeamInfoUpdate(ScorePlayerTeam var1) {
   }

   public void broadcastTeamRemove(ScorePlayerTeam var1) {
   }

   public static String getObjectiveDisplaySlot(int var0) {
      switch(var0) {
      case 0:
         return "list";
      case 1:
         return "sidebar";
      case 2:
         return "belowName";
      default:
         if (var0 >= 3 && var0 <= 18) {
            TextFormatting var1 = TextFormatting.fromColorIndex(var0 - 3);
            if (var1 != null && var1 != TextFormatting.RESET) {
               return "sidebar.team." + var1.getFriendlyName();
            }
         }

         return null;
      }
   }

   public static int getObjectiveDisplaySlotNumber(String var0) {
      if ("list".equalsIgnoreCase(var0)) {
         return 0;
      } else if ("sidebar".equalsIgnoreCase(var0)) {
         return 1;
      } else if ("belowName".equalsIgnoreCase(var0)) {
         return 2;
      } else {
         if (var0.startsWith("sidebar.team.")) {
            String var1 = var0.substring("sidebar.team.".length());
            TextFormatting var2 = TextFormatting.getValueByName(var1);
            if (var2 != null && var2.getColorIndex() >= 0) {
               return var2.getColorIndex() + 3;
            }
         }

         return -1;
      }
   }

   public static String[] getDisplaySlotStrings() {
      if (displaySlots == null) {
         displaySlots = new String[19];

         for(int var0 = 0; var0 < 19; ++var0) {
            displaySlots[var0] = getObjectiveDisplaySlot(var0);
         }
      }

      return displaySlots;
   }

   public void removeEntity(Entity var1) {
      if (var1 != null && !(var1 instanceof EntityPlayer) && !var1.isEntityAlive()) {
         String var2 = var1.getCachedUniqueIdString();
         this.removeObjectiveFromEntity(var2, (ScoreObjective)null);
         this.removePlayerFromTeams(var2);
      }
   }
}
