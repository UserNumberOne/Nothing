package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class EntityFishHook extends Entity {
   private static final DataParameter DATA_HOOKED_ENTITY = EntityDataManager.createKey(EntityFishHook.class, DataSerializers.VARINT);
   private BlockPos pos = new BlockPos(-1, -1, -1);
   private Block inTile;
   private boolean inGround;
   public EntityPlayer angler;
   private int ticksInGround;
   private int ticksInAir;
   private int ticksCatchable;
   private int ticksCaughtDelay;
   private int ticksCatchableDelay;
   private float fishApproachAngle;
   private int fishPosRotationIncrements;
   private double fishX;
   private double fishY;
   private double fishZ;
   private double fishYaw;
   private double fishPitch;
   public Entity caughtEntity;

   public EntityFishHook(World world) {
      super(world);
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
   }

   public EntityFishHook(World world, EntityPlayer entityhuman) {
      super(world);
      this.ignoreFrustumCheck = true;
      this.angler = entityhuman;
      this.angler.fishEntity = this;
      this.setSize(0.25F, 0.25F);
      this.setLocationAndAngles(entityhuman.posX, entityhuman.posY + (double)entityhuman.getEyeHeight(), entityhuman.posZ, entityhuman.rotationYaw, entityhuman.rotationPitch);
      this.posX -= (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * 0.16F);
      this.posY -= 0.10000000149011612D;
      this.posZ -= (double)(MathHelper.sin(this.rotationYaw * 0.017453292F) * 0.16F);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.motionX = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * 0.4F);
      this.motionZ = (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * 0.4F);
      this.motionY = (double)(-MathHelper.sin(this.rotationPitch * 0.017453292F) * 0.4F);
      this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
   }

   protected void entityInit() {
      this.getDataManager().register(DATA_HOOKED_ENTITY, Integer.valueOf(0));
   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      if (DATA_HOOKED_ENTITY.equals(datawatcherobject)) {
         int i = ((Integer)this.getDataManager().get(DATA_HOOKED_ENTITY)).intValue();
         if (i > 0 && this.caughtEntity != null) {
            this.caughtEntity = null;
         }
      }

      super.notifyDataManagerChange(datawatcherobject);
   }

   public void handleHookCasting(double d0, double d1, double d2, float f, float f1) {
      float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      d0 = d0 / (double)f2;
      d1 = d1 / (double)f2;
      d2 = d2 / (double)f2;
      d0 = d0 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d1 = d1 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d0 = d0 * (double)f;
      d1 = d1 * (double)f;
      d2 = d2 * (double)f;
      this.motionX = d0;
      this.motionY = d1;
      this.motionZ = d2;
      float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
      this.rotationYaw = (float)(MathHelper.atan2(d0, d2) * 57.2957763671875D);
      this.rotationPitch = (float)(MathHelper.atan2(d1, (double)f3) * 57.2957763671875D);
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.ticksInGround = 0;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         int i = ((Integer)this.getDataManager().get(DATA_HOOKED_ENTITY)).intValue();
         if (i > 0 && this.caughtEntity == null) {
            this.caughtEntity = this.world.getEntityByID(i - 1);
         }
      } else {
         ItemStack itemstack = this.angler.getHeldItemMainhand();
         if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || itemstack.getItem() != Items.FISHING_ROD || this.getDistanceSqToEntity(this.angler) > 1024.0D) {
            this.setDead();
            this.angler.fishEntity = null;
            return;
         }
      }

      if (this.caughtEntity != null) {
         if (!this.caughtEntity.isDead) {
            this.posX = this.caughtEntity.posX;
            double d0 = (double)this.caughtEntity.height;
            this.posY = this.caughtEntity.getEntityBoundingBox().minY + d0 * 0.8D;
            this.posZ = this.caughtEntity.posZ;
            return;
         }

         this.caughtEntity = null;
      }

      if (this.fishPosRotationIncrements > 0) {
         double d1 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
         double d2 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
         double d3 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
         double d4 = MathHelper.wrapDegrees(this.fishYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + d4 / (double)this.fishPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
         --this.fishPosRotationIncrements;
         this.setPosition(d1, d2, d3);
         this.setRotation(this.rotationYaw, this.rotationPitch);
      } else {
         if (this.inGround) {
            if (this.world.getBlockState(this.pos).getBlock() == this.inTile) {
               ++this.ticksInGround;
               if (this.ticksInGround == 1200) {
                  this.setDead();
               }

               return;
            }

            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
         } else {
            ++this.ticksInAir;
         }

         if (!this.world.isRemote) {
            Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3d, vec3d1);
            vec3d = new Vec3d(this.posX, this.posY, this.posZ);
            vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (movingobjectposition != null) {
               vec3d1 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
            double d5 = 0.0D;

            for(int j = 0; j < list.size(); ++j) {
               Entity entity1 = (Entity)list.get(j);
               if (this.canBeHooked(entity1) && (entity1 != this.angler || this.ticksInAir >= 5)) {
                  AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz(0.30000001192092896D);
                  RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
                  if (movingobjectposition1 != null) {
                     double d6 = vec3d.squareDistanceTo(movingobjectposition1.hitVec);
                     if (d6 < d5 || d5 == 0.0D) {
                        entity = entity1;
                        d5 = d6;
                     }
                  }
               }
            }

            if (entity != null) {
               movingobjectposition = new RayTraceResult(entity);
            }

            if (movingobjectposition != null) {
               CraftEventFactory.callProjectileHitEvent(this);
               if (movingobjectposition.entityHit != null) {
                  this.caughtEntity = movingobjectposition.entityHit;
                  this.getDataManager().set(DATA_HOOKED_ENTITY, Integer.valueOf(this.caughtEntity.getEntityId() + 1));
               } else {
                  this.inGround = true;
               }
            }
         }

         if (!this.inGround) {
            this.move(this.motionX, this.motionY, this.motionZ);
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

            for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
               ;
            }

            while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
               this.prevRotationPitch += 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
               this.prevRotationYaw -= 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
               this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float f1 = 0.92F;
            if (this.onGround || this.isCollidedHorizontally) {
               f1 = 0.5F;
            }

            double d7 = 0.0D;

            for(int k = 0; k < 5; ++k) {
               AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
               double d8 = axisalignedbb1.maxY - axisalignedbb1.minY;
               double d9 = axisalignedbb1.minY + d8 * (double)k / 5.0D;
               double d6 = axisalignedbb1.minY + d8 * (double)(k + 1) / 5.0D;
               AxisAlignedBB axisalignedbb2 = new AxisAlignedBB(axisalignedbb1.minX, d9, axisalignedbb1.minZ, axisalignedbb1.maxX, d6, axisalignedbb1.maxZ);
               if (this.world.isAABBInMaterial(axisalignedbb2, Material.WATER)) {
                  d7 += 0.2D;
               }
            }

            if (!this.world.isRemote && d7 > 0.0D) {
               WorldServer worldserver = (WorldServer)this.world;
               int l = 1;
               BlockPos blockposition = (new BlockPos(this)).up();
               if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(blockposition)) {
                  l = 2;
               }

               if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(blockposition)) {
                  --l;
               }

               if (this.ticksCatchable > 0) {
                  --this.ticksCatchable;
                  if (this.ticksCatchable <= 0) {
                     this.ticksCaughtDelay = 0;
                     this.ticksCatchableDelay = 0;
                     PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.FAILED_ATTEMPT);
                     this.world.getServer().getPluginManager().callEvent(playerFishEvent);
                  }
               } else if (this.ticksCatchableDelay > 0) {
                  this.ticksCatchableDelay -= l;
                  if (this.ticksCatchableDelay <= 0) {
                     PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.BITE);
                     this.world.getServer().getPluginManager().callEvent(playerFishEvent);
                     if (playerFishEvent.isCancelled()) {
                        return;
                     }

                     this.motionY -= 0.20000000298023224D;
                     this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                     float f2 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);
                     worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(f2 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(f2 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     this.ticksCatchable = MathHelper.getInt(this.rand, 10, 30);
                  } else {
                     this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                     float f2 = this.fishApproachAngle * 0.017453292F;
                     float f3 = MathHelper.sin(f2);
                     float f4 = MathHelper.cos(f2);
                     double d6 = this.posX + (double)(f3 * (float)this.ticksCatchableDelay * 0.1F);
                     double d11 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double d10 = this.posZ + (double)(f4 * (float)this.ticksCatchableDelay * 0.1F);
                     Block block = worldserver.getBlockState(new BlockPos((int)d6, (int)d11 - 1, (int)d10)).getBlock();
                     if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                        if (this.rand.nextFloat() < 0.15F) {
                           worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d6, d11 - 0.10000000149011612D, d10, 1, (double)f3, 0.1D, (double)f4, 0.0D);
                        }

                        float f5 = f3 * 0.04F;
                        float f6 = f4 * 0.04F;
                        worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d6, d11, d10, 0, (double)f6, 0.01D, (double)(-f5), 1.0D);
                        worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d6, d11, d10, 0, (double)(-f6), 0.01D, (double)f5, 1.0D);
                     }
                  }
               } else if (this.ticksCaughtDelay > 0) {
                  this.ticksCaughtDelay -= l;
                  float f2 = 0.15F;
                  if (this.ticksCaughtDelay < 20) {
                     f2 = (float)((double)f2 + (double)(20 - this.ticksCaughtDelay) * 0.05D);
                  } else if (this.ticksCaughtDelay < 40) {
                     f2 = (float)((double)f2 + (double)(40 - this.ticksCaughtDelay) * 0.02D);
                  } else if (this.ticksCaughtDelay < 60) {
                     f2 = (float)((double)f2 + (double)(60 - this.ticksCaughtDelay) * 0.01D);
                  }

                  if (this.rand.nextFloat() < f2) {
                     float f3 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292F;
                     float f4 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                     double d6 = this.posX + (double)(MathHelper.sin(f3) * f4 * 0.1F);
                     double d11 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double d10 = this.posZ + (double)(MathHelper.cos(f3) * f4 * 0.1F);
                     Block block = worldserver.getBlockState(new BlockPos((int)d6, (int)d11 - 1, (int)d10)).getBlock();
                     if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                        worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d6, d11, d10, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
                     }
                  }

                  if (this.ticksCaughtDelay <= 0) {
                     this.fishApproachAngle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
                     this.ticksCatchableDelay = MathHelper.getInt(this.rand, 20, 80);
                  }
               } else {
                  this.ticksCaughtDelay = MathHelper.getInt(this.rand, 100, 900);
                  this.ticksCaughtDelay -= EnchantmentHelper.getLureModifier(this.angler) * 20 * 5;
               }

               if (this.ticksCatchable > 0) {
                  this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2D;
               }
            }

            double d5 = d7 * 2.0D - 1.0D;
            this.motionY += 0.03999999910593033D * d5;
            if (d7 > 0.0D) {
               f1 = (float)((double)f1 * 0.9D);
               this.motionY *= 0.8D;
            }

            this.motionX *= (double)f1;
            this.motionY *= (double)f1;
            this.motionZ *= (double)f1;
            this.setPosition(this.posX, this.posY, this.posZ);
         }
      }

   }

   protected boolean canBeHooked(Entity entity) {
      return entity.canBeCollidedWith() || entity instanceof EntityItem;
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("xTile", this.pos.getX());
      nbttagcompound.setInteger("yTile", this.pos.getY());
      nbttagcompound.setInteger("zTile", this.pos.getZ());
      ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
      nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.pos = new BlockPos(nbttagcompound.getInteger("xTile"), nbttagcompound.getInteger("yTile"), nbttagcompound.getInteger("zTile"));
      if (nbttagcompound.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(nbttagcompound.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(nbttagcompound.getByte("inTile") & 255);
      }

      this.inGround = nbttagcompound.getByte("inGround") == 1;
   }

   public int handleHookRetraction() {
      if (this.world.isRemote) {
         return 0;
      } else {
         int i = 0;
         if (this.caughtEntity != null) {
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), this.caughtEntity.getBukkitEntity(), (Fish)this.getBukkitEntity(), State.CAUGHT_ENTITY);
            this.world.getServer().getPluginManager().callEvent(playerFishEvent);
            if (playerFishEvent.isCancelled()) {
               return 0;
            }

            this.bringInHookedEntity();
            this.world.setEntityState(this, (byte)31);
            i = this.caughtEntity instanceof EntityItem ? 3 : 5;
         } else if (this.ticksCatchable > 0) {
            LootContext.Builder loottableinfo_a = new LootContext.Builder((WorldServer)this.world);
            loottableinfo_a.withLuck((float)EnchantmentHelper.getLuckOfSeaModifier(this.angler) + this.angler.getLuck());

            for(ItemStack itemstack : this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, loottableinfo_a.build())) {
               EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
               PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), entityitem.getBukkitEntity(), (Fish)this.getBukkitEntity(), State.CAUGHT_FISH);
               playerFishEvent.setExpToDrop(this.rand.nextInt(6) + 1);
               this.world.getServer().getPluginManager().callEvent(playerFishEvent);
               if (playerFishEvent.isCancelled()) {
                  return 0;
               }

               double d0 = this.angler.posX - this.posX;
               double d1 = this.angler.posY - this.posY;
               double d2 = this.angler.posZ - this.posZ;
               double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
               entityitem.motionX = d0 * 0.1D;
               entityitem.motionY = d1 * 0.1D + (double)MathHelper.sqrt(d3) * 0.08D;
               entityitem.motionZ = d2 * 0.1D;
               this.world.spawnEntity(entityitem);
               if (playerFishEvent.getExpToDrop() > 0) {
                  this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, playerFishEvent.getExpToDrop()));
               }
            }

            i = 1;
         }

         if (this.inGround) {
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.IN_GROUND);
            this.world.getServer().getPluginManager().callEvent(playerFishEvent);
            if (playerFishEvent.isCancelled()) {
               return 0;
            }

            i = 2;
         }

         if (i == 0) {
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.FAILED_ATTEMPT);
            this.world.getServer().getPluginManager().callEvent(playerFishEvent);
            if (playerFishEvent.isCancelled()) {
               return 0;
            }
         }

         this.setDead();
         this.angler.fishEntity = null;
         return i;
      }
   }

   protected void bringInHookedEntity() {
      double d0 = this.angler.posX - this.posX;
      double d1 = this.angler.posY - this.posY;
      double d2 = this.angler.posZ - this.posZ;
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      this.caughtEntity.motionX += d0 * 0.1D;
      this.caughtEntity.motionY += d1 * 0.1D + (double)MathHelper.sqrt(d3) * 0.08D;
      this.caughtEntity.motionZ += d2 * 0.1D;
   }

   public void setDead() {
      super.setDead();
      if (this.angler != null) {
         this.angler.fishEntity = null;
      }

   }
}
