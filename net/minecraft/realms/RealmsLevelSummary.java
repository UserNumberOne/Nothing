package net.minecraft.realms;

import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsLevelSummary implements Comparable {
   private final WorldSummary levelSummary;

   public RealmsLevelSummary(WorldSummary var1) {
      this.levelSummary = levelSummaryIn;
   }

   public int getGameMode() {
      return this.levelSummary.getEnumGameType().getID();
   }

   public String getLevelId() {
      return this.levelSummary.getFileName();
   }

   public boolean hasCheats() {
      return this.levelSummary.getCheatsEnabled();
   }

   public boolean isHardcore() {
      return this.levelSummary.isHardcoreModeEnabled();
   }

   public boolean isRequiresConversion() {
      return this.levelSummary.requiresConversion();
   }

   public String getLevelName() {
      return this.levelSummary.getDisplayName();
   }

   public long getLastPlayed() {
      return this.levelSummary.getLastTimePlayed();
   }

   public int compareTo(WorldSummary var1) {
      return this.levelSummary.compareTo(p_compareTo_1_);
   }

   public long getSizeOnDisk() {
      return this.levelSummary.getSizeOnDisk();
   }

   public int compareTo(RealmsLevelSummary var1) {
      return this.levelSummary.getLastTimePlayed() < p_compareTo_1_.getLastPlayed() ? 1 : (this.levelSummary.getLastTimePlayed() > p_compareTo_1_.getLastPlayed() ? -1 : this.levelSummary.getFileName().compareTo(p_compareTo_1_.getLevelId()));
   }
}
