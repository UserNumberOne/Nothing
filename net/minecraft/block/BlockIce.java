package net.minecraft.block;

import java.util.ArrayList;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIce extends BlockBreakable {
   public BlockIce() {
      super(Material.ICE, false);
      this.slipperiness = 0.98F;
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      var2.addStat(StatList.getBlockStats(this));
      var2.addExhaustion(0.025F);
      if (this.canSilkHarvest(var1, var3, var4, var2) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, var6) > 0) {
         ArrayList var11 = new ArrayList();
         ItemStack var12 = this.getSilkTouchDrop(var4);
         if (var12 != null) {
            var11.add(var12);
         }

         ForgeEventFactory.fireBlockHarvesting(var11, var1, var3, var4, 0, 1.0F, true, var2);

         for(ItemStack var10 : var11) {
            spawnAsEntity(var1, var3, var10);
         }
      } else {
         if (var1.provider.doesWaterVaporize()) {
            var1.setBlockToAir(var3);
            return;
         }

         int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, var6);
         this.harvesters.set(var2);
         this.dropBlockAsItem(var1, var3, var4, var7);
         this.harvesters.set((Object)null);
         Material var8 = var1.getBlockState(var3.down()).getMaterial();
         if (var8.blocksMovement() || var8.isLiquid()) {
            var1.setBlockState(var3, Blocks.FLOWING_WATER.getDefaultState());
         }
      }

   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var1.getLightFor(EnumSkyBlock.BLOCK, var2) > 11 - this.getDefaultState().getLightOpacity()) {
         this.turnIntoWater(var1, var2);
      }

   }

   protected void turnIntoWater(World var1, BlockPos var2) {
      if (var1.provider.doesWaterVaporize()) {
         var1.setBlockToAir(var2);
      } else {
         this.dropBlockAsItem(var1, var2, var1.getBlockState(var2), 0);
         var1.setBlockState(var2, Blocks.WATER.getDefaultState());
         var1.notifyBlockOfStateChange(var2, Blocks.WATER);
      }

   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.NORMAL;
   }
}
