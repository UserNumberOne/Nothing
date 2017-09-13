package net.minecraft.world.chunk.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraft.world.storage.WorldInfo;

public class AnvilSaveHandler extends SaveHandler {
   public AnvilSaveHandler(File var1, String var2, boolean var3, DataFixer var4) {
      super(p_i46650_1_, p_i46650_2_, p_i46650_3_, dataFixerIn);
   }

   public IChunkLoader getChunkLoader(WorldProvider var1) {
      File file1 = this.getWorldDirectory();
      if (provider.getSaveFolder() != null) {
         File file3 = new File(file1, provider.getSaveFolder());
         file3.mkdirs();
         return new AnvilChunkLoader(file3, this.dataFixer);
      } else {
         return new AnvilChunkLoader(file1, this.dataFixer);
      }
   }

   public void saveWorldInfoWithPlayer(WorldInfo var1, @Nullable NBTTagCompound var2) {
      worldInformation.setSaveVersion(19133);
      super.saveWorldInfoWithPlayer(worldInformation, tagCompound);
   }

   public void flush() {
      try {
         ThreadedFileIOBase.getThreadedIOInstance().waitForFinish();
      } catch (InterruptedException var2) {
         var2.printStackTrace();
      }

      RegionFileCache.clearRegionFileReferences();
   }
}
