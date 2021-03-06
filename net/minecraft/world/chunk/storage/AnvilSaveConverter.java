package net.minecraft.world.chunk.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilSaveConverter extends SaveFormatOld {
   private static final Logger LOGGER = LogManager.getLogger();

   public AnvilSaveConverter(File var1, DataFixer var2) {
      super(var1, var2);
   }

   protected int getSaveVersion() {
      return 19133;
   }

   public ISaveHandler getSaveLoader(String var1, boolean var2) {
      return new AnvilSaveHandler(this.savesDirectory, var1, var2, this.dataFixer);
   }

   public boolean isOldMapFormat(String var1) {
      WorldInfo var2 = this.getWorldInfo(var1);
      return var2 != null && var2.getSaveVersion() != this.getSaveVersion();
   }

   public boolean convertMapFormat(String var1, IProgressUpdate var2) {
      var2.setLoadingProgress(0);
      ArrayList var3 = Lists.newArrayList();
      ArrayList var4 = Lists.newArrayList();
      ArrayList var5 = Lists.newArrayList();
      File var6 = new File(this.savesDirectory, var1);
      File var7 = new File(var6, "DIM-1");
      File var8 = new File(var6, "DIM1");
      LOGGER.info("Scanning folders...");
      this.addRegionFilesToCollection(var6, var3);
      if (var7.exists()) {
         this.addRegionFilesToCollection(var7, var4);
      }

      if (var8.exists()) {
         this.addRegionFilesToCollection(var8, var5);
      }

      int var9 = var3.size() + var4.size() + var5.size();
      LOGGER.info("Total conversion count is {}", new Object[]{var9});
      WorldInfo var10 = this.getWorldInfo(var1);
      Object var11;
      if (var10.getTerrainType() == WorldType.FLAT) {
         var11 = new BiomeProviderSingle(Biomes.PLAINS);
      } else {
         var11 = new BiomeProvider(var10);
      }

      this.convertFile(new File(var6, "region"), var3, (BiomeProvider)var11, 0, var9, var2);
      this.convertFile(new File(var7, "region"), var4, new BiomeProviderSingle(Biomes.HELL), var3.size(), var9, var2);
      this.convertFile(new File(var8, "region"), var5, new BiomeProviderSingle(Biomes.SKY), var3.size() + var4.size(), var9, var2);
      var10.setSaveVersion(19133);
      if (var10.getTerrainType() == WorldType.DEFAULT_1_1) {
         var10.setTerrainType(WorldType.DEFAULT);
      }

      this.createFile(var1);
      ISaveHandler var12 = this.getSaveLoader(var1, false);
      var12.saveWorldInfo(var10);
      return true;
   }

   private void createFile(String var1) {
      File var2 = new File(this.savesDirectory, var1);
      if (!var2.exists()) {
         LOGGER.warn("Unable to create level.dat_mcr backup");
      } else {
         File var3 = new File(var2, "level.dat");
         if (!var3.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
         } else {
            File var4 = new File(var2, "level.dat_mcr");
            if (!var3.renameTo(var4)) {
               LOGGER.warn("Unable to create level.dat_mcr backup");
            }

         }
      }
   }

   private void convertFile(File var1, Iterable var2, BiomeProvider var3, int var4, int var5, IProgressUpdate var6) {
      for(File var8 : var2) {
         this.convertChunks(var1, var8, var3, var4, var5, var6);
         ++var4;
         int var9 = (int)Math.round(100.0D * (double)var4 / (double)var5);
         var6.setLoadingProgress(var9);
      }

   }

   private void convertChunks(File var1, File var2, BiomeProvider var3, int var4, int var5, IProgressUpdate var6) {
      try {
         String var7 = var2.getName();
         RegionFile var8 = new RegionFile(var2);
         RegionFile var9 = new RegionFile(new File(var1, var7.substring(0, var7.length() - ".mcr".length()) + ".mca"));

         for(int var10 = 0; var10 < 32; ++var10) {
            for(int var11 = 0; var11 < 32; ++var11) {
               if (var8.isChunkSaved(var10, var11) && !var9.isChunkSaved(var10, var11)) {
                  DataInputStream var12 = var8.getChunkDataInputStream(var10, var11);
                  if (var12 == null) {
                     LOGGER.warn("Failed to fetch input stream");
                  } else {
                     NBTTagCompound var13 = CompressedStreamTools.read(var12);
                     var12.close();
                     NBTTagCompound var14 = var13.getCompoundTag("Level");
                     ChunkLoader.AnvilConverterData var15 = ChunkLoader.load(var14);
                     NBTTagCompound var16 = new NBTTagCompound();
                     NBTTagCompound var17 = new NBTTagCompound();
                     var16.setTag("Level", var17);
                     ChunkLoader.convertToAnvilFormat(var15, var17, var3);
                     DataOutputStream var18 = var9.getChunkDataOutputStream(var10, var11);
                     CompressedStreamTools.write(var16, var18);
                     var18.close();
                  }
               }
            }

            int var20 = (int)Math.round(100.0D * (double)(var4 * 1024) / (double)(var5 * 1024));
            int var21 = (int)Math.round(100.0D * (double)((var10 + 1) * 32 + var4 * 1024) / (double)(var5 * 1024));
            if (var21 > var20) {
               var6.setLoadingProgress(var21);
            }
         }

         var8.close();
         var9.close();
      } catch (IOException var19) {
         var19.printStackTrace();
      }

   }

   private void addRegionFilesToCollection(File var1, Collection var2) {
      File var3 = new File(var1, "region");
      File[] var4 = var3.listFiles(new FilenameFilter() {
         public boolean accept(File var1, String var2) {
            return var2.endsWith(".mcr");
         }
      });
      if (var4 != null) {
         Collections.addAll(var2, var4);
      }

   }
}
