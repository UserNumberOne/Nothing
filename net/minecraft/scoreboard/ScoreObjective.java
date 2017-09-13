package net.minecraft.scoreboard;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ScoreObjective {
   private final Scoreboard theScoreboard;
   private final String name;
   private final IScoreCriteria objectiveCriteria;
   private IScoreCriteria.EnumRenderType renderType;
   private String displayName;

   public ScoreObjective(Scoreboard var1, String var2, IScoreCriteria var3) {
      this.theScoreboard = theScoreboardIn;
      this.name = nameIn;
      this.objectiveCriteria = objectiveCriteriaIn;
      this.displayName = nameIn;
      this.renderType = objectiveCriteriaIn.getRenderType();
   }

   @SideOnly(Side.CLIENT)
   public Scoreboard getScoreboard() {
      return this.theScoreboard;
   }

   public String getName() {
      return this.name;
   }

   public IScoreCriteria getCriteria() {
      return this.objectiveCriteria;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String var1) {
      this.displayName = nameIn;
      this.theScoreboard.onObjectiveDisplayNameChanged(this);
   }

   public IScoreCriteria.EnumRenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderType(IScoreCriteria.EnumRenderType var1) {
      this.renderType = type;
      this.theScoreboard.onObjectiveDisplayNameChanged(this);
   }
}
