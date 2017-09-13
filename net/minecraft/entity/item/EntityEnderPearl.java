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
      super(worldIn);
   }

   public EntityEnderPearl(World var1, EntityLivingBase var2) {
      super(worldIn, throwerIn);
      this.thrower = throwerIn;
   }

   @SideOnly(Side.CLIENT)
   public EntityEnderPearl(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesEnderPearl(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(fixer, "ThrownEnderpearl");
   }

   protected void onImpact(RayTraceResult var1) {
      EntityLivingBase entitylivingbase = this.getThrower();
      if (result.entityHit != null) {
         if (result.entityHit == this.thrower) {
            return;
         }

         result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, entitylivingbase), 0.0F);
      }

      if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
         BlockPos blockpos = result.getBlockPos();
         TileEntity tileentity = this.world.getTileEntity(blockpos);
         if (tileentity instanceof TileEntityEndGateway) {
            TileEntityEndGateway tileentityendgateway = (TileEntityEndGateway)tileentity;
            if (entitylivingbase != null) {
               tileentityendgateway.teleportEntity(entitylivingbase);
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
         if (entitylivingbase instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)entitylivingbase;
            if (entityplayermp.connection.getNetworkManager().isChannelOpen() && entityplayermp.world == this.world && !entityplayermp.isPlayerSleeping()) {
               EnderTeleportEvent event = new EnderTeleportEvent(entityplayermp, this.posX, this.posY, this.posZ, 5.0F);
               if (!MinecraftForge.EVENT_BUS.post(event)) {
                  if (this.rand.nextFloat() < 0.05F && this.world.getGameRules().getBoolean("doMobSpawning")) {
                     EntityEndermite entityendermite = new EntityEndermite(this.world);
                     entityendermite.setSpawnedByPlayer(true);
                     entityendermite.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
                     this.world.spawnEntity(entityendermite);
                  }

                  if (entitylivingbase.isRiding()) {
                     entitylivingbase.dismountRidingEntity();
                  }

                  entitylivingbase.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                  entitylivingbase.fallDistance = 0.0F;
                  entitylivingbase.attackEntityFrom(DamageSource.fall, event.getAttackDamage());
               }
            }
         } else if (entitylivingbase != null) {
            entitylivingbase.setPositionAndUpdate(this.posX, this.posY, this.posZ);
            entitylivingbase.fallDistance = 0.0F;
         }

         this.setDead();
      }

   }

   public void onUpdate() {
      EntityLivingBase entitylivingbase = this.getThrower();
      if (entitylivingbase != null && entitylivingbase instanceof EntityPlayer && !entitylivingbase.isEntityAlive()) {
         this.setDead();
      } else {
         super.onUpdate();
      }

   }
}
