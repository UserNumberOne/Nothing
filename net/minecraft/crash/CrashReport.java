package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.util.ReportedException;
import net.minecraft.world.gen.layer.IntCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.CraftCrashReport;

public class CrashReport {
   private static final Logger LOGGER = LogManager.getLogger();
   private final String description;
   private final Throwable cause;
   private final CrashReportCategory theReportCategory = new CrashReportCategory(this, "System Details");
   private final List crashReportSections = Lists.newArrayList();
   private File crashReportFile;
   private boolean firstCategoryInCrashReport = true;
   private StackTraceElement[] stacktrace = new StackTraceElement[0];

   public CrashReport(String s, Throwable throwable) {
      this.description = s;
      this.cause = throwable;
      this.populateEnvironment();
   }

   private void populateEnvironment() {
      this.theReportCategory.setDetail("Minecraft Version", new ICrashReportDetail() {
         public String call() {
            return "1.10.2";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("Operating System", new ICrashReportDetail() {
         public String call() {
            return System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("Java Version", new ICrashReportDetail() {
         public String call() {
            return System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("Java VM Version", new ICrashReportDetail() {
         public String call() {
            return System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor");
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("Memory", new ICrashReportDetail() {
         public String call() {
            Runtime runtime = Runtime.getRuntime();
            long i = runtime.maxMemory();
            long j = runtime.totalMemory();
            long k = runtime.freeMemory();
            long l = i / 1024L / 1024L;
            long i1 = j / 1024L / 1024L;
            long j1 = k / 1024L / 1024L;
            return k + " bytes (" + j1 + " MB) / " + j + " bytes (" + i1 + " MB) up to " + i + " bytes (" + l + " MB)";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("JVM Flags", new ICrashReportDetail() {
         public String call() {
            RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
            List list = runtimemxbean.getInputArguments();
            int i = 0;
            StringBuilder stringbuilder = new StringBuilder();

            for(String s : list) {
               if (s.startsWith("-X")) {
                  if (i++ > 0) {
                     stringbuilder.append(" ");
                  }

                  stringbuilder.append(s);
               }
            }

            return String.format("%d total; %s", i, stringbuilder.toString());
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("IntCache", new ICrashReportDetail() {
         public String call() throws Exception {
            return IntCache.getCacheSizes();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      this.theReportCategory.setDetail("CraftBukkit Information", new CraftCrashReport());
   }

   public String getDescription() {
      return this.description;
   }

   public Throwable getCrashCause() {
      return this.cause;
   }

   public void getSectionsInStringBuilder(StringBuilder stringbuilder) {
      if ((this.stacktrace == null || this.stacktrace.length <= 0) && !this.crashReportSections.isEmpty()) {
         this.stacktrace = (StackTraceElement[])ArrayUtils.subarray(((CrashReportCategory)this.crashReportSections.get(0)).getStackTrace(), 0, 1);
      }

      if (this.stacktrace != null && this.stacktrace.length > 0) {
         stringbuilder.append("-- Head --\n");
         stringbuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
         stringbuilder.append("Stacktrace:\n");

         for(StackTraceElement stacktraceelement : this.stacktrace) {
            stringbuilder.append("\t").append("at ").append(stacktraceelement);
            stringbuilder.append("\n");
         }

         stringbuilder.append("\n");
      }

      for(CrashReportCategory crashreportsystemdetails : this.crashReportSections) {
         crashreportsystemdetails.appendToStringBuilder(stringbuilder);
         stringbuilder.append("\n\n");
      }

      this.theReportCategory.appendToStringBuilder(stringbuilder);
   }

   public String getCauseStackTraceOrString() {
      StringWriter stringwriter = null;
      PrintWriter printwriter = null;
      Object object = this.cause;
      if (((Throwable)object).getMessage() == null) {
         if (object instanceof NullPointerException) {
            object = new NullPointerException(this.description);
         } else if (object instanceof StackOverflowError) {
            object = new StackOverflowError(this.description);
         } else if (object instanceof OutOfMemoryError) {
            object = new OutOfMemoryError(this.description);
         }

         ((Throwable)object).setStackTrace(this.cause.getStackTrace());
      }

      String s = ((Throwable)object).toString();

      try {
         stringwriter = new StringWriter();
         printwriter = new PrintWriter(stringwriter);
         ((Throwable)object).printStackTrace(printwriter);
         s = stringwriter.toString();
      } finally {
         IOUtils.closeQuietly(stringwriter);
         IOUtils.closeQuietly(printwriter);
      }

      return s;
   }

   public String getCompleteReport() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("---- Minecraft Crash Report ----\n");
      stringbuilder.append("// ");
      stringbuilder.append(getWittyComment());
      stringbuilder.append("\n\n");
      stringbuilder.append("Time: ");
      stringbuilder.append((new SimpleDateFormat()).format(new Date()));
      stringbuilder.append("\n");
      stringbuilder.append("Description: ");
      stringbuilder.append(this.description);
      stringbuilder.append("\n\n");
      stringbuilder.append(this.getCauseStackTraceOrString());
      stringbuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

      for(int i = 0; i < 87; ++i) {
         stringbuilder.append("-");
      }

      stringbuilder.append("\n\n");
      this.getSectionsInStringBuilder(stringbuilder);
      return stringbuilder.toString();
   }

   public boolean saveToFile(File file) {
      if (this.crashReportFile != null) {
         return false;
      } else {
         if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
         }

         FileWriter filewriter = null;

         boolean flag;
         try {
            filewriter = new FileWriter(file);
            filewriter.write(this.getCompleteReport());
            this.crashReportFile = file;
            boolean flag1 = true;
            boolean var4 = flag1;
            return var4;
         } catch (Throwable var9) {
            LOGGER.error("Could not save crash report to {}", new Object[]{file, var9});
            flag = false;
         } finally {
            IOUtils.closeQuietly(filewriter);
         }

         return flag;
      }
   }

   public CrashReportCategory getCategory() {
      return this.theReportCategory;
   }

   public CrashReportCategory makeCategory(String s) {
      return this.makeCategoryDepth(s, 1);
   }

   public CrashReportCategory makeCategoryDepth(String s, int i) {
      CrashReportCategory crashreportsystemdetails = new CrashReportCategory(this, s);
      if (this.firstCategoryInCrashReport) {
         int j = crashreportsystemdetails.getPrunedStackTrace(i);
         StackTraceElement[] astacktraceelement = this.cause.getStackTrace();
         StackTraceElement stacktraceelement = null;
         StackTraceElement stacktraceelement1 = null;
         int k = astacktraceelement.length - j;
         if (k < 0) {
            System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + j + ")");
         }

         if (astacktraceelement != null && k >= 0 && k < astacktraceelement.length) {
            stacktraceelement = astacktraceelement[k];
            if (astacktraceelement.length + 1 - j < astacktraceelement.length) {
               stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - j];
            }
         }

         this.firstCategoryInCrashReport = crashreportsystemdetails.firstTwoElementsOfStackTraceMatch(stacktraceelement, stacktraceelement1);
         if (j > 0 && !this.crashReportSections.isEmpty()) {
            CrashReportCategory crashreportsystemdetails1 = (CrashReportCategory)this.crashReportSections.get(this.crashReportSections.size() - 1);
            crashreportsystemdetails1.trimStackTraceEntriesFromBottom(j);
         } else if (astacktraceelement != null && astacktraceelement.length >= j && k >= 0 && k < astacktraceelement.length) {
            this.stacktrace = new StackTraceElement[k];
            System.arraycopy(astacktraceelement, 0, this.stacktrace, 0, this.stacktrace.length);
         } else {
            this.firstCategoryInCrashReport = false;
         }
      }

      this.crashReportSections.add(crashreportsystemdetails);
      return crashreportsystemdetails;
   }

   private static String getWittyComment() {
      String[] astring = new String[]{"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

      try {
         return astring[(int)(System.nanoTime() % (long)astring.length)];
      } catch (Throwable var1) {
         return "Witty comment unavailable :(";
      }
   }

   public static CrashReport makeCrashReport(Throwable throwable, String s) {
      CrashReport crashreport;
      if (throwable instanceof ReportedException) {
         crashreport = ((ReportedException)throwable).getCrashReport();
      } else {
         crashreport = new CrashReport(s, throwable);
      }

      return crashreport;
   }
}
