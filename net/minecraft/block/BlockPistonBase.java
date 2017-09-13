package net.minecraft.block;

import com.google.common.collect.Lists;
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

public class BlockPistonBase extends BlockDirectional {
   public static final PropertyBool EXTENDED = PropertyBool.create("extended");
   protected static final AxisAlignedBB PISTON_BASE_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_WEST_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
   protected static final AxisAlignedBB PISTON_BASE_NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_UP_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
   protected static final AxisAlignedBB PISTON_BASE_DOWN_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 1.0D, 1.0D);
   private final boolean isSticky;

   public BlockPistonBase(boolean var1) {
      super(Material.PISTON);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = var1;
      this.setSoundType(SoundType.STONE);
      this.setHardness(0.5F);
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (((Boolean)var1.getValue(EXTENDED)).booleanValue()) {
         switch((EnumFacing)var1.getValue(FACING)) {
         case DOWN:
            return PISTON_BASE_DOWN_AABB;
         case UP:
         default:
            return PISTON_BASE_UP_AABB;
         case NORTH:
            return PISTON_BASE_NORTH_AABB;
         case SOUTH:
            return PISTON_BASE_SOUTH_AABB;
         case WEST:
            return PISTON_BASE_WEST_AABB;
         case EAST:
            return PISTON_BASE_EAST_AABB;
         }
      } else {
         return FULL_BLOCK_AABB;
      }
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return !((Boolean)var1.getValue(EXTENDED)).booleanValue() || var1.getValue(FACING) == EnumFacing.DOWN;
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      addCollisionBoxToList(var3, var4, var5, var1.getBoundingBox(var2, var3));
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      var1.setBlockState(var2, var3.withProperty(FACING, getFacingFromEntity(var2, var4)), 2);
      if (!var1.isRemote) {
         this.checkForMove(var1, var2, var3);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         this.checkForMove(var2, var3, var1);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote && var1.getTileEntity(var2) == null) {
         this.checkForMove(var1, var2, var3);
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, getFacingFromEntity(var2, var8)).withProperty(EXTENDED, Boolean.valueOf(false));
   }

   private void checkForMove(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
      boolean var5 = this.shouldBeExtended(var1, var2, var4);
      if (var5 && !((Boolean)var3.getValue(EXTENDED)).booleanValue()) {
         if ((new BlockPistonStructureHelper(var1, var2, var4, true)).canMove()) {
            var1.addBlockEvent(var2, this, 0, var4.getIndex());
         }
      } else if (!var5 && ((Boolean)var3.getValue(EXTENDED)).booleanValue()) {
         var1.addBlockEvent(var2, this, 1, var4.getIndex());
      }

   }

   private boolean shouldBeExtended(World var1, BlockPos var2, EnumFacing var3) {
      for(EnumFacing var7 : EnumFacing.values()) {
         if (var7 != var3 && var1.isSidePowered(var2.offset(var7), var7)) {
            return true;
         }
      }

      if (var1.isSidePowered(var2, EnumFacing.DOWN)) {
         return true;
      } else {
         BlockPos var9 = var2.up();

         for(EnumFacing var8 : EnumFacing.values()) {
            if (var8 != EnumFacing.DOWN && var1.isSidePowered(var9.offset(var8), var8)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      EnumFacing var6 = (EnumFacing)var1.getValue(FACING);
      if (!var2.isRemote) {
         boolean var7 = this.shouldBeExtended(var2, var3, var6);
         if (var7 && var4 == 1) {
            var2.setBlockState(var3, var1.withProperty(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!var7 && var4 == 0) {
            return false;
         }
      }

      if (var4 == 0) {
         if (!this.doMove(var2, var3, var6, true)) {
            return false;
         }

         var2.setBlockState(var3, var1.withProperty(EXTENDED, Boolean.valueOf(true)), 2);
         var2.playSound((EntityPlayer)null, var3, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, var2.rand.nextFloat() * 0.25F + 0.6F);
      } else if (var4 == 1) {
         TileEntity var14 = var2.getTileEntity(var3.offset(var6));
         if (var14 instanceof TileEntityPiston) {
            ((TileEntityPiston)var14).clearPistonTileEntity();
         }

         var2.setBlockState(var3, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, var6).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT), 3);
         var2.setTileEntity(var3, BlockPistonMoving.createTilePiston(this.getStateFromMeta(var5), var6, false, true));
         if (this.isSticky) {
            BlockPos var8 = var3.add(var6.getFrontOffsetX() * 2, var6.getFrontOffsetY() * 2, var6.getFrontOffsetZ() * 2);
            IBlockState var9 = var2.getBlockState(var8);
            Block var10 = var9.getBlock();
            boolean var11 = false;
            if (var10 == Blocks.PISTON_EXTENSION) {
               TileEntity var12 = var2.getTileEntity(var8);
               if (var12 instanceof TileEntityPiston) {
                  TileEntityPiston var13 = (TileEntityPiston)var12;
                  if (var13.getFacing() == var6 && var13.isExtending()) {
                     var13.clearPistonTileEntity();
                     var11 = true;
                  }
               }
            }

            if (!var11 && !var9.getBlock().isAir(var9, var2, var3) && canPush(var9, var2, var8, var6.getOpposite(), false) && (var9.getMobilityFlag() == EnumPushReaction.NORMAL || var10 == Blocks.PISTON || var10 == Blocks.STICKY_PISTON)) {
               this.doMove(var2, var3, var6, false);
            }
         } else {
            var2.setBlockToAir(var3.offset(var6));
         }

         var2.playSound((EntityPlayer)null, var3, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, var2.rand.nextFloat() * 0.15F + 0.6F);
      }

      return true;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   @Nullable
   public static EnumFacing getFacing(int var0) {
      int var1 = var0 & 7;
      return var1 > 5 ? null : EnumFacing.getFront(var1);
   }

   public static EnumFacing getFacingFromEntity(BlockPos var0, EntityLivingBase var1) {
      if (MathHelper.abs((float)var1.posX - (float)var0.getX()) < 2.0F && MathHelper.abs((float)var1.posZ - (float)var0.getZ()) < 2.0F) {
         double var2 = var1.posY + (double)var1.getEyeHeight();
         if (var2 - (double)var0.getY() > 2.0D) {
            return EnumFacing.UP;
         }

         if ((double)var0.getY() - var2 > 0.0D) {
            return EnumFacing.DOWN;
         }
      }

      return var1.getHorizontalFacing().getOpposite();
   }

   public static boolean canPush(IBlockState var0, World var1, BlockPos var2, EnumFacing var3, boolean var4) {
      Block var5 = var0.getBlock();
      if (var5 == Blocks.OBSIDIAN) {
         return false;
      } else if (!var1.getWorldBorder().contains(var2)) {
         return false;
      } else if (var2.getY() >= 0 && (var3 != EnumFacing.DOWN || var2.getY() != 0)) {
         if (var2.getY() <= var1.getHeight() - 1 && (var3 != EnumFacing.UP || var2.getY() != var1.getHeight() - 1)) {
            if (var5 != Blocks.PISTON && var5 != Blocks.STICKY_PISTON) {
               if (var0.getBlockHardness(var1, var2) == -1.0F) {
                  return false;
               }

               if (var0.getMobilityFlag() == EnumPushReaction.BLOCK) {
                  return false;
               }

               if (var0.getMobilityFlag() == EnumPushReaction.DESTROY) {
                  return var4;
               }
            } else if (((Boolean)var0.getValue(EXTENDED)).booleanValue()) {
               return false;
            }

            return !var5.hasTileEntity(var0);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean doMove(World var1, BlockPos var2, EnumFacing var3, boolean var4) {
      if (!var4) {
         var1.setBlockToAir(var2.offset(var3));
      }

      BlockPistonStructureHelper var5 = new BlockPistonStructureHelper(var1, var2, var3, var4);
      if (!var5.canMove()) {
         return false;
      } else {
         List var6 = var5.getBlocksToMove();
         ArrayList var7 = Lists.newArrayList();

         for(int var8 = 0; var8 < var6.size(); ++var8) {
            BlockPos var9 = (BlockPos)var6.get(var8);
            var7.add(var1.getBlockState(var9).getActualState(var1, var9));
         }

         List var16 = var5.getBlocksToDestroy();
         int var17 = var6.size() + var16.size();
         IBlockState[] var10 = new IBlockState[var17];
         EnumFacing var11 = var4 ? var3 : var3.getOpposite();

         for(int var12 = var16.size() - 1; var12 >= 0; --var12) {
            BlockPos var13 = (BlockPos)var16.get(var12);
            IBlockState var14 = var1.getBlockState(var13);
            float var15 = var14.getBlock() instanceof BlockSnow ? -1.0F : 1.0F;
            var14.getBlock().dropBlockAsItemWithChance(var1, var13, var14, var15, 0);
            var1.setBlockToAir(var13);
            --var17;
            var10[var17] = var14;
         }

         for(int var18 = var6.size() - 1; var18 >= 0; --var18) {
            BlockPos var20 = (BlockPos)var6.get(var18);
            IBlockState var25 = var1.getBlockState(var20);
            var1.setBlockState(var20, Blocks.AIR.getDefaultState(), 2);
            var20 = var20.offset(var11);
            var1.setBlockState(var20, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(FACING, var3), 4);
            var1.setTileEntity(var20, BlockPistonMoving.createTilePiston((IBlockState)var7.get(var18), var3, var4, false));
            --var17;
            var10[var17] = var25;
         }

         BlockPos var19 = var2.offset(var3);
         if (var4) {
            BlockPistonExtension.EnumPistonType var22 = this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
            IBlockState var26 = Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.FACING, var3).withProperty(BlockPistonExtension.TYPE, var22);
            IBlockState var27 = Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, var3).withProperty(BlockPistonMoving.TYPE, this.isSticky ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
            var1.setBlockState(var19, var27, 4);
            var1.setTileEntity(var19, BlockPistonMoving.createTilePiston(var26, var3, true, false));
         }

         for(int var23 = var16.size() - 1; var23 >= 0; --var23) {
            var1.notifyNeighborsOfStateChange((BlockPos)var16.get(var23), var10[var17++].getBlock());
         }

         for(int var24 = var6.size() - 1; var24 >= 0; --var24) {
            var1.notifyNeighborsOfStateChange((BlockPos)var6.get(var24), var10[var17++].getBlock());
         }

         if (var4) {
            var1.notifyNeighborsOfStateChange(var19, Blocks.PISTON_HEAD);
            var1.notifyNeighborsOfStateChange(var2, this);
         }

         return true;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, getFacing(var1)).withProperty(EXTENDED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getIndex();
      if (((Boolean)var1.getValue(EXTENDED)).booleanValue()) {
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
      return new BlockStateContainer(this, new IProperty[]{FACING, EXTENDED});
   }
}
