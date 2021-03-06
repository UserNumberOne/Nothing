package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockModelShapes {
   private final Map bakedModelStore = Maps.newIdentityHashMap();
   private final BlockStateMapper blockStateMapper = new BlockStateMapper();
   private final ModelManager modelManager;

   public BlockModelShapes(ModelManager var1) {
      this.modelManager = var1;
      this.registerAllBlocks();
   }

   public BlockStateMapper getBlockStateMapper() {
      return this.blockStateMapper;
   }

   public TextureAtlasSprite getTexture(IBlockState var1) {
      Block var2 = var1.getBlock();
      IBakedModel var3 = this.getModelForState(var1);
      if (var3 == null || var3 == this.modelManager.getMissingModel()) {
         if (var2 == Blocks.WALL_SIGN || var2 == Blocks.STANDING_SIGN || var2 == Blocks.CHEST || var2 == Blocks.TRAPPED_CHEST || var2 == Blocks.STANDING_BANNER || var2 == Blocks.WALL_BANNER) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/planks_oak");
         }

         if (var2 == Blocks.ENDER_CHEST) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/obsidian");
         }

         if (var2 == Blocks.FLOWING_LAVA || var2 == Blocks.LAVA) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/lava_still");
         }

         if (var2 == Blocks.FLOWING_WATER || var2 == Blocks.WATER) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/water_still");
         }

         if (var2 == Blocks.SKULL) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/soul_sand");
         }

         if (var2 == Blocks.BARRIER) {
            return this.modelManager.getTextureMap().getAtlasSprite("minecraft:items/barrier");
         }
      }

      if (var3 == null) {
         var3 = this.modelManager.getMissingModel();
      }

      return var3.getParticleTexture();
   }

   public IBakedModel getModelForState(IBlockState var1) {
      IBakedModel var2 = (IBakedModel)this.bakedModelStore.get(var1);
      if (var2 == null) {
         var2 = this.modelManager.getMissingModel();
      }

      return var2;
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void reloadModels() {
      this.bakedModelStore.clear();

      for(Entry var2 : this.blockStateMapper.putAllStateModelLocations().entrySet()) {
         this.bakedModelStore.put(var2.getKey(), this.modelManager.getModel((ModelResourceLocation)var2.getValue()));
      }

   }

   public void registerBlockWithStateMapper(Block var1, IStateMapper var2) {
      this.blockStateMapper.registerBlockStateMapper(var1, var2);
   }

   public void registerBuiltInBlocks(Block... var1) {
      this.blockStateMapper.registerBuiltInBlocks(var1);
   }

   private void registerAllBlocks() {
      this.registerBuiltInBlocks(Blocks.AIR, Blocks.FLOWING_WATER, Blocks.WATER, Blocks.FLOWING_LAVA, Blocks.LAVA, Blocks.PISTON_EXTENSION, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.STANDING_SIGN, Blocks.SKULL, Blocks.END_PORTAL, Blocks.BARRIER, Blocks.WALL_SIGN, Blocks.WALL_BANNER, Blocks.STANDING_BANNER, Blocks.END_GATEWAY);
      this.registerBlockWithStateMapper(Blocks.STONE, (new StateMap.Builder()).withName(BlockStone.VARIANT).build());
      this.registerBlockWithStateMapper(Blocks.PRISMARINE, (new StateMap.Builder()).withName(BlockPrismarine.VARIANT).build());
      this.registerBlockWithStateMapper(Blocks.LEAVES, (new StateMap.Builder()).withName(BlockOldLeaf.VARIANT).withSuffix("_leaves").ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE).build());
      this.registerBlockWithStateMapper(Blocks.LEAVES2, (new StateMap.Builder()).withName(BlockNewLeaf.VARIANT).withSuffix("_leaves").ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE).build());
      this.registerBlockWithStateMapper(Blocks.CACTUS, (new StateMap.Builder()).ignore(BlockCactus.AGE).build());
      this.registerBlockWithStateMapper(Blocks.REEDS, (new StateMap.Builder()).ignore(BlockReed.AGE).build());
      this.registerBlockWithStateMapper(Blocks.JUKEBOX, (new StateMap.Builder()).ignore(BlockJukebox.HAS_RECORD).build());
      this.registerBlockWithStateMapper(Blocks.COBBLESTONE_WALL, (new StateMap.Builder()).withName(BlockWall.VARIANT).withSuffix("_wall").build());
      this.registerBlockWithStateMapper(Blocks.DOUBLE_PLANT, (new StateMap.Builder()).withName(BlockDoublePlant.VARIANT).ignore(BlockDoublePlant.FACING).build());
      this.registerBlockWithStateMapper(Blocks.OAK_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.SPRUCE_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.BIRCH_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.JUNGLE_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.DARK_OAK_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.ACACIA_FENCE_GATE, (new StateMap.Builder()).ignore(BlockFenceGate.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.TRIPWIRE, (new StateMap.Builder()).ignore(BlockTripWire.DISARMED, BlockTripWire.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.DOUBLE_WOODEN_SLAB, (new StateMap.Builder()).withName(BlockPlanks.VARIANT).withSuffix("_double_slab").build());
      this.registerBlockWithStateMapper(Blocks.WOODEN_SLAB, (new StateMap.Builder()).withName(BlockPlanks.VARIANT).withSuffix("_slab").build());
      this.registerBlockWithStateMapper(Blocks.TNT, (new StateMap.Builder()).ignore(BlockTNT.EXPLODE).build());
      this.registerBlockWithStateMapper(Blocks.FIRE, (new StateMap.Builder()).ignore(BlockFire.AGE).build());
      this.registerBlockWithStateMapper(Blocks.REDSTONE_WIRE, (new StateMap.Builder()).ignore(BlockRedstoneWire.POWER).build());
      this.registerBlockWithStateMapper(Blocks.OAK_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.SPRUCE_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.BIRCH_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.JUNGLE_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.ACACIA_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.DARK_OAK_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.IRON_DOOR, (new StateMap.Builder()).ignore(BlockDoor.POWERED).build());
      this.registerBlockWithStateMapper(Blocks.WOOL, (new StateMap.Builder()).withName(BlockColored.COLOR).withSuffix("_wool").build());
      this.registerBlockWithStateMapper(Blocks.CARPET, (new StateMap.Builder()).withName(BlockColored.COLOR).withSuffix("_carpet").build());
      this.registerBlockWithStateMapper(Blocks.STAINED_HARDENED_CLAY, (new StateMap.Builder()).withName(BlockColored.COLOR).withSuffix("_stained_hardened_clay").build());
      this.registerBlockWithStateMapper(Blocks.STAINED_GLASS_PANE, (new StateMap.Builder()).withName(BlockColored.COLOR).withSuffix("_stained_glass_pane").build());
      this.registerBlockWithStateMapper(Blocks.STAINED_GLASS, (new StateMap.Builder()).withName(BlockColored.COLOR).withSuffix("_stained_glass").build());
      this.registerBlockWithStateMapper(Blocks.SANDSTONE, (new StateMap.Builder()).withName(BlockSandStone.TYPE).build());
      this.registerBlockWithStateMapper(Blocks.RED_SANDSTONE, (new StateMap.Builder()).withName(BlockRedSandstone.TYPE).build());
      this.registerBlockWithStateMapper(Blocks.TALLGRASS, (new StateMap.Builder()).withName(BlockTallGrass.TYPE).build());
      this.registerBlockWithStateMapper(Blocks.BED, (new StateMap.Builder()).ignore(BlockBed.OCCUPIED).build());
      this.registerBlockWithStateMapper(Blocks.YELLOW_FLOWER, (new StateMap.Builder()).withName(Blocks.YELLOW_FLOWER.getTypeProperty()).build());
      this.registerBlockWithStateMapper(Blocks.RED_FLOWER, (new StateMap.Builder()).withName(Blocks.RED_FLOWER.getTypeProperty()).build());
      this.registerBlockWithStateMapper(Blocks.STONE_SLAB, (new StateMap.Builder()).withName(BlockStoneSlab.VARIANT).withSuffix("_slab").build());
      this.registerBlockWithStateMapper(Blocks.STONE_SLAB2, (new StateMap.Builder()).withName(BlockStoneSlabNew.VARIANT).withSuffix("_slab").build());
      this.registerBlockWithStateMapper(Blocks.MONSTER_EGG, (new StateMap.Builder()).withName(BlockSilverfish.VARIANT).withSuffix("_monster_egg").build());
      this.registerBlockWithStateMapper(Blocks.STONEBRICK, (new StateMap.Builder()).withName(BlockStoneBrick.VARIANT).build());
      this.registerBlockWithStateMapper(Blocks.DISPENSER, (new StateMap.Builder()).ignore(BlockDispenser.TRIGGERED).build());
      this.registerBlockWithStateMapper(Blocks.DROPPER, (new StateMap.Builder()).ignore(BlockDropper.TRIGGERED).build());
      this.registerBlockWithStateMapper(Blocks.LOG, (new StateMap.Builder()).withName(BlockOldLog.VARIANT).withSuffix("_log").build());
      this.registerBlockWithStateMapper(Blocks.LOG2, (new StateMap.Builder()).withName(BlockNewLog.VARIANT).withSuffix("_log").build());
      this.registerBlockWithStateMapper(Blocks.PLANKS, (new StateMap.Builder()).withName(BlockPlanks.VARIANT).withSuffix("_planks").build());
      this.registerBlockWithStateMapper(Blocks.SAPLING, (new StateMap.Builder()).withName(BlockSapling.TYPE).withSuffix("_sapling").build());
      this.registerBlockWithStateMapper(Blocks.SAND, (new StateMap.Builder()).withName(BlockSand.VARIANT).build());
      this.registerBlockWithStateMapper(Blocks.HOPPER, (new StateMap.Builder()).ignore(BlockHopper.ENABLED).build());
      this.registerBlockWithStateMapper(Blocks.FLOWER_POT, (new StateMap.Builder()).ignore(BlockFlowerPot.LEGACY_DATA).build());
      this.registerBlockWithStateMapper(Blocks.QUARTZ_BLOCK, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            BlockQuartz.EnumType var2 = (BlockQuartz.EnumType)var1.getValue(BlockQuartz.VARIANT);
            switch(var2) {
            case DEFAULT:
            default:
               return new ModelResourceLocation("quartz_block", "normal");
            case CHISELED:
               return new ModelResourceLocation("chiseled_quartz_block", "normal");
            case LINES_Y:
               return new ModelResourceLocation("quartz_column", "axis=y");
            case LINES_X:
               return new ModelResourceLocation("quartz_column", "axis=x");
            case LINES_Z:
               return new ModelResourceLocation("quartz_column", "axis=z");
            }
         }
      });
      this.registerBlockWithStateMapper(Blocks.DEADBUSH, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            return new ModelResourceLocation("dead_bush", "normal");
         }
      });
      this.registerBlockWithStateMapper(Blocks.PUMPKIN_STEM, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
            if (var1.getValue(BlockStem.FACING) != EnumFacing.UP) {
               var2.remove(BlockStem.AGE);
            }

            return new ModelResourceLocation((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock()), this.getPropertyString(var2));
         }
      });
      this.registerBlockWithStateMapper(Blocks.MELON_STEM, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
            if (var1.getValue(BlockStem.FACING) != EnumFacing.UP) {
               var2.remove(BlockStem.AGE);
            }

            return new ModelResourceLocation((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock()), this.getPropertyString(var2));
         }
      });
      this.registerBlockWithStateMapper(Blocks.DIRT, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
            String var3 = BlockDirt.VARIANT.getName((BlockDirt.DirtType)var2.remove(BlockDirt.VARIANT));
            if (BlockDirt.DirtType.PODZOL != var1.getValue(BlockDirt.VARIANT)) {
               var2.remove(BlockDirt.SNOWY);
            }

            return new ModelResourceLocation(var3, this.getPropertyString(var2));
         }
      });
      this.registerBlockWithStateMapper(Blocks.DOUBLE_STONE_SLAB, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
            String var3 = BlockStoneSlab.VARIANT.getName((BlockStoneSlab.EnumType)var2.remove(BlockStoneSlab.VARIANT));
            var2.remove(BlockStoneSlab.SEAMLESS);
            String var4 = ((Boolean)var1.getValue(BlockStoneSlab.SEAMLESS)).booleanValue() ? "all" : "normal";
            return new ModelResourceLocation(var3 + "_double_slab", var4);
         }
      });
      this.registerBlockWithStateMapper(Blocks.DOUBLE_STONE_SLAB2, new StateMapperBase() {
         protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
            LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
            String var3 = BlockStoneSlabNew.VARIANT.getName((BlockStoneSlabNew.EnumType)var2.remove(BlockStoneSlabNew.VARIANT));
            var2.remove(BlockStoneSlab.SEAMLESS);
            String var4 = ((Boolean)var1.getValue(BlockStoneSlabNew.SEAMLESS)).booleanValue() ? "all" : "normal";
            return new ModelResourceLocation(var3 + "_double_slab", var4);
         }
      });
      ModelLoader.onRegisterAllBlocks(this);
   }
}
