package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ScorePlayerTeam extends Team {
   private final Scoreboard theScoreboard;
   private final String registeredName;
   private final Set membershipSet = Sets.newHashSet();
   private String teamNameSPT;
   private String namePrefixSPT = "";
   private String colorSuffix = "";
   private boolean allowFriendlyFire = true;
   private boolean canSeeFriendlyInvisibles = true;
   private Team.EnumVisible nameTagVisibility = Team.EnumVisible.ALWAYS;
   private Team.EnumVisible deathMessageVisibility = Team.EnumVisible.ALWAYS;
   private TextFormatting chatFormat = TextFormatting.RESET;
   private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

   public ScorePlayerTeam(Scoreboard var1, String var2) {
      this.theScoreboard = theScoreboardIn;
      this.registeredName = name;
      this.teamNameSPT = name;
   }

   public String getRegisteredName() {
      return this.registeredName;
   }

   public String getTeamName() {
      return this.teamNameSPT;
   }

   public void setTeamName(String var1) {
      if (name == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.teamNameSPT = name;
         this.theScoreboard.broadcastTeamInfoUpdate(this);
      }
   }

   public Collection getMembershipCollection() {
      return this.membershipSet;
   }

   public String getColorPrefix() {
      return this.namePrefixSPT;
   }

   public void setNamePrefix(String var1) {
      if (prefix == null) {
         throw new IllegalArgumentException("Prefix cannot be null");
      } else {
         this.namePrefixSPT = prefix;
         this.theScoreboard.broadcastTeamInfoUpdate(this);
      }
   }

   public String getColorSuffix() {
      return this.colorSuffix;
   }

   public void setNameSuffix(String var1) {
      this.colorSuffix = suffix;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public String formatString(String var1) {
      return this.getColorPrefix() + input + this.getColorSuffix();
   }

   public static String formatPlayerName(@Nullable Team var0, String var1) {
      return teamIn == null ? string : teamIn.formatString(string);
   }

   public boolean getAllowFriendlyFire() {
      return this.allowFriendlyFire;
   }

   public void setAllowFriendlyFire(boolean var1) {
      this.allowFriendlyFire = friendlyFire;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public boolean getSeeFriendlyInvisiblesEnabled() {
      return this.canSeeFriendlyInvisibles;
   }

   public void setSeeFriendlyInvisiblesEnabled(boolean var1) {
      this.canSeeFriendlyInvisibles = friendlyInvisibles;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public Team.EnumVisible getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   public Team.EnumVisible getDeathMessageVisibility() {
      return this.deathMessageVisibility;
   }

   public void setNameTagVisibility(Team.EnumVisible var1) {
      this.nameTagVisibility = visibility;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public void setDeathMessageVisibility(Team.EnumVisible var1) {
      this.deathMessageVisibility = visibility;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public Team.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   public void setCollisionRule(Team.CollisionRule var1) {
      this.collisionRule = rule;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public int getFriendlyFlags() {
      int i = 0;
      if (this.getAllowFriendlyFire()) {
         i |= 1;
      }

      if (this.getSeeFriendlyInvisiblesEnabled()) {
         i |= 2;
      }

      return i;
   }

   @SideOnly(Side.CLIENT)
   public void setFriendlyFlags(int var1) {
      this.setAllowFriendlyFire((flags & 1) > 0);
      this.setSeeFriendlyInvisiblesEnabled((flags & 2) > 0);
   }

   public void setChatFormat(TextFormatting var1) {
      this.chatFormat = format;
   }

   public TextFormatting getChatFormat() {
      return this.chatFormat;
   }
}
