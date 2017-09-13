package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLeashKnot extends EntityHanging {
   public EntityLeashKnot(World var1) {
      super(worldIn);
   }

   public EntityLeashKnot(World var1, BlockPos var2) {
      super(worldIn, hangingPositionIn);
      this.setPosition((double)hangingPositionIn.getX() + 0.5D, (double)hangingPositionIn.getY() + 0.5D, (double)hangingPositionIn.getZ() + 0.5D);
      float f = 0.125F;
      float f1 = 0.1875F;
      float f2 = 0.25F;
      this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.1875D, this.posY - 0.25D + 0.125D, this.posZ - 0.1875D, this.posX + 0.1875D, this.posY + 0.25D + 0.125D, this.posZ + 0.1875D));
   }

   public void setPosition(double var1, double var3, double var5) {
      super.setPosition((double)MathHelper.floor(x) + 0.5D, (double)MathHelper.floor(y) + 0.5D, (double)MathHelper.floor(z) + 0.5D);
   }

   protected void updateBoundingBox() {
      this.posX = (double)this.hangingPosition.getX() + 0.5D;
      this.posY = (double)this.hangingPosition.getY() + 0.5D;
      this.posZ = (double)this.hangingPosition.getZ() + 0.5D;
   }

   public void updateFacingWithBoundingBox(EnumFacing var1) {
   }

   public int getWidthPixels() {
      return 9;
   }

   public int getHeightPixels() {
      return 9;
   }

   public float getEyeHeight() {
      return -0.0625F;
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      return distance < 1024.0D;
   }

   public void onBroken(@Nullable Entity var1) {
      this.playSound(SoundEvents.ENTITY_LEASHKNOT_BREAK, 1.0F, 1.0F);
   }

   public boolean writeToNBTOptional(NBTTagCompound var1) {
      return false;
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (this.world.isRemote) {
         return true;
      } else {
         boolean flag = false;
         if (stack != null && stack.getItem() == Items.LEAD) {
            double d0 = 7.0D;

            for(EntityLiving entityliving : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
               if (entityliving.getLeashed() && entityliving.getLeashedToEntity() == player) {
                  entityliving.setLeashedToEntity(this, true);
                  flag = true;
               }
            }
         }

         if (!flag) {
            this.setDead();
            if (player.capabilities.isCreativeMode) {
               double d1 = 7.0D;

               for(EntityLiving entityliving1 : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
                  if (entityliving1.getLeashed() && entityliving1.getLeashedToEntity() == this) {
                     entityliving1.clearLeashed(true, false);
                  }
               }
            }
         }

         return true;
      }
   }

   public boolean onValidSurface() {
      return this.world.getBlockState(this.hangingPosition).getBlock() instanceof BlockFence;
   }

   public static EntityLeashKnot createKnot(World var0, BlockPos var1) {
      EntityLeashKnot entityleashknot = new EntityLeashKnot(worldIn, fence);
      entityleashknot.forceSpawn = true;
      worldIn.spawnEntity(entityleashknot);
      entityleashknot.playPlaceSound();
      return entityleashknot;
   }

   public static EntityLeashKnot getKnotForPosition(World var0, BlockPos var1) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();

      for(EntityLeashKnot entityleashknot : worldIn.getEntitiesWithinAABB(EntityLeashKnot.class, new AxisAlignedBB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D))) {
         if (entityleashknot.getHangingPosition().equals(pos)) {
            return entityleashknot;
         }
      }

      return null;
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_LEASHKNOT_PLACE, 1.0F, 1.0F);
   }
}
