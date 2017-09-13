package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlock;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonBase extends BlockDirectional {
   public static final PropertyBool EXTENDED = PropertyBool.create("extended");
   protected static final AxisAlignedBB PISTON_BASE_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_WEST_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
   protected static final AxisAlignedBB PISTON_BASE_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_UP_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_DOWN_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 1.0D, 1.0D);
   private final boolean isSticky;

   public BlockPistonBase(boolean flag) {
      super(Material.PISTON);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = flag;
      this.setSoundType(SoundType.STONE);
      this.setHardness(0.5F);
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      if (((Boolean)iblockdata.getValue(EXTENDED)).booleanValue()) {
         switch(BlockPistonBase.SyntheticClass_1.a[((EnumFacing)iblockdata.getValue(FACING)).ordinal()]) {
         case 1:
            return PISTON_BASE_DOWN_AABB;
         case 2:
         default:
            return PISTON_BASE_UP_AABB;
         case 3:
            return PISTON_BASE_NORTH_AABB;
         case 4:
            return PISTON_BASE_SOUTH_AABB;
         case 5:
            return PISTON_BASE_WEST_AABB;
         case 6:
            return PISTON_BASE_EAST_AABB;
         }
      } else {
         return FULL_BLOCK_AABB;
      }
   }

   public boolean isFullyOpaque(IBlockState iblockdata) {
      return !((Boolean)iblockdata.getValue(EXTENDED)).booleanValue() || iblockdata.getValue(FACING) == EnumFacing.DOWN;
   }

   public void addCollisionBoxToList(IBlockState iblockdata, World world, BlockPos blockposition, AxisAlignedBB axisalignedbb, List list, @Nullable Entity entity) {
      addCollisionBoxToList(blockposition, axisalignedbb, list, iblockdata.getBoundingBox(world, blockposition));
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public void onBlockPlacedBy(World world, BlockPos blockposition, IBlockState iblockdata, EntityLivingBase entityliving, ItemStack itemstack) {
      world.setBlockState(blockposition, iblockdata.withProperty(FACING, getFacingFromEntity(blockposition, entityliving)), 2);
      if (!world.isRemote) {
         this.checkForMove(world, blockposition, iblockdata);
      }

   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.isRemote) {
         this.checkForMove(world, blockposition, iblockdata);
      }

   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!world.isRemote && world.getTileEntity(blockposition) == null) {
         this.checkForMove(world, blockposition, iblockdata);
      }

   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return this.getDefaultState().withProperty(FACING, getFacingFromEntity(blockposition, entityliving)).withProperty(EXTENDED, Boolean.valueOf(false));
   }

   private void checkForMove(World world, BlockPos blockposition, IBlockState iblockdata) {
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      boolean flag = this.shouldBeExtended(world, blockposition, enumdirection);
      if (flag && !((Boolean)iblockdata.getValue(EXTENDED)).booleanValue()) {
         if ((new BlockPistonStructureHelper(world, blockposition, enumdirection, true)).canMove()) {
            world.addBlockEvent(blockposition, this, 0, enumdirection.getIndex());
         }
      } else if (!flag && ((Boolean)iblockdata.getValue(EXTENDED)).booleanValue()) {
         if (!this.isSticky) {
            org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            BlockPistonRetractEvent event = new BlockPistonRetractEvent(block, ImmutableList.of(), CraftBlock.notchToBlockFace(enumdirection));
            world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }
         }

         world.addBlockEvent(blockposition, this, 1, enumdirection.getIndex());
      }

   }

   private boolean shouldBeExtended(World world, BlockPos blockposition, EnumFacing enumdirection) {
      for(EnumFacing enumdirection1 : EnumFacing.values()) {
         if (enumdirection1 != enumdirection && world.isSidePowered(blockposition.offset(enumdirection1), enumdirection1)) {
            return true;
         }
      }

      if (world.isSidePowered(blockposition, EnumFacing.DOWN)) {
         return true;
      } else {
         BlockPos blockposition1 = blockposition.up();

         for(EnumFacing enumdirection2 : EnumFacing.values()) {
            if (enumdirection2 != EnumFacing.DOWN && world.isSidePowered(blockposition1.offset(enumdirection2), enumdirection2)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean eventReceived(IBlockState iblockdata, World world, BlockPos blockposition, int i, int j) {
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      if (!world.isRemote) {
         boolean flag = this.shouldBeExtended(world, blockposition, enumdirection);
         if (flag && i == 1) {
            world.setBlockState(blockposition, iblockdata.withProperty(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!flag && i == 0) {
            return false;
         }
      }

      if (i == 0) {
         if (!this.doMove(world, blockposition, enumdirection, true)) {
            return false;
         }

         world.setBlockState(blockposition, iblockdata.withProperty(EXTENDED, Boolean.valueOf(true)), 2);
         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.25F + 0.6F);
      } else if (i == 1) {
         TileEntity tileentity = world.getTileEntity(blockposition.offset(enumdirection));
         if (tileentity instanceof TileEntityPiston) {
            ((TileEntityPiston)tileentity).clearPistonTileEntity();
         }

         world.setBlockState(blockposition, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, enumdirection).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT), 3);
         world.setTileEntity(blockposition, BlockPistonMoving.createTilePiston(this.getStateFromMeta(j), enumdirection, false, true));
         if (this.isSticky) {
            BlockPos blockposition1 = blockposition.add(enumdirection.getFrontOffsetX() * 2, enumdirection.getFrontOffsetY() * 2, enumdirection.getFrontOffsetZ() * 2);
            IBlockState iblockdata1 = world.getBlockState(blockposition1);
            Block block = iblockdata1.getBlock();
            boolean flag1 = false;
            if (block == Blocks.PISTON_EXTENSION) {
               TileEntity tileentity1 = world.getTileEntity(blockposition1);
               if (tileentity1 instanceof TileEntityPiston) {
                  TileEntityPiston tileentitypiston = (TileEntityPiston)tileentity1;
                  if (tileentitypiston.getFacing() == enumdirection && tileentitypiston.isExtending()) {
                     tileentitypiston.clearPistonTileEntity();
                     flag1 = true;
                  }
               }
            }

            if (!flag1 && canPush(iblockdata1, world, blockposition1, enumdirection.getOpposite(), false) && (iblockdata1.getMobilityFlag() == EnumPushReaction.NORMAL || block == Blocks.PISTON || block == Blocks.STICKY_PISTON)) {
               this.doMove(world, blockposition, enumdirection, false);
            }
         } else {
            world.setBlockToAir(blockposition.offset(enumdirection));
         }

         world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
      }

      return true;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   @Nullable
   public static EnumFacing getFacing(int i) {
      int j = i & 7;
      return j > 5 ? null : EnumFacing.getFront(j);
   }

   public static EnumFacing getFacingFromEntity(BlockPos blockposition, EntityLivingBase entityliving) {
      if (MathHelper.abs((float)entityliving.posX - (float)blockposition.getX()) < 2.0F && MathHelper.abs((float)entityliving.posZ - (float)blockposition.getZ()) < 2.0F) {
         double d0 = entityliving.posY + (double)entityliving.getEyeHeight();
         if (d0 - (double)blockposition.getY() > 2.0D) {
            return EnumFacing.UP;
         }

         if ((double)blockposition.getY() - d0 > 0.0D) {
            return EnumFacing.DOWN;
         }
      }

      return entityliving.getHorizontalFacing().getOpposite();
   }

   public static boolean canPush(IBlockState iblockdata, World world, BlockPos blockposition, EnumFacing enumdirection, boolean flag) {
      Block block = iblockdata.getBlock();
      if (block == Blocks.OBSIDIAN) {
         return false;
      } else if (!world.getWorldBorder().contains(blockposition)) {
         return false;
      } else if (blockposition.getY() >= 0 && (enumdirection != EnumFacing.DOWN || blockposition.getY() != 0)) {
         if (blockposition.getY() <= world.getHeight() - 1 && (enumdirection != EnumFacing.UP || blockposition.getY() != world.getHeight() - 1)) {
            if (block != Blocks.PISTON && block != Blocks.STICKY_PISTON) {
               if (iblockdata.getBlockHardness(world, blockposition) == -1.0F) {
                  return false;
               }

               if (iblockdata.getMobilityFlag() == EnumPushReaction.BLOCK) {
                  return false;
               }

               if (iblockdata.getMobilityFlag() == EnumPushReaction.DESTROY) {
                  return flag;
               }
            } else if (((Boolean)iblockdata.getValue(EXTENDED)).booleanValue()) {
               return false;
            }

            return !block.hasTileEntity();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean doMove(World world, BlockPos blockposition, EnumFacing enumdirection, boolean flag) {
      if (!flag) {
         world.setBlockToAir(blockposition.offset(enumdirection));
      }

      BlockPistonStructureHelper pistonextendschecker = new BlockPistonStructureHelper(world, blockposition, enumdirection, flag);
      if (!pistonextendschecker.canMove()) {
         return false;
      } else {
         List list = pistonextendschecker.getBlocksToMove();
         ArrayList arraylist = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockposition1 = (BlockPos)list.get(i);
            arraylist.add(world.getBlockState(blockposition1).getActualState(world, blockposition1));
         }

         List list1 = pistonextendschecker.getBlocksToDestroy();
         int j = list.size() + list1.size();
         IBlockState[] aiblockdata = new IBlockState[j];
         EnumFacing enumdirection1 = flag ? enumdirection : enumdirection.getOpposite();
         final org.bukkit.block.Block bblock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         final List moved = pistonextendschecker.getBlocksToMove();
         final List broken = pistonextendschecker.getBlocksToDestroy();
         List blocks = new AbstractList() {
            public int size() {
               return moved.size() + broken.size();
            }

            public org.bukkit.block.Block get(int index) {
               if (index < this.size() && index >= 0) {
                  BlockPos pos = index < moved.size() ? (BlockPos)moved.get(index) : (BlockPos)broken.get(index - moved.size());
                  return bblock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
               } else {
                  throw new ArrayIndexOutOfBoundsException(index);
               }
            }
         };
         BlockPistonEvent event;
         if (flag) {
            event = new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
         } else {
            event = new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
         }

         world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            for(BlockPos b : broken) {
               world.notifyBlockUpdate(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
            }

            for(BlockPos b : moved) {
               world.notifyBlockUpdate(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
               b = b.offset(enumdirection1);
               world.notifyBlockUpdate(b, Blocks.AIR.getDefaultState(), world.getBlockState(b), 3);
            }

            return false;
         } else {
            for(int k = list1.size() - 1; k >= 0; --k) {
               BlockPos blockposition2 = (BlockPos)list1.get(k);
               IBlockState iblockdata = world.getBlockState(blockposition2);
               iblockdata.getBlock().dropBlockAsItem(world, blockposition2, iblockdata, 0);
               world.setBlockToAir(blockposition2);
               --j;
               aiblockdata[j] = iblockdata;
            }

            for(int var29 = list.size() - 1; var29 >= 0; --var29) {
               BlockPos blockposition2 = (BlockPos)list.get(var29);
               IBlockState iblockdata = world.getBlockState(blockposition2);
               world.setBlockState(blockposition2, Blocks.AIR.getDefaultState(), 2);
               blockposition2 = blockposition2.offset(enumdirection1);
               world.setBlockState(blockposition2, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(FACING, enumdirection), 4);
               world.setTileEntity(blockposition2, BlockPistonMoving.createTilePiston((IBlockState)arraylist.get(var29), enumdirection, flag, false));
               --j;
               aiblockdata[j] = iblockdata;
            }

            BlockPos blockposition3 = blockposition.offset(enumdirection);
            if (flag) {
               BlockPistonExtension.EnumPistonType blockpistonextension_enumpistontype = this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
               IBlockState iblockdata = Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.FACING, enumdirection).withProperty(BlockPistonExtension.TYPE, blockpistonextension_enumpistontype);
               IBlockState iblockdata1 = Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, enumdirection).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
               world.setBlockState(blockposition3, iblockdata1, 4);
               world.setTileEntity(blockposition3, BlockPistonMoving.createTilePiston(iblockdata, enumdirection, true, false));
            }

            for(int l = list1.size() - 1; l >= 0; --l) {
               world.notifyNeighborsOfStateChange((BlockPos)list1.get(l), aiblockdata[j++].getBlock());
            }

            for(int var36 = list.size() - 1; var36 >= 0; --var36) {
               world.notifyNeighborsOfStateChange((BlockPos)list.get(var36), aiblockdata[j++].getBlock());
            }

            if (flag) {
               world.notifyNeighborsOfStateChange(blockposition3, Blocks.PISTON_HEAD);
               world.notifyNeighborsOfStateChange(blockposition, this);
            }

            return true;
         }
      }
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, getFacing(i)).withProperty(EXTENDED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((EnumFacing)iblockdata.getValue(FACING)).getIndex();
      if (((Boolean)iblockdata.getValue(EXTENDED)).booleanValue()) {
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
      return new BlockStateContainer(this, new IProperty[]{FACING, EXTENDED});
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.DOWN.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[EnumFacing.UP.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 6;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
