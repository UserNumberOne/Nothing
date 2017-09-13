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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEnderPearl extends EntityThrowable {
   private EntityLivingBase thrower;

   public EntityEnderPearl(World var1) {
      super(var1);
   }

   public EntityEnderPearl(World var1, EntityLivingBase var2) {
      super(var1, var2);
      this.thrower = var2;
   }

   @SideOnly(Side.CLIENT)
   public EntityEnderPearl(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
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
            TileEntityEndGateway var9 = (TileEntityEndGateway)var4;
            if (var2 != null) {
               var9.teleportEntity(var2);
               this.setDead();
               return;
            }

            var9.teleportEntity(this);
            return;
         }
      }

      for(int var6 = 0; var6 < 32; ++var6) {
         this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY + this.rand.nextDouble() * 2.0D, this.posZ, this.rand.nextGaussian(), 0.0D, this.rand.nextGaussian());
      }

      if (!this.world.isRemote) {
         if (var2 instanceof EntityPlayerMP) {
            EntityPlayerMP var7 = (EntityPlayerMP)var2;
            if (var7.connection.getNetworkManager().isChannelOpen() && var7.world == this.world && !var7.isPlayerSleeping()) {
               EnderTeleportEvent var8 = new EnderTeleportEvent(var7, this.posX, this.posY, this.posZ, 5.0F);
               if (!MinecraftForge.EVENT_BUS.post(var8)) {
                  if (this.rand.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning")) {
                     EntityEndermite var5 = new EntityEndermite(this.world);
                     var5.setSpawnedByPlayer(true);
                     var5.setLocationAndAngles(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
                     this.world.spawnEntity(var5);
                  }

                  if (var2.isRiding()) {
                     var2.dismountRidingEntity();
                  }

                  var2.setPositionAndUpdate(var8.getTargetX(), var8.getTargetY(), var8.getTargetZ());
                  var2.fallDistance = 0.0F;
                  var2.attackEntityFrom(DamageSource.fall, var8.getAttackDamage());
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
