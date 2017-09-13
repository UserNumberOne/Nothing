package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFlowerPot extends BlockContainer {
   public static final PropertyInteger LEGACY_DATA = PropertyInteger.create("legacy_data", 0, 15);
   public static final PropertyEnum CONTENTS = PropertyEnum.create("contents", BlockFlowerPot.EnumFlowerType.class);
   protected static final AxisAlignedBB FLOWER_POT_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

   public BlockFlowerPot() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(CONTENTS, BlockFlowerPot.EnumFlowerType.EMPTY).withProperty(LEGACY_DATA, Integer.valueOf(0)));
   }

   public String getLocalizedName() {
      return I18n.translateToLocal("item.flowerPot.name");
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FLOWER_POT_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
         TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);
         if (tileentityflowerpot == null) {
            return false;
         } else if (tileentityflowerpot.getFlowerPotItem() != null) {
            return false;
         } else {
            Block block = Block.getBlockFromItem(heldItem.getItem());
            if (!this.canContain(block, heldItem.getMetadata())) {
               return false;
            } else {
               tileentityflowerpot.setFlowerPotData(heldItem.getItem(), heldItem.getMetadata());
               tileentityflowerpot.markDirty();
               worldIn.notifyBlockUpdate(pos, state, state, 3);
               playerIn.addStat(StatList.FLOWER_POTTED);
               if (!playerIn.capabilities.isCreativeMode) {
                  --heldItem.stackSize;
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean canContain(@Nullable Block var1, int var2) {
      return blockIn != Blocks.YELLOW_FLOWER && blockIn != Blocks.RED_FLOWER && blockIn != Blocks.CACTUS && blockIn != Blocks.BROWN_MUSHROOM && blockIn != Blocks.RED_MUSHROOM && blockIn != Blocks.SAPLING && blockIn != Blocks.DEADBUSH ? blockIn == Blocks.TALLGRASS && meta == BlockTallGrass.EnumType.FERN.getMeta() : true;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);
      if (tileentityflowerpot != null) {
         ItemStack itemstack = tileentityflowerpot.getFlowerItemStack();
         if (itemstack != null) {
            return itemstack;
         }
      }

      return new ItemStack(Items.FLOWER_POT);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isFullyOpaque();
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.getBlockState(pos.down()).isFullyOpaque()) {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(worldIn, pos, state);
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      super.onBlockHarvested(worldIn, pos, state, player);
      if (player.capabilities.isCreativeMode) {
         TileEntityFlowerPot tileentityflowerpot = this.getTileEntity(worldIn, pos);
         if (tileentityflowerpot != null) {
            tileentityflowerpot.setFlowerPotData((Item)null, 0);
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.FLOWER_POT;
   }

   @Nullable
   private TileEntityFlowerPot getTileEntity(World var1, BlockPos var2) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)tileentity : null;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      Block block = null;
      int i = 0;
      switch(meta) {
      case 1:
         block = Blocks.RED_FLOWER;
         i = BlockFlower.EnumFlowerType.POPPY.getMeta();
         break;
      case 2:
         block = Blocks.YELLOW_FLOWER;
         break;
      case 3:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.OAK.getMetadata();
         break;
      case 4:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.SPRUCE.getMetadata();
         break;
      case 5:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.BIRCH.getMetadata();
         break;
      case 6:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.JUNGLE.getMetadata();
         break;
      case 7:
         block = Blocks.RED_MUSHROOM;
         break;
      case 8:
         block = Blocks.BROWN_MUSHROOM;
         break;
      case 9:
         block = Blocks.CACTUS;
         break;
      case 10:
         block = Blocks.DEADBUSH;
         break;
      case 11:
         block = Blocks.TALLGRASS;
         i = BlockTallGrass.EnumType.FERN.getMeta();
         break;
      case 12:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.ACACIA.getMetadata();
         break;
      case 13:
         block = Blocks.SAPLING;
         i = BlockPlanks.EnumType.DARK_OAK.getMetadata();
      }

      return new TileEntityFlowerPot(Item.getItemFromBlock(block), i);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{CONTENTS, LEGACY_DATA});
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(LEGACY_DATA)).intValue();
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      BlockFlowerPot.EnumFlowerType blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.EMPTY;
      TileEntity tileentity = worldIn instanceof ChunkCache ? ((ChunkCache)worldIn).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityFlowerPot) {
         TileEntityFlowerPot tileentityflowerpot = (TileEntityFlowerPot)tileentity;
         Item item = tileentityflowerpot.getFlowerPotItem();
         if (item instanceof ItemBlock) {
            int i = tileentityflowerpot.getFlowerPotData();
            Block block = Block.getBlockFromItem(item);
            if (block == Blocks.SAPLING) {
               switch(BlockPlanks.EnumType.byMetadata(i)) {
               case OAK:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.OAK_SAPLING;
                  break;
               case SPRUCE:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.SPRUCE_SAPLING;
                  break;
               case BIRCH:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.BIRCH_SAPLING;
                  break;
               case JUNGLE:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.JUNGLE_SAPLING;
                  break;
               case ACACIA:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.ACACIA_SAPLING;
                  break;
               case DARK_OAK:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.DARK_OAK_SAPLING;
                  break;
               default:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (block == Blocks.TALLGRASS) {
               switch(i) {
               case 0:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                  break;
               case 2:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.FERN;
                  break;
               default:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (block == Blocks.YELLOW_FLOWER) {
               blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.DANDELION;
            } else if (block == Blocks.RED_FLOWER) {
               switch(BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, i)) {
               case POPPY:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.POPPY;
                  break;
               case BLUE_ORCHID:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.BLUE_ORCHID;
                  break;
               case ALLIUM:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.ALLIUM;
                  break;
               case HOUSTONIA:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.HOUSTONIA;
                  break;
               case RED_TULIP:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.RED_TULIP;
                  break;
               case ORANGE_TULIP:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.ORANGE_TULIP;
                  break;
               case WHITE_TULIP:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.WHITE_TULIP;
                  break;
               case PINK_TULIP:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.PINK_TULIP;
                  break;
               case OXEYE_DAISY:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.OXEYE_DAISY;
                  break;
               default:
                  blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (block == Blocks.RED_MUSHROOM) {
               blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.MUSHROOM_RED;
            } else if (block == Blocks.BROWN_MUSHROOM) {
               blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.MUSHROOM_BROWN;
            } else if (block == Blocks.DEADBUSH) {
               blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
            } else if (block == Blocks.CACTUS) {
               blockflowerpot$enumflowertype = BlockFlowerPot.EnumFlowerType.CACTUS;
            }
         }
      }

      return state.withProperty(CONTENTS, blockflowerpot$enumflowertype);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      List ret = super.getDrops(world, pos, state, fortune);
      TileEntityFlowerPot te = world.getTileEntity(pos) instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)world.getTileEntity(pos) : null;
      if (te != null && te.getFlowerPotItem() != null) {
         ret.add(new ItemStack(te.getFlowerPotItem(), 1, te.getFlowerPotData()));
      }

      return ret;
   }

   public boolean removedByPlayer(IBlockState var1, World var2, BlockPos var3, EntityPlayer var4, boolean var5) {
      return willHarvest ? true : super.removedByPlayer(state, world, pos, player, willHarvest);
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, TileEntity var5, ItemStack var6) {
      super.harvestBlock(world, player, pos, state, te, tool);
      world.setBlockToAir(pos);
   }

   public static enum EnumFlowerType implements IStringSerializable {
      EMPTY("empty"),
      POPPY("rose"),
      BLUE_ORCHID("blue_orchid"),
      ALLIUM("allium"),
      HOUSTONIA("houstonia"),
      RED_TULIP("red_tulip"),
      ORANGE_TULIP("orange_tulip"),
      WHITE_TULIP("white_tulip"),
      PINK_TULIP("pink_tulip"),
      OXEYE_DAISY("oxeye_daisy"),
      DANDELION("dandelion"),
      OAK_SAPLING("oak_sapling"),
      SPRUCE_SAPLING("spruce_sapling"),
      BIRCH_SAPLING("birch_sapling"),
      JUNGLE_SAPLING("jungle_sapling"),
      ACACIA_SAPLING("acacia_sapling"),
      DARK_OAK_SAPLING("dark_oak_sapling"),
      MUSHROOM_RED("mushroom_red"),
      MUSHROOM_BROWN("mushroom_brown"),
      DEAD_BUSH("dead_bush"),
      FERN("fern"),
      CACTUS("cactus");

      private final String name;

      private EnumFlowerType(String var3) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
