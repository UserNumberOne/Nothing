package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDeadBush extends BlockBush {
   protected static final AxisAlignedBB DEAD_BUSH_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

   protected BlockDeadBush() {
      super(Material.VINE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return DEAD_BUSH_AABB;
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.WOOD;
   }

   protected boolean canSustainBush(IBlockState var1) {
      return var1.getBlock() == Blocks.SAND || var1.getBlock() == Blocks.HARDENED_CLAY || var1.getBlock() == Blocks.STAINED_HARDENED_CLAY || var1.getBlock() == Blocks.DIRT;
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public int quantityDropped(Random var1) {
      return var1.nextInt(3);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.STICK;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (!var1.isRemote && var6 != null && var6.getItem() == Items.SHEARS) {
         var2.addStat(StatList.getBlockStats(this));
         spawnAsEntity(var1, var3, new ItemStack(Blocks.DEADBUSH, 1, 0));
      } else {
         super.harvestBlock(var1, var2, var3, var4, var5, var6);
      }

   }
}
