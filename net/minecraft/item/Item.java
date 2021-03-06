package net.minecraft.item;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class Item {
   public static final RegistryNamespaced REGISTRY = new RegistryNamespaced();
   private static final Map BLOCK_TO_ITEM = Maps.newHashMap();
   private static final IItemPropertyGetter DAMAGED_GETTER = new IItemPropertyGetter() {
   };
   private static final IItemPropertyGetter DAMAGE_GETTER = new IItemPropertyGetter() {
   };
   private static final IItemPropertyGetter LEFTHANDED_GETTER = new IItemPropertyGetter() {
   };
   private static final IItemPropertyGetter COOLDOWN_GETTER = new IItemPropertyGetter() {
   };
   private final IRegistry properties = new RegistrySimple();
   protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
   protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
   private CreativeTabs tabToDisplayOn;
   protected static Random itemRand = new Random();
   protected int maxStackSize = 64;
   private int maxDamage;
   protected boolean bFull3D;
   protected boolean hasSubtypes;
   private Item containerItem;
   private String unlocalizedName;

   public static int getIdFromItem(Item var0) {
      return var0 == null ? 0 : REGISTRY.getIDForObject(var0);
   }

   public static Item getItemById(int var0) {
      return (Item)REGISTRY.getObjectById(var0);
   }

   @Nullable
   public static Item getItemFromBlock(Block var0) {
      return (Item)BLOCK_TO_ITEM.get(var0);
   }

   public static Item getByNameOrId(String var0) {
      Item var1 = (Item)REGISTRY.getObject(new ResourceLocation(var0));
      if (var1 == null) {
         try {
            return getItemById(Integer.parseInt(var0));
         } catch (NumberFormatException var3) {
            ;
         }
      }

      return var1;
   }

   public final void addPropertyOverride(ResourceLocation var1, IItemPropertyGetter var2) {
      this.properties.putObject(var1, var2);
   }

   public boolean updateItemStackNBT(NBTTagCompound var1) {
      return false;
   }

   public Item() {
      this.addPropertyOverride(new ResourceLocation("lefthanded"), LEFTHANDED_GETTER);
      this.addPropertyOverride(new ResourceLocation("cooldown"), COOLDOWN_GETTER);
   }

   public Item setMaxStackSize(int var1) {
      this.maxStackSize = var1;
      return this;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      return EnumActionResult.PASS;
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      return 1.0F;
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      return new ActionResult(EnumActionResult.PASS, var1);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3) {
      return var1;
   }

   public int getItemStackLimit() {
      return this.maxStackSize;
   }

   public int getMetadata(int var1) {
      return 0;
   }

   public boolean getHasSubtypes() {
      return this.hasSubtypes;
   }

   protected Item setHasSubtypes(boolean var1) {
      this.hasSubtypes = var1;
      return this;
   }

   public int getMaxDamage() {
      return this.maxDamage;
   }

   protected Item setMaxDamage(int var1) {
      this.maxDamage = var1;
      if (var1 > 0) {
         this.addPropertyOverride(new ResourceLocation("damaged"), DAMAGED_GETTER);
         this.addPropertyOverride(new ResourceLocation("damage"), DAMAGE_GETTER);
      }

      return this;
   }

   public boolean isDamageable() {
      return this.maxDamage > 0 && (!this.hasSubtypes || this.maxStackSize == 1);
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      return false;
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      return false;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      return false;
   }

   public boolean itemInteractionForEntity(ItemStack var1, EntityPlayer var2, EntityLivingBase var3, EnumHand var4) {
      return false;
   }

   public Item setFull3D() {
      this.bFull3D = true;
      return this;
   }

   public Item setUnlocalizedName(String var1) {
      this.unlocalizedName = var1;
      return this;
   }

   public String getUnlocalizedNameInefficiently(ItemStack var1) {
      String var2 = this.getUnlocalizedName(var1);
      return var2 == null ? "" : I18n.translateToLocal(var2);
   }

   public String getUnlocalizedName() {
      return "item." + this.unlocalizedName;
   }

   public String getUnlocalizedName(ItemStack var1) {
      return "item." + this.unlocalizedName;
   }

   public Item setContainerItem(Item var1) {
      this.containerItem = var1;
      return this;
   }

   public boolean getShareTag() {
      return true;
   }

   public Item getContainerItem() {
      return this.containerItem;
   }

   public boolean hasContainerItem() {
      return this.containerItem != null;
   }

   public void onUpdate(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
   }

   public void onCreated(ItemStack var1, World var2, EntityPlayer var3) {
   }

   public boolean isMap() {
      return false;
   }

   public EnumAction getItemUseAction(ItemStack var1) {
      return EnumAction.NONE;
   }

   public int getMaxItemUseDuration(ItemStack var1) {
      return 0;
   }

   public void onPlayerStoppedUsing(ItemStack var1, World var2, EntityLivingBase var3, int var4) {
   }

   public String getItemStackDisplayName(ItemStack var1) {
      return ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(var1) + ".name")).trim();
   }

   public EnumRarity getRarity(ItemStack var1) {
      return var1.isItemEnchanted() ? EnumRarity.RARE : EnumRarity.COMMON;
   }

   public boolean isEnchantable(ItemStack var1) {
      return this.getItemStackLimit() == 1 && this.isDamageable();
   }

   protected RayTraceResult rayTrace(World var1, EntityPlayer var2, boolean var3) {
      float var4 = var2.rotationPitch;
      float var5 = var2.rotationYaw;
      double var6 = var2.posX;
      double var8 = var2.posY + (double)var2.getEyeHeight();
      double var10 = var2.posZ;
      Vec3d var12 = new Vec3d(var6, var8, var10);
      float var13 = MathHelper.cos(-var5 * 0.017453292F - 3.1415927F);
      float var14 = MathHelper.sin(-var5 * 0.017453292F - 3.1415927F);
      float var15 = -MathHelper.cos(-var4 * 0.017453292F);
      float var16 = MathHelper.sin(-var4 * 0.017453292F);
      float var17 = var14 * var15;
      float var19 = var13 * var15;
      double var20 = 5.0D;
      Vec3d var22 = var12.addVector((double)var17 * 5.0D, (double)var16 * 5.0D, (double)var19 * 5.0D);
      return var1.rayTraceBlocks(var12, var22, var3, !var3, false);
   }

   public int getItemEnchantability() {
      return 0;
   }

   public Item setCreativeTab(CreativeTabs var1) {
      this.tabToDisplayOn = var1;
      return this;
   }

   public boolean canItemEditBlocks() {
      return false;
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return false;
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      return HashMultimap.create();
   }

   public static void registerItems() {
      registerItemBlock(Blocks.STONE, (new ItemMultiTexture(Blocks.STONE, Blocks.STONE, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockStone.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("stone"));
      registerItemBlock(Blocks.GRASS, new ItemColored(Blocks.GRASS, false));
      registerItemBlock(Blocks.DIRT, (new ItemMultiTexture(Blocks.DIRT, Blocks.DIRT, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockDirt.DirtType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("dirt"));
      registerItemBlock(Blocks.COBBLESTONE);
      registerItemBlock(Blocks.PLANKS, (new ItemMultiTexture(Blocks.PLANKS, Blocks.PLANKS, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockPlanks.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("wood"));
      registerItemBlock(Blocks.SAPLING, (new ItemMultiTexture(Blocks.SAPLING, Blocks.SAPLING, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockPlanks.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("sapling"));
      registerItemBlock(Blocks.BEDROCK);
      registerItemBlock(Blocks.SAND, (new ItemMultiTexture(Blocks.SAND, Blocks.SAND, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockSand.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("sand"));
      registerItemBlock(Blocks.GRAVEL);
      registerItemBlock(Blocks.GOLD_ORE);
      registerItemBlock(Blocks.IRON_ORE);
      registerItemBlock(Blocks.COAL_ORE);
      registerItemBlock(Blocks.LOG, (new ItemMultiTexture(Blocks.LOG, Blocks.LOG, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockPlanks.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("log"));
      registerItemBlock(Blocks.LOG2, (new ItemMultiTexture(Blocks.LOG2, Blocks.LOG2, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockPlanks.EnumType.byMetadata(var1.getMetadata() + 4).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("log"));
      registerItemBlock(Blocks.LEAVES, (new ItemLeaves(Blocks.LEAVES)).setUnlocalizedName("leaves"));
      registerItemBlock(Blocks.LEAVES2, (new ItemLeaves(Blocks.LEAVES2)).setUnlocalizedName("leaves"));
      registerItemBlock(Blocks.SPONGE, (new ItemMultiTexture(Blocks.SPONGE, Blocks.SPONGE, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return (var1.getMetadata() & 1) == 1 ? "wet" : "dry";
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("sponge"));
      registerItemBlock(Blocks.GLASS);
      registerItemBlock(Blocks.LAPIS_ORE);
      registerItemBlock(Blocks.LAPIS_BLOCK);
      registerItemBlock(Blocks.DISPENSER);
      registerItemBlock(Blocks.SANDSTONE, (new ItemMultiTexture(Blocks.SANDSTONE, Blocks.SANDSTONE, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockSandStone.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("sandStone"));
      registerItemBlock(Blocks.NOTEBLOCK);
      registerItemBlock(Blocks.GOLDEN_RAIL);
      registerItemBlock(Blocks.DETECTOR_RAIL);
      registerItemBlock(Blocks.STICKY_PISTON, new ItemPiston(Blocks.STICKY_PISTON));
      registerItemBlock(Blocks.WEB);
      registerItemBlock(Blocks.TALLGRASS, (new ItemColored(Blocks.TALLGRASS, true)).setSubtypeNames(new String[]{"shrub", "grass", "fern"}));
      registerItemBlock(Blocks.DEADBUSH);
      registerItemBlock(Blocks.PISTON, new ItemPiston(Blocks.PISTON));
      registerItemBlock(Blocks.WOOL, (new ItemCloth(Blocks.WOOL)).setUnlocalizedName("cloth"));
      registerItemBlock(Blocks.YELLOW_FLOWER, (new ItemMultiTexture(Blocks.YELLOW_FLOWER, Blocks.YELLOW_FLOWER, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.YELLOW, var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("flower"));
      registerItemBlock(Blocks.RED_FLOWER, (new ItemMultiTexture(Blocks.RED_FLOWER, Blocks.RED_FLOWER, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("rose"));
      registerItemBlock(Blocks.BROWN_MUSHROOM);
      registerItemBlock(Blocks.RED_MUSHROOM);
      registerItemBlock(Blocks.GOLD_BLOCK);
      registerItemBlock(Blocks.IRON_BLOCK);
      registerItemBlock(Blocks.STONE_SLAB, (new ItemSlab(Blocks.STONE_SLAB, Blocks.STONE_SLAB, Blocks.DOUBLE_STONE_SLAB)).setUnlocalizedName("stoneSlab"));
      registerItemBlock(Blocks.BRICK_BLOCK);
      registerItemBlock(Blocks.TNT);
      registerItemBlock(Blocks.BOOKSHELF);
      registerItemBlock(Blocks.MOSSY_COBBLESTONE);
      registerItemBlock(Blocks.OBSIDIAN);
      registerItemBlock(Blocks.TORCH);
      registerItemBlock(Blocks.END_ROD);
      registerItemBlock(Blocks.CHORUS_PLANT);
      registerItemBlock(Blocks.CHORUS_FLOWER);
      registerItemBlock(Blocks.PURPUR_BLOCK);
      registerItemBlock(Blocks.PURPUR_PILLAR);
      registerItemBlock(Blocks.PURPUR_STAIRS);
      registerItemBlock(Blocks.PURPUR_SLAB, (new ItemSlab(Blocks.PURPUR_SLAB, Blocks.PURPUR_SLAB, Blocks.PURPUR_DOUBLE_SLAB)).setUnlocalizedName("purpurSlab"));
      registerItemBlock(Blocks.MOB_SPAWNER);
      registerItemBlock(Blocks.OAK_STAIRS);
      registerItemBlock(Blocks.CHEST);
      registerItemBlock(Blocks.DIAMOND_ORE);
      registerItemBlock(Blocks.DIAMOND_BLOCK);
      registerItemBlock(Blocks.CRAFTING_TABLE);
      registerItemBlock(Blocks.FARMLAND);
      registerItemBlock(Blocks.FURNACE);
      registerItemBlock(Blocks.LADDER);
      registerItemBlock(Blocks.RAIL);
      registerItemBlock(Blocks.STONE_STAIRS);
      registerItemBlock(Blocks.LEVER);
      registerItemBlock(Blocks.STONE_PRESSURE_PLATE);
      registerItemBlock(Blocks.WOODEN_PRESSURE_PLATE);
      registerItemBlock(Blocks.REDSTONE_ORE);
      registerItemBlock(Blocks.REDSTONE_TORCH);
      registerItemBlock(Blocks.STONE_BUTTON);
      registerItemBlock(Blocks.SNOW_LAYER, new ItemSnow(Blocks.SNOW_LAYER));
      registerItemBlock(Blocks.ICE);
      registerItemBlock(Blocks.SNOW);
      registerItemBlock(Blocks.CACTUS);
      registerItemBlock(Blocks.CLAY);
      registerItemBlock(Blocks.JUKEBOX);
      registerItemBlock(Blocks.OAK_FENCE);
      registerItemBlock(Blocks.SPRUCE_FENCE);
      registerItemBlock(Blocks.BIRCH_FENCE);
      registerItemBlock(Blocks.JUNGLE_FENCE);
      registerItemBlock(Blocks.DARK_OAK_FENCE);
      registerItemBlock(Blocks.ACACIA_FENCE);
      registerItemBlock(Blocks.PUMPKIN);
      registerItemBlock(Blocks.NETHERRACK);
      registerItemBlock(Blocks.SOUL_SAND);
      registerItemBlock(Blocks.GLOWSTONE);
      registerItemBlock(Blocks.LIT_PUMPKIN);
      registerItemBlock(Blocks.TRAPDOOR);
      registerItemBlock(Blocks.MONSTER_EGG, (new ItemMultiTexture(Blocks.MONSTER_EGG, Blocks.MONSTER_EGG, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockSilverfish.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("monsterStoneEgg"));
      registerItemBlock(Blocks.STONEBRICK, (new ItemMultiTexture(Blocks.STONEBRICK, Blocks.STONEBRICK, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockStoneBrick.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("stonebricksmooth"));
      registerItemBlock(Blocks.BROWN_MUSHROOM_BLOCK);
      registerItemBlock(Blocks.RED_MUSHROOM_BLOCK);
      registerItemBlock(Blocks.IRON_BARS);
      registerItemBlock(Blocks.GLASS_PANE);
      registerItemBlock(Blocks.MELON_BLOCK);
      registerItemBlock(Blocks.VINE, new ItemColored(Blocks.VINE, false));
      registerItemBlock(Blocks.OAK_FENCE_GATE);
      registerItemBlock(Blocks.SPRUCE_FENCE_GATE);
      registerItemBlock(Blocks.BIRCH_FENCE_GATE);
      registerItemBlock(Blocks.JUNGLE_FENCE_GATE);
      registerItemBlock(Blocks.DARK_OAK_FENCE_GATE);
      registerItemBlock(Blocks.ACACIA_FENCE_GATE);
      registerItemBlock(Blocks.BRICK_STAIRS);
      registerItemBlock(Blocks.STONE_BRICK_STAIRS);
      registerItemBlock(Blocks.MYCELIUM);
      registerItemBlock(Blocks.WATERLILY, new ItemLilyPad(Blocks.WATERLILY));
      registerItemBlock(Blocks.NETHER_BRICK);
      registerItemBlock(Blocks.NETHER_BRICK_FENCE);
      registerItemBlock(Blocks.NETHER_BRICK_STAIRS);
      registerItemBlock(Blocks.ENCHANTING_TABLE);
      registerItemBlock(Blocks.END_PORTAL_FRAME);
      registerItemBlock(Blocks.END_STONE);
      registerItemBlock(Blocks.END_BRICKS);
      registerItemBlock(Blocks.DRAGON_EGG);
      registerItemBlock(Blocks.REDSTONE_LAMP);
      registerItemBlock(Blocks.WOODEN_SLAB, (new ItemSlab(Blocks.WOODEN_SLAB, Blocks.WOODEN_SLAB, Blocks.DOUBLE_WOODEN_SLAB)).setUnlocalizedName("woodSlab"));
      registerItemBlock(Blocks.SANDSTONE_STAIRS);
      registerItemBlock(Blocks.EMERALD_ORE);
      registerItemBlock(Blocks.ENDER_CHEST);
      registerItemBlock(Blocks.TRIPWIRE_HOOK);
      registerItemBlock(Blocks.EMERALD_BLOCK);
      registerItemBlock(Blocks.SPRUCE_STAIRS);
      registerItemBlock(Blocks.BIRCH_STAIRS);
      registerItemBlock(Blocks.JUNGLE_STAIRS);
      registerItemBlock(Blocks.COMMAND_BLOCK);
      registerItemBlock(Blocks.BEACON);
      registerItemBlock(Blocks.COBBLESTONE_WALL, (new ItemMultiTexture(Blocks.COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockWall.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("cobbleWall"));
      registerItemBlock(Blocks.WOODEN_BUTTON);
      registerItemBlock(Blocks.ANVIL, (new ItemAnvilBlock(Blocks.ANVIL)).setUnlocalizedName("anvil"));
      registerItemBlock(Blocks.TRAPPED_CHEST);
      registerItemBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
      registerItemBlock(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
      registerItemBlock(Blocks.DAYLIGHT_DETECTOR);
      registerItemBlock(Blocks.REDSTONE_BLOCK);
      registerItemBlock(Blocks.QUARTZ_ORE);
      registerItemBlock(Blocks.HOPPER);
      registerItemBlock(Blocks.QUARTZ_BLOCK, (new ItemMultiTexture(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, new String[]{"default", "chiseled", "lines"})).setUnlocalizedName("quartzBlock"));
      registerItemBlock(Blocks.QUARTZ_STAIRS);
      registerItemBlock(Blocks.ACTIVATOR_RAIL);
      registerItemBlock(Blocks.DROPPER);
      registerItemBlock(Blocks.STAINED_HARDENED_CLAY, (new ItemCloth(Blocks.STAINED_HARDENED_CLAY)).setUnlocalizedName("clayHardenedStained"));
      registerItemBlock(Blocks.BARRIER);
      registerItemBlock(Blocks.IRON_TRAPDOOR);
      registerItemBlock(Blocks.HAY_BLOCK);
      registerItemBlock(Blocks.CARPET, (new ItemCloth(Blocks.CARPET)).setUnlocalizedName("woolCarpet"));
      registerItemBlock(Blocks.HARDENED_CLAY);
      registerItemBlock(Blocks.COAL_BLOCK);
      registerItemBlock(Blocks.PACKED_ICE);
      registerItemBlock(Blocks.ACACIA_STAIRS);
      registerItemBlock(Blocks.DARK_OAK_STAIRS);
      registerItemBlock(Blocks.SLIME_BLOCK);
      registerItemBlock(Blocks.GRASS_PATH);
      registerItemBlock(Blocks.DOUBLE_PLANT, (new ItemMultiTexture(Blocks.DOUBLE_PLANT, Blocks.DOUBLE_PLANT, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockDoublePlant.EnumPlantType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("doublePlant"));
      registerItemBlock(Blocks.STAINED_GLASS, (new ItemCloth(Blocks.STAINED_GLASS)).setUnlocalizedName("stainedGlass"));
      registerItemBlock(Blocks.STAINED_GLASS_PANE, (new ItemCloth(Blocks.STAINED_GLASS_PANE)).setUnlocalizedName("stainedGlassPane"));
      registerItemBlock(Blocks.PRISMARINE, (new ItemMultiTexture(Blocks.PRISMARINE, Blocks.PRISMARINE, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockPrismarine.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("prismarine"));
      registerItemBlock(Blocks.SEA_LANTERN);
      registerItemBlock(Blocks.RED_SANDSTONE, (new ItemMultiTexture(Blocks.RED_SANDSTONE, Blocks.RED_SANDSTONE, new Function() {
         @Nullable
         public String apply(@Nullable ItemStack var1) {
            return BlockRedSandstone.EnumType.byMetadata(var1.getMetadata()).getUnlocalizedName();
         }

         // $FF: synthetic method
         public Object apply(Object var1) {
            return this.apply((ItemStack)var1);
         }
      })).setUnlocalizedName("redSandStone"));
      registerItemBlock(Blocks.RED_SANDSTONE_STAIRS);
      registerItemBlock(Blocks.STONE_SLAB2, (new ItemSlab(Blocks.STONE_SLAB2, Blocks.STONE_SLAB2, Blocks.DOUBLE_STONE_SLAB2)).setUnlocalizedName("stoneSlab2"));
      registerItemBlock(Blocks.REPEATING_COMMAND_BLOCK);
      registerItemBlock(Blocks.CHAIN_COMMAND_BLOCK);
      registerItemBlock(Blocks.MAGMA);
      registerItemBlock(Blocks.NETHER_WART_BLOCK);
      registerItemBlock(Blocks.RED_NETHER_BRICK);
      registerItemBlock(Blocks.BONE_BLOCK);
      registerItemBlock(Blocks.STRUCTURE_VOID);
      registerItemBlock(Blocks.STRUCTURE_BLOCK);
      registerItem(256, "iron_shovel", (new ItemSpade(Item.ToolMaterial.IRON)).setUnlocalizedName("shovelIron"));
      registerItem(257, "iron_pickaxe", (new ItemPickaxe(Item.ToolMaterial.IRON)).setUnlocalizedName("pickaxeIron"));
      registerItem(258, "iron_axe", (new ItemAxe(Item.ToolMaterial.IRON)).setUnlocalizedName("hatchetIron"));
      registerItem(259, "flint_and_steel", (new ItemFlintAndSteel()).setUnlocalizedName("flintAndSteel"));
      registerItem(260, "apple", (new ItemFood(4, 0.3F, false)).setUnlocalizedName("apple"));
      registerItem(261, "bow", (new ItemBow()).setUnlocalizedName("bow"));
      registerItem(262, "arrow", (new ItemArrow()).setUnlocalizedName("arrow"));
      registerItem(263, "coal", (new ItemCoal()).setUnlocalizedName("coal"));
      registerItem(264, "diamond", (new Item()).setUnlocalizedName("diamond").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(265, "iron_ingot", (new Item()).setUnlocalizedName("ingotIron").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(266, "gold_ingot", (new Item()).setUnlocalizedName("ingotGold").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(267, "iron_sword", (new ItemSword(Item.ToolMaterial.IRON)).setUnlocalizedName("swordIron"));
      registerItem(268, "wooden_sword", (new ItemSword(Item.ToolMaterial.WOOD)).setUnlocalizedName("swordWood"));
      registerItem(269, "wooden_shovel", (new ItemSpade(Item.ToolMaterial.WOOD)).setUnlocalizedName("shovelWood"));
      registerItem(270, "wooden_pickaxe", (new ItemPickaxe(Item.ToolMaterial.WOOD)).setUnlocalizedName("pickaxeWood"));
      registerItem(271, "wooden_axe", (new ItemAxe(Item.ToolMaterial.WOOD)).setUnlocalizedName("hatchetWood"));
      registerItem(272, "stone_sword", (new ItemSword(Item.ToolMaterial.STONE)).setUnlocalizedName("swordStone"));
      registerItem(273, "stone_shovel", (new ItemSpade(Item.ToolMaterial.STONE)).setUnlocalizedName("shovelStone"));
      registerItem(274, "stone_pickaxe", (new ItemPickaxe(Item.ToolMaterial.STONE)).setUnlocalizedName("pickaxeStone"));
      registerItem(275, "stone_axe", (new ItemAxe(Item.ToolMaterial.STONE)).setUnlocalizedName("hatchetStone"));
      registerItem(276, "diamond_sword", (new ItemSword(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("swordDiamond"));
      registerItem(277, "diamond_shovel", (new ItemSpade(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("shovelDiamond"));
      registerItem(278, "diamond_pickaxe", (new ItemPickaxe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("pickaxeDiamond"));
      registerItem(279, "diamond_axe", (new ItemAxe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("hatchetDiamond"));
      registerItem(280, "stick", (new Item()).setFull3D().setUnlocalizedName("stick").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(281, "bowl", (new Item()).setUnlocalizedName("bowl").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(282, "mushroom_stew", (new ItemSoup(6)).setUnlocalizedName("mushroomStew"));
      registerItem(283, "golden_sword", (new ItemSword(Item.ToolMaterial.GOLD)).setUnlocalizedName("swordGold"));
      registerItem(284, "golden_shovel", (new ItemSpade(Item.ToolMaterial.GOLD)).setUnlocalizedName("shovelGold"));
      registerItem(285, "golden_pickaxe", (new ItemPickaxe(Item.ToolMaterial.GOLD)).setUnlocalizedName("pickaxeGold"));
      registerItem(286, "golden_axe", (new ItemAxe(Item.ToolMaterial.GOLD)).setUnlocalizedName("hatchetGold"));
      registerItem(287, "string", (new ItemBlockSpecial(Blocks.TRIPWIRE)).setUnlocalizedName("string").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(288, "feather", (new Item()).setUnlocalizedName("feather").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(289, "gunpowder", (new Item()).setUnlocalizedName("sulphur").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(290, "wooden_hoe", (new ItemHoe(Item.ToolMaterial.WOOD)).setUnlocalizedName("hoeWood"));
      registerItem(291, "stone_hoe", (new ItemHoe(Item.ToolMaterial.STONE)).setUnlocalizedName("hoeStone"));
      registerItem(292, "iron_hoe", (new ItemHoe(Item.ToolMaterial.IRON)).setUnlocalizedName("hoeIron"));
      registerItem(293, "diamond_hoe", (new ItemHoe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("hoeDiamond"));
      registerItem(294, "golden_hoe", (new ItemHoe(Item.ToolMaterial.GOLD)).setUnlocalizedName("hoeGold"));
      registerItem(295, "wheat_seeds", (new ItemSeeds(Blocks.WHEAT, Blocks.FARMLAND)).setUnlocalizedName("seeds"));
      registerItem(296, "wheat", (new Item()).setUnlocalizedName("wheat").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(297, "bread", (new ItemFood(5, 0.6F, false)).setUnlocalizedName("bread"));
      registerItem(298, "leather_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetCloth"));
      registerItem(299, "leather_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateCloth"));
      registerItem(300, "leather_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsCloth"));
      registerItem(301, "leather_boots", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsCloth"));
      registerItem(302, "chainmail_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetChain"));
      registerItem(303, "chainmail_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateChain"));
      registerItem(304, "chainmail_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsChain"));
      registerItem(305, "chainmail_boots", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsChain"));
      registerItem(306, "iron_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetIron"));
      registerItem(307, "iron_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateIron"));
      registerItem(308, "iron_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsIron"));
      registerItem(309, "iron_boots", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsIron"));
      registerItem(310, "diamond_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetDiamond"));
      registerItem(311, "diamond_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateDiamond"));
      registerItem(312, "diamond_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsDiamond"));
      registerItem(313, "diamond_boots", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsDiamond"));
      registerItem(314, "golden_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetGold"));
      registerItem(315, "golden_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateGold"));
      registerItem(316, "golden_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsGold"));
      registerItem(317, "golden_boots", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsGold"));
      registerItem(318, "flint", (new Item()).setUnlocalizedName("flint").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(319, "porkchop", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("porkchopRaw"));
      registerItem(320, "cooked_porkchop", (new ItemFood(8, 0.8F, true)).setUnlocalizedName("porkchopCooked"));
      registerItem(321, "painting", (new ItemHangingEntity(EntityPainting.class)).setUnlocalizedName("painting"));
      registerItem(322, "golden_apple", (new ItemAppleGold(4, 1.2F, false)).setAlwaysEdible().setUnlocalizedName("appleGold"));
      registerItem(323, "sign", (new ItemSign()).setUnlocalizedName("sign"));
      registerItem(324, "wooden_door", (new ItemDoor(Blocks.OAK_DOOR)).setUnlocalizedName("doorOak"));
      Item var0 = (new ItemBucket(Blocks.AIR)).setUnlocalizedName("bucket").setMaxStackSize(16);
      registerItem(325, "bucket", var0);
      registerItem(326, "water_bucket", (new ItemBucket(Blocks.FLOWING_WATER)).setUnlocalizedName("bucketWater").setContainerItem(var0));
      registerItem(327, "lava_bucket", (new ItemBucket(Blocks.FLOWING_LAVA)).setUnlocalizedName("bucketLava").setContainerItem(var0));
      registerItem(328, "minecart", (new ItemMinecart(EntityMinecart.Type.RIDEABLE)).setUnlocalizedName("minecart"));
      registerItem(329, "saddle", (new ItemSaddle()).setUnlocalizedName("saddle"));
      registerItem(330, "iron_door", (new ItemDoor(Blocks.IRON_DOOR)).setUnlocalizedName("doorIron"));
      registerItem(331, "redstone", (new ItemRedstone()).setUnlocalizedName("redstone"));
      registerItem(332, "snowball", (new ItemSnowball()).setUnlocalizedName("snowball"));
      registerItem(333, "boat", new ItemBoat(EntityBoat.Type.OAK));
      registerItem(334, "leather", (new Item()).setUnlocalizedName("leather").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(335, "milk_bucket", (new ItemBucketMilk()).setUnlocalizedName("milk").setContainerItem(var0));
      registerItem(336, "brick", (new Item()).setUnlocalizedName("brick").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(337, "clay_ball", (new Item()).setUnlocalizedName("clay").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(338, "reeds", (new ItemBlockSpecial(Blocks.REEDS)).setUnlocalizedName("reeds").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(339, "paper", (new Item()).setUnlocalizedName("paper").setCreativeTab(CreativeTabs.MISC));
      registerItem(340, "book", (new ItemBook()).setUnlocalizedName("book").setCreativeTab(CreativeTabs.MISC));
      registerItem(341, "slime_ball", (new Item()).setUnlocalizedName("slimeball").setCreativeTab(CreativeTabs.MISC));
      registerItem(342, "chest_minecart", (new ItemMinecart(EntityMinecart.Type.CHEST)).setUnlocalizedName("minecartChest"));
      registerItem(343, "furnace_minecart", (new ItemMinecart(EntityMinecart.Type.FURNACE)).setUnlocalizedName("minecartFurnace"));
      registerItem(344, "egg", (new ItemEgg()).setUnlocalizedName("egg"));
      registerItem(345, "compass", (new ItemCompass()).setUnlocalizedName("compass").setCreativeTab(CreativeTabs.TOOLS));
      registerItem(346, "fishing_rod", (new ItemFishingRod()).setUnlocalizedName("fishingRod"));
      registerItem(347, "clock", (new ItemClock()).setUnlocalizedName("clock").setCreativeTab(CreativeTabs.TOOLS));
      registerItem(348, "glowstone_dust", (new Item()).setUnlocalizedName("yellowDust").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(349, "fish", (new ItemFishFood(false)).setUnlocalizedName("fish").setHasSubtypes(true));
      registerItem(350, "cooked_fish", (new ItemFishFood(true)).setUnlocalizedName("fish").setHasSubtypes(true));
      registerItem(351, "dye", (new ItemDye()).setUnlocalizedName("dyePowder"));
      registerItem(352, "bone", (new Item()).setUnlocalizedName("bone").setFull3D().setCreativeTab(CreativeTabs.MISC));
      registerItem(353, "sugar", (new Item()).setUnlocalizedName("sugar").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(354, "cake", (new ItemBlockSpecial(Blocks.CAKE)).setMaxStackSize(1).setUnlocalizedName("cake").setCreativeTab(CreativeTabs.FOOD));
      registerItem(355, "bed", (new ItemBed()).setMaxStackSize(1).setUnlocalizedName("bed"));
      registerItem(356, "repeater", (new ItemBlockSpecial(Blocks.UNPOWERED_REPEATER)).setUnlocalizedName("diode").setCreativeTab(CreativeTabs.REDSTONE));
      registerItem(357, "cookie", (new ItemFood(2, 0.1F, false)).setUnlocalizedName("cookie"));
      registerItem(358, "filled_map", (new ItemMap()).setUnlocalizedName("map"));
      registerItem(359, "shears", (new ItemShears()).setUnlocalizedName("shears"));
      registerItem(360, "melon", (new ItemFood(2, 0.3F, false)).setUnlocalizedName("melon"));
      registerItem(361, "pumpkin_seeds", (new ItemSeeds(Blocks.PUMPKIN_STEM, Blocks.FARMLAND)).setUnlocalizedName("seeds_pumpkin"));
      registerItem(362, "melon_seeds", (new ItemSeeds(Blocks.MELON_STEM, Blocks.FARMLAND)).setUnlocalizedName("seeds_melon"));
      registerItem(363, "beef", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("beefRaw"));
      registerItem(364, "cooked_beef", (new ItemFood(8, 0.8F, true)).setUnlocalizedName("beefCooked"));
      registerItem(365, "chicken", (new ItemFood(2, 0.3F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.3F).setUnlocalizedName("chickenRaw"));
      registerItem(366, "cooked_chicken", (new ItemFood(6, 0.6F, true)).setUnlocalizedName("chickenCooked"));
      registerItem(367, "rotten_flesh", (new ItemFood(4, 0.1F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F).setUnlocalizedName("rottenFlesh"));
      registerItem(368, "ender_pearl", (new ItemEnderPearl()).setUnlocalizedName("enderPearl"));
      registerItem(369, "blaze_rod", (new Item()).setUnlocalizedName("blazeRod").setCreativeTab(CreativeTabs.MATERIALS).setFull3D());
      registerItem(370, "ghast_tear", (new Item()).setUnlocalizedName("ghastTear").setCreativeTab(CreativeTabs.BREWING));
      registerItem(371, "gold_nugget", (new Item()).setUnlocalizedName("goldNugget").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(372, "nether_wart", (new ItemSeeds(Blocks.NETHER_WART, Blocks.SOUL_SAND)).setUnlocalizedName("netherStalkSeeds"));
      registerItem(373, "potion", (new ItemPotion()).setUnlocalizedName("potion"));
      Item var1 = (new ItemGlassBottle()).setUnlocalizedName("glassBottle");
      registerItem(374, "glass_bottle", var1);
      registerItem(375, "spider_eye", (new ItemFood(2, 0.8F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 1.0F).setUnlocalizedName("spiderEye"));
      registerItem(376, "fermented_spider_eye", (new Item()).setUnlocalizedName("fermentedSpiderEye").setCreativeTab(CreativeTabs.BREWING));
      registerItem(377, "blaze_powder", (new Item()).setUnlocalizedName("blazePowder").setCreativeTab(CreativeTabs.BREWING));
      registerItem(378, "magma_cream", (new Item()).setUnlocalizedName("magmaCream").setCreativeTab(CreativeTabs.BREWING));
      registerItem(379, "brewing_stand", (new ItemBlockSpecial(Blocks.BREWING_STAND)).setUnlocalizedName("brewingStand").setCreativeTab(CreativeTabs.BREWING));
      registerItem(380, "cauldron", (new ItemBlockSpecial(Blocks.CAULDRON)).setUnlocalizedName("cauldron").setCreativeTab(CreativeTabs.BREWING));
      registerItem(381, "ender_eye", (new ItemEnderEye()).setUnlocalizedName("eyeOfEnder"));
      registerItem(382, "speckled_melon", (new Item()).setUnlocalizedName("speckledMelon").setCreativeTab(CreativeTabs.BREWING));
      registerItem(383, "spawn_egg", (new ItemMonsterPlacer()).setUnlocalizedName("monsterPlacer"));
      registerItem(384, "experience_bottle", (new ItemExpBottle()).setUnlocalizedName("expBottle"));
      registerItem(385, "fire_charge", (new ItemFireball()).setUnlocalizedName("fireball"));
      registerItem(386, "writable_book", (new ItemWritableBook()).setUnlocalizedName("writingBook").setCreativeTab(CreativeTabs.MISC));
      registerItem(387, "written_book", (new ItemWrittenBook()).setUnlocalizedName("writtenBook").setMaxStackSize(16));
      registerItem(388, "emerald", (new Item()).setUnlocalizedName("emerald").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(389, "item_frame", (new ItemHangingEntity(EntityItemFrame.class)).setUnlocalizedName("frame"));
      registerItem(390, "flower_pot", (new ItemBlockSpecial(Blocks.FLOWER_POT)).setUnlocalizedName("flowerPot").setCreativeTab(CreativeTabs.DECORATIONS));
      registerItem(391, "carrot", (new ItemSeedFood(3, 0.6F, Blocks.CARROTS, Blocks.FARMLAND)).setUnlocalizedName("carrots"));
      registerItem(392, "potato", (new ItemSeedFood(1, 0.3F, Blocks.POTATOES, Blocks.FARMLAND)).setUnlocalizedName("potato"));
      registerItem(393, "baked_potato", (new ItemFood(5, 0.6F, false)).setUnlocalizedName("potatoBaked"));
      registerItem(394, "poisonous_potato", (new ItemFood(2, 0.3F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 0.6F).setUnlocalizedName("potatoPoisonous"));
      registerItem(395, "map", (new ItemEmptyMap()).setUnlocalizedName("emptyMap"));
      registerItem(396, "golden_carrot", (new ItemFood(6, 1.2F, false)).setUnlocalizedName("carrotGolden").setCreativeTab(CreativeTabs.BREWING));
      registerItem(397, "skull", (new ItemSkull()).setUnlocalizedName("skull"));
      registerItem(398, "carrot_on_a_stick", (new ItemCarrotOnAStick()).setUnlocalizedName("carrotOnAStick"));
      registerItem(399, "nether_star", (new ItemSimpleFoiled()).setUnlocalizedName("netherStar").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(400, "pumpkin_pie", (new ItemFood(8, 0.3F, false)).setUnlocalizedName("pumpkinPie").setCreativeTab(CreativeTabs.FOOD));
      registerItem(401, "fireworks", (new ItemFirework()).setUnlocalizedName("fireworks"));
      registerItem(402, "firework_charge", (new ItemFireworkCharge()).setUnlocalizedName("fireworksCharge").setCreativeTab(CreativeTabs.MISC));
      registerItem(403, "enchanted_book", (new ItemEnchantedBook()).setMaxStackSize(1).setUnlocalizedName("enchantedBook"));
      registerItem(404, "comparator", (new ItemBlockSpecial(Blocks.UNPOWERED_COMPARATOR)).setUnlocalizedName("comparator").setCreativeTab(CreativeTabs.REDSTONE));
      registerItem(405, "netherbrick", (new Item()).setUnlocalizedName("netherbrick").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(406, "quartz", (new Item()).setUnlocalizedName("netherquartz").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(407, "tnt_minecart", (new ItemMinecart(EntityMinecart.Type.TNT)).setUnlocalizedName("minecartTnt"));
      registerItem(408, "hopper_minecart", (new ItemMinecart(EntityMinecart.Type.HOPPER)).setUnlocalizedName("minecartHopper"));
      registerItem(409, "prismarine_shard", (new Item()).setUnlocalizedName("prismarineShard").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(410, "prismarine_crystals", (new Item()).setUnlocalizedName("prismarineCrystals").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(411, "rabbit", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("rabbitRaw"));
      registerItem(412, "cooked_rabbit", (new ItemFood(5, 0.6F, true)).setUnlocalizedName("rabbitCooked"));
      registerItem(413, "rabbit_stew", (new ItemSoup(10)).setUnlocalizedName("rabbitStew"));
      registerItem(414, "rabbit_foot", (new Item()).setUnlocalizedName("rabbitFoot").setCreativeTab(CreativeTabs.BREWING));
      registerItem(415, "rabbit_hide", (new Item()).setUnlocalizedName("rabbitHide").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(416, "armor_stand", (new ItemArmorStand()).setUnlocalizedName("armorStand").setMaxStackSize(16));
      registerItem(417, "iron_horse_armor", (new Item()).setUnlocalizedName("horsearmormetal").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
      registerItem(418, "golden_horse_armor", (new Item()).setUnlocalizedName("horsearmorgold").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
      registerItem(419, "diamond_horse_armor", (new Item()).setUnlocalizedName("horsearmordiamond").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
      registerItem(420, "lead", (new ItemLead()).setUnlocalizedName("leash"));
      registerItem(421, "name_tag", (new ItemNameTag()).setUnlocalizedName("nameTag"));
      registerItem(422, "command_block_minecart", (new ItemMinecart(EntityMinecart.Type.COMMAND_BLOCK)).setUnlocalizedName("minecartCommandBlock").setCreativeTab((CreativeTabs)null));
      registerItem(423, "mutton", (new ItemFood(2, 0.3F, true)).setUnlocalizedName("muttonRaw"));
      registerItem(424, "cooked_mutton", (new ItemFood(6, 0.8F, true)).setUnlocalizedName("muttonCooked"));
      registerItem(425, "banner", (new ItemBanner()).setUnlocalizedName("banner"));
      registerItem(426, "end_crystal", new ItemEndCrystal());
      registerItem(427, "spruce_door", (new ItemDoor(Blocks.SPRUCE_DOOR)).setUnlocalizedName("doorSpruce"));
      registerItem(428, "birch_door", (new ItemDoor(Blocks.BIRCH_DOOR)).setUnlocalizedName("doorBirch"));
      registerItem(429, "jungle_door", (new ItemDoor(Blocks.JUNGLE_DOOR)).setUnlocalizedName("doorJungle"));
      registerItem(430, "acacia_door", (new ItemDoor(Blocks.ACACIA_DOOR)).setUnlocalizedName("doorAcacia"));
      registerItem(431, "dark_oak_door", (new ItemDoor(Blocks.DARK_OAK_DOOR)).setUnlocalizedName("doorDarkOak"));
      registerItem(432, "chorus_fruit", (new ItemChorusFruit(4, 0.3F)).setAlwaysEdible().setUnlocalizedName("chorusFruit").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(433, "chorus_fruit_popped", (new Item()).setUnlocalizedName("chorusFruitPopped").setCreativeTab(CreativeTabs.MATERIALS));
      registerItem(434, "beetroot", (new ItemFood(1, 0.6F, false)).setUnlocalizedName("beetroot"));
      registerItem(435, "beetroot_seeds", (new ItemSeeds(Blocks.BEETROOTS, Blocks.FARMLAND)).setUnlocalizedName("beetroot_seeds"));
      registerItem(436, "beetroot_soup", (new ItemSoup(6)).setUnlocalizedName("beetroot_soup"));
      registerItem(437, "dragon_breath", (new Item()).setCreativeTab(CreativeTabs.BREWING).setUnlocalizedName("dragon_breath").setContainerItem(var1));
      registerItem(438, "splash_potion", (new ItemSplashPotion()).setUnlocalizedName("splash_potion"));
      registerItem(439, "spectral_arrow", (new ItemSpectralArrow()).setUnlocalizedName("spectral_arrow"));
      registerItem(440, "tipped_arrow", (new ItemTippedArrow()).setUnlocalizedName("tipped_arrow"));
      registerItem(441, "lingering_potion", (new ItemLingeringPotion()).setUnlocalizedName("lingering_potion"));
      registerItem(442, "shield", (new ItemShield()).setUnlocalizedName("shield"));
      registerItem(443, "elytra", (new ItemElytra()).setUnlocalizedName("elytra"));
      registerItem(444, "spruce_boat", new ItemBoat(EntityBoat.Type.SPRUCE));
      registerItem(445, "birch_boat", new ItemBoat(EntityBoat.Type.BIRCH));
      registerItem(446, "jungle_boat", new ItemBoat(EntityBoat.Type.JUNGLE));
      registerItem(447, "acacia_boat", new ItemBoat(EntityBoat.Type.ACACIA));
      registerItem(448, "dark_oak_boat", new ItemBoat(EntityBoat.Type.DARK_OAK));
      registerItem(2256, "record_13", (new ItemRecord("13", SoundEvents.RECORD_13)).setUnlocalizedName("record"));
      registerItem(2257, "record_cat", (new ItemRecord("cat", SoundEvents.RECORD_CAT)).setUnlocalizedName("record"));
      registerItem(2258, "record_blocks", (new ItemRecord("blocks", SoundEvents.RECORD_BLOCKS)).setUnlocalizedName("record"));
      registerItem(2259, "record_chirp", (new ItemRecord("chirp", SoundEvents.RECORD_CHIRP)).setUnlocalizedName("record"));
      registerItem(2260, "record_far", (new ItemRecord("far", SoundEvents.RECORD_FAR)).setUnlocalizedName("record"));
      registerItem(2261, "record_mall", (new ItemRecord("mall", SoundEvents.RECORD_MALL)).setUnlocalizedName("record"));
      registerItem(2262, "record_mellohi", (new ItemRecord("mellohi", SoundEvents.RECORD_MELLOHI)).setUnlocalizedName("record"));
      registerItem(2263, "record_stal", (new ItemRecord("stal", SoundEvents.RECORD_STAL)).setUnlocalizedName("record"));
      registerItem(2264, "record_strad", (new ItemRecord("strad", SoundEvents.RECORD_STRAD)).setUnlocalizedName("record"));
      registerItem(2265, "record_ward", (new ItemRecord("ward", SoundEvents.RECORD_WARD)).setUnlocalizedName("record"));
      registerItem(2266, "record_11", (new ItemRecord("11", SoundEvents.RECORD_11)).setUnlocalizedName("record"));
      registerItem(2267, "record_wait", (new ItemRecord("wait", SoundEvents.RECORD_WAIT)).setUnlocalizedName("record"));
   }

   private static void registerItemBlock(Block var0) {
      registerItemBlock(var0, new ItemBlock(var0));
   }

   protected static void registerItemBlock(Block var0, Item var1) {
      registerItem(Block.getIdFromBlock(var0), (ResourceLocation)Block.REGISTRY.getNameForObject(var0), var1);
      BLOCK_TO_ITEM.put(var0, var1);
   }

   private static void registerItem(int var0, String var1, Item var2) {
      registerItem(var0, new ResourceLocation(var1), var2);
   }

   private static void registerItem(int var0, ResourceLocation var1, Item var2) {
      REGISTRY.register(var0, var1, var2);
   }

   public static enum ToolMaterial {
      WOOD(0, 59, 2.0F, 0.0F, 15),
      STONE(1, 131, 4.0F, 1.0F, 5),
      IRON(2, 250, 6.0F, 2.0F, 14),
      DIAMOND(3, 1561, 8.0F, 3.0F, 10),
      GOLD(0, 32, 12.0F, 0.0F, 22);

      private final int harvestLevel;
      private final int maxUses;
      private final float efficiencyOnProperMaterial;
      private final float damageVsEntity;
      private final int enchantability;

      private ToolMaterial(int var3, int var4, float var5, float var6, int var7) {
         this.harvestLevel = var3;
         this.maxUses = var4;
         this.efficiencyOnProperMaterial = var5;
         this.damageVsEntity = var6;
         this.enchantability = var7;
      }

      public int getMaxUses() {
         return this.maxUses;
      }

      public float getEfficiencyOnProperMaterial() {
         return this.efficiencyOnProperMaterial;
      }

      public float getDamageVsEntity() {
         return this.damageVsEntity;
      }

      public int getHarvestLevel() {
         return this.harvestLevel;
      }

      public int getEnchantability() {
         return this.enchantability;
      }

      public Item getRepairItem() {
         if (this == WOOD) {
            return Item.getItemFromBlock(Blocks.PLANKS);
         } else if (this == STONE) {
            return Item.getItemFromBlock(Blocks.COBBLESTONE);
         } else if (this == GOLD) {
            return Items.GOLD_INGOT;
         } else if (this == IRON) {
            return Items.IRON_INGOT;
         } else {
            return this == DIAMOND ? Items.DIAMOND : null;
         }
      }
   }
}
