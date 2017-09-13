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

   public static synchronized RegionFile createOrLoadRegionFile(File file, int i, int j) {
      File file1 = new File(file, "region");
      File file2 = new File(file1, "r." + (i >> 5) + "." + (j >> 5) + ".mca");
      RegionFile regionfile = (RegionFile)REGIONS_BY_FILE.get(file2);
      if (regionfile != null) {
         return regionfile;
      } else {
         if (!file1.exists()) {
            file1.mkdirs();
         }

         if (REGIONS_BY_FILE.size() >= 256) {
            clearRegionFileReferences();
         }

         RegionFile regionfile1 = new RegionFile(file2);
         REGIONS_BY_FILE.put(file2, regionfile1);
         return regionfile1;
      }
   }

   public static synchronized void clearRegionFileReferences() {
      for(RegionFile regionfile : REGIONS_BY_FILE.values()) {
         try {
            if (regionfile != null) {
               regionfile.close();
            }
         } catch (IOException var3) {
            var3.printStackTrace();
         }
      }

      REGIONS_BY_FILE.clear();
   }

   public static synchronized NBTTagCompound c(File file, int i, int j) throws IOException {
      RegionFile regionfile = createOrLoadRegionFile(file, i, j);
      DataInputStream datainputstream = regionfile.getChunkDataInputStream(i & 31, j & 31);
      return datainputstream == null ? null : CompressedStreamTools.read(datainputstream);
   }

   public static synchronized void d(File file, int i, int j, NBTTagCompound nbttagcompound) throws IOException {
      RegionFile regionfile = createOrLoadRegionFile(file, i, j);
      DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(i & 31, j & 31);
      CompressedStreamTools.write(nbttagcompound, dataoutputstream);
      dataoutputstream.close();
   }
}
