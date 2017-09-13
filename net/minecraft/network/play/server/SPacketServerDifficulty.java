package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketServerDifficulty implements Packet {
   private EnumDifficulty difficulty;
   private boolean difficultyLocked;

   public SPacketServerDifficulty() {
   }

   public SPacketServerDifficulty(EnumDifficulty var1, boolean var2) {
      this.difficulty = difficultyIn;
      this.difficultyLocked = difficultyLockedIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleServerDifficulty(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.difficulty = EnumDifficulty.getDifficultyEnum(buf.readUnsignedByte());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.difficulty.getDifficultyId());
   }

   @SideOnly(Side.CLIENT)
   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   @SideOnly(Side.CLIENT)
   public EnumDifficulty getDifficulty() {
      return this.difficulty;
   }
}
