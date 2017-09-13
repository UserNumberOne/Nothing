package net.minecraft.world.storage;

import java.io.File;
import java.util.List;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.util.IProgressUpdate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISaveFormat {
   @SideOnly(Side.CLIENT)
   String getName();

   ISaveHandler getSaveLoader(String var1, boolean var2);

   @SideOnly(Side.CLIENT)
   List getSaveList() throws AnvilConverterException;

   boolean isOldMapFormat(String var1);

   @SideOnly(Side.CLIENT)
   void flushCache();

   @SideOnly(Side.CLIENT)
   WorldInfo getWorldInfo(String var1);

   @SideOnly(Side.CLIENT)
   boolean isNewLevelIdAcceptable(String var1);

   @SideOnly(Side.CLIENT)
   boolean deleteWorldDirectory(String var1);

   @SideOnly(Side.CLIENT)
   void renameWorld(String var1, String var2);

   @SideOnly(Side.CLIENT)
   boolean isConvertible(String var1);

   boolean convertMapFormat(String var1, IProgressUpdate var2);

   File getFile(String var1, String var2);

   @SideOnly(Side.CLIENT)
   boolean canLoadWorld(String var1);
}
