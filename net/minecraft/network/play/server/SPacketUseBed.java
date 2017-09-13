package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUseBed implements Packet {
   private int playerID;
   private BlockPos bedPos;

   public SPacketUseBed() {
   }

   public SPacketUseBed(EntityPlayer var1, BlockPos var2) {
      this.playerID = player.getEntityId();
      this.bedPos = posIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.playerID = buf.readVarInt();
      this.bedPos = buf.readBlockPos();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.playerID);
      buf.writeBlockPos(this.bedPos);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleUseBed(this);
   }

   @SideOnly(Side.CLIENT)
   public EntityPlayer getPlayer(World var1) {
      return (EntityPlayer)worldIn.getEntityByID(this.playerID);
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getBedPosition() {
      return this.bedPos;
   }
}
