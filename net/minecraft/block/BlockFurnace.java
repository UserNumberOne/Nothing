package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFurnace extends BlockContainer {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   private final boolean isBurning;
   private static boolean keepInventory;

   protected BlockFurnace(boolean var1) {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.isBurning = var1;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.FURNACE);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.setDefaultFacing(var1, var2, var3);
   }

   private void setDefaultFacing(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         IBlockState var4 = var1.getBlockState(var2.north());
         IBlockState var5 = var1.getBlockState(var2.south());
         IBlockState var6 = var1.getBlockState(var2.west());
         IBlockState var7 = var1.getBlockState(var2.east());
         EnumFacing var8 = (EnumFacing)var3.getValue(FACING);
         if (var8 == EnumFacing.NORTH && var4.isFullBlock() && !var5.isFullBlock()) {
            var8 = EnumFacing.SOUTH;
         } else if (var8 == EnumFacing.SOUTH && var5.isFullBlock() && !var4.isFullBlock()) {
            var8 = EnumFacing.NORTH;
         } else if (var8 == EnumFacing.WEST && var6.isFullBlock() && !var7.isFullBlock()) {
            var8 = EnumFacing.EAST;
         } else if (var8 == EnumFacing.EAST && var7.isFullBlock() && !var6.isFullBlock()) {
            var8 = EnumFacing.WEST;
         }

         var1.setBlockState(var2, var3.withProperty(FACING, var8), 2);
      }

   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (this.isBurning) {
         EnumFacing var5 = (EnumFacing)var1.getValue(FACING);
         double var6 = (double)var3.getX() + 0.5D;
         double var8 = (double)var3.getY() + var4.nextDouble() * 6.0D / 16.0D;
         double var10 = (double)var3.getZ() + 0.5D;
         double var12 = 0.52D;
         double var14 = var4.nextDouble() * 0.6D - 0.3D;
         if (var4.nextDouble() < 0.1D) {
            var2.playSound((double)var3.getX() + 0.5D, (double)var3.getY(), (double)var3.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         switch(var5) {
         case WEST:
            var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6 - 0.52D, var8, var10 + var14, 0.0D, 0.0D, 0.0D);
            var2.spawnParticle(EnumParticleTypes.FLAME, var6 - 0.52D, var8, var10 + var14, 0.0D, 0.0D, 0.0D);
            break;
         case EAST:
            var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6 + 0.52D, var8, var10 + var14, 0.0D, 0.0D, 0.0D);
            var2.spawnParticle(EnumParticleTypes.FLAME, var6 + 0.52D, var8, var10 + var14, 0.0D, 0.0D, 0.0D);
            break;
         case NORTH:
            var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6 + var14, var8, var10 - 0.52D, 0.0D, 0.0D, 0.0D);
            var2.spawnParticle(EnumParticleTypes.FLAME, var6 + var14, var8, var10 - 0.52D, 0.0D, 0.0D, 0.0D);
            break;
         case SOUTH:
            var2.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6 + var14, var8, var10 + 0.52D, 0.0D, 0.0D, 0.0D);
            var2.spawnParticle(EnumParticleTypes.FLAME, var6 + var14, var8, var10 + 0.52D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         TileEntity var11 = var1.getTileEntity(var2);
         if (var11 instanceof TileEntityFurnace) {
            var4.displayGUIChest((TileEntityFurnace)var11);
            var4.addStat(StatList.FURNACE_INTERACTION);
         }

         return true;
      }
   }

   public static void setState(boolean var0, World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      TileEntity var4 = var1.getTileEntity(var2);
      keepInventory = true;
      if (var0) {
         var1.setBlockState(var2, Blocks.LIT_FURNACE.getDefaultState().withProperty(FACING, var3.getValue(FACING)), 3);
         var1.setBlockState(var2, Blocks.LIT_FURNACE.getDefaultState().withProperty(FACING, var3.getValue(FACING)), 3);
      } else {
         var1.setBlockState(var2, Blocks.FURNACE.getDefaultState().withProperty(FACING, var3.getValue(FACING)), 3);
         var1.setBlockState(var2, Blocks.FURNACE.getDefaultState().withProperty(FACING, var3.getValue(FACING)), 3);
      }

      keepInventory = false;
      if (var4 != null) {
         var4.validate();
         var1.setTileEntity(var2, var4);
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityFurnace();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing().getOpposite());
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      var1.setBlockState(var2, var3.withProperty(FACING, var4.getHorizontalFacing().getOpposite()), 2);
      if (var5.hasDisplayName()) {
         TileEntity var6 = var1.getTileEntity(var2);
         if (var6 instanceof TileEntityFurnace) {
            ((TileEntityFurnace)var6).setCustomInventoryName(var5.getDisplayName());
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!keepInventory) {
         TileEntity var4 = var1.getTileEntity(var2);
         if (var4 instanceof TileEntityFurnace) {
            InventoryHelper.dropInventoryItems(var1, var2, (TileEntityFurnace)var4);
            var1.updateComparatorOutputLevel(var2, this);
         }
      }

      super.breakBlock(var1, var2, var3);
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstone(var2.getTileEntity(var3));
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Blocks.FURNACE);
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing var2 = EnumFacing.getFront(var1);
      if (var2.getAxis() == EnumFacing.Axis.Y) {
         var2 = EnumFacing.NORTH;
      }

      return this.getDefaultState().withProperty(FACING, var2);
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)var1.getValue(FACING)).getIndex();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }
}
