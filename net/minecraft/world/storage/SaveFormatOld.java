package net.minecraft.world.storage;

import java.io.File;
import java.io.FileInputStream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld implements ISaveFormat {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final File savesDirectory;
   protected final DataFixer dataFixer;

   public SaveFormatOld(File var1, DataFixer var2) {
      this.dataFixer = var2;
      if (!var1.exists()) {
         var1.mkdirs();
      }

      this.savesDirectory = var1;
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

   public ISaveHandler getSaveLoader(String var1, boolean var2) {
      return new SaveHandler(this.savesDirectory, var1, var2, this.dataFixer);
   }

   public boolean isOldMapFormat(String var1) {
      return false;
   }

   public boolean convertMapFormat(String var1, IProgressUpdate var2) {
      return false;
   }

   public File getFile(String var1, String var2) {
      return new File(new File(this.savesDirectory, var1), var2);
   }
}
