package net.minecraft.scoreboard;

public class ScoreCriteria implements IScoreCriteria {
   private final String dummyName;

   public ScoreCriteria(String var1) {
      this.dummyName = var1;
      IScoreCriteria.INSTANCES.put(var1, this);
   }

   public String getName() {
      return this.dummyName;
   }

   public boolean isReadOnly() {
      return false;
   }

   public IScoreCriteria.EnumRenderType getRenderType() {
      return IScoreCriteria.EnumRenderType.INTEGER;
   }
}
