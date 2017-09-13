package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;

public class BlockChest extends BlockContainer {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   protected static final AxisAlignedBB NORTH_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0D, 0.9375D, 0.875D, 0.9375D);
   protected static final AxisAlignedBB SOUTH_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 1.0D);
   protected static final AxisAlignedBB WEST_CHEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);
   protected static final AxisAlignedBB EAST_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 1.0D, 0.875D, 0.9375D);
   protected static final AxisAlignedBB NOT_CONNECTED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);
   public final BlockChest.Type chestType;

   protected BlockChest(BlockChest.Type var1) {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.chestType = var1;
      this.setCreativeTab(var1 == BlockChest.Type.TRAP ? CreativeTabs.REDSTONE : CreativeTabs.DECORATIONS);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (var2.getBlockState(var3.north()).getBlock() == this) {
         return NORTH_CHEST_AABB;
      } else if (var2.getBlockState(var3.south()).getBlock() == this) {
         return SOUTH_CHEST_AABB;
      } else if (var2.getBlockState(var3.west()).getBlock() == this) {
         return WEST_CHEST_AABB;
      } else {
         return var2.getBlockState(var3.east()).getBlock() == this ? EAST_CHEST_AABB : NOT_CONNECTED_AABB;
      }
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.checkForSurroundingChests(var1, var2, var3);

      for(EnumFacing var5 : EnumFacing.Plane.HORIZONTAL) {
         BlockPos var6 = var2.offset(var5);
         IBlockState var7 = var1.getBlockState(var6);
         if (var7.getBlock() == this) {
            this.checkForSurroundingChests(var1, var6, var7);
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing());
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      EnumFacing var6 = EnumFacing.getHorizontal(MathHelper.floor((double)(var4.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
      var3 = var3.withProperty(FACING, var6);
      BlockPos var7 = var2.north();
      BlockPos var8 = var2.south();
      BlockPos var9 = var2.west();
      BlockPos var10 = var2.east();
      boolean var11 = this == var1.getBlockState(var7).getBlock();
      boolean var12 = this == var1.getBlockState(var8).getBlock();
      boolean var13 = this == var1.getBlockState(var9).getBlock();
      boolean var14 = this == var1.getBlockState(var10).getBlock();
      if (!var11 && !var12 && !var13 && !var14) {
         var1.setBlockState(var2, var3, 3);
      } else if (var6.getAxis() != EnumFacing.Axis.X || !var11 && !var12) {
         if (var6.getAxis() == EnumFacing.Axis.Z && (var13 || var14)) {
            if (var13) {
               var1.setBlockState(var9, var3, 3);
            } else {
               var1.setBlockState(var10, var3, 3);
            }

            var1.setBlockState(var2, var3, 3);
         }
      } else {
         if (var11) {
            var1.setBlockState(var7, var3, 3);
         } else {
            var1.setBlockState(var8, var3, 3);
         }

         var1.setBlockState(var2, var3, 3);
      }

      if (var5.hasDisplayName()) {
         TileEntity var15 = var1.getTileEntity(var2);
         if (var15 instanceof TileEntityChest) {
            ((TileEntityChest)var15).setCustomName(var5.getDisplayName());
         }
      }

   }

   public IBlockState checkForSurroundingChests(World var1, BlockPos var2, IBlockState var3) {
      if (var1.isRemote) {
         return var3;
      } else {
         IBlockState var4 = var1.getBlockState(var2.north());
         IBlockState var5 = var1.getBlockState(var2.south());
         IBlockState var6 = var1.getBlockState(var2.west());
         IBlockState var7 = var1.getBlockState(var2.east());
         EnumFacing var8 = (EnumFacing)var3.getValue(FACING);
         if (var4.getBlock() != this && var5.getBlock() != this) {
            boolean var16 = var4.isFullBlock();
            boolean var17 = var5.isFullBlock();
            if (var6.getBlock() == this || var7.getBlock() == this) {
               BlockPos var18 = var6.getBlock() == this ? var2.west() : var2.east();
               IBlockState var19 = var1.getBlockState(var18.north());
               IBlockState var13 = var1.getBlockState(var18.south());
               var8 = EnumFacing.SOUTH;
               EnumFacing var14;
               if (var6.getBlock() == this) {
                  var14 = (EnumFacing)var6.getValue(FACING);
               } else {
                  var14 = (EnumFacing)var7.getValue(FACING);
               }

               if (var14 == EnumFacing.NORTH) {
                  var8 = EnumFacing.NORTH;
               }

               if ((var16 || var19.isFullBlock()) && !var17 && !var13.isFullBlock()) {
                  var8 = EnumFacing.SOUTH;
               }

               if ((var17 || var13.isFullBlock()) && !var16 && !var19.isFullBlock()) {
                  var8 = EnumFacing.NORTH;
               }
            }
         } else {
            BlockPos var9 = var4.getBlock() == this ? var2.north() : var2.south();
            IBlockState var10 = var1.getBlockState(var9.west());
            IBlockState var11 = var1.getBlockState(var9.east());
            var8 = EnumFacing.EAST;
            EnumFacing var12;
            if (var4.getBlock() == this) {
               var12 = (EnumFacing)var4.getValue(FACING);
            } else {
               var12 = (EnumFacing)var5.getValue(FACING);
            }

            if (var12 == EnumFacing.WEST) {
               var8 = EnumFacing.WEST;
            }

            if ((var6.isFullBlock() || var10.isFullBlock()) && !var7.isFullBlock() && !var11.isFullBlock()) {
               var8 = EnumFacing.EAST;
            }

            if ((var7.isFullBlock() || var11.isFullBlock()) && !var6.isFullBlock() && !var10.isFullBlock()) {
               var8 = EnumFacing.WEST;
            }
         }

         var3 = var3.withProperty(FACING, var8);
         var1.setBlockState(var2, var3, 3);
         return var3;
      }
   }

   public IBlockState correctFacing(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = null;

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         IBlockState var7 = var1.getBlockState(var2.offset(var6));
         if (var7.getBlock() == this) {
            return var3;
         }

         if (var7.isFullBlock()) {
            if (var4 != null) {
               var4 = null;
               break;
            }

            var4 = var6;
         }
      }

      if (var4 != null) {
         return var3.withProperty(FACING, var4.getOpposite());
      } else {
         EnumFacing var8 = (EnumFacing)var3.getValue(FACING);
         if (var1.getBlockState(var2.offset(var8)).isFullBlock()) {
            var8 = var8.getOpposite();
         }

         if (var1.getBlockState(var2.offset(var8)).isFullBlock()) {
            var8 = var8.rotateY();
         }

         if (var1.getBlockState(var2.offset(var8)).isFullBlock()) {
            var8 = var8.getOpposite();
         }

         return var3.withProperty(FACING, var8);
      }
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      int var3 = 0;
      BlockPos var4 = var2.west();
      BlockPos var5 = var2.east();
      BlockPos var6 = var2.north();
      BlockPos var7 = var2.south();
      if (var1.getBlockState(var4).getBlock() == this) {
         if (this.isDoubleChest(var1, var4)) {
            return false;
         }

         ++var3;
      }

      if (var1.getBlockState(var5).getBlock() == this) {
         if (this.isDoubleChest(var1, var5)) {
            return false;
         }

         ++var3;
      }

      if (var1.getBlockState(var6).getBlock() == this) {
         if (this.isDoubleChest(var1, var6)) {
            return false;
         }

         ++var3;
      }

      if (var1.getBlockState(var7).getBlock() == this) {
         if (this.isDoubleChest(var1, var7)) {
            return false;
         }

         ++var3;
      }

      return var3 <= 1;
   }

   private boolean isDoubleChest(World var1, BlockPos var2) {
      if (var1.getBlockState(var2).getBlock() != this) {
         return false;
      } else {
         for(EnumFacing var4 : EnumFacing.Plane.HORIZONTAL) {
            if (var1.getBlockState(var2.offset(var4)).getBlock() == this) {
               return true;
            }
         }

         return false;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(var1, var2, var3, var4);
      TileEntity var5 = var2.getTileEntity(var3);
      if (var5 instanceof TileEntityChest) {
         var5.updateContainingBlockInfo();
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity var4 = var1.getTileEntity(var2);
      if (var4 instanceof IInventory) {
         InventoryHelper.dropInventoryItems(var1, var2, (IInventory)var4);
         var1.updateComparatorOutputLevel(var2, this);
      }

      super.breakBlock(var1, var2, var3);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         ILockableContainer var11 = this.getLockableContainer(var1, var2);
         if (var11 != null) {
            var4.displayGUIChest(var11);
            if (this.chestType == BlockChest.Type.BASIC) {
               var4.addStat(StatList.CHEST_OPENED);
            } else if (this.chestType == BlockChest.Type.TRAP) {
               var4.addStat(StatList.TRAPPED_CHEST_TRIGGERED);
            }
         }

         return true;
      }
   }

   @Nullable
   public ILockableContainer getLockableContainer(World var1, BlockPos var2) {
      return this.getContainer(var1, var2, false);
   }

   @Nullable
   public ILockableContainer getContainer(World var1, BlockPos var2, boolean var3) {
      TileEntity var4 = var1.getTileEntity(var2);
      if (!(var4 instanceof TileEntityChest)) {
         return null;
      } else {
         Object var5 = (TileEntityChest)var4;
         if (!var3 && this.isBlocked(var1, var2)) {
            return null;
         } else {
            for(EnumFacing var7 : EnumFacing.Plane.HORIZONTAL) {
               BlockPos var8 = var2.offset(var7);
               Block var9 = var1.getBlockState(var8).getBlock();
               if (var9 == this) {
                  if (this.isBlocked(var1, var8)) {
                     return null;
                  }

                  TileEntity var10 = var1.getTileEntity(var8);
                  if (var10 instanceof TileEntityChest) {
                     if (var7 != EnumFacing.WEST && var7 != EnumFacing.NORTH) {
                        var5 = new InventoryLargeChest("container.chestDouble", (ILockableContainer)var5, (TileEntityChest)var10);
                     } else {
                        var5 = new InventoryLargeChest("container.chestDouble", (TileEntityChest)var10, (ILockableContainer)var5);
                     }
                  }
               }
            }

            return (ILockableContainer)var5;
         }
      }
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityChest();
   }

   public boolean canProvidePower(IBlockState var1) {
      return this.chestType == BlockChest.Type.TRAP;
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (!var1.canProvidePower()) {
         return 0;
      } else {
         int var5 = 0;
         TileEntity var6 = var2.getTileEntity(var3);
         if (var6 instanceof TileEntityChest) {
            var5 = ((TileEntityChest)var6).numPlayersUsing;
         }

         return MathHelper.clamp(var5, 0, 15);
      }
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return var4 == EnumFacing.UP ? var1.getWeakPower(var2, var3, var4) : 0;
   }

   private boolean isBlocked(World var1, BlockPos var2) {
      return this.isBelowSolidBlock(var1, var2) || this.isOcelotSittingOnChest(var1, var2);
   }

   private boolean isBelowSolidBlock(World var1, BlockPos var2) {
      return var1.getBlockState(var2.up()).isNormalCube();
   }

   private boolean isOcelotSittingOnChest(World var1, BlockPos var2) {
      for(Entity var4 : var1.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB((double)var2.getX(), (double)(var2.getY() + 1), (double)var2.getZ(), (double)(var2.getX() + 1), (double)(var2.getY() + 2), (double)(var2.getZ() + 1)))) {
         EntityOcelot var5 = (EntityOcelot)var4;
         if (var5.isSitting()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstoneFromInventory(this.getLockableContainer(var2, var3));
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

   public static enum Type {
      BASIC,
      TRAP;
   }
}
