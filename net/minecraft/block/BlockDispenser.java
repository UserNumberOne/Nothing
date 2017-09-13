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
      super.onBlockAdded(var1, var2, var3);
      this.setDefaultDirection(var1, var2, var3);
   }

   private void setDefaultDirection(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
         boolean var5 = var1.getBlockState(var2.north()).isFullBlock();
         boolean var6 = var1.getBlockState(var2.south()).isFullBlock();
         if (var4 == EnumFacing.NORTH && var5 && !var6) {
            var4 = EnumFacing.SOUTH;
         } else if (var4 == EnumFacing.SOUTH && var6 && !var5) {
            var4 = EnumFacing.NORTH;
         } else {
            boolean var7 = var1.getBlockState(var2.west()).isFullBlock();
            boolean var8 = var1.getBlockState(var2.east()).isFullBlock();
            if (var4 == EnumFacing.WEST && var7 && !var8) {
               var4 = EnumFacing.EAST;
            } else if (var4 == EnumFacing.EAST && var8 && !var7) {
               var4 = EnumFacing.WEST;
            }
         }

         var1.setBlockState(var2, var3.withProperty(FACING, var4).withProperty(TRIGGERED, Boolean.valueOf(false)), 2);
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         TileEntity var11 = var1.getTileEntity(var2);
         if (var11 instanceof TileEntityDispenser) {
            var4.displayGUIChest((TileEntityDispenser)var11);
            if (var11 instanceof TileEntityDropper) {
               var4.addStat(StatList.DROPPER_INSPECTED);
            } else {
               var4.addStat(StatList.DISPENSER_INSPECTED);
            }
         }

         return true;
      }
   }

   protected void dispense(World var1, BlockPos var2) {
      BlockSourceImpl var3 = new BlockSourceImpl(var1, var2);
      TileEntityDispenser var4 = (TileEntityDispenser)var3.getBlockTileEntity();
      if (var4 != null) {
         int var5 = var4.getDispenseSlot();
         if (var5 < 0) {
            var1.playEvent(1001, var2, 0);
         } else {
            ItemStack var6 = var4.getStackInSlot(var5);
            IBehaviorDispenseItem var7 = this.getBehavior(var6);
            if (var7 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR) {
               ItemStack var8 = var7.dispense(var3, var6);
               var4.setInventorySlotContents(var5, var8.stackSize <= 0 ? null : var8);
            }
         }
      }

   }

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack var1) {
      return (IBehaviorDispenseItem)DISPENSE_BEHAVIOR_REGISTRY.getObject(var1 == null ? null : var1.getItem());
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      boolean var5 = var2.isBlockPowered(var3) || var2.isBlockPowered(var3.up());
      boolean var6 = ((Boolean)var1.getValue(TRIGGERED)).booleanValue();
      if (var5 && !var6) {
         var2.scheduleUpdate(var3, this, this.tickRate(var2));
         var2.setBlockState(var3, var1.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
      } else if (!var5 && var6) {
         var2.setBlockState(var3, var1.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote) {
         this.dispense(var1, var2);
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityDispenser();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(var2, var8)).withProperty(TRIGGERED, Boolean.valueOf(false));
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      var1.setBlockState(var2, var3.withProperty(FACING, BlockPistonBase.getFacingFromEntity(var2, var4)), 2);
      if (var5.hasDisplayName()) {
         TileEntity var6 = var1.getTileEntity(var2);
         if (var6 instanceof TileEntityDispenser) {
            ((TileEntityDispenser)var6).setCustomName(var5.getDisplayName());
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity var4 = var1.getTileEntity(var2);
      if (var4 instanceof TileEntityDispenser) {
         InventoryHelper.dropInventoryItems(var1, var2, (TileEntityDispenser)var4);
         var1.updateComparatorOutputLevel(var2, this);
      }

      super.breakBlock(var1, var2, var3);
   }

   public static IPosition getDispensePosition(IBlockSource var0) {
      EnumFacing var1 = (EnumFacing)var0.getBlockState().getValue(FACING);
      double var2 = var0.getX() + 0.7D * (double)var1.getFrontOffsetX();
      double var4 = var0.getY() + 0.7D * (double)var1.getFrontOffsetY();
      double var6 = var0.getZ() + 0.7D * (double)var1.getFrontOffsetZ();
      return new PositionImpl(var2, var4, var6);
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstone(var2.getTileEntity(var3));
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(var1 & 7)).withProperty(TRIGGERED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getIndex();
      if (((Boolean)var1.getValue(TRIGGERED)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TRIGGERED});
   }
}
