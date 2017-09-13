package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Iterator;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.util.BlockStateListPopulator;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class BlockSkull extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool NODROP = PropertyBool.create("nodrop");
   private static final Predicate IS_WITHER_SKELETON = new Predicate() {
      public boolean apply(@Nullable BlockWorldState shapedetectorblock) {
         return shapedetectorblock.getBlockState() != null && shapedetectorblock.getBlockState().getBlock() == Blocks.SKULL && shapedetectorblock.getTileEntity() instanceof TileEntitySkull && ((TileEntitySkull)shapedetectorblock.getTileEntity()).getSkullType() == 1;
      }

      public boolean apply(Object object) {
         return this.apply((BlockWorldState)object);
      }
   };
   protected static final AxisAlignedBB DEFAULT_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D);
   protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.25D, 0.25D, 0.5D, 0.75D, 0.75D, 1.0D);
   protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.5D);
   protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.5D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
   protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.25D, 0.25D, 0.5D, 0.75D, 0.75D);
   private BlockPattern witherBasePattern;
   private BlockPattern witherPattern;

   protected BlockSkull() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(NODROP, Boolean.valueOf(false)));
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("tile.skull.skeleton.name");
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      switch(BlockSkull.SyntheticClass_1.a[((EnumFacing)iblockdata.getValue(FACING)).ordinal()]) {
      case 1:
      default:
         return DEFAULT_AABB;
      case 2:
         return NORTH_AABB;
      case 3:
         return SOUTH_AABB;
      case 4:
         return WEST_AABB;
      case 5:
         return EAST_AABB;
      }
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return this.getDefaultState().withProperty(FACING, entityliving.getHorizontalFacing()).withProperty(NODROP, Boolean.valueOf(false));
   }

   public TileEntity createNewTileEntity(World world, int i) {
      return new TileEntitySkull();
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      int i = 0;
      TileEntity tileentity = world.getTileEntity(blockposition);
      if (tileentity instanceof TileEntitySkull) {
         i = ((TileEntitySkull)tileentity).getSkullType();
      }

      return new ItemStack(Items.SKULL, 1, i);
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      if (world.rand.nextFloat() < f) {
         TileEntitySkull tileentityskull = (TileEntitySkull)world.getTileEntity(blockposition);
         ItemStack itemstack = this.getItem(world, blockposition, iblockdata);
         if (tileentityskull.getSkullType() == 3 && tileentityskull.getPlayerProfile() != null) {
            itemstack.setTagCompound(new NBTTagCompound());
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            NBTUtil.writeGameProfile(nbttagcompound, tileentityskull.getPlayerProfile());
            itemstack.getTagCompound().setTag("SkullOwner", nbttagcompound);
         }

         spawnAsEntity(world, blockposition, itemstack);
      }

   }

   public void onBlockHarvested(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman) {
      if (entityhuman.capabilities.isCreativeMode) {
         iblockdata = iblockdata.withProperty(NODROP, Boolean.valueOf(true));
         world.setBlockState(blockposition, iblockdata, 4);
      }

      super.onBlockHarvested(world, blockposition, iblockdata, entityhuman);
   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!world.isRemote) {
         super.breakBlock(world, blockposition, iblockdata);
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Items.SKULL;
   }

   public boolean canDispenserPlace(World world, BlockPos blockposition, ItemStack itemstack) {
      return itemstack.getMetadata() == 1 && blockposition.getY() >= 2 && world.getDifficulty() != EnumDifficulty.PEACEFUL && !world.isRemote ? this.getWitherBasePattern().match(world, blockposition) != null : false;
   }

   public void checkWitherSpawn(World world, BlockPos blockposition, TileEntitySkull tileentityskull) {
      if (!world.captureBlockStates) {
         if (tileentityskull.getSkullType() == 1 && blockposition.getY() >= 2 && world.getDifficulty() != EnumDifficulty.PEACEFUL && !world.isRemote) {
            BlockPattern shapedetector = this.getWitherPattern();
            BlockPattern.PatternHelper shapedetector_shapedetectorcollection = shapedetector.match(world, blockposition);
            if (shapedetector_shapedetectorcollection != null) {
               BlockStateListPopulator blockList = new BlockStateListPopulator(world.getWorld());

               for(int i = 0; i < 3; ++i) {
                  BlockWorldState shapedetectorblock = shapedetector_shapedetectorcollection.translateOffset(i, 0, 0);
                  BlockPos pos = shapedetectorblock.getPos();
                  IBlockState data = shapedetectorblock.getBlockState().withProperty(NODROP, Boolean.valueOf(true));
                  blockList.setTypeAndData(pos.getX(), pos.getY(), pos.getZ(), data.getBlock(), data.getBlock().getMetaFromState(data), 2);
               }

               for(int var15 = 0; var15 < shapedetector.getPalmLength(); ++var15) {
                  for(int j = 0; j < shapedetector.getThumbLength(); ++j) {
                     BlockWorldState shapedetectorblock1 = shapedetector_shapedetectorcollection.translateOffset(var15, j, 0);
                     BlockPos pos = shapedetectorblock1.getPos();
                     blockList.setTypeAndData(pos.getX(), pos.getY(), pos.getZ(), Blocks.AIR, 0, 2);
                  }
               }

               BlockPos blockposition1 = shapedetector_shapedetectorcollection.translateOffset(1, 0, 0).getPos();
               EntityWither entitywither = new EntityWither(world);
               BlockPos blockposition2 = shapedetector_shapedetectorcollection.translateOffset(1, 2, 0).getPos();
               entitywither.setLocationAndAngles((double)blockposition2.getX() + 0.5D, (double)blockposition2.getY() + 0.55D, (double)blockposition2.getZ() + 0.5D, shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F, 0.0F);
               entitywither.renderYawOffset = shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F;
               entitywither.ignite();
               Iterator iterator = world.getEntitiesWithinAABB(EntityPlayer.class, entitywither.getEntityBoundingBox().expandXyz(50.0D)).iterator();
               if (world.addEntity(entitywither, SpawnReason.BUILD_WITHER)) {
                  blockList.updateList();

                  while(iterator.hasNext()) {
                     EntityPlayer entityhuman = (EntityPlayer)iterator.next();
                     entityhuman.addStat(AchievementList.SPAWN_WITHER);
                  }

                  for(int k = 0; k < 120; ++k) {
                     world.spawnParticle(EnumParticleTypes.SNOWBALL, (double)blockposition1.getX() + world.rand.nextDouble(), (double)(blockposition1.getY() - 2) + world.rand.nextDouble() * 3.9D, (double)blockposition1.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
                  }

                  for(int var23 = 0; var23 < shapedetector.getPalmLength(); ++var23) {
                     for(int l = 0; l < shapedetector.getThumbLength(); ++l) {
                        BlockWorldState shapedetectorblock2 = shapedetector_shapedetectorcollection.translateOffset(var23, l, 0);
                        world.notifyNeighborsRespectDebug(shapedetectorblock2.getPos(), Blocks.AIR);
                     }
                  }
               }
            }
         }

      }
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(i & 7)).withProperty(NODROP, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((EnumFacing)iblockdata.getValue(FACING)).getIndex();
      if (((Boolean)iblockdata.getValue(NODROP)).booleanValue()) {
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
      return new BlockStateContainer(this, new IProperty[]{FACING, NODROP});
   }

   protected BlockPattern getWitherBasePattern() {
      if (this.witherBasePattern == null) {
         this.witherBasePattern = FactoryBlockPattern.start().aisle("   ", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SOUL_SAND))).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.witherBasePattern;
   }

   protected BlockPattern getWitherPattern() {
      if (this.witherPattern == null) {
         this.witherPattern = FactoryBlockPattern.start().aisle("^^^", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SOUL_SAND))).where('^', IS_WITHER_SKELETON).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.witherPattern;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.UP.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 5;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
