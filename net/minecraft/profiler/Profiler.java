package net.minecraft.profiler;

import java.util.List;

public class Profiler {
   public boolean profilingEnabled;

   public void clearProfiling() {
   }

   public void startSection(String s) {
   }

   public void endSection() {
   }

   public List getProfilingData(String s) {
      return null;
   }

   public void endStartSection(String s) {
   }

   public String getNameOfLastSection() {
      return "";
   }

   public static final class Result implements Comparable {
      public double usePercentage;
      public double totalUsePercentage;
      public String profilerName;

      public Result(String s, double d0, double d1) {
         this.profilerName = s;
         this.usePercentage = d0;
         this.totalUsePercentage = d1;
      }

      public int compareTo(Profiler.Result param1) {
         // $FF: Couldn't be decompiled
      }

      public int compareTo(Profiler.Result param1) {
         // $FF: Couldn't be decompiled
      }
   }
}
