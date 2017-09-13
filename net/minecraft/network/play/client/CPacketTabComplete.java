package net.minecraft.network.play.client;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

public class CPacketTabComplete implements Packet {
   private String message;
   private boolean hasTargetBlock;
   @Nullable
   private BlockPos targetBlock;

   public CPacketTabComplete() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketTabComplete(String var1, @Nullable BlockPos var2, boolean var3) {
      this.message = messageIn;
      this.targetBlock = targetBlockIn;
      this.hasTargetBlock = hasTargetBlockIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.message = buf.readString(32767);
      this.hasTargetBlock = buf.readBoolean();
      boolean flag = buf.readBoolean();
      if (flag) {
         this.targetBlock = buf.readBlockPos();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(StringUtils.substring(this.message, 0, 32767));
      buf.writeBoolean(this.hasTargetBlock);
      boolean flag = this.targetBlock != null;
      buf.writeBoolean(flag);
      if (flag) {
         buf.writeBlockPos(this.targetBlock);
      }

   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processTabComplete(this);
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
