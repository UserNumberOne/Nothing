package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EntitySnowman extends EntityGolem implements IRangedAttackMob {
   private static final DataParameter PUMPKIN_EQUIPPED = EntityDataManager.createKey(EntitySnowman.class, DataSerializers.BYTE);

   public EntitySnowman(World world) {
      super(world);
      this.setSize(0.7F, 1.9F);
   }

   public static void registerFixesSnowman(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "SnowMan");
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAIAttackRanged(this, 1.25D, 20, 10.0F));
      this.tasks.addTask(2, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(4, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityLiving.class, 10, true, false, IMob.MOB_SELECTOR));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PUMPKIN_EQUIPPED, Byte.valueOf((byte)0));
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (!this.world.isRemote) {
         int i = MathHelper.floor(this.posX);
         int j = MathHelper.floor(this.posY);
         int k = MathHelper.floor(this.posZ);
         if (this.isWet()) {
            this.attackEntityFrom(DamageSource.drown, 1.0F);
         }

         if (this.world.getBiome(new BlockPos(i, 0, k)).getFloatTemperature(new BlockPos(i, j, k)) > 1.0F) {
            this.attackEntityFrom(CraftEventFactory.MELTING, 1.0F);
         }

         if (!this.world.getGameRules().getBoolean("mobGriefing")) {
            return;
         }

         for(int l = 0; l < 4; ++l) {
            i = MathHelper.floor(this.posX + (double)((float)(l % 2 * 2 - 1) * 0.25F));
            j = MathHelper.floor(this.posY);
            k = MathHelper.floor(this.posZ + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
            BlockPos blockposition = new BlockPos(i, j, k);
            if (this.world.getBlockState(blockposition).getMaterial() == Material.AIR && this.world.getBiome(new BlockPos(i, 0, k)).getFloatTemperature(blockposition) < 0.8F && Blocks.SNOW_LAYER.canPlaceBlockAt(this.world, blockposition)) {
               BlockState blockState = this.world.getWorld().getBlockAt(i, j, k).getState();
               blockState.setType(CraftMagicNumbers.getMaterial(Blocks.SNOW_LAYER));
               EntityBlockFormEvent event = new EntityBlockFormEvent(this.getBukkitEntity(), blockState.getBlock(), blockState);
               this.world.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  blockState.update(true);
               }
            }
         }
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_SNOWMAN;
   }

   public void attackEntityWithRangedAttack(EntityLivingBase entityliving, float f) {
      EntitySnowball entitysnowball = new EntitySnowball(this.world, this);
      double d0 = entityliving.posY + (double)entityliving.getEyeHeight() - 1.100000023841858D;
      double d1 = entityliving.posX - this.posX;
      double d2 = d0 - entitysnowball.posY;
      double d3 = entityliving.posZ - this.posZ;
      float f1 = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.2F;
      entitysnowball.setThrowableHeading(d1, d2 + (double)f1, d3, 1.6F, 12.0F);
      this.playSound(SoundEvents.ENTITY_SNOWMAN_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(entitysnowball);
   }

   public float getEyeHeight() {
      return 1.7F;
   }

   protected boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null && itemstack.getItem() == Items.SHEARS && !this.isPumpkinEquipped() && !this.world.isRemote) {
         this.setPumpkinEquipped(true);
         itemstack.damageItem(1, entityhuman);
      }

      return super.processInteract(entityhuman, enumhand, itemstack);
   }

   public boolean isPumpkinEquipped() {
      return (((Byte)this.dataManager.get(PUMPKIN_EQUIPPED)).byteValue() & 16) != 0;
   }

   public void setPumpkinEquipped(boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(PUMPKIN_EQUIPPED)).byteValue();
      if (flag) {
         this.dataManager.set(PUMPKIN_EQUIPPED, Byte.valueOf((byte)(b0 | 16)));
      } else {
         this.dataManager.set(PUMPKIN_EQUIPPED, Byte.valueOf((byte)(b0 & -17)));
      }

   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SNOWMAN_AMBIENT;
   }

   @Nullable
   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_SNOWMAN_HURT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SNOWMAN_DEATH;
   }
}
