package net.minecraft.entity.item;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityEnderCrystal extends Entity {
   private static final DataParameter BEAM_TARGET = EntityDataManager.createKey(EntityEnderCrystal.class, DataSerializers.OPTIONAL_BLOCK_POS);
   private static final DataParameter SHOW_BOTTOM = EntityDataManager.createKey(EntityEnderCrystal.class, DataSerializers.BOOLEAN);
   public int innerRotation;

   public EntityEnderCrystal(World var1) {
      super(var1);
      this.preventEntitySpawning = true;
      this.setSize(2.0F, 2.0F);
      this.innerRotation = this.rand.nextInt(100000);
   }

   public EntityEnderCrystal(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
      this.getDataManager().register(BEAM_TARGET, Optional.absent());
      this.getDataManager().register(SHOW_BOTTOM, Boolean.valueOf(true));
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      ++this.innerRotation;
      if (!this.world.isRemote) {
         BlockPos var1 = new BlockPos(this);
         if (this.world.provider instanceof WorldProviderEnd && this.world.getBlockState(var1).getBlock() != Blocks.FIRE) {
            this.world.setBlockState(var1, Blocks.FIRE.getDefaultState());
         }
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      if (this.getBeamTarget() != null) {
         var1.setTag("BeamTarget", NBTUtil.createPosTag(this.getBeamTarget()));
      }

      var1.setBoolean("ShowBottom", this.shouldShowBottom());
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      if (var1.hasKey("BeamTarget", 10)) {
         this.setBeamTarget(NBTUtil.getPosFromTag(var1.getCompoundTag("BeamTarget")));
      }

      if (var1.hasKey("ShowBottom", 1)) {
         this.setShowBottom(var1.getBoolean("ShowBottom"));
      }

   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (var1.getEntity() instanceof EntityDragon) {
         return false;
      } else {
         if (!this.isDead && !this.world.isRemote) {
            this.setDead();
            if (!this.world.isRemote) {
               this.world.createExplosion((Entity)null, this.posX, this.posY, this.posZ, 6.0F, true);
               this.onCrystalDestroyed(var1);
            }
         }

         return true;
      }
   }

   public void onKillCommand() {
      this.onCrystalDestroyed(DamageSource.generic);
      super.onKillCommand();
   }

   private void onCrystalDestroyed(DamageSource var1) {
      if (this.world.provider instanceof WorldProviderEnd) {
         WorldProviderEnd var2 = (WorldProviderEnd)this.world.provider;
         DragonFightManager var3 = var2.getDragonFightManager();
         if (var3 != null) {
            var3.onCrystalDestroyed(this, var1);
         }
      }

   }

   public void setBeamTarget(@Nullable BlockPos var1) {
      this.getDataManager().set(BEAM_TARGET, Optional.fromNullable(var1));
   }

   @Nullable
   public BlockPos getBeamTarget() {
      return (BlockPos)((Optional)this.getDataManager().get(BEAM_TARGET)).orNull();
   }

   public void setShowBottom(boolean var1) {
      this.getDataManager().set(SHOW_BOTTOM, Boolean.valueOf(var1));
   }

   public boolean shouldShowBottom() {
      return ((Boolean)this.getDataManager().get(SHOW_BOTTOM)).booleanValue();
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      return super.isInRangeToRenderDist(var1) || this.getBeamTarget() != null;
   }
}
