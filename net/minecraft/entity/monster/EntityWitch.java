package net.minecraft.entity.monster;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityWitch extends EntityMob implements IRangedAttackMob {
   private static final UUID MODIFIER_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
   private static final AttributeModifier MODIFIER = (new AttributeModifier(MODIFIER_UUID, "Drinking speed penalty", -0.25D, 0)).setSaved(false);
   private static final DataParameter IS_AGGRESSIVE = EntityDataManager.createKey(EntityWitch.class, DataSerializers.BOOLEAN);
   private int witchAttackTimer;

   public EntityWitch(World var1) {
      super(var1);
      this.setSize(0.6F, 1.95F);
   }

   public static void registerFixesWitch(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Witch");
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIAttackRanged(this, 1.0D, 60, 10.0F));
      this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(3, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
   }

   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(IS_AGGRESSIVE, Boolean.valueOf(false));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_WITCH_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_WITCH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_WITCH_DEATH;
   }

   public void setAggressive(boolean var1) {
      this.getDataManager().set(IS_AGGRESSIVE, Boolean.valueOf(var1));
   }

   public boolean isDrinkingPotion() {
      return ((Boolean)this.getDataManager().get(IS_AGGRESSIVE)).booleanValue();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(26.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public void onLivingUpdate() {
      if (!this.world.isRemote) {
         if (this.isDrinkingPotion()) {
            if (this.witchAttackTimer-- <= 0) {
               this.setAggressive(false);
               ItemStack var5 = this.getHeldItemMainhand();
               this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, (ItemStack)null);
               if (var5 != null && var5.getItem() == Items.POTIONITEM) {
                  List var6 = PotionUtils.getEffectsFromStack(var5);
                  if (var6 != null) {
                     for(PotionEffect var4 : var6) {
                        this.addPotionEffect(new PotionEffect(var4));
                     }
                  }
               }

               this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MODIFIER);
            }
         } else {
            PotionType var1 = null;
            if (this.rand.nextFloat() < 0.15F && this.isInsideOfMaterial(Material.WATER) && !this.isPotionActive(MobEffects.WATER_BREATHING)) {
               var1 = PotionTypes.WATER_BREATHING;
            } else if (this.rand.nextFloat() < 0.15F && (this.isBurning() || this.getLastDamageSource() != null && this.getLastDamageSource().isFireDamage()) && !this.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
               var1 = PotionTypes.FIRE_RESISTANCE;
            } else if (this.rand.nextFloat() < 0.05F && this.getHealth() < this.getMaxHealth()) {
               var1 = PotionTypes.HEALING;
            } else if (this.rand.nextFloat() < 0.5F && this.getAttackTarget() != null && !this.isPotionActive(MobEffects.SPEED) && this.getAttackTarget().getDistanceSqToEntity(this) > 121.0D) {
               var1 = PotionTypes.SWIFTNESS;
            }

            if (var1 != null) {
               this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), var1));
               this.witchAttackTimer = this.getHeldItemMainhand().getMaxItemUseDuration();
               this.setAggressive(true);
               this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITCH_DRINK, this.getSoundCategory(), 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
               IAttributeInstance var2 = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
               var2.removeModifier(MODIFIER);
               var2.applyModifier(MODIFIER);
            }
         }

         if (this.rand.nextFloat() < 7.5E-4F) {
            this.world.setEntityState(this, (byte)15);
         }
      }

      super.onLivingUpdate();
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 15) {
         for(int var2 = 0; var2 < this.rand.nextInt(35) + 10; ++var2) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_WITCH, this.posX + this.rand.nextGaussian() * 0.12999999523162842D, this.getEntityBoundingBox().maxY + 0.5D + this.rand.nextGaussian() * 0.12999999523162842D, this.posZ + this.rand.nextGaussian() * 0.12999999523162842D, 0.0D, 0.0D, 0.0D);
         }
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   protected float applyPotionDamageCalculations(DamageSource var1, float var2) {
      var2 = super.applyPotionDamageCalculations(var1, var2);
      if (var1.getEntity() == this) {
         var2 = 0.0F;
      }

      if (var1.isMagicDamage()) {
         var2 = (float)((double)var2 * 0.15D);
      }

      return var2;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_WITCH;
   }

   public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2) {
      if (!this.isDrinkingPotion()) {
         double var3 = var1.posY + (double)var1.getEyeHeight() - 1.100000023841858D;
         double var5 = var1.posX + var1.motionX - this.posX;
         double var7 = var3 - this.posY;
         double var9 = var1.posZ + var1.motionZ - this.posZ;
         float var11 = MathHelper.sqrt(var5 * var5 + var9 * var9);
         PotionType var12 = PotionTypes.HARMING;
         if (var11 >= 8.0F && !var1.isPotionActive(MobEffects.SLOWNESS)) {
            var12 = PotionTypes.SLOWNESS;
         } else if (var1.getHealth() >= 8.0F && !var1.isPotionActive(MobEffects.POISON)) {
            var12 = PotionTypes.POISON;
         } else if (var11 <= 3.0F && !var1.isPotionActive(MobEffects.WEAKNESS) && this.rand.nextFloat() < 0.25F) {
            var12 = PotionTypes.WEAKNESS;
         }

         EntityPotion var13 = new EntityPotion(this.world, this, PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), var12));
         var13.rotationPitch -= -20.0F;
         var13.setThrowableHeading(var5, var7 + (double)(var11 * 0.2F), var9, 0.75F, 8.0F);
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
         this.world.spawnEntity(var13);
      }

   }

   public float getEyeHeight() {
      return 1.62F;
   }
}
