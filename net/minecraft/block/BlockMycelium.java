package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMycelium extends Block {
   public static final PropertyBool SNOWY = PropertyBool.create("snowy");

   protected BlockMycelium() {
      super(Material.GRASS, MapColor.PURPLE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(SNOWY, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      Block var4 = var2.getBlockState(var3.up()).getBlock();
      return var1.withProperty(SNOWY, Boolean.valueOf(var4 == Blocks.SNOW || var4 == Blocks.SNOW_LAYER));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         if (var1.getLightFromNeighbors(var2.up()) < 4 && var1.getBlockState(var2.up()).getLightOpacity(var1, var2.up()) > 2) {
            var1.setBlockState(var2, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
         } else if (var1.getLightFromNeighbors(var2.up()) >= 9) {
            for(int var5 = 0; var5 < 4; ++var5) {
               BlockPos var6 = var2.add(var4.nextInt(3) - 1, var4.nextInt(5) - 3, var4.nextInt(3) - 1);
               IBlockState var7 = var1.getBlockState(var6);
               IBlockState var8 = var1.getBlockState(var6.up());
               if (var7.getBlock() == Blocks.DIRT && var7.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && var1.getLightFromNeighbors(var6.up()) >= 4 && var8.getLightOpacity(var1, var6.up()) <= 2) {
                  var1.setBlockState(var6, this.getDefaultState());
               }
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      super.randomDisplayTick(var1, var2, var3, var4);
      if (var4.nextInt(10) == 0) {
         var2.spawnParticle(EnumParticleTypes.TOWN_AURA, (double)((float)var3.getX() + var4.nextFloat()), (double)((float)var3.getY() + 1.1F), (double)((float)var3.getZ() + var4.nextFloat()), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Blocks.DIRT.getItemDropped(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), var2, var3);
   }

   public int getMetaFromState(IBlockState var1) {
      return 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SNOWY});
   }
}
