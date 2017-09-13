package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.LinkedList;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class BlockSponge extends Block {
   public static final PropertyBool WET = PropertyBool.create("wet");

   protected BlockSponge() {
      super(Material.SPONGE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(WET, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public String getLocalizedName() {
      return I18n.translateToLocal(this.getUnlocalizedName() + ".dry.name");
   }

   public int damageDropped(IBlockState var1) {
      return ((Boolean)var1.getValue(WET)).booleanValue() ? 1 : 0;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.tryAbsorb(var1, var2, var3);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.tryAbsorb(var2, var3, var1);
      super.neighborChanged(var1, var2, var3, var4);
   }

   protected void tryAbsorb(World var1, BlockPos var2, IBlockState var3) {
      if (!((Boolean)var3.getValue(WET)).booleanValue() && this.absorb(var1, var2)) {
         var1.setBlockState(var2, var3.withProperty(WET, Boolean.valueOf(true)), 2);
         var1.playEvent(2001, var2, Block.getIdFromBlock(Blocks.WATER));
      }

   }

   private boolean absorb(World var1, BlockPos var2) {
      LinkedList var3 = Lists.newLinkedList();
      ArrayList var4 = Lists.newArrayList();
      var3.add(new Tuple(var2, Integer.valueOf(0)));
      int var5 = 0;

      while(!var3.isEmpty()) {
         Tuple var6 = (Tuple)var3.poll();
         BlockPos var7 = (BlockPos)var6.getFirst();
         int var8 = ((Integer)var6.getSecond()).intValue();

         for(EnumFacing var12 : EnumFacing.values()) {
            BlockPos var13 = var7.offset(var12);
            if (var1.getBlockState(var13).getMaterial() == Material.WATER) {
               var1.setBlockState(var13, Blocks.AIR.getDefaultState(), 2);
               var4.add(var13);
               ++var5;
               if (var8 < 6) {
                  var3.add(new Tuple(var13, var8 + 1));
               }
            }
         }

         if (var5 > 64) {
            break;
         }
      }

      for(BlockPos var15 : var4) {
         var1.notifyNeighborsOfStateChange(var15, Blocks.AIR);
      }

      return var5 > 0;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(WET, Boolean.valueOf((var1 & 1) == 1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)var1.getValue(WET)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{WET});
   }
}
