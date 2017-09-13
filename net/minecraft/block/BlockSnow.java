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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockSnow extends Block {
   public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 1, 8);
   protected static final AxisAlignedBB[] SNOW_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};

   protected BlockSnow() {
      super(Material.SNOW);
      this.setDefaultState(this.blockState.getBaseState().withProperty(LAYERS, Integer.valueOf(1)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return SNOW_AABB[((Integer)var1.getValue(LAYERS)).intValue()];
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return ((Integer)var1.getBlockState(var2).getValue(LAYERS)).intValue() < 5;
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return ((Integer)var1.getValue(LAYERS)).intValue() == 7;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      int var4 = ((Integer)var1.getValue(LAYERS)).intValue() - 1;
      AxisAlignedBB var5 = var1.getBoundingBox(var2, var3);
      return new AxisAlignedBB(var5.minX, var5.minY, var5.minZ, var5.maxX, (double)((float)var4 * 0.125F), var5.maxZ);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2.down());
      Block var4 = var3.getBlock();
      return var4 != Blocks.ICE && var4 != Blocks.PACKED_ICE ? (var3.getMaterial() == Material.LEAVES ? true : (var4 == this && ((Integer)var3.getValue(LAYERS)).intValue() >= 7 ? true : var3.isOpaqueCube() && var3.getMaterial().blocksMovement())) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      this.checkAndDropBlock(var2, var3, var1);
   }

   private boolean checkAndDropBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canPlaceBlockAt(var1, var2)) {
         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockToAir(var2);
         return false;
      } else {
         return true;
      }
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      spawnAsEntity(var1, var3, new ItemStack(Items.SNOWBALL, ((Integer)var4.getValue(LAYERS)).intValue() + 1, 0));
      var1.setBlockToAir(var3);
      var2.addStat(StatList.getBlockStats(this));
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.SNOWBALL;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var1.getLightFor(EnumSkyBlock.BLOCK, var2) > 11) {
         if (CraftEventFactory.callBlockFadeEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), Blocks.AIR).isCancelled()) {
            return;
         }

         this.dropBlockAsItem(var1, var2, var1.getBlockState(var2), 0);
         var1.setBlockToAir(var2);
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(LAYERS, Integer.valueOf((var1 & 7) + 1));
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return ((Integer)var1.getBlockState(var2).getValue(LAYERS)).intValue() == 1;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(LAYERS)).intValue() - 1;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LAYERS});
   }
}
