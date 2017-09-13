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

   public EntityEnderPearl(World var1) {
      super(var1);
   }

   public EntityEnderPearl(World var1, EntityLivingBase var2) {
      super(var1, var2);
      this.thrower = var2;
   }

   public static void registerFixesEnderPearl(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(var0, "ThrownEnderpearl");
   }

   protected void onImpact(RayTraceResult var1) {
      EntityLivingBase var2 = this.getThrower();
      if (var1.entityHit != null) {
         if (var1.entityHit == this.thrower) {
            return;
         }

         var1.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, var2), 0.0F);
      }

      if (var1.typeOfHit == RayTraceResult.Type.BLOCK) {
         BlockPos var3 = var1.getBlockPos();
         TileEntity var4 = this.world.getTileEntity(var3);
         if (var4 instanceof TileEntityEndGateway) {
            TileEntityEndGateway var11 = (TileEntityEndGateway)var4;
            if (var2 != null) {
               var11.teleportEntity(var2);
               this.setDead();
               return;
            }

            var11.teleportEntity(this);
            return;
         }
      }

      for(int var8 = 0; var8 < 32; ++var8) {
         this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian());
      }

      if (!this.world.isRemote) {
         if (var2 instanceof EntityPlayerMP) {
            EntityPlayerMP var9 = (EntityPlayerMP)var2;
            if (var9.connection.getNetworkManager().isChannelOpen() && var9.world == this.world && !var9.isPlayerSleeping()) {
               CraftPlayer var10 = var9.getBukkitEntity();
               Location var5 = this.getBukkitEntity().getLocation();
               var5.setPitch(var10.getLocation().getPitch());
               var5.setYaw(var10.getLocation().getYaw());
               PlayerTeleportEvent var6 = new PlayerTeleportEvent(var10, var10.getLocation(), var5, TeleportCause.ENDER_PEARL);
               Bukkit.getPluginManager().callEvent(var6);
               if (!var6.isCancelled() && !var9.connection.isDisconnected()) {
                  if (this.rand.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning")) {
                     EntityEndermite var7 = new EntityEndermite(this.world);
                     var7.setSpawnedByPlayer(true);
                     var7.setLocationAndAngles(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
                     this.world.addEntity(var7, SpawnReason.ENDER_PEARL);
                  }

                  if (var2.isRiding()) {
                     var2.dismountRidingEntity();
                  }

                  var9.connection.teleport(var6.getTo());
                  var2.fallDistance = 0.0F;
                  CraftEventFactory.entityDamage = this;
                  var2.attackEntityFrom(DamageSource.fall, 5.0F);
                  CraftEventFactory.entityDamage = null;
               }
            }
         } else if (var2 != null) {
            var2.setPositionAndUpdate(this.posX, this.posY, this.posZ);
            var2.fallDistance = 0.0F;
         }

         this.setDead();
      }

   }

   public void onUpdate() {
      EntityLivingBase var1 = this.getThrower();
      if (var1 != null && var1 instanceof EntityPlayer && !var1.isEntityAlive()) {
         this.setDead();
      } else {
         super.onUpdate();
      }

   }
}
