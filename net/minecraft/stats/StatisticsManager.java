package net.minecraft.stats;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;

public class StatisticsManager {
   protected final Map statsData = Maps.newConcurrentMap();

   public boolean hasAchievementUnlocked(Achievement achievement) {
      return this.readStat(achievement) > 0;
   }

   public boolean canUnlockAchievement(Achievement achievement) {
      return achievement.parentAchievement == null || this.hasAchievementUnlocked(achievement.parentAchievement);
   }

   public void increaseStat(EntityPlayer entityhuman, StatBase statistic, int i) {
      if (!statistic.isAchievement() || this.canUnlockAchievement((Achievement)statistic)) {
         Cancellable cancellable = CraftEventFactory.handleStatisticsIncrease(entityhuman, statistic, this.readStat(statistic), i);
         if (cancellable != null && cancellable.isCancelled()) {
            return;
         }

         this.unlockAchievement(entityhuman, statistic, this.readStat(statistic) + i);
      }

   }

   public void unlockAchievement(EntityPlayer entityhuman, StatBase statistic, int i) {
      TupleIntJsonSerializable statisticwrapper = (TupleIntJsonSerializable)this.statsData.get(statistic);
      if (statisticwrapper == null) {
         statisticwrapper = new TupleIntJsonSerializable();
         this.statsData.put(statistic, statisticwrapper);
      }

      statisticwrapper.setIntegerValue(i);
   }

   public int readStat(StatBase statistic) {
      TupleIntJsonSerializable statisticwrapper = (TupleIntJsonSerializable)this.statsData.get(statistic);
      return statisticwrapper == null ? 0 : statisticwrapper.getIntegerValue();
   }

   public IJsonSerializable getProgress(StatBase statistic) {
      TupleIntJsonSerializable statisticwrapper = (TupleIntJsonSerializable)this.statsData.get(statistic);
      return statisticwrapper != null ? statisticwrapper.getJsonSerializableValue() : null;
   }

   public IJsonSerializable setProgress(StatBase statistic, IJsonSerializable t0) {
      TupleIntJsonSerializable statisticwrapper = (TupleIntJsonSerializable)this.statsData.get(statistic);
      if (statisticwrapper == null) {
         statisticwrapper = new TupleIntJsonSerializable();
         this.statsData.put(statistic, statisticwrapper);
      }

      statisticwrapper.setJsonSerializableValue(t0);
      return t0;
   }
}
