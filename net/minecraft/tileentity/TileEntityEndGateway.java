package net.minecraft.tileentity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.WorldGenEndGateway;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TileEntityEndGateway extends TileEntity implements ITickable {
   private static final Logger LOG = LogManager.getLogger();
   private long age;
   private int teleportCooldown;
   public BlockPos exitPortal;
   public boolean exactTeleport;

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setLong("Age", this.age);
      if (this.exitPortal != null) {
         var1.setTag("ExitPortal", NBTUtil.createPosTag(this.exitPortal));
      }

      if (this.exactTeleport) {
         var1.setBoolean("ExactTeleport", this.exactTeleport);
      }

      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.age = var1.getLong("Age");
      if (var1.hasKey("ExitPortal", 10)) {
         this.exitPortal = NBTUtil.getPosFromTag(var1.getCompoundTag("ExitPortal"));
      }

      this.exactTeleport = var1.getBoolean("ExactTeleport");
   }

   public void update() {
      boolean var1 = this.isSpawning();
      boolean var2 = this.isCoolingDown();
      ++this.age;
      if (var2) {
         --this.teleportCooldown;
      } else if (!this.world.isRemote) {
         List var3 = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.getPos()));
         if (!var3.isEmpty()) {
            this.teleportEntity((Entity)var3.get(0));
         }
      }

      if (var1 != this.isSpawning() || var2 != this.isCoolingDown()) {
         this.markDirty();
      }

   }

   public boolean isSpawning() {
      return this.age < 200L;
   }

   public boolean isCoolingDown() {
      return this.teleportCooldown > 0;
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 8, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public void triggerCooldown() {
      if (!this.world.isRemote) {
         this.teleportCooldown = 20;
         this.world.addBlockEvent(this.getPos(), this.getBlockType(), 1, 0);
         this.markDirty();
      }

   }

   public boolean receiveClientEvent(int var1, int var2) {
      if (var1 == 1) {
         this.teleportCooldown = 20;
         return true;
      } else {
         return super.receiveClientEvent(var1, var2);
      }
   }

   public void teleportEntity(Entity var1) {
      if (!this.world.isRemote && !this.isCoolingDown()) {
         this.teleportCooldown = 100;
         if (this.exitPortal == null && this.world.provider instanceof WorldProviderEnd) {
            this.findExitPortal();
         }

         if (this.exitPortal != null) {
            BlockPos var2 = this.exactTeleport ? this.exitPortal : this.findExitPosition();
            if (var1 instanceof EntityPlayerMP) {
               CraftPlayer var3 = (CraftPlayer)var1.getBukkitEntity();
               Location var4 = new Location(this.world.getWorld(), (double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D);
               var4.setPitch(var3.getLocation().getPitch());
               var4.setYaw(var3.getLocation().getYaw());
               PlayerTeleportEvent var5 = new PlayerTeleportEvent(var3, var3.getLocation(), var4, TeleportCause.END_GATEWAY);
               Bukkit.getPluginManager().callEvent(var5);
               if (var5.isCancelled()) {
                  return;
               }

               ((EntityPlayerMP)var1).connection.teleport(var5.getTo());
               this.triggerCooldown();
               return;
            }

            var1.setPositionAndUpdate((double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D);
         }

         this.triggerCooldown();
      }

   }

   private BlockPos findExitPosition() {
      BlockPos var1 = findHighestBlock(this.world, this.exitPortal, 5, false);
      LOG.debug("Best exit position for portal at {} is {}", new Object[]{this.exitPortal, var1});
      return var1.up();
   }

   private void findExitPortal() {
      Vec3d var1 = (new Vec3d((double)this.getPos().getX(), 0.0D, (double)this.getPos().getZ())).normalize();
      Vec3d var2 = var1.scale(1024.0D);

      for(int var3 = 16; getChunk(this.world, var2).getTopFilledSegment() > 0 && var3-- > 0; var2 = var2.add(var1.scale(-16.0D))) {
         LOG.debug("Skipping backwards past nonempty chunk at {}", new Object[]{var2});
      }

      for(int var5 = 16; getChunk(this.world, var2).getTopFilledSegment() == 0 && var5-- > 0; var2 = var2.add(var1.scale(16.0D))) {
         LOG.debug("Skipping forward past empty chunk at {}", new Object[]{var2});
      }

      LOG.debug("Found chunk at {}", new Object[]{var2});
      Chunk var4 = getChunk(this.world, var2);
      this.exitPortal = findSpawnpointInChunk(var4);
      if (this.exitPortal == null) {
         this.exitPortal = new BlockPos(var2.xCoord + 0.5D, 75.0D, var2.zCoord + 0.5D);
         LOG.debug("Failed to find suitable block, settling on {}", new Object[]{this.exitPortal});
         (new WorldGenEndIsland()).generate(this.world, new Random(this.exitPortal.toLong()), this.exitPortal);
      } else {
         LOG.debug("Found block at {}", new Object[]{this.exitPortal});
      }

      this.exitPortal = findHighestBlock(this.world, this.exitPortal, 16, true);
      LOG.debug("Creating portal at {}", new Object[]{this.exitPortal});
      this.exitPortal = this.exitPortal.up(10);
      this.createExitPortal(this.exitPortal);
      this.markDirty();
   }

   private static BlockPos findHighestBlock(World var0, BlockPos var1, int var2, boolean var3) {
      BlockPos var4 = null;

      for(int var5 = -var2; var5 <= var2; ++var5) {
         for(int var6 = -var2; var6 <= var2; ++var6) {
            if (var5 != 0 || var6 != 0 || var3) {
               for(int var7 = 255; var7 > (var4 == null ? 0 : var4.getY()); --var7) {
                  BlockPos var8 = new BlockPos(var1.getX() + var5, var7, var1.getZ() + var6);
                  IBlockState var9 = var0.getBlockState(var8);
                  if (var9.isBlockNormalCube() && (var3 || var9.getBlock() != Blocks.BEDROCK)) {
                     var4 = var8;
                     break;
                  }
               }
            }
         }
      }

      return var4 == null ? var1 : var4;
   }

   private static Chunk getChunk(World var0, Vec3d var1) {
      return var0.getChunkFromChunkCoords(MathHelper.floor(var1.xCoord / 16.0D), MathHelper.floor(var1.zCoord / 16.0D));
   }

   @Nullable
   private static BlockPos findSpawnpointInChunk(Chunk var0) {
      BlockPos var1 = new BlockPos(var0.xPosition * 16, 30, var0.zPosition * 16);
      int var2 = var0.getTopFilledSegment() + 16 - 1;
      BlockPos var3 = new BlockPos(var0.xPosition * 16 + 16 - 1, var2, var0.zPosition * 16 + 16 - 1);
      BlockPos var4 = null;
      double var5 = 0.0D;

      for(BlockPos var8 : BlockPos.getAllInBox(var1, var3)) {
         IBlockState var9 = var0.getBlockState(var8);
         if (var9.getBlock() == Blocks.END_STONE && !var0.getBlockState(var8.up(1)).isBlockNormalCube() && !var0.getBlockState(var8.up(2)).isBlockNormalCube()) {
            double var10 = var8.distanceSqToCenter(0.0D, 0.0D, 0.0D);
            if (var4 == null || var10 < var5) {
               var4 = var8;
               var5 = var10;
            }
         }
      }

      return var4;
   }

   private void createExitPortal(BlockPos var1) {
      (new WorldGenEndGateway()).generate(this.world, new Random(), var1);
      TileEntity var2 = this.world.getTileEntity(var1);
      if (var2 instanceof TileEntityEndGateway) {
         TileEntityEndGateway var3 = (TileEntityEndGateway)var2;
         var3.exitPortal = new BlockPos(this.getPos());
         var3.markDirty();
      } else {
         LOG.warn("Couldn't save exit portal at {}", new Object[]{var1});
      }

   }
}
