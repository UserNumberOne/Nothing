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
      this.theScoreboard = var1;
      this.registeredName = var2;
      this.teamNameSPT = var2;
   }

   public String getRegisteredName() {
      return this.registeredName;
   }

   public String getTeamName() {
      return this.teamNameSPT;
   }

   public void setTeamName(String var1) {
      if (var1 == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.teamNameSPT = var1;
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
      if (var1 == null) {
         throw new IllegalArgumentException("Prefix cannot be null");
      } else {
         this.namePrefixSPT = var1;
         this.theScoreboard.broadcastTeamInfoUpdate(this);
      }
   }

   public String getColorSuffix() {
      return this.colorSuffix;
   }

   public void setNameSuffix(String var1) {
      this.colorSuffix = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public String formatString(String var1) {
      return this.getColorPrefix() + var1 + this.getColorSuffix();
   }

   public static String formatPlayerName(@Nullable Team var0, String var1) {
      return var0 == null ? var1 : var0.formatString(var1);
   }

   public boolean getAllowFriendlyFire() {
      return this.allowFriendlyFire;
   }

   public void setAllowFriendlyFire(boolean var1) {
      this.allowFriendlyFire = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public boolean getSeeFriendlyInvisiblesEnabled() {
      return this.canSeeFriendlyInvisibles;
   }

   public void setSeeFriendlyInvisiblesEnabled(boolean var1) {
      this.canSeeFriendlyInvisibles = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public Team.EnumVisible getNameTagVisibility() {
      return this.nameTagVisibility;
   }

   public Team.EnumVisible getDeathMessageVisibility() {
      return this.deathMessageVisibility;
   }

   public void setNameTagVisibility(Team.EnumVisible var1) {
      this.nameTagVisibility = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public void setDeathMessageVisibility(Team.EnumVisible var1) {
      this.deathMessageVisibility = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public Team.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   public void setCollisionRule(Team.CollisionRule var1) {
      this.collisionRule = var1;
      this.theScoreboard.broadcastTeamInfoUpdate(this);
   }

   public int getFriendlyFlags() {
      int var1 = 0;
      if (this.getAllowFriendlyFire()) {
         var1 |= 1;
      }

      if (this.getSeeFriendlyInvisiblesEnabled()) {
         var1 |= 2;
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   public void setFriendlyFlags(int var1) {
      this.setAllowFriendlyFire((var1 & 1) > 0);
      this.setSeeFriendlyInvisiblesEnabled((var1 & 2) > 0);
   }

   public void setChatFormat(TextFormatting var1) {
      this.chatFormat = var1;
   }

   public TextFormatting getChatFormat() {
      return this.chatFormat;
   }
}
