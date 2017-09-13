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

   public EntityFishHook(World var1) {
      super(var1);
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
   }

   public EntityFishHook(World var1, EntityPlayer var2) {
      super(var1);
      this.ignoreFrustumCheck = true;
      this.angler = var2;
      this.angler.fishEntity = this;
      this.setSize(0.25F, 0.25F);
      this.setLocationAndAngles(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, var2.rotationYaw, var2.rotationPitch);
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

   public void notifyDataManagerChange(DataParameter var1) {
      if (DATA_HOOKED_ENTITY.equals(var1)) {
         int var2 = ((Integer)this.getDataManager().get(DATA_HOOKED_ENTITY)).intValue();
         if (var2 > 0 && this.caughtEntity != null) {
            this.caughtEntity = null;
         }
      }

      super.notifyDataManagerChange(var1);
   }

   public void handleHookCasting(double var1, double var3, double var5, float var7, float var8) {
      float var9 = MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      var1 = var1 / (double)var9;
      var3 = var3 / (double)var9;
      var5 = var5 / (double)var9;
      var1 = var1 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var3 = var3 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var5 = var5 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var1 = var1 * (double)var7;
      var3 = var3 * (double)var7;
      var5 = var5 * (double)var7;
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      float var10 = MathHelper.sqrt(var1 * var1 + var5 * var5);
      this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.2957763671875D);
      this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var10) * 57.2957763671875D);
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.ticksInGround = 0;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         int var1 = ((Integer)this.getDataManager().get(DATA_HOOKED_ENTITY)).intValue();
         if (var1 > 0 && this.caughtEntity == null) {
            this.caughtEntity = this.world.getEntityByID(var1 - 1);
         }
      } else {
         ItemStack var33 = this.angler.getHeldItemMainhand();
         if (this.angler.isDead || !this.angler.isEntityAlive() || var33 == null || var33.getItem() != Items.FISHING_ROD || this.getDistanceSqToEntity(this.angler) > 1024.0D) {
            this.setDead();
            this.angler.fishEntity = null;
            return;
         }
      }

      if (this.caughtEntity != null) {
         if (!this.caughtEntity.isDead) {
            this.posX = this.caughtEntity.posX;
            double var36 = (double)this.caughtEntity.height;
            this.posY = this.caughtEntity.getEntityBoundingBox().minY + var36 * 0.8D;
            this.posZ = this.caughtEntity.posZ;
            return;
         }

         this.caughtEntity = null;
      }

      if (this.fishPosRotationIncrements > 0) {
         double var2 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
         double var4 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
         double var6 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
         double var8 = MathHelper.wrapDegrees(this.fishYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + var8 / (double)this.fishPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
         --this.fishPosRotationIncrements;
         this.setPosition(var2, var4, var6);
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
            Vec3d var10 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d var11 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult var12 = this.world.rayTraceBlocks(var10, var11);
            var10 = new Vec3d(this.posX, this.posY, this.posZ);
            var11 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (var12 != null) {
               var11 = new Vec3d(var12.hitVec.xCoord, var12.hitVec.yCoord, var12.hitVec.zCoord);
            }

            Entity var13 = null;
            List var14 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
            double var34 = 0.0D;

            for(int var15 = 0; var15 < var14.size(); ++var15) {
               Entity var16 = (Entity)var14.get(var15);
               if (this.canBeHooked(var16) && (var16 != this.angler || this.ticksInAir >= 5)) {
                  AxisAlignedBB var17 = var16.getEntityBoundingBox().expandXyz(0.30000001192092896D);
                  RayTraceResult var18 = var17.calculateIntercept(var10, var11);
                  if (var18 != null) {
                     double var37 = var10.squareDistanceTo(var18.hitVec);
                     if (var37 < var34 || var34 == 0.0D) {
                        var13 = var16;
                        var34 = var37;
                     }
                  }
               }
            }

            if (var13 != null) {
               var12 = new RayTraceResult(var13);
            }

            if (var12 != null) {
               CraftEventFactory.callProjectileHitEvent(this);
               if (var12.entityHit != null) {
                  this.caughtEntity = var12.entityHit;
                  this.getDataManager().set(DATA_HOOKED_ENTITY, Integer.valueOf(this.caughtEntity.getEntityId() + 1));
               } else {
                  this.inGround = true;
               }
            }
         }

         if (!this.inGround) {
            this.move(this.motionX, this.motionY, this.motionZ);
            float var43 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

            for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var43) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
            float var45 = 0.92F;
            if (this.onGround || this.isCollidedHorizontally) {
               var45 = 0.5F;
            }

            double var41 = 0.0D;

            for(int var46 = 0; var46 < 5; ++var46) {
               AxisAlignedBB var48 = this.getEntityBoundingBox();
               double var19 = var48.maxY - var48.minY;
               double var21 = var48.minY + var19 * (double)var46 / 5.0D;
               double var38 = var48.minY + var19 * (double)(var46 + 1) / 5.0D;
               AxisAlignedBB var23 = new AxisAlignedBB(var48.minX, var21, var48.minZ, var48.maxX, var38, var48.maxZ);
               if (this.world.isAABBInMaterial(var23, Material.WATER)) {
                  var41 += 0.2D;
               }
            }

            if (!this.world.isRemote && var41 > 0.0D) {
               WorldServer var47 = (WorldServer)this.world;
               int var49 = 1;
               BlockPos var50 = (new BlockPos(this)).up();
               if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(var50)) {
                  var49 = 2;
               }

               if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(var50)) {
                  --var49;
               }

               if (this.ticksCatchable > 0) {
                  --this.ticksCatchable;
                  if (this.ticksCatchable <= 0) {
                     this.ticksCaughtDelay = 0;
                     this.ticksCatchableDelay = 0;
                     PlayerFishEvent var51 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.FAILED_ATTEMPT);
                     this.world.getServer().getPluginManager().callEvent(var51);
                  }
               } else if (this.ticksCatchableDelay > 0) {
                  this.ticksCatchableDelay -= var49;
                  if (this.ticksCatchableDelay <= 0) {
                     PlayerFishEvent var24 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.BITE);
                     this.world.getServer().getPluginManager().callEvent(var24);
                     if (var24.isCancelled()) {
                        return;
                     }

                     this.motionY -= 0.20000000298023224D;
                     this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                     float var52 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);
                     var47.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(var52 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     var47.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(var52 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     this.ticksCatchable = MathHelper.getInt(this.rand, 10, 30);
                  } else {
                     this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                     float var53 = this.fishApproachAngle * 0.017453292F;
                     float var25 = MathHelper.sin(var53);
                     float var26 = MathHelper.cos(var53);
                     double var39 = this.posX + (double)(var25 * (float)this.ticksCatchableDelay * 0.1F);
                     double var27 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double var29 = this.posZ + (double)(var26 * (float)this.ticksCatchableDelay * 0.1F);
                     Block var31 = var47.getBlockState(new BlockPos((int)var39, (int)var27 - 1, (int)var29)).getBlock();
                     if (var31 == Blocks.WATER || var31 == Blocks.FLOWING_WATER) {
                        if (this.rand.nextFloat() < 0.15F) {
                           var47.spawnParticle(EnumParticleTypes.WATER_BUBBLE, var39, var27 - 0.10000000149011612D, var29, 1, (double)var25, 0.1D, (double)var26, 0.0D);
                        }

                        float var55 = var25 * 0.04F;
                        float var32 = var26 * 0.04F;
                        var47.spawnParticle(EnumParticleTypes.WATER_WAKE, var39, var27, var29, 0, (double)var32, 0.01D, (double)(-var55), 1.0D);
                        var47.spawnParticle(EnumParticleTypes.WATER_WAKE, var39, var27, var29, 0, (double)(-var32), 0.01D, (double)var55, 1.0D);
                     }
                  }
               } else if (this.ticksCaughtDelay > 0) {
                  this.ticksCaughtDelay -= var49;
                  float var54 = 0.15F;
                  if (this.ticksCaughtDelay < 20) {
                     var54 = (float)((double)var54 + (double)(20 - this.ticksCaughtDelay) * 0.05D);
                  } else if (this.ticksCaughtDelay < 40) {
                     var54 = (float)((double)var54 + (double)(40 - this.ticksCaughtDelay) * 0.02D);
                  } else if (this.ticksCaughtDelay < 60) {
                     var54 = (float)((double)var54 + (double)(60 - this.ticksCaughtDelay) * 0.01D);
                  }

                  if (this.rand.nextFloat() < var54) {
                     float var56 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292F;
                     float var57 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                     double var40 = this.posX + (double)(MathHelper.sin(var56) * var57 * 0.1F);
                     double var58 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double var59 = this.posZ + (double)(MathHelper.cos(var56) * var57 * 0.1F);
                     Block var60 = var47.getBlockState(new BlockPos((int)var40, (int)var58 - 1, (int)var59)).getBlock();
                     if (var60 == Blocks.WATER || var60 == Blocks.FLOWING_WATER) {
                        var47.spawnParticle(EnumParticleTypes.WATER_SPLASH, var40, var58, var59, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
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

            double var35 = var41 * 2.0D - 1.0D;
            this.motionY += 0.03999999910593033D * var35;
            if (var41 > 0.0D) {
               var45 = (float)((double)var45 * 0.9D);
               this.motionY *= 0.8D;
            }

            this.motionX *= (double)var45;
            this.motionY *= (double)var45;
            this.motionZ *= (double)var45;
            this.setPosition(this.posX, this.posY, this.posZ);
         }
      }

   }

   protected boolean canBeHooked(Entity var1) {
      return var1.canBeCollidedWith() || var1 instanceof EntityItem;
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("xTile", this.pos.getX());
      var1.setInteger("yTile", this.pos.getY());
      var1.setInteger("zTile", this.pos.getZ());
      ResourceLocation var2 = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      var1.setString("inTile", var2 == null ? "" : var2.toString());
      var1.setByte("inGround", (byte)(this.inGround ? 1 : 0));
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.pos = new BlockPos(var1.getInteger("xTile"), var1.getInteger("yTile"), var1.getInteger("zTile"));
      if (var1.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(var1.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(var1.getByte("inTile") & 255);
      }

      this.inGround = var1.getByte("inGround") == 1;
   }

   public int handleHookRetraction() {
      if (this.world.isRemote) {
         return 0;
      } else {
         int var1 = 0;
         if (this.caughtEntity != null) {
            PlayerFishEvent var2 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), this.caughtEntity.getBukkitEntity(), (Fish)this.getBukkitEntity(), State.CAUGHT_ENTITY);
            this.world.getServer().getPluginManager().callEvent(var2);
            if (var2.isCancelled()) {
               return 0;
            }

            this.bringInHookedEntity();
            this.world.setEntityState(this, (byte)31);
            var1 = this.caughtEntity instanceof EntityItem ? 3 : 5;
         } else if (this.ticksCatchable > 0) {
            LootContext.Builder var15 = new LootContext.Builder((WorldServer)this.world);
            var15.withLuck((float)EnchantmentHelper.getLuckOfSeaModifier(this.angler) + this.angler.getLuck());

            for(ItemStack var4 : this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, var15.build())) {
               EntityItem var5 = new EntityItem(this.world, this.posX, this.posY, this.posZ, var4);
               PlayerFishEvent var6 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), var5.getBukkitEntity(), (Fish)this.getBukkitEntity(), State.CAUGHT_FISH);
               var6.setExpToDrop(this.rand.nextInt(6) + 1);
               this.world.getServer().getPluginManager().callEvent(var6);
               if (var6.isCancelled()) {
                  return 0;
               }

               double var7 = this.angler.posX - this.posX;
               double var9 = this.angler.posY - this.posY;
               double var11 = this.angler.posZ - this.posZ;
               double var13 = (double)MathHelper.sqrt(var7 * var7 + var9 * var9 + var11 * var11);
               var5.motionX = var7 * 0.1D;
               var5.motionY = var9 * 0.1D + (double)MathHelper.sqrt(var13) * 0.08D;
               var5.motionZ = var11 * 0.1D;
               this.world.spawnEntity(var5);
               if (var6.getExpToDrop() > 0) {
                  this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, var6.getExpToDrop()));
               }
            }

            var1 = 1;
         }

         if (this.inGround) {
            PlayerFishEvent var16 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.IN_GROUND);
            this.world.getServer().getPluginManager().callEvent(var16);
            if (var16.isCancelled()) {
               return 0;
            }

            var1 = 2;
         }

         if (var1 == 0) {
            PlayerFishEvent var17 = new PlayerFishEvent((Player)this.angler.getBukkitEntity(), (org.bukkit.entity.Entity)null, (Fish)this.getBukkitEntity(), State.FAILED_ATTEMPT);
            this.world.getServer().getPluginManager().callEvent(var17);
            if (var17.isCancelled()) {
               return 0;
            }
         }

         this.setDead();
         this.angler.fishEntity = null;
         return var1;
      }
   }

   protected void bringInHookedEntity() {
      double var1 = this.angler.posX - this.posX;
      double var3 = this.angler.posY - this.posY;
      double var5 = this.angler.posZ - this.posZ;
      double var7 = (double)MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      this.caughtEntity.motionX += var1 * 0.1D;
      this.caughtEntity.motionY += var3 * 0.1D + (double)MathHelper.sqrt(var7) * 0.08D;
      this.caughtEntity.motionZ += var5 * 0.1D;
   }

   public void setDead() {
      super.setDead();
      if (this.angler != null) {
         this.angler.fishEntity = null;
      }

   }
}
