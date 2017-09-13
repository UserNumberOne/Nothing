package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRailBase extends Block {
   protected static final AxisAlignedBB FLAT_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
   protected final boolean isPowered;

   public static boolean isRailBlock(World var0, BlockPos var1) {
      return isRailBlock(var0.getBlockState(var1));
   }

   public static boolean isRailBlock(IBlockState var0) {
      Block var1 = var0.getBlock();
      return var1 == Blocks.RAIL || var1 == Blocks.GOLDEN_RAIL || var1 == Blocks.DETECTOR_RAIL || var1 == Blocks.ACTIVATOR_RAIL;
   }

   protected BlockRailBase(boolean var1) {
      super(Material.CIRCUITS);
      this.isPowered = var1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      BlockRailBase.EnumRailDirection var4 = var1.getBlock() == this ? (BlockRailBase.EnumRailDirection)var1.getValue(this.getShapeProperty()) : null;
      return var4 != null && var4.isAscending() ? FULL_BLOCK_AABB : FLAT_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque();
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         var3 = this.updateDir(var1, var2, var3, true);
         if (this.isPowered) {
            var3.neighborChanged(var1, var2, this);
         }
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         BlockRailBase.EnumRailDirection var5 = (BlockRailBase.EnumRailDirection)var1.getValue(this.getShapeProperty());
         boolean var6 = false;
         if (!var2.getBlockState(var3.down()).isFullyOpaque()) {
            var6 = true;
         }

         if (var5 == BlockRailBase.EnumRailDirection.ASCENDING_EAST && !var2.getBlockState(var3.east()).isFullyOpaque()) {
            var6 = true;
         } else if (var5 == BlockRailBase.EnumRailDirection.ASCENDING_WEST && !var2.getBlockState(var3.west()).isFullyOpaque()) {
            var6 = true;
         } else if (var5 == BlockRailBase.EnumRailDirection.ASCENDING_NORTH && !var2.getBlockState(var3.north()).isFullyOpaque()) {
            var6 = true;
         } else if (var5 == BlockRailBase.EnumRailDirection.ASCENDING_SOUTH && !var2.getBlockState(var3.south()).isFullyOpaque()) {
            var6 = true;
         }

         if (var6 && !var2.isAirBlock(var3)) {
            this.dropBlockAsItem(var2, var3, var1, 0);
            var2.setBlockToAir(var3);
         } else {
            this.updateState(var1, var2, var3, var4);
         }

      }
   }

   protected void updateState(IBlockState var1, World var2, BlockPos var3, Block var4) {
   }

   protected IBlockState updateDir(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      return var1.isRemote ? var3 : (new BlockRailBase.Rail(var1, var2, var3)).place(var1.isBlockPowered(var2), var4).getBlockState();
   }

   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return EnumPushReaction.NORMAL;
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
      if (((BlockRailBase.EnumRailDirection)var3.getValue(this.getShapeProperty())).isAscending()) {
         var1.notifyNeighborsOfStateChange(var2.up(), this);
      }

      if (this.isPowered) {
         var1.notifyNeighborsOfStateChange(var2, this);
         var1.notifyNeighborsOfStateChange(var2.down(), this);
      }

   }

   public abstract IProperty getShapeProperty();

   public static enum EnumRailDirection implements IStringSerializable {
      NORTH_SOUTH(0, "north_south"),
      EAST_WEST(1, "east_west"),
      ASCENDING_EAST(2, "ascending_east"),
      ASCENDING_WEST(3, "ascending_west"),
      ASCENDING_NORTH(4, "ascending_north"),
      ASCENDING_SOUTH(5, "ascending_south"),
      SOUTH_EAST(6, "south_east"),
      SOUTH_WEST(7, "south_west"),
      NORTH_WEST(8, "north_west"),
      NORTH_EAST(9, "north_east");

      private static final BlockRailBase.EnumRailDirection[] META_LOOKUP = new BlockRailBase.EnumRailDirection[values().length];
      private final int meta;
      private final String name;

      private EnumRailDirection(int var3, String var4) {
         this.meta = var3;
         this.name = var4;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public boolean isAscending() {
         return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
      }

      public static BlockRailBase.EnumRailDirection byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockRailBase.EnumRailDirection var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }

   public class Rail {
      private final World world;
      private final BlockPos pos;
      private final BlockRailBase block;
      private IBlockState state;
      private final boolean isPowered;
      private final List connectedRails = Lists.newArrayList();

      public Rail(World var2, BlockPos var3, IBlockState var4) {
         this.world = var2;
         this.pos = var3;
         this.state = var4;
         this.block = (BlockRailBase)var4.getBlock();
         BlockRailBase.EnumRailDirection var5 = (BlockRailBase.EnumRailDirection)var4.getValue(this.block.getShapeProperty());
         this.isPowered = this.block.isPowered;
         this.updateConnectedRails(var5);
      }

      public List getConnectedRails() {
         return this.connectedRails;
      }

      private void updateConnectedRails(BlockRailBase.EnumRailDirection var1) {
         this.connectedRails.clear();
         switch(var1) {
         case NORTH_SOUTH:
            this.connectedRails.add(this.pos.north());
            this.connectedRails.add(this.pos.south());
            break;
         case EAST_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.east().up());
            break;
         case ASCENDING_WEST:
            this.connectedRails.add(this.pos.west().up());
            this.connectedRails.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.connectedRails.add(this.pos.north().up());
            this.connectedRails.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.connectedRails.add(this.pos.north());
            this.connectedRails.add(this.pos.south().up());
            break;
         case SOUTH_EAST:
            this.connectedRails.add(this.pos.east());
            this.connectedRails.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.connectedRails.add(this.pos.west());
            this.connectedRails.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.connectedRails.add(this.pos.east());
            this.connectedRails.add(this.pos.north());
         }

      }

      private void removeSoftConnections() {
         for(int var1 = 0; var1 < this.connectedRails.size(); ++var1) {
            BlockRailBase.Rail var2 = this.findRailAt((BlockPos)this.connectedRails.get(var1));
            if (var2 != null && var2.isConnectedToRail(this)) {
               this.connectedRails.set(var1, var2.pos);
            } else {
               this.connectedRails.remove(var1--);
            }
         }

      }

      private boolean hasRailAt(BlockPos var1) {
         return BlockRailBase.isRailBlock(this.world, var1) || BlockRailBase.isRailBlock(this.world, var1.up()) || BlockRailBase.isRailBlock(this.world, var1.down());
      }

      @Nullable
      private BlockRailBase.Rail findRailAt(BlockPos var1) {
         IBlockState var3 = this.world.getBlockState(var1);
         if (BlockRailBase.isRailBlock(var3)) {
            return BlockRailBase.this.new Rail(this.world, var1, var3);
         } else {
            BlockPos var2 = var1.up();
            var3 = this.world.getBlockState(var2);
            if (BlockRailBase.isRailBlock(var3)) {
               return BlockRailBase.this.new Rail(this.world, var2, var3);
            } else {
               var2 = var1.down();
               var3 = this.world.getBlockState(var2);
               return BlockRailBase.isRailBlock(var3) ? BlockRailBase.this.new Rail(this.world, var2, var3) : null;
            }
         }
      }

      private boolean isConnectedToRail(BlockRailBase.Rail var1) {
         return this.isConnectedTo(var1.pos);
      }

      private boolean isConnectedTo(BlockPos var1) {
         for(int var2 = 0; var2 < this.connectedRails.size(); ++var2) {
            BlockPos var3 = (BlockPos)this.connectedRails.get(var2);
            if (var3.getX() == var1.getX() && var3.getZ() == var1.getZ()) {
               return true;
            }
         }

         return false;
      }

      protected int countAdjacentRails() {
         int var1 = 0;

         for(EnumFacing var3 : EnumFacing.Plane.HORIZONTAL) {
            if (this.hasRailAt(this.pos.offset(var3))) {
               ++var1;
            }
         }

         return var1;
      }

      private boolean canConnectTo(BlockRailBase.Rail var1) {
         return this.isConnectedToRail(var1) || this.connectedRails.size() != 2;
      }

      private void connectTo(BlockRailBase.Rail var1) {
         this.connectedRails.add(var1.pos);
         BlockPos var2 = this.pos.north();
         BlockPos var3 = this.pos.south();
         BlockPos var4 = this.pos.west();
         BlockPos var5 = this.pos.east();
         boolean var6 = this.isConnectedTo(var2);
         boolean var7 = this.isConnectedTo(var3);
         boolean var8 = this.isConnectedTo(var4);
         boolean var9 = this.isConnectedTo(var5);
         BlockRailBase.EnumRailDirection var10 = null;
         if (var6 || var7) {
            var10 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         if (var8 || var9) {
            var10 = BlockRailBase.EnumRailDirection.EAST_WEST;
         }

         if (!this.isPowered) {
            if (var7 && var9 && !var6 && !var8) {
               var10 = BlockRailBase.EnumRailDirection.SOUTH_EAST;
            }

            if (var7 && var8 && !var6 && !var9) {
               var10 = BlockRailBase.EnumRailDirection.SOUTH_WEST;
            }

            if (var6 && var8 && !var7 && !var9) {
               var10 = BlockRailBase.EnumRailDirection.NORTH_WEST;
            }

            if (var6 && var9 && !var7 && !var8) {
               var10 = BlockRailBase.EnumRailDirection.NORTH_EAST;
            }
         }

         if (var10 == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (BlockRailBase.isRailBlock(this.world, var2.up())) {
               var10 = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
            }

            if (BlockRailBase.isRailBlock(this.world, var3.up())) {
               var10 = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
            }
         }

         if (var10 == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (BlockRailBase.isRailBlock(this.world, var5.up())) {
               var10 = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
            }

            if (BlockRailBase.isRailBlock(this.world, var4.up())) {
               var10 = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
            }
         }

         if (var10 == null) {
            var10 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         this.state = this.state.withProperty(this.block.getShapeProperty(), var10);
         this.world.setBlockState(this.pos, this.state, 3);
      }

      private boolean hasNeighborRail(BlockPos var1) {
         BlockRailBase.Rail var2 = this.findRailAt(var1);
         if (var2 == null) {
            return false;
         } else {
            var2.removeSoftConnections();
            return var2.canConnectTo(this);
         }
      }

      public BlockRailBase.Rail place(boolean var1, boolean var2) {
         BlockPos var3 = this.pos.north();
         BlockPos var4 = this.pos.south();
         BlockPos var5 = this.pos.west();
         BlockPos var6 = this.pos.east();
         boolean var7 = this.hasNeighborRail(var3);
         boolean var8 = this.hasNeighborRail(var4);
         boolean var9 = this.hasNeighborRail(var5);
         boolean var10 = this.hasNeighborRail(var6);
         BlockRailBase.EnumRailDirection var11 = null;
         if ((var7 || var8) && !var9 && !var10) {
            var11 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         if ((var9 || var10) && !var7 && !var8) {
            var11 = BlockRailBase.EnumRailDirection.EAST_WEST;
         }

         if (!this.isPowered) {
            if (var8 && var10 && !var7 && !var9) {
               var11 = BlockRailBase.EnumRailDirection.SOUTH_EAST;
            }

            if (var8 && var9 && !var7 && !var10) {
               var11 = BlockRailBase.EnumRailDirection.SOUTH_WEST;
            }

            if (var7 && var9 && !var8 && !var10) {
               var11 = BlockRailBase.EnumRailDirection.NORTH_WEST;
            }

            if (var7 && var10 && !var8 && !var9) {
               var11 = BlockRailBase.EnumRailDirection.NORTH_EAST;
            }
         }

         if (var11 == null) {
            if (var7 || var8) {
               var11 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            }

            if (var9 || var10) {
               var11 = BlockRailBase.EnumRailDirection.EAST_WEST;
            }

            if (!this.isPowered) {
               if (var1) {
                  if (var8 && var10) {
                     var11 = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                  }

                  if (var9 && var8) {
                     var11 = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                  }

                  if (var10 && var7) {
                     var11 = BlockRailBase.EnumRailDirection.NORTH_EAST;
                  }

                  if (var7 && var9) {
                     var11 = BlockRailBase.EnumRailDirection.NORTH_WEST;
                  }
               } else {
                  if (var7 && var9) {
                     var11 = BlockRailBase.EnumRailDirection.NORTH_WEST;
                  }

                  if (var10 && var7) {
                     var11 = BlockRailBase.EnumRailDirection.NORTH_EAST;
                  }

                  if (var9 && var8) {
                     var11 = BlockRailBase.EnumRailDirection.SOUTH_WEST;
                  }

                  if (var8 && var10) {
                     var11 = BlockRailBase.EnumRailDirection.SOUTH_EAST;
                  }
               }
            }
         }

         if (var11 == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (BlockRailBase.isRailBlock(this.world, var3.up())) {
               var11 = BlockRailBase.EnumRailDirection.ASCENDING_NORTH;
            }

            if (BlockRailBase.isRailBlock(this.world, var4.up())) {
               var11 = BlockRailBase.EnumRailDirection.ASCENDING_SOUTH;
            }
         }

         if (var11 == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (BlockRailBase.isRailBlock(this.world, var6.up())) {
               var11 = BlockRailBase.EnumRailDirection.ASCENDING_EAST;
            }

            if (BlockRailBase.isRailBlock(this.world, var5.up())) {
               var11 = BlockRailBase.EnumRailDirection.ASCENDING_WEST;
            }
         }

         if (var11 == null) {
            var11 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         this.updateConnectedRails(var11);
         this.state = this.state.withProperty(this.block.getShapeProperty(), var11);
         if (var2 || this.world.getBlockState(this.pos) != this.state) {
            this.world.setBlockState(this.pos, this.state, 3);

            for(int var12 = 0; var12 < this.connectedRails.size(); ++var12) {
               BlockRailBase.Rail var13 = this.findRailAt((BlockPos)this.connectedRails.get(var12));
               if (var13 != null) {
                  var13.removeSoftConnections();
                  if (var13.canConnectTo(this)) {
                     var13.connectTo(this);
                  }
               }
            }
         }

         return this;
      }

      public IBlockState getBlockState() {
         return this.state;
      }
   }
}
