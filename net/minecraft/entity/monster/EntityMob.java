package net.minecraft.entity.monster;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public abstract class EntityMob extends EntityCreature implements IMob {
   public EntityMob(World var1) {
      super(var1);
      this.experienceValue = 5;
   }

   public static void registerFixesMonster(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Monster");
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.HOSTILE;
   }

   public void onLivingUpdate() {
      this.updateArmSwingProgress();
      float var1 = this.getBrightness(1.0F);
      if (var1 > 0.5F) {
         this.entityAge += 2;
      }

      super.onLivingUpdate();
   }

   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
         this.setDead();
      }

   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_HOSTILE_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_HOSTILE_SPLASH;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return this.isEntityInvulnerable(var1) ? false : super.attackEntityFrom(var1, var2);
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_HOSTILE_DEATH;
   }

   protected SoundEvent getFallSound(int var1) {
      return var1 > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
   }

   public boolean attackEntityAsMob(Entity var1) {
      float var2 = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
      int var3 = 0;
      if (var1 instanceof EntityLivingBase) {
         var2 += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)var1).getCreatureAttribute());
         var3 += EnchantmentHelper.getKnockbackModifier(this);
      }

      boolean var4 = var1.attackEntityFrom(DamageSource.causeMobDamage(this), var2);
      if (var4) {
         if (var3 > 0 && var1 instanceof EntityLivingBase) {
            ((EntityLivingBase)var1).knockBack(this, (float)var3 * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
            this.motionX *= 0.6D;
            this.motionZ *= 0.6D;
         }

         int var5 = EnchantmentHelper.getFireAspectModifier(this);
         if (var5 > 0) {
            var1.setFire(var5 * 4);
         }

         if (var1 instanceof EntityPlayer) {
            EntityPlayer var6 = (EntityPlayer)var1;
            ItemStack var7 = this.getHeldItemMainhand();
            ItemStack var8 = var6.isHandActive() ? var6.getActiveItemStack() : null;
            if (var7 != null && var8 != null && var7.getItem() instanceof ItemAxe && var8.getItem() == Items.SHIELD) {
               float var9 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
               if (this.rand.nextFloat() < var9) {
                  var6.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                  this.world.setEntityState(var6, (byte)30);
               }
            }
         }

         this.applyEnchantments(this, var1);
      }

      return var4;
   }

   public float getBlockPathWeight(BlockPos var1) {
      return 0.5F - this.world.getLightBrightness(var1);
   }

   protected boolean isValidLightLevel() {
      BlockPos var1 = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
      if (this.world.getLightFor(EnumSkyBlock.SKY, var1) > this.rand.nextInt(32)) {
         return false;
      } else {
         int var2 = this.world.getLightFromNeighbors(var1);
         if (this.world.isThundering()) {
            int var3 = this.world.getSkylightSubtracted();
            this.world.setSkylightSubtracted(10);
            var2 = this.world.getLightFromNeighbors(var1);
            this.world.setSkylightSubtracted(var3);
         }

         return var2 <= this.rand.nextInt(8);
      }
   }

   public boolean getCanSpawnHere() {
      return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.isValidLightLevel() && super.getCanSpawnHere();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
   }

   protected boolean canDropLoot() {
      return true;
   }
}
