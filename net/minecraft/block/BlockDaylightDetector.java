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
      this.inverted = var1;
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
      return ((Integer)var1.getValue(POWER)).intValue();
   }

   public void updatePower(World var1, BlockPos var2) {
      if (!var1.provider.hasNoSky()) {
         IBlockState var3 = var1.getBlockState(var2);
         int var4 = var1.getLightFor(EnumSkyBlock.SKY, var2) - var1.getSkylightSubtracted();
         float var5 = var1.getCelestialAngleRadians(1.0F);
         if (this.inverted) {
            var4 = 15 - var4;
         }

         if (var4 > 0 && !this.inverted) {
            float var6 = var5 < 3.1415927F ? 0.0F : 6.2831855F;
            var5 = var5 + (var6 - var5) * 0.2F;
            var4 = Math.round((float)var4 * MathHelper.cos(var5));
         }

         var4 = MathHelper.clamp(var4, 0, 15);
         if (((Integer)var3.getValue(POWER)).intValue() != var4) {
            var1.setBlockState(var2, var3.withProperty(POWER, Integer.valueOf(var4)), 3);
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var4.isAllowEdit()) {
         if (var1.isRemote) {
            return true;
         } else {
            if (this.inverted) {
               var1.setBlockState(var2, Blocks.DAYLIGHT_DETECTOR.getDefaultState().withProperty(POWER, var3.getValue(POWER)), 4);
               Blocks.DAYLIGHT_DETECTOR.updatePower(var1, var2);
            } else {
               var1.setBlockState(var2, Blocks.DAYLIGHT_DETECTOR_INVERTED.getDefaultState().withProperty(POWER, var3.getValue(POWER)), 4);
               Blocks.DAYLIGHT_DETECTOR_INVERTED.updatePower(var1, var2);
            }

            return true;
         }
      } else {
         return super.onBlockActivated(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10);
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
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(POWER)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWER});
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      if (!this.inverted) {
         super.getSubBlocks(var1, var2, var3);
      }

   }
}
