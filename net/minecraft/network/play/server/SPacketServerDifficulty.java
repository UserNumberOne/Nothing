package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;

public class SPacketServerDifficulty implements Packet {
   private EnumDifficulty difficulty;
   private boolean difficultyLocked;

   public SPacketServerDifficulty() {
   }

   public SPacketServerDifficulty(EnumDifficulty var1, boolean var2) {
      this.difficulty = var1;
      this.difficultyLocked = var2;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleServerDifficulty(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.difficulty = EnumDifficulty.getDifficultyEnum(var1.readUnsignedByte());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.difficulty.getDifficultyId());
   }
}
