package net.minecraft.tileentity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class TileEntityPiston extends TileEntity implements ITickable {
   private IBlockState pistonState;
   private EnumFacing pistonFacing;
   private boolean extending;
   private boolean shouldHeadBeRendered;
   private float progress;
   private float lastProgress;

   public TileEntityPiston() {
   }

   public TileEntityPiston(IBlockState iblockdata, EnumFacing enumdirection, boolean flag, boolean flag1) {
      this.pistonState = iblockdata;
      this.pistonFacing = enumdirection;
      this.extending = flag;
      this.shouldHeadBeRendered = flag1;
   }

   public IBlockState getPistonState() {
      return this.pistonState;
   }

   public int getBlockMetadata() {
      return 0;
   }

   public boolean isExtending() {
      return this.extending;
   }

   public EnumFacing getFacing() {
      return this.pistonFacing;
   }

   private float getExtendedProgress(float f) {
      return this.extending ? f - 1.0F : 1.0F - f;
   }

   public AxisAlignedBB getAABB(IBlockAccess iblockaccess, BlockPos blockposition) {
      return this.getAABB(iblockaccess, blockposition, this.progress).union(this.getAABB(iblockaccess, blockposition, this.lastProgress));
   }

   public AxisAlignedBB getAABB(IBlockAccess iblockaccess, BlockPos blockposition, float f) {
      f = this.getExtendedProgress(f);
      return this.pistonState.getBoundingBox(iblockaccess, blockposition).offset((double)(f * (float)this.pistonFacing.getFrontOffsetX()), (double)(f * (float)this.pistonFacing.getFrontOffsetY()), (double)(f * (float)this.pistonFacing.getFrontOffsetZ()));
   }

   private void moveCollidedEntities() {
      AxisAlignedBB axisalignedbb = this.getAABB(this.world, this.pos).offset(this.pos);
      List list = this.world.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb);
      if (!list.isEmpty()) {
         EnumFacing enumdirection = this.extending ? this.pistonFacing : this.pistonFacing.getOpposite();

         for(int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
               if (this.pistonState.getBlock() == Blocks.SLIME_BLOCK) {
                  switch(TileEntityPiston.SyntheticClass_1.a[enumdirection.getAxis().ordinal()]) {
                  case 1:
                     entity.motionX = (double)enumdirection.getFrontOffsetX();
                     break;
                  case 2:
                     entity.motionY = (double)enumdirection.getFrontOffsetY();
                     break;
                  case 3:
                     entity.motionZ = (double)enumdirection.getFrontOffsetZ();
                  }
               }

               double d0 = 0.0D;
               double d1 = 0.0D;
               double d2 = 0.0D;
               AxisAlignedBB axisalignedbb1 = entity.getEntityBoundingBox();
               switch(TileEntityPiston.SyntheticClass_1.a[enumdirection.getAxis().ordinal()]) {
               case 1:
                  if (enumdirection.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     d0 = axisalignedbb.maxX - axisalignedbb1.minX;
                  } else {
                     d0 = axisalignedbb1.maxX - axisalignedbb.minX;
                  }

                  d0 = d0 + 0.01D;
                  break;
               case 2:
                  if (enumdirection.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     d1 = axisalignedbb.maxY - axisalignedbb1.minY;
                  } else {
                     d1 = axisalignedbb1.maxY - axisalignedbb.minY;
                  }

                  d1 = d1 + 0.01D;
                  break;
               case 3:
                  if (enumdirection.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     d2 = axisalignedbb.maxZ - axisalignedbb1.minZ;
                  } else {
                     d2 = axisalignedbb1.maxZ - axisalignedbb.minZ;
                  }

                  d2 = d2 + 0.01D;
               }

               entity.move(d0 * (double)enumdirection.getFrontOffsetX(), d1 * (double)enumdirection.getFrontOffsetY(), d2 * (double)enumdirection.getFrontOffsetZ());
            }
         }
      }

   }

   public void clearPistonTileEntity() {
      if (this.lastProgress < 1.0F && this.world != null) {
         this.progress = 1.0F;
         this.lastProgress = this.progress;
         this.world.removeTileEntity(this.pos);
         this.invalidate();
         if (this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION) {
            this.world.setBlockState(this.pos, this.pistonState, 3);
            this.world.notifyBlockOfStateChange(this.pos, this.pistonState.getBlock());
         }
      }

   }

   public void update() {
      if (this.world != null) {
         this.lastProgress = this.progress;
         if (this.lastProgress >= 1.0F) {
            this.moveCollidedEntities();
            this.world.removeTileEntity(this.pos);
            this.invalidate();
            if (this.world.getBlockState(this.pos).getBlock() == Blocks.PISTON_EXTENSION) {
               this.world.setBlockState(this.pos, this.pistonState, 3);
               this.world.notifyBlockOfStateChange(this.pos, this.pistonState.getBlock());
            }
         } else {
            this.progress += 0.5F;
            if (this.progress >= 1.0F) {
               this.progress = 1.0F;
            }

            this.moveCollidedEntities();
         }

      }
   }

   public static void registerFixesPiston(DataFixer dataconvertermanager) {
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.pistonState = Block.getBlockById(nbttagcompound.getInteger("blockId")).getStateFromMeta(nbttagcompound.getInteger("blockData"));
      this.pistonFacing = EnumFacing.getFront(nbttagcompound.getInteger("facing"));
      this.progress = nbttagcompound.getFloat("progress");
      this.lastProgress = this.progress;
      this.extending = nbttagcompound.getBoolean("extending");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("blockId", Block.getIdFromBlock(this.pistonState.getBlock()));
      nbttagcompound.setInteger("blockData", this.pistonState.getBlock().getMetaFromState(this.pistonState));
      nbttagcompound.setInteger("facing", this.pistonFacing.getIndex());
      nbttagcompound.setFloat("progress", this.lastProgress);
      nbttagcompound.setBoolean("extending", this.extending);
      return nbttagcompound;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.Axis.values().length];

      static {
         try {
            a[EnumFacing.Axis.X.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.Axis.Y.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.Axis.Z.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
