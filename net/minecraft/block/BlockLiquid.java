package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockLiquid extends Block {
   public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

   protected BlockLiquid(Material var1) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FULL_BLOCK_AABB;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return this.blockMaterial != Material.LAVA;
   }

   public static float getLiquidHeightPercent(int var0) {
      if (var0 >= 8) {
         var0 = 0;
      }

      return (float)(var0 + 1) / 9.0F;
   }

   protected int getDepth(IBlockState var1) {
      return var1.getMaterial() == this.blockMaterial ? ((Integer)var1.getValue(LEVEL)).intValue() : -1;
   }

   protected int getRenderedDepth(IBlockState var1) {
      int var2 = this.getDepth(var1);
      return var2 >= 8 ? 0 : var2;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canCollideCheck(IBlockState var1, boolean var2) {
      return var2 && ((Integer)var1.getValue(LEVEL)).intValue() == 0;
   }

   public boolean isBlockSolid(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      Material var4 = var1.getBlockState(var2).getMaterial();
      if (var4 == this.blockMaterial) {
         return false;
      } else if (var3 == EnumFacing.UP) {
         return true;
      } else {
         return var4 == Material.ICE ? false : super.isBlockSolid(var1, var2, var3);
      }
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.LIQUID;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   protected Vec3d getFlow(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      double var4 = 0.0D;
      double var6 = 0.0D;
      double var8 = 0.0D;
      int var10 = this.getRenderedDepth(var3);
      BlockPos.PooledMutableBlockPos var11 = BlockPos.PooledMutableBlockPos.retain();

      for(EnumFacing var13 : EnumFacing.Plane.HORIZONTAL) {
         var11.setPos(var2).move(var13);
         int var14 = this.getRenderedDepth(var1.getBlockState(var11));
         if (var14 < 0) {
            if (!var1.getBlockState(var11).getMaterial().blocksMovement()) {
               var14 = this.getRenderedDepth(var1.getBlockState(var11.down()));
               if (var14 >= 0) {
                  int var15 = var14 - (var10 - 8);
                  var4 += (double)(var13.getFrontOffsetX() * var15);
                  var6 += (double)(var13.getFrontOffsetY() * var15);
                  var8 += (double)(var13.getFrontOffsetZ() * var15);
               }
            }
         } else if (var14 >= 0) {
            int var20 = var14 - var10;
            var4 += (double)(var13.getFrontOffsetX() * var20);
            var6 += (double)(var13.getFrontOffsetY() * var20);
            var8 += (double)(var13.getFrontOffsetZ() * var20);
         }
      }

      Vec3d var16 = new Vec3d(var4, var6, var8);
      if (((Integer)var3.getValue(LEVEL)).intValue() >= 8) {
         for(EnumFacing var19 : EnumFacing.Plane.HORIZONTAL) {
            var11.setPos(var2).move(var19);
            if (this.isBlockSolid(var1, var11, var19) || this.isBlockSolid(var1, var11.up(), var19)) {
               var16 = var16.normalize().addVector(0.0D, -6.0D, 0.0D);
               break;
            }
         }
      }

      var11.release();
      return var16.normalize();
   }

   public Vec3d modifyAcceleration(World var1, BlockPos var2, Entity var3, Vec3d var4) {
      return var4.add(this.getFlow(var1, var2, var1.getBlockState(var2)));
   }

   public int tickRate(World var1) {
      if (this.blockMaterial == Material.WATER) {
         return 5;
      } else if (this.blockMaterial == Material.LAVA) {
         return var1.provider.hasNoSky() ? 10 : 30;
      } else {
         return 0;
      }
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.checkForMixing(var1, var2, var3);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.checkForMixing(var2, var3, var1);
   }

   public boolean checkForMixing(World var1, BlockPos var2, IBlockState var3) {
      if (this.blockMaterial == Material.LAVA) {
         boolean var4 = false;

         for(EnumFacing var8 : EnumFacing.values()) {
            if (var8 != EnumFacing.DOWN && var1.getBlockState(var2.offset(var8)).getMaterial() == Material.WATER) {
               var4 = true;
               break;
            }
         }

         if (var4) {
            Integer var9 = (Integer)var3.getValue(LEVEL);
            if (var9.intValue() == 0) {
               var1.setBlockState(var2, Blocks.OBSIDIAN.getDefaultState());
               this.triggerMixEffects(var1, var2);
               return true;
            }

            if (var9.intValue() <= 4) {
               var1.setBlockState(var2, Blocks.COBBLESTONE.getDefaultState());
               this.triggerMixEffects(var1, var2);
               return true;
            }
         }
      }

      return false;
   }

   protected void triggerMixEffects(World var1, BlockPos var2) {
      double var3 = (double)var2.getX();
      double var5 = (double)var2.getY();
      double var7 = (double)var2.getZ();
      var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (var1.rand.nextFloat() - var1.rand.nextFloat()) * 0.8F);

      for(int var9 = 0; var9 < 8; ++var9) {
         var1.spawnParticle(EnumParticleTypes.SMOKE_LARGE, var3 + Math.random(), var5 + 1.2D, var7 + Math.random(), 0.0D, 0.0D, 0.0D);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(LEVEL, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(LEVEL)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LEVEL});
   }

   public static BlockDynamicLiquid getFlowingBlock(Material var0) {
      if (var0 == Material.WATER) {
         return Blocks.FLOWING_WATER;
      } else if (var0 == Material.LAVA) {
         return Blocks.FLOWING_LAVA;
      } else {
         throw new IllegalArgumentException("Invalid material");
      }
   }

   public static BlockStaticLiquid getStaticBlock(Material var0) {
      if (var0 == Material.WATER) {
         return Blocks.WATER;
      } else if (var0 == Material.LAVA) {
         return Blocks.LAVA;
      } else {
         throw new IllegalArgumentException("Invalid material");
      }
   }
}
