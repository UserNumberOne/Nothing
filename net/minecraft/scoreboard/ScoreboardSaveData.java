package net.minecraft.scoreboard;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreboardSaveData extends WorldSavedData {
   private static final Logger LOGGER = LogManager.getLogger();
   private Scoreboard theScoreboard;
   private NBTTagCompound delayedInitNbt;

   public ScoreboardSaveData() {
      this("scoreboard");
   }

   public ScoreboardSaveData(String var1) {
      super(var1);
   }

   public void setScoreboard(Scoreboard var1) {
      this.theScoreboard = var1;
      if (this.delayedInitNbt != null) {
         this.readFromNBT(this.delayedInitNbt);
      }

   }

   public void readFromNBT(NBTTagCompound var1) {
      if (this.theScoreboard == null) {
         this.delayedInitNbt = var1;
      } else {
         this.readObjectives(var1.getTagList("Objectives", 10));
         this.readScores(var1.getTagList("PlayerScores", 10));
         if (var1.hasKey("DisplaySlots", 10)) {
            this.readDisplayConfig(var1.getCompoundTag("DisplaySlots"));
         }

         if (var1.hasKey("Teams", 9)) {
            this.readTeams(var1.getTagList("Teams", 10));
         }

      }
   }

   protected void readTeams(NBTTagList var1) {
      for(int var2 = 0; var2 < var1.tagCount(); ++var2) {
         NBTTagCompound var3 = var1.getCompoundTagAt(var2);
         String var4 = var3.getString("Name");
         if (var4.length() > 16) {
            var4 = var4.substring(0, 16);
         }

         ScorePlayerTeam var5 = this.theScoreboard.createTeam(var4);
         String var6 = var3.getString("DisplayName");
         if (var6.length() > 32) {
            var6 = var6.substring(0, 32);
         }

         var5.setTeamName(var6);
         if (var3.hasKey("TeamColor", 8)) {
            var5.setChatFormat(TextFormatting.getValueByName(var3.getString("TeamColor")));
         }

         var5.setNamePrefix(var3.getString("Prefix"));
         var5.setNameSuffix(var3.getString("Suffix"));
         if (var3.hasKey("AllowFriendlyFire", 99)) {
            var5.setAllowFriendlyFire(var3.getBoolean("AllowFriendlyFire"));
         }

         if (var3.hasKey("SeeFriendlyInvisibles", 99)) {
            var5.setSeeFriendlyInvisiblesEnabled(var3.getBoolean("SeeFriendlyInvisibles"));
         }

         if (var3.hasKey("NameTagVisibility", 8)) {
            Team.EnumVisible var7 = Team.EnumVisible.getByName(var3.getString("NameTagVisibility"));
            if (var7 != null) {
               var5.setNameTagVisibility(var7);
            }
         }

         if (var3.hasKey("DeathMessageVisibility", 8)) {
            Team.EnumVisible var8 = Team.EnumVisible.getByName(var3.getString("DeathMessageVisibility"));
            if (var8 != null) {
               var5.setDeathMessageVisibility(var8);
            }
         }

         if (var3.hasKey("CollisionRule", 8)) {
            Team.CollisionRule var9 = Team.CollisionRule.getByName(var3.getString("CollisionRule"));
            if (var9 != null) {
               var5.setCollisionRule(var9);
            }
         }

         this.loadTeamPlayers(var5, var3.getTagList("Players", 8));
      }

   }

   protected void loadTeamPlayers(ScorePlayerTeam var1, NBTTagList var2) {
      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         this.theScoreboard.addPlayerToTeam(var2.getStringTagAt(var3), var1.getRegisteredName());
      }

   }

   protected void readDisplayConfig(NBTTagCompound var1) {
      for(int var2 = 0; var2 < 19; ++var2) {
         if (var1.hasKey("slot_" + var2, 8)) {
            String var3 = var1.getString("slot_" + var2);
            ScoreObjective var4 = this.theScoreboard.getObjective(var3);
            this.theScoreboard.setObjectiveInDisplaySlot(var2, var4);
         }
      }

   }

   protected void readObjectives(NBTTagList var1) {
      for(int var2 = 0; var2 < var1.tagCount(); ++var2) {
         NBTTagCompound var3 = var1.getCompoundTagAt(var2);
         IScoreCriteria var4 = (IScoreCriteria)IScoreCriteria.INSTANCES.get(var3.getString("CriteriaName"));
         if (var4 != null) {
            String var5 = var3.getString("Name");
            if (var5.length() > 16) {
               var5 = var5.substring(0, 16);
            }

            ScoreObjective var6 = this.theScoreboard.addScoreObjective(var5, var4);
            var6.setDisplayName(var3.getString("DisplayName"));
            var6.setRenderType(IScoreCriteria.EnumRenderType.getByName(var3.getString("RenderType")));
         }
      }

   }

   protected void readScores(NBTTagList var1) {
      for(int var2 = 0; var2 < var1.tagCount(); ++var2) {
         NBTTagCompound var3 = var1.getCompoundTagAt(var2);
         ScoreObjective var4 = this.theScoreboard.getObjective(var3.getString("Objective"));
         String var5 = var3.getString("Name");
         if (var5.length() > 40) {
            var5 = var5.substring(0, 40);
         }

         Score var6 = this.theScoreboard.getOrCreateScore(var5, var4);
         var6.setScorePoints(var3.getInteger("Score"));
         if (var3.hasKey("Locked")) {
            var6.setLocked(var3.getBoolean("Locked"));
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      if (this.theScoreboard == null) {
         LOGGER.warn("Tried to save scoreboard without having a scoreboard...");
         return var1;
      } else {
         var1.setTag("Objectives", this.objectivesToNbt());
         var1.setTag("PlayerScores", this.scoresToNbt());
         var1.setTag("Teams", this.teamsToNbt());
         this.fillInDisplaySlots(var1);
         return var1;
      }
   }

   protected NBTTagList teamsToNbt() {
      NBTTagList var1 = new NBTTagList();

      for(ScorePlayerTeam var4 : this.theScoreboard.getTeams()) {
         NBTTagCompound var5 = new NBTTagCompound();
         var5.setString("Name", var4.getRegisteredName());
         var5.setString("DisplayName", var4.getTeamName());
         if (var4.getChatFormat().getColorIndex() >= 0) {
            var5.setString("TeamColor", var4.getChatFormat().getFriendlyName());
         }

         var5.setString("Prefix", var4.getColorPrefix());
         var5.setString("Suffix", var4.getColorSuffix());
         var5.setBoolean("AllowFriendlyFire", var4.getAllowFriendlyFire());
         var5.setBoolean("SeeFriendlyInvisibles", var4.getSeeFriendlyInvisiblesEnabled());
         var5.setString("NameTagVisibility", var4.getNameTagVisibility().internalName);
         var5.setString("DeathMessageVisibility", var4.getDeathMessageVisibility().internalName);
         var5.setString("CollisionRule", var4.getCollisionRule().name);
         NBTTagList var6 = new NBTTagList();

         for(String var8 : var4.getMembershipCollection()) {
            var6.appendTag(new NBTTagString(var8));
         }

         var5.setTag("Players", var6);
         var1.appendTag(var5);
      }

      return var1;
   }

   protected void fillInDisplaySlots(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      boolean var3 = false;

      for(int var4 = 0; var4 < 19; ++var4) {
         ScoreObjective var5 = this.theScoreboard.getObjectiveInDisplaySlot(var4);
         if (var5 != null) {
            var2.setString("slot_" + var4, var5.getName());
            var3 = true;
         }
      }

      if (var3) {
         var1.setTag("DisplaySlots", var2);
      }

   }

   protected NBTTagList objectivesToNbt() {
      NBTTagList var1 = new NBTTagList();

      for(ScoreObjective var4 : this.theScoreboard.getScoreObjectives()) {
         if (var4.getCriteria() != null) {
            NBTTagCompound var5 = new NBTTagCompound();
            var5.setString("Name", var4.getName());
            var5.setString("CriteriaName", var4.getCriteria().getName());
            var5.setString("DisplayName", var4.getDisplayName());
            var5.setString("RenderType", var4.getRenderType().getRenderType());
            var1.appendTag(var5);
         }
      }

      return var1;
   }

   protected NBTTagList scoresToNbt() {
      NBTTagList var1 = new NBTTagList();

      for(Score var4 : this.theScoreboard.getScores()) {
         if (var4.getObjective() != null) {
            NBTTagCompound var5 = new NBTTagCompound();
            var5.setString("Name", var4.getPlayerName());
            var5.setString("Objective", var4.getObjective().getName());
            var5.setInteger("Score", var4.getScorePoints());
            var5.setBoolean("Locked", var4.isLocked());
            var1.appendTag(var5);
         }
      }

      return var1;
   }
}
