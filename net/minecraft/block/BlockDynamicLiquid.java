package net.minecraft.block;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockDynamicLiquid extends BlockLiquid {
   int adjacentSourceBlocks;

   protected BlockDynamicLiquid(Material material) {
      super(material);
   }

   private void placeStaticBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      world.setBlockState(blockposition, getStaticBlock(this.blockMaterial).getDefaultState().withProperty(LEVEL, (Integer)iblockdata.getValue(LEVEL)), 2);
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      org.bukkit.World bworld = world.getWorld();
      Server server = world.getServer();
      org.bukkit.block.Block source = bworld == null ? null : bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
      int i = ((Integer)iblockdata.getValue(LEVEL)).intValue();
      byte b0 = 1;
      if (this.blockMaterial == Material.LAVA && !world.provider.doesWaterVaporize()) {
         b0 = 2;
      }

      int j = this.tickRate(world);
      if (i > 0) {
         int l = -100;
         this.adjacentSourceBlocks = 0;

         for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
            l = this.checkAdjacentBlock(world, blockposition.offset(enumdirection), l);
         }

         int i1 = l + b0;
         if (i1 >= 8 || l < 0) {
            i1 = -1;
         }

         int k = this.getDepth(world.getBlockState(blockposition.up()));
         if (k >= 0) {
            if (k >= 8) {
               i1 = k;
            } else {
               i1 = k + 8;
            }
         }

         if (this.adjacentSourceBlocks >= 2 && this.blockMaterial == Material.WATER) {
            IBlockState iblockdata1 = world.getBlockState(blockposition.down());
            if (iblockdata1.getMaterial().isSolid()) {
               i1 = 0;
            } else if (iblockdata1.getMaterial() == this.blockMaterial && ((Integer)iblockdata1.getValue(LEVEL)).intValue() == 0) {
               i1 = 0;
            }
         }

         if (this.blockMaterial == Material.LAVA && i < 8 && i1 < 8 && i1 > i && random.nextInt(4) != 0) {
            j *= 4;
         }

         if (i1 == i) {
            this.placeStaticBlock(world, blockposition, iblockdata);
         } else {
            i = i1;
            if (i1 < 0) {
               world.setBlockToAir(blockposition);
            } else {
               iblockdata = iblockdata.withProperty(LEVEL, Integer.valueOf(i1));
               world.setBlockState(blockposition, iblockdata, 2);
               world.scheduleUpdate(blockposition, this, j);
               world.notifyNeighborsOfStateChange(blockposition, this);
            }
         }
      } else {
         this.placeStaticBlock(world, blockposition, iblockdata);
      }

      IBlockState iblockdata2 = world.getBlockState(blockposition.down());
      if (this.canFlowInto(world, blockposition.down(), iblockdata2)) {
         BlockFromToEvent event = new BlockFromToEvent(source, BlockFace.DOWN);
         if (server != null) {
            server.getPluginManager().callEvent(event);
         }

         if (!event.isCancelled()) {
            if (this.blockMaterial == Material.LAVA && world.getBlockState(blockposition.down()).getMaterial() == Material.WATER) {
               world.setBlockState(blockposition.down(), Blocks.STONE.getDefaultState());
               this.triggerMixEffects(world, blockposition.down());
               return;
            }

            if (i >= 8) {
               this.tryFlowInto(world, blockposition.down(), iblockdata2, i);
            } else {
               this.tryFlowInto(world, blockposition.down(), iblockdata2, i + 8);
            }
         }
      } else if (i >= 0 && (i == 0 || this.isBlocked(world, blockposition.down(), iblockdata2))) {
         Set set = this.getPossibleFlowDirections(world, blockposition);
         int k = i + b0;
         if (i >= 8) {
            k = 1;
         }

         if (k >= 8) {
            return;
         }

         for(EnumFacing enumdirection1 : set) {
            BlockFromToEvent event = new BlockFromToEvent(source, CraftBlock.notchToBlockFace(enumdirection1));
            if (server != null) {
               server.getPluginManager().callEvent(event);
            }

            if (!event.isCancelled()) {
               this.tryFlowInto(world, blockposition.offset(enumdirection1), world.getBlockState(blockposition.offset(enumdirection1)), k);
            }
         }
      }

   }

   private void tryFlowInto(World world, BlockPos blockposition, IBlockState iblockdata, int i) {
      if (world.isBlockLoaded(blockposition) && this.canFlowInto(world, blockposition, iblockdata)) {
         if (iblockdata.getMaterial() != Material.AIR) {
            if (this.blockMaterial == Material.LAVA) {
               this.triggerMixEffects(world, blockposition);
            } else {
               iblockdata.getBlock().dropBlockAsItem(world, blockposition, iblockdata, 0);
            }
         }

         world.setBlockState(blockposition, this.getDefaultState().withProperty(LEVEL, Integer.valueOf(i)), 3);
      }

   }

   private int getSlopeDistance(World world, BlockPos blockposition, int i, EnumFacing enumdirection) {
      int j = 1000;

      for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
         if (enumdirection1 != enumdirection) {
            BlockPos blockposition1 = blockposition.offset(enumdirection1);
            IBlockState iblockdata = world.getBlockState(blockposition1);
            if (!this.isBlocked(world, blockposition1, iblockdata) && (iblockdata.getMaterial() != this.blockMaterial || ((Integer)iblockdata.getValue(LEVEL)).intValue() > 0)) {
               if (!this.isBlocked(world, blockposition1.down(), iblockdata)) {
                  return i;
               }

               if (i < this.getSlopeFindDistance(world)) {
                  int k = this.getSlopeDistance(world, blockposition1, i + 1, enumdirection1.getOpposite());
                  if (k < j) {
                     j = k;
                  }
               }
            }
         }
      }

      return j;
   }

   private int getSlopeFindDistance(World world) {
      return this.blockMaterial == Material.LAVA && !world.provider.doesWaterVaporize() ? 2 : 4;
   }

   private Set getPossibleFlowDirections(World world, BlockPos blockposition) {
      int i = 1000;
      EnumSet enumset = EnumSet.noneOf(EnumFacing.class);

      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         BlockPos blockposition1 = blockposition.offset(enumdirection);
         IBlockState iblockdata = world.getBlockState(blockposition1);
         if (!this.isBlocked(world, blockposition1, iblockdata) && (iblockdata.getMaterial() != this.blockMaterial || ((Integer)iblockdata.getValue(LEVEL)).intValue() > 0)) {
            int j;
            if (this.isBlocked(world, blockposition1.down(), world.getBlockState(blockposition1.down()))) {
               j = this.getSlopeDistance(world, blockposition1, 1, enumdirection.getOpposite());
            } else {
               j = 0;
            }

            if (j < i) {
               enumset.clear();
            }

            if (j <= i) {
               enumset.add(enumdirection);
               i = j;
            }
         }
      }

      return enumset;
   }

   private boolean isBlocked(World world, BlockPos blockposition, IBlockState iblockdata) {
      Block block = world.getBlockState(blockposition).getBlock();
      return !(block instanceof BlockDoor) && block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS ? (block.blockMaterial != Material.PORTAL && block.blockMaterial != Material.STRUCTURE_VOID ? block.blockMaterial.blocksMovement() : true) : true;
   }

   protected int checkAdjacentBlock(World world, BlockPos blockposition, int i) {
      int j = this.getDepth(world.getBlockState(blockposition));
      if (j < 0) {
         return i;
      } else {
         if (j == 0) {
            ++this.adjacentSourceBlocks;
         }

         if (j >= 8) {
            j = 0;
         }

         return i >= 0 && j >= i ? i : j;
      }
   }

   private boolean canFlowInto(World world, BlockPos blockposition, IBlockState iblockdata) {
      Material material = iblockdata.getMaterial();
      return material != this.blockMaterial && material != Material.LAVA && !this.isBlocked(world, blockposition, iblockdata);
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!this.checkForMixing(world, blockposition, iblockdata)) {
         world.scheduleUpdate(blockposition, this, this.tickRate(world));
      }

   }
}
