package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityAnimal extends EntityAgeable implements IAnimals {
   protected Block spawnableBlock = Blocks.GRASS;
   private int inLove;
   private EntityPlayer playerInLove;

   public EntityAnimal(World var1) {
      super(worldIn);
   }

   protected void updateAITasks() {
      if (this.getGrowingAge() != 0) {
         this.inLove = 0;
      }

      super.updateAITasks();
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      if (this.getGrowingAge() != 0) {
         this.inLove = 0;
      }

      if (this.inLove > 0) {
         --this.inLove;
         if (this.inLove % 10 == 0) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else {
         this.inLove = 0;
         return super.attackEntityFrom(source, amount);
      }
   }

   public float getBlockPathWeight(BlockPos var1) {
      return this.world.getBlockState(pos.down()).getBlock() == Blocks.GRASS ? 10.0F : this.world.getLightBrightness(pos) - 0.5F;
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("InLove", this.inLove);
   }

   public double getYOffset() {
      return 0.29D;
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      this.inLove = compound.getInteger("InLove");
   }

   public boolean getCanSpawnHere() {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.getEntityBoundingBox().minY);
      int k = MathHelper.floor(this.posZ);
      BlockPos blockpos = new BlockPos(i, j, k);
      return this.world.getBlockState(blockpos.down()).getBlock() == this.spawnableBlock && this.world.getLight(blockpos) > 8 && super.getCanSpawnHere();
   }

   public int getTalkInterval() {
      return 120;
   }

   protected boolean canDespawn() {
      return false;
   }

   protected int getExperiencePoints(EntityPlayer var1) {
      return 1 + this.world.rand.nextInt(3);
   }

   public boolean isBreedingItem(@Nullable ItemStack var1) {
      return stack == null ? false : stack.getItem() == Items.WHEAT;
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (stack != null) {
         if (this.isBreedingItem(stack) && this.getGrowingAge() == 0 && this.inLove <= 0) {
            this.consumeItemFromStack(player, stack);
            this.setInLove(player);
            return true;
         }

         if (this.isChild() && this.isBreedingItem(stack)) {
            this.consumeItemFromStack(player, stack);
            this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
            return true;
         }
      }

      return super.processInteract(player, hand, stack);
   }

   protected void consumeItemFromStack(EntityPlayer var1, ItemStack var2) {
      if (!player.capabilities.isCreativeMode) {
         --stack.stackSize;
      }

   }

   public void setInLove(EntityPlayer var1) {
      this.inLove = 600;
      this.playerInLove = player;
      this.world.setEntityState(this, (byte)18);
   }

   public EntityPlayer getPlayerInLove() {
      return this.playerInLove;
   }

   public boolean isInLove() {
      return this.inLove > 0;
   }

   public void resetInLove() {
      this.inLove = 0;
   }

   public boolean canMateWith(EntityAnimal var1) {
      return otherAnimal == this ? false : (otherAnimal.getClass() != this.getClass() ? false : this.isInLove() && otherAnimal.isInLove());
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 18) {
         for(int i = 0; i < 7; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
         }
      } else {
         super.handleStatusUpdate(id);
      }

   }
}
