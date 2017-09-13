package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class CrashReportCategory {
   private final CrashReport crashReport;
   private final String name;
   private final List children = Lists.newArrayList();
   private StackTraceElement[] stackTrace = new StackTraceElement[0];

   public CrashReportCategory(CrashReport var1, String var2) {
      this.crashReport = var1;
      this.name = var2;
   }

   public static String getCoordinateInfo(BlockPos var0) {
      return getCoordinateInfo(var0.getX(), var0.getY(), var0.getZ());
   }

   public static String getCoordinateInfo(int var0, int var1, int var2) {
      StringBuilder var3 = new StringBuilder();

      try {
         var3.append(String.format("World: (%d,%d,%d)", var0, var1, var2));
      } catch (Throwable var16) {
         var3.append("(Error finding world loc)");
      }

      var3.append(", ");

      try {
         int var4 = var0 >> 4;
         int var5 = var2 >> 4;
         int var6 = var0 & 15;
         int var7 = var1 >> 4;
         int var8 = var2 & 15;
         int var9 = var4 << 4;
         int var10 = var5 << 4;
         int var11 = (var4 + 1 << 4) - 1;
         int var12 = (var5 + 1 << 4) - 1;
         var3.append(String.format("Chunk: (at %d,%d,%d in %d,%d; contains blocks %d,0,%d to %d,255,%d)", var6, var7, var8, var4, var5, var9, var10, var11, var12));
      } catch (Throwable var15) {
         var3.append("(Error finding chunk loc)");
      }

      var3.append(", ");

      try {
         int var17 = var0 >> 9;
         int var18 = var2 >> 9;
         int var19 = var17 << 5;
         int var20 = var18 << 5;
         int var21 = (var17 + 1 << 5) - 1;
         int var22 = (var18 + 1 << 5) - 1;
         int var23 = var17 << 9;
         int var24 = var18 << 9;
         int var25 = (var17 + 1 << 9) - 1;
         int var13 = (var18 + 1 << 9) - 1;
         var3.append(String.format("Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,0,%d to %d,255,%d)", var17, var18, var19, var20, var21, var22, var23, var24, var25, var13));
      } catch (Throwable var14) {
         var3.append("(Error finding world loc)");
      }

      return var3.toString();
   }

   public void setDetail(String var1, ICrashReportDetail var2) {
      try {
         this.addCrashSection(var1, var2.call());
      } catch (Throwable var4) {
         this.addCrashSectionThrowable(var1, var4);
      }

   }

   public void addCrashSection(String var1, Object var2) {
      this.children.add(new CrashReportCategory.Entry(var1, var2));
   }

   public void addCrashSectionThrowable(String var1, Throwable var2) {
      this.addCrashSection(var1, var2);
   }

   public int getPrunedStackTrace(int var1) {
      StackTraceElement[] var2 = Thread.currentThread().getStackTrace();
      if (var2.length <= 0) {
         return 0;
      } else {
         this.stackTrace = new StackTraceElement[var2.length - 3 - var1];
         System.arraycopy(var2, 3 + var1, this.stackTrace, 0, this.stackTrace.length);
         return this.stackTrace.length;
      }
   }

   public boolean firstTwoElementsOfStackTraceMatch(StackTraceElement var1, StackTraceElement var2) {
      if (this.stackTrace.length != 0 && var1 != null) {
         StackTraceElement var3 = this.stackTrace[0];
         if (var3.isNativeMethod() == var1.isNativeMethod() && var3.getClassName().equals(var1.getClassName()) && var3.getFileName().equals(var1.getFileName()) && var3.getMethodName().equals(var1.getMethodName())) {
            if (var2 != null != this.stackTrace.length > 1) {
               return false;
            } else if (var2 != null && !this.stackTrace[1].equals(var2)) {
               return false;
            } else {
               this.stackTrace[0] = var1;
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void trimStackTraceEntriesFromBottom(int var1) {
      StackTraceElement[] var2 = new StackTraceElement[this.stackTrace.length - var1];
      System.arraycopy(this.stackTrace, 0, var2, 0, var2.length);
      this.stackTrace = var2;
   }

   public void appendToStringBuilder(StringBuilder var1) {
      var1.append("-- ").append(this.name).append(" --\n");
      var1.append("Details:");

      for(CrashReportCategory.Entry var3 : this.children) {
         var1.append("\n\t");
         var1.append(var3.getKey());
         var1.append(": ");
         var1.append(var3.getValue());
      }

      if (this.stackTrace != null && this.stackTrace.length > 0) {
         var1.append("\nStacktrace:");

         for(StackTraceElement var5 : this.stackTrace) {
            var1.append("\n\tat ");
            var1.append(var5);
         }
      }

   }

   public StackTraceElement[] getStackTrace() {
      return this.stackTrace;
   }

   public static void addBlockInfo(CrashReportCategory var0, final BlockPos var1, final Block var2, final int var3) {
      final int var4 = Block.getIdFromBlock(var2);
      var0.setDetail("Block type", new ICrashReportDetail() {
         public String call() throws Exception {
            try {
               return String.format("ID #%d (%s // %s)", var4, var2.getUnlocalizedName(), var2.getClass().getCanonicalName());
            } catch (Throwable var2x) {
               return "ID #" + var4;
            }
         }

         // $FF: synthetic method
         public Object call() throws Exception {
            return this.call();
         }
      });
      var0.setDetail("Block data value", new ICrashReportDetail() {
         public String call() throws Exception {
            if (var3 < 0) {
               return "Unknown? (Got " + var3 + ")";
            } else {
               String var1 = String.format("%4s", Integer.toBinaryString(var3)).replace(" ", "0");
               return String.format("%1$d / 0x%1$X / 0b%2$s", var3, var1);
            }
         }

         // $FF: synthetic method
         public Object call() throws Exception {
            return this.call();
         }
      });
      var0.setDetail("Block location", new ICrashReportDetail() {
         public String call() throws Exception {
            return CrashReportCategory.getCoordinateInfo(var1);
         }

         // $FF: synthetic method
         public Object call() throws Exception {
            return this.call();
         }
      });
   }

   public static void addBlockInfo(CrashReportCategory var0, final BlockPos var1, final IBlockState var2) {
      var0.setDetail("Block", new ICrashReportDetail() {
         public String call() throws Exception {
            return var2.toString();
         }

         // $FF: synthetic method
         public Object call() throws Exception {
            return this.call();
         }
      });
      var0.setDetail("Block location", new ICrashReportDetail() {
         public String call() throws Exception {
            return CrashReportCategory.getCoordinateInfo(var1);
         }

         // $FF: synthetic method
         public Object call() throws Exception {
            return this.call();
         }
      });
   }

   static class Entry {
      private final String key;
      private final String value;

      public Entry(String var1, Object var2) {
         this.key = var1;
         if (var2 == null) {
            this.value = "~~NULL~~";
         } else if (var2 instanceof Throwable) {
            Throwable var3 = (Throwable)var2;
            this.value = "~~ERROR~~ " + var3.getClass().getSimpleName() + ": " + var3.getMessage();
         } else {
            this.value = var2.toString();
         }

      }

      public String getKey() {
         return this.key;
      }

      public String getValue() {
         return this.value;
      }
   }
}
