package net.minecraft.entity.effect;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityLightningBolt extends EntityWeatherEffect {
   private int lightningState;
   public long boltVertex;
   private int boltLivingTime;
   private final boolean effectOnly;
   public boolean isEffect;

   public EntityLightningBolt(World world, double d0, double d1, double d2, boolean flag) {
      super(world);
      this.isEffect = flag;
      this.setLocationAndAngles(d0, d1, d2, 0.0F, 0.0F);
      this.lightningState = 2;
      this.boltVertex = this.rand.nextLong();
      this.boltLivingTime = this.rand.nextInt(3) + 1;
      this.effectOnly = flag;
      BlockPos blockposition = new BlockPos(this);
      if (!flag && !world.isRemote && world.getGameRules().getBoolean("doFireTick") && (world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD) && world.isAreaLoaded(blockposition, 10)) {
         if (world.getBlockState(blockposition).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(world, blockposition) && !CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
            world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
         }

         for(int i = 0; i < 4; ++i) {
            BlockPos blockposition1 = blockposition.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);
            if (world.getBlockState(blockposition1).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(world, blockposition1) && !CraftEventFactory.callBlockIgniteEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), this).isCancelled()) {
               world.setBlockState(blockposition1, Blocks.FIRE.getDefaultState());
            }
         }
      }

   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.WEATHER;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.lightningState == 2) {
         float pitch = 0.8F + this.rand.nextFloat() * 0.2F;
         int viewDistance = ((WorldServer)this.world).getServer().getViewDistance() * 16;

         for(EntityPlayerMP player : this.world.playerEntities) {
            double deltaX = this.posX - player.posX;
            double deltaZ = this.posZ - player.posZ;
            double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            if (distanceSquared > (double)(viewDistance * viewDistance)) {
               double deltaLength = Math.sqrt(distanceSquared);
               double relativeX = player.posX + deltaX / deltaLength * (double)viewDistance;
               double relativeZ = player.posZ + deltaZ / deltaLength * (double)viewDistance;
               player.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, relativeX, this.posY, relativeZ, 10000.0F, pitch));
            } else {
               player.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, this.posX, this.posY, this.posZ, 10000.0F, pitch));
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
      }

      --this.lightningState;
      if (this.lightningState < 0) {
         if (this.boltLivingTime == 0) {
            this.setDead();
         } else if (this.lightningState < -this.rand.nextInt(10)) {
            --this.boltLivingTime;
            this.lightningState = 1;
            if (!this.effectOnly && !this.world.isRemote) {
               this.boltVertex = this.rand.nextLong();
               BlockPos blockposition = new BlockPos(this);
               if (this.world.getGameRules().getBoolean("doFireTick") && this.world.isAreaLoaded(blockposition, 10) && this.world.getBlockState(blockposition).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(this.world, blockposition) && !this.isEffect && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                  this.world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
               }
            }
         }
      }

      if (this.lightningState >= 0 && !this.isEffect) {
         if (this.world.isRemote) {
            this.world.setLastLightningBolt(2);
         } else if (!this.effectOnly) {
            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - 3.0D, this.posY - 3.0D, this.posZ - 3.0D, this.posX + 3.0D, this.posY + 6.0D + 3.0D, this.posZ + 3.0D));

            for(int i = 0; i < list.size(); ++i) {
               Entity entity = (Entity)list.get(i);
               entity.onStruckByLightning(this);
            }
         }
      }

   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
   }
}
