package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FrameTimer {
   private final long[] frames = new long[240];
   private int lastIndex;
   private int counter;
   private int index;

   public void addFrame(long var1) {
      this.frames[this.index] = var1;
      ++this.index;
      if (this.index == 240) {
         this.index = 0;
      }

      if (this.counter < 240) {
         this.lastIndex = 0;
         ++this.counter;
      } else {
         this.lastIndex = this.parseIndex(this.index + 1);
      }

   }

   public int getLagometerValue(long var1, int var3) {
      double var4 = (double)var1 / 1.6666666E7D;
      return (int)(var4 * (double)var3);
   }

   public int getLastIndex() {
      return this.lastIndex;
   }

   public int getIndex() {
      return this.index;
   }

   public int parseIndex(int var1) {
      return var1 % 240;
   }

   public long[] getFrames() {
      return this.frames;
   }
}
