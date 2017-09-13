package net.minecraft.scoreboard;

public class ScoreCriteriaHealth extends ScoreCriteria {
   public ScoreCriteriaHealth(String var1) {
      super(var1);
   }

   public boolean isReadOnly() {
      return true;
   }

   public IScoreCriteria.EnumRenderType getRenderType() {
      return IScoreCriteria.EnumRenderType.HEARTS;
   }
}
