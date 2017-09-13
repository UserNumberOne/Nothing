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
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!this.canSurvive(var1, var2)) {
         var1.destroyBlock(var2, true);
      } else {
         BlockPos var5 = var2.up();
         if (var1.isAirBlock(var5) && var5.getY() < 256) {
            int var6 = ((Integer)var3.getValue(AGE)).intValue();
            if (var6 < 5 && var4.nextInt(1) == 0) {
               boolean var7 = false;
               boolean var8 = false;
               IBlockState var9 = var1.getBlockState(var2.down());
               Block var10 = var9.getBlock();
               if (var10 == Blocks.END_STONE) {
                  var7 = true;
               } else if (var10 != Blocks.CHORUS_PLANT) {
                  if (var9.getMaterial() == Material.AIR) {
                     var7 = true;
                  }
               } else {
                  int var11 = 1;

                  for(int var12 = 0; var12 < 4; ++var12) {
                     Block var13 = var1.getBlockState(var2.down(var11 + 1)).getBlock();
                     if (var13 != Blocks.CHORUS_PLANT) {
                        if (var13 == Blocks.END_STONE) {
                           var8 = true;
                        }
                        break;
                     }

                     ++var11;
                  }

                  int var18 = 4;
                  if (var8) {
                     ++var18;
                  }

                  if (var11 < 2 || var4.nextInt(var18) >= var11) {
                     var7 = true;
                  }
               }

               if (var7 && areAllNeighborsEmpty(var1, var5, (EnumFacing)null) && var1.isAirBlock(var2.up(2))) {
                  if (CraftEventFactory.handleBlockSpreadEvent(var1.getWorld().getBlockAt(var5.getX(), var5.getY(), var5.getZ()), var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), this, this.getMetaFromState(this.getDefaultState().withProperty(AGE, Integer.valueOf(var6))))) {
                     var1.setBlockState(var2, Blocks.CHORUS_PLANT.getDefaultState(), 2);
                     var1.playEvent(1033, var2, 0);
                  }
               } else if (var6 < 4) {
                  int var17 = var4.nextInt(4);
                  boolean var19 = false;
                  if (var8) {
                     ++var17;
                  }

                  for(int var20 = 0; var20 < var17; ++var20) {
                     EnumFacing var14 = EnumFacing.Plane.HORIZONTAL.random(var4);
                     BlockPos var15 = var2.offset(var14);
                     if (var1.isAirBlock(var15) && var1.isAirBlock(var15.down()) && areAllNeighborsEmpty(var1, var15, var14.getOpposite()) && CraftEventFactory.handleBlockSpreadEvent(var1.getWorld().getBlockAt(var15.getX(), var15.getY(), var15.getZ()), var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), this, this.getMetaFromState(this.getDefaultState().withProperty(AGE, Integer.valueOf(var6 + 1))))) {
                        var1.playEvent(1033, var2, 0);
                        var19 = true;
                     }
                  }

                  if (var19) {
                     var1.setBlockState(var2, Blocks.CHORUS_PLANT.getDefaultState(), 2);
                  } else if (CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var3.withProperty(AGE, Integer.valueOf(5))))) {
                     var1.playEvent(1034, var2, 0);
                  }
               } else if (var6 == 4 && CraftEventFactory.handleBlockGrowEvent(var1, var2.getX(), var2.getY(), var2.getZ(), this, this.getMetaFromState(var3.withProperty(AGE, Integer.valueOf(5))))) {
                  var1.playEvent(1034, var2, 0);
               }
            }
         }
      }

   }

   private void placeGrownFlower(World var1, BlockPos var2, int var3) {
      var1.setBlockState(var2, this.getDefaultState().withProperty(AGE, Integer.valueOf(var3)), 2);
      var1.playEvent(1033, var2, 0);
   }

   private void placeDeadFlower(World var1, BlockPos var2) {
      var1.setBlockState(var2, this.getDefaultState().withProperty(AGE, Integer.valueOf(5)), 2);
      var1.playEvent(1034, var2, 0);
   }

   private static boolean areAllNeighborsEmpty(World var0, BlockPos var1, EnumFacing var2) {
      for(EnumFacing var4 : EnumFacing.Plane.HORIZONTAL) {
         if (var4 != var2 && !var0.isAirBlock(var1.offset(var4))) {
            return false;
         }
      }

      return true;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) && this.canSurvive(var1, var2);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canSurvive(var2, var3)) {
         var2.scheduleUpdate(var3, this, 1);
      }

   }

   public boolean canSurvive(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2.down());
      Block var4 = var3.getBlock();
      if (var4 != Blocks.CHORUS_PLANT && var4 != Blocks.END_STONE) {
         if (var3.getMaterial() == Material.AIR) {
            int var5 = 0;

            for(EnumFacing var7 : EnumFacing.Plane.HORIZONTAL) {
               IBlockState var8 = var1.getBlockState(var2.offset(var7));
               Block var9 = var8.getBlock();
               if (var9 == Blocks.CHORUS_PLANT) {
                  ++var5;
               } else if (var8.getMaterial() != Material.AIR) {
                  return false;
               }
            }

            if (var5 == 1) {
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

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      super.harvestBlock(var1, var2, var3, var4, var5, var6);
      spawnAsEntity(var1, var3, new ItemStack(Item.getItemFromBlock(this)));
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return null;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(var1, var2, var3);
   }

   public static void generatePlant(World var0, BlockPos var1, Random var2, int var3) {
      var0.setBlockState(var1, Blocks.CHORUS_PLANT.getDefaultState(), 2);
      growTreeRecursive(var0, var1, var2, var1, var3, 0);
   }

   private static void growTreeRecursive(World var0, BlockPos var1, Random var2, BlockPos var3, int var4, int var5) {
      int var6 = var2.nextInt(4) + 1;
      if (var5 == 0) {
         ++var6;
      }

      for(int var7 = 0; var7 < var6; ++var7) {
         BlockPos var8 = var1.up(var7 + 1);
         if (!areAllNeighborsEmpty(var0, var8, (EnumFacing)null)) {
            return;
         }

         var0.setBlockState(var8, Blocks.CHORUS_PLANT.getDefaultState(), 2);
      }

      boolean var12 = false;
      if (var5 < 4) {
         int var13 = var2.nextInt(4);
         if (var5 == 0) {
            ++var13;
         }

         for(int var9 = 0; var9 < var13; ++var9) {
            EnumFacing var10 = EnumFacing.Plane.HORIZONTAL.random(var2);
            BlockPos var11 = var1.up(var6).offset(var10);
            if (Math.abs(var11.getX() - var3.getX()) < var4 && Math.abs(var11.getZ() - var3.getZ()) < var4 && var0.isAirBlock(var11) && var0.isAirBlock(var11.down()) && areAllNeighborsEmpty(var0, var11, var10.getOpposite())) {
               var12 = true;
               var0.setBlockState(var11, Blocks.CHORUS_PLANT.getDefaultState(), 2);
               growTreeRecursive(var0, var11, var2, var3, var4, var5 + 1);
            }
         }
      }

      if (!var12) {
         var0.setBlockState(var1.up(var6), Blocks.CHORUS_FLOWER.getDefaultState().withProperty(AGE, Integer.valueOf(5)), 2);
      }

   }
}
