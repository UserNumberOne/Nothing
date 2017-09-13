package net.minecraft.command;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandDebug extends CommandBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private long profileStartTime;
   private int profileStartTick;

   public String getName() {
      return "debug";
   }

   public int getRequiredPermissionLevel() {
      return 3;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.debug.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.debug.usage", new Object[0]);
      } else {
         if ("start".equals(var3[0])) {
            if (var3.length != 1) {
               throw new WrongUsageException("commands.debug.usage", new Object[0]);
            }

            notifyCommandListener(var2, this, "commands.debug.start", new Object[0]);
            var1.enableProfiling();
            this.profileStartTime = MinecraftServer.getCurrentTimeMillis();
            this.profileStartTick = var1.getTickCounter();
         } else {
            if (!"stop".equals(var3[0])) {
               throw new WrongUsageException("commands.debug.usage", new Object[0]);
            }

            if (var3.length != 1) {
               throw new WrongUsageException("commands.debug.usage", new Object[0]);
            }

            if (!var1.theProfiler.profilingEnabled) {
               throw new CommandException("commands.debug.notStarted", new Object[0]);
            }

            long var4 = MinecraftServer.getCurrentTimeMillis();
            int var6 = var1.getTickCounter();
            long var7 = var4 - this.profileStartTime;
            int var9 = var6 - this.profileStartTick;
            this.saveProfilerResults(var7, var9, var1);
            var1.theProfiler.profilingEnabled = false;
            notifyCommandListener(var2, this, "commands.debug.stop", new Object[]{(float)var7 / 1000.0F, var9});
         }

      }
   }

   private void saveProfilerResults(long var1, int var3, MinecraftServer var4) {
      File var5 = new File(var4.getFile("debug"), "profile-results-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
      var5.getParentFile().mkdirs();
      FileWriter var6 = null;

      try {
         var6 = new FileWriter(var5);
         var6.write(this.getProfilerResults(var1, var3, var4));
      } catch (Throwable var11) {
         IOUtils.closeQuietly(var6);
         LOGGER.error("Could not save profiler results to {}", new Object[]{var5, var11});
      } finally {
         IOUtils.closeQuietly(var6);
      }

   }

   private String getProfilerResults(long var1, int var3, MinecraftServer var4) {
      StringBuilder var5 = new StringBuilder();
      var5.append("---- Minecraft Profiler Results ----\n");
      var5.append("// ");
      var5.append(getWittyComment());
      var5.append("\n\n");
      var5.append("Time span: ").append(var1).append(" ms\n");
      var5.append("Tick span: ").append(var3).append(" ticks\n");
      var5.append("// This is approximately ").append(String.format("%.2f", (float)var3 / ((float)var1 / 1000.0F))).append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
      var5.append("--- BEGIN PROFILE DUMP ---\n\n");
      this.appendProfilerResults(0, "root", var5, var4);
      var5.append("--- END PROFILE DUMP ---\n\n");
      return var5.toString();
   }

   private void appendProfilerResults(int var1, String var2, StringBuilder var3, MinecraftServer var4) {
      List var5 = var4.theProfiler.getProfilingData(var2);
      if (var5 != null && var5.size() >= 3) {
         for(int var6 = 1; var6 < var5.size(); ++var6) {
            Profiler.Result var7 = (Profiler.Result)var5.get(var6);
            var3.append(String.format("[%02d] ", var1));

            for(int var8 = 0; var8 < var1; ++var8) {
               var3.append("|   ");
            }

            var3.append(var7.profilerName).append(" - ").append(String.format("%.2f", var7.usePercentage)).append("%/").append(String.format("%.2f", var7.totalUsePercentage)).append("%\n");
            if (!"unspecified".equals(var7.profilerName)) {
               try {
                  this.appendProfilerResults(var1 + 1, var2 + "." + var7.profilerName, var3, var4);
               } catch (Exception var9) {
                  var3.append("[[ EXCEPTION ").append(var9).append(" ]]");
               }
            }
         }
      }

   }

   private static String getWittyComment() {
      String[] var0 = new String[]{"Shiny numbers!", "Am I not running fast enough? :(", "I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."};

      try {
         return var0[(int)(System.nanoTime() % (long)var0.length)];
      } catch (Throwable var2) {
         return "Witty comment unavailable :(";
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"start", "stop"}) : Collections.emptyList();
   }
}
