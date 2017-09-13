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
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
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

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return !var2.getBlockState(var3.down()).isFullyOpaque() && !Blocks.FIRE.canCatchFire(var2, var3.down()) ? var1.withProperty(NORTH, Boolean.valueOf(this.canCatchFire(var2, var3.north()))).withProperty(EAST, Boolean.valueOf(this.canCatchFire(var2, var3.east()))).withProperty(SOUTH, Boolean.valueOf(this.canCatchFire(var2, var3.south()))).withProperty(WEST, Boolean.valueOf(this.canCatchFire(var2, var3.west()))).withProperty(UPPER, Boolean.valueOf(this.canCatchFire(var2, var3.up()))) : this.getDefaultState();
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

   public void setFireInfo(Block var1, int var2, int var3) {
      this.encouragements.put(var1, Integer.valueOf(var2));
      this.flammabilities.put(var1, Integer.valueOf(var3));
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public int tickRate(World var1) {
      return 30;
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (var1.getGameRules().getBoolean("doFireTick")) {
         if (!this.canPlaceBlockAt(var1, var2)) {
            this.fireExtinguished(var1, var2);
         }

         Block var5 = var1.getBlockState(var2.down()).getBlock();
         boolean var6 = var5 == Blocks.NETHERRACK;
         if (var1.provider instanceof WorldProviderEnd && var5 == Blocks.BEDROCK) {
            var6 = true;
         }

         int var7 = ((Integer)var3.getValue(AGE)).intValue();
         if (!var6 && var1.isRaining() && this.canDie(var1, var2) && var4.nextFloat() < 0.2F + (float)var7 * 0.03F) {
            this.fireExtinguished(var1, var2);
         } else {
            if (var7 < 15) {
               var3 = var3.withProperty(AGE, Integer.valueOf(var7 + var4.nextInt(3) / 2));
               var1.setBlockState(var2, var3, 4);
            }

            var1.scheduleUpdate(var2, this, this.tickRate(var1) + var4.nextInt(10));
            if (!var6) {
               if (!this.canNeighborCatchFire(var1, var2)) {
                  if (!var1.getBlockState(var2.down()).isFullyOpaque() || var7 > 3) {
                     this.fireExtinguished(var1, var2);
                  }

                  return;
               }

               if (!this.canCatchFire(var1, var2.down()) && var7 == 15 && var4.nextInt(4) == 0) {
                  this.fireExtinguished(var1, var2);
                  return;
               }
            }

            boolean var8 = var1.isBlockinHighHumidity(var2);
            byte var9 = 0;
            if (var8) {
               var9 = -50;
            }

            this.catchOnFire(var1, var2.east(), 300 + var9, var4, var7);
            this.catchOnFire(var1, var2.west(), 300 + var9, var4, var7);
            this.catchOnFire(var1, var2.down(), 250 + var9, var4, var7);
            this.catchOnFire(var1, var2.up(), 250 + var9, var4, var7);
            this.catchOnFire(var1, var2.north(), 300 + var9, var4, var7);
            this.catchOnFire(var1, var2.south(), 300 + var9, var4, var7);

            for(int var10 = -1; var10 <= 1; ++var10) {
               for(int var11 = -1; var11 <= 1; ++var11) {
                  for(int var12 = -1; var12 <= 4; ++var12) {
                     if (var10 != 0 || var12 != 0 || var11 != 0) {
                        int var13 = 100;
                        if (var12 > 1) {
                           var13 += (var12 - 1) * 100;
                        }

                        BlockPos var14 = var2.add(var10, var12, var11);
                        int var15 = this.getNeighborEncouragement(var1, var14);
                        if (var15 > 0) {
                           int var16 = (var15 + 40 + var1.getDifficulty().getDifficultyId() * 7) / (var7 + 30);
                           if (var8) {
                              var16 /= 2;
                           }

                           if (var16 > 0 && var4.nextInt(var13) <= var16 && (!var1.isRaining() || !this.canDie(var1, var14))) {
                              int var17 = var7 + var4.nextInt(5) / 4;
                              if (var17 > 15) {
                                 var17 = 15;
                              }

                              if (var1.getBlockState(var14) != Blocks.FIRE && !CraftEventFactory.callBlockIgniteEvent(var1, var14.getX(), var14.getY(), var14.getZ(), var2.getX(), var2.getY(), var2.getZ()).isCancelled()) {
                                 CraftServer var18 = var1.getServer();
                                 CraftWorld var19 = var1.getWorld();
                                 BlockState var20 = var19.getBlockAt(var14.getX(), var14.getY(), var14.getZ()).getState();
                                 var20.setTypeId(Block.getIdFromBlock(this));
                                 var20.setData(new MaterialData(Block.getIdFromBlock(this), (byte)var17));
                                 BlockSpreadEvent var21 = new BlockSpreadEvent(var20.getBlock(), var19.getBlockAt(var2.getX(), var2.getY(), var2.getZ()), var20);
                                 var18.getPluginManager().callEvent(var21);
                                 if (!var21.isCancelled()) {
                                    var20.update(true);
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

   protected boolean canDie(World var1, BlockPos var2) {
      return var1.isRainingAt(var2) || var1.isRainingAt(var2.west()) || var1.isRainingAt(var2.east()) || var1.isRainingAt(var2.north()) || var1.isRainingAt(var2.south());
   }

   public boolean requiresUpdates() {
      return false;
   }

   private int getFlammability(Block var1) {
      Integer var2 = (Integer)this.flammabilities.get(var1);
      return var2 == null ? 0 : var2.intValue();
   }

   private int getEncouragement(Block var1) {
      Integer var2 = (Integer)this.encouragements.get(var1);
      return var2 == null ? 0 : var2.intValue();
   }

   private void catchOnFire(World var1, BlockPos var2, int var3, Random var4, int var5) {
      int var6 = this.getFlammability(var1.getBlockState(var2).getBlock());
      if (var4.nextInt(var3) < var6) {
         IBlockState var7 = var1.getBlockState(var2);
         org.bukkit.block.Block var8 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         BlockBurnEvent var9 = new BlockBurnEvent(var8);
         var1.getServer().getPluginManager().callEvent(var9);
         if (var9.isCancelled()) {
            return;
         }

         if (var4.nextInt(var5 + 10) < 5 && !var1.isRainingAt(var2)) {
            int var10 = var5 + var4.nextInt(5) / 4;
            if (var10 > 15) {
               var10 = 15;
            }

            var1.setBlockState(var2, this.getDefaultState().withProperty(AGE, Integer.valueOf(var10)), 3);
         } else {
            var1.setBlockToAir(var2);
         }

         if (var7.getBlock() == Blocks.TNT) {
            Blocks.TNT.onBlockDestroyedByPlayer(var1, var2, var7.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
         }
      }

   }

   private boolean canNeighborCatchFire(World var1, BlockPos var2) {
      for(EnumFacing var6 : EnumFacing.values()) {
         if (this.canCatchFire(var1, var2.offset(var6))) {
            return true;
         }
      }

      return false;
   }

   private int getNeighborEncouragement(World var1, BlockPos var2) {
      if (!var1.isAirBlock(var2)) {
         return 0;
      } else {
         int var3 = 0;

         for(EnumFacing var7 : EnumFacing.values()) {
            var3 = Math.max(this.getEncouragement(var1.getBlockState(var2.offset(var7)).getBlock()), var3);
         }

         return var3;
      }
   }

   public boolean isCollidable() {
      return false;
   }

   public boolean canCatchFire(IBlockAccess var1, BlockPos var2) {
      return this.getEncouragement(var1.getBlockState(var2).getBlock()) > 0;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque() || this.canNeighborCatchFire(var1, var2);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.getBlockState(var3.down()).isFullyOpaque() && !this.canNeighborCatchFire(var2, var3)) {
         this.fireExtinguished(var2, var3);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (var1.provider.getDimensionType().getId() > 0 || !Blocks.PORTAL.trySpawnPortal(var1, var2)) {
         if (!var1.getBlockState(var2.down()).isFullyOpaque() && !this.canNeighborCatchFire(var1, var2)) {
            this.fireExtinguished(var1, var2);
         } else {
            var1.scheduleUpdate(var2, this, this.tickRate(var1) + var1.rand.nextInt(10));
         }
      }

   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.TNT;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, NORTH, EAST, SOUTH, WEST, UPPER});
   }

   private void fireExtinguished(World var1, BlockPos var2) {
      if (!CraftEventFactory.callBlockFadeEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), Blocks.AIR).isCancelled()) {
         var1.setBlockToAir(var2);
      }

   }
}
