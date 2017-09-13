package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAnimal extends EntityAgeable implements IAnimals {
   protected Block spawnableBlock = Blocks.GRASS;
   private int inLove;
   private EntityPlayer playerInLove;
   public ItemStack breedItem;

   public EntityAnimal(World world) {
      super(world);
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

   public float getBlockPathWeight(BlockPos blockposition) {
      return this.world.getBlockState(blockposition.down()).getBlock() == Blocks.GRASS ? 10.0F : this.world.getLightBrightness(blockposition) - 0.5F;
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setInteger("InLove", this.inLove);
   }

   public double getYOffset() {
      return 0.29D;
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.inLove = nbttagcompound.getInteger("InLove");
   }

   public boolean getCanSpawnHere() {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.getEntityBoundingBox().minY);
      int k = MathHelper.floor(this.posZ);
      BlockPos blockposition = new BlockPos(i, j, k);
      return this.world.getBlockState(blockposition.down()).getBlock() == this.spawnableBlock && this.world.getLight(blockposition) > 8 && super.getCanSpawnHere();
   }

   public int getTalkInterval() {
      return 120;
   }

   protected boolean canDespawn() {
      return false;
   }

   protected int getExperiencePoints(EntityPlayer entityhuman) {
      return 1 + this.world.rand.nextInt(3);
   }

   public boolean isBreedingItem(@Nullable ItemStack itemstack) {
      return itemstack == null ? false : itemstack.getItem() == Items.WHEAT;
   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null) {
         if (this.isBreedingItem(itemstack) && this.getGrowingAge() == 0 && this.inLove <= 0) {
            this.consumeItemFromStack(entityhuman, itemstack);
            this.setInLove(entityhuman);
            return true;
         }

         if (this.isChild() && this.isBreedingItem(itemstack)) {
            this.consumeItemFromStack(entityhuman, itemstack);
            this.ageUp((int)((float)(-this.getGrowingAge() / 20) * 0.1F), true);
            return true;
         }
      }

      return super.processInteract(entityhuman, enumhand, itemstack);
   }

   protected void consumeItemFromStack(EntityPlayer entityhuman, ItemStack itemstack) {
      if (!entityhuman.capabilities.isCreativeMode) {
         --itemstack.stackSize;
      }

   }

   public void setInLove(EntityPlayer entityhuman) {
      this.inLove = 600;
      this.playerInLove = entityhuman;
      this.breedItem = entityhuman.inventory.getCurrentItem();
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

   public boolean canMateWith(EntityAnimal entityanimal) {
      return entityanimal == this ? false : (entityanimal.getClass() != this.getClass() ? false : this.isInLove() && entityanimal.isInLove());
   }
}
