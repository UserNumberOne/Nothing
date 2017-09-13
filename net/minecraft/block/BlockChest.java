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
      this.chestType = chestTypeIn;
      this.setCreativeTab(chestTypeIn == BlockChest.Type.TRAP ? CreativeTabs.REDSTONE : CreativeTabs.DECORATIONS);
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
      return source.getBlockState(pos.north()).getBlock() == this ? NORTH_CHEST_AABB : (source.getBlockState(pos.south()).getBlock() == this ? SOUTH_CHEST_AABB : (source.getBlockState(pos.west()).getBlock() == this ? WEST_CHEST_AABB : (source.getBlockState(pos.east()).getBlock() == this ? EAST_CHEST_AABB : NOT_CONNECTED_AABB)));
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.checkForSurroundingChests(worldIn, pos, state);

      for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
         BlockPos blockpos = pos.offset(enumfacing);
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate.getBlock() == this) {
            this.checkForSurroundingChests(worldIn, blockpos, iblockstate);
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      EnumFacing enumfacing = EnumFacing.getHorizontal(MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
      state = state.withProperty(FACING, enumfacing);
      BlockPos blockpos = pos.north();
      BlockPos blockpos1 = pos.south();
      BlockPos blockpos2 = pos.west();
      BlockPos blockpos3 = pos.east();
      boolean flag = this == worldIn.getBlockState(blockpos).getBlock();
      boolean flag1 = this == worldIn.getBlockState(blockpos1).getBlock();
      boolean flag2 = this == worldIn.getBlockState(blockpos2).getBlock();
      boolean flag3 = this == worldIn.getBlockState(blockpos3).getBlock();
      if (!flag && !flag1 && !flag2 && !flag3) {
         worldIn.setBlockState(pos, state, 3);
      } else if (enumfacing.getAxis() != EnumFacing.Axis.X || !flag && !flag1) {
         if (enumfacing.getAxis() == EnumFacing.Axis.Z && (flag2 || flag3)) {
            if (flag2) {
               worldIn.setBlockState(blockpos2, state, 3);
            } else {
               worldIn.setBlockState(blockpos3, state, 3);
            }

            worldIn.setBlockState(pos, state, 3);
         }
      } else {
         if (flag) {
            worldIn.setBlockState(blockpos, state, 3);
         } else {
            worldIn.setBlockState(blockpos1, state, 3);
         }

         worldIn.setBlockState(pos, state, 3);
      }

      if (stack.hasDisplayName()) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityChest) {
            ((TileEntityChest)tileentity).setCustomName(stack.getDisplayName());
         }
      }

   }

   public IBlockState checkForSurroundingChests(World var1, BlockPos var2, IBlockState var3) {
      if (worldIn.isRemote) {
         return state;
      } else {
         IBlockState iblockstate = worldIn.getBlockState(pos.north());
         IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
         IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
         IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
         EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
         if (iblockstate.getBlock() != this && iblockstate1.getBlock() != this) {
            boolean flag = iblockstate.isFullBlock();
            boolean flag1 = iblockstate1.isFullBlock();
            if (iblockstate2.getBlock() == this || iblockstate3.getBlock() == this) {
               BlockPos blockpos1 = iblockstate2.getBlock() == this ? pos.west() : pos.east();
               IBlockState iblockstate7 = worldIn.getBlockState(blockpos1.north());
               IBlockState iblockstate6 = worldIn.getBlockState(blockpos1.south());
               enumfacing = EnumFacing.SOUTH;
               EnumFacing enumfacing2;
               if (iblockstate2.getBlock() == this) {
                  enumfacing2 = (EnumFacing)iblockstate2.getValue(FACING);
               } else {
                  enumfacing2 = (EnumFacing)iblockstate3.getValue(FACING);
               }

               if (enumfacing2 == EnumFacing.NORTH) {
                  enumfacing = EnumFacing.NORTH;
               }

               if ((flag || iblockstate7.isFullBlock()) && !flag1 && !iblockstate6.isFullBlock()) {
                  enumfacing = EnumFacing.SOUTH;
               }

               if ((flag1 || iblockstate6.isFullBlock()) && !flag && !iblockstate7.isFullBlock()) {
                  enumfacing = EnumFacing.NORTH;
               }
            }
         } else {
            BlockPos blockpos = iblockstate.getBlock() == this ? pos.north() : pos.south();
            IBlockState iblockstate4 = worldIn.getBlockState(blockpos.west());
            IBlockState iblockstate5 = worldIn.getBlockState(blockpos.east());
            enumfacing = EnumFacing.EAST;
            EnumFacing enumfacing1;
            if (iblockstate.getBlock() == this) {
               enumfacing1 = (EnumFacing)iblockstate.getValue(FACING);
            } else {
               enumfacing1 = (EnumFacing)iblockstate1.getValue(FACING);
            }

            if (enumfacing1 == EnumFacing.WEST) {
               enumfacing = EnumFacing.WEST;
            }

            if ((iblockstate2.isFullBlock() || iblockstate4.isFullBlock()) && !iblockstate3.isFullBlock() && !iblockstate5.isFullBlock()) {
               enumfacing = EnumFacing.EAST;
            }

            if ((iblockstate3.isFullBlock() || iblockstate5.isFullBlock()) && !iblockstate2.isFullBlock() && !iblockstate4.isFullBlock()) {
               enumfacing = EnumFacing.WEST;
            }
         }

         state = state.withProperty(FACING, enumfacing);
         worldIn.setBlockState(pos, state, 3);
         return state;
      }
   }

   public IBlockState correctFacing(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = null;

      for(EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
         IBlockState iblockstate = worldIn.getBlockState(pos.offset(enumfacing1));
         if (iblockstate.getBlock() == this) {
            return state;
         }

         if (iblockstate.isFullBlock()) {
            if (enumfacing != null) {
               enumfacing = null;
               break;
            }

            enumfacing = enumfacing1;
         }
      }

      if (enumfacing != null) {
         return state.withProperty(FACING, enumfacing.getOpposite());
      } else {
         EnumFacing enumfacing2 = (EnumFacing)state.getValue(FACING);
         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
         }

         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.rotateY();
         }

         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
         }

         return state.withProperty(FACING, enumfacing2);
      }
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      int i = 0;
      BlockPos blockpos = pos.west();
      BlockPos blockpos1 = pos.east();
      BlockPos blockpos2 = pos.north();
      BlockPos blockpos3 = pos.south();
      if (worldIn.getBlockState(blockpos).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos)) {
            return false;
         }

         ++i;
      }

      if (worldIn.getBlockState(blockpos1).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos1)) {
            return false;
         }

         ++i;
      }

      if (worldIn.getBlockState(blockpos2).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos2)) {
            return false;
         }

         ++i;
      }

      if (worldIn.getBlockState(blockpos3).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos3)) {
            return false;
         }

         ++i;
      }

      return i <= 1;
   }

   private boolean isDoubleChest(World var1, BlockPos var2) {
      if (worldIn.getBlockState(pos).getBlock() != this) {
         return false;
      } else {
         for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this) {
               return true;
            }
         }

         return false;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      super.neighborChanged(state, worldIn, pos, blockIn);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityChest) {
         tileentity.updateContainingBlockInfo();
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof IInventory) {
         InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
         worldIn.updateComparatorOutputLevel(pos, this);
      }

      super.breakBlock(worldIn, pos, state);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (worldIn.isRemote) {
         return true;
      } else {
         ILockableContainer ilockablecontainer = this.getLockableContainer(worldIn, pos);
         if (ilockablecontainer != null) {
            playerIn.displayGUIChest(ilockablecontainer);
            if (this.chestType == BlockChest.Type.BASIC) {
               playerIn.addStat(StatList.CHEST_OPENED);
            } else if (this.chestType == BlockChest.Type.TRAP) {
               playerIn.addStat(StatList.TRAPPED_CHEST_TRIGGERED);
            }
         }

         return true;
      }
   }

   @Nullable
   public ILockableContainer getLockableContainer(World var1, BlockPos var2) {
      return this.getContainer(worldIn, pos, false);
   }

   @Nullable
   public ILockableContainer getContainer(World var1, BlockPos var2, boolean var3) {
      TileEntity tileentity = p_189418_1_.getTileEntity(p_189418_2_);
      if (!(tileentity instanceof TileEntityChest)) {
         return null;
      } else {
         ILockableContainer ilockablecontainer = (TileEntityChest)tileentity;
         if (!p_189418_3_ && this.isBlocked(p_189418_1_, p_189418_2_)) {
            return null;
         } else {
            for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
               BlockPos blockpos = p_189418_2_.offset(enumfacing);
               Block block = p_189418_1_.getBlockState(blockpos).getBlock();
               if (block == this) {
                  if (this.isBlocked(p_189418_1_, blockpos)) {
                     return null;
                  }

                  TileEntity tileentity1 = p_189418_1_.getTileEntity(blockpos);
                  if (tileentity1 instanceof TileEntityChest) {
                     if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH) {
                        ilockablecontainer = new InventoryLargeChest("container.chestDouble", ilockablecontainer, (TileEntityChest)tileentity1);
                     } else {
                        ilockablecontainer = new InventoryLargeChest("container.chestDouble", (TileEntityChest)tileentity1, ilockablecontainer);
                     }
                  }
               }
            }

            return ilockablecontainer;
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
      if (!blockState.canProvidePower()) {
         return 0;
      } else {
         int i = 0;
         TileEntity tileentity = blockAccess.getTileEntity(pos);
         if (tileentity instanceof TileEntityChest) {
            i = ((TileEntityChest)tileentity).numPlayersUsing;
         }

         return MathHelper.clamp(i, 0, 15);
      }
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return side == EnumFacing.UP ? blockState.getWeakPower(blockAccess, pos, side) : 0;
   }

   private boolean isBlocked(World var1, BlockPos var2) {
      return this.isBelowSolidBlock(worldIn, pos) || this.isOcelotSittingOnChest(worldIn, pos);
   }

   private boolean isBelowSolidBlock(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos.up()).isSideSolid(worldIn, pos.up(), EnumFacing.DOWN);
   }

   private boolean isOcelotSittingOnChest(World var1, BlockPos var2) {
      for(Entity entity : worldIn.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB((double)pos.getX(), (double)(pos.getY() + 1), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 2), (double)(pos.getZ() + 1)))) {
         EntityOcelot entityocelot = (EntityOcelot)entity;
         if (entityocelot.isSitting()) {
            return true;
         }
      }

      return false;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return Container.calcRedstoneFromInventory(this.getLockableContainer(worldIn, pos));
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing enumfacing = EnumFacing.getFront(meta);
      if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
         enumfacing = EnumFacing.NORTH;
      }

      return this.getDefaultState().withProperty(FACING, enumfacing);
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)state.getValue(FACING)).getIndex();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public static enum Type {
      BASIC,
      TRAP;
   }
}
