package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMobSpawner extends BlockContainer {
   protected BlockMobSpawner() {
      super(Material.ROCK);
   }

   public TileEntity createNewTileEntity(World world, int i) {
      return new TileEntityMobSpawner();
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      super.dropBlockAsItemWithChance(world, blockposition, iblockdata, f, i);
   }

   public int getExpDrop(World world, IBlockState iblockdata, int enchantmentLevel) {
      int j = 15 + world.rand.nextInt(15) + world.rand.nextInt(15);
      return j;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState iblockdata) {
      return EnumBlockRenderType.MODEL;
   }

   @Nullable
   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return null;
   }
}
