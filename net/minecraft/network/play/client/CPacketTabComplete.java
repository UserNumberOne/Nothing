package net.minecraft.network.play.client;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

public class CPacketTabComplete implements Packet {
   private String message;
   private boolean hasTargetBlock;
   @Nullable
   private BlockPos targetBlock;

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.message = var1.readString(32767);
      this.hasTargetBlock = var1.readBoolean();
      boolean var2 = var1.readBoolean();
      if (var2) {
         this.targetBlock = var1.readBlockPos();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(StringUtils.substring(this.message, 0, 32767));
      var1.writeBoolean(this.hasTargetBlock);
      boolean var2 = this.targetBlock != null;
      var1.writeBoolean(var2);
      if (var2) {
         var1.writeBlockPos(this.targetBlock);
      }

   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processTabComplete(this);
   }

   public String getMessage() {
      return this.message;
   }

   @Nullable
   public BlockPos getTargetBlock() {
      return this.targetBlock;
   }

   public boolean hasTargetBlock() {
      return this.hasTargetBlock;
   }
}
