package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
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
      player.addStat(StatList.getBlockStats(this));
      player.addExhaustion(0.025F);
      if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
         List items = new ArrayList();
         ItemStack itemstack = this.getSilkTouchDrop(state);
         if (itemstack != null) {
            items.add(itemstack);
         }

         ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0F, true, player);

         for(ItemStack is : items) {
            spawnAsEntity(worldIn, pos, is);
         }
      } else {
         if (worldIn.provider.doesWaterVaporize()) {
            worldIn.setBlockToAir(pos);
            return;
         }

         int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
         this.harvesters.set(player);
         this.dropBlockAsItem(worldIn, pos, state, i);
         this.harvesters.set((Object)null);
         Material material = worldIn.getBlockState(pos.down()).getMaterial();
         if (material.blocksMovement() || material.isLiquid()) {
            worldIn.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
         }
      }

   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (worldIn.getLightFor(EnumSkyBlock.BLOCK, pos) > 11 - this.getDefaultState().getLightOpacity()) {
         this.turnIntoWater(worldIn, pos);
      }

   }

   protected void turnIntoWater(World var1, BlockPos var2) {
      if (worldIn.provider.doesWaterVaporize()) {
         worldIn.setBlockToAir(pos);
      } else {
         this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
         worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
         worldIn.notifyBlockOfStateChange(pos, Blocks.WATER);
      }

   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.NORMAL;
   }
}
