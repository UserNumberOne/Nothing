package net.minecraft.entity.boss;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityWither extends EntityMob implements IRangedAttackMob {
   private static final DataParameter FIRST_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
   private static final DataParameter SECOND_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
   private static final DataParameter THIRD_HEAD_TARGET = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
   private static final DataParameter[] HEAD_TARGETS = new DataParameter[]{FIRST_HEAD_TARGET, SECOND_HEAD_TARGET, THIRD_HEAD_TARGET};
   private static final DataParameter INVULNERABILITY_TIME = EntityDataManager.createKey(EntityWither.class, DataSerializers.VARINT);
   private final float[] xRotationHeads = new float[2];
   private final float[] yRotationHeads = new float[2];
   private final float[] xRotOHeads = new float[2];
   private final float[] yRotOHeads = new float[2];
   private final int[] nextHeadUpdate = new int[2];
   private final int[] idleHeadUpdates = new int[2];
   private int blockBreakCounter;
   private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
   private static final Predicate NOT_UNDEAD = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityLivingBase && ((EntityLivingBase)var1).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD;
      }
   };

   public EntityWither(World var1) {
      super(var1);
      this.setHealth(this.getMaxHealth());
      this.setSize(0.9F, 3.5F);
      this.isImmuneToFire = true;
      ((PathNavigateGround)this.getNavigator()).setCanSwim(true);
      this.experienceValue = 50;
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityWither.AIDoNothing());
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIAttackRanged(this, 1.0D, 40, 20.0F));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(7, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, false, NOT_UNDEAD));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(FIRST_HEAD_TARGET, Integer.valueOf(0));
      this.dataManager.register(SECOND_HEAD_TARGET, Integer.valueOf(0));
      this.dataManager.register(THIRD_HEAD_TARGET, Integer.valueOf(0));
      this.dataManager.register(INVULNERABILITY_TIME, Integer.valueOf(0));
   }

   public static void registerFixesWither(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "WitherBoss");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Invul", this.getInvulTime());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setInvulTime(var1.getInteger("Invul"));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_WITHER_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_WITHER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WITHER_DEATH;
   }

   public void onLivingUpdate() {
      this.motionY *= 0.6000000238418579D;
      if (!this.world.isRemote && this.getWatchedTargetId(0) > 0) {
         Entity var1 = this.world.getEntityByID(this.getWatchedTargetId(0));
         if (var1 != null) {
            if (this.posY < var1.posY || !this.isArmored() && this.posY < var1.posY + 5.0D) {
               if (this.motionY < 0.0D) {
                  this.motionY = 0.0D;
               }

               this.motionY += (0.5D - this.motionY) * 0.6000000238418579D;
            }

            double var2 = var1.posX - this.posX;
            double var4 = var1.posZ - this.posZ;
            double var6 = var2 * var2 + var4 * var4;
            if (var6 > 9.0D) {
               double var8 = (double)MathHelper.sqrt(var6);
               this.motionX += (var2 / var8 * 0.5D - this.motionX) * 0.6000000238418579D;
               this.motionZ += (var4 / var8 * 0.5D - this.motionZ) * 0.6000000238418579D;
            }
         }
      }

      if (this.motionX * this.motionX + this.motionZ * this.motionZ > 0.05000000074505806D) {
         this.rotationYaw = (float)MathHelper.atan2(this.motionZ, this.motionX) * 57.295776F - 90.0F;
      }

      super.onLivingUpdate();

      for(int var20 = 0; var20 < 2; ++var20) {
         this.yRotOHeads[var20] = this.yRotationHeads[var20];
         this.xRotOHeads[var20] = this.xRotationHeads[var20];
      }

      for(int var21 = 0; var21 < 2; ++var21) {
         int var23 = this.getWatchedTargetId(var21 + 1);
         Entity var3 = null;
         if (var23 > 0) {
            var3 = this.world.getEntityByID(var23);
         }

         if (var3 != null) {
            double var27 = this.getHeadX(var21 + 1);
            double var28 = this.getHeadY(var21 + 1);
            double var29 = this.getHeadZ(var21 + 1);
            double var10 = var3.posX - var27;
            double var12 = var3.posY + (double)var3.getEyeHeight() - var28;
            double var14 = var3.posZ - var29;
            double var16 = (double)MathHelper.sqrt(var10 * var10 + var14 * var14);
            float var18 = (float)(MathHelper.atan2(var14, var10) * 57.29577951308232D) - 90.0F;
            float var19 = (float)(-(MathHelper.atan2(var12, var16) * 57.29577951308232D));
            this.xRotationHeads[var21] = this.rotlerp(this.xRotationHeads[var21], var19, 40.0F);
            this.yRotationHeads[var21] = this.rotlerp(this.yRotationHeads[var21], var18, 10.0F);
         } else {
            this.yRotationHeads[var21] = this.rotlerp(this.yRotationHeads[var21], this.renderYawOffset, 10.0F);
         }
      }

      boolean var22 = this.isArmored();

      for(int var24 = 0; var24 < 3; ++var24) {
         double var26 = this.getHeadX(var24);
         double var5 = this.getHeadY(var24);
         double var7 = this.getHeadZ(var24);
         this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var26 + this.rand.nextGaussian() * 0.30000001192092896D, var5 + this.rand.nextGaussian() * 0.30000001192092896D, var7 + this.rand.nextGaussian() * 0.30000001192092896D, 0.0D, 0.0D, 0.0D);
         if (var22 && this.world.rand.nextInt(4) == 0) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, var26 + this.rand.nextGaussian() * 0.30000001192092896D, var5 + this.rand.nextGaussian() * 0.30000001192092896D, var7 + this.rand.nextGaussian() * 0.30000001192092896D, 0.699999988079071D, 0.699999988079071D, 0.5D);
         }
      }

      if (this.getInvulTime() > 0) {
         for(int var25 = 0; var25 < 3; ++var25) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + this.rand.nextGaussian(), this.posY + (double)(this.rand.nextFloat() * 3.3F), this.posZ + this.rand.nextGaussian(), 0.699999988079071D, 0.699999988079071D, 0.8999999761581421D);
         }
      }

   }

   protected void updateAITasks() {
      if (this.getInvulTime() > 0) {
         int var1 = this.getInvulTime() - 1;
         if (var1 <= 0) {
            this.world.newExplosion(this, this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, 7.0F, false, this.world.getGameRules().getBoolean("mobGriefing"));
            this.world.playBroadcastSound(1023, new BlockPos(this), 0);
         }

         this.setInvulTime(var1);
         if (this.ticksExisted % 10 == 0) {
            this.heal(10.0F);
         }
      } else {
         super.updateAITasks();

         for(int var14 = 1; var14 < 3; ++var14) {
            if (this.ticksExisted >= this.nextHeadUpdate[var14 - 1]) {
               this.nextHeadUpdate[var14 - 1] = this.ticksExisted + 10 + this.rand.nextInt(10);
               if (this.world.getDifficulty() == EnumDifficulty.NORMAL || this.world.getDifficulty() == EnumDifficulty.HARD) {
                  int var2 = var14 - 1;
                  int var3 = this.idleHeadUpdates[var14 - 1];
                  this.idleHeadUpdates[var2] = this.idleHeadUpdates[var14 - 1] + 1;
                  if (var3 > 15) {
                     float var4 = 10.0F;
                     float var5 = 5.0F;
                     double var6 = MathHelper.nextDouble(this.rand, this.posX - 10.0D, this.posX + 10.0D);
                     double var8 = MathHelper.nextDouble(this.rand, this.posY - 5.0D, this.posY + 5.0D);
                     double var10 = MathHelper.nextDouble(this.rand, this.posZ - 10.0D, this.posZ + 10.0D);
                     this.launchWitherSkullToCoords(var14 + 1, var6, var8, var10, true);
                     this.idleHeadUpdates[var14 - 1] = 0;
                  }
               }

               int var16 = this.getWatchedTargetId(var14);
               if (var16 > 0) {
                  Entity var19 = this.world.getEntityByID(var16);
                  if (var19 != null && var19.isEntityAlive() && this.getDistanceSqToEntity(var19) <= 900.0D && this.canEntityBeSeen(var19)) {
                     if (var19 instanceof EntityPlayer && ((EntityPlayer)var19).capabilities.disableDamage) {
                        this.updateWatchedTargetId(var14, 0);
                     } else {
                        this.launchWitherSkullToEntity(var14 + 1, (EntityLivingBase)var19);
                        this.nextHeadUpdate[var14 - 1] = this.ticksExisted + 40 + this.rand.nextInt(20);
                        this.idleHeadUpdates[var14 - 1] = 0;
                     }
                  } else {
                     this.updateWatchedTargetId(var14, 0);
                  }
               } else {
                  List var18 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(20.0D, 8.0D, 20.0D), Predicates.and(NOT_UNDEAD, EntitySelectors.NOT_SPECTATING));

                  for(int var21 = 0; var21 < 10 && !var18.isEmpty(); ++var21) {
                     EntityLivingBase var23 = (EntityLivingBase)var18.get(this.rand.nextInt(var18.size()));
                     if (var23 != this && var23.isEntityAlive() && this.canEntityBeSeen(var23)) {
                        if (var23 instanceof EntityPlayer) {
                           if (!((EntityPlayer)var23).capabilities.disableDamage) {
                              this.updateWatchedTargetId(var14, var23.getEntityId());
                           }
                        } else {
                           this.updateWatchedTargetId(var14, var23.getEntityId());
                        }
                        break;
                     }

                     var18.remove(var23);
                  }
               }
            }
         }

         if (this.getAttackTarget() != null) {
            this.updateWatchedTargetId(0, this.getAttackTarget().getEntityId());
         } else {
            this.updateWatchedTargetId(0, 0);
         }

         if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            if (this.blockBreakCounter == 0 && this.world.getGameRules().getBoolean("mobGriefing")) {
               int var15 = MathHelper.floor(this.posY);
               int var17 = MathHelper.floor(this.posX);
               int var20 = MathHelper.floor(this.posZ);
               boolean var22 = false;

               for(int var24 = -1; var24 <= 1; ++var24) {
                  for(int var25 = -1; var25 <= 1; ++var25) {
                     for(int var7 = 0; var7 <= 3; ++var7) {
                        int var26 = var17 + var24;
                        int var9 = var15 + var7;
                        int var27 = var20 + var25;
                        BlockPos var11 = new BlockPos(var26, var9, var27);
                        IBlockState var12 = this.world.getBlockState(var11);
                        Block var13 = var12.getBlock();
                        if (!var13.isAir(var12, this.world, var11) && var13.canEntityDestroy(var12, this.world, var11, this)) {
                           var22 = this.world.destroyBlock(var11, true) || var22;
                        }
                     }
                  }
               }

               if (var22) {
                  this.world.playEvent((EntityPlayer)null, 1022, new BlockPos(this), 0);
               }
            }
         }

         if (this.ticksExisted % 20 == 0) {
            this.heal(1.0F);
         }

         this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
      }

   }

   public static boolean canDestroyBlock(Block var0) {
      return var0 != Blocks.BEDROCK && var0 != Blocks.END_PORTAL && var0 != Blocks.END_PORTAL_FRAME && var0 != Blocks.COMMAND_BLOCK && var0 != Blocks.REPEATING_COMMAND_BLOCK && var0 != Blocks.CHAIN_COMMAND_BLOCK && var0 != Blocks.BARRIER;
   }

   public void ignite() {
      this.setInvulTime(220);
      this.setHealth(this.getMaxHealth() / 3.0F);
   }

   public void setInWeb() {
   }

   public void addTrackingPlayer(EntityPlayerMP var1) {
      super.addTrackingPlayer(var1);
      this.bossInfo.addPlayer(var1);
   }

   public void removeTrackingPlayer(EntityPlayerMP var1) {
      super.removeTrackingPlayer(var1);
      this.bossInfo.removePlayer(var1);
   }

   private double getHeadX(int var1) {
      if (var1 <= 0) {
         return this.posX;
      } else {
         float var2 = (this.renderYawOffset + (float)(180 * (var1 - 1))) * 0.017453292F;
         float var3 = MathHelper.cos(var2);
         return this.posX + (double)var3 * 1.3D;
      }
   }

   private double getHeadY(int var1) {
      return var1 <= 0 ? this.posY + 3.0D : this.posY + 2.2D;
   }

   private double getHeadZ(int var1) {
      if (var1 <= 0) {
         return this.posZ;
      } else {
         float var2 = (this.renderYawOffset + (float)(180 * (var1 - 1))) * 0.017453292F;
         float var3 = MathHelper.sin(var2);
         return this.posZ + (double)var3 * 1.3D;
      }
   }

   private float rotlerp(float var1, float var2, float var3) {
      float var4 = MathHelper.wrapDegrees(var2 - var1);
      if (var4 > var3) {
         var4 = var3;
      }

      if (var4 < -var3) {
         var4 = -var3;
      }

      return var1 + var4;
   }

   private void launchWitherSkullToEntity(int var1, EntityLivingBase var2) {
      this.launchWitherSkullToCoords(var1, var2.posX, var2.posY + (double)var2.getEyeHeight() * 0.5D, var2.posZ, var1 == 0 && this.rand.nextFloat() < 0.001F);
   }

   private void launchWitherSkullToCoords(int var1, double var2, double var4, double var6, boolean var8) {
      this.world.playEvent((EntityPlayer)null, 1024, new BlockPos(this), 0);
      double var9 = this.getHeadX(var1);
      double var11 = this.getHeadY(var1);
      double var13 = this.getHeadZ(var1);
      double var15 = var2 - var9;
      double var17 = var4 - var11;
      double var19 = var6 - var13;
      EntityWitherSkull var21 = new EntityWitherSkull(this.world, this, var15, var17, var19);
      if (var8) {
         var21.setInvulnerable(true);
      }

      var21.posY = var11;
      var21.posX = var9;
      var21.posZ = var13;
      this.world.spawnEntity(var21);
   }

   public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2) {
      this.launchWitherSkullToEntity(0, var1);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (var1 != DamageSource.drown && !(var1.getEntity() instanceof EntityWither)) {
         if (this.getInvulTime() > 0 && var1 != DamageSource.outOfWorld) {
            return false;
         } else {
            if (this.isArmored()) {
               Entity var3 = var1.getSourceOfDamage();
               if (var3 instanceof EntityArrow) {
                  return false;
               }
            }

            Entity var5 = var1.getEntity();
            if (var5 != null && !(var5 instanceof EntityPlayer) && var5 instanceof EntityLivingBase && ((EntityLivingBase)var5).getCreatureAttribute() == this.getCreatureAttribute()) {
               return false;
            } else {
               if (this.blockBreakCounter <= 0) {
                  this.blockBreakCounter = 20;
               }

               for(int var4 = 0; var4 < this.idleHeadUpdates.length; ++var4) {
                  this.idleHeadUpdates[var4] += 3;
               }

               return super.attackEntityFrom(var1, var2);
            }
         }
      } else {
         return false;
      }
   }

   protected void dropFewItems(boolean var1, int var2) {
      EntityItem var3 = this.dropItem(Items.NETHER_STAR, 1);
      if (var3 != null) {
         var3.setNoDespawn();
      }

      if (!this.world.isRemote) {
         for(EntityPlayer var5 : this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().expand(50.0D, 100.0D, 50.0D))) {
            var5.addStat(AchievementList.KILL_WITHER);
         }
      }

   }

   protected void despawnEntity() {
      this.entityAge = 0;
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      return 15728880;
   }

   public void fall(float var1, float var2) {
   }

   public void addPotionEffect(PotionEffect var1) {
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6000000238418579D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
      this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
   }

   @SideOnly(Side.CLIENT)
   public float getHeadYRotation(int var1) {
      return this.yRotationHeads[var1];
   }

   @SideOnly(Side.CLIENT)
   public float getHeadXRotation(int var1) {
      return this.xRotationHeads[var1];
   }

   public int getInvulTime() {
      return ((Integer)this.dataManager.get(INVULNERABILITY_TIME)).intValue();
   }

   public void setInvulTime(int var1) {
      this.dataManager.set(INVULNERABILITY_TIME, Integer.valueOf(var1));
   }

   public int getWatchedTargetId(int var1) {
      return ((Integer)this.dataManager.get(HEAD_TARGETS[var1])).intValue();
   }

   public void updateWatchedTargetId(int var1, int var2) {
      this.dataManager.set(HEAD_TARGETS[var1], Integer.valueOf(var2));
   }

   public boolean isArmored() {
      return this.getHealth() <= this.getMaxHealth() / 2.0F;
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   protected boolean canBeRidden(Entity var1) {
      return false;
   }

   public boolean isNonBoss() {
      return false;
   }

   class AIDoNothing extends EntityAIBase {
      public AIDoNothing() {
         this.setMutexBits(7);
      }

      public boolean shouldExecute() {
         return EntityWither.this.getInvulTime() > 0;
      }
   }
}
