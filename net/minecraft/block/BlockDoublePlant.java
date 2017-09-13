package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDoublePlant extends BlockBush implements IGrowable, IShearable {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockDoublePlant.EnumPlantType.class);
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockDoublePlant.EnumBlockHalf.class);
   public static final PropertyEnum FACING = BlockHorizontal.FACING;

   public BlockDoublePlant() {
      super(Material.VINE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockDoublePlant.EnumPlantType.SUNFLOWER).withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER).withProperty(FACING, EnumFacing.NORTH));
      this.setHardness(0.0F);
      this.setSoundType(SoundType.PLANT);
      this.setUnlocalizedName("doublePlant");
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return FULL_BLOCK_AABB;
   }

   private BlockDoublePlant.EnumPlantType getType(IBlockAccess blockAccess, BlockPos pos, IBlockState state) {
      if (state.getBlock() == this) {
         state = state.getActualState(blockAccess, pos);
         return (BlockDoublePlant.EnumPlantType)state.getValue(VARIANT);
      } else {
         return BlockDoublePlant.EnumPlantType.FERN;
      }
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
   }

   public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() != this) {
         return true;
      } else {
         BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType)iblockstate.getActualState(worldIn, pos).getValue(VARIANT);
         return blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.FERN || blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.GRASS;
      }
   }

   protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
      if (!this.canBlockStay(worldIn, pos, state)) {
         boolean flag = state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER;
         BlockPos blockpos = flag ? pos : pos.up();
         BlockPos blockpos1 = flag ? pos.down() : pos;
         Block block = (Block)(flag ? this : worldIn.getBlockState(blockpos).getBlock());
         Block block1 = (Block)(flag ? worldIn.getBlockState(blockpos1).getBlock() : this);
         if (!flag) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
         }

         if (block == this) {
            worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
         }

         if (block1 == this) {
            worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);
         }
      }

   }

   public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
      if (state.getBlock() != this) {
         return super.canBlockStay(worldIn, pos, state);
      } else if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
         return worldIn.getBlockState(pos.down()).getBlock() == this;
      } else {
         IBlockState iblockstate = worldIn.getBlockState(pos.up());
         return iblockstate.getBlock() == this && super.canBlockStay(worldIn, pos, iblockstate);
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
         return null;
      } else {
         BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType)state.getValue(VARIANT);
         return blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.FERN ? null : (blockdoubleplant$enumplanttype == BlockDoublePlant.EnumPlantType.GRASS ? (rand.nextInt(8) == 0 ? Items.WHEAT_SEEDS : null) : Item.getItemFromBlock(this));
      }
   }

   public int damageDropped(IBlockState state) {
      return state.getValue(HALF) != BlockDoublePlant.EnumBlockHalf.UPPER && state.getValue(VARIANT) != BlockDoublePlant.EnumPlantType.GRASS ? ((BlockDoublePlant.EnumPlantType)state.getValue(VARIANT)).getMeta() : 0;
   }

   public void placeAt(World worldIn, BlockPos lowerPos, BlockDoublePlant.EnumPlantType variant, int flags) {
      worldIn.setBlockState(lowerPos, this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER).withProperty(VARIANT, variant), flags);
      worldIn.setBlockState(lowerPos.up(), this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER), flags);
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      worldIn.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
   }

   public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) {
      super.harvestBlock(worldIn, player, pos, state, te, stack);
   }

   public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
      if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
         if (worldIn.getBlockState(pos.down()).getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
               worldIn.setBlockToAir(pos.down());
            } else {
               IBlockState iblockstate = worldIn.getBlockState(pos.down());
               BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType)iblockstate.getValue(VARIANT);
               if (blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS) {
                  worldIn.destroyBlock(pos.down(), true);
               } else if (worldIn.isRemote) {
                  worldIn.setBlockToAir(pos.down());
               } else if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == Items.SHEARS) {
                  this.onHarvest(worldIn, pos, iblockstate, player);
                  worldIn.setBlockToAir(pos.down());
               } else {
                  worldIn.destroyBlock(pos.down(), true);
               }
            }
         }
      } else if (worldIn.getBlockState(pos.up()).getBlock() == this) {
         worldIn.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), 2);
      }

      super.onBlockHarvested(worldIn, pos, state, player);
   }

   private boolean onHarvest(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
      BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = (BlockDoublePlant.EnumPlantType)state.getValue(VARIANT);
      if (blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS) {
         return false;
      } else {
         player.addStat(StatList.getBlockStats(this));
         return true;
      }
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
      for(BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype : BlockDoublePlant.EnumPlantType.values()) {
         list.add(new ItemStack(itemIn, 1, blockdoubleplant$enumplanttype.getMeta()));
      }

   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(this, 1, this.getType(worldIn, pos, state).getMeta());
   }

   public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype = this.getType(worldIn, pos, state);
      return blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.GRASS && blockdoubleplant$enumplanttype != BlockDoublePlant.EnumPlantType.FERN;
   }

   public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      spawnAsEntity(worldIn, pos, new ItemStack(this, 1, this.getType(worldIn, pos, state).getMeta()));
   }

   public IBlockState getStateFromMeta(int meta) {
      return (meta & 8) > 0 ? this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.UPPER) : this.getDefaultState().withProperty(HALF, BlockDoublePlant.EnumBlockHalf.LOWER).withProperty(VARIANT, BlockDoublePlant.EnumPlantType.byMetadata(meta & 7));
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      if (state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
         IBlockState iblockstate = worldIn.getBlockState(pos.down());
         if (iblockstate.getBlock() == this) {
            state = state.withProperty(VARIANT, iblockstate.getValue(VARIANT));
         }
      }

      return state;
   }

   public int getMetaFromState(IBlockState state) {
      return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER ? 8 | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex() : ((BlockDoublePlant.EnumPlantType)state.getValue(VARIANT)).getMeta();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HALF, VARIANT, FACING});
   }

   @SideOnly(Side.CLIENT)
   public Block.EnumOffsetType getOffsetType() {
      return Block.EnumOffsetType.XZ;
   }

   public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      BlockDoublePlant.EnumPlantType type = (BlockDoublePlant.EnumPlantType)state.getValue(VARIANT);
      return state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER && (type == BlockDoublePlant.EnumPlantType.FERN || type == BlockDoublePlant.EnumPlantType.GRASS);
   }

   public List onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      List ret = new ArrayList();
      BlockDoublePlant.EnumPlantType type = (BlockDoublePlant.EnumPlantType)world.getBlockState(pos).getValue(VARIANT);
      if (type == BlockDoublePlant.EnumPlantType.FERN) {
         ret.add(new ItemStack(Blocks.TALLGRASS, 2, BlockTallGrass.EnumType.FERN.getMeta()));
      }

      if (type == BlockDoublePlant.EnumPlantType.GRASS) {
         ret.add(new ItemStack(Blocks.TALLGRASS, 2, BlockTallGrass.EnumType.GRASS.getMeta()));
      }

      return ret;
   }

   public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
      if (state.getBlock() == this && state.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER && world.getBlockState(pos.up()).getBlock() == this) {
         world.setBlockToAir(pos.up());
      }

      return world.setBlockToAir(pos);
   }

   public static enum EnumBlockHalf implements IStringSerializable {
      UPPER,
      LOWER;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == UPPER ? "upper" : "lower";
      }
   }

   public static enum EnumPlantType implements IStringSerializable {
      SUNFLOWER(0, "sunflower"),
      SYRINGA(1, "syringa"),
      GRASS(2, "double_grass", "grass"),
      FERN(3, "double_fern", "fern"),
      ROSE(4, "double_rose", "rose"),
      PAEONIA(5, "paeonia");

      private static final BlockDoublePlant.EnumPlantType[] META_LOOKUP = new BlockDoublePlant.EnumPlantType[values().length];
      private final int meta;
      private final String name;
      private final String unlocalizedName;

      private EnumPlantType(int meta, String name) {
         this(meta, name, name);
      }

      private EnumPlantType(int meta, String name, String unlocalizedName) {
         this.meta = meta;
         this.name = name;
         this.unlocalizedName = unlocalizedName;
      }

      public int getMeta() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockDoublePlant.EnumPlantType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         for(BlockDoublePlant.EnumPlantType blockdoubleplant$enumplanttype : values()) {
            META_LOOKUP[blockdoubleplant$enumplanttype.getMeta()] = blockdoubleplant$enumplanttype;
         }

      }
   }
}
