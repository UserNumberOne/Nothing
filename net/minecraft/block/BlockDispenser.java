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

   protected BlockDispenser() {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TRIGGERED, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public int tickRate(World var1) {
      return 4;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(worldIn, pos, state);
      this.setDefaultDirection(worldIn, pos, state);
   }

   private void setDefaultDirection(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isRemote) {
         EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
         boolean flag = worldIn.getBlockState(pos.north()).isFullBlock();
         boolean flag1 = worldIn.getBlockState(pos.south()).isFullBlock();
         if (enumfacing == EnumFacing.NORTH && flag && !flag1) {
            enumfacing = EnumFacing.SOUTH;
         } else if (enumfacing == EnumFacing.SOUTH && flag1 && !flag) {
            enumfacing = EnumFacing.NORTH;
         } else {
            boolean flag2 = worldIn.getBlockState(pos.west()).isFullBlock();
            boolean flag3 = worldIn.getBlockState(pos.east()).isFullBlock();
            if (enumfacing == EnumFacing.WEST && flag2 && !flag3) {
               enumfacing = EnumFacing.EAST;
            } else if (enumfacing == EnumFacing.EAST && flag3 && !flag2) {
               enumfacing = EnumFacing.WEST;
            }
         }

         worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing).withProperty(TRIGGERED, Boolean.valueOf(false)), 2);
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (worldIn.isRemote) {
         return true;
      } else {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityDispenser) {
            playerIn.displayGUIChest((TileEntityDispenser)tileentity);
            if (tileentity instanceof TileEntityDropper) {
               playerIn.addStat(StatList.DROPPER_INSPECTED);
            } else {
               playerIn.addStat(StatList.DISPENSER_INSPECTED);
            }
         }

         return true;
      }
   }

   protected void dispense(World var1, BlockPos var2) {
      BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
      TileEntityDispenser tileentitydispenser = (TileEntityDispenser)blocksourceimpl.getBlockTileEntity();
      if (tileentitydispenser != null) {
         int i = tileentitydispenser.getDispenseSlot();
         if (i < 0) {
            worldIn.playEvent(1001, pos, 0);
         } else {
            ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
            IBehaviorDispenseItem ibehaviordispenseitem = this.getBehavior(itemstack);
            if (ibehaviordispenseitem != IBehaviorDispenseItem.DEFAULT_BEHAVIOR) {
               ItemStack itemstack1 = ibehaviordispenseitem.dispense(blocksourceimpl, itemstack);
               tileentitydispenser.setInventorySlotContents(i, itemstack1.stackSize <= 0 ? null : itemstack1);
            }
         }
      }

   }

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack var1) {
      return (IBehaviorDispenseItem)DISPENSE_BEHAVIOR_REGISTRY.getObject(stack == null ? null : stack.getItem());
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
      boolean flag1 = ((Boolean)state.getValue(TRIGGERED)).booleanValue();
      if (flag && !flag1) {
         worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
         worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
      } else if (!flag && flag1) {
         worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote) {
         this.dispense(worldIn, pos);
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityDispenser();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer)).withProperty(TRIGGERED, Boolean.valueOf(false));
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      worldIn.setBlockState(pos, state.withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer)), 2);
      if (stack.hasDisplayName()) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityDispenser) {
            ((TileEntityDispenser)tileentity).setCustomName(stack.getDisplayName());
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityDispenser) {
         InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityDispenser)tileentity);
         worldIn.updateComparatorOutputLevel(pos, this);
      }

      super.breakBlock(worldIn, pos, state);
   }

   public static IPosition getDispensePosition(IBlockSource var0) {
      EnumFacing enumfacing = (EnumFacing)coords.getBlockState().getValue(FACING);
      double d0 = coords.getX() + 0.7D * (double)enumfacing.getFrontOffsetX();
      double d1 = coords.getY() + 0.7D * (double)enumfacing.getFrontOffsetY();
      double d2 = coords.getZ() + 0.7D * (double)enumfacing.getFrontOffsetZ();
      return new PositionImpl(d0, d1, d2);
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstone(worldIn.getTileEntity(pos));
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(TRIGGERED, Boolean.valueOf((meta & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getIndex();
      if (((Boolean)state.getValue(TRIGGERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TRIGGERED});
   }
}
