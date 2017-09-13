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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRedstoneRepeater extends BlockRedstoneDiode {
   public static final PropertyBool LOCKED = PropertyBool.create("locked");
   public static final PropertyInteger DELAY = PropertyInteger.create("delay", 1, 4);

   protected BlockRedstoneRepeater(boolean var1) {
      super(powered);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(DELAY, Integer.valueOf(1)).withProperty(LOCKED, Boolean.valueOf(false)));
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.diode.name");
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.withProperty(LOCKED, Boolean.valueOf(this.isLocked(worldIn, pos, state)));
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!playerIn.capabilities.allowEdit) {
         return false;
      } else {
         worldIn.setBlockState(pos, state.cycleProperty(DELAY), 3);
         return true;
      }
   }

   protected int getDelay(IBlockState var1) {
      return ((Integer)state.getValue(DELAY)).intValue() * 2;
   }

   protected IBlockState getPoweredState(IBlockState var1) {
      Integer integer = (Integer)unpoweredState.getValue(DELAY);
      Boolean obool = (Boolean)unpoweredState.getValue(LOCKED);
      EnumFacing enumfacing = (EnumFacing)unpoweredState.getValue(FACING);
      return Blocks.POWERED_REPEATER.getDefaultState().withProperty(FACING, enumfacing).withProperty(DELAY, integer).withProperty(LOCKED, obool);
   }

   protected IBlockState getUnpoweredState(IBlockState var1) {
      Integer integer = (Integer)poweredState.getValue(DELAY);
      Boolean obool = (Boolean)poweredState.getValue(LOCKED);
      EnumFacing enumfacing = (EnumFacing)poweredState.getValue(FACING);
      return Blocks.UNPOWERED_REPEATER.getDefaultState().withProperty(FACING, enumfacing).withProperty(DELAY, integer).withProperty(LOCKED, obool);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REPEATER;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.REPEATER);
   }

   public boolean isLocked(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return this.getPowerOnSides(worldIn, pos, state) > 0;
   }

   protected boolean isAlternateInput(IBlockState var1) {
      return isDiode(state);
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (this.isRepeaterPowered) {
         EnumFacing enumfacing = (EnumFacing)stateIn.getValue(FACING);
         double d0 = (double)((float)pos.getX() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
         double d1 = (double)((float)pos.getY() + 0.4F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
         double d2 = (double)((float)pos.getZ() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
         float f = -5.0F;
         if (rand.nextBoolean()) {
            f = (float)(((Integer)stateIn.getValue(DELAY)).intValue() * 2 - 1);
         }

         f = f / 16.0F;
         double d3 = (double)(f * (float)enumfacing.getFrontOffsetX());
         double d4 = (double)(f * (float)enumfacing.getFrontOffsetZ());
         worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(worldIn, pos, state);
      this.notifyNeighbors(worldIn, pos, state);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(LOCKED, Boolean.valueOf(false)).withProperty(DELAY, Integer.valueOf(1 + (meta >> 2)));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
      i = i | ((Integer)state.getValue(DELAY)).intValue() - 1 << 2;
      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, DELAY, LOCKED});
   }
}
