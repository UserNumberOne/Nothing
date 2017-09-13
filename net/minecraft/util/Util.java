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
      String var0 = System.getProperty("os.name").toLowerCase();
      return var0.contains("win") ? Util.EnumOS.WINDOWS : (var0.contains("mac") ? Util.EnumOS.OSX : (var0.contains("solaris") ? Util.EnumOS.SOLARIS : (var0.contains("sunos") ? Util.EnumOS.SOLARIS : (var0.contains("linux") ? Util.EnumOS.LINUX : (var0.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
   }

   @Nullable
   public static Object runTask(FutureTask var0, Logger var1) {
      try {
         var0.run();
         return var0.get();
      } catch (ExecutionException var3) {
         var1.fatal("Error executing task", var3);
      } catch (InterruptedException var4) {
         var1.fatal("Error executing task", var4);
      }

      return null;
   }

   public static Object getLastElement(List var0) {
      return var0.get(var0.size() - 1);
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
