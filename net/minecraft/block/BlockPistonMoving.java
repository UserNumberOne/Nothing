package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPistonMoving extends BlockContainer {
   public static final PropertyDirection FACING = BlockPistonExtension.FACING;
   public static final PropertyEnum TYPE = BlockPistonExtension.TYPE;

   public BlockPistonMoving() {
      super(Material.PISTON);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, BlockPistonExtension.EnumPistonType.DEFAULT));
      this.setHardness(-1.0F);
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return null;
   }

   public static TileEntity createTilePiston(IBlockState var0, EnumFacing var1, boolean var2, boolean var3) {
      return new TileEntityPiston(blockStateIn, facingIn, extendingIn, shouldHeadBeRenderedIn);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityPiston) {
         ((TileEntityPiston)tileentity).clearPistonTileEntity();
      } else {
         super.breakBlock(worldIn, pos, state);
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return false;
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      BlockPos blockpos = pos.offset(((EnumFacing)state.getValue(FACING)).getOpposite());
      IBlockState iblockstate = worldIn.getBlockState(blockpos);
      if (iblockstate.getBlock() instanceof BlockPistonBase && ((Boolean)iblockstate.getValue(BlockPistonBase.EXTENDED)).booleanValue()) {
         worldIn.setBlockToAir(blockpos);
      }

   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!worldIn.isRemote && worldIn.getTileEntity(pos) == null) {
         worldIn.setBlockToAir(pos);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(worldIn, pos, state, 1.0F, fortune);
   }

   public RayTraceResult collisionRayTrace(IBlockState var1, World var2, BlockPos var3, Vec3d var4, Vec3d var5) {
      return null;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote) {
         worldIn.getTileEntity(pos);
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      TileEntityPiston tileentitypiston = this.getTilePistonAt(worldIn, pos);
      return tileentitypiston == null ? null : tileentitypiston.getAABB(worldIn, pos);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      TileEntityPiston tileentitypiston = this.getTilePistonAt(source, pos);
      return tileentitypiston != null ? tileentitypiston.getAABB(source, pos) : FULL_BLOCK_AABB;
   }

   @Nullable
   private TileEntityPiston getTilePistonAt(IBlockAccess var1, BlockPos var2) {
      TileEntity tileentity = iBlockAccessIn.getTileEntity(blockPosIn);
      return tileentity instanceof TileEntityPiston ? (TileEntityPiston)tileentity : null;
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, BlockPistonExtension.getFacing(meta)).withProperty(TYPE, (meta & 8) > 0 ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getIndex();
      if (state.getValue(TYPE) == BlockPistonExtension.EnumPistonType.STICKY) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TYPE});
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      TileEntityPiston tileentitypiston = this.getTilePistonAt(world, pos);
      if (tileentitypiston != null) {
         IBlockState pushed = tileentitypiston.getPistonState();
         return pushed.getBlock().getDrops(world, pos, pushed, fortune);
      } else {
         return new ArrayList();
      }
   }
}
