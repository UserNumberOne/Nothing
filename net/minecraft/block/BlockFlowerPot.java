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
      if (var6 != null && var6.getItem() instanceof ItemBlock) {
         TileEntityFlowerPot var11 = this.getTileEntity(var1, var2);
         if (var11 == null) {
            return false;
         } else if (var11.getFlowerPotItem() != null) {
            return false;
         } else {
            Block var12 = Block.getBlockFromItem(var6.getItem());
            if (!this.canContain(var12, var6.getMetadata())) {
               return false;
            } else {
               var11.setFlowerPotData(var6.getItem(), var6.getMetadata());
               var11.markDirty();
               var1.notifyBlockUpdate(var2, var3, var3, 3);
               var4.addStat(StatList.FLOWER_POTTED);
               if (!var4.capabilities.isCreativeMode) {
                  --var6.stackSize;
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean canContain(@Nullable Block var1, int var2) {
      return var1 != Blocks.YELLOW_FLOWER && var1 != Blocks.RED_FLOWER && var1 != Blocks.CACTUS && var1 != Blocks.BROWN_MUSHROOM && var1 != Blocks.RED_MUSHROOM && var1 != Blocks.SAPLING && var1 != Blocks.DEADBUSH ? var1 == Blocks.TALLGRASS && var2 == BlockTallGrass.EnumType.FERN.getMeta() : true;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      TileEntityFlowerPot var4 = this.getTileEntity(var1, var2);
      if (var4 != null) {
         ItemStack var5 = var4.getFlowerItemStack();
         if (var5 != null) {
            return var5;
         }
      }

      return new ItemStack(Items.FLOWER_POT);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return super.canPlaceBlockAt(var1, var2) && var1.getBlockState(var2.down()).isFullyOpaque();
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.getBlockState(var3.down()).isFullyOpaque()) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      super.onBlockHarvested(var1, var2, var3, var4);
      if (var4.capabilities.isCreativeMode) {
         TileEntityFlowerPot var5 = this.getTileEntity(var1, var2);
         if (var5 != null) {
            var5.setFlowerPotData((Item)null, 0);
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.FLOWER_POT;
   }

   @Nullable
   private TileEntityFlowerPot getTileEntity(World var1, BlockPos var2) {
      TileEntity var3 = var1.getTileEntity(var2);
      return var3 instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)var3 : null;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      Object var3 = null;
      int var4 = 0;
      switch(var2) {
      case 1:
         var3 = Blocks.RED_FLOWER;
         var4 = BlockFlower.EnumFlowerType.POPPY.getMeta();
         break;
      case 2:
         var3 = Blocks.YELLOW_FLOWER;
         break;
      case 3:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.OAK.getMetadata();
         break;
      case 4:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.SPRUCE.getMetadata();
         break;
      case 5:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.BIRCH.getMetadata();
         break;
      case 6:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.JUNGLE.getMetadata();
         break;
      case 7:
         var3 = Blocks.RED_MUSHROOM;
         break;
      case 8:
         var3 = Blocks.BROWN_MUSHROOM;
         break;
      case 9:
         var3 = Blocks.CACTUS;
         break;
      case 10:
         var3 = Blocks.DEADBUSH;
         break;
      case 11:
         var3 = Blocks.TALLGRASS;
         var4 = BlockTallGrass.EnumType.FERN.getMeta();
         break;
      case 12:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.ACACIA.getMetadata();
         break;
      case 13:
         var3 = Blocks.SAPLING;
         var4 = BlockPlanks.EnumType.DARK_OAK.getMetadata();
      }

      return new TileEntityFlowerPot(Item.getItemFromBlock((Block)var3), var4);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{CONTENTS, LEGACY_DATA});
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(LEGACY_DATA)).intValue();
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      BlockFlowerPot.EnumFlowerType var4 = BlockFlowerPot.EnumFlowerType.EMPTY;
      TileEntity var5 = var2 instanceof ChunkCache ? ((ChunkCache)var2).getTileEntity(var3, Chunk.EnumCreateEntityType.CHECK) : var2.getTileEntity(var3);
      if (var5 instanceof TileEntityFlowerPot) {
         TileEntityFlowerPot var6 = (TileEntityFlowerPot)var5;
         Item var7 = var6.getFlowerPotItem();
         if (var7 instanceof ItemBlock) {
            int var8 = var6.getFlowerPotData();
            Block var9 = Block.getBlockFromItem(var7);
            if (var9 == Blocks.SAPLING) {
               switch(BlockPlanks.EnumType.byMetadata(var8)) {
               case OAK:
                  var4 = BlockFlowerPot.EnumFlowerType.OAK_SAPLING;
                  break;
               case SPRUCE:
                  var4 = BlockFlowerPot.EnumFlowerType.SPRUCE_SAPLING;
                  break;
               case BIRCH:
                  var4 = BlockFlowerPot.EnumFlowerType.BIRCH_SAPLING;
                  break;
               case JUNGLE:
                  var4 = BlockFlowerPot.EnumFlowerType.JUNGLE_SAPLING;
                  break;
               case ACACIA:
                  var4 = BlockFlowerPot.EnumFlowerType.ACACIA_SAPLING;
                  break;
               case DARK_OAK:
                  var4 = BlockFlowerPot.EnumFlowerType.DARK_OAK_SAPLING;
                  break;
               default:
                  var4 = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (var9 == Blocks.TALLGRASS) {
               switch(var8) {
               case 0:
                  var4 = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
                  break;
               case 2:
                  var4 = BlockFlowerPot.EnumFlowerType.FERN;
                  break;
               default:
                  var4 = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (var9 == Blocks.YELLOW_FLOWER) {
               var4 = BlockFlowerPot.EnumFlowerType.DANDELION;
            } else if (var9 == Blocks.RED_FLOWER) {
               switch(BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, var8)) {
               case POPPY:
                  var4 = BlockFlowerPot.EnumFlowerType.POPPY;
                  break;
               case BLUE_ORCHID:
                  var4 = BlockFlowerPot.EnumFlowerType.BLUE_ORCHID;
                  break;
               case ALLIUM:
                  var4 = BlockFlowerPot.EnumFlowerType.ALLIUM;
                  break;
               case HOUSTONIA:
                  var4 = BlockFlowerPot.EnumFlowerType.HOUSTONIA;
                  break;
               case RED_TULIP:
                  var4 = BlockFlowerPot.EnumFlowerType.RED_TULIP;
                  break;
               case ORANGE_TULIP:
                  var4 = BlockFlowerPot.EnumFlowerType.ORANGE_TULIP;
                  break;
               case WHITE_TULIP:
                  var4 = BlockFlowerPot.EnumFlowerType.WHITE_TULIP;
                  break;
               case PINK_TULIP:
                  var4 = BlockFlowerPot.EnumFlowerType.PINK_TULIP;
                  break;
               case OXEYE_DAISY:
                  var4 = BlockFlowerPot.EnumFlowerType.OXEYE_DAISY;
                  break;
               default:
                  var4 = BlockFlowerPot.EnumFlowerType.EMPTY;
               }
            } else if (var9 == Blocks.RED_MUSHROOM) {
               var4 = BlockFlowerPot.EnumFlowerType.MUSHROOM_RED;
            } else if (var9 == Blocks.BROWN_MUSHROOM) {
               var4 = BlockFlowerPot.EnumFlowerType.MUSHROOM_BROWN;
            } else if (var9 == Blocks.DEADBUSH) {
               var4 = BlockFlowerPot.EnumFlowerType.DEAD_BUSH;
            } else if (var9 == Blocks.CACTUS) {
               var4 = BlockFlowerPot.EnumFlowerType.CACTUS;
            }
         }
      }

      return var1.withProperty(CONTENTS, var4);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      List var5 = super.getDrops(var1, var2, var3, var4);
      TileEntityFlowerPot var6 = var1.getTileEntity(var2) instanceof TileEntityFlowerPot ? (TileEntityFlowerPot)var1.getTileEntity(var2) : null;
      if (var6 != null && var6.getFlowerPotItem() != null) {
         var5.add(new ItemStack(var6.getFlowerPotItem(), 1, var6.getFlowerPotData()));
      }

      return var5;
   }

   public boolean removedByPlayer(IBlockState var1, World var2, BlockPos var3, EntityPlayer var4, boolean var5) {
      return var5 ? true : super.removedByPlayer(var1, var2, var3, var4, var5);
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, TileEntity var5, ItemStack var6) {
      super.harvestBlock(var1, var2, var3, var4, var5, var6);
      var1.setBlockToAir(var3);
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
         this.name = var3;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
