package net.minecraft.world.chunk.storage;

import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

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

   public static synchronized NBTTagCompound c(File var0, int var1, int var2) throws IOException {
      RegionFile var3 = createOrLoadRegionFile(var0, var1, var2);
      DataInputStream var4 = var3.getChunkDataInputStream(var1 & 31, var2 & 31);
      return var4 == null ? null : CompressedStreamTools.read(var4);
   }

   public static synchronized void d(File var0, int var1, int var2, NBTTagCompound var3) throws IOException {
      RegionFile var4 = createOrLoadRegionFile(var0, var1, var2);
      DataOutputStream var5 = var4.getChunkDataOutputStream(var1 & 31, var2 & 31);
      CompressedStreamTools.write(var3, var5);
      var5.close();
   }
}
