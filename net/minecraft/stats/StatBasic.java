package net.minecraft.stats;

import net.minecraft.util.text.ITextComponent;

public class StatBasic extends StatBase {
   public StatBasic(String var1, ITextComponent var2, IStatType var3) {
      super(statIdIn, statNameIn, typeIn);
   }

   public StatBasic(String var1, ITextComponent var2) {
      super(statIdIn, statNameIn);
   }

   public StatBase registerStat() {
      super.registerStat();
      StatList.BASIC_STATS.add(this);
      return this;
   }
}
