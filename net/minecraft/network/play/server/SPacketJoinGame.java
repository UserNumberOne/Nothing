package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketJoinGame implements Packet {
   private int playerId;
   private boolean hardcoreMode;
   private GameType gameType;
   private int dimension;
   private EnumDifficulty difficulty;
   private int maxPlayers;
   private WorldType worldType;
   private boolean reducedDebugInfo;

   public SPacketJoinGame() {
   }

   public SPacketJoinGame(int var1, GameType var2, boolean var3, int var4, EnumDifficulty var5, int var6, WorldType var7, boolean var8) {
      this.playerId = var1;
      this.dimension = var4;
      this.difficulty = var5;
      this.gameType = var2;
      this.maxPlayers = var6;
      this.hardcoreMode = var3;
      this.worldType = var7;
      this.reducedDebugInfo = var8;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.playerId = var1.readInt();
      int var2 = var1.readUnsignedByte();
      this.hardcoreMode = (var2 & 8) == 8;
      var2 = var2 & -9;
      this.gameType = GameType.getByID(var2);
      this.dimension = var1.readInt();
      this.difficulty = EnumDifficulty.getDifficultyEnum(var1.readUnsignedByte());
      this.maxPlayers = var1.readUnsignedByte();
      this.worldType = WorldType.parseWorldType(var1.readString(16));
      if (this.worldType == null) {
         this.worldType = WorldType.DEFAULT;
      }

      this.reducedDebugInfo = var1.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.playerId);
      int var2 = this.gameType.getID();
      if (this.hardcoreMode) {
         var2 |= 8;
      }

      var1.writeByte(var2);
      var1.writeInt(this.dimension);
      var1.writeByte(this.difficulty.getDifficultyId());
      var1.writeByte(this.maxPlayers);
      var1.writeString(this.worldType.getName());
      var1.writeBoolean(this.reducedDebugInfo);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleJoinGame(this);
   }

   @SideOnly(Side.CLIENT)
   public int getPlayerId() {
      return this.playerId;
   }

   @SideOnly(Side.CLIENT)
   public boolean isHardcoreMode() {
      return this.hardcoreMode;
   }

   @SideOnly(Side.CLIENT)
   public GameType getGameType() {
      return this.gameType;
   }

   @SideOnly(Side.CLIENT)
   public int getDimension() {
      return this.dimension;
   }

   @SideOnly(Side.CLIENT)
   public EnumDifficulty getDifficulty() {
      return this.difficulty;
   }

   @SideOnly(Side.CLIENT)
   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   @SideOnly(Side.CLIENT)
   public WorldType getWorldType() {
      return this.worldType;
   }

   @SideOnly(Side.CLIENT)
   public boolean isReducedDebugInfo() {
      return this.reducedDebugInfo;
   }
}
