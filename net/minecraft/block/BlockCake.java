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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return CAKE_AABB[((Integer)var1.getValue(BITES)).intValue()];
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      this.eatCake(var1, var2, var3, var4);
      return true;
   }

   private void eatCake(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (var4.canEat(false)) {
         var4.addStat(StatList.CAKE_SLICES_EATEN);
         int var5 = var4.getFoodStats().foodLevel;
         FoodLevelChangeEvent var6 = CraftEventFactory.callFoodLevelChangeEvent(var4, 2 + var5);
         if (!var6.isCancelled()) {
            var4.getFoodStats().addStats(var6.getFoodLevel() - var5, 0.1F);
         }

         ((EntityPlayerMP)var4).getBukkitEntity().sendHealthUpdate();
         int var7 = ((Integer)var3.getValue(BITES)).intValue();
         if (var7 < 6) {
            var1.setBlockState(var2, var3.withProperty(BITES, Integer.valueOf(var7 + 1)), 3);
         } else {
            var1.setBlockToAir(var2);
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) ? this.canBlockStay(var1, var2) : false;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.canBlockStay(var2, var3)) {
         var2.setBlockToAir(var3);
      }

   }

   private boolean canBlockStay(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).getMaterial().isSolid();
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.CAKE);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(BITES, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(BITES)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BITES});
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return (7 - ((Integer)var1.getValue(BITES)).intValue()) * 2;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }
}
