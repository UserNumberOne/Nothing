package net.minecraft.profiler;

import java.util.List;

public class Profiler {
   public boolean profilingEnabled;

   public void clearProfiling() {
   }

   public void startSection(String var1) {
   }

   public void endSection() {
   }

   public List getProfilingData(String var1) {
      return null;
   }

   public void endStartSection(String var1) {
   }

   public String getNameOfLastSection() {
      return "";
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

      public int compareTo(Profiler.Result param1) {
         // $FF: Couldn't be decompiled
      }

      public int compareTo(Profiler.Result param1) {
         // $FF: Couldn't be decompiled
      }
   }
}
