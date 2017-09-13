package net.minecraft.block;

import net.minecraft.util.math.BlockPos;

public class BlockEventData {
   private final BlockPos position;
   private final Block blockType;
   private final int eventID;
   private final int eventParameter;

   public BlockEventData(BlockPos var1, Block var2, int var3, int var4) {
      this.position = pos;
      this.eventID = eventId;
      this.eventParameter = p_i45756_4_;
      this.blockType = blockType;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getEventID() {
      return this.eventID;
   }

   public int getEventParameter() {
      return this.eventParameter;
   }

   public Block getBlock() {
      return this.blockType;
   }

   public boolean equals(Object var1) {
      if (!(p_equals_1_ instanceof BlockEventData)) {
         return false;
      } else {
         BlockEventData blockeventdata = (BlockEventData)p_equals_1_;
         return this.position.equals(blockeventdata.position) && this.eventID == blockeventdata.eventID && this.eventParameter == blockeventdata.eventParameter && this.blockType == blockeventdata.blockType;
      }
   }

   public String toString() {
      return "TE(" + this.position + ")," + this.eventID + "," + this.eventParameter + "," + this.blockType;
   }
}
