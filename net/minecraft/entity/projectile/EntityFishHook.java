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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFishHook extends Entity {
   private static final DataParameter DATA_HOOKED_ENTITY = EntityDataManager.createKey(EntityFishHook.class, DataSerializers.VARINT);
   private BlockPos pos;
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
   @SideOnly(Side.CLIENT)
   private double clientMotionX;
   @SideOnly(Side.CLIENT)
   private double clientMotionY;
   @SideOnly(Side.CLIENT)
   private double clientMotionZ;
   public Entity caughtEntity;

   public EntityFishHook(World var1) {
      super(var1);
      this.pos = new BlockPos(-1, -1, -1);
      this.setSize(0.25F, 0.25F);
      this.ignoreFrustumCheck = true;
   }

   @SideOnly(Side.CLIENT)
   public EntityFishHook(World var1, double var2, double var4, double var6, EntityPlayer var8) {
      this(var1);
      this.setPosition(var2, var4, var6);
      this.ignoreFrustumCheck = true;
      this.angler = var8;
      var8.fishEntity = this;
   }

   public EntityFishHook(World var1, EntityPlayer var2) {
      super(var1);
      this.pos = new BlockPos(-1, -1, -1);
      this.ignoreFrustumCheck = true;
      this.angler = var2;
      this.angler.fishEntity = this;
      this.setSize(0.25F, 0.25F);
      this.setLocationAndAngles(var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, var2.rotationYaw, var2.rotationPitch);
      this.posX -= (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * 0.16F);
      this.posY -= 0.10000000149011612D;
      this.posZ -= (double)(MathHelper.sin(this.rotationYaw * 0.017453292F) * 0.16F);
      this.setPosition(this.posX, this.posY, this.posZ);
      float var3 = 0.4F;
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

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
      if (Double.isNaN(var3)) {
         var3 = 4.0D;
      }

      var3 = var3 * 64.0D;
      return var1 < var3 * var3;
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
      this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.29577951308232D);
      this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var10) * 57.29577951308232D);
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.ticksInGround = 0;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.fishX = var1;
      this.fishY = var3;
      this.fishZ = var5;
      this.fishYaw = (double)var7;
      this.fishPitch = (double)var8;
      this.fishPosRotationIncrements = var9;
      this.motionX = this.clientMotionX;
      this.motionY = this.clientMotionY;
      this.motionZ = this.clientMotionZ;
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      this.clientMotionX = this.motionX;
      this.clientMotionY = this.motionY;
      this.clientMotionZ = this.motionZ;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         int var1 = ((Integer)this.getDataManager().get(DATA_HOOKED_ENTITY)).intValue();
         if (var1 > 0 && this.caughtEntity == null) {
            this.caughtEntity = this.world.getEntityByID(var1 - 1);
         }
      } else {
         ItemStack var21 = this.angler.getHeldItemMainhand();
         if (this.angler.isDead || !this.angler.isEntityAlive() || var21 == null || var21.getItem() != Items.FISHING_ROD || this.getDistanceSqToEntity(this.angler) > 1024.0D) {
            this.setDead();
            this.angler.fishEntity = null;
            return;
         }
      }

      if (this.caughtEntity != null) {
         if (!this.caughtEntity.isDead) {
            this.posX = this.caughtEntity.posX;
            double var26 = (double)this.caughtEntity.height;
            this.posY = this.caughtEntity.getEntityBoundingBox().minY + var26 * 0.8D;
            this.posZ = this.caughtEntity.posZ;
            return;
         }

         this.caughtEntity = null;
      }

      if (this.fishPosRotationIncrements > 0) {
         double var22 = this.posX + (this.fishX - this.posX) / (double)this.fishPosRotationIncrements;
         double var3 = this.posY + (this.fishY - this.posY) / (double)this.fishPosRotationIncrements;
         double var5 = this.posZ + (this.fishZ - this.posZ) / (double)this.fishPosRotationIncrements;
         double var7 = MathHelper.wrapDegrees(this.fishYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.fishPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.fishPitch - (double)this.rotationPitch) / (double)this.fishPosRotationIncrements);
         --this.fishPosRotationIncrements;
         this.setPosition(var22, var3, var5);
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
            Vec3d var23 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            RayTraceResult var29 = this.world.rayTraceBlocks(var23, var2);
            var23 = new Vec3d(this.posX, this.posY, this.posZ);
            var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            if (var29 != null) {
               var2 = new Vec3d(var29.hitVec.xCoord, var29.hitVec.yCoord, var29.hitVec.zCoord);
            }

            Entity var4 = null;
            List var32 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
            double var6 = 0.0D;

            for(int var8 = 0; var8 < var32.size(); ++var8) {
               Entity var9 = (Entity)var32.get(var8);
               if (this.canBeHooked(var9) && (var9 != this.angler || this.ticksInAir >= 5)) {
                  AxisAlignedBB var10 = var9.getEntityBoundingBox().expandXyz(0.30000001192092896D);
                  RayTraceResult var11 = var10.calculateIntercept(var23, var2);
                  if (var11 != null) {
                     double var12 = var23.squareDistanceTo(var11.hitVec);
                     if (var12 < var6 || var6 == 0.0D) {
                        var4 = var9;
                        var6 = var12;
                     }
                  }
               }
            }

            if (var4 != null) {
               var29 = new RayTraceResult(var4);
            }

            if (var29 != null) {
               if (var29.entityHit != null) {
                  this.caughtEntity = var29.entityHit;
                  this.getDataManager().set(DATA_HOOKED_ENTITY, Integer.valueOf(this.caughtEntity.getEntityId() + 1));
               } else {
                  this.inGround = true;
               }
            }
         }

         if (!this.inGround) {
            this.move(this.motionX, this.motionY, this.motionZ);
            float var25 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

            for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var25) * 57.29577951308232D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
            float var28 = 0.92F;
            if (this.onGround || this.isCollidedHorizontally) {
               var28 = 0.5F;
            }

            boolean var30 = true;
            double var31 = 0.0D;

            for(int var33 = 0; var33 < 5; ++var33) {
               AxisAlignedBB var36 = this.getEntityBoundingBox();
               double var38 = var36.maxY - var36.minY;
               double var43 = var36.minY + var38 * (double)var33 / 5.0D;
               double var48 = var36.minY + var38 * (double)(var33 + 1) / 5.0D;
               AxisAlignedBB var14 = new AxisAlignedBB(var36.minX, var43, var36.minZ, var36.maxX, var48, var36.maxZ);
               if (this.world.isAABBInMaterial(var14, Material.WATER)) {
                  var31 += 0.2D;
               }
            }

            if (!this.world.isRemote && var31 > 0.0D) {
               WorldServer var34 = (WorldServer)this.world;
               int var37 = 1;
               BlockPos var39 = (new BlockPos(this)).up();
               if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(var39)) {
                  var37 = 2;
               }

               if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(var39)) {
                  --var37;
               }

               if (this.ticksCatchable > 0) {
                  --this.ticksCatchable;
                  if (this.ticksCatchable <= 0) {
                     this.ticksCaughtDelay = 0;
                     this.ticksCatchableDelay = 0;
                  }
               } else if (this.ticksCatchableDelay > 0) {
                  this.ticksCatchableDelay -= var37;
                  if (this.ticksCatchableDelay <= 0) {
                     this.motionY -= 0.20000000298023224D;
                     this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                     float var40 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);
                     var34.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, (double)(var40 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     var34.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, (double)(var40 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                     this.ticksCatchable = MathHelper.getInt(this.rand, 10, 30);
                  } else {
                     this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                     float var41 = this.fishApproachAngle * 0.017453292F;
                     float var44 = MathHelper.sin(var41);
                     float var46 = MathHelper.cos(var41);
                     double var49 = this.posX + (double)(var44 * (float)this.ticksCatchableDelay * 0.1F);
                     double var51 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double var16 = this.posZ + (double)(var46 * (float)this.ticksCatchableDelay * 0.1F);
                     Block var18 = var34.getBlockState(new BlockPos((int)var49, (int)var51 - 1, (int)var16)).getBlock();
                     if (var18 == Blocks.WATER || var18 == Blocks.FLOWING_WATER) {
                        if (this.rand.nextFloat() < 0.15F) {
                           var34.spawnParticle(EnumParticleTypes.WATER_BUBBLE, var49, var51 - 0.10000000149011612D, var16, 1, (double)var44, 0.1D, (double)var46, 0.0D);
                        }

                        float var19 = var44 * 0.04F;
                        float var20 = var46 * 0.04F;
                        var34.spawnParticle(EnumParticleTypes.WATER_WAKE, var49, var51, var16, 0, (double)var20, 0.01D, (double)(-var19), 1.0D);
                        var34.spawnParticle(EnumParticleTypes.WATER_WAKE, var49, var51, var16, 0, (double)(-var20), 0.01D, (double)var19, 1.0D);
                     }
                  }
               } else if (this.ticksCaughtDelay > 0) {
                  this.ticksCaughtDelay -= var37;
                  float var42 = 0.15F;
                  if (this.ticksCaughtDelay < 20) {
                     var42 = (float)((double)var42 + (double)(20 - this.ticksCaughtDelay) * 0.05D);
                  } else if (this.ticksCaughtDelay < 40) {
                     var42 = (float)((double)var42 + (double)(40 - this.ticksCaughtDelay) * 0.02D);
                  } else if (this.ticksCaughtDelay < 60) {
                     var42 = (float)((double)var42 + (double)(60 - this.ticksCaughtDelay) * 0.01D);
                  }

                  if (this.rand.nextFloat() < var42) {
                     float var45 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292F;
                     float var47 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                     double var50 = this.posX + (double)(MathHelper.sin(var45) * var47 * 0.1F);
                     double var52 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                     double var53 = this.posZ + (double)(MathHelper.cos(var45) * var47 * 0.1F);
                     Block var54 = var34.getBlockState(new BlockPos((int)var50, (int)var52 - 1, (int)var53)).getBlock();
                     if (var54 == Blocks.WATER || var54 == Blocks.FLOWING_WATER) {
                        var34.spawnParticle(EnumParticleTypes.WATER_SPLASH, var50, var52, var53, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
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

            double var35 = var31 * 2.0D - 1.0D;
            this.motionY += 0.03999999910593033D * var35;
            if (var31 > 0.0D) {
               var28 = (float)((double)var28 * 0.9D);
               this.motionY *= 0.8D;
            }

            this.motionX *= (double)var28;
            this.motionY *= (double)var28;
            this.motionZ *= (double)var28;
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
            this.bringInHookedEntity();
            this.world.setEntityState(this, (byte)31);
            var1 = this.caughtEntity instanceof EntityItem ? 3 : 5;
         } else if (this.ticksCatchable > 0) {
            LootContext.Builder var2 = new LootContext.Builder((WorldServer)this.world);
            var2.withLuck((float)EnchantmentHelper.getLuckOfSeaModifier(this.angler) + this.angler.getLuck());

            for(ItemStack var4 : this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, var2.build())) {
               EntityItem var5 = new EntityItem(this.world, this.posX, this.posY, this.posZ, var4);
               double var6 = this.angler.posX - this.posX;
               double var8 = this.angler.posY - this.posY;
               double var10 = this.angler.posZ - this.posZ;
               double var12 = (double)MathHelper.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
               double var14 = 0.1D;
               var5.motionX = var6 * 0.1D;
               var5.motionY = var8 * 0.1D + (double)MathHelper.sqrt(var12) * 0.08D;
               var5.motionZ = var10 * 0.1D;
               this.world.spawnEntity(var5);
               this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
            }

            var1 = 1;
         }

         if (this.inGround) {
            var1 = 2;
         }

         this.setDead();
         this.angler.fishEntity = null;
         return var1;
      }
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 31 && this.world.isRemote && this.caughtEntity instanceof EntityPlayer && ((EntityPlayer)this.caughtEntity).isUser()) {
         this.bringInHookedEntity();
      }

      super.handleStatusUpdate(var1);
   }

   protected void bringInHookedEntity() {
      double var1 = this.angler.posX - this.posX;
      double var3 = this.angler.posY - this.posY;
      double var5 = this.angler.posZ - this.posZ;
      double var7 = (double)MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      double var9 = 0.1D;
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
