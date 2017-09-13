package net.minecraft.client.renderer.block.model;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.ITextureMapPopulator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.registry.RegistryDelegate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ModelBakery {
   protected static final Set LOCATIONS_BUILTIN_TEXTURES = Sets.newHashSet(new ResourceLocation[]{new ResourceLocation("blocks/water_flow"), new ResourceLocation("blocks/water_still"), new ResourceLocation("blocks/lava_flow"), new ResourceLocation("blocks/lava_still"), new ResourceLocation("blocks/water_overlay"), new ResourceLocation("blocks/destroy_stage_0"), new ResourceLocation("blocks/destroy_stage_1"), new ResourceLocation("blocks/destroy_stage_2"), new ResourceLocation("blocks/destroy_stage_3"), new ResourceLocation("blocks/destroy_stage_4"), new ResourceLocation("blocks/destroy_stage_5"), new ResourceLocation("blocks/destroy_stage_6"), new ResourceLocation("blocks/destroy_stage_7"), new ResourceLocation("blocks/destroy_stage_8"), new ResourceLocation("blocks/destroy_stage_9"), new ResourceLocation("items/empty_armor_slot_helmet"), new ResourceLocation("items/empty_armor_slot_chestplate"), new ResourceLocation("items/empty_armor_slot_leggings"), new ResourceLocation("items/empty_armor_slot_boots"), new ResourceLocation("items/empty_armor_slot_shield")});
   private static final Logger LOGGER = LogManager.getLogger();
   protected static final ModelResourceLocation MODEL_MISSING = new ModelResourceLocation("builtin/missing", "missing");
   private static final String MISSING_MODEL_MESH = "{    'textures': {       'particle': 'missingno',       'missingno': 'missingno'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}".replaceAll("'", "\"");
   private static final Map BUILT_IN_MODELS = Maps.newHashMap();
   private static final Joiner JOINER = Joiner.on(" -> ");
   protected final IResourceManager resourceManager;
   protected final Map sprites = Maps.newHashMap();
   private final Map models = Maps.newLinkedHashMap();
   private final Map variants = Maps.newLinkedHashMap();
   private final Map multipartVariantMap = Maps.newLinkedHashMap();
   protected final TextureMap textureMap;
   protected final BlockModelShapes blockModelShapes;
   private final FaceBakery faceBakery = new FaceBakery();
   private final ItemModelGenerator itemModelGenerator = new ItemModelGenerator();
   protected final RegistrySimple bakedRegistry = new RegistrySimple();
   private static final String EMPTY_MODEL_RAW = "{    'elements': [        {   'from': [0, 0, 0],            'to': [16, 16, 16],            'faces': {                'down': {'uv': [0, 0, 16, 16], 'texture': '' }            }        }    ]}".replaceAll("'", "\"");
   protected static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize(EMPTY_MODEL_RAW);
   protected static final ModelBlock MODEL_ENTITY = ModelBlock.deserialize(EMPTY_MODEL_RAW);
   private final Map itemLocations = Maps.newLinkedHashMap();
   private final Map blockDefinitions = Maps.newHashMap();
   private final Map variantNames = Maps.newIdentityHashMap();
   private static Map customVariantNames = Maps.newHashMap();

   public ModelBakery(IResourceManager var1, TextureMap var2, BlockModelShapes var3) {
      this.resourceManager = resourceManagerIn;
      this.textureMap = textureMapIn;
      this.blockModelShapes = blockModelShapesIn;
   }

   public IRegistry setupModelRegistry() {
      this.loadBlocks();
      this.loadVariantItemModels();
      this.loadModelsCheck();
      this.loadSprites();
      this.makeItemModels();
      this.bakeBlockModels();
      this.bakeItemModels();
      return this.bakedRegistry;
   }

   protected void loadBlocks() {
      BlockStateMapper blockstatemapper = this.blockModelShapes.getBlockStateMapper();

      for(Block block : Block.REGISTRY) {
         for(ResourceLocation resourcelocation : blockstatemapper.getBlockstateLocations(block)) {
            try {
               this.loadBlock(blockstatemapper, block, resourcelocation);
            } catch (Exception var7) {
               LOGGER.warn("Unable to load definition " + resourcelocation, var7);
            }
         }
      }

   }

   protected void loadBlock(BlockStateMapper var1, Block var2, final ResourceLocation var3) {
      ModelBlockDefinition modelblockdefinition = this.getModelBlockDefinition(resourcelocation);
      Map map = blockstatemapper.getVariants(block);
      if (modelblockdefinition.hasMultipartData()) {
         Collection collection = Sets.newHashSet(map.values());
         modelblockdefinition.getMultipartData().setStateContainer(block.getBlockState());
         this.registerMultipartVariant(modelblockdefinition, Lists.newArrayList(Iterables.filter(collection, new Predicate() {
            public boolean apply(@Nullable ModelResourceLocation var1) {
               return resourcelocation.equals(p_apply_1_);
            }
         })));
      }

      for(Entry entry : map.entrySet()) {
         ModelResourceLocation modelresourcelocation = (ModelResourceLocation)entry.getValue();
         if (resourcelocation.equals(modelresourcelocation)) {
            try {
               this.registerVariant(modelblockdefinition, modelresourcelocation);
            } catch (RuntimeException var10) {
               if (!modelblockdefinition.hasMultipartData()) {
                  LOGGER.warn("Unable to load variant: " + modelresourcelocation.getVariant() + " from " + modelresourcelocation, var10);
               }
            }
         }
      }

   }

   protected void loadVariantItemModels() {
      this.variants.put(MODEL_MISSING, new VariantList(Lists.newArrayList(new Variant[]{new Variant(new ResourceLocation(MODEL_MISSING.getResourcePath()), ModelRotation.X0_Y0, false, 1)})));
      ResourceLocation resourcelocation = new ResourceLocation("item_frame");
      ModelBlockDefinition modelblockdefinition = this.getModelBlockDefinition(resourcelocation);
      this.registerVariant(modelblockdefinition, new ModelResourceLocation(resourcelocation, "normal"));
      this.registerVariant(modelblockdefinition, new ModelResourceLocation(resourcelocation, "map"));
      this.loadVariantModels();
      this.loadMultipartVariantModels();
      this.loadItemModels();
   }

   protected void registerVariant(ModelBlockDefinition var1, ModelResourceLocation var2) {
      this.variants.put(location, blockstateDefinition.getVariant(location.getVariant()));
   }

   protected ModelBlockDefinition getModelBlockDefinition(ResourceLocation var1) {
      ResourceLocation resourcelocation = this.getBlockstateLocation(location);
      ModelBlockDefinition modelblockdefinition = (ModelBlockDefinition)this.blockDefinitions.get(resourcelocation);
      if (modelblockdefinition == null) {
         modelblockdefinition = this.loadMultipartMBD(location, resourcelocation);
         this.blockDefinitions.put(resourcelocation, modelblockdefinition);
      }

      return modelblockdefinition;
   }

   private ModelBlockDefinition loadMultipartMBD(ResourceLocation var1, ResourceLocation var2) {
      List list = Lists.newArrayList();

      try {
         for(IResource iresource : this.resourceManager.getAllResources(fileIn)) {
            list.add(this.loadModelBlockDefinition(location, iresource));
         }
      } catch (IOException var6) {
         throw new RuntimeException("Encountered an exception when loading model definition of model " + fileIn, var6);
      }

      return new ModelBlockDefinition(list);
   }

   private ModelBlockDefinition loadModelBlockDefinition(ResourceLocation var1, IResource var2) {
      InputStream inputstream = null;

      ModelBlockDefinition lvt_4_1_;
      try {
         inputstream = resource.getInputStream();
         lvt_4_1_ = ModelBlockDefinition.parseFromReader(new InputStreamReader(inputstream, Charsets.UTF_8));
      } catch (Exception var9) {
         throw new RuntimeException("Encountered an exception when loading model definition of '" + location + "' from: '" + resource.getResourceLocation() + "' in resourcepack: '" + resource.getResourcePackName() + "'", var9);
      } finally {
         IOUtils.closeQuietly(inputstream);
      }

      return lvt_4_1_;
   }

   private ResourceLocation getBlockstateLocation(ResourceLocation var1) {
      return new ResourceLocation(location.getResourceDomain(), "blockstates/" + location.getResourcePath() + ".json");
   }

   protected void loadVariantModels() {
      for(Entry entry : this.variants.entrySet()) {
         this.loadVariantList((ModelResourceLocation)entry.getKey(), (VariantList)entry.getValue());
      }

   }

   protected void loadMultipartVariantModels() {
      for(Entry entry : this.multipartVariantMap.entrySet()) {
         ModelResourceLocation modelresourcelocation = (ModelResourceLocation)((Collection)entry.getValue()).iterator().next();

         for(VariantList variantlist : ((ModelBlockDefinition)entry.getKey()).getMultipartVariants()) {
            this.loadVariantList(modelresourcelocation, variantlist);
         }
      }

   }

   protected void loadVariantList(ModelResourceLocation var1, VariantList var2) {
      for(Variant variant : p_188638_2_.getVariantList()) {
         ResourceLocation resourcelocation = variant.getModelLocation();
         if (this.models.get(resourcelocation) == null) {
            try {
               this.models.put(resourcelocation, this.loadModel(resourcelocation));
            } catch (Exception var7) {
               LOGGER.warn("Unable to load block model: '{}' for variant: '{}': {} ", new Object[]{resourcelocation, p_188638_1_, var7});
            }
         }
      }

   }

   protected ModelBlock loadModel(ResourceLocation var1) throws IOException {
      Reader reader = null;
      IResource iresource = null;

      ModelBlock var6;
      try {
         String s = location.getResourcePath();
         if ("builtin/generated".equals(s)) {
            ModelBlock lvt_5_1_ = MODEL_GENERATED;
            return lvt_5_1_;
         }

         if (!"builtin/entity".equals(s)) {
            if (s.startsWith("builtin/")) {
               String s2 = s.substring("builtin/".length());
               String s1 = (String)BUILT_IN_MODELS.get(s2);
               if (s1 == null) {
                  throw new FileNotFoundException(location.toString());
               }

               reader = new StringReader(s1);
            } else {
               iresource = this.resourceManager.getResource(this.getModelLocation(location));
               reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
            }

            ModelBlock lvt_5_1_ = ModelBlock.deserialize(reader);
            lvt_5_1_.name = location.toString();
            ModelBlock var14 = lvt_5_1_;
            return var14;
         }

         ModelBlock lvt_5_1_ = MODEL_ENTITY;
         var6 = lvt_5_1_;
      } finally {
         IOUtils.closeQuietly(reader);
         IOUtils.closeQuietly(iresource);
      }

      return var6;
   }

   protected ResourceLocation getModelLocation(ResourceLocation var1) {
      return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
   }

   protected void loadItemModels() {
      this.registerVariantNames();

      for(Item item : Item.REGISTRY) {
         for(String s : this.getVariantNames(item)) {
            ResourceLocation resourcelocation = this.getItemLocation(s);
            ResourceLocation resourcelocation1 = (ResourceLocation)Item.REGISTRY.getNameForObject(item);
            this.loadItemModel(s, resourcelocation, resourcelocation1);
            if (item.hasCustomProperties()) {
               ModelBlock modelblock = (ModelBlock)this.models.get(resourcelocation);
               if (modelblock != null) {
                  for(ResourceLocation resourcelocation2 : modelblock.getOverrideLocations()) {
                     this.loadItemModel(resourcelocation2.toString(), resourcelocation2, resourcelocation1);
                  }
               }
            }
         }
      }

   }

   private void loadItemModel(String var1, ResourceLocation var2, ResourceLocation var3) {
      this.itemLocations.put(variantName, location);
      if (this.models.get(location) == null) {
         try {
            ModelBlock modelblock = this.loadModel(location);
            this.models.put(location, modelblock);
         } catch (Exception var5) {
            LOGGER.warn("Unable to load item model: '{}' for item: '{}'", new Object[]{location, itemName, var5});
         }
      }

   }

   protected void registerVariantNames() {
      this.variantNames.clear();
      this.variantNames.put(Item.getItemFromBlock(Blocks.STONE), Lists.newArrayList(new String[]{"stone", "granite", "granite_smooth", "diorite", "diorite_smooth", "andesite", "andesite_smooth"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.DIRT), Lists.newArrayList(new String[]{"dirt", "coarse_dirt", "podzol"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.PLANKS), Lists.newArrayList(new String[]{"oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.SAPLING), Lists.newArrayList(new String[]{"oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.SAND), Lists.newArrayList(new String[]{"sand", "red_sand"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.LOG), Lists.newArrayList(new String[]{"oak_log", "spruce_log", "birch_log", "jungle_log"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.LEAVES), Lists.newArrayList(new String[]{"oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.SPONGE), Lists.newArrayList(new String[]{"sponge", "sponge_wet"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.SANDSTONE), Lists.newArrayList(new String[]{"sandstone", "chiseled_sandstone", "smooth_sandstone"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.RED_SANDSTONE), Lists.newArrayList(new String[]{"red_sandstone", "chiseled_red_sandstone", "smooth_red_sandstone"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.TALLGRASS), Lists.newArrayList(new String[]{"dead_bush", "tall_grass", "fern"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.DEADBUSH), Lists.newArrayList(new String[]{"dead_bush"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.WOOL), Lists.newArrayList(new String[]{"black_wool", "red_wool", "green_wool", "brown_wool", "blue_wool", "purple_wool", "cyan_wool", "silver_wool", "gray_wool", "pink_wool", "lime_wool", "yellow_wool", "light_blue_wool", "magenta_wool", "orange_wool", "white_wool"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), Lists.newArrayList(new String[]{"dandelion"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.RED_FLOWER), Lists.newArrayList(new String[]{"poppy", "blue_orchid", "allium", "houstonia", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STONE_SLAB), Lists.newArrayList(new String[]{"stone_slab", "sandstone_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STONE_SLAB2), Lists.newArrayList(new String[]{"red_sandstone_slab"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STAINED_GLASS), Lists.newArrayList(new String[]{"black_stained_glass", "red_stained_glass", "green_stained_glass", "brown_stained_glass", "blue_stained_glass", "purple_stained_glass", "cyan_stained_glass", "silver_stained_glass", "gray_stained_glass", "pink_stained_glass", "lime_stained_glass", "yellow_stained_glass", "light_blue_stained_glass", "magenta_stained_glass", "orange_stained_glass", "white_stained_glass"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.MONSTER_EGG), Lists.newArrayList(new String[]{"stone_monster_egg", "cobblestone_monster_egg", "stone_brick_monster_egg", "mossy_brick_monster_egg", "cracked_brick_monster_egg", "chiseled_brick_monster_egg"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STONEBRICK), Lists.newArrayList(new String[]{"stonebrick", "mossy_stonebrick", "cracked_stonebrick", "chiseled_stonebrick"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.WOODEN_SLAB), Lists.newArrayList(new String[]{"oak_slab", "spruce_slab", "birch_slab", "jungle_slab", "acacia_slab", "dark_oak_slab"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.COBBLESTONE_WALL), Lists.newArrayList(new String[]{"cobblestone_wall", "mossy_cobblestone_wall"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.ANVIL), Lists.newArrayList(new String[]{"anvil_intact", "anvil_slightly_damaged", "anvil_very_damaged"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.QUARTZ_BLOCK), Lists.newArrayList(new String[]{"quartz_block", "chiseled_quartz_block", "quartz_column"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STAINED_HARDENED_CLAY), Lists.newArrayList(new String[]{"black_stained_hardened_clay", "red_stained_hardened_clay", "green_stained_hardened_clay", "brown_stained_hardened_clay", "blue_stained_hardened_clay", "purple_stained_hardened_clay", "cyan_stained_hardened_clay", "silver_stained_hardened_clay", "gray_stained_hardened_clay", "pink_stained_hardened_clay", "lime_stained_hardened_clay", "yellow_stained_hardened_clay", "light_blue_stained_hardened_clay", "magenta_stained_hardened_clay", "orange_stained_hardened_clay", "white_stained_hardened_clay"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE), Lists.newArrayList(new String[]{"black_stained_glass_pane", "red_stained_glass_pane", "green_stained_glass_pane", "brown_stained_glass_pane", "blue_stained_glass_pane", "purple_stained_glass_pane", "cyan_stained_glass_pane", "silver_stained_glass_pane", "gray_stained_glass_pane", "pink_stained_glass_pane", "lime_stained_glass_pane", "yellow_stained_glass_pane", "light_blue_stained_glass_pane", "magenta_stained_glass_pane", "orange_stained_glass_pane", "white_stained_glass_pane"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.LEAVES2), Lists.newArrayList(new String[]{"acacia_leaves", "dark_oak_leaves"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.LOG2), Lists.newArrayList(new String[]{"acacia_log", "dark_oak_log"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.PRISMARINE), Lists.newArrayList(new String[]{"prismarine", "prismarine_bricks", "dark_prismarine"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.CARPET), Lists.newArrayList(new String[]{"black_carpet", "red_carpet", "green_carpet", "brown_carpet", "blue_carpet", "purple_carpet", "cyan_carpet", "silver_carpet", "gray_carpet", "pink_carpet", "lime_carpet", "yellow_carpet", "light_blue_carpet", "magenta_carpet", "orange_carpet", "white_carpet"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), Lists.newArrayList(new String[]{"sunflower", "syringa", "double_grass", "double_fern", "double_rose", "paeonia"}));
      this.variantNames.put(Items.COAL, Lists.newArrayList(new String[]{"coal", "charcoal"}));
      this.variantNames.put(Items.FISH, Lists.newArrayList(new String[]{"cod", "salmon", "clownfish", "pufferfish"}));
      this.variantNames.put(Items.COOKED_FISH, Lists.newArrayList(new String[]{"cooked_cod", "cooked_salmon"}));
      this.variantNames.put(Items.DYE, Lists.newArrayList(new String[]{"dye_black", "dye_red", "dye_green", "dye_brown", "dye_blue", "dye_purple", "dye_cyan", "dye_silver", "dye_gray", "dye_pink", "dye_lime", "dye_yellow", "dye_light_blue", "dye_magenta", "dye_orange", "dye_white"}));
      this.variantNames.put(Items.POTIONITEM, Lists.newArrayList(new String[]{"bottle_drinkable"}));
      this.variantNames.put(Items.SKULL, Lists.newArrayList(new String[]{"skull_skeleton", "skull_wither", "skull_zombie", "skull_char", "skull_creeper", "skull_dragon"}));
      this.variantNames.put(Items.SPLASH_POTION, Lists.newArrayList(new String[]{"bottle_splash"}));
      this.variantNames.put(Items.LINGERING_POTION, Lists.newArrayList(new String[]{"bottle_lingering"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.OAK_FENCE_GATE), Lists.newArrayList(new String[]{"oak_fence_gate"}));
      this.variantNames.put(Item.getItemFromBlock(Blocks.OAK_FENCE), Lists.newArrayList(new String[]{"oak_fence"}));
      this.variantNames.put(Items.OAK_DOOR, Lists.newArrayList(new String[]{"oak_door"}));
      this.variantNames.put(Items.BOAT, Lists.newArrayList(new String[]{"oak_boat"}));

      for(Entry e : customVariantNames.entrySet()) {
         this.variantNames.put(((RegistryDelegate)e.getKey()).get(), Lists.newArrayList(((Set)e.getValue()).iterator()));
      }

   }

   protected List getVariantNames(Item var1) {
      List list = (List)this.variantNames.get(stack);
      if (list == null) {
         list = Collections.singletonList(((ResourceLocation)Item.REGISTRY.getNameForObject(stack)).toString());
      }

      return list;
   }

   protected ResourceLocation getItemLocation(String var1) {
      ResourceLocation resourcelocation = new ResourceLocation(location.replaceAll("#.*", ""));
      return new ResourceLocation(resourcelocation.getResourceDomain(), "item/" + resourcelocation.getResourcePath());
   }

   private void bakeBlockModels() {
      for(ModelResourceLocation modelresourcelocation : this.variants.keySet()) {
         IBakedModel ibakedmodel = this.createRandomModelForVariantList((VariantList)this.variants.get(modelresourcelocation), modelresourcelocation.toString());
         if (ibakedmodel != null) {
            this.bakedRegistry.putObject(modelresourcelocation, ibakedmodel);
         }
      }

      for(Entry entry : this.multipartVariantMap.entrySet()) {
         ModelBlockDefinition modelblockdefinition = (ModelBlockDefinition)entry.getKey();
         Multipart multipart = modelblockdefinition.getMultipartData();
         String s = ((ResourceLocation)Block.REGISTRY.getNameForObject(multipart.getStateContainer().getBlock())).toString();
         MultipartBakedModel.Builder multipartbakedmodel$builder = new MultipartBakedModel.Builder();

         for(Selector selector : multipart.getSelectors()) {
            IBakedModel ibakedmodel1 = this.createRandomModelForVariantList(selector.getVariantList(), "selector of " + s);
            if (ibakedmodel1 != null) {
               multipartbakedmodel$builder.putModel(selector.getPredicate(multipart.getStateContainer()), ibakedmodel1);
            }
         }

         IBakedModel ibakedmodel2 = multipartbakedmodel$builder.makeMultipartModel();

         for(ModelResourceLocation modelresourcelocation1 : (Collection)entry.getValue()) {
            if (!modelblockdefinition.hasVariant(modelresourcelocation1.getVariant())) {
               this.bakedRegistry.putObject(modelresourcelocation1, ibakedmodel2);
            }
         }
      }

   }

   @Nullable
   private IBakedModel createRandomModelForVariantList(VariantList var1, String var2) {
      if (variantsIn.getVariantList().isEmpty()) {
         return null;
      } else {
         WeightedBakedModel.Builder weightedbakedmodel$builder = new WeightedBakedModel.Builder();
         int i = 0;

         for(Variant variant : variantsIn.getVariantList()) {
            ModelBlock modelblock = (ModelBlock)this.models.get(variant.getModelLocation());
            if (modelblock != null && modelblock.isResolved()) {
               if (modelblock.getElements().isEmpty()) {
                  LOGGER.warn("Missing elements for: {}", new Object[]{modelLocation});
               } else {
                  IBakedModel ibakedmodel = this.bakeModel(modelblock, variant.getRotation(), variant.isUvLock());
                  if (ibakedmodel != null) {
                     ++i;
                     weightedbakedmodel$builder.add(ibakedmodel, variant.getWeight());
                  }
               }
            } else {
               LOGGER.warn("Missing model for: {}", new Object[]{modelLocation});
            }
         }

         IBakedModel ibakedmodel1 = null;
         if (i == 0) {
            LOGGER.warn("No weighted models for: {}", new Object[]{modelLocation});
         } else if (i == 1) {
            ibakedmodel1 = weightedbakedmodel$builder.first();
         } else {
            ibakedmodel1 = weightedbakedmodel$builder.build();
         }

         return ibakedmodel1;
      }
   }

   private void bakeItemModels() {
      for(Entry entry : this.itemLocations.entrySet()) {
         ResourceLocation resourcelocation = (ResourceLocation)entry.getValue();
         ModelResourceLocation modelresourcelocation = ModelLoader.getInventoryVariant((String)entry.getKey());
         ModelBlock modelblock = (ModelBlock)this.models.get(resourcelocation);
         if (modelblock != null && modelblock.isResolved()) {
            if (modelblock.getElements().isEmpty()) {
               LOGGER.warn("Missing elements for: {}", new Object[]{resourcelocation});
            } else if (this.isCustomRenderer(modelblock)) {
               this.bakedRegistry.putObject(modelresourcelocation, new BuiltInModel(modelblock.getAllTransforms(), modelblock.createOverrides()));
            } else {
               IBakedModel ibakedmodel = this.bakeModel(modelblock, ModelRotation.X0_Y0, false);
               if (ibakedmodel != null) {
                  this.bakedRegistry.putObject(modelresourcelocation, ibakedmodel);
               }
            }
         } else {
            LOGGER.warn("Missing model for: {}", new Object[]{resourcelocation});
         }
      }

   }

   private Set getVariantsTextureLocations() {
      Set set = Sets.newHashSet();
      List list = Lists.newArrayList(this.variants.keySet());
      Collections.sort(list, new Comparator() {
         public int compare(ModelResourceLocation var1, ModelResourceLocation var2) {
            return p_compare_1_.toString().compareTo(p_compare_2_.toString());
         }
      });

      for(ModelResourceLocation modelresourcelocation : list) {
         VariantList variantlist = (VariantList)this.variants.get(modelresourcelocation);

         for(Variant variant : variantlist.getVariantList()) {
            ModelBlock modelblock = (ModelBlock)this.models.get(variant.getModelLocation());
            if (modelblock == null) {
               LOGGER.warn("Missing model for: {}", new Object[]{modelresourcelocation});
            } else {
               set.addAll(this.getTextureLocations(modelblock));
            }
         }
      }

      for(ModelBlockDefinition modelblockdefinition : this.multipartVariantMap.keySet()) {
         for(VariantList variantlist1 : modelblockdefinition.getMultipartData().getVariants()) {
            for(Variant variant1 : variantlist1.getVariantList()) {
               ModelBlock modelblock1 = (ModelBlock)this.models.get(variant1.getModelLocation());
               if (modelblock1 == null) {
                  LOGGER.warn("Missing model for: {}", new Object[]{Block.REGISTRY.getNameForObject(modelblockdefinition.getMultipartData().getStateContainer().getBlock())});
               } else {
                  set.addAll(this.getTextureLocations(modelblock1));
               }
            }
         }
      }

      set.addAll(LOCATIONS_BUILTIN_TEXTURES);
      return set;
   }

   @Nullable
   private IBakedModel bakeModel(ModelBlock var1, ModelRotation var2, boolean var3) {
      return this.bakeModel(modelBlockIn, (ITransformation)modelRotationIn, uvLocked);
   }

   protected IBakedModel bakeModel(ModelBlock var1, ITransformation var2, boolean var3) {
      TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)this.sprites.get(new ResourceLocation(modelBlockIn.resolveTextureName("particle")));
      SimpleBakedModel.Builder simplebakedmodel$builder = (new SimpleBakedModel.Builder(modelBlockIn, modelBlockIn.createOverrides())).setTexture(textureatlassprite);
      if (modelBlockIn.getElements().isEmpty()) {
         return null;
      } else {
         for(BlockPart blockpart : modelBlockIn.getElements()) {
            for(EnumFacing enumfacing : blockpart.mapFaces.keySet()) {
               BlockPartFace blockpartface = (BlockPartFace)blockpart.mapFaces.get(enumfacing);
               TextureAtlasSprite textureatlassprite1 = (TextureAtlasSprite)this.sprites.get(new ResourceLocation(modelBlockIn.resolveTextureName(blockpartface.texture)));
               if (blockpartface.cullFace != null && TRSRTransformation.isInteger(modelRotationIn.getMatrix())) {
                  simplebakedmodel$builder.addFaceQuad(modelRotationIn.rotate(blockpartface.cullFace), this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing, modelRotationIn, uvLocked));
               } else {
                  simplebakedmodel$builder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing, modelRotationIn, uvLocked));
               }
            }
         }

         return simplebakedmodel$builder.makeBakedModel();
      }
   }

   private BakedQuad makeBakedQuad(BlockPart var1, BlockPartFace var2, TextureAtlasSprite var3, EnumFacing var4, ModelRotation var5, boolean var6) {
      return this.makeBakedQuad(p_177589_1_, p_177589_2_, p_177589_3_, p_177589_4_, (ITransformation)p_177589_5_, p_177589_6_);
   }

   protected BakedQuad makeBakedQuad(BlockPart var1, BlockPartFace var2, TextureAtlasSprite var3, EnumFacing var4, ITransformation var5, boolean var6) {
      return this.faceBakery.makeBakedQuad(p_177589_1_.positionFrom, p_177589_1_.positionTo, p_177589_2_, p_177589_3_, p_177589_4_, p_177589_5_, p_177589_1_.partRotation, p_177589_6_, p_177589_1_.shade);
   }

   private void loadModelsCheck() {
      this.loadModels();

      for(ModelBlock modelblock : this.models.values()) {
         modelblock.getParentFromMap(this.models);
      }

      ModelBlock.checkModelHierarchy(this.models);
   }

   private void loadModels() {
      Deque deque = Queues.newArrayDeque();
      Set set = Sets.newHashSet();

      for(ResourceLocation resourcelocation : this.models.keySet()) {
         set.add(resourcelocation);
         this.addModelParentLocation(deque, set, (ModelBlock)this.models.get(resourcelocation));
      }

      while(!((Deque)deque).isEmpty()) {
         ResourceLocation resourcelocation1 = (ResourceLocation)deque.pop();

         try {
            if (this.models.get(resourcelocation1) != null) {
               continue;
            }

            ModelBlock modelblock = this.loadModel(resourcelocation1);
            this.models.put(resourcelocation1, modelblock);
            this.addModelParentLocation(deque, set, modelblock);
         } catch (Exception var5) {
            LOGGER.warn("In parent chain: {}; unable to load model: '{}'", new Object[]{JOINER.join(this.getParentPath(resourcelocation1)), resourcelocation1, var5});
         }

         set.add(resourcelocation1);
      }

   }

   private void addModelParentLocation(Deque var1, Set var2, ModelBlock var3) {
      ResourceLocation resourcelocation = p_188633_3_.getParentLocation();
      if (resourcelocation != null && !p_188633_2_.contains(resourcelocation)) {
         p_188633_1_.add(resourcelocation);
      }

   }

   private List getParentPath(ResourceLocation var1) {
      List list = Lists.newArrayList(new ResourceLocation[]{p_177573_1_});
      ResourceLocation resourcelocation = p_177573_1_;

      while((resourcelocation = this.getParentLocation(resourcelocation)) != null) {
         list.add(0, resourcelocation);
      }

      return list;
   }

   @Nullable
   private ResourceLocation getParentLocation(ResourceLocation var1) {
      for(Entry entry : this.models.entrySet()) {
         ModelBlock modelblock = (ModelBlock)entry.getValue();
         if (modelblock != null && p_177576_1_.equals(modelblock.getParentLocation())) {
            return (ResourceLocation)entry.getKey();
         }
      }

      return null;
   }

   protected Set getTextureLocations(ModelBlock var1) {
      Set set = Sets.newHashSet();

      for(BlockPart blockpart : p_177585_1_.getElements()) {
         for(BlockPartFace blockpartface : blockpart.mapFaces.values()) {
            ResourceLocation resourcelocation = new ResourceLocation(p_177585_1_.resolveTextureName(blockpartface.texture));
            set.add(resourcelocation);
         }
      }

      set.add(new ResourceLocation(p_177585_1_.resolveTextureName("particle")));
      return set;
   }

   private void loadSprites() {
      final Set set = this.getVariantsTextureLocations();
      set.addAll(this.getItemsTextureLocations());
      set.remove(TextureMap.LOCATION_MISSING_TEXTURE);
      ITextureMapPopulator itexturemappopulator = new ITextureMapPopulator() {
         public void registerSprites(TextureMap var1x) {
            for(ResourceLocation resourcelocation : set) {
               TextureAtlasSprite textureatlassprite = textureMapIn.registerSprite(resourcelocation);
               ModelBakery.this.sprites.put(resourcelocation, textureatlassprite);
            }

         }
      };
      this.textureMap.loadSprites(this.resourceManager, itexturemappopulator);
      this.sprites.put(new ResourceLocation("missingno"), this.textureMap.getMissingSprite());
   }

   private Set getItemsTextureLocations() {
      Set set = Sets.newHashSet();

      for(ResourceLocation resourcelocation : this.itemLocations.values()) {
         ModelBlock modelblock = (ModelBlock)this.models.get(resourcelocation);
         if (modelblock != null) {
            set.add(new ResourceLocation(modelblock.resolveTextureName("particle")));
            if (this.hasItemModel(modelblock)) {
               for(String s : ItemModelGenerator.LAYERS) {
                  set.add(new ResourceLocation(modelblock.resolveTextureName(s)));
               }
            } else if (!this.isCustomRenderer(modelblock)) {
               for(BlockPart blockpart : modelblock.getElements()) {
                  for(BlockPartFace blockpartface : blockpart.mapFaces.values()) {
                     ResourceLocation resourcelocation1 = new ResourceLocation(modelblock.resolveTextureName(blockpartface.texture));
                     set.add(resourcelocation1);
                  }
               }
            }
         }
      }

      return set;
   }

   protected boolean hasItemModel(@Nullable ModelBlock var1) {
      return p_177581_1_ == null ? false : p_177581_1_.getRootModel() == MODEL_GENERATED;
   }

   protected boolean isCustomRenderer(@Nullable ModelBlock var1) {
      if (p_177587_1_ == null) {
         return false;
      } else {
         ModelBlock modelblock = p_177587_1_.getRootModel();
         return modelblock == MODEL_ENTITY;
      }
   }

   private void makeItemModels() {
      for(ResourceLocation resourcelocation : this.itemLocations.values()) {
         ModelBlock modelblock = (ModelBlock)this.models.get(resourcelocation);
         if (this.hasItemModel(modelblock)) {
            ModelBlock modelblock1 = this.makeItemModel(modelblock);
            if (modelblock1 != null) {
               modelblock1.name = resourcelocation.toString();
            }

            this.models.put(resourcelocation, modelblock1);
         } else if (this.isCustomRenderer(modelblock)) {
            this.models.put(resourcelocation, modelblock);
         }
      }

      for(TextureAtlasSprite textureatlassprite : this.sprites.values()) {
         if (!textureatlassprite.hasAnimationMetadata()) {
            textureatlassprite.clearFramesTextureData();
         }
      }

   }

   protected ModelBlock makeItemModel(ModelBlock var1) {
      return this.itemModelGenerator.makeItemModel(this.textureMap, p_177582_1_);
   }

   protected void registerMultipartVariant(ModelBlockDefinition var1, Collection var2) {
      this.multipartVariantMap.put(definition, locations);
   }

   public static void registerItemVariants(Item var0, ResourceLocation... var1) {
      if (!customVariantNames.containsKey(item.delegate)) {
         customVariantNames.put(item.delegate, Sets.newHashSet());
      }

      for(ResourceLocation name : names) {
         ((Set)customVariantNames.get(item.delegate)).add(name.toString());
      }

   }

   static {
      BUILT_IN_MODELS.put("missing", MISSING_MODEL_MESH);
      MODEL_GENERATED.name = "generation marker";
      MODEL_ENTITY.name = "block entity marker";
   }
}
