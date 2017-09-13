package net.minecraft.world.gen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public abstract class StructureComponent {
   protected StructureBoundingBox boundingBox;
   @Nullable
   private EnumFacing coordBaseMode;
   private Mirror mirror;
   private Rotation rotation;
   protected int componentType;

   public StructureComponent() {
   }

   protected StructureComponent(int var1) {
      this.componentType = type;
   }

   public final NBTTagCompound createStructureBaseNBT() {
      if (MapGenStructureIO.getStructureComponentName(this) == null) {
         throw new RuntimeException("StructureComponent \"" + this.getClass().getName() + "\" missing ID Mapping, Modder see MapGenStructureIO");
      } else {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         nbttagcompound.setString("id", MapGenStructureIO.getStructureComponentName(this));
         nbttagcompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
         EnumFacing enumfacing = this.getCoordBaseMode();
         nbttagcompound.setInteger("O", enumfacing == null ? -1 : enumfacing.getHorizontalIndex());
         nbttagcompound.setInteger("GD", this.componentType);
         this.writeStructureToNBT(nbttagcompound);
         return nbttagcompound;
      }
   }

   protected abstract void writeStructureToNBT(NBTTagCompound var1);

   public void readStructureBaseNBT(World var1, NBTTagCompound var2) {
      if (tagCompound.hasKey("BB")) {
         this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
      }

      int i = tagCompound.getInteger("O");
      this.setCoordBaseMode(i == -1 ? null : EnumFacing.getHorizontal(i));
      this.componentType = tagCompound.getInteger("GD");
      this.readStructureFromNBT(tagCompound);
   }

   protected abstract void readStructureFromNBT(NBTTagCompound var1);

   public void buildComponent(StructureComponent var1, List var2, Random var3) {
   }

   public abstract boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3);

   public StructureBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getComponentType() {
      return this.componentType;
   }

   public static StructureComponent findIntersecting(List var0, StructureBoundingBox var1) {
      for(StructureComponent structurecomponent : listIn) {
         if (structurecomponent.getBoundingBox() != null && structurecomponent.getBoundingBox().intersectsWith(boundingboxIn)) {
            return structurecomponent;
         }
      }

      return null;
   }

   public BlockPos getBoundingBoxCenter() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected boolean isLiquidInStructureBoundingBox(World var1, StructureBoundingBox var2) {
      int i = Math.max(this.boundingBox.minX - 1, boundingboxIn.minX);
      int j = Math.max(this.boundingBox.minY - 1, boundingboxIn.minY);
      int k = Math.max(this.boundingBox.minZ - 1, boundingboxIn.minZ);
      int l = Math.min(this.boundingBox.maxX + 1, boundingboxIn.maxX);
      int i1 = Math.min(this.boundingBox.maxY + 1, boundingboxIn.maxY);
      int j1 = Math.min(this.boundingBox.maxZ + 1, boundingboxIn.maxZ);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = k; l1 <= j1; ++l1) {
            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(k1, j, l1)).getMaterial().isLiquid()) {
               return true;
            }

            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(k1, i1, l1)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int i2 = i; i2 <= l; ++i2) {
         for(int k2 = j; k2 <= i1; ++k2) {
            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i2, k2, k)).getMaterial().isLiquid()) {
               return true;
            }

            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i2, k2, j1)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int j2 = k; j2 <= j1; ++j2) {
         for(int l2 = j; l2 <= i1; ++l2) {
            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(i, l2, j2)).getMaterial().isLiquid()) {
               return true;
            }

            if (worldIn.getBlockState(blockpos$mutableblockpos.setPos(l, l2, j2)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      return false;
   }

   protected int getXWithOffset(int var1, int var2) {
      EnumFacing enumfacing = this.getCoordBaseMode();
      if (enumfacing == null) {
         return x;
      } else {
         switch(enumfacing) {
         case NORTH:
         case SOUTH:
            return this.boundingBox.minX + x;
         case WEST:
            return this.boundingBox.maxX - z;
         case EAST:
            return this.boundingBox.minX + z;
         default:
            return x;
         }
      }
   }

   protected int getYWithOffset(int var1) {
      return this.getCoordBaseMode() == null ? y : y + this.boundingBox.minY;
   }

   protected int getZWithOffset(int var1, int var2) {
      EnumFacing enumfacing = this.getCoordBaseMode();
      if (enumfacing == null) {
         return z;
      } else {
         switch(enumfacing) {
         case NORTH:
            return this.boundingBox.maxZ - z;
         case SOUTH:
            return this.boundingBox.minZ + z;
         case WEST:
         case EAST:
            return this.boundingBox.minZ + x;
         default:
            return z;
         }
      }
   }

   protected void setBlockState(World var1, IBlockState var2, int var3, int var4, int var5, StructureBoundingBox var6) {
      BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
      if (boundingboxIn.isVecInside(blockpos)) {
         if (this.mirror != Mirror.NONE) {
            blockstateIn = blockstateIn.withMirror(this.mirror);
         }

         if (this.rotation != Rotation.NONE) {
            blockstateIn = blockstateIn.withRotation(this.rotation);
         }

         worldIn.setBlockState(blockpos, blockstateIn, 2);
      }

   }

   protected IBlockState getBlockStateFromPos(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      int i = this.getXWithOffset(x, z);
      int j = this.getYWithOffset(y);
      int k = this.getZWithOffset(x, z);
      BlockPos blockpos = new BlockPos(i, j, k);
      return !boundingboxIn.isVecInside(blockpos) ? Blocks.AIR.getDefaultState() : worldIn.getBlockState(blockpos);
   }

   protected int func_189916_b(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      int i = this.getXWithOffset(p_189916_2_, p_189916_4_);
      int j = this.getYWithOffset(p_189916_3_ + 1);
      int k = this.getZWithOffset(p_189916_2_, p_189916_4_);
      BlockPos blockpos = new BlockPos(i, j, k);
      return !p_189916_5_.isVecInside(blockpos) ? EnumSkyBlock.SKY.defaultLightValue : p_189916_1_.getLightFor(EnumSkyBlock.SKY, blockpos);
   }

   protected void fillWithAir(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               this.setBlockState(worldIn, Blocks.AIR.getDefaultState(), j, i, k, structurebb);
            }
         }
      }

   }

   protected void fillWithBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, IBlockState var9, IBlockState var10, boolean var11) {
      for(int i = yMin; i <= yMax; ++i) {
         for(int j = xMin; j <= xMax; ++j) {
            for(int k = zMin; k <= zMax; ++k) {
               if (!existingOnly || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) {
                  if (i != yMin && i != yMax && j != xMin && j != xMax && k != zMin && k != zMax) {
                     this.setBlockState(worldIn, insideBlockState, j, i, k, boundingboxIn);
                  } else {
                     this.setBlockState(worldIn, boundaryBlockState, j, i, k, boundingboxIn);
                  }
               }
            }
         }
      }

   }

   protected void fillWithRandomizedBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, Random var10, StructureComponent.BlockSelector var11) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               if (!alwaysReplace || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) {
                  blockselector.selectBlocks(rand, j, i, k, i == minY || i == maxY || j == minX || j == maxX || k == minZ || k == maxZ);
                  this.setBlockState(worldIn, blockselector.getBlockState(), j, i, k, boundingboxIn);
               }
            }
         }
      }

   }

   protected void func_189914_a(World var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7, int var8, int var9, int var10, IBlockState var11, IBlockState var12, boolean var13, int var14) {
      for(int i = p_189914_6_; i <= p_189914_9_; ++i) {
         for(int j = p_189914_5_; j <= p_189914_8_; ++j) {
            for(int k = p_189914_7_; k <= p_189914_10_; ++k) {
               if (p_189914_3_.nextFloat() <= p_189914_4_ && (!p_189914_13_ || this.getBlockStateFromPos(p_189914_1_, j, i, k, p_189914_2_).getMaterial() != Material.AIR) && (p_189914_14_ <= 0 || this.func_189916_b(p_189914_1_, j, i, k, p_189914_2_) < p_189914_14_)) {
                  if (i != p_189914_6_ && i != p_189914_9_ && j != p_189914_5_ && j != p_189914_8_ && k != p_189914_7_ && k != p_189914_10_) {
                     this.setBlockState(p_189914_1_, p_189914_12_, j, i, k, p_189914_2_);
                  } else {
                     this.setBlockState(p_189914_1_, p_189914_11_, j, i, k, p_189914_2_);
                  }
               }
            }
         }
      }

   }

   protected void randomlyPlaceBlock(World var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7, IBlockState var8) {
      if (rand.nextFloat() < chance) {
         this.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
      }

   }

   protected void randomlyRareFillWithBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, IBlockState var9, boolean var10) {
      float f = (float)(maxX - minX + 1);
      float f1 = (float)(maxY - minY + 1);
      float f2 = (float)(maxZ - minZ + 1);
      float f3 = (float)minX + f / 2.0F;
      float f4 = (float)minZ + f2 / 2.0F;

      for(int i = minY; i <= maxY; ++i) {
         float f5 = (float)(i - minY) / f1;

         for(int j = minX; j <= maxX; ++j) {
            float f6 = ((float)j - f3) / (f * 0.5F);

            for(int k = minZ; k <= maxZ; ++k) {
               float f7 = ((float)k - f4) / (f2 * 0.5F);
               if (!excludeAir || this.getBlockStateFromPos(worldIn, j, i, k, boundingboxIn).getMaterial() != Material.AIR) {
                  float f8 = f6 * f6 + f5 * f5 + f7 * f7;
                  if (f8 <= 1.05F) {
                     this.setBlockState(worldIn, blockstateIn, j, i, k, boundingboxIn);
                  }
               }
            }
         }
      }

   }

   protected void clearCurrentPositionBlocksUpwards(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
      if (structurebb.isVecInside(blockpos)) {
         while(!worldIn.isAirBlock(blockpos) && blockpos.getY() < 255) {
            worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
            blockpos = blockpos.up();
         }
      }

   }

   protected void replaceAirAndLiquidDownwards(World var1, IBlockState var2, int var3, int var4, int var5, StructureBoundingBox var6) {
      int i = this.getXWithOffset(x, z);
      int j = this.getYWithOffset(y);
      int k = this.getZWithOffset(x, z);
      if (boundingboxIn.isVecInside(new BlockPos(i, j, k))) {
         while((worldIn.isAirBlock(new BlockPos(i, j, k)) || worldIn.getBlockState(new BlockPos(i, j, k)).getMaterial().isLiquid()) && j > 1) {
            worldIn.setBlockState(new BlockPos(i, j, k), blockstateIn, 2);
            --j;
         }
      }

   }

   protected boolean generateChest(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, ResourceLocation var7) {
      BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
      if (structurebb.isVecInside(blockpos) && worldIn.getBlockState(blockpos).getBlock() != Blocks.CHEST) {
         IBlockState iblockstate = Blocks.CHEST.getDefaultState();
         worldIn.setBlockState(blockpos, Blocks.CHEST.correctFacing(worldIn, blockpos, iblockstate), 2);
         TileEntity tileentity = worldIn.getTileEntity(blockpos);
         if (tileentity instanceof TileEntityChest) {
            ((TileEntityChest)tileentity).setLootTable(loot, randomIn.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, EnumFacing var7, ResourceLocation var8) {
      BlockPos blockpos = new BlockPos(this.getXWithOffset(p_189419_4_, p_189419_6_), this.getYWithOffset(p_189419_5_), this.getZWithOffset(p_189419_4_, p_189419_6_));
      if (p_189419_2_.isVecInside(blockpos) && p_189419_1_.getBlockState(blockpos).getBlock() != Blocks.DISPENSER) {
         this.setBlockState(p_189419_1_, Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, p_189419_7_), p_189419_4_, p_189419_5_, p_189419_6_, p_189419_2_);
         TileEntity tileentity = p_189419_1_.getTileEntity(blockpos);
         if (tileentity instanceof TileEntityDispenser) {
            ((TileEntityDispenser)tileentity).setLootTable(p_189419_8_, p_189419_3_.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected void func_189915_a(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, EnumFacing var7, BlockDoor var8) {
      this.setBlockState(p_189915_1_, p_189915_8_.getDefaultState().withProperty(BlockDoor.FACING, p_189915_7_), p_189915_4_, p_189915_5_, p_189915_6_, p_189915_2_);
      this.setBlockState(p_189915_1_, p_189915_8_.getDefaultState().withProperty(BlockDoor.FACING, p_189915_7_).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), p_189915_4_, p_189915_5_ + 1, p_189915_6_, p_189915_2_);
   }

   public void offset(int var1, int var2, int var3) {
      this.boundingBox.offset(x, y, z);
   }

   @Nullable
   public EnumFacing getCoordBaseMode() {
      return this.coordBaseMode;
   }

   public void setCoordBaseMode(@Nullable EnumFacing var1) {
      this.coordBaseMode = facing;
      if (facing == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch(facing) {
         case SOUTH:
            this.mirror = Mirror.LEFT_RIGHT;
            this.rotation = Rotation.NONE;
            break;
         case WEST:
            this.mirror = Mirror.LEFT_RIGHT;
            this.rotation = Rotation.CLOCKWISE_90;
            break;
         case EAST:
            this.mirror = Mirror.NONE;
            this.rotation = Rotation.CLOCKWISE_90;
            break;
         default:
            this.mirror = Mirror.NONE;
            this.rotation = Rotation.NONE;
         }
      }

   }

   public abstract static class BlockSelector {
      protected IBlockState blockstate = Blocks.AIR.getDefaultState();

      public abstract void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5);

      public IBlockState getBlockState() {
         return this.blockstate;
      }
   }
}
