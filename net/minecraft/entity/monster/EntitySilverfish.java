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

   public EntitySilverfish(World var1) {
      super(var1);
      this.setSize(0.4F, 0.3F);
   }

   public static void registerFixesSilverfish(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Silverfish");
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

   protected void playStepSound(BlockPos var1, Block var2) {
      this.playSound(SoundEvents.ENTITY_SILVERFISH_STEP, 0.15F, 1.0F);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         if ((var1 instanceof EntityDamageSource || var1 == DamageSource.magic) && this.summonSilverfish != null) {
            this.summonSilverfish.notifyHurt();
         }

         return super.attackEntityFrom(var1, var2);
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

   public float getBlockPathWeight(BlockPos var1) {
      return this.world.getBlockState(var1.down()).getBlock() == Blocks.STONE ? 10.0F : super.getBlockPathWeight(var1);
   }

   protected boolean isValidLightLevel() {
      return true;
   }

   public boolean getCanSpawnHere() {
      if (super.getCanSpawnHere()) {
         EntityPlayer var1 = this.world.getNearestPlayerNotCreative(this, 5.0D);
         return var1 == null;
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

      public AIHideInStone(EntitySilverfish var1) {
         super(var1, 1.0D, 10);
         this.silverfish = var1;
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
            Random var1 = this.silverfish.getRNG();
            if (var1.nextInt(10) == 0) {
               this.facing = EnumFacing.random(var1);
               BlockPos var2 = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
               IBlockState var3 = this.silverfish.world.getBlockState(var2);
               if (BlockSilverfish.canContainSilverfish(var3)) {
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
            World var1 = this.silverfish.world;
            BlockPos var2 = (new BlockPos(this.silverfish.posX, this.silverfish.posY + 0.5D, this.silverfish.posZ)).offset(this.facing);
            IBlockState var3 = var1.getBlockState(var2);
            if (BlockSilverfish.canContainSilverfish(var3)) {
               if (CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, var2, Blocks.MONSTER_EGG, Block.getIdFromBlock(BlockSilverfish.getBlockById(var3.getBlock().getMetaFromState(var3)))).isCancelled()) {
                  return;
               }

               var1.setBlockState(var2, Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.forModelBlock(var3)), 3);
               this.silverfish.spawnExplosionParticle();
               this.silverfish.setDead();
            }
         }

      }
   }

   static class AISummonSilverfish extends EntityAIBase {
      private final EntitySilverfish silverfish;
      private int lookForFriends;

      public AISummonSilverfish(EntitySilverfish var1) {
         this.silverfish = var1;
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
            World var1 = this.silverfish.world;
            Random var2 = this.silverfish.getRNG();
            BlockPos var3 = new BlockPos(this.silverfish);

            for(int var4 = 0; var4 <= 5 && var4 >= -5; var4 = var4 <= 0 ? 1 - var4 : 0 - var4) {
               for(int var5 = 0; var5 <= 10 && var5 >= -10; var5 = var5 <= 0 ? 1 - var5 : 0 - var5) {
                  for(int var6 = 0; var6 <= 10 && var6 >= -10; var6 = var6 <= 0 ? 1 - var6 : 0 - var6) {
                     BlockPos var7 = var3.add(var5, var4, var6);
                     IBlockState var8 = var1.getBlockState(var7);
                     if (var8.getBlock() == Blocks.MONSTER_EGG && !CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, var7, Blocks.AIR, 0).isCancelled()) {
                        if (var1.getGameRules().getBoolean("mobGriefing")) {
                           var1.destroyBlock(var7, true);
                        } else {
                           var1.setBlockState(var7, ((BlockSilverfish.EnumType)var8.getValue(BlockSilverfish.VARIANT)).getModelBlock(), 3);
                        }

                        if (var2.nextBoolean()) {
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
