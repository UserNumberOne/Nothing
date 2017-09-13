package net.minecraft.stats;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StatisticsManager {
   protected final Map statsData = Maps.newConcurrentMap();

   public boolean hasAchievementUnlocked(Achievement achievementIn) {
      return this.readStat(achievementIn) > 0;
   }

   public boolean canUnlockAchievement(Achievement achievementIn) {
      return achievementIn.parentAchievement == null || this.hasAchievementUnlocked(achievementIn.parentAchievement);
   }

   public void increaseStat(EntityPlayer player, StatBase stat, int amount) {
      if (!stat.isAchievement() || this.canUnlockAchievement((Achievement)stat)) {
         this.unlockAchievement(player, stat, this.readStat(stat) + amount);
      }

   }

   @SideOnly(Side.CLIENT)
   public int countRequirementsUntilAvailable(Achievement achievementIn) {
      if (this.hasAchievementUnlocked(achievementIn)) {
         return 0;
      } else {
         int i = 0;

         for(Achievement achievement = achievementIn.parentAchievement; achievement != null && !this.hasAchievementUnlocked(achievement); ++i) {
            achievement = achievement.parentAchievement;
         }

         return i;
      }
   }

   public void unlockAchievement(EntityPlayer playerIn, StatBase statIn, int p_150873_3_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(statIn);
      if (tupleintjsonserializable == null) {
         tupleintjsonserializable = new TupleIntJsonSerializable();
         this.statsData.put(statIn, tupleintjsonserializable);
      }

      tupleintjsonserializable.setIntegerValue(p_150873_3_);
   }

   public int readStat(StatBase stat) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(stat);
      return tupleintjsonserializable == null ? 0 : tupleintjsonserializable.getIntegerValue();
   }

   public IJsonSerializable getProgress(StatBase p_150870_1_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(p_150870_1_);
      return tupleintjsonserializable != null ? tupleintjsonserializable.getJsonSerializableValue() : null;
   }

   public IJsonSerializable setProgress(StatBase p_150872_1_, IJsonSerializable p_150872_2_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(p_150872_1_);
      if (tupleintjsonserializable == null) {
         tupleintjsonserializable = new TupleIntJsonSerializable();
         this.statsData.put(p_150872_1_, tupleintjsonserializable);
      }

      tupleintjsonserializable.setJsonSerializableValue(p_150872_2_);
      return p_150872_2_;
   }
}
