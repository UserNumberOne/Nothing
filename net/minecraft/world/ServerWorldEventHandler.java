package net.minecraft.world;

import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class ServerWorldEventHandler implements IWorldEventListener {
   private final MinecraftServer mcServer;
   private final WorldServer world;

   public ServerWorldEventHandler(MinecraftServer var1, WorldServer var2) {
      this.mcServer = var1;
      this.world = var2;
   }

   public void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
   }

   public void onEntityAdded(Entity var1) {
      this.world.getEntityTracker().track(var1);
      if (var1 instanceof EntityPlayerMP) {
         this.world.provider.onPlayerAdded((EntityPlayerMP)var1);
      }

   }

   public void onEntityRemoved(Entity var1) {
      this.world.getEntityTracker().untrack(var1);
      this.world.getScoreboard().removeEntity(var1);
      if (var1 instanceof EntityPlayerMP) {
         this.world.provider.onPlayerRemoved((EntityPlayerMP)var1);
      }

   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer var1, SoundEvent var2, SoundCategory var3, double var4, double var6, double var8, float var10, float var11) {
      this.mcServer.getPlayerList().sendToAllNearExcept(var1, var4, var6, var8, var10 > 1.0F ? (double)(16.0F * var10) : 16.0D, this.world.dimension, new SPacketSoundEffect(var2, var3, var4, var6, var8, var10, var11));
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
   }

   public void notifyBlockUpdate(World var1, BlockPos var2, IBlockState var3, IBlockState var4, int var5) {
      this.world.getPlayerChunkMap().markBlockForUpdate(var2);
   }

   public void notifyLightSet(BlockPos var1) {
   }

   public void playRecord(SoundEvent var1, BlockPos var2) {
   }

   public void playEvent(EntityPlayer var1, int var2, BlockPos var3, int var4) {
      this.mcServer.getPlayerList().sendToAllNearExcept(var1, (double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), 64.0D, this.world.dimension, new SPacketEffect(var2, var3, var4, false));
   }

   public void broadcastSound(int var1, BlockPos var2, int var3) {
      this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketEffect(var1, var2, var3, true));
   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
      Iterator var4 = this.mcServer.getPlayerList().getPlayers().iterator();
      EntityPlayer var5 = null;
      Entity var6 = this.world.getEntityByID(var1);
      if (var6 instanceof EntityPlayer) {
         var5 = (EntityPlayer)var6;
      }

      while(var4.hasNext()) {
         EntityPlayerMP var7 = (EntityPlayerMP)var4.next();
         if (var7 != null && var7.world == this.world && var7.getEntityId() != var1) {
            double var8 = (double)var2.getX() - var7.posX;
            double var10 = (double)var2.getY() - var7.posY;
            double var12 = (double)var2.getZ() - var7.posZ;
            if ((var5 == null || !(var5 instanceof EntityPlayerMP) || var7.getBukkitEntity().canSee(((EntityPlayerMP)var5).getBukkitEntity())) && var8 * var8 + var10 * var10 + var12 * var12 < 1024.0D) {
               var7.connection.sendPacket(new SPacketBlockBreakAnim(var1, var2, var3));
            }
         }
      }

   }
}
