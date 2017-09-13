package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import org.bukkit.Server;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.material.MaterialData;

public class BlockFire extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   public static final PropertyBool UPPER = PropertyBool.create("up");
   private final Map encouragements = Maps.newIdentityHashMap();
   private final Map flammabilities = Maps.newIdentityHashMap();

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return !iblockaccess.getBlockState(blockposition.down()).isFullyOpaque() && !Blocks.FIRE.canCatchFire(iblockaccess, blockposition.down()) ? iblockdata.withProperty(NORTH, Boolean.valueOf(this.canCatchFire(iblockaccess, blockposition.north()))).withProperty(EAST, Boolean.valueOf(this.canCatchFire(iblockaccess, blockposition.east()))).withProperty(SOUTH, Boolean.valueOf(this.canCatchFire(iblockaccess, blockposition.south()))).withProperty(WEST, Boolean.valueOf(this.canCatchFire(iblockaccess, blockposition.west()))).withProperty(UPPER, Boolean.valueOf(this.canCatchFire(iblockaccess, blockposition.up()))) : this.getDefaultState();
   }

   protected BlockFire() {
      super(Material.FIRE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)).withProperty(NORTH, Boolean.valueOf(false)).withProperty(EAST, Boolean.valueOf(false)).withProperty(SOUTH, Boolean.valueOf(false)).withProperty(WEST, Boolean.valueOf(false)).withProperty(UPPER, Boolean.valueOf(false)));
      this.setTickRandomly(true);
   }

   public static void init() {
      Blocks.FIRE.setFireInfo(Blocks.PLANKS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DOUBLE_WOODEN_SLAB, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.WOODEN_SLAB, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_FENCE_GATE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_FENCE, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.OAK_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.BIRCH_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.SPRUCE_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.JUNGLE_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.ACACIA_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.DARK_OAK_STAIRS, 5, 20);
      Blocks.FIRE.setFireInfo(Blocks.LOG, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.LOG2, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.LEAVES, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.LEAVES2, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.BOOKSHELF, 30, 20);
      Blocks.FIRE.setFireInfo(Blocks.TNT, 15, 100);
      Blocks.FIRE.setFireInfo(Blocks.TALLGRASS, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.DOUBLE_PLANT, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.YELLOW_FLOWER, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.RED_FLOWER, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.DEADBUSH, 60, 100);
      Blocks.FIRE.setFireInfo(Blocks.WOOL, 30, 60);
      Blocks.FIRE.setFireInfo(Blocks.VINE, 15, 100);
      Blocks.FIRE.setFireInfo(Blocks.COAL_BLOCK, 5, 5);
      Blocks.FIRE.setFireInfo(Blocks.HAY_BLOCK, 60, 20);
      Blocks.FIRE.setFireInfo(Blocks.CARPET, 60, 20);
   }

   public void setFireInfo(Block block, int i, int j) {
      this.encouragements.put(block, Integer.valueOf(i));
      this.flammabilities.put(block, Integer.valueOf(j));
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public int tickRate(World world) {
      return 30;
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (world.getGameRules().getBoolean("doFireTick")) {
         if (!this.canPlaceBlockAt(world, blockposition)) {
            this.fireExtinguished(world, blockposition);
         }

         Block block = world.getBlockState(blockposition.down()).getBlock();
         boolean flag = block == Blocks.NETHERRACK;
         if (world.provider instanceof WorldProviderEnd && block == Blocks.BEDROCK) {
            flag = true;
         }

         int i = ((Integer)iblockdata.getValue(AGE)).intValue();
         if (!flag && world.isRaining() && this.canDie(world, blockposition) && random.nextFloat() < 0.2F + (float)i * 0.03F) {
            this.fireExtinguished(world, blockposition);
         } else {
            if (i < 15) {
               iblockdata = iblockdata.withProperty(AGE, Integer.valueOf(i + random.nextInt(3) / 2));
               world.setBlockState(blockposition, iblockdata, 4);
            }

            world.scheduleUpdate(blockposition, this, this.tickRate(world) + random.nextInt(10));
            if (!flag) {
               if (!this.canNeighborCatchFire(world, blockposition)) {
                  if (!world.getBlockState(blockposition.down()).isFullyOpaque() || i > 3) {
                     this.fireExtinguished(world, blockposition);
                  }

                  return;
               }

               if (!this.canCatchFire(world, blockposition.down()) && i == 15 && random.nextInt(4) == 0) {
                  this.fireExtinguished(world, blockposition);
                  return;
               }
            }

            boolean flag1 = world.isBlockinHighHumidity(blockposition);
            byte b0 = 0;
            if (flag1) {
               b0 = -50;
            }

            this.catchOnFire(world, blockposition.east(), 300 + b0, random, i);
            this.catchOnFire(world, blockposition.west(), 300 + b0, random, i);
            this.catchOnFire(world, blockposition.down(), 250 + b0, random, i);
            this.catchOnFire(world, blockposition.up(), 250 + b0, random, i);
            this.catchOnFire(world, blockposition.north(), 300 + b0, random, i);
            this.catchOnFire(world, blockposition.south(), 300 + b0, random, i);

            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  for(int l = -1; l <= 4; ++l) {
                     if (j != 0 || l != 0 || k != 0) {
                        int i1 = 100;
                        if (l > 1) {
                           i1 += (l - 1) * 100;
                        }

                        BlockPos blockposition1 = blockposition.add(j, l, k);
                        int j1 = this.getNeighborEncouragement(world, blockposition1);
                        if (j1 > 0) {
                           int k1 = (j1 + 40 + world.getDifficulty().getDifficultyId() * 7) / (i + 30);
                           if (flag1) {
                              k1 /= 2;
                           }

                           if (k1 > 0 && random.nextInt(i1) <= k1 && (!world.isRaining() || !this.canDie(world, blockposition1))) {
                              int l1 = i + random.nextInt(5) / 4;
                              if (l1 > 15) {
                                 l1 = 15;
                              }

                              if (world.getBlockState(blockposition1) != Blocks.FIRE && !CraftEventFactory.callBlockIgniteEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), blockposition.getX(), blockposition.getY(), blockposition.getZ()).isCancelled()) {
                                 Server server = world.getServer();
                                 org.bukkit.World bworld = world.getWorld();
                                 BlockState blockState = bworld.getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                                 blockState.setTypeId(Block.getIdFromBlock(this));
                                 blockState.setData(new MaterialData(Block.getIdFromBlock(this), (byte)l1));
                                 BlockSpreadEvent spreadEvent = new BlockSpreadEvent(blockState.getBlock(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), blockState);
                                 server.getPluginManager().callEvent(spreadEvent);
                                 if (!spreadEvent.isCancelled()) {
                                    blockState.update(true);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   protected boolean canDie(World world, BlockPos blockposition) {
      return world.isRainingAt(blockposition) || world.isRainingAt(blockposition.west()) || world.isRainingAt(blockposition.east()) || world.isRainingAt(blockposition.north()) || world.isRainingAt(blockposition.south());
   }

   public boolean requiresUpdates() {
      return false;
   }

   private int getFlammability(Block block) {
      Integer integer = (Integer)this.flammabilities.get(block);
      return integer == null ? 0 : integer.intValue();
   }

   private int getEncouragement(Block block) {
      Integer integer = (Integer)this.encouragements.get(block);
      return integer == null ? 0 : integer.intValue();
   }

   private void catchOnFire(World world, BlockPos blockposition, int i, Random random, int j) {
      int k = this.getFlammability(world.getBlockState(blockposition).getBlock());
      if (random.nextInt(i) < k) {
         IBlockState iblockdata = world.getBlockState(blockposition);
         org.bukkit.block.Block theBlock = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         BlockBurnEvent event = new BlockBurnEvent(theBlock);
         world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }

         if (random.nextInt(j + 10) < 5 && !world.isRainingAt(blockposition)) {
            int l = j + random.nextInt(5) / 4;
            if (l > 15) {
               l = 15;
            }

            world.setBlockState(blockposition, this.getDefaultState().withProperty(AGE, Integer.valueOf(l)), 3);
         } else {
            world.setBlockToAir(blockposition);
         }

         if (iblockdata.getBlock() == Blocks.TNT) {
            Blocks.TNT.onBlockDestroyedByPlayer(world, blockposition, iblockdata.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
         }
      }

   }

   private boolean canNeighborCatchFire(World world, BlockPos blockposition) {
      for(EnumFacing enumdirection : EnumFacing.values()) {
         if (this.canCatchFire(world, blockposition.offset(enumdirection))) {
            return true;
         }
      }

      return false;
   }

   private int getNeighborEncouragement(World world, BlockPos blockposition) {
      if (!world.isAirBlock(blockposition)) {
         return 0;
      } else {
         int i = 0;

         for(EnumFacing enumdirection : EnumFacing.values()) {
            i = Math.max(this.getEncouragement(world.getBlockState(blockposition.offset(enumdirection)).getBlock()), i);
         }

         return i;
      }
   }

   public boolean isCollidable() {
      return false;
   }

   public boolean canCatchFire(IBlockAccess iblockaccess, BlockPos blockposition) {
      return this.getEncouragement(iblockaccess.getBlockState(blockposition).getBlock()) > 0;
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return world.getBlockState(blockposition.down()).isFullyOpaque() || this.canNeighborCatchFire(world, blockposition);
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.getBlockState(blockposition.down()).isFullyOpaque() && !this.canNeighborCatchFire(world, blockposition)) {
         this.fireExtinguished(world, blockposition);
      }

   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (world.provider.getDimensionType().getId() > 0 || !Blocks.PORTAL.trySpawnPortal(world, blockposition)) {
         if (!world.getBlockState(blockposition.down()).isFullyOpaque() && !this.canNeighborCatchFire(world, blockposition)) {
            this.fireExtinguished(world, blockposition);
         } else {
            world.scheduleUpdate(blockposition, this, this.tickRate(world) + world.rand.nextInt(10));
         }
      }

   }

   public MapColor getMapColor(IBlockState iblockdata) {
      return MapColor.TNT;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, NORTH, EAST, SOUTH, WEST, UPPER});
   }

   private void fireExtinguished(World world, BlockPos position) {
      if (!CraftEventFactory.callBlockFadeEvent(world.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()), Blocks.AIR).isCancelled()) {
         world.setBlockToAir(position);
      }

   }
}
