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
      this.pos = var1;
      this.lines = new String[]{var2[0].getUnformattedText(), var2[1].getUnformattedText(), var2[2].getUnformattedText(), var2[3].getUnformattedText()};
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.pos = var1.readBlockPos();
      this.lines = new String[4];

      for(int var2 = 0; var2 < 4; ++var2) {
         this.lines[var2] = var1.readString(384);
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.pos);

      for(int var2 = 0; var2 < 4; ++var2) {
         var1.writeString(this.lines[var2]);
      }

   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processUpdateSign(this);
   }

   public BlockPos getPosition() {
      return this.pos;
   }

   public String[] getLines() {
      return this.lines;
   }
}
