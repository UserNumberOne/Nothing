package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryDefaulted;
import net.minecraft.world.World;

public class BlockDispenser extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");
   public static final RegistryDefaulted DISPENSE_BEHAVIOR_REGISTRY = new RegistryDefaulted(new BehaviorDefaultDispenseItem());
   protected Random rand = new Random();
   public static boolean eventFired = false;

   protected BlockDispenser() {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TRIGGERED, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public int tickRate(World world) {
      return 4;
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      super.onBlockAdded(world, blockposition, iblockdata);
      this.setDefaultDirection(world, blockposition, iblockdata);
   }

   private void setDefaultDirection(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!world.isRemote) {
         EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
         boolean flag = world.getBlockState(blockposition.north()).isFullBlock();
         boolean flag1 = world.getBlockState(blockposition.south()).isFullBlock();
         if (enumdirection == EnumFacing.NORTH && flag && !flag1) {
            enumdirection = EnumFacing.SOUTH;
         } else if (enumdirection == EnumFacing.SOUTH && flag1 && !flag) {
            enumdirection = EnumFacing.NORTH;
         } else {
            boolean flag2 = world.getBlockState(blockposition.west()).isFullBlock();
            boolean flag3 = world.getBlockState(blockposition.east()).isFullBlock();
            if (enumdirection == EnumFacing.WEST && flag2 && !flag3) {
               enumdirection = EnumFacing.EAST;
            } else if (enumdirection == EnumFacing.EAST && flag3 && !flag2) {
               enumdirection = EnumFacing.WEST;
            }
         }

         world.setBlockState(blockposition, iblockdata.withProperty(FACING, enumdirection).withProperty(TRIGGERED, Boolean.valueOf(false)), 2);
      }

   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      if (world.isRemote) {
         return true;
      } else {
         TileEntity tileentity = world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityDispenser) {
            entityhuman.displayGUIChest((TileEntityDispenser)tileentity);
            if (tileentity instanceof TileEntityDropper) {
               entityhuman.addStat(StatList.DROPPER_INSPECTED);
            } else {
               entityhuman.addStat(StatList.DISPENSER_INSPECTED);
            }
         }

         return true;
      }
   }

   public void dispense(World world, BlockPos blockposition) {
      BlockSourceImpl sourceblock = new BlockSourceImpl(world, blockposition);
      TileEntityDispenser tileentitydispenser = (TileEntityDispenser)sourceblock.getBlockTileEntity();
      if (tileentitydispenser != null) {
         int i = tileentitydispenser.getDispenseSlot();
         if (i < 0) {
            world.playEvent(1001, blockposition, 0);
         } else {
            ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
            IBehaviorDispenseItem idispensebehavior = this.getBehavior(itemstack);
            if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR) {
               ItemStack itemstack1 = idispensebehavior.dispense(sourceblock, itemstack);
               eventFired = false;
               tileentitydispenser.setInventorySlotContents(i, itemstack1.stackSize <= 0 ? null : itemstack1);
            }
         }
      }

   }

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack itemstack) {
      return (IBehaviorDispenseItem)DISPENSE_BEHAVIOR_REGISTRY.getObject(itemstack == null ? null : itemstack.getItem());
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      boolean flag = world.isBlockPowered(blockposition) || world.isBlockPowered(blockposition.up());
      boolean flag1 = ((Boolean)iblockdata.getValue(TRIGGERED)).booleanValue();
      if (flag && !flag1) {
         world.scheduleUpdate(blockposition, this, this.tickRate(world));
         world.setBlockState(blockposition, iblockdata.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
      } else if (!flag && flag1) {
         world.setBlockState(blockposition, iblockdata.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
      }

   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote) {
         this.dispense(world, blockposition);
      }

   }

   public TileEntity createNewTileEntity(World world, int i) {
      return new TileEntityDispenser();
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(blockposition, entityliving)).withProperty(TRIGGERED, Boolean.valueOf(false));
   }

   public void onBlockPlacedBy(World world, BlockPos blockposition, IBlockState iblockdata, EntityLivingBase entityliving, ItemStack itemstack) {
      world.setBlockState(blockposition, iblockdata.withProperty(FACING, BlockPistonBase.getFacingFromEntity(blockposition, entityliving)), 2);
      if (itemstack.hasDisplayName()) {
         TileEntity tileentity = world.getTileEntity(blockposition);
         if (tileentity instanceof TileEntityDispenser) {
            ((TileEntityDispenser)tileentity).setCustomName(itemstack.getDisplayName());
         }
      }

   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      TileEntity tileentity = world.getTileEntity(blockposition);
      if (tileentity instanceof TileEntityDispenser) {
         InventoryHelper.dropInventoryItems(world, blockposition, (TileEntityDispenser)tileentity);
         world.updateComparatorOutputLevel(blockposition, this);
      }

      super.breakBlock(world, blockposition, iblockdata);
   }

   public static IPosition getDispensePosition(IBlockSource isourceblock) {
      EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(FACING);
      double d0 = isourceblock.getX() + 0.7D * (double)enumdirection.getFrontOffsetX();
      double d1 = isourceblock.getY() + 0.7D * (double)enumdirection.getFrontOffsetY();
      double d2 = isourceblock.getZ() + 0.7D * (double)enumdirection.getFrontOffsetZ();
      return new PositionImpl(d0, d1, d2);
   }

   public boolean hasComparatorInputOverride(IBlockState iblockdata) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState iblockdata, World world, BlockPos blockposition) {
      return Container.calcRedstone(world.getTileEntity(blockposition));
   }

   public EnumBlockRenderType getRenderType(IBlockState iblockdata) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(i & 7)).withProperty(TRIGGERED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((EnumFacing)iblockdata.getValue(FACING)).getIndex();
      if (((Boolean)iblockdata.getValue(TRIGGERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TRIGGERED});
   }
}
