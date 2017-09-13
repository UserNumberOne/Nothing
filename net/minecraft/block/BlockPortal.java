package net.minecraft.block;

import com.google.common.cache.LoadingCache;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;

public class BlockPortal extends BlockBreakable {
   public static final PropertyEnum AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, (Enum[])(EnumFacing.Axis.X, EnumFacing.Axis.Z));
   protected static final AxisAlignedBB X_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.375D, 1.0D, 1.0D, 0.625D);
   protected static final AxisAlignedBB Z_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.0D, 0.625D, 1.0D, 1.0D);
   protected static final AxisAlignedBB Y_AABB = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);

   public BlockPortal() {
      super(Material.PORTAL, false);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      switch(BlockPortal.SyntheticClass_1.a[((EnumFacing.Axis)iblockdata.getValue(AXIS)).ordinal()]) {
      case 1:
         return X_AABB;
      case 2:
      default:
         return Y_AABB;
      case 3:
         return Z_AABB;
      }
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      super.updateTick(world, blockposition, iblockdata, random);
      if (world.provider.isSurfaceWorld() && world.getGameRules().getBoolean("doMobSpawning") && random.nextInt(2000) < world.getDifficulty().getDifficultyId()) {
         int i = blockposition.getY();

         BlockPos blockposition1;
         for(blockposition1 = blockposition; !world.getBlockState(blockposition1).isFullyOpaque() && blockposition1.getY() > 0; blockposition1 = blockposition1.down()) {
            ;
         }

         if (i > 0 && !world.getBlockState(blockposition1.up()).isNormalCube()) {
            Entity entity = ItemMonsterPlacer.spawnCreature(world, EntityList.getEntityStringFromClass(EntityPigZombie.class), (double)blockposition1.getX() + 0.5D, (double)blockposition1.getY() + 1.1D, (double)blockposition1.getZ() + 0.5D, SpawnReason.NETHER_PORTAL);
            if (entity != null) {
               entity.timeUntilPortal = entity.getPortalCooldown();
            }
         }
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public static int getMetaForAxis(EnumFacing.Axis enumdirection_enumaxis) {
      return enumdirection_enumaxis == EnumFacing.Axis.X ? 1 : (enumdirection_enumaxis == EnumFacing.Axis.Z ? 2 : 0);
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean trySpawnPortal(World world, BlockPos blockposition) {
      BlockPortal.Size blockportal_shape = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.X);
      if (blockportal_shape.isValid() && blockportal_shape.portalBlockCount == 0) {
         return blockportal_shape.createPortal();
      } else {
         BlockPortal.Size blockportal_shape1 = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.Z);
         return blockportal_shape1.isValid() && blockportal_shape1.portalBlockCount == 0 ? blockportal_shape1.createPortal() : false;
      }
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      EnumFacing.Axis enumdirection_enumaxis = (EnumFacing.Axis)iblockdata.getValue(AXIS);
      if (enumdirection_enumaxis == EnumFacing.Axis.X) {
         BlockPortal.Size blockportal_shape = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.X);
         if (!blockportal_shape.isValid() || blockportal_shape.portalBlockCount < blockportal_shape.width * blockportal_shape.height) {
            world.setBlockState(blockposition, Blocks.AIR.getDefaultState());
         }
      } else if (enumdirection_enumaxis == EnumFacing.Axis.Z) {
         BlockPortal.Size blockportal_shape = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.Z);
         if (!blockportal_shape.isValid() || blockportal_shape.portalBlockCount < blockportal_shape.width * blockportal_shape.height) {
            world.setBlockState(blockposition, Blocks.AIR.getDefaultState());
         }
      }

   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss()) {
         EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new Location(world.getWorld(), (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ()));
         world.getServer().getPluginManager().callEvent(event);
         entity.setPortal(blockposition);
      }

   }

   @Nullable
   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return null;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(AXIS, (i & 3) == 2 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return getMetaForAxis((EnumFacing.Axis)iblockdata.getValue(AXIS));
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockPortal.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
      case 1:
      case 2:
         switch(BlockPortal.SyntheticClass_1.a[((EnumFacing.Axis)iblockdata.getValue(AXIS)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(AXIS, EnumFacing.Axis.Z);
         case 2:
         default:
            return iblockdata;
         case 3:
            return iblockdata.withProperty(AXIS, EnumFacing.Axis.X);
         }
      default:
         return iblockdata;
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AXIS});
   }

   public BlockPattern.PatternHelper createPatternHelper(World world, BlockPos blockposition) {
      EnumFacing.Axis enumdirection_enumaxis = EnumFacing.Axis.Z;
      BlockPortal.Size blockportal_shape = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.X);
      LoadingCache loadingcache = BlockPattern.createLoadingCache(world, true);
      if (!blockportal_shape.isValid()) {
         enumdirection_enumaxis = EnumFacing.Axis.X;
         blockportal_shape = new BlockPortal.Size(world, blockposition, EnumFacing.Axis.Z);
      }

      if (!blockportal_shape.isValid()) {
         return new BlockPattern.PatternHelper(blockposition, EnumFacing.NORTH, EnumFacing.UP, loadingcache, 1, 1, 1);
      } else {
         int[] aint = new int[EnumFacing.AxisDirection.values().length];
         EnumFacing enumdirection = blockportal_shape.rightDir.rotateYCCW();
         BlockPos blockposition1 = blockportal_shape.bottomLeft.up(blockportal_shape.getHeight() - 1);

         for(EnumFacing.AxisDirection enumdirection_enumaxisdirection : EnumFacing.AxisDirection.values()) {
            BlockPattern.PatternHelper shapedetector_shapedetectorcollection = new BlockPattern.PatternHelper(enumdirection.getAxisDirection() == enumdirection_enumaxisdirection ? blockposition1 : blockposition1.offset(blockportal_shape.rightDir, blockportal_shape.getWidth() - 1), EnumFacing.getFacingFromAxis(enumdirection_enumaxisdirection, enumdirection_enumaxis), EnumFacing.UP, loadingcache, blockportal_shape.getWidth(), blockportal_shape.getHeight(), 1);

            for(int k = 0; k < blockportal_shape.getWidth(); ++k) {
               for(int l = 0; l < blockportal_shape.getHeight(); ++l) {
                  BlockWorldState shapedetectorblock = shapedetector_shapedetectorcollection.translateOffset(k, l, 1);
                  if (shapedetectorblock.getBlockState() != null && shapedetectorblock.getBlockState().getMaterial() != Material.AIR) {
                     ++aint[enumdirection_enumaxisdirection.ordinal()];
                  }
               }
            }
         }

         EnumFacing.AxisDirection enumdirection_enumaxisdirection1 = EnumFacing.AxisDirection.POSITIVE;

         for(EnumFacing.AxisDirection enumdirection_enumaxisdirection2 : EnumFacing.AxisDirection.values()) {
            if (aint[enumdirection_enumaxisdirection2.ordinal()] < aint[enumdirection_enumaxisdirection1.ordinal()]) {
               enumdirection_enumaxisdirection1 = enumdirection_enumaxisdirection2;
            }
         }

         return new BlockPattern.PatternHelper(enumdirection.getAxisDirection() == enumdirection_enumaxisdirection1 ? blockposition1 : blockposition1.offset(blockportal_shape.rightDir, blockportal_shape.getWidth() - 1), EnumFacing.getFacingFromAxis(enumdirection_enumaxisdirection1, enumdirection_enumaxis), EnumFacing.UP, loadingcache, blockportal_shape.getWidth(), blockportal_shape.getHeight(), 1);
      }
   }

   public static class Size {
      private final World world;
      private final EnumFacing.Axis axis;
      private final EnumFacing rightDir;
      private final EnumFacing leftDir;
      private int portalBlockCount;
      private BlockPos bottomLeft;
      private int height;
      private int width;
      Collection blocks = new HashSet();

      public Size(World world, BlockPos blockposition, EnumFacing.Axis enumdirection_enumaxis) {
         this.world = world;
         this.axis = enumdirection_enumaxis;
         if (enumdirection_enumaxis == EnumFacing.Axis.X) {
            this.leftDir = EnumFacing.EAST;
            this.rightDir = EnumFacing.WEST;
         } else {
            this.leftDir = EnumFacing.NORTH;
            this.rightDir = EnumFacing.SOUTH;
         }

         for(BlockPos blockposition1 = blockposition; blockposition.getY() > blockposition1.getY() - 21 && blockposition.getY() > 0 && this.isEmptyBlock(world.getBlockState(blockposition.down()).getBlock()); blockposition = blockposition.down()) {
            ;
         }

         int i = this.getDistanceUntilEdge(blockposition, this.leftDir) - 1;
         if (i >= 0) {
            this.bottomLeft = blockposition.offset(this.leftDir, i);
            this.width = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
            if (this.width < 2 || this.width > 21) {
               this.bottomLeft = null;
               this.width = 0;
            }
         }

         if (this.bottomLeft != null) {
            this.height = this.calculatePortalHeight();
         }

      }

      protected int getDistanceUntilEdge(BlockPos blockposition, EnumFacing enumdirection) {
         int i;
         for(i = 0; i < 22; ++i) {
            BlockPos blockposition1 = blockposition.offset(enumdirection, i);
            if (!this.isEmptyBlock(this.world.getBlockState(blockposition1).getBlock()) || this.world.getBlockState(blockposition1.down()).getBlock() != Blocks.OBSIDIAN) {
               break;
            }
         }

         Block block = this.world.getBlockState(blockposition.offset(enumdirection, i)).getBlock();
         return block == Blocks.OBSIDIAN ? i : 0;
      }

      public int getHeight() {
         return this.height;
      }

      public int getWidth() {
         return this.width;
      }

      protected int calculatePortalHeight() {
         this.blocks.clear();
         org.bukkit.World bworld = this.world.getWorld();

         label60:
         for(this.height = 0; this.height < 21; ++this.height) {
            for(int i = 0; i < this.width; ++i) {
               BlockPos blockposition = this.bottomLeft.offset(this.rightDir, i).up(this.height);
               Block block = this.world.getBlockState(blockposition).getBlock();
               if (!this.isEmptyBlock(block)) {
                  break label60;
               }

               if (block == Blocks.PORTAL) {
                  ++this.portalBlockCount;
               }

               if (i == 0) {
                  block = this.world.getBlockState(blockposition.offset(this.leftDir)).getBlock();
                  if (block != Blocks.OBSIDIAN) {
                     break label60;
                  }

                  BlockPos pos = blockposition.offset(this.leftDir);
                  this.blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
               } else if (i == this.width - 1) {
                  block = this.world.getBlockState(blockposition.offset(this.rightDir)).getBlock();
                  if (block != Blocks.OBSIDIAN) {
                     break label60;
                  }

                  BlockPos pos = blockposition.offset(this.rightDir);
                  this.blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
               }
            }
         }

         for(int i = 0; i < this.width; ++i) {
            if (this.world.getBlockState(this.bottomLeft.offset(this.rightDir, i).up(this.height)).getBlock() != Blocks.OBSIDIAN) {
               this.height = 0;
               break;
            }

            BlockPos pos = this.bottomLeft.offset(this.rightDir, i).up(this.height);
            this.blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
         }

         if (this.height <= 21 && this.height >= 3) {
            return this.height;
         } else {
            this.bottomLeft = null;
            this.width = 0;
            this.height = 0;
            return 0;
         }
      }

      protected boolean isEmptyBlock(Block block) {
         return block.blockMaterial == Material.AIR || block == Blocks.FIRE || block == Blocks.PORTAL;
      }

      public boolean isValid() {
         return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
      }

      public boolean createPortal() {
         org.bukkit.World bworld = this.world.getWorld();

         for(int i = 0; i < this.width; ++i) {
            BlockPos blockposition = this.bottomLeft.offset(this.rightDir, i);

            for(int j = 0; j < this.height; ++j) {
               BlockPos pos = blockposition.up(j);
               this.blocks.add(bworld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()));
            }
         }

         PortalCreateEvent event = new PortalCreateEvent(this.blocks, bworld, CreateReason.FIRE);
         this.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return false;
         } else {
            for(int i = 0; i < this.width; ++i) {
               BlockPos blockposition = this.bottomLeft.offset(this.rightDir, i);

               for(int j = 0; j < this.height; ++j) {
                  this.world.setBlockState(blockposition.up(j), Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, this.axis), 2);
               }
            }

            return true;
         }
      }
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[Rotation.values().length];

      static {
         try {
            b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[Rotation.CLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         a = new int[EnumFacing.Axis.values().length];

         try {
            a[EnumFacing.Axis.X.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.Axis.Y.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.Axis.Z.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
