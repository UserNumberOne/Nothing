package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler {
   private static final Logger LOGGER = LogManager.getLogger();
   private final List sectionList = Lists.newArrayList();
   private final List timestampList = Lists.newArrayList();
   public boolean profilingEnabled;
   private String profilingSection = "";
   private final Map profilingMap = Maps.newHashMap();

   public void clearProfiling() {
      this.profilingMap.clear();
      this.profilingSection = "";
      this.sectionList.clear();
   }

   public void startSection(String var1) {
      if (this.profilingEnabled) {
         if (this.profilingSection.length() > 0) {
            this.profilingSection = this.profilingSection + ".";
         }

         this.profilingSection = this.profilingSection + var1;
         this.sectionList.add(this.profilingSection);
         this.timestampList.add(Long.valueOf(System.nanoTime()));
      }

   }

   public void endSection() {
      if (this.profilingEnabled) {
         long var1 = System.nanoTime();
         long var3 = ((Long)this.timestampList.remove(this.timestampList.size() - 1)).longValue();
         this.sectionList.remove(this.sectionList.size() - 1);
         long var5 = var1 - var3;
         if (this.profilingMap.containsKey(this.profilingSection)) {
            this.profilingMap.put(this.profilingSection, Long.valueOf(((Long)this.profilingMap.get(this.profilingSection)).longValue() + var5));
         } else {
            this.profilingMap.put(this.profilingSection, Long.valueOf(var5));
         }

         if (var5 > 100000000L) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", new Object[]{this.profilingSection, (double)var5 / 1000000.0D});
         }

         this.profilingSection = this.sectionList.isEmpty() ? "" : (String)this.sectionList.get(this.sectionList.size() - 1);
      }

   }

   public List getProfilingData(String var1) {
      if (!this.profilingEnabled) {
         return Collections.emptyList();
      } else {
         long var2 = this.profilingMap.containsKey("root") ? ((Long)this.profilingMap.get("root")).longValue() : 0L;
         long var4 = this.profilingMap.containsKey(var1) ? ((Long)this.profilingMap.get(var1)).longValue() : -1L;
         ArrayList var6 = Lists.newArrayList();
         if (var1.length() > 0) {
            var1 = var1 + ".";
         }

         long var7 = 0L;

         for(String var10 : this.profilingMap.keySet()) {
            if (var10.length() > var1.length() && var10.startsWith(var1) && var10.indexOf(".", var1.length() + 1) < 0) {
               var7 += ((Long)this.profilingMap.get(var10)).longValue();
            }
         }

         float var19 = (float)var7;
         if (var7 < var4) {
            var7 = var4;
         }

         if (var2 < var7) {
            var2 = var7;
         }

         for(String var11 : this.profilingMap.keySet()) {
            if (var11.length() > var1.length() && var11.startsWith(var1) && var11.indexOf(".", var1.length() + 1) < 0) {
               long var12 = ((Long)this.profilingMap.get(var11)).longValue();
               double var14 = (double)var12 * 100.0D / (double)var7;
               double var16 = (double)var12 * 100.0D / (double)var2;
               String var18 = var11.substring(var1.length());
               var6.add(new Profiler.Result(var18, var14, var16));
            }
         }

         for(String var22 : this.profilingMap.keySet()) {
            this.profilingMap.put(var22, Long.valueOf(((Long)this.profilingMap.get(var22)).longValue() * 999L / 1000L));
         }

         if ((float)var7 > var19) {
            var6.add(new Profiler.Result("unspecified", (double)((float)var7 - var19) * 100.0D / (double)var7, (double)((float)var7 - var19) * 100.0D / (double)var2));
         }

         Collections.sort(var6);
         var6.add(0, new Profiler.Result(var1, 100.0D, (double)var7 * 100.0D / (double)var2));
         return var6;
      }
   }

   public void endStartSection(String var1) {
      this.endSection();
      this.startSection(var1);
   }

   public String getNameOfLastSection() {
      return this.sectionList.size() == 0 ? "[UNKNOWN]" : (String)this.sectionList.get(this.sectionList.size() - 1);
   }

   public static final class Result implements Comparable {
      public double usePercentage;
      public double totalUsePercentage;
      public String profilerName;

      public Result(String var1, double var2, double var4) {
         this.profilerName = var1;
         this.usePercentage = var2;
         this.totalUsePercentage = var4;
      }

      public int compareTo(Profiler.Result var1) {
         return var1.usePercentage < this.usePercentage ? -1 : (var1.usePercentage > this.usePercentage ? 1 : var1.profilerName.compareTo(this.profilerName));
      }

      @SideOnly(Side.CLIENT)
      public int getColor() {
         return (this.profilerName.hashCode() & 11184810) + 4473924;
      }
   }
}
