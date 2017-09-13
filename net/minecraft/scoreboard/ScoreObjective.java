package net.minecraft.scoreboard;

public class ScoreObjective {
   private final Scoreboard theScoreboard;
   private final String name;
   private final IScoreCriteria objectiveCriteria;
   private IScoreCriteria.EnumRenderType renderType;
   private String displayName;

   public ScoreObjective(Scoreboard var1, String var2, IScoreCriteria var3) {
      this.theScoreboard = var1;
      this.name = var2;
      this.objectiveCriteria = var3;
      this.displayName = var2;
      this.renderType = var3.getRenderType();
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
      this.displayName = var1;
      this.theScoreboard.onObjectiveDisplayNameChanged(this);
   }

   public IScoreCriteria.EnumRenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderType(IScoreCriteria.EnumRenderType var1) {
      this.renderType = var1;
      this.theScoreboard.onObjectiveDisplayNameChanged(this);
   }
}
