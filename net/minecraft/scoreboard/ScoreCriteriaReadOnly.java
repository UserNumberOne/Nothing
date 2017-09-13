package net.minecraft.scoreboard;

public class ScoreCriteriaReadOnly extends ScoreCriteria {
   public ScoreCriteriaReadOnly(String var1) {
      super(name);
   }

   public boolean isReadOnly() {
      return true;
   }
}
