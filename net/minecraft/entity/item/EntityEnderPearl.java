package net.minecraft.entity.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class EntityEnderPearl extends EntityThrowable {
   private EntityLivingBase thrower;

   public EntityEnderPearl(World world) {
      super(world);
   }

   public EntityEnderPearl(World world, EntityLivingBase entityliving) {
      super(world, entityliving);
      this.thrower = entityliving;
   }

   public static void registerFixesEnderPearl(DataFixer dataconvertermanager) {
      EntityThrowable.registerFixesThrowable(dataconvertermanager, "ThrownEnderpearl");
   }

   protected void onImpact(RayTraceResult movingobjectposition) {
      EntityLivingBase entityliving = this.getThrower();
      if (movingobjectposition.entityHit != null) {
         if (movingobjectposition.entityHit == this.thrower) {
            return;
         }

         movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, entityliving), 0.0F);
      }

      if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
         BlockPos blockposition = movingobjectposition.getBlockPos();
         TileEntity tileentity = this.world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityEndGateway) {
            TileEntityEndGateway tileentityendgateway = (TileEntityEndGateway)tileentity;
            if (entityliving != null) {
               tileentityendgateway.teleportEntity(entityliving);
               this.setDead();
               return;
            }

            tileentityendgateway.teleportEntity(this);
            return;
         }
      }

      for(int i = 0; i < 32; ++i) {
         this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian());
      }

      if (!this.world.isRemote) {
         if (entityliving instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayer = (EntityPlayerMP)entityliving;
            if (entityplayer.connection.getNetworkManager().isChannelOpen() && entityplayer.world == this.world && !entityplayer.isPlayerSleeping()) {
               CraftPlayer player = entityplayer.getBukkitEntity();
               Location location = this.getBukkitEntity().getLocation();
               location.setPitch(player.getLocation().getPitch());
               location.setYaw(player.getLocation().getYaw());
               PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, TeleportCause.ENDER_PEARL);
               Bukkit.getPluginManager().callEvent(teleEvent);
               if (!teleEvent.isCancelled() && !entityplayer.connection.isDisconnected()) {
                  if (this.rand.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning")) {
                     EntityEndermite entityendermite = new EntityEndermite(this.world);
                     entityendermite.setSpawnedByPlayer(true);
                     entityendermite.setLocationAndAngles(entityliving.posX, entityliving.posY, entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
                     this.world.addEntity(entityendermite, SpawnReason.ENDER_PEARL);
                  }

                  if (entityliving.isRiding()) {
                     entityliving.dismountRidingEntity();
                  }

                  entityplayer.connection.teleport(teleEvent.getTo());
                  entityliving.fallDistance = 0.0F;
                  CraftEventFactory.entityDamage = this;
                  entityliving.attackEntityFrom(DamageSource.fall, 5.0F);
                  CraftEventFactory.entityDamage = null;
               }
            }
         } else if (entityliving != null) {
            entityliving.setPositionAndUpdate(this.posX, this.posY, this.posZ);
            entityliving.fallDistance = 0.0F;
         }

         this.setDead();
      }

   }

   public void onUpdate() {
      EntityLivingBase entityliving = this.getThrower();
      if (entityliving != null && entityliving instanceof EntityPlayer && !entityliving.isEntityAlive()) {
         this.setDead();
      } else {
         super.onUpdate();
      }

   }
}
