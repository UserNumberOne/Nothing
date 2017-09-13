package net.minecraft.block;

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
      return new TileEntityPiston(var0, var1, var2, var3);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      TileEntity var4 = var1.getTileEntity(var2);
      if (var4 instanceof TileEntityPiston) {
         ((TileEntityPiston)var4).clearPistonTileEntity();
      } else {
         super.breakBlock(var1, var2, var3);
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return false;
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      BlockPos var4 = var2.offset(((EnumFacing)var3.getValue(FACING)).getOpposite());
      IBlockState var5 = var1.getBlockState(var4);
      if (var5.getBlock() instanceof BlockPistonBase && ((Boolean)var5.getValue(BlockPistonBase.EXTENDED)).booleanValue()) {
         var1.setBlockToAir(var4);
      }

   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!var1.isRemote && var1.getTileEntity(var2) == null) {
         var1.setBlockToAir(var2);
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
      if (!var1.isRemote) {
         TileEntityPiston var6 = this.getTilePistonAt(var1, var2);
         if (var6 != null) {
            IBlockState var7 = var6.getPistonState();
            var7.getBlock().dropBlockAsItem(var1, var2, var7, 0);
         }
      }
   }

   public RayTraceResult collisionRayTrace(IBlockState var1, World var2, BlockPos var3, Vec3d var4, Vec3d var5) {
      return null;
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         var2.getTileEntity(var3);
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      TileEntityPiston var4 = this.getTilePistonAt(var2, var3);
      return var4 == null ? null : var4.getAABB(var2, var3);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      TileEntityPiston var4 = this.getTilePistonAt(var2, var3);
      return var4 != null ? var4.getAABB(var2, var3) : FULL_BLOCK_AABB;
   }

   @Nullable
   private TileEntityPiston getTilePistonAt(IBlockAccess var1, BlockPos var2) {
      TileEntity var3 = var1.getTileEntity(var2);
      return var3 instanceof TileEntityPiston ? (TileEntityPiston)var3 : null;
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, BlockPistonExtension.getFacing(var1)).withProperty(TYPE, (var1 & 8) > 0 ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getIndex();
      if (var1.getValue(TYPE) == BlockPistonExtension.EnumPistonType.STICKY) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TYPE});
   }
}
