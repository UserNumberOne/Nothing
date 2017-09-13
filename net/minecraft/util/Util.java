package net.minecraft.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

public class Util {
   @SideOnly(Side.CLIENT)
   public static Util.EnumOS getOSType() {
      String s = System.getProperty("os.name").toLowerCase();
      return s.contains("win") ? Util.EnumOS.WINDOWS : (s.contains("mac") ? Util.EnumOS.OSX : (s.contains("solaris") ? Util.EnumOS.SOLARIS : (s.contains("sunos") ? Util.EnumOS.SOLARIS : (s.contains("linux") ? Util.EnumOS.LINUX : (s.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
   }

   @Nullable
   public static Object runTask(FutureTask var0, Logger var1) {
      try {
         task.run();
         return task.get();
      } catch (ExecutionException var3) {
         logger.fatal("Error executing task", var3);
      } catch (InterruptedException var4) {
         logger.fatal("Error executing task", var4);
      }

      return null;
   }

   public static Object getLastElement(List var0) {
      return list.get(list.size() - 1);
   }

   @SideOnly(Side.CLIENT)
   public static enum EnumOS {
      LINUX,
      SOLARIS,
      WINDOWS,
      OSX,
      UNKNOWN;
   }
}
