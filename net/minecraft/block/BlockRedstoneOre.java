package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRedstoneOre extends Block {
   private final boolean isOn;

   public BlockRedstoneOre(boolean var1) {
      super(Material.ROCK);
      if (isOn) {
         this.setTickRandomly(true);
      }

      this.isOn = isOn;
   }

   public int tickRate(World var1) {
      return 30;
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      this.activate(worldIn, pos);
      super.onBlockClicked(worldIn, pos, playerIn);
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
      this.activate(worldIn, pos);
      super.onEntityWalk(worldIn, pos, entityIn);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.activate(worldIn, pos);
      return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
   }

   private void activate(World var1, BlockPos var2) {
      this.spawnParticles(worldIn, pos);
      if (this == Blocks.REDSTONE_ORE) {
         worldIn.setBlockState(pos, Blocks.LIT_REDSTONE_ORE.getDefaultState());
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this == Blocks.LIT_REDSTONE_ORE) {
         worldIn.setBlockState(pos, Blocks.REDSTONE_ORE.getDefaultState());
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REDSTONE;
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return this.quantityDropped(random) + random.nextInt(fortune + 1);
   }

   public int quantityDropped(Random var1) {
      return 4 + random.nextInt(2);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
   }

   public int getExpDrop(IBlockState var1, IBlockAccess var2, BlockPos var3, int var4) {
      return this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this) ? 1 + RANDOM.nextInt(5) : 0;
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (this.isOn) {
         this.spawnParticles(worldIn, pos);
      }

   }

   private void spawnParticles(World var1, BlockPos var2) {
      Random random = worldIn.rand;
      double d0 = 0.0625D;

      for(int i = 0; i < 6; ++i) {
         double d1 = (double)((float)pos.getX() + random.nextFloat());
         double d2 = (double)((float)pos.getY() + random.nextFloat());
         double d3 = (double)((float)pos.getZ() + random.nextFloat());
         if (i == 0 && !worldIn.getBlockState(pos.up()).isOpaqueCube()) {
            d2 = (double)pos.getY() + 0.0625D + 1.0D;
         }

         if (i == 1 && !worldIn.getBlockState(pos.down()).isOpaqueCube()) {
            d2 = (double)pos.getY() - 0.0625D;
         }

         if (i == 2 && !worldIn.getBlockState(pos.south()).isOpaqueCube()) {
            d3 = (double)pos.getZ() + 0.0625D + 1.0D;
         }

         if (i == 3 && !worldIn.getBlockState(pos.north()).isOpaqueCube()) {
            d3 = (double)pos.getZ() - 0.0625D;
         }

         if (i == 4 && !worldIn.getBlockState(pos.east()).isOpaqueCube()) {
            d1 = (double)pos.getX() + 0.0625D + 1.0D;
         }

         if (i == 5 && !worldIn.getBlockState(pos.west()).isOpaqueCube()) {
            d1 = (double)pos.getX() - 0.0625D;
         }

         if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1)) {
            worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Blocks.REDSTONE_ORE);
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Item.getItemFromBlock(Blocks.REDSTONE_ORE), 1, this.damageDropped(state));
   }
}
