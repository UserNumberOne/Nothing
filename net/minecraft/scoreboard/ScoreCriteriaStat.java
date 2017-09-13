package net.minecraft.scoreboard;

import net.minecraft.stats.StatBase;

public class ScoreCriteriaStat extends ScoreCriteria {
   private final StatBase stat;

   public ScoreCriteriaStat(StatBase var1) {
      super(var1.statId);
      this.stat = var1;
   }
}
