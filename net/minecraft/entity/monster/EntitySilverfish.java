package net.minecraft.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntitySilverfish extends EntityMob {
   private EntitySilverfish.AISummonSilverfish summonSilverfish;

   public EntitySilverfish(World world) {
      super(world);
      this.setSize(0.4F, 0.3F);
   }

   public static void registerFixesSilverfish(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Silverfish");
   }

   protected void initEntityAI() {
      this.summonSilverfish = new EntitySilverfish.AISummonSilverfish(this);
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(3, this.summonSilverfish);
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false));
      this.tasks.addTask(5, new EntitySilverfish.AIHideInStone(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
   }

   public double getYOffset() {
      return 0.2D;
   }

   public float getEyeHeight() {
      return 0.1F;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SILVERFISH_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_SILVERFISH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SILVERFISH_DEATH;
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      this.playSound(SoundEvents.ENTITY_SILVERFISH_STEP, 0.15F, 1.0F);
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else {
         if ((damagesource instanceof EntityDamageSource || damagesource == DamageSource.magic) && this.summonSilverfish != null) {
            this.summonSilverfish.notifyHurt();
         }

         return super.attackEntityFrom(damagesource, f);
      }
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_SILVERFISH;
   }

   public void onUpdate() {
      this.renderYawOffset = this.rotationYaw;
      super.onUpdate();
   }

   public float getBlockPathWeight(BlockPos blockposition) {
      return this.world.getBlockState(blockposition.down()).getBlock() == Blocks.STONE ? 10.0F : super.getBlockPathWeight(blockposition);
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   public boolean getCanSpawnHere() {
      if (super.getCanSpawnHere()) {
         EntityPlayer entityhuman = this.world.getNearestPlayerNotCreative(this, 5.0D);
         return entityhuman == null;
      } else {
         return false;
      }
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.ARTHROPOD;
   }

   static class AIHideInStone extends EntityAIWander {
      private final EntitySilverfish silverfish;
      private EnumFacing facing;
      private boolean doMerge;

      public AIHideInStone(EntitySilverfish entitysilverfish) {
         super(entitysilverfish, 1.0D, 10);
         this.silverfish = entitysilverfish;
         this.setMutexBits(1);
      }

      public boolean shouldExecute() {
         if (!this.silverfish.world.getGameRules().getBoolean("mobGriefing")) {
            return false;
         } else if (this.silverfish.getAttackTarget() != null) {
            return false;
         } else if (!this.silverfish.getNavigator().noPath()) {
            return false;
         } else {
            Random random = this.silverfish.getRNG();
            if (random.nextInt(10) == 0) {
               this.facing = EnumFacing.random(random);
               BlockPos blockposition = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
               IBlockState iblockdata = this.silverfish.world.getBlockState(blockposition);
               if (BlockSilverfish.canContainSilverfish(iblockdata)) {
                  this.doMerge = true;
                  return true;
               }
            }

            this.doMerge = false;
            return super.shouldExecute();
         }
      }

      public boolean continueExecuting() {
         return this.doMerge ? false : super.continueExecuting();
      }

      public void startExecuting() {
         if (!this.doMerge) {
            super.startExecuting();
         } else {
            World world = this.silverfish.world;
            BlockPos blockposition = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
            IBlockState iblockdata = world.getBlockState(blockposition);
            if (BlockSilverfish.canContainSilverfish(iblockdata)) {
               if (CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, blockposition, Blocks.MONSTER_EGG, Block.getIdFromBlock(BlockSilverfish.getBlockById(iblockdata.getBlock().getMetaFromState(iblockdata)))).isCancelled()) {
                  return;
               }

               world.setBlockState(blockposition, Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.forModelBlock(iblockdata)), 3);
               this.silverfish.spawnExplosionParticle();
               this.silverfish.setDead();
            }
         }

      }
   }

   static class AISummonSilverfish extends EntityAIBase {
      private final EntitySilverfish silverfish;
      private int lookForFriends;

      public AISummonSilverfish(EntitySilverfish entitysilverfish) {
         this.silverfish = entitysilverfish;
      }

      public void notifyHurt() {
         if (this.lookForFriends == 0) {
            this.lookForFriends = 20;
         }

      }

      public boolean shouldExecute() {
         return this.lookForFriends > 0;
      }

      public void updateTask() {
         --this.lookForFriends;
         if (this.lookForFriends <= 0) {
            World world = this.silverfish.world;
            Random random = this.silverfish.getRNG();
            BlockPos blockposition = new BlockPos(this.silverfish);

            for(int i = 0; i <= 5 && i >= -5; i = i <= 0 ? 1 - i : 0 - i) {
               for(int j = 0; j <= 10 && j >= -10; j = j <= 0 ? 1 - j : 0 - j) {
                  for(int k = 0; k <= 10 && k >= -10; k = k <= 0 ? 1 - k : 0 - k) {
                     BlockPos blockposition1 = blockposition.add(j, i, k);
                     IBlockState iblockdata = world.getBlockState(blockposition1);
                     if (iblockdata.getBlock() == Blocks.MONSTER_EGG && !CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, blockposition1, Blocks.AIR, 0).isCancelled()) {
                        if (world.getGameRules().getBoolean("mobGriefing")) {
                           world.destroyBlock(blockposition1, true);
                        } else {
                           world.setBlockState(blockposition1, ((BlockSilverfish.EnumType)iblockdata.getValue(BlockSilverfish.VARIANT)).getModelBlock(), 3);
                        }

                        if (random.nextBoolean()) {
                           return;
                        }
                     }
                  }
               }
            }
         }

      }
   }
}
