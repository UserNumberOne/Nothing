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

   public boolean hasAchievementUnlocked(Achievement var1) {
      return this.readStat(var1) > 0;
   }

   public boolean canUnlockAchievement(Achievement var1) {
      return var1.parentAchievement == null || this.hasAchievementUnlocked(var1.parentAchievement);
   }

   public void increaseStat(EntityPlayer var1, StatBase var2, int var3) {
      if (!var2.isAchievement() || this.canUnlockAchievement((Achievement)var2)) {
         Cancellable var4 = CraftEventFactory.handleStatisticsIncrease(var1, var2, this.readStat(var2), var3);
         if (var4 != null && var4.isCancelled()) {
            return;
         }

         this.unlockAchievement(var1, var2, this.readStat(var2) + var3);
      }

   }

   public void unlockAchievement(EntityPlayer var1, StatBase var2, int var3) {
      TupleIntJsonSerializable var4 = (TupleIntJsonSerializable)this.statsData.get(var2);
      if (var4 == null) {
         var4 = new TupleIntJsonSerializable();
         this.statsData.put(var2, var4);
      }

      var4.setIntegerValue(var3);
   }

   public int readStat(StatBase var1) {
      TupleIntJsonSerializable var2 = (TupleIntJsonSerializable)this.statsData.get(var1);
      return var2 == null ? 0 : var2.getIntegerValue();
   }

   public IJsonSerializable getProgress(StatBase var1) {
      TupleIntJsonSerializable var2 = (TupleIntJsonSerializable)this.statsData.get(var1);
      return var2 != null ? var2.getJsonSerializableValue() : null;
   }

   public IJsonSerializable setProgress(StatBase var1, IJsonSerializable var2) {
      TupleIntJsonSerializable var3 = (TupleIntJsonSerializable)this.statsData.get(var1);
      if (var3 == null) {
         var3 = new TupleIntJsonSerializable();
         this.statsData.put(var1, var3);
      }

      var3.setJsonSerializableValue(var2);
      return var2;
   }
}
