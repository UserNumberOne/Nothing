package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.StartupQuery.AbortedException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld implements ISaveFormat {
   private static final Logger LOGGER = LogManager.getLogger();
   public final File savesDirectory;
   protected final DataFixer dataFixer;

   public SaveFormatOld(File savesDirectoryIn, DataFixer dataFixerIn) {
      this.dataFixer = dataFixerIn;
      if (!savesDirectoryIn.exists()) {
         savesDirectoryIn.mkdirs();
      }

      this.savesDirectory = savesDirectoryIn;
   }

   @SideOnly(Side.CLIENT)
   public String getName() {
      return "Old Format";
   }

   @SideOnly(Side.CLIENT)
   public List getSaveList() throws AnvilConverterException {
      List list = Lists.newArrayList();

      for(int i = 0; i < 5; ++i) {
         String s = "World" + (i + 1);
         WorldInfo worldinfo = this.getWorldInfo(s);
         if (worldinfo != null) {
            list.add(new WorldSummary(worldinfo, s, "", worldinfo.getSizeOnDisk(), false));
         }
      }

      return list;
   }

   @SideOnly(Side.CLIENT)
   public void flushCache() {
   }

   public WorldInfo getWorldInfo(String saveName) {
      File file1 = new File(this.savesDirectory, saveName);
      if (!file1.exists()) {
         return null;
      } else {
         File file2 = new File(file1, "level.dat");
         if (file2.exists()) {
            WorldInfo worldinfo = getWorldData(file2, this.dataFixer);
            if (worldinfo != null) {
               return worldinfo;
            }
         }

         file2 = new File(file1, "level.dat_old");
         return file2.exists() ? getWorldData(file2, this.dataFixer) : null;
      }
   }

   @Nullable
   public static WorldInfo getWorldData(File p_186353_0_, DataFixer dataFixerIn) {
      try {
         NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(p_186353_0_));
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
         return new WorldInfo(dataFixerIn.process(FixTypes.LEVEL, nbttagcompound1));
      } catch (Exception var4) {
         LOGGER.error("Exception reading {}", new Object[]{p_186353_0_, var4});
         return null;
      }
   }

   public static WorldInfo loadAndFix(File file, DataFixer fixer, SaveHandler save) {
      try {
         NBTTagCompound nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
         WorldInfo info = new WorldInfo(fixer.process(FixTypes.LEVEL, nbt.getCompoundTag("Data")));
         FMLCommonHandler.instance().handleWorldDataLoad(save, info, nbt);
         return info;
      } catch (AbortedException var5) {
         throw var5;
      } catch (Exception var6) {
         LOGGER.error("Exception reading " + file, var6);
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public void renameWorld(String dirName, String newName) {
      File file1 = new File(this.savesDirectory, dirName);
      if (file1.exists()) {
         File file2 = new File(file1, "level.dat");
         if (file2.exists()) {
            try {
               NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file2));
               NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
               nbttagcompound1.setString("LevelName", newName);
               CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file2));
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }

   }

   public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata) {
      return new SaveHandler(this.savesDirectory, saveName, storePlayerdata, this.dataFixer);
   }

   @SideOnly(Side.CLIENT)
   public boolean isNewLevelIdAcceptable(String saveName) {
      File file1 = new File(this.savesDirectory, saveName);
      if (file1.exists()) {
         return false;
      } else {
         try {
            file1.mkdir();
            file1.delete();
            return true;
         } catch (Throwable var4) {
            LOGGER.warn("Couldn't make new level", var4);
            return false;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean deleteWorldDirectory(String saveName) {
      File file1 = new File(this.savesDirectory, saveName);
      if (!file1.exists()) {
         return true;
      } else {
         LOGGER.info("Deleting level {}", new Object[]{saveName});

         for(int i = 1; i <= 5; ++i) {
            LOGGER.info("Attempt {}...", new Object[]{i});
            if (deleteFiles(file1.listFiles())) {
               break;
            }

            LOGGER.warn("Unsuccessful in deleting contents.");
            if (i < 5) {
               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
                  ;
               }
            }
         }

         return file1.delete();
      }
   }

   @SideOnly(Side.CLIENT)
   protected static boolean deleteFiles(File[] files) {
      for(File file1 : files) {
         LOGGER.debug("Deleting {}", new Object[]{file1});
         if (file1.isDirectory() && !deleteFiles(file1.listFiles())) {
            LOGGER.warn("Couldn't delete directory {}", new Object[]{file1});
            return false;
         }

         if (!file1.delete()) {
            LOGGER.warn("Couldn't delete file {}", new Object[]{file1});
            return false;
         }
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean isConvertible(String saveName) {
      return false;
   }

   public boolean isOldMapFormat(String saveName) {
      return false;
   }

   public boolean convertMapFormat(String filename, IProgressUpdate progressCallback) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean canLoadWorld(String saveName) {
      File file1 = new File(this.savesDirectory, saveName);
      return file1.isDirectory();
   }

   public File getFile(String p_186352_1_, String p_186352_2_) {
      return new File(new File(this.savesDirectory, p_186352_1_), p_186352_2_);
   }
}
