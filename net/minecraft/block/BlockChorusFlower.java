package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockChorusFlower extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 5);

   protected BlockChorusFlower() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setTickRandomly(true);
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!this.canSurvive(world, blockposition)) {
         world.destroyBlock(blockposition, true);
      } else {
         BlockPos blockposition1 = blockposition.up();
         if (world.isAirBlock(blockposition1) && blockposition1.getY() < 256) {
            int i = ((Integer)iblockdata.getValue(AGE)).intValue();
            if (i < 5 && random.nextInt(1) == 0) {
               boolean flag = false;
               boolean flag1 = false;
               IBlockState iblockdata1 = world.getBlockState(blockposition.down());
               Block block = iblockdata1.getBlock();
               if (block == Blocks.END_STONE) {
                  flag = true;
               } else if (block != Blocks.CHORUS_PLANT) {
                  if (iblockdata1.getMaterial() == Material.AIR) {
                     flag = true;
                  }
               } else {
                  int j = 1;

                  for(int k = 0; k < 4; ++k) {
                     Block block1 = world.getBlockState(blockposition.down(j + 1)).getBlock();
                     if (block1 != Blocks.CHORUS_PLANT) {
                        if (block1 == Blocks.END_STONE) {
                           flag1 = true;
                        }
                        break;
                     }

                     ++j;
                  }

                  int var18 = 4;
                  if (flag1) {
                     ++var18;
                  }

                  if (j < 2 || random.nextInt(var18) >= j) {
                     flag = true;
                  }
               }

               if (flag && areAllNeighborsEmpty(world, blockposition1, (EnumFacing)null) && world.isAirBlock(blockposition.up(2))) {
                  if (CraftEventFactory.handleBlockSpreadEvent(world.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), this, this.getMetaFromState(this.getDefaultState().withProperty(AGE, Integer.valueOf(i))))) {
                     world.setBlockState(blockposition, Blocks.CHORUS_PLANT.getDefaultState(), 2);
                     world.playEvent(1033, blockposition, 0);
                  }
               } else if (i < 4) {
                  int j = random.nextInt(4);
                  boolean flag2 = false;
                  if (flag1) {
                     ++j;
                  }

                  for(int l = 0; l < j; ++l) {
                     EnumFacing enumdirection = EnumFacing.Plane.HORIZONTAL.random(random);
                     BlockPos blockposition2 = blockposition.offset(enumdirection);
                     if (world.isAirBlock(blockposition2) && world.isAirBlock(blockposition2.down()) && areAllNeighborsEmpty(world, blockposition2, enumdirection.getOpposite()) && CraftEventFactory.handleBlockSpreadEvent(world.getWorld().getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ()), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), this, this.getMetaFromState(this.getDefaultState().withProperty(AGE, Integer.valueOf(i + 1))))) {
                        world.playEvent(1033, blockposition, 0);
                        flag2 = true;
                     }
                  }

                  if (flag2) {
                     world.setBlockState(blockposition, Blocks.CHORUS_PLANT.getDefaultState(), 2);
                  } else if (CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, this.getMetaFromState(iblockdata.withProperty(AGE, Integer.valueOf(5))))) {
                     world.playEvent(1034, blockposition, 0);
                  }
               } else if (i == 4 && CraftEventFactory.handleBlockGrowEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this, this.getMetaFromState(iblockdata.withProperty(AGE, Integer.valueOf(5))))) {
                  world.playEvent(1034, blockposition, 0);
               }
            }
         }
      }

   }

   private void placeGrownFlower(World world, BlockPos blockposition, int i) {
      world.setBlockState(blockposition, this.getDefaultState().withProperty(AGE, Integer.valueOf(i)), 2);
      world.playEvent(1033, blockposition, 0);
   }

   private void placeDeadFlower(World world, BlockPos blockposition) {
      world.setBlockState(blockposition, this.getDefaultState().withProperty(AGE, Integer.valueOf(5)), 2);
      world.playEvent(1034, blockposition, 0);
   }

   private static boolean areAllNeighborsEmpty(World world, BlockPos blockposition, EnumFacing enumdirection) {
      for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
         if (enumdirection1 != enumdirection && !world.isAirBlock(blockposition.offset(enumdirection1))) {
            return false;
         }
      }

      return true;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return super.canPlaceBlockAt(world, blockposition) && this.canSurvive(world, blockposition);
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!this.canSurvive(world, blockposition)) {
         world.scheduleUpdate(blockposition, this, 1);
      }

   }

   public boolean canSurvive(World world, BlockPos blockposition) {
      IBlockState iblockdata = world.getBlockState(blockposition.down());
      Block block = iblockdata.getBlock();
      if (block != Blocks.CHORUS_PLANT && block != Blocks.END_STONE) {
         if (iblockdata.getMaterial() == Material.AIR) {
            int i = 0;

            for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
               IBlockState iblockdata1 = world.getBlockState(blockposition.offset(enumdirection));
               Block block1 = iblockdata1.getBlock();
               if (block1 == Blocks.CHORUS_PLANT) {
                  ++i;
               } else if (iblockdata1.getMaterial() != Material.AIR) {
                  return false;
               }
            }

            if (i == 1) {
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public void harvestBlock(World world, EntityPlayer entityhuman, BlockPos blockposition, IBlockState iblockdata, @Nullable TileEntity tileentity, @Nullable ItemStack itemstack) {
      super.harvestBlock(world, entityhuman, blockposition, iblockdata, tileentity, itemstack);
      spawnAsEntity(world, blockposition, new ItemStack(Item.getItemFromBlock(this)));
   }

   protected ItemStack getSilkTouchDrop(IBlockState iblockdata) {
      return null;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      super.onBlockAdded(world, blockposition, iblockdata);
   }

   public static void generatePlant(World world, BlockPos blockposition, Random random, int i) {
      world.setBlockState(blockposition, Blocks.CHORUS_PLANT.getDefaultState(), 2);
      growTreeRecursive(world, blockposition, random, blockposition, i, 0);
   }

   private static void growTreeRecursive(World world, BlockPos blockposition, Random random, BlockPos blockposition1, int i, int j) {
      int k = random.nextInt(4) + 1;
      if (j == 0) {
         ++k;
      }

      for(int l = 0; l < k; ++l) {
         BlockPos blockposition2 = blockposition.up(l + 1);
         if (!areAllNeighborsEmpty(world, blockposition2, (EnumFacing)null)) {
            return;
         }

         world.setBlockState(blockposition2, Blocks.CHORUS_PLANT.getDefaultState(), 2);
      }

      boolean flag = false;
      if (j < 4) {
         int i1 = random.nextInt(4);
         if (j == 0) {
            ++i1;
         }

         for(int j1 = 0; j1 < i1; ++j1) {
            EnumFacing enumdirection = EnumFacing.Plane.HORIZONTAL.random(random);
            BlockPos blockposition3 = blockposition.up(k).offset(enumdirection);
            if (Math.abs(blockposition3.getX() - blockposition1.getX()) < i && Math.abs(blockposition3.getZ() - blockposition1.getZ()) < i && world.isAirBlock(blockposition3) && world.isAirBlock(blockposition3.down()) && areAllNeighborsEmpty(world, blockposition3, enumdirection.getOpposite())) {
               flag = true;
               world.setBlockState(blockposition3, Blocks.CHORUS_PLANT.getDefaultState(), 2);
               growTreeRecursive(world, blockposition3, random, blockposition1, i, j + 1);
            }
         }
      }

      if (!flag) {
         world.setBlockState(blockposition.up(k), Blocks.CHORUS_FLOWER.getDefaultState().withProperty(AGE, Integer.valueOf(5)), 2);
      }

   }
}
