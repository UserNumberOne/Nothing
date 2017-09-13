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
   public EntityLeashKnot(World var1) {
      super(var1);
   }

   public EntityLeashKnot(World var1, BlockPos var2) {
      super(var1, var2);
      this.setPosition((double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D);
      this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.1875D, this.posY - 0.25D + 0.125D, this.posZ - 0.1875D, this.posX + 0.1875D, this.posY + 0.25D + 0.125D, this.posZ + 0.1875D));
   }

   public void setPosition(double var1, double var3, double var5) {
      super.setPosition((double)MathHelper.floor(var1) + 0.5D, (double)MathHelper.floor(var3) + 0.5D, (double)MathHelper.floor(var5) + 0.5D);
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
         boolean var4 = false;
         if (var2 != null && var2.getItem() == Items.LEAD) {
            for(EntityLiving var7 : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
               if (var7.getLeashed() && var7.getLeashedToEntity() == var1) {
                  if (CraftEventFactory.callPlayerLeashEntityEvent(var7, this, var1).isCancelled()) {
                     ((EntityPlayerMP)var1).connection.sendPacket(new SPacketEntityAttach(var7, var7.getLeashedToEntity()));
                  } else {
                     var7.setLeashedToEntity(this, true);
                     var4 = true;
                  }
               }
            }
         }

         if (!var4) {
            boolean var8 = true;

            for(EntityLiving var11 : this.world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(this.posX - 7.0D, this.posY - 7.0D, this.posZ - 7.0D, this.posX + 7.0D, this.posY + 7.0D, this.posZ + 7.0D))) {
               if (var11.getLeashed() && var11.getLeashedToEntity() == this) {
                  if (CraftEventFactory.callPlayerUnleashEntityEvent(var11, var1).isCancelled()) {
                     var8 = false;
                  } else {
                     var11.clearLeashed(true, !var1.capabilities.isCreativeMode);
                  }
               }
            }

            if (var8) {
               this.setDead();
            }
         }

         return true;
      }
   }

   public boolean onValidSurface() {
      return this.world.getBlockState(this.hangingPosition).getBlock() instanceof BlockFence;
   }

   public static EntityLeashKnot createKnot(World var0, BlockPos var1) {
      EntityLeashKnot var2 = new EntityLeashKnot(var0, var1);
      var2.forceSpawn = true;
      var0.spawnEntity(var2);
      var2.playPlaceSound();
      return var2;
   }

   public static EntityLeashKnot getKnotForPosition(World var0, BlockPos var1) {
      int var2 = var1.getX();
      int var3 = var1.getY();
      int var4 = var1.getZ();

      for(EntityLeashKnot var7 : var0.getEntitiesWithinAABB(EntityLeashKnot.class, new AxisAlignedBB((double)var2 - 1.0D, (double)var3 - 1.0D, (double)var4 - 1.0D, (double)var2 + 1.0D, (double)var3 + 1.0D, (double)var4 + 1.0D))) {
         if (var7.getHangingPosition().equals(var1)) {
            return var7;
         }
      }

      return null;
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_LEASHKNOT_PLACE, 1.0F, 1.0F);
   }
}
