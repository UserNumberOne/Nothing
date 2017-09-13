package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneRepeater extends BlockRedstoneDiode {
   public static final PropertyBool LOCKED = PropertyBool.create("locked");
   public static final PropertyInteger DELAY = PropertyInteger.create("delay", 1, 4);

   protected BlockRedstoneRepeater(boolean var1) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(DELAY, Integer.valueOf(1)).withProperty(LOCKED, Boolean.valueOf(false)));
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.diode.name");
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return var1.withProperty(LOCKED, Boolean.valueOf(this.isLocked(var2, var3, var1)));
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!var4.capabilities.allowEdit) {
         return false;
      } else {
         var1.setBlockState(var2, var3.cycleProperty(DELAY), 3);
         return true;
      }
   }

   protected int getDelay(IBlockState var1) {
      return ((Integer)var1.getValue(DELAY)).intValue() * 2;
   }

   protected IBlockState getPoweredState(IBlockState var1) {
      Integer var2 = (Integer)var1.getValue(DELAY);
      Boolean var3 = (Boolean)var1.getValue(LOCKED);
      EnumFacing var4 = (EnumFacing)var1.getValue(FACING);
      return Blocks.POWERED_REPEATER.getDefaultState().withProperty(FACING, var4).withProperty(DELAY, var2).withProperty(LOCKED, var3);
   }

   protected IBlockState getUnpoweredState(IBlockState var1) {
      Integer var2 = (Integer)var1.getValue(DELAY);
      Boolean var3 = (Boolean)var1.getValue(LOCKED);
      EnumFacing var4 = (EnumFacing)var1.getValue(FACING);
      return Blocks.UNPOWERED_REPEATER.getDefaultState().withProperty(FACING, var4).withProperty(DELAY, var2).withProperty(LOCKED, var3);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REPEATER;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.REPEATER);
   }

   public boolean isLocked(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return this.getPowerOnSides(var1, var2, var3) > 0;
   }

   protected boolean isAlternateInput(IBlockState var1) {
      return isDiode(var1);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
      this.notifyNeighbors(var1, var2, var3);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1)).withProperty(LOCKED, Boolean.valueOf(false)).withProperty(DELAY, Integer.valueOf(1 + (var1 >> 2)));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      var2 = var2 | ((Integer)var1.getValue(DELAY)).intValue() - 1 << 2;
      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, DELAY, LOCKED});
   }
}
