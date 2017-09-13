package net.minecraft.profiler;

import com.google.common.collect.Maps;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.HttpUtil;

public class Snooper {
   private final Map snooperStats = Maps.newHashMap();
   private final Map clientStats = Maps.newHashMap();
   private final String uniqueID = UUID.randomUUID().toString();
   private final URL serverUrl;
   private final ISnooperInfo playerStatsCollector;
   private final Timer threadTrigger = new Timer("Snooper Timer", true);
   private final Object syncLock = new Object();
   private final long minecraftStartTimeMilis;
   private boolean isRunning;
   private int selfCounter;

   public Snooper(String var1, ISnooperInfo var2, long var3) {
      try {
         this.serverUrl = new URL("http://snoop.minecraft.net/" + var1 + "?version=" + 2);
      } catch (MalformedURLException var6) {
         throw new IllegalArgumentException();
      }

      this.playerStatsCollector = var2;
      this.minecraftStartTimeMilis = var3;
   }

   public void startSnooper() {
      if (!this.isRunning) {
         this.isRunning = true;
         this.addOSData();
         this.threadTrigger.schedule(new TimerTask() {
            public void run() {
               if (Snooper.this.playerStatsCollector.isSnooperEnabled()) {
                  HashMap var2;
                  synchronized(Snooper.this.syncLock) {
                     var2 = Maps.newHashMap(Snooper.this.clientStats);
                     if (Snooper.this.selfCounter == 0) {
                        var2.putAll(Snooper.this.snooperStats);
                     }

                     var2.put("snooper_count", Integer.valueOf(Snooper.this.selfCounter++));
                     var2.put("snooper_token", Snooper.this.uniqueID);
                  }

                  MinecraftServer var1 = Snooper.this.playerStatsCollector instanceof MinecraftServer ? (MinecraftServer)Snooper.this.playerStatsCollector : null;
                  HttpUtil.postMap(Snooper.this.serverUrl, var2, true, var1 == null ? null : var1.au());
               }
            }
         }, 0L, 900000L);
      }
   }

   private void addOSData() {
      this.addJvmArgsToSnooper();
      this.addClientStat("snooper_token", this.uniqueID);
      this.addStatToSnooper("snooper_token", this.uniqueID);
      this.addStatToSnooper("os_name", System.getProperty("os.name"));
      this.addStatToSnooper("os_version", System.getProperty("os.version"));
      this.addStatToSnooper("os_architecture", System.getProperty("os.arch"));
      this.addStatToSnooper("java_version", System.getProperty("java.version"));
      this.addClientStat("version", "1.10.2");
      this.playerStatsCollector.addServerTypeToSnooper(this);
   }

   private void addJvmArgsToSnooper() {
      RuntimeMXBean var1 = ManagementFactory.getRuntimeMXBean();
      List var2 = var1.getInputArguments();
      int var3 = 0;

      for(String var5 : var2) {
         if (var5.startsWith("-X")) {
            this.addClientStat("jvm_arg[" + var3++ + "]", var5);
         }
      }

      this.addClientStat("jvm_args", Integer.valueOf(var3));
   }

   public void addMemoryStatsToSnooper() {
      this.addStatToSnooper("memory_total", Long.valueOf(Runtime.getRuntime().totalMemory()));
      this.addStatToSnooper("memory_max", Long.valueOf(Runtime.getRuntime().maxMemory()));
      this.addStatToSnooper("memory_free", Long.valueOf(Runtime.getRuntime().freeMemory()));
      this.addStatToSnooper("cpu_cores", Integer.valueOf(Runtime.getRuntime().availableProcessors()));
      this.playerStatsCollector.addServerStatsToSnooper(this);
   }

   public void addClientStat(String var1, Object var2) {
      synchronized(this.syncLock) {
         this.clientStats.put(var1, var2);
      }
   }

   public void addStatToSnooper(String var1, Object var2) {
      synchronized(this.syncLock) {
         this.snooperStats.put(var1, var2);
      }
   }

   public boolean isSnooperRunning() {
      return this.isRunning;
   }

   public void stopSnooper() {
      this.threadTrigger.cancel();
   }

   public long getMinecraftStartTimeMillis() {
      return this.minecraftStartTimeMilis;
   }
}
