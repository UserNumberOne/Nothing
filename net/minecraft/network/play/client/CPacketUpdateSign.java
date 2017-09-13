package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketUpdateSign implements Packet {
   private BlockPos pos;
   private String[] lines;

   public CPacketUpdateSign() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketUpdateSign(BlockPos var1, ITextComponent[] var2) {
      this.pos = posIn;
      this.lines = new String[]{linesIn[0].getUnformattedText(), linesIn[1].getUnformattedText(), linesIn[2].getUnformattedText(), linesIn[3].getUnformattedText()};
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.pos = buf.readBlockPos();
      this.lines = new String[4];

      for(int i = 0; i < 4; ++i) {
         this.lines[i] = buf.readString(384);
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBlockPos(this.pos);

      for(int i = 0; i < 4; ++i) {
         buf.writeString(this.lines[i]);
      }

   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processUpdateSign(this);
   }

   public BlockPos getPosition() {
      return this.pos;
   }

   public String[] getLines() {
      return this.lines;
   }
}
