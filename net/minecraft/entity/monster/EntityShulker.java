package net.minecraft.entity.monster;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityBodyHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityShulker extends EntityGolem implements IMob {
   private static final UUID COVERED_ARMOR_BONUS_ID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
   private static final AttributeModifier COVERED_ARMOR_BONUS_MODIFIER = (new AttributeModifier(COVERED_ARMOR_BONUS_ID, "Covered armor bonus", 20.0D, 0)).setSaved(false);
   protected static final DataParameter ATTACHED_FACE = EntityDataManager.createKey(EntityShulker.class, DataSerializers.FACING);
   protected static final DataParameter ATTACHED_BLOCK_POS = EntityDataManager.createKey(EntityShulker.class, DataSerializers.OPTIONAL_BLOCK_POS);
   protected static final DataParameter PEEK_TICK = EntityDataManager.createKey(EntityShulker.class, DataSerializers.BYTE);
   private float prevPeekAmount;
   private float peekAmount;
   private BlockPos currentAttachmentPosition;
   private int clientSideTeleportInterpolation;

   public EntityShulker(World var1) {
      super(var1);
      this.setSize(1.0F, 1.0F);
      this.prevRenderYawOffset = 180.0F;
      this.renderYawOffset = 180.0F;
      this.isImmuneToFire = true;
      this.currentAttachmentPosition = null;
      this.experienceValue = 5;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      this.renderYawOffset = 180.0F;
      this.prevRenderYawOffset = 180.0F;
      this.rotationYaw = 180.0F;
      this.prevRotationYaw = 180.0F;
      this.rotationYawHead = 180.0F;
      this.prevRotationYawHead = 180.0F;
      return super.onInitialSpawn(var1, var2);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(4, new EntityShulker.AIAttack());
      this.tasks.addTask(7, new EntityShulker.AIPeek());
      this.tasks.addTask(8, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
      this.targetTasks.addTask(2, new EntityShulker.AIAttackNearest(this));
      this.targetTasks.addTask(3, new EntityShulker.AIDefenseAttack(this));
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SHULKER_AMBIENT;
   }

   public void playLivingSound() {
      if (!this.isClosed()) {
         super.playLivingSound();
      }

   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SHULKER_DEATH;
   }

   protected SoundEvent getHurtSound() {
      return this.isClosed() ? SoundEvents.ENTITY_SHULKER_HURT_CLOSED : SoundEvents.ENTITY_SHULKER_HURT;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(ATTACHED_FACE, EnumFacing.DOWN);
      this.dataManager.register(ATTACHED_BLOCK_POS, Optional.absent());
      this.dataManager.register(PEEK_TICK, Byte.valueOf((byte)0));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
   }

   protected EntityBodyHelper createBodyHelper() {
      return new EntityShulker.BodyHelper(this);
   }

   public static void registerFixesShulker(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Shulker");
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.dataManager.set(ATTACHED_FACE, EnumFacing.getFront(var1.getByte("AttachFace")));
      this.dataManager.set(PEEK_TICK, Byte.valueOf(var1.getByte("Peek")));
      if (var1.hasKey("APX")) {
         int var2 = var1.getInteger("APX");
         int var3 = var1.getInteger("APY");
         int var4 = var1.getInteger("APZ");
         this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(new BlockPos(var2, var3, var4)));
      } else {
         this.dataManager.set(ATTACHED_BLOCK_POS, Optional.absent());
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setByte("AttachFace", (byte)((EnumFacing)this.dataManager.get(ATTACHED_FACE)).getIndex());
      var1.setByte("Peek", ((Byte)this.dataManager.get(PEEK_TICK)).byteValue());
      BlockPos var2 = this.getAttachmentPos();
      if (var2 != null) {
         var1.setInteger("APX", var2.getX());
         var1.setInteger("APY", var2.getY());
         var1.setInteger("APZ", var2.getZ());
      }

   }

   public void onUpdate() {
      super.onUpdate();
      BlockPos var1 = (BlockPos)((Optional)this.dataManager.get(ATTACHED_BLOCK_POS)).orNull();
      if (var1 == null && !this.world.isRemote) {
         var1 = new BlockPos(this);
         this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(var1));
      }

      if (this.isRiding()) {
         var1 = null;
         float var2 = this.getRidingEntity().rotationYaw;
         this.rotationYaw = var2;
         this.renderYawOffset = var2;
         this.prevRenderYawOffset = var2;
         this.clientSideTeleportInterpolation = 0;
      } else if (!this.world.isRemote) {
         IBlockState var19 = this.world.getBlockState(var1);
         if (var19.getMaterial() != Material.AIR) {
            if (var19.getBlock() == Blocks.PISTON_EXTENSION) {
               EnumFacing var3 = (EnumFacing)var19.getValue(BlockPistonBase.FACING);
               var1 = var1.offset(var3);
               this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(var1));
            } else if (var19.getBlock() == Blocks.PISTON_HEAD) {
               EnumFacing var21 = (EnumFacing)var19.getValue(BlockPistonExtension.FACING);
               var1 = var1.offset(var21);
               this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(var1));
            } else {
               this.tryTeleportToNewPosition();
            }
         }

         BlockPos var22 = var1.offset(this.getAttachmentFacing());
         if (!this.world.isBlockNormalCube(var22, false)) {
            boolean var4 = false;

            for(EnumFacing var8 : EnumFacing.values()) {
               var22 = var1.offset(var8);
               if (this.world.isBlockNormalCube(var22, false)) {
                  this.dataManager.set(ATTACHED_FACE, var8);
                  var4 = true;
                  break;
               }
            }

            if (!var4) {
               this.tryTeleportToNewPosition();
            }
         }

         BlockPos var25 = var1.offset(this.getAttachmentFacing().getOpposite());
         if (this.world.isBlockNormalCube(var25, false)) {
            this.tryTeleportToNewPosition();
         }
      }

      float var20 = (float)this.getPeekTick() * 0.01F;
      this.prevPeekAmount = this.peekAmount;
      if (this.peekAmount > var20) {
         this.peekAmount = MathHelper.clamp(this.peekAmount - 0.05F, var20, 1.0F);
      } else if (this.peekAmount < var20) {
         this.peekAmount = MathHelper.clamp(this.peekAmount + 0.05F, 0.0F, var20);
      }

      if (var1 != null) {
         if (this.world.isRemote) {
            if (this.clientSideTeleportInterpolation > 0 && this.currentAttachmentPosition != null) {
               --this.clientSideTeleportInterpolation;
            } else {
               this.currentAttachmentPosition = var1;
            }
         }

         this.posX = (double)var1.getX() + 0.5D;
         this.posY = (double)var1.getY();
         this.posZ = (double)var1.getZ() + 0.5D;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.lastTickPosX = this.posX;
         this.lastTickPosY = this.posY;
         this.lastTickPosZ = this.posZ;
         double var24 = 0.5D - (double)MathHelper.sin((0.5F + this.peekAmount) * 3.1415927F) * 0.5D;
         double var26 = 0.5D - (double)MathHelper.sin((0.5F + this.prevPeekAmount) * 3.1415927F) * 0.5D;
         double var27 = var24 - var26;
         double var9 = 0.0D;
         double var11 = 0.0D;
         double var13 = 0.0D;
         EnumFacing var15 = this.getAttachmentFacing();
         switch(var15) {
         case DOWN:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D, this.posY, this.posZ - 0.5D, this.posX + 0.5D, this.posY + 1.0D + var24, this.posZ + 0.5D));
            var11 = var27;
            break;
         case UP:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D, this.posY - var24, this.posZ - 0.5D, this.posX + 0.5D, this.posY + 1.0D, this.posZ + 0.5D));
            var11 = -var27;
            break;
         case NORTH:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D, this.posY, this.posZ - 0.5D, this.posX + 0.5D, this.posY + 1.0D, this.posZ + 0.5D + var24));
            var13 = var27;
            break;
         case SOUTH:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D, this.posY, this.posZ - 0.5D - var24, this.posX + 0.5D, this.posY + 1.0D, this.posZ + 0.5D));
            var13 = -var27;
            break;
         case WEST:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D, this.posY, this.posZ - 0.5D, this.posX + 0.5D + var24, this.posY + 1.0D, this.posZ + 0.5D));
            var9 = var27;
            break;
         case EAST:
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.5D - var24, this.posY, this.posZ - 0.5D, this.posX + 0.5D, this.posY + 1.0D, this.posZ + 0.5D));
            var9 = -var27;
         }

         if (var27 > 0.0D) {
            List var16 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox());
            if (!var16.isEmpty()) {
               for(Entity var18 : var16) {
                  if (!(var18 instanceof EntityShulker) && !var18.noClip) {
                     var18.move(var9, var11, var13);
                  }
               }
            }
         }
      }

   }

   public void setPosition(double var1, double var3, double var5) {
      super.setPosition(var1, var3, var5);
      if (this.dataManager != null && this.ticksExisted != 0) {
         Optional var7 = (Optional)this.dataManager.get(ATTACHED_BLOCK_POS);
         Optional var8 = Optional.of(new BlockPos(var1, var3, var5));
         if (!var8.equals(var7)) {
            this.dataManager.set(ATTACHED_BLOCK_POS, var8);
            this.dataManager.set(PEEK_TICK, Byte.valueOf((byte)0));
            this.isAirBorne = true;
         }
      }

   }

   protected boolean tryTeleportToNewPosition() {
      if (!this.isAIDisabled() && this.isEntityAlive()) {
         BlockPos var1 = new BlockPos(this);

         for(int var2 = 0; var2 < 5; ++var2) {
            BlockPos var3 = var1.add(8 - this.rand.nextInt(17), 8 - this.rand.nextInt(17), 8 - this.rand.nextInt(17));
            if (var3.getY() > 0 && this.world.isAirBlock(var3) && this.world.isInsideBorder(this.world.getWorldBorder(), this) && this.world.getCollisionBoxes(this, new AxisAlignedBB(var3)).isEmpty()) {
               boolean var4 = false;

               for(EnumFacing var8 : EnumFacing.values()) {
                  if (this.world.isBlockNormalCube(var3.offset(var8), false)) {
                     this.dataManager.set(ATTACHED_FACE, var8);
                     var4 = true;
                     break;
                  }
               }

               if (var4) {
                  EnderTeleportEvent var9 = new EnderTeleportEvent(this, (double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), 0.0F);
                  if (MinecraftForge.EVENT_BUS.post(var9)) {
                     var4 = false;
                  }

                  var3 = new BlockPos(var9.getTargetX(), var9.getTargetY(), var9.getTargetZ());
               }

               if (var4) {
                  this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0F, 1.0F);
                  this.dataManager.set(ATTACHED_BLOCK_POS, Optional.of(var3));
                  this.dataManager.set(PEEK_TICK, Byte.valueOf((byte)0));
                  this.setAttackTarget((EntityLivingBase)null);
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevRenderYawOffset = 180.0F;
      this.renderYawOffset = 180.0F;
      this.rotationYaw = 180.0F;
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (ATTACHED_BLOCK_POS.equals(var1) && this.world.isRemote && !this.isRiding()) {
         BlockPos var2 = this.getAttachmentPos();
         if (var2 != null) {
            if (this.currentAttachmentPosition == null) {
               this.currentAttachmentPosition = var2;
            } else {
               this.clientSideTeleportInterpolation = 6;
            }

            this.posX = (double)var2.getX() + 0.5D;
            this.posY = (double)var2.getY();
            this.posZ = (double)var2.getZ() + 0.5D;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.lastTickPosX = this.posX;
            this.lastTickPosY = this.posY;
            this.lastTickPosZ = this.posZ;
         }
      }

      super.notifyDataManagerChange(var1);
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.newPosRotationIncrements = 0;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isClosed()) {
         Entity var3 = var1.getSourceOfDamage();
         if (var3 instanceof EntityArrow) {
            return false;
         }
      }

      if (super.attackEntityFrom(var1, var2)) {
         if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5D && this.rand.nextInt(4) == 0) {
            this.tryTeleportToNewPosition();
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isClosed() {
      return this.getPeekTick() == 0;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return this.isEntityAlive() ? this.getEntityBoundingBox() : null;
   }

   public EnumFacing getAttachmentFacing() {
      return (EnumFacing)this.dataManager.get(ATTACHED_FACE);
   }

   @Nullable
   public BlockPos getAttachmentPos() {
      return (BlockPos)((Optional)this.dataManager.get(ATTACHED_BLOCK_POS)).orNull();
   }

   public void setAttachmentPos(@Nullable BlockPos var1) {
      this.dataManager.set(ATTACHED_BLOCK_POS, Optional.fromNullable(var1));
   }

   public int getPeekTick() {
      return ((Byte)this.dataManager.get(PEEK_TICK)).byteValue();
   }

   public void updateArmorModifier(int var1) {
      if (!this.world.isRemote) {
         this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(COVERED_ARMOR_BONUS_MODIFIER);
         if (var1 == 0) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).applyModifier(COVERED_ARMOR_BONUS_MODIFIER);
            this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0F, 1.0F);
         } else {
            this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0F, 1.0F);
         }
      }

      this.dataManager.set(PEEK_TICK, Byte.valueOf((byte)var1));
   }

   @SideOnly(Side.CLIENT)
   public float getClientPeekAmount(float var1) {
      return this.prevPeekAmount + (this.peekAmount - this.prevPeekAmount) * var1;
   }

   @SideOnly(Side.CLIENT)
   public int getClientTeleportInterp() {
      return this.clientSideTeleportInterpolation;
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getOldAttachPos() {
      return this.currentAttachmentPosition;
   }

   public float getEyeHeight() {
      return 0.5F;
   }

   public int getVerticalFaceSpeed() {
      return 180;
   }

   public int getHorizontalFaceSpeed() {
      return 180;
   }

   public void applyEntityCollision(Entity var1) {
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public boolean isAttachedToBlock() {
      return this.currentAttachmentPosition != null && this.getAttachmentPos() != null;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_SHULKER;
   }

   class AIAttack extends EntityAIBase {
      private int attackTime;

      public AIAttack() {
         this.setMutexBits(3);
      }

      public boolean shouldExecute() {
         EntityLivingBase var1 = EntityShulker.this.getAttackTarget();
         return var1 != null && var1.isEntityAlive() ? EntityShulker.this.world.getDifficulty() != EnumDifficulty.PEACEFUL : false;
      }

      public void startExecuting() {
         this.attackTime = 20;
         EntityShulker.this.updateArmorModifier(100);
      }

      public void resetTask() {
         EntityShulker.this.updateArmorModifier(0);
      }

      public void updateTask() {
         if (EntityShulker.this.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
            --this.attackTime;
            EntityLivingBase var1 = EntityShulker.this.getAttackTarget();
            EntityShulker.this.getLookHelper().setLookPositionWithEntity(var1, 180.0F, 180.0F);
            double var2 = EntityShulker.this.getDistanceSqToEntity(var1);
            if (var2 < 400.0D) {
               if (this.attackTime <= 0) {
                  this.attackTime = 20 + EntityShulker.this.rand.nextInt(10) * 20 / 2;
                  EntityShulkerBullet var4 = new EntityShulkerBullet(EntityShulker.this.world, EntityShulker.this, var1, EntityShulker.this.getAttachmentFacing().getAxis());
                  EntityShulker.this.world.spawnEntity(var4);
                  EntityShulker.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (EntityShulker.this.rand.nextFloat() - EntityShulker.this.rand.nextFloat()) * 0.2F + 1.0F);
               }
            } else {
               EntityShulker.this.setAttackTarget((EntityLivingBase)null);
            }

            super.updateTask();
         }

      }
   }

   class AIAttackNearest extends EntityAINearestAttackableTarget {
      public AIAttackNearest(EntityShulker var2) {
         super(var2, EntityPlayer.class, true);
      }

      public boolean shouldExecute() {
         return EntityShulker.this.world.getDifficulty() == EnumDifficulty.PEACEFUL ? false : super.shouldExecute();
      }

      protected AxisAlignedBB getTargetableArea(double var1) {
         EnumFacing var3 = ((EntityShulker)this.taskOwner).getAttachmentFacing();
         return var3.getAxis() == EnumFacing.Axis.X ? this.taskOwner.getEntityBoundingBox().expand(4.0D, var1, var1) : (var3.getAxis() == EnumFacing.Axis.Z ? this.taskOwner.getEntityBoundingBox().expand(var1, var1, 4.0D) : this.taskOwner.getEntityBoundingBox().expand(var1, 4.0D, var1));
      }
   }

   static class AIDefenseAttack extends EntityAINearestAttackableTarget {
      public AIDefenseAttack(EntityShulker var1) {
         super(var1, EntityLivingBase.class, 10, true, false, new Predicate() {
            public boolean apply(@Nullable EntityLivingBase var1) {
               return var1 instanceof IMob;
            }
         });
      }

      public boolean shouldExecute() {
         return this.taskOwner.getTeam() == null ? false : super.shouldExecute();
      }

      protected AxisAlignedBB getTargetableArea(double var1) {
         EnumFacing var3 = ((EntityShulker)this.taskOwner).getAttachmentFacing();
         return var3.getAxis() == EnumFacing.Axis.X ? this.taskOwner.getEntityBoundingBox().expand(4.0D, var1, var1) : (var3.getAxis() == EnumFacing.Axis.Z ? this.taskOwner.getEntityBoundingBox().expand(var1, var1, 4.0D) : this.taskOwner.getEntityBoundingBox().expand(var1, 4.0D, var1));
      }
   }

   class AIPeek extends EntityAIBase {
      private int peekTime;

      private AIPeek() {
      }

      public boolean shouldExecute() {
         return EntityShulker.this.getAttackTarget() == null && EntityShulker.this.rand.nextInt(40) == 0;
      }

      public boolean continueExecuting() {
         return EntityShulker.this.getAttackTarget() == null && this.peekTime > 0;
      }

      public void startExecuting() {
         this.peekTime = 20 * (1 + EntityShulker.this.rand.nextInt(3));
         EntityShulker.this.updateArmorModifier(30);
      }

      public void resetTask() {
         if (EntityShulker.this.getAttackTarget() == null) {
            EntityShulker.this.updateArmorModifier(0);
         }

      }

      public void updateTask() {
         --this.peekTime;
      }
   }

   class BodyHelper extends EntityBodyHelper {
      public BodyHelper(EntityLivingBase var2) {
         super(var2);
      }

      public void updateRenderAngles() {
      }
   }
}
