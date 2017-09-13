package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class BlockCake extends Block {
   public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 6);
   protected static final AxisAlignedBB[] CAKE_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.1875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.4375D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.5625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.6875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.8125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D)};

   protected BlockCake() {
      super(Material.CAKE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(BITES, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return CAKE_AABB[((Integer)iblockdata.getValue(BITES)).intValue()];
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      this.eatCake(world, blockposition, iblockdata, entityhuman);
      return true;
   }

   private void eatCake(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman) {
      if (entityhuman.canEat(false)) {
         entityhuman.addStat(StatList.CAKE_SLICES_EATEN);
         int oldFoodLevel = entityhuman.getFoodStats().foodLevel;
         FoodLevelChangeEvent event = CraftEventFactory.callFoodLevelChangeEvent(entityhuman, 2 + oldFoodLevel);
         if (!event.isCancelled()) {
            entityhuman.getFoodStats().addStats(event.getFoodLevel() - oldFoodLevel, 0.1F);
         }

         ((EntityPlayerMP)entityhuman).getBukkitEntity().sendHealthUpdate();
         int i = ((Integer)iblockdata.getValue(BITES)).intValue();
         if (i < 6) {
            world.setBlockState(blockposition, iblockdata.withProperty(BITES, Integer.valueOf(i + 1)), 3);
         } else {
            world.setBlockToAir(blockposition);
         }
      }

   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return super.canPlaceBlockAt(world, blockposition) ? this.canBlockStay(world, blockposition) : false;
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!this.canBlockStay(world, blockposition)) {
         world.setBlockToAir(blockposition);
      }

   }

   private boolean canBlockStay(World world, BlockPos blockposition) {
      return world.getBlockState(blockposition.down()).getMaterial().isSolid();
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return null;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Items.CAKE);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(BITES, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(BITES)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BITES});
   }

   public int getComparatorInputOverride(IBlockState iblockdata, World world, BlockPos blockposition) {
      return (7 - ((Integer)iblockdata.getValue(BITES)).intValue()) * 2;
   }

   public boolean hasComparatorInputOverride(IBlockState iblockdata) {
      return true;
   }
}
