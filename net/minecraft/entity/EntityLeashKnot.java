package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityLeashKnot extends EntityHanging {
   public EntityLeashKnot(World world) {
      super(world);
   }

   public EntityLeashKnot(World world, BlockPos blockposition) {
      super(world, blockposition);
      this.setPosition((double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.5D, (double)blockposition.getZ() + 0.5D);
      this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.1875D, this.posY - 0.25D + 0.125D, this.posZ - 0.1875D, this.posX + 0.1875D, this.posY + 0.25D + 0.125D, this.posZ + 0.1875D));
   }

   public void setPosition(double d0, double d1, double d2) {
      super.setPosition((double)MathHelper.floor(d0) + 0.5D, (double)MathHelper.floor(d1) + 0.5D, (double)MathHelper.floor(d2) + 0.5D);
   }

   protected void updateBoundingBox() {
      this.posX = (double)this.hangingPosition.getX() + 0.5D;
      this.posY = (double)this.hangingPosition.getY() + 0.5D;
      this.posZ = (double)this.hangingPosition.getZ() + 0.5D;
   }

   public void updateFacingWithBoundingBox(EnumFacing enumdirection) {
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

   public void onBroken(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_LEASHKNOT_BREAK, 1.0F, 1.0F);
   }

   public boolean writeToNBTOptional(NBTTagCompound nbttagcompound) {
      return false;
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
   }

   public boolean processInitialInteract(EntityPlayer entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (this.world.isRemote) {
         return true;
      } else {
         boolean flag = false;
         if (itemstack != null && itemstack.getItem() == Items.LEAD) {
            for(EntityLiving entityinsentient : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
               if (entityinsentient.getLeashed() && entityinsentient.getLeashedToEntity() == entityhuman) {
                  if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, this, entityhuman).isCancelled()) {
                     ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketEntityAttach(entityinsentient, entityinsentient.getLeashedToEntity()));
                  } else {
                     entityinsentient.setLeashedToEntity(this, true);
                     flag = true;
                  }
               }
            }
         }

         if (!flag) {
            boolean die = true;

            for(EntityLiving entityinsentient : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
               if (entityinsentient.getLeashed() && entityinsentient.getLeashedToEntity() == this) {
                  if (CraftEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman).isCancelled()) {
                     die = false;
                  } else {
                     entityinsentient.clearLeashed(true, !entityhuman.capabilities.isCreativeMode);
                  }
               }
            }

            if (die) {
               this.setDead();
            }
         }

         return true;
      }
   }

   public boolean onValidSurface() {
      return this.world.getBlockState(this.hangingPosition).getBlock() instanceof BlockFence;
   }

   public static EntityLeashKnot createKnot(World world, BlockPos blockposition) {
      EntityLeashKnot entityleash = new EntityLeashKnot(world, blockposition);
      entityleash.forceSpawn = true;
      world.spawnEntity(entityleash);
      entityleash.playPlaceSound();
      return entityleash;
   }

   public static EntityLeashKnot getKnotForPosition(World world, BlockPos blockposition) {
      int i = blockposition.getX();
      int j = blockposition.getY();
      int k = blockposition.getZ();

      for(EntityLeashKnot entityleash : world.getEntitiesWithinAABB(EntityLeashKnot.class, new AxisAlignedBB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D))) {
         if (entityleash.getHangingPosition().equals(blockposition)) {
            return entityleash;
         }
      }

      return null;
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_LEASHKNOT_PLACE, 1.0F, 1.0F);
   }
}
