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

   public EntitySnowman(World var1) {
      super(var1);
      this.setSize(0.7F, 1.9F);
   }

   public static void registerFixesSnowman(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "SnowMan");
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
         int var1 = MathHelper.floor(this.posX);
         int var2 = MathHelper.floor(this.posY);
         int var3 = MathHelper.floor(this.posZ);
         if (this.isWet()) {
            this.attackEntityFrom(DamageSource.drown, 1.0F);
         }

         if (this.world.getBiome(new BlockPos(var1, 0, var3)).getFloatTemperature(new BlockPos(var1, var2, var3)) > 1.0F) {
            this.attackEntityFrom(CraftEventFactory.MELTING, 1.0F);
         }

         if (!this.world.getGameRules().getBoolean("mobGriefing")) {
            return;
         }

         for(int var4 = 0; var4 < 4; ++var4) {
            var1 = MathHelper.floor(this.posX + (double)((float)(var4 % 2 * 2 - 1) * 0.25F));
            var2 = MathHelper.floor(this.posY);
            var3 = MathHelper.floor(this.posZ + (double)((float)(var4 / 2 % 2 * 2 - 1) * 0.25F));
            BlockPos var5 = new BlockPos(var1, var2, var3);
            if (this.world.getBlockState(var5).getMaterial() == Material.AIR && this.world.getBiome(new BlockPos(var1, 0, var3)).getFloatTemperature(var5) < 0.8F && Blocks.SNOW_LAYER.canPlaceBlockAt(this.world, var5)) {
               BlockState var6 = this.world.getWorld().getBlockAt(var1, var2, var3).getState();
               var6.setType(CraftMagicNumbers.getMaterial(Blocks.SNOW_LAYER));
               EntityBlockFormEvent var7 = new EntityBlockFormEvent(this.getBukkitEntity(), var6.getBlock(), var6);
               this.world.getServer().getPluginManager().callEvent(var7);
               if (!var7.isCancelled()) {
                  var6.update(true);
               }
            }
         }
      }

   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_SNOWMAN;
   }

   public void attackEntityWithRangedAttack(EntityLivingBase var1, float var2) {
      EntitySnowball var3 = new EntitySnowball(this.world, this);
      double var4 = var1.posY + (double)var1.getEyeHeight() - 1.100000023841858D;
      double var6 = var1.posX - this.posX;
      double var8 = var4 - var3.posY;
      double var10 = var1.posZ - this.posZ;
      float var12 = MathHelper.sqrt(var6 * var6 + var10 * var10) * 0.2F;
      var3.setThrowableHeading(var6, var8 + (double)var12, var10, 1.6F, 12.0F);
      this.playSound(SoundEvents.ENTITY_SNOWMAN_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      this.world.spawnEntity(var3);
   }

   public float getEyeHeight() {
      return 1.7F;
   }

   protected boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (var3 != null && var3.getItem() == Items.SHEARS && !this.isPumpkinEquipped() && !this.world.isRemote) {
         this.setPumpkinEquipped(true);
         var3.damageItem(1, var1);
      }

      return super.processInteract(var1, var2, var3);
   }

   public boolean isPumpkinEquipped() {
      return (((Byte)this.dataManager.get(PUMPKIN_EQUIPPED)).byteValue() & 16) != 0;
   }

   public void setPumpkinEquipped(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(PUMPKIN_EQUIPPED)).byteValue();
      if (var1) {
         this.dataManager.set(PUMPKIN_EQUIPPED, Byte.valueOf((byte)(var2 | 16)));
      } else {
         this.dataManager.set(PUMPKIN_EQUIPPED, Byte.valueOf((byte)(var2 & -17)));
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
