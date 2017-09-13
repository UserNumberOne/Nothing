package net.minecraft.block;

import java.util.List;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDaylightDetector extends BlockContainer {
   public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
   protected static final AxisAlignedBB DAYLIGHT_DETECTOR_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D);
   private final boolean inverted;

   public BlockDaylightDetector(boolean var1) {
      super(Material.WOOD);
      this.inverted = inverted;
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, Integer.valueOf(0)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.setHardness(0.2F);
      this.setSoundType(SoundType.WOOD);
      this.setUnlocalizedName("daylightDetector");
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return DAYLIGHT_DETECTOR_AABB;
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Integer)blockState.getValue(POWER)).intValue();
   }

   public void updatePower(World var1, BlockPos var2) {
      if (!worldIn.provider.hasNoSky()) {
         IBlockState iblockstate = worldIn.getBlockState(pos);
         int i = worldIn.getLightFor(EnumSkyBlock.SKY, pos) - worldIn.getSkylightSubtracted();
         float f = worldIn.getCelestialAngleRadians(1.0F);
         if (this.inverted) {
            i = 15 - i;
         }

         if (i > 0 && !this.inverted) {
            float f1 = f < 3.1415927F ? 0.0F : 6.2831855F;
            f = f + (f1 - f) * 0.2F;
            i = Math.round((float)i * MathHelper.cos(f));
         }

         i = MathHelper.clamp(i, 0, 15);
         if (((Integer)iblockstate.getValue(POWER)).intValue() != i) {
            worldIn.setBlockState(pos, iblockstate.withProperty(POWER, Integer.valueOf(i)), 3);
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (playerIn.isAllowEdit()) {
         if (worldIn.isRemote) {
            return true;
         } else {
            if (this.inverted) {
               worldIn.setBlockState(pos, Blocks.DAYLIGHT_DETECTOR.getDefaultState().withProperty(POWER, state.getValue(POWER)), 4);
               Blocks.DAYLIGHT_DETECTOR.updatePower(worldIn, pos);
            } else {
               worldIn.setBlockState(pos, Blocks.DAYLIGHT_DETECTOR_INVERTED.getDefaultState().withProperty(POWER, state.getValue(POWER)), 4);
               Blocks.DAYLIGHT_DETECTOR_INVERTED.updatePower(worldIn, pos);
            }

            return true;
         }
      } else {
         return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.DAYLIGHT_DETECTOR);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityDaylightDetector();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(POWER)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWER});
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (!this.inverted) {
         super.getSubBlocks(itemIn, tab, list);
      }

   }
}
