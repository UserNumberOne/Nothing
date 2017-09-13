package net.minecraft.entity.monster;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class EntityEnderman extends EntityMob {
   private static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
   private static final AttributeModifier ATTACKING_SPEED_BOOST = (new AttributeModifier(ATTACKING_SPEED_BOOST_ID, "Attacking speed boost", 0.15000000596046448D, 0)).setSaved(false);
   private static final Set CARRIABLE_BLOCKS = Sets.newIdentityHashSet();
   private static final DataParameter CARRIED_BLOCK = EntityDataManager.createKey(EntityEnderman.class, DataSerializers.OPTIONAL_BLOCK_STATE);
   private static final DataParameter SCREAMING = EntityDataManager.createKey(EntityEnderman.class, DataSerializers.BOOLEAN);
   private int lastCreepySound;
   private int targetChangeTime;

   public EntityEnderman(World var1) {
      super(var1);
      this.setSize(0.6F, 2.9F);
      this.stepHeight = 1.0F;
      this.setPathPriority(PathNodeType.WATER, -1.0F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
      this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.tasks.addTask(10, new EntityEnderman.AIPlaceBlock(this));
      this.tasks.addTask(11, new EntityEnderman.AITakeBlock(this));
      this.targetTasks.addTask(1, new EntityEnderman.AIFindPlayer(this));
      this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityEndermite.class, 10, true, false, new Predicate() {
         public boolean apply(@Nullable EntityEndermite var1) {
            return var1.isSpawnedByPlayer();
         }
      }));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0D);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
   }

   public void setAttackTarget(@Nullable EntityLivingBase var1) {
      super.setAttackTarget(var1);
      IAttributeInstance var2 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      if (var1 == null) {
         this.targetChangeTime = 0;
         this.dataManager.set(SCREAMING, Boolean.valueOf(false));
         var2.removeModifier(ATTACKING_SPEED_BOOST);
      } else {
         this.targetChangeTime = this.ticksExisted;
         this.dataManager.set(SCREAMING, Boolean.valueOf(true));
         if (!var2.hasModifier(ATTACKING_SPEED_BOOST)) {
            var2.applyModifier(ATTACKING_SPEED_BOOST);
         }
      }

   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(CARRIED_BLOCK, Optional.absent());
      this.dataManager.register(SCREAMING, Boolean.valueOf(false));
   }

   public void playEndermanSound() {
      if (this.ticksExisted >= this.lastCreepySound + 400) {
         this.lastCreepySound = this.ticksExisted;
         if (!this.isSilent()) {
            this.world.playSound(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ, SoundEvents.ENTITY_ENDERMEN_STARE, this.getSoundCategory(), 2.5F, 1.0F, false);
         }
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (SCREAMING.equals(var1) && this.isScreaming() && this.world.isRemote) {
         this.playEndermanSound();
      }

      super.notifyDataManagerChange(var1);
   }

   public static void registerFixesEnderman(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Enderman");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      IBlockState var2 = this.getHeldBlockState();
      if (var2 != null) {
         var1.setShort("carried", (short)Block.getIdFromBlock(var2.getBlock()));
         var1.setShort("carriedData", (short)var2.getBlock().getMetaFromState(var2));
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      IBlockState var2;
      if (var1.hasKey("carried", 8)) {
         var2 = Block.getBlockFromName(var1.getString("carried")).getStateFromMeta(var1.getShort("carriedData") & '\uffff');
      } else {
         var2 = Block.getBlockById(var1.getShort("carried")).getStateFromMeta(var1.getShort("carriedData") & '\uffff');
      }

      if (var2 == null || var2.getBlock() == null || var2.getMaterial() == Material.AIR) {
         var2 = null;
      }

      this.setHeldBlockState(var2);
   }

   private boolean shouldAttackPlayer(EntityPlayer var1) {
      ItemStack var2 = var1.inventory.armorInventory[3];
      if (var2 != null && var2.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
         return false;
      } else {
         Vec3d var3 = var1.getLook(1.0F).normalize();
         Vec3d var4 = new Vec3d(this.posX - var1.posX, this.getEntityBoundingBox().minY + (double)this.getEyeHeight() - (var1.posY + (double)var1.getEyeHeight()), this.posZ - var1.posZ);
         double var5 = var4.lengthVector();
         var4 = var4.normalize();
         double var7 = var3.dotProduct(var4);
         return var7 > 1.0D - 0.025D / var5 ? var1.canEntityBeSeen(this) : false;
      }
   }

   public float getEyeHeight() {
      return 2.55F;
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         for(int var1 = 0; var1 < 2; ++var1) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height - 0.25D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
         }
      }

      this.isJumping = false;
      super.onLivingUpdate();
   }

   protected void updateAITasks() {
      if (this.isWet()) {
         this.attackEntityFrom(DamageSource.drown, 1.0F);
      }

      if (this.world.isDaytime() && this.ticksExisted >= this.targetChangeTime + 600) {
         float var1 = this.getBrightness(1.0F);
         if (var1 > 0.5F && this.world.canSeeSky(new BlockPos(this)) && this.rand.nextFloat() * 30.0F < (var1 - 0.4F) * 2.0F) {
            this.setAttackTarget((EntityLivingBase)null);
            this.teleportRandomly();
         }
      }

      super.updateAITasks();
   }

   protected boolean teleportRandomly() {
      double var1 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
      double var3 = this.posY + (double)(this.rand.nextInt(64) - 32);
      double var5 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
      return this.teleportTo(var1, var3, var5);
   }

   protected boolean teleportToEntity(Entity var1) {
      Vec3d var2 = new Vec3d(this.posX - var1.posX, this.getEntityBoundingBox().minY + (double)(this.height / 2.0F) - var1.posY + (double)var1.getEyeHeight(), this.posZ - var1.posZ);
      var2 = var2.normalize();
      double var3 = 16.0D;
      double var5 = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - var2.xCoord * 16.0D;
      double var7 = this.posY + (double)(this.rand.nextInt(16) - 8) - var2.yCoord * 16.0D;
      double var9 = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - var2.zCoord * 16.0D;
      return this.teleportTo(var5, var7, var9);
   }

   private boolean teleportTo(double var1, double var3, double var5) {
      EnderTeleportEvent var7 = new EnderTeleportEvent(this, var1, var3, var5, 0.0F);
      if (MinecraftForge.EVENT_BUS.post(var7)) {
         return false;
      } else {
         boolean var8 = this.attemptTeleport(var7.getTargetX(), var7.getTargetY(), var7.getTargetZ());
         if (var8) {
            this.world.playSound((EntityPlayer)null, this.prevPosX, this.prevPosY, this.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
         }

         return var8;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isScreaming() ? SoundEvents.ENTITY_ENDERMEN_SCREAM : SoundEvents.ENTITY_ENDERMEN_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_ENDERMEN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ENDERMEN_DEATH;
   }

   protected void dropEquipment(boolean var1, int var2) {
      super.dropEquipment(var1, var2);
      IBlockState var3 = this.getHeldBlockState();
      if (var3 != null) {
         Item var4 = Item.getItemFromBlock(var3.getBlock());
         if (var4 != null) {
            int var5 = var4.getHasSubtypes() ? var3.getBlock().getMetaFromState(var3) : 0;
            this.entityDropItem(new ItemStack(var4, 1, var5), 0.0F);
         }
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_ENDERMAN;
   }

   public void setHeldBlockState(@Nullable IBlockState var1) {
      this.dataManager.set(CARRIED_BLOCK, Optional.fromNullable(var1));
   }

   @Nullable
   public IBlockState getHeldBlockState() {
      return (IBlockState)((Optional)this.dataManager.get(CARRIED_BLOCK)).orNull();
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (var1 instanceof EntityDamageSourceIndirect) {
         for(int var4 = 0; var4 < 64; ++var4) {
            if (this.teleportRandomly()) {
               return true;
            }
         }

         return false;
      } else {
         boolean var3 = super.attackEntityFrom(var1, var2);
         if (var1.isUnblockable() && this.rand.nextInt(10) != 0) {
            this.teleportRandomly();
         }

         return var3;
      }
   }

   public static void setCarriable(Block var0, boolean var1) {
      if (var1) {
         CARRIABLE_BLOCKS.add(var0);
      } else {
         CARRIABLE_BLOCKS.remove(var0);
      }

   }

   public static boolean getCarriable(Block var0) {
      return CARRIABLE_BLOCKS.contains(var0);
   }

   public boolean isScreaming() {
      return ((Boolean)this.dataManager.get(SCREAMING)).booleanValue();
   }

   static {
      CARRIABLE_BLOCKS.add(Blocks.GRASS);
      CARRIABLE_BLOCKS.add(Blocks.DIRT);
      CARRIABLE_BLOCKS.add(Blocks.SAND);
      CARRIABLE_BLOCKS.add(Blocks.GRAVEL);
      CARRIABLE_BLOCKS.add(Blocks.YELLOW_FLOWER);
      CARRIABLE_BLOCKS.add(Blocks.RED_FLOWER);
      CARRIABLE_BLOCKS.add(Blocks.BROWN_MUSHROOM);
      CARRIABLE_BLOCKS.add(Blocks.RED_MUSHROOM);
      CARRIABLE_BLOCKS.add(Blocks.TNT);
      CARRIABLE_BLOCKS.add(Blocks.CACTUS);
      CARRIABLE_BLOCKS.add(Blocks.CLAY);
      CARRIABLE_BLOCKS.add(Blocks.PUMPKIN);
      CARRIABLE_BLOCKS.add(Blocks.MELON_BLOCK);
      CARRIABLE_BLOCKS.add(Blocks.MYCELIUM);
      CARRIABLE_BLOCKS.add(Blocks.NETHERRACK);
   }

   static class AIFindPlayer extends EntityAINearestAttackableTarget {
      private final EntityEnderman enderman;
      private EntityPlayer player;
      private int aggroTime;
      private int teleportTime;

      public AIFindPlayer(EntityEnderman var1) {
         super(var1, EntityPlayer.class, false);
         this.enderman = var1;
      }

      public boolean shouldExecute() {
         double var1 = this.getTargetDistance();
         this.player = this.enderman.world.getNearestAttackablePlayer(this.enderman.posX, this.enderman.posY, this.enderman.posZ, var1, var1, (Function)null, new Predicate() {
            public boolean apply(@Nullable EntityPlayer var1) {
               return var1 != null && AIFindPlayer.this.enderman.shouldAttackPlayer(var1);
            }
         });
         return this.player != null;
      }

      public void startExecuting() {
         this.aggroTime = 5;
         this.teleportTime = 0;
      }

      public void resetTask() {
         this.player = null;
         super.resetTask();
      }

      public boolean continueExecuting() {
         if (this.player != null) {
            if (!this.enderman.shouldAttackPlayer(this.player)) {
               return false;
            } else {
               this.enderman.faceEntity(this.player, 10.0F, 10.0F);
               return true;
            }
         } else {
            return this.targetEntity != null && ((EntityPlayer)this.targetEntity).isEntityAlive() ? true : super.continueExecuting();
         }
      }

      public void updateTask() {
         if (this.player != null) {
            if (--this.aggroTime <= 0) {
               this.targetEntity = this.player;
               this.player = null;
               super.startExecuting();
            }
         } else {
            if (this.targetEntity != null) {
               if (this.enderman.shouldAttackPlayer((EntityPlayer)this.targetEntity)) {
                  if (((EntityPlayer)this.targetEntity).getDistanceSqToEntity(this.enderman) < 16.0D) {
                     this.enderman.teleportRandomly();
                  }

                  this.teleportTime = 0;
               } else if (((EntityPlayer)this.targetEntity).getDistanceSqToEntity(this.enderman) > 256.0D && this.teleportTime++ >= 30 && this.enderman.teleportToEntity(this.targetEntity)) {
                  this.teleportTime = 0;
               }
            }

            super.updateTask();
         }

      }
   }

   static class AIPlaceBlock extends EntityAIBase {
      private final EntityEnderman enderman;

      public AIPlaceBlock(EntityEnderman var1) {
         this.enderman = var1;
      }

      public boolean shouldExecute() {
         return this.enderman.getHeldBlockState() == null ? false : (!this.enderman.world.getGameRules().getBoolean("mobGriefing") ? false : this.enderman.getRNG().nextInt(2000) == 0);
      }

      public void updateTask() {
         Random var1 = this.enderman.getRNG();
         World var2 = this.enderman.world;
         int var3 = MathHelper.floor(this.enderman.posX - 1.0D + var1.nextDouble() * 2.0D);
         int var4 = MathHelper.floor(this.enderman.posY + var1.nextDouble() * 2.0D);
         int var5 = MathHelper.floor(this.enderman.posZ - 1.0D + var1.nextDouble() * 2.0D);
         BlockPos var6 = new BlockPos(var3, var4, var5);
         IBlockState var7 = var2.getBlockState(var6);
         IBlockState var8 = var2.getBlockState(var6.down());
         IBlockState var9 = this.enderman.getHeldBlockState();
         if (var9 != null && this.canPlaceBlock(var2, var6, var9.getBlock(), var7, var8)) {
            var2.setBlockState(var6, var9, 3);
            this.enderman.setHeldBlockState((IBlockState)null);
         }

      }

      private boolean canPlaceBlock(World var1, BlockPos var2, Block var3, IBlockState var4, IBlockState var5) {
         return !var3.canPlaceBlockAt(var1, var2) ? false : (var4.getMaterial() != Material.AIR ? false : (var5.getMaterial() == Material.AIR ? false : var5.isFullCube()));
      }
   }

   static class AITakeBlock extends EntityAIBase {
      private final EntityEnderman enderman;

      public AITakeBlock(EntityEnderman var1) {
         this.enderman = var1;
      }

      public boolean shouldExecute() {
         return this.enderman.getHeldBlockState() != null ? false : (!this.enderman.world.getGameRules().getBoolean("mobGriefing") ? false : this.enderman.getRNG().nextInt(20) == 0);
      }

      public void updateTask() {
         Random var1 = this.enderman.getRNG();
         World var2 = this.enderman.world;
         int var3 = MathHelper.floor(this.enderman.posX - 2.0D + var1.nextDouble() * 4.0D);
         int var4 = MathHelper.floor(this.enderman.posY + var1.nextDouble() * 3.0D);
         int var5 = MathHelper.floor(this.enderman.posZ - 2.0D + var1.nextDouble() * 4.0D);
         BlockPos var6 = new BlockPos(var3, var4, var5);
         IBlockState var7 = var2.getBlockState(var6);
         Block var8 = var7.getBlock();
         RayTraceResult var9 = var2.rayTraceBlocks(new Vec3d((double)((float)MathHelper.floor(this.enderman.posX) + 0.5F), (double)((float)var4 + 0.5F), (double)((float)MathHelper.floor(this.enderman.posZ) + 0.5F)), new Vec3d((double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), (double)((float)var5 + 0.5F)), false, true, false);
         boolean var10 = var9 != null && var9.getBlockPos().equals(var6);
         if (EntityEnderman.CARRIABLE_BLOCKS.contains(var8) && var10) {
            this.enderman.setHeldBlockState(var7);
            var2.setBlockToAir(var6);
         }

      }
   }
}
