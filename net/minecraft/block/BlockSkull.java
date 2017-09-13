package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
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

public class BlockSkull extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool NODROP = PropertyBool.create("nodrop");
   private static final Predicate IS_WITHER_SKELETON = new Predicate() {
      public boolean apply(@Nullable BlockWorldState var1) {
         return var1.getBlockState() != null && var1.getBlockState().getBlock() == Blocks.SKULL && var1.getTileEntity() instanceof TileEntitySkull && ((TileEntitySkull)var1.getTileEntity()).getSkullType() == 1;
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
      switch((EnumFacing)var1.getValue(FACING)) {
      case UP:
      default:
         return DEFAULT_AABB;
      case NORTH:
         return NORTH_AABB;
      case SOUTH:
         return SOUTH_AABB;
      case WEST:
         return WEST_AABB;
      case EAST:
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

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      if (var4.capabilities.isCreativeMode) {
         var3 = var3.withProperty(NODROP, Boolean.valueOf(true));
         var1.setBlockState(var2, var3, 4);
      }

      this.dropBlockAsItem(var1, var2, var3, 0);
      super.onBlockHarvested(var1, var2, var3, var4);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      ArrayList var5 = new ArrayList();
      if (!((Boolean)var3.getValue(NODROP)).booleanValue()) {
         TileEntity var6 = var1.getTileEntity(var2);
         if (var6 instanceof TileEntitySkull) {
            TileEntitySkull var7 = (TileEntitySkull)var6;
            ItemStack var8 = new ItemStack(Items.SKULL, 1, var7.getSkullType());
            if (var7.getSkullType() == 3 && var7.getPlayerProfile() != null) {
               var8.setTagCompound(new NBTTagCompound());
               NBTTagCompound var9 = new NBTTagCompound();
               NBTUtil.writeGameProfile(var9, var7.getPlayerProfile());
               var8.getTagCompound().setTag("SkullOwner", var9);
            }

            var5.add(var8);
         }
      }

      return var5;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.SKULL;
   }

   public boolean canDispenserPlace(World var1, BlockPos var2, ItemStack var3) {
      return var3.getMetadata() == 1 && var2.getY() >= 2 && var1.getDifficulty() != EnumDifficulty.PEACEFUL && !var1.isRemote ? this.getWitherBasePattern().match(var1, var2) != null : false;
   }

   public void checkWitherSpawn(World var1, BlockPos var2, TileEntitySkull var3) {
      if (var3.getSkullType() == 1 && var2.getY() >= 2 && var1.getDifficulty() != EnumDifficulty.PEACEFUL && !var1.isRemote) {
         BlockPattern var4 = this.getWitherPattern();
         BlockPattern.PatternHelper var5 = var4.match(var1, var2);
         if (var5 != null) {
            for(int var6 = 0; var6 < 3; ++var6) {
               BlockWorldState var7 = var5.translateOffset(var6, 0, 0);
               var1.setBlockState(var7.getPos(), var7.getBlockState().withProperty(NODROP, Boolean.valueOf(true)), 2);
            }

            for(int var12 = 0; var12 < var4.getPalmLength(); ++var12) {
               for(int var14 = 0; var14 < var4.getThumbLength(); ++var14) {
                  BlockWorldState var8 = var5.translateOffset(var12, var14, 0);
                  var1.setBlockState(var8.getPos(), Blocks.AIR.getDefaultState(), 2);
               }
            }

            BlockPos var13 = var5.translateOffset(1, 0, 0).getPos();
            EntityWither var15 = new EntityWither(var1);
            BlockPos var16 = var5.translateOffset(1, 2, 0).getPos();
            var15.setLocationAndAngles((double)var16.getX() + 0.5D, (double)var16.getY() + 0.55D, (double)var16.getZ() + 0.5D, var5.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F, 0.0F);
            var15.renderYawOffset = var5.getForwards().getAxis() == EnumFacing.Axis.X ? 0.0F : 90.0F;
            var15.ignite();

            for(EntityPlayer var10 : var1.getEntitiesWithinAABB(EntityPlayer.class, var15.getEntityBoundingBox().expandXyz(50.0D))) {
               var10.addStat(AchievementList.SPAWN_WITHER);
            }

            var1.spawnEntity(var15);

            for(int var17 = 0; var17 < 120; ++var17) {
               var1.spawnParticle(EnumParticleTypes.SNOWBALL, (double)var13.getX() + var1.rand.nextDouble(), (double)(var13.getY() - 2) + var1.rand.nextDouble() * 3.9D, (double)var13.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

            for(int var18 = 0; var18 < var4.getPalmLength(); ++var18) {
               for(int var19 = 0; var19 < var4.getThumbLength(); ++var19) {
                  BlockWorldState var11 = var5.translateOffset(var18, var19, 0);
                  var1.notifyNeighborsRespectDebug(var11.getPos(), Blocks.AIR);
               }
            }
         }
      }

   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(var1 & 7)).withProperty(NODROP, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getIndex();
      if (((Boolean)var1.getValue(NODROP)).booleanValue()) {
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
}
