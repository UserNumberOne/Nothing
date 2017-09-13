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
      public boolean apply(@Nullable BlockWorldState var1) {
         return var1.getBlockState() != null && var1.getBlockState().getBlock() == Blocks.SKULL && var1.getTileEntity() instanceof TileEntitySkull && ((TileEntitySkull)var1.getTileEntity()).getSkullType() == 1;
      }

      public boolean apply(Object var1) {
         return this.apply((BlockWorldState)var1);
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

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch(BlockSkull.SyntheticClass_1.a[((EnumFacing)var1.getValue(FACING)).ordinal()]) {
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

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing()).withProperty(NODROP, Boolean.valueOf(false));
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntitySkull();
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      int var4 = 0;
      TileEntity var5 = var1.getTileEntity(var2);
      if (var5 instanceof TileEntitySkull) {
         var4 = ((TileEntitySkull)var5).getSkullType();
      }

      return new ItemStack(Items.SKULL, 1, var4);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (var1.rand.nextFloat() < var4) {
         TileEntitySkull var6 = (TileEntitySkull)var1.getTileEntity(var2);
         ItemStack var7 = this.getItem(var1, var2, var3);
         if (var6.getSkullType() == 3 && var6.getPlayerProfile() != null) {
            var7.setTagCompound(new NBTTagCompound());
            NBTTagCompound var8 = new NBTTagCompound();
            NBTUtil.writeGameProfile(var8, var6.getPlayerProfile());
            var7.getTagCompound().setTag("SkullOwner", var8);
         }

         spawnAsEntity(var1, var2, var7);
      }

   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (var4.capabilities.isCreativeMode) {
         var3 = var3.withProperty(NODROP, Boolean.valueOf(true));
         var1.setBlockState(var2, var3, 4);
      }

      super.onBlockHarvested(var1, var2, var3, var4);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         super.breakBlock(var1, var2, var3);
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.SKULL;
   }

   public boolean canDispenserPlace(World var1, BlockPos var2, ItemStack var3) {
      return var3.getMetadata() == 1 && var2.getY() >= 2 && var1.getDifficulty() != EnumDifficulty.PEACEFUL && !var1.isRemote ? this.getWitherBasePattern().match(var1, var2) != null : false;
   }

   public void checkWitherSpawn(World var1, BlockPos var2, TileEntitySkull var3) {
      if (!var1.captureBlockStates) {
         if (var3.getSkullType() == 1 && var2.getY() >= 2 && var1.getDifficulty() != EnumDifficulty.PEACEFUL && !var1.isRemote) {
            BlockPattern var4 = this.getWitherPattern();
            BlockPattern.PatternHelper var5 = var4.match(var1, var2);
            if (var5 != null) {
               BlockStateListPopulator var6 = new BlockStateListPopulator(var1.getWorld());

               for(int var7 = 0; var7 < 3; ++var7) {
                  BlockWorldState var8 = var5.translateOffset(var7, 0, 0);
                  BlockPos var9 = var8.getPos();
                  IBlockState var10 = var8.getBlockState().withProperty(NODROP, Boolean.valueOf(true));
                  var6.setTypeAndData(var9.getX(), var9.getY(), var9.getZ(), var10.getBlock(), var10.getBlock().getMetaFromState(var10), 2);
               }

               for(int var15 = 0; var15 < var4.getPalmLength(); ++var15) {
                  for(int var16 = 0; var16 < var4.getThumbLength(); ++var16) {
                     BlockWorldState var18 = var5.translateOffset(var15, var16, 0);
                     BlockPos var20 = var18.getPos();
                     var6.setTypeAndData(var20.getX(), var20.getY(), var20.getZ(), Blocks.AIR, 0, 2);
                  }
               }

               BlockPos var17 = var5.translateOffset(1, 0, 0).getPos();
               EntityWither var19 = new EntityWither(var1);
               BlockPos var21 = var5.translateOffset(1, 2, 0).getPos();
               var19.setLocationAndAngles((double)var21.getX() + 0.5D, (double)var21.getY() + 0.55D, (double)var21.getZ() + 0.5D, var5.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F, 0.0F);
               var19.renderYawOffset = var5.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F;
               var19.ignite();
               Iterator var11 = var1.getEntitiesWithinAABB(EntityPlayer.class, var19.getEntityBoundingBox().expandXyz(50.0D)).iterator();
               if (var1.addEntity(var19, SpawnReason.BUILD_WITHER)) {
                  var6.updateList();

                  while(var11.hasNext()) {
                     EntityPlayer var12 = (EntityPlayer)var11.next();
                     var12.addStat(AchievementList.SPAWN_WITHER);
                  }

                  for(int var22 = 0; var22 < 120; ++var22) {
                     var1.spawnParticle(EnumParticleTypes.SNOWBALL, (double)var17.getX() + var1.rand.nextDouble(), (double)(var17.getY() - 2) + var1.rand.nextDouble() * 3.9D, (double)var17.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
                  }

                  for(int var23 = 0; var23 < var4.getPalmLength(); ++var23) {
                     for(int var13 = 0; var13 < var4.getThumbLength(); ++var13) {
                        BlockWorldState var14 = var5.translateOffset(var23, var13, 0);
                        var1.notifyNeighborsRespectDebug(var14.getPos(), Blocks.AIR);
                     }
                  }
               }
            }
         }

      }
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(var1 & 7)).withProperty(NODROP, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((EnumFacing)var1.getValue(FACING)).getIndex();
      if (((Boolean)var1.getValue(NODROP)).booleanValue()) {
         var3 |= 8;
      }

      return var3;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
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
