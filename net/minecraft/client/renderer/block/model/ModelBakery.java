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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
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
      this.resourceManager = var1;
      this.textureMap = var2;
      this.blockModelShapes = var3;
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
      BlockStateMapper var1 = this.blockModelShapes.getBlockStateMapper();

      for(Block var3 : Block.REGISTRY) {
         for(ResourceLocation var5 : var1.getBlockstateLocations(var3)) {
            try {
               this.loadBlock(var1, var3, var5);
            } catch (Exception var7) {
               LOGGER.warn("Unable to load definition " + var5, var7);
            }
         }
      }

   }

   protected void loadBlock(BlockStateMapper var1, Block var2, final ResourceLocation var3) {
      ModelBlockDefinition var4 = this.getModelBlockDefinition(var3);
      Map var5 = var1.getVariants(var2);
      if (var4.hasMultipartData()) {
         HashSet var6 = Sets.newHashSet(var5.values());
         var4.getMultipartData().setStateContainer(var2.getBlockState());
         this.registerMultipartVariant(var4, Lists.newArrayList(Iterables.filter(var6, new Predicate() {
            public boolean apply(@Nullable ModelResourceLocation var1) {
               return var3.equals(var1);
            }
         })));
      }

      for(Entry var7 : var5.entrySet()) {
         ModelResourceLocation var8 = (ModelResourceLocation)var7.getValue();
         if (var3.equals(var8)) {
            try {
               this.registerVariant(var4, var8);
            } catch (RuntimeException var10) {
               if (!var4.hasMultipartData()) {
                  LOGGER.warn("Unable to load variant: " + var8.getVariant() + " from " + var8, var10);
               }
            }
         }
      }

   }

   protected void loadVariantItemModels() {
      this.variants.put(MODEL_MISSING, new VariantList(Lists.newArrayList(new Variant[]{new Variant(new ResourceLocation(MODEL_MISSING.getResourcePath()), ModelRotation.X0_Y0, false, 1)})));
      ResourceLocation var1 = new ResourceLocation("item_frame");
      ModelBlockDefinition var2 = this.getModelBlockDefinition(var1);
      this.registerVariant(var2, new ModelResourceLocation(var1, "normal"));
      this.registerVariant(var2, new ModelResourceLocation(var1, "map"));
      this.loadVariantModels();
      this.loadMultipartVariantModels();
      this.loadItemModels();
   }

   protected void registerVariant(ModelBlockDefinition var1, ModelResourceLocation var2) {
      this.variants.put(var2, var1.getVariant(var2.getVariant()));
   }

   protected ModelBlockDefinition getModelBlockDefinition(ResourceLocation var1) {
      ResourceLocation var2 = this.getBlockstateLocation(var1);
      ModelBlockDefinition var3 = (ModelBlockDefinition)this.blockDefinitions.get(var2);
      if (var3 == null) {
         var3 = this.loadMultipartMBD(var1, var2);
         this.blockDefinitions.put(var2, var3);
      }

      return var3;
   }

   private ModelBlockDefinition loadMultipartMBD(ResourceLocation var1, ResourceLocation var2) {
      ArrayList var3 = Lists.newArrayList();

      try {
         for(IResource var5 : this.resourceManager.getAllResources(var2)) {
            var3.add(this.loadModelBlockDefinition(var1, var5));
         }
      } catch (IOException var6) {
         throw new RuntimeException("Encountered an exception when loading model definition of model " + var2, var6);
      }

      return new ModelBlockDefinition(var3);
   }

   private ModelBlockDefinition loadModelBlockDefinition(ResourceLocation var1, IResource var2) {
      InputStream var3 = null;

      ModelBlockDefinition var4;
      try {
         var3 = var2.getInputStream();
         var4 = ModelBlockDefinition.parseFromReader(new InputStreamReader(var3, Charsets.UTF_8));
      } catch (Exception var9) {
         throw new RuntimeException("Encountered an exception when loading model definition of '" + var1 + "' from: '" + var2.getResourceLocation() + "' in resourcepack: '" + var2.getResourcePackName() + "'", var9);
      } finally {
         IOUtils.closeQuietly(var3);
      }

      return var4;
   }

   private ResourceLocation getBlockstateLocation(ResourceLocation var1) {
      return new ResourceLocation(var1.getResourceDomain(), "blockstates/" + var1.getResourcePath() + ".json");
   }

   protected void loadVariantModels() {
      for(Entry var2 : this.variants.entrySet()) {
         this.loadVariantList((ModelResourceLocation)var2.getKey(), (VariantList)var2.getValue());
      }

   }

   protected void loadMultipartVariantModels() {
      for(Entry var2 : this.multipartVariantMap.entrySet()) {
         ModelResourceLocation var3 = (ModelResourceLocation)((Collection)var2.getValue()).iterator().next();

         for(VariantList var5 : ((ModelBlockDefinition)var2.getKey()).getMultipartVariants()) {
            this.loadVariantList(var3, var5);
         }
      }

   }

   protected void loadVariantList(ModelResourceLocation var1, VariantList var2) {
      for(Variant var4 : var2.getVariantList()) {
         ResourceLocation var5 = var4.getModelLocation();
         if (this.models.get(var5) == null) {
            try {
               this.models.put(var5, this.loadModel(var5));
            } catch (Exception var7) {
               LOGGER.warn("Unable to load block model: '{}' for variant: '{}': {} ", new Object[]{var5, var1, var7});
            }
         }
      }

   }

   protected ModelBlock loadModel(ResourceLocation var1) throws IOException {
      Object var2 = null;
      IResource var3 = null;

      ModelBlock var6;
      try {
         String var5 = var1.getResourcePath();
         if ("builtin/generated".equals(var5)) {
            ModelBlock var12 = MODEL_GENERATED;
            return var12;
         }

         if (!"builtin/entity".equals(var5)) {
            if (var5.startsWith("builtin/")) {
               String var13 = var5.substring("builtin/".length());
               String var7 = (String)BUILT_IN_MODELS.get(var13);
               if (var7 == null) {
                  throw new FileNotFoundException(var1.toString());
               }

               var2 = new StringReader(var7);
            } else {
               var3 = this.resourceManager.getResource(this.getModelLocation(var1));
               var2 = new InputStreamReader(var3.getInputStream(), Charsets.UTF_8);
            }

            ModelBlock var11 = ModelBlock.deserialize((Reader)var2);
            var11.name = var1.toString();
            ModelBlock var14 = var11;
            return var14;
         }

         ModelBlock var4 = MODEL_ENTITY;
         var6 = var4;
      } finally {
         IOUtils.closeQuietly((Reader)var2);
         IOUtils.closeQuietly(var3);
      }

      return var6;
   }

   protected ResourceLocation getModelLocation(ResourceLocation var1) {
      return new ResourceLocation(var1.getResourceDomain(), "models/" + var1.getResourcePath() + ".json");
   }

   protected void loadItemModels() {
      this.registerVariantNames();

      for(Item var2 : Item.REGISTRY) {
         for(String var4 : this.getVariantNames(var2)) {
            ResourceLocation var5 = this.getItemLocation(var4);
            ResourceLocation var6 = (ResourceLocation)Item.REGISTRY.getNameForObject(var2);
            this.loadItemModel(var4, var5, var6);
            if (var2.hasCustomProperties()) {
               ModelBlock var7 = (ModelBlock)this.models.get(var5);
               if (var7 != null) {
                  for(ResourceLocation var9 : var7.getOverrideLocations()) {
                     this.loadItemModel(var9.toString(), var9, var6);
                  }
               }
            }
         }
      }

   }

   private void loadItemModel(String var1, ResourceLocation var2, ResourceLocation var3) {
      this.itemLocations.put(var1, var2);
      if (this.models.get(var2) == null) {
         try {
            ModelBlock var4 = this.loadModel(var2);
            this.models.put(var2, var4);
         } catch (Exception var5) {
            LOGGER.warn("Unable to load item model: '{}' for item: '{}'", new Object[]{var2, var3, var5});
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

      for(Entry var2 : customVariantNames.entrySet()) {
         this.variantNames.put(((RegistryDelegate)var2.getKey()).get(), Lists.newArrayList(((Set)var2.getValue()).iterator()));
      }

   }

   protected List getVariantNames(Item var1) {
      List var2 = (List)this.variantNames.get(var1);
      if (var2 == null) {
         var2 = Collections.singletonList(((ResourceLocation)Item.REGISTRY.getNameForObject(var1)).toString());
      }

      return var2;
   }

   protected ResourceLocation getItemLocation(String var1) {
      ResourceLocation var2 = new ResourceLocation(var1.replaceAll("#.*", ""));
      return new ResourceLocation(var2.getResourceDomain(), "item/" + var2.getResourcePath());
   }

   private void bakeBlockModels() {
      for(ModelResourceLocation var2 : this.variants.keySet()) {
         IBakedModel var3 = this.createRandomModelForVariantList((VariantList)this.variants.get(var2), var2.toString());
         if (var3 != null) {
            this.bakedRegistry.putObject(var2, var3);
         }
      }

      for(Entry var11 : this.multipartVariantMap.entrySet()) {
         ModelBlockDefinition var12 = (ModelBlockDefinition)var11.getKey();
         Multipart var4 = var12.getMultipartData();
         String var5 = ((ResourceLocation)Block.REGISTRY.getNameForObject(var4.getStateContainer().getBlock())).toString();
         MultipartBakedModel.Builder var6 = new MultipartBakedModel.Builder();

         for(Selector var8 : var4.getSelectors()) {
            IBakedModel var9 = this.createRandomModelForVariantList(var8.getVariantList(), "selector of " + var5);
            if (var9 != null) {
               var6.putModel(var8.getPredicate(var4.getStateContainer()), var9);
            }
         }

         IBakedModel var13 = var6.makeMultipartModel();

         for(ModelResourceLocation var15 : (Collection)var11.getValue()) {
            if (!var12.hasVariant(var15.getVariant())) {
               this.bakedRegistry.putObject(var15, var13);
            }
         }
      }

   }

   @Nullable
   private IBakedModel createRandomModelForVariantList(VariantList var1, String var2) {
      if (var1.getVariantList().isEmpty()) {
         return null;
      } else {
         WeightedBakedModel.Builder var3 = new WeightedBakedModel.Builder();
         int var4 = 0;

         for(Variant var6 : var1.getVariantList()) {
            ModelBlock var7 = (ModelBlock)this.models.get(var6.getModelLocation());
            if (var7 != null && var7.isResolved()) {
               if (var7.getElements().isEmpty()) {
                  LOGGER.warn("Missing elements for: {}", new Object[]{var2});
               } else {
                  IBakedModel var8 = this.bakeModel(var7, var6.getRotation(), var6.isUvLock());
                  if (var8 != null) {
                     ++var4;
                     var3.add(var8, var6.getWeight());
                  }
               }
            } else {
               LOGGER.warn("Missing model for: {}", new Object[]{var2});
            }
         }

         Object var9 = null;
         if (var4 == 0) {
            LOGGER.warn("No weighted models for: {}", new Object[]{var2});
         } else if (var4 == 1) {
            var9 = var3.first();
         } else {
            var9 = var3.build();
         }

         return (IBakedModel)var9;
      }
   }

   private void bakeItemModels() {
      for(Entry var2 : this.itemLocations.entrySet()) {
         ResourceLocation var3 = (ResourceLocation)var2.getValue();
         ModelResourceLocation var4 = ModelLoader.getInventoryVariant((String)var2.getKey());
         ModelBlock var5 = (ModelBlock)this.models.get(var3);
         if (var5 != null && var5.isResolved()) {
            if (var5.getElements().isEmpty()) {
               LOGGER.warn("Missing elements for: {}", new Object[]{var3});
            } else if (this.isCustomRenderer(var5)) {
               this.bakedRegistry.putObject(var4, new BuiltInModel(var5.getAllTransforms(), var5.createOverrides()));
            } else {
               IBakedModel var6 = this.bakeModel(var5, ModelRotation.X0_Y0, false);
               if (var6 != null) {
                  this.bakedRegistry.putObject(var4, var6);
               }
            }
         } else {
            LOGGER.warn("Missing model for: {}", new Object[]{var3});
         }
      }

   }

   private Set getVariantsTextureLocations() {
      HashSet var1 = Sets.newHashSet();
      ArrayList var2 = Lists.newArrayList(this.variants.keySet());
      Collections.sort(var2, new Comparator() {
         public int compare(ModelResourceLocation var1, ModelResourceLocation var2) {
            return var1.toString().compareTo(var2.toString());
         }
      });

      for(ModelResourceLocation var4 : var2) {
         VariantList var5 = (VariantList)this.variants.get(var4);

         for(Variant var7 : var5.getVariantList()) {
            ModelBlock var8 = (ModelBlock)this.models.get(var7.getModelLocation());
            if (var8 == null) {
               LOGGER.warn("Missing model for: {}", new Object[]{var4});
            } else {
               var1.addAll(this.getTextureLocations(var8));
            }
         }
      }

      for(ModelBlockDefinition var11 : this.multipartVariantMap.keySet()) {
         for(VariantList var13 : var11.getMultipartData().getVariants()) {
            for(Variant var15 : var13.getVariantList()) {
               ModelBlock var9 = (ModelBlock)this.models.get(var15.getModelLocation());
               if (var9 == null) {
                  LOGGER.warn("Missing model for: {}", new Object[]{Block.REGISTRY.getNameForObject(var11.getMultipartData().getStateContainer().getBlock())});
               } else {
                  var1.addAll(this.getTextureLocations(var9));
               }
            }
         }
      }

      var1.addAll(LOCATIONS_BUILTIN_TEXTURES);
      return var1;
   }

   @Nullable
   private IBakedModel bakeModel(ModelBlock var1, ModelRotation var2, boolean var3) {
      return this.bakeModel(var1, (ITransformation)var2, var3);
   }

   protected IBakedModel bakeModel(ModelBlock var1, ITransformation var2, boolean var3) {
      TextureAtlasSprite var4 = (TextureAtlasSprite)this.sprites.get(new ResourceLocation(var1.resolveTextureName("particle")));
      SimpleBakedModel.Builder var5 = (new SimpleBakedModel.Builder(var1, var1.createOverrides())).setTexture(var4);
      if (var1.getElements().isEmpty()) {
         return null;
      } else {
         for(BlockPart var7 : var1.getElements()) {
            for(EnumFacing var9 : var7.mapFaces.keySet()) {
               BlockPartFace var10 = (BlockPartFace)var7.mapFaces.get(var9);
               TextureAtlasSprite var11 = (TextureAtlasSprite)this.sprites.get(new ResourceLocation(var1.resolveTextureName(var10.texture)));
               if (var10.cullFace != null && TRSRTransformation.isInteger(var2.getMatrix())) {
                  var5.addFaceQuad(var2.rotate(var10.cullFace), this.makeBakedQuad(var7, var10, var11, var9, var2, var3));
               } else {
                  var5.addGeneralQuad(this.makeBakedQuad(var7, var10, var11, var9, var2, var3));
               }
            }
         }

         return var5.makeBakedModel();
      }
   }

   private BakedQuad makeBakedQuad(BlockPart var1, BlockPartFace var2, TextureAtlasSprite var3, EnumFacing var4, ModelRotation var5, boolean var6) {
      return this.makeBakedQuad(var1, var2, var3, var4, (ITransformation)var5, var6);
   }

   protected BakedQuad makeBakedQuad(BlockPart var1, BlockPartFace var2, TextureAtlasSprite var3, EnumFacing var4, ITransformation var5, boolean var6) {
      return this.faceBakery.makeBakedQuad(var1.positionFrom, var1.positionTo, var2, var3, var4, var5, var1.partRotation, var6, var1.shade);
   }

   private void loadModelsCheck() {
      this.loadModels();

      for(ModelBlock var2 : this.models.values()) {
         var2.getParentFromMap(this.models);
      }

      ModelBlock.checkModelHierarchy(this.models);
   }

   private void loadModels() {
      ArrayDeque var1 = Queues.newArrayDeque();
      HashSet var2 = Sets.newHashSet();

      for(ResourceLocation var4 : this.models.keySet()) {
         var2.add(var4);
         this.addModelParentLocation(var1, var2, (ModelBlock)this.models.get(var4));
      }

      while(!var1.isEmpty()) {
         ResourceLocation var6 = (ResourceLocation)var1.pop();

         try {
            if (this.models.get(var6) != null) {
               continue;
            }

            ModelBlock var7 = this.loadModel(var6);
            this.models.put(var6, var7);
            this.addModelParentLocation(var1, var2, var7);
         } catch (Exception var5) {
            LOGGER.warn("In parent chain: {}; unable to load model: '{}'", new Object[]{JOINER.join(this.getParentPath(var6)), var6, var5});
         }

         var2.add(var6);
      }

   }

   private void addModelParentLocation(Deque var1, Set var2, ModelBlock var3) {
      ResourceLocation var4 = var3.getParentLocation();
      if (var4 != null && !var2.contains(var4)) {
         var1.add(var4);
      }

   }

   private List getParentPath(ResourceLocation var1) {
      ArrayList var2 = Lists.newArrayList(new ResourceLocation[]{var1});
      ResourceLocation var3 = var1;

      while((var3 = this.getParentLocation(var3)) != null) {
         var2.add(0, var3);
      }

      return var2;
   }

   @Nullable
   private ResourceLocation getParentLocation(ResourceLocation var1) {
      for(Entry var3 : this.models.entrySet()) {
         ModelBlock var4 = (ModelBlock)var3.getValue();
         if (var4 != null && var1.equals(var4.getParentLocation())) {
            return (ResourceLocation)var3.getKey();
         }
      }

      return null;
   }

   protected Set getTextureLocations(ModelBlock var1) {
      HashSet var2 = Sets.newHashSet();

      for(BlockPart var4 : var1.getElements()) {
         for(BlockPartFace var6 : var4.mapFaces.values()) {
            ResourceLocation var7 = new ResourceLocation(var1.resolveTextureName(var6.texture));
            var2.add(var7);
         }
      }

      var2.add(new ResourceLocation(var1.resolveTextureName("particle")));
      return var2;
   }

   private void loadSprites() {
      final Set var1 = this.getVariantsTextureLocations();
      var1.addAll(this.getItemsTextureLocations());
      var1.remove(TextureMap.LOCATION_MISSING_TEXTURE);
      ITextureMapPopulator var2 = new ITextureMapPopulator() {
         public void registerSprites(TextureMap var1x) {
            for(ResourceLocation var3 : var1) {
               TextureAtlasSprite var4 = var1x.registerSprite(var3);
               ModelBakery.this.sprites.put(var3, var4);
            }

         }
      };
      this.textureMap.loadSprites(this.resourceManager, var2);
      this.sprites.put(new ResourceLocation("missingno"), this.textureMap.getMissingSprite());
   }

   private Set getItemsTextureLocations() {
      HashSet var1 = Sets.newHashSet();

      for(ResourceLocation var3 : this.itemLocations.values()) {
         ModelBlock var4 = (ModelBlock)this.models.get(var3);
         if (var4 != null) {
            var1.add(new ResourceLocation(var4.resolveTextureName("particle")));
            if (this.hasItemModel(var4)) {
               for(String var11 : ItemModelGenerator.LAYERS) {
                  var1.add(new ResourceLocation(var4.resolveTextureName(var11)));
               }
            } else if (!this.isCustomRenderer(var4)) {
               for(BlockPart var6 : var4.getElements()) {
                  for(BlockPartFace var8 : var6.mapFaces.values()) {
                     ResourceLocation var9 = new ResourceLocation(var4.resolveTextureName(var8.texture));
                     var1.add(var9);
                  }
               }
            }
         }
      }

      return var1;
   }

   protected boolean hasItemModel(@Nullable ModelBlock var1) {
      return var1 == null ? false : var1.getRootModel() == MODEL_GENERATED;
   }

   protected boolean isCustomRenderer(@Nullable ModelBlock var1) {
      if (var1 == null) {
         return false;
      } else {
         ModelBlock var2 = var1.getRootModel();
         return var2 == MODEL_ENTITY;
      }
   }

   private void makeItemModels() {
      for(ResourceLocation var2 : this.itemLocations.values()) {
         ModelBlock var3 = (ModelBlock)this.models.get(var2);
         if (this.hasItemModel(var3)) {
            ModelBlock var4 = this.makeItemModel(var3);
            if (var4 != null) {
               var4.name = var2.toString();
            }

            this.models.put(var2, var4);
         } else if (this.isCustomRenderer(var3)) {
            this.models.put(var2, var3);
         }
      }

      for(TextureAtlasSprite var6 : this.sprites.values()) {
         if (!var6.hasAnimationMetadata()) {
            var6.clearFramesTextureData();
         }
      }

   }

   protected ModelBlock makeItemModel(ModelBlock var1) {
      return this.itemModelGenerator.makeItemModel(this.textureMap, var1);
   }

   protected void registerMultipartVariant(ModelBlockDefinition var1, Collection var2) {
      this.multipartVariantMap.put(var1, var2);
   }

   public static void registerItemVariants(Item var0, ResourceLocation... var1) {
      if (!customVariantNames.containsKey(var0.delegate)) {
         customVariantNames.put(var0.delegate, Sets.newHashSet());
      }

      for(ResourceLocation var5 : var1) {
         ((Set)customVariantNames.get(var0.delegate)).add(var5.toString());
      }

   }

   static {
      BUILT_IN_MODELS.put("missing", MISSING_MODEL_MESH);
      MODEL_GENERATED.name = "generation marker";
      MODEL_ENTITY.name = "block entity marker";
   }
}
