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
      this.playerId = playerIdIn;
      this.dimension = dimensionIn;
      this.difficulty = difficultyIn;
      this.gameType = gameTypeIn;
      this.maxPlayers = maxPlayersIn;
      this.hardcoreMode = hardcoreModeIn;
      this.worldType = worldTypeIn;
      this.reducedDebugInfo = reducedDebugInfoIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.playerId = buf.readInt();
      int i = buf.readUnsignedByte();
      this.hardcoreMode = (i & 8) == 8;
      i = i & -9;
      this.gameType = GameType.getByID(i);
      this.dimension = buf.readInt();
      this.difficulty = EnumDifficulty.getDifficultyEnum(buf.readUnsignedByte());
      this.maxPlayers = buf.readUnsignedByte();
      this.worldType = WorldType.parseWorldType(buf.readString(16));
      if (this.worldType == null) {
         this.worldType = WorldType.DEFAULT;
      }

      this.reducedDebugInfo = buf.readBoolean();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeInt(this.playerId);
      int i = this.gameType.getID();
      if (this.hardcoreMode) {
         i |= 8;
      }

      buf.writeByte(i);
      buf.writeInt(this.dimension);
      buf.writeByte(this.difficulty.getDifficultyId());
      buf.writeByte(this.maxPlayers);
      buf.writeString(this.worldType.getName());
      buf.writeBoolean(this.reducedDebugInfo);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleJoinGame(this);
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
