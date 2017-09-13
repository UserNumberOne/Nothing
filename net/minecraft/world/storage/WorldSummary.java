package net.minecraft.world.storage;

import net.minecraft.util.StringUtils;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldSummary implements Comparable {
   private final String fileName;
   private final String displayName;
   private final long lastTimePlayed;
   private final long sizeOnDisk;
   private final boolean requiresConversion;
   private final GameType theEnumGameType;
   private final boolean hardcore;
   private final boolean cheatsEnabled;
   private final String versionName;
   private final int versionId;
   private final boolean versionSnapshot;

   public WorldSummary(WorldInfo var1, String var2, String var3, long var4, boolean var6) {
      this.fileName = var2;
      this.displayName = var3;
      this.lastTimePlayed = var1.getLastTimePlayed();
      this.sizeOnDisk = var4;
      this.theEnumGameType = var1.getGameType();
      this.requiresConversion = var6;
      this.hardcore = var1.isHardcoreModeEnabled();
      this.cheatsEnabled = var1.areCommandsAllowed();
      this.versionName = var1.getVersionName();
      this.versionId = var1.getVersionId();
      this.versionSnapshot = var1.isVersionSnapshot();
   }

   public String getFileName() {
      return this.fileName;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public long getSizeOnDisk() {
      return this.sizeOnDisk;
   }

   public boolean requiresConversion() {
      return this.requiresConversion;
   }

   public long getLastTimePlayed() {
      return this.lastTimePlayed;
   }

   public int compareTo(WorldSummary var1) {
      return this.lastTimePlayed < var1.lastTimePlayed ? 1 : (this.lastTimePlayed > var1.lastTimePlayed ? -1 : this.fileName.compareTo(var1.fileName));
   }

   public GameType getEnumGameType() {
      return this.theEnumGameType;
   }

   public boolean isHardcoreModeEnabled() {
      return this.hardcore;
   }

   public boolean getCheatsEnabled() {
      return this.cheatsEnabled;
   }

   public String getVersionName() {
      return StringUtils.isNullOrEmpty(this.versionName) ? I18n.translateToLocal("selectWorld.versionUnknown") : this.versionName;
   }

   public boolean markVersionInList() {
      return this.askToOpenWorld();
   }

   public boolean askToOpenWorld() {
      return this.versionId > 512;
   }
}
