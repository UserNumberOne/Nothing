package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWeb extends Block {
   public BlockWeb() {
      super(Material.WEB);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      var4.setInWeb();
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.STRING;
   }

   protected boolean canSilkHarvest() {
      return true;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (!var1.isRemote && var6 != null && var6.getItem() == Items.SHEARS) {
         var2.addStat(StatList.getBlockStats(this));
         spawnAsEntity(var1, var3, new ItemStack(Item.getItemFromBlock(this), 1));
      } else {
         super.harvestBlock(var1, var2, var3, var4, var5, var6);
      }
   }
}
