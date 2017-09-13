package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteriaStat;
import net.minecraft.util.IJsonSerializable;
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
         return StatBase.numberFormat.format((long)number);
      }
   };
   private static final DecimalFormat decimalFormat = new DecimalFormat("########0.00");
   public static IStatType timeStatType = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         double d0 = (double)number / 20.0D;
         double d1 = d0 / 60.0D;
         double d2 = d1 / 60.0D;
         double d3 = d2 / 24.0D;
         double d4 = d3 / 365.0D;
         return d4 > 0.5D ? StatBase.decimalFormat.format(d4) + " y" : (d3 > 0.5D ? StatBase.decimalFormat.format(d3) + " d" : (d2 > 0.5D ? StatBase.decimalFormat.format(d2) + " h" : (d1 > 0.5D ? StatBase.decimalFormat.format(d1) + " m" : d0 + " s")));
      }
   };
   public static IStatType distanceStatType = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         double d0 = (double)number / 100.0D;
         double d1 = d0 / 1000.0D;
         return d1 > 0.5D ? StatBase.decimalFormat.format(d1) + " km" : (d0 > 0.5D ? StatBase.decimalFormat.format(d0) + " m" : number + " cm");
      }
   };
   public static IStatType divideByTen = new IStatType() {
      @SideOnly(Side.CLIENT)
      public String format(int var1) {
         return StatBase.decimalFormat.format((double)number * 0.1D);
      }
   };

   public StatBase(String var1, ITextComponent var2, IStatType var3) {
      this.statId = statIdIn;
      this.statName = statNameIn;
      this.formatter = formatterIn;
      this.objectiveCriteria = new ScoreCriteriaStat(this);
      IScoreCriteria.INSTANCES.put(this.objectiveCriteria.getName(), this.objectiveCriteria);
   }

   public StatBase(String var1, ITextComponent var2) {
      this(statIdIn, statNameIn, simpleStatType);
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
      return this.formatter.format(number);
   }

   public ITextComponent getStatName() {
      ITextComponent itextcomponent = this.statName.createCopy();
      itextcomponent.getStyle().setColor(TextFormatting.GRAY);
      itextcomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponentString(this.statId)));
      return itextcomponent;
   }

   public ITextComponent createChatComponent() {
      ITextComponent itextcomponent = this.getStatName();
      ITextComponent itextcomponent1 = (new TextComponentString("[")).appendSibling(itextcomponent).appendText("]");
      itextcomponent1.setStyle(itextcomponent.getStyle());
      return itextcomponent1;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         StatBase statbase = (StatBase)p_equals_1_;
         return this.statId.equals(statbase.statId);
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
      this.serializableClazz = clazz;
      return this;
   }
}
