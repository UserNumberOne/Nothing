package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
   public ScoreObjective getObjective(String name) {
      return (ScoreObjective)this.scoreObjectives.get(name);
   }

   public ScoreObjective addScoreObjective(String name, IScoreCriteria criteria) {
      if (name.length() > 16) {
         throw new IllegalArgumentException("The objective name '" + name + "' is too long!");
      } else {
         ScoreObjective scoreobjective = this.getObjective(name);
         if (scoreobjective != null) {
            throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
         } else {
            scoreobjective = new ScoreObjective(this, name, criteria);
            List list = (List)this.scoreObjectiveCriterias.get(criteria);
            if (list == null) {
               list = Lists.newArrayList();
               this.scoreObjectiveCriterias.put(criteria, list);
            }

            list.add(scoreobjective);
            this.scoreObjectives.put(name, scoreobjective);
            this.onScoreObjectiveAdded(scoreobjective);
            return scoreobjective;
         }
      }
   }

   public Collection getObjectivesFromCriteria(IScoreCriteria criteria) {
      Collection collection = (Collection)this.scoreObjectiveCriterias.get(criteria);
      return collection == null ? Lists.newArrayList() : Lists.newArrayList(collection);
   }

   public boolean entityHasObjective(String name, ScoreObjective objective) {
      Map map = (Map)this.entitiesScoreObjectives.get(name);
      if (map == null) {
         return false;
      } else {
         Score score = (Score)map.get(objective);
         return score != null;
      }
   }

   public Score getOrCreateScore(String username, ScoreObjective objective) {
      if (username.length() > 40) {
         throw new IllegalArgumentException("The player name '" + username + "' is too long!");
      } else {
         Map map = (Map)this.entitiesScoreObjectives.get(username);
         if (map == null) {
            map = Maps.newHashMap();
            this.entitiesScoreObjectives.put(username, map);
         }

         Score score = (Score)map.get(objective);
         if (score == null) {
            score = new Score(this, objective, username);
            map.put(objective, score);
         }

         return score;
      }
   }

   public Collection getSortedScores(ScoreObjective objective) {
      List list = Lists.newArrayList();

      for(Map map : this.entitiesScoreObjectives.values()) {
         Score score = (Score)map.get(objective);
         if (score != null) {
            list.add(score);
         }
      }

      Collections.sort(list, Score.SCORE_COMPARATOR);
      return list;
   }

   public Collection getScoreObjectives() {
      return this.scoreObjectives.values();
   }

   public Collection getObjectiveNames() {
      return this.entitiesScoreObjectives.keySet();
   }

   public void removeObjectiveFromEntity(String name, ScoreObjective objective) {
      if (objective == null) {
         Map map = (Map)this.entitiesScoreObjectives.remove(name);
         if (map != null) {
            this.broadcastScoreUpdate(name);
         }
      } else {
         Map map2 = (Map)this.entitiesScoreObjectives.get(name);
         if (map2 != null) {
            Score score = (Score)map2.remove(objective);
            if (map2.size() < 1) {
               Map map1 = (Map)this.entitiesScoreObjectives.remove(name);
               if (map1 != null) {
                  this.broadcastScoreUpdate(name);
               }
            } else if (score != null) {
               this.broadcastScoreUpdate(name, objective);
            }
         }
      }

   }

   public Collection getScores() {
      Collection collection = this.entitiesScoreObjectives.values();
      List list = Lists.newArrayList();

      for(Map map : collection) {
         list.addAll(map.values());
      }

      return list;
   }

   public Map getObjectivesForEntity(String name) {
      Map map = (Map)this.entitiesScoreObjectives.get(name);
      if (map == null) {
         map = Maps.newHashMap();
      }

      return map;
   }

   public void removeObjective(ScoreObjective objective) {
      this.scoreObjectives.remove(objective.getName());

      for(int i = 0; i < 19; ++i) {
         if (this.getObjectiveInDisplaySlot(i) == objective) {
            this.setObjectiveInDisplaySlot(i, (ScoreObjective)null);
         }
      }

      List list = (List)this.scoreObjectiveCriterias.get(objective.getCriteria());
      if (list != null) {
         list.remove(objective);
      }

      for(Map map : this.entitiesScoreObjectives.values()) {
         map.remove(objective);
      }

      this.onScoreObjectiveRemoved(objective);
   }

   public void setObjectiveInDisplaySlot(int objectiveSlot, ScoreObjective objective) {
      this.objectiveDisplaySlots[objectiveSlot] = objective;
   }

   @Nullable
   public ScoreObjective getObjectiveInDisplaySlot(int slotIn) {
      return this.objectiveDisplaySlots[slotIn];
   }

   public ScorePlayerTeam getTeam(String teamName) {
      return (ScorePlayerTeam)this.teams.get(teamName);
   }

   public ScorePlayerTeam createTeam(String name) {
      if (name.length() > 16) {
         throw new IllegalArgumentException("The team name '" + name + "' is too long!");
      } else {
         ScorePlayerTeam scoreplayerteam = this.getTeam(name);
         if (scoreplayerteam != null) {
            throw new IllegalArgumentException("A team with the name '" + name + "' already exists!");
         } else {
            scoreplayerteam = new ScorePlayerTeam(this, name);
            this.teams.put(name, scoreplayerteam);
            this.broadcastTeamCreated(scoreplayerteam);
            return scoreplayerteam;
         }
      }
   }

   public void removeTeam(ScorePlayerTeam playerTeam) {
      this.teams.remove(playerTeam.getRegisteredName());

      for(String s : playerTeam.getMembershipCollection()) {
         this.teamMemberships.remove(s);
      }

      this.broadcastTeamRemove(playerTeam);
   }

   public boolean addPlayerToTeam(String player, String newTeam) {
      if (player.length() > 40) {
         throw new IllegalArgumentException("The player name '" + player + "' is too long!");
      } else if (!this.teams.containsKey(newTeam)) {
         return false;
      } else {
         ScorePlayerTeam scoreplayerteam = this.getTeam(newTeam);
         if (this.getPlayersTeam(player) != null) {
            this.removePlayerFromTeams(player);
         }

         this.teamMemberships.put(player, scoreplayerteam);
         scoreplayerteam.getMembershipCollection().add(player);
         return true;
      }
   }

   public boolean removePlayerFromTeams(String playerName) {
      ScorePlayerTeam scoreplayerteam = this.getPlayersTeam(playerName);
      if (scoreplayerteam != null) {
         this.removePlayerFromTeam(playerName, scoreplayerteam);
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String username, ScorePlayerTeam playerTeam) {
      if (this.getPlayersTeam(username) != playerTeam) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getRegisteredName() + "'.");
      } else {
         this.teamMemberships.remove(username);
         playerTeam.getMembershipCollection().remove(username);
      }
   }

   public Collection getTeamNames() {
      return this.teams.keySet();
   }

   public Collection getTeams() {
      return this.teams.values();
   }

   @Nullable
   public ScorePlayerTeam getPlayersTeam(String username) {
      return (ScorePlayerTeam)this.teamMemberships.get(username);
   }

   public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn) {
   }

   public void onObjectiveDisplayNameChanged(ScoreObjective objective) {
   }

   public void onScoreObjectiveRemoved(ScoreObjective objective) {
   }

   public void onScoreUpdated(Score scoreIn) {
   }

   public void broadcastScoreUpdate(String scoreName) {
   }

   public void broadcastScoreUpdate(String scoreName, ScoreObjective objective) {
   }

   public void broadcastTeamCreated(ScorePlayerTeam playerTeam) {
   }

   public void broadcastTeamInfoUpdate(ScorePlayerTeam playerTeam) {
   }

   public void broadcastTeamRemove(ScorePlayerTeam playerTeam) {
   }

   public static String getObjectiveDisplaySlot(int id) {
      switch(id) {
      case 0:
         return "list";
      case 1:
         return "sidebar";
      case 2:
         return "belowName";
      default:
         if (id >= 3 && id <= 18) {
            TextFormatting textformatting = TextFormatting.fromColorIndex(id - 3);
            if (textformatting != null && textformatting != TextFormatting.RESET) {
               return "sidebar.team." + textformatting.getFriendlyName();
            }
         }

         return null;
      }
   }

   public static int getObjectiveDisplaySlotNumber(String name) {
      if ("list".equalsIgnoreCase(name)) {
         return 0;
      } else if ("sidebar".equalsIgnoreCase(name)) {
         return 1;
      } else if ("belowName".equalsIgnoreCase(name)) {
         return 2;
      } else {
         if (name.startsWith("sidebar.team.")) {
            String s = name.substring("sidebar.team.".length());
            TextFormatting textformatting = TextFormatting.getValueByName(s);
            if (textformatting != null && textformatting.getColorIndex() >= 0) {
               return textformatting.getColorIndex() + 3;
            }
         }

         return -1;
      }
   }

   public static String[] getDisplaySlotStrings() {
      if (displaySlots == null) {
         displaySlots = new String[19];

         for(int i = 0; i < 19; ++i) {
            displaySlots[i] = getObjectiveDisplaySlot(i);
         }
      }

      return displaySlots;
   }

   public void removeEntity(Entity entityIn) {
      if (entityIn != null && !(entityIn instanceof EntityPlayer) && !entityIn.isEntityAlive()) {
         String s = entityIn.getCachedUniqueIdString();
         this.removeObjectiveFromEntity(s, (ScoreObjective)null);
         this.removePlayerFromTeams(s);
      }

   }
}
