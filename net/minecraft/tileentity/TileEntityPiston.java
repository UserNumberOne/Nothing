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

   public TileEntityPiston(IBlockState var1, EnumFacing var2, boolean var3, boolean var4) {
      this.pistonState = var1;
      this.pistonFacing = var2;
      this.extending = var3;
      this.shouldHeadBeRendered = var4;
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

   private float getExtendedProgress(float var1) {
      return this.extending ? var1 - 1.0F : 1.0F - var1;
   }

   public AxisAlignedBB getAABB(IBlockAccess var1, BlockPos var2) {
      return this.getAABB(var1, var2, this.progress).union(this.getAABB(var1, var2, this.lastProgress));
   }

   public AxisAlignedBB getAABB(IBlockAccess var1, BlockPos var2, float var3) {
      var3 = this.getExtendedProgress(var3);
      return this.pistonState.getBoundingBox(var1, var2).offset((double)(var3 * (float)this.pistonFacing.getFrontOffsetX()), (double)(var3 * (float)this.pistonFacing.getFrontOffsetY()), (double)(var3 * (float)this.pistonFacing.getFrontOffsetZ()));
   }

   private void moveCollidedEntities() {
      AxisAlignedBB var1 = this.getAABB(this.world, this.pos).offset(this.pos);
      List var2 = this.world.getEntitiesWithinAABBExcludingEntity((Entity)null, var1);
      if (!var2.isEmpty()) {
         EnumFacing var3 = this.extending ? this.pistonFacing : this.pistonFacing.getOpposite();

         for(int var4 = 0; var4 < var2.size(); ++var4) {
            Entity var5 = (Entity)var2.get(var4);
            if (var5.getPushReaction() != EnumPushReaction.IGNORE) {
               if (this.pistonState.getBlock() == Blocks.SLIME_BLOCK) {
                  switch(TileEntityPiston.SyntheticClass_1.a[var3.getAxis().ordinal()]) {
                  case 1:
                     var5.motionX = (double)var3.getFrontOffsetX();
                     break;
                  case 2:
                     var5.motionY = (double)var3.getFrontOffsetY();
                     break;
                  case 3:
                     var5.motionZ = (double)var3.getFrontOffsetZ();
                  }
               }

               double var6 = 0.0D;
               double var8 = 0.0D;
               double var10 = 0.0D;
               AxisAlignedBB var12 = var5.getEntityBoundingBox();
               switch(TileEntityPiston.SyntheticClass_1.a[var3.getAxis().ordinal()]) {
               case 1:
                  if (var3.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     var6 = var1.maxX - var12.minX;
                  } else {
                     var6 = var12.maxX - var1.minX;
                  }

                  var6 = var6 + 0.01D;
                  break;
               case 2:
                  if (var3.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     var8 = var1.maxY - var12.minY;
                  } else {
                     var8 = var12.maxY - var1.minY;
                  }

                  var8 = var8 + 0.01D;
                  break;
               case 3:
                  if (var3.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                     var10 = var1.maxZ - var12.minZ;
                  } else {
                     var10 = var12.maxZ - var1.minZ;
                  }

                  var10 = var10 + 0.01D;
               }

               var5.move(var6 * (double)var3.getFrontOffsetX(), var8 * (double)var3.getFrontOffsetY(), var10 * (double)var3.getFrontOffsetZ());
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

   public static void registerFixesPiston(DataFixer var0) {
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.pistonState = Block.getBlockById(var1.getInteger("blockId")).getStateFromMeta(var1.getInteger("blockData"));
      this.pistonFacing = EnumFacing.getFront(var1.getInteger("facing"));
      this.progress = var1.getFloat("progress");
      this.lastProgress = this.progress;
      this.extending = var1.getBoolean("extending");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setInteger("blockId", Block.getIdFromBlock(this.pistonState.getBlock()));
      var1.setInteger("blockData", this.pistonState.getBlock().getMetaFromState(this.pistonState));
      var1.setInteger("facing", this.pistonFacing.getIndex());
      var1.setFloat("progress", this.lastProgress);
      var1.setBoolean("extending", this.extending);
      return var1;
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
