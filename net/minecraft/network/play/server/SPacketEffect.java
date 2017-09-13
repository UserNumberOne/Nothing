package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEffect implements Packet {
   private int soundType;
   private BlockPos soundPos;
   private int soundData;
   private boolean serverWide;

   public SPacketEffect() {
   }

   public SPacketEffect(int var1, BlockPos var2, int var3, boolean var4) {
      this.soundType = soundTypeIn;
      this.soundPos = soundPosIn;
      this.soundData = soundDataIn;
      this.serverWide = serverWideIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.soundType = buf.readInt();
      this.soundPos = buf.readBlockPos();
      this.soundData = buf.readInt();
      this.serverWide = buf.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeInt(this.soundType);
      buf.writeBlockPos(this.soundPos);
      buf.writeInt(this.soundData);
      buf.writeBoolean(this.serverWide);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEffect(this);
   }

   @SideOnly(Side.CLIENT)
   public boolean isSoundServerwide() {
      return this.serverWide;
   }

   @SideOnly(Side.CLIENT)
   public int getSoundType() {
      return this.soundType;
   }

   @SideOnly(Side.CLIENT)
   public int getSoundData() {
      return this.soundData;
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getSoundPos() {
      return this.soundPos;
   }
}
