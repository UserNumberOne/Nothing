package net.minecraft.world;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class NextTickListEntry implements Comparable {
   private static long nextTickEntryID;
   private final Block block;
   public final BlockPos position;
   public long scheduledTime;
   public int priority;
   private final long tickEntryID;

   public NextTickListEntry(BlockPos var1, Block var2) {
      this.tickEntryID = (long)(nextTickEntryID++);
      this.position = positionIn;
      this.block = blockIn;
   }

   public boolean equals(Object var1) {
      if (!(p_equals_1_ instanceof NextTickListEntry)) {
         return false;
      } else {
         NextTickListEntry nextticklistentry = (NextTickListEntry)p_equals_1_;
         return this.position.equals(nextticklistentry.position) && Block.isEqualTo(this.block, nextticklistentry.block);
      }
   }

   public int hashCode() {
      return this.position.hashCode();
   }

   public NextTickListEntry setScheduledTime(long var1) {
      this.scheduledTime = scheduledTimeIn;
      return this;
   }

   public void setPriority(int var1) {
      this.priority = priorityIn;
   }

   public int compareTo(NextTickListEntry var1) {
      return this.scheduledTime < p_compareTo_1_.scheduledTime ? -1 : (this.scheduledTime > p_compareTo_1_.scheduledTime ? 1 : (this.priority != p_compareTo_1_.priority ? this.priority - p_compareTo_1_.priority : (this.tickEntryID < p_compareTo_1_.tickEntryID ? -1 : (this.tickEntryID > p_compareTo_1_.tickEntryID ? 1 : 0))));
   }

   public String toString() {
      return Block.getIdFromBlock(this.block) + ": " + this.position + ", " + this.scheduledTime + ", " + this.priority + ", " + this.tickEntryID;
   }

   public Block getBlock() {
      return this.block;
   }
}
