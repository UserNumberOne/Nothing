package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class RegionFileCache {
   private static final Map REGIONS_BY_FILE = Maps.newHashMap();

   public static synchronized RegionFile createOrLoadRegionFile(File var0, int var1, int var2) {
      File var3 = new File(var0, "region");
      File var4 = new File(var3, "r." + (var1 >> 5) + "." + (var2 >> 5) + ".mca");
      RegionFile var5 = (RegionFile)REGIONS_BY_FILE.get(var4);
      if (var5 != null) {
         return var5;
      } else {
         if (!var3.exists()) {
            var3.mkdirs();
         }

         if (REGIONS_BY_FILE.size() >= 256) {
            clearRegionFileReferences();
         }

         RegionFile var6 = new RegionFile(var4);
         REGIONS_BY_FILE.put(var4, var6);
         return var6;
      }
   }

   public static synchronized void clearRegionFileReferences() {
      for(RegionFile var1 : REGIONS_BY_FILE.values()) {
         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      }

      REGIONS_BY_FILE.clear();
   }

   public static DataInputStream getChunkInputStream(File var0, int var1, int var2) {
      RegionFile var3 = createOrLoadRegionFile(var0, var1, var2);
      return var3.getChunkDataInputStream(var1 & 31, var2 & 31);
   }

   public static DataOutputStream getChunkOutputStream(File var0, int var1, int var2) {
      RegionFile var3 = createOrLoadRegionFile(var0, var1, var2);
      return var3.getChunkDataOutputStream(var1 & 31, var2 & 31);
   }
}
