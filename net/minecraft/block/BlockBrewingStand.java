package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBrewingStand extends BlockContainer {
   public static final PropertyBool[] HAS_BOTTLE = new PropertyBool[]{PropertyBool.create("has_bottle_0"), PropertyBool.create("has_bottle_1"), PropertyBool.create("has_bottle_2")};
   protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
   protected static final AxisAlignedBB STICK_AABB = new AxisAlignedBB(0.4375D, 0.0D, 0.4375D, 0.5625D, 0.875D, 0.5625D);

   public BlockBrewingStand() {
      super(Material.IRON);
      this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_BOTTLE[0], Boolean.valueOf(false)).withProperty(HAS_BOTTLE[1], Boolean.valueOf(false)).withProperty(HAS_BOTTLE[2], Boolean.valueOf(false)));
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.brewingStand.name");
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityBrewingStand();
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      addCollisionBoxToList(pos, entityBox, collidingBoxes, STICK_AABB);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return BASE_AABB;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (worldIn.isRemote) {
         return true;
      } else {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityBrewingStand) {
            playerIn.displayGUIChest((TileEntityBrewingStand)tileentity);
            playerIn.addStat(StatList.BREWINGSTAND_INTERACTION);
         }

         return true;
      }
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      if (stack.hasDisplayName()) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityBrewingStand) {
            ((TileEntityBrewingStand)tileentity).setName(stack.getDisplayName());
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      double d0 = (double)((float)pos.getX() + 0.4F + rand.nextFloat() * 0.2F);
      double d1 = (double)((float)pos.getY() + 0.7F + rand.nextFloat() * 0.3F);
      double d2 = (double)((float)pos.getZ() + 0.4F + rand.nextFloat() * 0.2F);
      worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityBrewingStand) {
         InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityBrewingStand)tileentity);
      }

      super.breakBlock(worldIn, pos, state);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.BREWING_STAND;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.BREWING_STAND);
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstone(worldIn.getTileEntity(pos));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public IBlockState getStateFromMeta(int var1) {
      IBlockState iblockstate = this.getDefaultState();

      for(int i = 0; i < 3; ++i) {
         iblockstate = iblockstate.withProperty(HAS_BOTTLE[i], Boolean.valueOf((meta & 1 << i) > 0));
      }

      return iblockstate;
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;

      for(int j = 0; j < 3; ++j) {
         if (((Boolean)state.getValue(HAS_BOTTLE[j])).booleanValue()) {
            i |= 1 << j;
         }
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HAS_BOTTLE[0], HAS_BOTTLE[1], HAS_BOTTLE[2]});
   }
}
