package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteriaStat;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StatBase {
   public final String statId;
   private final ITextComponent statName;
   public boolean isIndependent;
   private final IStatType formatter;
   private final IScoreCriteria objectiveCriteria;
   private Class serializableClazz;
   private static final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
   public static IStatType simpleStatType = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         return StatBase.numberFormat.format((long)var1);
      }
   };
   private static final DecimalFormat decimalFormat = new DecimalFormat("########0.00");
   public static IStatType timeStatType = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         double var2 = (double)var1 / 20.0D;
         double var4 = var2 / 60.0D;
         double var6 = var4 / 60.0D;
         double var8 = var6 / 24.0D;
         double var10 = var8 / 365.0D;
         return var10 > 0.5D ? StatBase.decimalFormat.format(var10) + " y" : (var8 > 0.5D ? StatBase.decimalFormat.format(var8) + " d" : (var6 > 0.5D ? StatBase.decimalFormat.format(var6) + " h" : (var4 > 0.5D ? StatBase.decimalFormat.format(var4) + " m" : var2 + " s")));
      }
   };
   public static IStatType distanceStatType = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         double var2 = (double)var1 / 100.0D;
         double var4 = var2 / 1000.0D;
         return var4 > 0.5D ? StatBase.decimalFormat.format(var4) + " km" : (var2 > 0.5D ? StatBase.decimalFormat.format(var2) + " m" : var1 + " cm");
      }
   };
   public static IStatType divideByTen = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         return StatBase.decimalFormat.format((double)var1 * 0.1D);
      }
   };

   public StatBase(String var1, ITextComponent var2, IStatType var3) {
      this.statId = var1;
      this.statName = var2;
      this.formatter = var3;
      this.objectiveCriteria = new ScoreCriteriaStat(this);
      IScoreCriteria.INSTANCES.put(this.objectiveCriteria.getName(), this.objectiveCriteria);
   }

   public StatBase(String var1, ITextComponent var2) {
      this(var1, var2, simpleStatType);
   }

   public StatBase initIndependentStat() {
      this.isIndependent = true;
      return this;
   }

   public StatBase registerStat() {
      if (StatList.ID_TO_STAT_MAP.containsKey(this.statId)) {
         throw new RuntimeException("Duplicate stat id: \"" + ((StatBase)StatList.ID_TO_STAT_MAP.get(this.statId)).statName + "\" and \"" + this.statName + "\" at id " + this.statId);
      } else {
         StatList.ALL_STATS.add(this);
         StatList.ID_TO_STAT_MAP.put(this.statId, this);
         return this;
      }
   }

   public boolean isAchievement() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public String format(int var1) {
      return this.formatter.format(var1);
   }

   public ITextComponent getStatName() {
      ITextComponent var1 = this.statName.createCopy();
      var1.getStyle().setColor(TextFormatting.GRAY);
      var1.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponentString(this.statId)));
      return var1;
   }

   public ITextComponent createChatComponent() {
      ITextComponent var1 = this.getStatName();
      ITextComponent var2 = (new TextComponentString("[")).appendSibling(var1).appendText("]");
      var2.setStyle(var1.getStyle());
      return var2;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         StatBase var2 = (StatBase)var1;
         return this.statId.equals(var2.statId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.statId.hashCode();
   }

   public String toString() {
      return "Stat{id=" + this.statId + ", nameId=" + this.statName + ", awardLocallyOnly=" + this.isIndependent + ", formatter=" + this.formatter + ", objectiveCriteria=" + this.objectiveCriteria + '}';
   }

   public IScoreCriteria getCriteria() {
      return this.objectiveCriteria;
   }

   public Class getSerializableClazz() {
      return this.serializableClazz;
   }

   public StatBase setSerializableClazz(Class var1) {
      this.serializableClazz = var1;
      return this;
   }
}
