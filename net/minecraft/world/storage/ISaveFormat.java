package net.minecraft.world.storage;

import java.io.File;
import net.minecraft.util.IProgressUpdate;

public interface ISaveFormat {
   ISaveHandler getSaveLoader(String var1, boolean var2);

   boolean isOldMapFormat(String var1);

   boolean convertMapFormat(String var1, IProgressUpdate var2);

   File getFile(String var1, String var2);
}
