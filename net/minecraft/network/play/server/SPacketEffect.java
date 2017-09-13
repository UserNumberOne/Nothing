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
      this.soundType = var1;
      this.soundPos = var2;
      this.soundData = var3;
      this.serverWide = var4;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.soundType = var1.readInt();
      this.soundPos = var1.readBlockPos();
      this.soundData = var1.readInt();
      this.serverWide = var1.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.soundType);
      var1.writeBlockPos(this.soundPos);
      var1.writeInt(this.soundData);
      var1.writeBoolean(this.serverWide);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEffect(this);
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
