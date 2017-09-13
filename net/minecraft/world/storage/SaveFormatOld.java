package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
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

   public SaveFormatOld(File var1, DataFixer var2) {
      this.dataFixer = var2;
      if (!var1.exists()) {
         var1.mkdirs();
      }

      this.savesDirectory = var1;
   }

   @SideOnly(Side.CLIENT)
   public String getName() {
      return "Old Format";
   }

   @SideOnly(Side.CLIENT)
   public List getSaveList() throws AnvilConverterException {
      ArrayList var1 = Lists.newArrayList();

      for(int var2 = 0; var2 < 5; ++var2) {
         String var3 = "World" + (var2 + 1);
         WorldInfo var4 = this.getWorldInfo(var3);
         if (var4 != null) {
            var1.add(new WorldSummary(var4, var3, "", var4.getSizeOnDisk(), false));
         }
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   public void flushCache() {
   }

   public WorldInfo getWorldInfo(String var1) {
      File var2 = new File(this.savesDirectory, var1);
      if (!var2.exists()) {
         return null;
      } else {
         File var3 = new File(var2, "level.dat");
         if (var3.exists()) {
            WorldInfo var4 = getWorldData(var3, this.dataFixer);
            if (var4 != null) {
               return var4;
            }
         }

         var3 = new File(var2, "level.dat_old");
         return var3.exists() ? getWorldData(var3, this.dataFixer) : null;
      }
   }

   @Nullable
   public static WorldInfo getWorldData(File var0, DataFixer var1) {
      try {
         NBTTagCompound var2 = CompressedStreamTools.readCompressed(new FileInputStream(var0));
         NBTTagCompound var3 = var2.getCompoundTag("Data");
         return new WorldInfo(var1.process(FixTypes.LEVEL, var3));
      } catch (Exception var4) {
         LOGGER.error("Exception reading {}", new Object[]{var0, var4});
         return null;
      }
   }

   public static WorldInfo loadAndFix(File var0, DataFixer var1, SaveHandler var2) {
      try {
         NBTTagCompound var3 = CompressedStreamTools.readCompressed(new FileInputStream(var0));
         WorldInfo var4 = new WorldInfo(var1.process(FixTypes.LEVEL, var3.getCompoundTag("Data")));
         FMLCommonHandler.instance().handleWorldDataLoad(var2, var4, var3);
         return var4;
      } catch (AbortedException var5) {
         throw var5;
      } catch (Exception var6) {
         LOGGER.error("Exception reading " + var0, var6);
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public void renameWorld(String var1, String var2) {
      File var3 = new File(this.savesDirectory, var1);
      if (var3.exists()) {
         File var4 = new File(var3, "level.dat");
         if (var4.exists()) {
            try {
               NBTTagCompound var5 = CompressedStreamTools.readCompressed(new FileInputStream(var4));
               NBTTagCompound var6 = var5.getCompoundTag("Data");
               var6.setString("LevelName", var2);
               CompressedStreamTools.writeCompressed(var5, new FileOutputStream(var4));
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }

   }

   public ISaveHandler getSaveLoader(String var1, boolean var2) {
      return new SaveHandler(this.savesDirectory, var1, var2, this.dataFixer);
   }

   @SideOnly(Side.CLIENT)
   public boolean isNewLevelIdAcceptable(String var1) {
      File var2 = new File(this.savesDirectory, var1);
      if (var2.exists()) {
         return false;
      } else {
         try {
            var2.mkdir();
            var2.delete();
            return true;
         } catch (Throwable var4) {
            LOGGER.warn("Couldn't make new level", var4);
            return false;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean deleteWorldDirectory(String var1) {
      File var2 = new File(this.savesDirectory, var1);
      if (!var2.exists()) {
         return true;
      } else {
         LOGGER.info("Deleting level {}", new Object[]{var1});

         for(int var3 = 1; var3 <= 5; ++var3) {
            LOGGER.info("Attempt {}...", new Object[]{var3});
            if (deleteFiles(var2.listFiles())) {
               break;
            }

            LOGGER.warn("Unsuccessful in deleting contents.");
            if (var3 < 5) {
               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
                  ;
               }
            }
         }

         return var2.delete();
      }
   }

   @SideOnly(Side.CLIENT)
   protected static boolean deleteFiles(File[] var0) {
      for(File var4 : var0) {
         LOGGER.debug("Deleting {}", new Object[]{var4});
         if (var4.isDirectory() && !deleteFiles(var4.listFiles())) {
            LOGGER.warn("Couldn't delete directory {}", new Object[]{var4});
            return false;
         }

         if (!var4.delete()) {
            LOGGER.warn("Couldn't delete file {}", new Object[]{var4});
            return false;
         }
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean isConvertible(String var1) {
      return false;
   }

   public boolean isOldMapFormat(String var1) {
      return false;
   }

   public boolean convertMapFormat(String var1, IProgressUpdate var2) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean canLoadWorld(String var1) {
      File var2 = new File(this.savesDirectory, var1);
      return var2.isDirectory();
   }

   public File getFile(String var1, String var2) {
      return new File(new File(this.savesDirectory, var1), var2);
   }
}
