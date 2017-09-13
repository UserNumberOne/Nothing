package net.minecraft.realms;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsAnvilLevelStorageSource {
   private final ISaveFormat levelStorageSource;

   public RealmsAnvilLevelStorageSource(ISaveFormat var1) {
      this.levelStorageSource = levelStorageSourceIn;
   }

   public String getName() {
      return this.levelStorageSource.getName();
   }

   public boolean levelExists(String var1) {
      return this.levelStorageSource.canLoadWorld(p_levelExists_1_);
   }

   public boolean convertLevel(String var1, IProgressUpdate var2) {
      return this.levelStorageSource.convertMapFormat(p_convertLevel_1_, p_convertLevel_2_);
   }

   public boolean requiresConversion(String var1) {
      return this.levelStorageSource.isOldMapFormat(p_requiresConversion_1_);
   }

   public boolean isNewLevelIdAcceptable(String var1) {
      return this.levelStorageSource.isNewLevelIdAcceptable(p_isNewLevelIdAcceptable_1_);
   }

   public boolean deleteLevel(String var1) {
      return this.levelStorageSource.deleteWorldDirectory(p_deleteLevel_1_);
   }

   public boolean isConvertible(String var1) {
      return this.levelStorageSource.isConvertible(p_isConvertible_1_);
   }

   public void renameLevel(String var1, String var2) {
      this.levelStorageSource.renameWorld(p_renameLevel_1_, p_renameLevel_2_);
   }

   public void clearAll() {
      this.levelStorageSource.flushCache();
   }

   public List getLevelList() throws AnvilConverterException {
      List list = Lists.newArrayList();

      for(WorldSummary worldsummary : this.levelStorageSource.getSaveList()) {
         list.add(new RealmsLevelSummary(worldsummary));
      }

      return list;
   }
}
