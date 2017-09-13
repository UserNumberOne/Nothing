package net.minecraft.nbt;

public class NBTSizeTracker {
   public static final NBTSizeTracker INFINITE = new NBTSizeTracker(0L) {
      public void read(long var1) {
      }
   };
   private final long max;
   private long read;

   public NBTSizeTracker(long var1) {
      this.max = var1;
   }

   public void read(long var1) {
      this.read += var1 / 8L;
      if (this.read > this.max) {
         throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.read + "bytes where max allowed: " + this.max);
      }
   }

   public static void readUTF(NBTSizeTracker var0, String var1) {
      var0.read(16L);
      if (var1 != null) {
         int var2 = var1.length();
         int var3 = 0;

         for(int var4 = 0; var4 < var2; ++var4) {
            char var5 = var1.charAt(var4);
            if (var5 >= 1 && var5 <= 127) {
               ++var3;
            } else if (var5 > 2047) {
               var3 += 3;
            } else {
               var3 += 2;
            }
         }

         var0.read((long)(8 * var3));
      }
   }
}
