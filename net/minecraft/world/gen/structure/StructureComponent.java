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
      this.componentType = var1;
   }

   public final NBTTagCompound createStructureBaseNBT() {
      NBTTagCompound var1 = new NBTTagCompound();
      var1.setString("id", MapGenStructureIO.getStructureComponentName(this));
      var1.setTag("BB", this.boundingBox.toNBTTagIntArray());
      EnumFacing var2 = this.getCoordBaseMode();
      var1.setInteger("O", var2 == null ? -1 : var2.getHorizontalIndex());
      var1.setInteger("GD", this.componentType);
      this.writeStructureToNBT(var1);
      return var1;
   }

   protected abstract void writeStructureToNBT(NBTTagCompound var1);

   public void readStructureBaseNBT(World var1, NBTTagCompound var2) {
      if (var2.hasKey("BB")) {
         this.boundingBox = new StructureBoundingBox(var2.getIntArray("BB"));
      }

      int var3 = var2.getInteger("O");
      this.setCoordBaseMode(var3 == -1 ? null : EnumFacing.getHorizontal(var3));
      this.componentType = var2.getInteger("GD");
      this.readStructureFromNBT(var2);
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
      for(StructureComponent var3 : var0) {
         if (var3.getBoundingBox() != null && var3.getBoundingBox().intersectsWith(var1)) {
            return var3;
         }
      }

      return null;
   }

   public BlockPos getBoundingBoxCenter() {
      return new BlockPos(this.boundingBox.getCenter());
   }

   protected boolean isLiquidInStructureBoundingBox(World var1, StructureBoundingBox var2) {
      int var3 = Math.max(this.boundingBox.minX - 1, var2.minX);
      int var4 = Math.max(this.boundingBox.minY - 1, var2.minY);
      int var5 = Math.max(this.boundingBox.minZ - 1, var2.minZ);
      int var6 = Math.min(this.boundingBox.maxX + 1, var2.maxX);
      int var7 = Math.min(this.boundingBox.maxY + 1, var2.maxY);
      int var8 = Math.min(this.boundingBox.maxZ + 1, var2.maxZ);
      BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

      for(int var10 = var3; var10 <= var6; ++var10) {
         for(int var11 = var5; var11 <= var8; ++var11) {
            if (var1.getBlockState(var9.setPos(var10, var4, var11)).getMaterial().isLiquid()) {
               return true;
            }

            if (var1.getBlockState(var9.setPos(var10, var7, var11)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int var12 = var3; var12 <= var6; ++var12) {
         for(int var14 = var4; var14 <= var7; ++var14) {
            if (var1.getBlockState(var9.setPos(var12, var14, var5)).getMaterial().isLiquid()) {
               return true;
            }

            if (var1.getBlockState(var9.setPos(var12, var14, var8)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int var13 = var5; var13 <= var8; ++var13) {
         for(int var15 = var4; var15 <= var7; ++var15) {
            if (var1.getBlockState(var9.setPos(var3, var15, var13)).getMaterial().isLiquid()) {
               return true;
            }

            if (var1.getBlockState(var9.setPos(var6, var15, var13)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      return false;
   }

   protected int getXWithOffset(int var1, int var2) {
      EnumFacing var3 = this.getCoordBaseMode();
      if (var3 == null) {
         return var1;
      } else {
         switch(var3) {
         case NORTH:
         case SOUTH:
            return this.boundingBox.minX + var1;
         case WEST:
            return this.boundingBox.maxX - var2;
         case EAST:
            return this.boundingBox.minX + var2;
         default:
            return var1;
         }
      }
   }

   protected int getYWithOffset(int var1) {
      return this.getCoordBaseMode() == null ? var1 : var1 + this.boundingBox.minY;
   }

   protected int getZWithOffset(int var1, int var2) {
      EnumFacing var3 = this.getCoordBaseMode();
      if (var3 == null) {
         return var2;
      } else {
         switch(var3) {
         case NORTH:
            return this.boundingBox.maxZ - var2;
         case SOUTH:
            return this.boundingBox.minZ + var2;
         case WEST:
         case EAST:
            return this.boundingBox.minZ + var1;
         default:
            return var2;
         }
      }
   }

   protected void setBlockState(World var1, IBlockState var2, int var3, int var4, int var5, StructureBoundingBox var6) {
      BlockPos var7 = new BlockPos(this.getXWithOffset(var3, var5), this.getYWithOffset(var4), this.getZWithOffset(var3, var5));
      if (var6.isVecInside(var7)) {
         if (this.mirror != Mirror.NONE) {
            var2 = var2.withMirror(this.mirror);
         }

         if (this.rotation != Rotation.NONE) {
            var2 = var2.withRotation(this.rotation);
         }

         var1.setBlockState(var7, var2, 2);
      }
   }

   protected IBlockState getBlockStateFromPos(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      int var6 = this.getXWithOffset(var2, var4);
      int var7 = this.getYWithOffset(var3);
      int var8 = this.getZWithOffset(var2, var4);
      BlockPos var9 = new BlockPos(var6, var7, var8);
      return !var5.isVecInside(var9) ? Blocks.AIR.getDefaultState() : var1.getBlockState(var9);
   }

   protected int func_189916_b(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      int var6 = this.getXWithOffset(var2, var4);
      int var7 = this.getYWithOffset(var3 + 1);
      int var8 = this.getZWithOffset(var2, var4);
      BlockPos var9 = new BlockPos(var6, var7, var8);
      return !var5.isVecInside(var9) ? EnumSkyBlock.SKY.defaultLightValue : var1.getLightFor(EnumSkyBlock.SKY, var9);
   }

   protected void fillWithAir(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      for(int var9 = var4; var9 <= var7; ++var9) {
         for(int var10 = var3; var10 <= var6; ++var10) {
            for(int var11 = var5; var11 <= var8; ++var11) {
               this.setBlockState(var1, Blocks.AIR.getDefaultState(), var10, var9, var11, var2);
            }
         }
      }

   }

   protected void fillWithBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, IBlockState var9, IBlockState var10, boolean var11) {
      for(int var12 = var4; var12 <= var7; ++var12) {
         for(int var13 = var3; var13 <= var6; ++var13) {
            for(int var14 = var5; var14 <= var8; ++var14) {
               if (!var11 || this.getBlockStateFromPos(var1, var13, var12, var14, var2).getMaterial() != Material.AIR) {
                  if (var12 != var4 && var12 != var7 && var13 != var3 && var13 != var6 && var14 != var5 && var14 != var8) {
                     this.setBlockState(var1, var10, var13, var12, var14, var2);
                  } else {
                     this.setBlockState(var1, var9, var13, var12, var14, var2);
                  }
               }
            }
         }
      }

   }

   protected void fillWithRandomizedBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, Random var10, StructureComponent.BlockSelector var11) {
      for(int var12 = var4; var12 <= var7; ++var12) {
         for(int var13 = var3; var13 <= var6; ++var13) {
            for(int var14 = var5; var14 <= var8; ++var14) {
               if (!var9 || this.getBlockStateFromPos(var1, var13, var12, var14, var2).getMaterial() != Material.AIR) {
                  var11.selectBlocks(var10, var13, var12, var14, var12 == var4 || var12 == var7 || var13 == var3 || var13 == var6 || var14 == var5 || var14 == var8);
                  this.setBlockState(var1, var11.getBlockState(), var13, var12, var14, var2);
               }
            }
         }
      }

   }

   protected void func_189914_a(World var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7, int var8, int var9, int var10, IBlockState var11, IBlockState var12, boolean var13, int var14) {
      for(int var15 = var6; var15 <= var9; ++var15) {
         for(int var16 = var5; var16 <= var8; ++var16) {
            for(int var17 = var7; var17 <= var10; ++var17) {
               if (var3.nextFloat() <= var4 && (!var13 || this.getBlockStateFromPos(var1, var16, var15, var17, var2).getMaterial() != Material.AIR) && (var14 <= 0 || this.func_189916_b(var1, var16, var15, var17, var2) < var14)) {
                  if (var15 != var6 && var15 != var9 && var16 != var5 && var16 != var8 && var17 != var7 && var17 != var10) {
                     this.setBlockState(var1, var12, var16, var15, var17, var2);
                  } else {
                     this.setBlockState(var1, var11, var16, var15, var17, var2);
                  }
               }
            }
         }
      }

   }

   protected void randomlyPlaceBlock(World var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7, IBlockState var8) {
      if (var3.nextFloat() < var4) {
         this.setBlockState(var1, var8, var5, var6, var7, var2);
      }

   }

   protected void randomlyRareFillWithBlocks(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, IBlockState var9, boolean var10) {
      float var11 = (float)(var6 - var3 + 1);
      float var12 = (float)(var7 - var4 + 1);
      float var13 = (float)(var8 - var5 + 1);
      float var14 = (float)var3 + var11 / 2.0F;
      float var15 = (float)var5 + var13 / 2.0F;

      for(int var16 = var4; var16 <= var7; ++var16) {
         float var17 = (float)(var16 - var4) / var12;

         for(int var18 = var3; var18 <= var6; ++var18) {
            float var19 = ((float)var18 - var14) / (var11 * 0.5F);

            for(int var20 = var5; var20 <= var8; ++var20) {
               float var21 = ((float)var20 - var15) / (var13 * 0.5F);
               if (!var10 || this.getBlockStateFromPos(var1, var18, var16, var20, var2).getMaterial() != Material.AIR) {
                  float var22 = var19 * var19 + var17 * var17 + var21 * var21;
                  if (var22 <= 1.05F) {
                     this.setBlockState(var1, var9, var18, var16, var20, var2);
                  }
               }
            }
         }
      }

   }

   protected void clearCurrentPositionBlocksUpwards(World var1, int var2, int var3, int var4, StructureBoundingBox var5) {
      BlockPos var6 = new BlockPos(this.getXWithOffset(var2, var4), this.getYWithOffset(var3), this.getZWithOffset(var2, var4));
      if (var5.isVecInside(var6)) {
         while(!var1.isAirBlock(var6) && var6.getY() < 255) {
            var1.setBlockState(var6, Blocks.AIR.getDefaultState(), 2);
            var6 = var6.up();
         }

      }
   }

   protected void replaceAirAndLiquidDownwards(World var1, IBlockState var2, int var3, int var4, int var5, StructureBoundingBox var6) {
      int var7 = this.getXWithOffset(var3, var5);
      int var8 = this.getYWithOffset(var4);
      int var9 = this.getZWithOffset(var3, var5);
      if (var6.isVecInside(new BlockPos(var7, var8, var9))) {
         while((var1.isAirBlock(new BlockPos(var7, var8, var9)) || var1.getBlockState(new BlockPos(var7, var8, var9)).getMaterial().isLiquid()) && var8 > 1) {
            var1.setBlockState(new BlockPos(var7, var8, var9), var2, 2);
            --var8;
         }

      }
   }

   protected boolean generateChest(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, ResourceLocation var7) {
      BlockPos var8 = new BlockPos(this.getXWithOffset(var4, var6), this.getYWithOffset(var5), this.getZWithOffset(var4, var6));
      if (var2.isVecInside(var8) && var1.getBlockState(var8).getBlock() != Blocks.CHEST) {
         IBlockState var9 = Blocks.CHEST.getDefaultState();
         var1.setBlockState(var8, Blocks.CHEST.correctFacing(var1, var8, var9), 2);
         TileEntity var10 = var1.getTileEntity(var8);
         if (var10 instanceof TileEntityChest) {
            ((TileEntityChest)var10).setLootTable(var7, var3.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, EnumFacing var7, ResourceLocation var8) {
      BlockPos var9 = new BlockPos(this.getXWithOffset(var4, var6), this.getYWithOffset(var5), this.getZWithOffset(var4, var6));
      if (var2.isVecInside(var9) && var1.getBlockState(var9).getBlock() != Blocks.DISPENSER) {
         this.setBlockState(var1, Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, var7), var4, var5, var6, var2);
         TileEntity var10 = var1.getTileEntity(var9);
         if (var10 instanceof TileEntityDispenser) {
            ((TileEntityDispenser)var10).setLootTable(var8, var3.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected void func_189915_a(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, EnumFacing var7, BlockDoor var8) {
      this.setBlockState(var1, var8.getDefaultState().withProperty(BlockDoor.FACING, var7), var4, var5, var6, var2);
      this.setBlockState(var1, var8.getDefaultState().withProperty(BlockDoor.FACING, var7).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), var4, var5 + 1, var6, var2);
   }

   public void offset(int var1, int var2, int var3) {
      this.boundingBox.offset(var1, var2, var3);
   }

   @Nullable
   public EnumFacing getCoordBaseMode() {
      return this.coordBaseMode;
   }

   public void setCoordBaseMode(@Nullable EnumFacing var1) {
      this.coordBaseMode = var1;
      if (var1 == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch(var1) {
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
