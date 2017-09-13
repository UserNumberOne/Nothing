package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ServerWorldEventHandler implements IWorldEventListener {
   private final MinecraftServer mcServer;
   private final WorldServer world;

   public ServerWorldEventHandler(MinecraftServer var1, WorldServer var2) {
      this.mcServer = mcServerIn;
      this.world = worldServerIn;
   }

   public void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
   }

   public void onEntityAdded(Entity var1) {
      this.world.getEntityTracker().track(entityIn);
      if (entityIn instanceof EntityPlayerMP) {
         this.world.provider.onPlayerAdded((EntityPlayerMP)entityIn);
      }

   }

   public void onEntityRemoved(Entity var1) {
      this.world.getEntityTracker().untrack(entityIn);
      this.world.getScoreboard().removeEntity(entityIn);
      if (entityIn instanceof EntityPlayerMP) {
         this.world.provider.onPlayerRemoved((EntityPlayerMP)entityIn);
      }

   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer var1, SoundEvent var2, SoundCategory var3, double var4, double var6, double var8, float var10, float var11) {
      this.mcServer.getPlayerList().sendToAllNearExcept(player, x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, this.world.provider.getDimension(), new SPacketSoundEffect(soundIn, category, x, y, z, volume, pitch));
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public void notifyBlockUpdate(World var1, BlockPos var2, IBlockState var3, IBlockState var4, int var5) {
      this.world.getPlayerChunkMap().markBlockForUpdate(pos);
   }

   public void notifyLightSet(BlockPos var1) {
   }

   public void playRecord(SoundEvent var1, BlockPos var2) {
   }

   public void playEvent(EntityPlayer var1, int var2, BlockPos var3, int var4) {
      this.mcServer.getPlayerList().sendToAllNearExcept(player, (double)blockPosIn.getX(), (double)blockPosIn.getY(), (double)blockPosIn.getZ(), 64.0D, this.world.provider.getDimension(), new SPacketEffect(type, blockPosIn, data, false));
   }

   public void broadcastSound(int var1, BlockPos var2, int var3) {
      this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketEffect(soundID, pos, data, true));
   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
      for(EntityPlayerMP entityplayermp : this.mcServer.getPlayerList().getPlayers()) {
         if (entityplayermp != null && entityplayermp.world == this.world && entityplayermp.getEntityId() != breakerId) {
            double d0 = (double)pos.getX() - entityplayermp.posX;
            double d1 = (double)pos.getY() - entityplayermp.posY;
            double d2 = (double)pos.getZ() - entityplayermp.posZ;
            if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
               entityplayermp.connection.sendPacket(new SPacketBlockBreakAnim(breakerId, pos, progress));
            }
         }
      }

   }
}
