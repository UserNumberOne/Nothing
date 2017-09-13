package net.minecraft.block;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Block extends Impl {
   private static final ResourceLocation AIR_ID = new ResourceLocation("air");
   public static final RegistryNamespacedDefaultedByKey REGISTRY = GameData.getBlockRegistry();
   /** @deprecated */
   @Deprecated
   public static final ObjectIntIdentityMap BLOCK_STATE_IDS = GameData.getBlockStateIDMap();
   public static final AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   public static final AxisAlignedBB NULL_AABB = null;
   private CreativeTabs displayOnCreativeTab;
   protected boolean fullBlock;
   protected int lightOpacity;
   protected boolean translucent;
   protected int lightValue;
   protected boolean useNeighborBrightness;
   protected float blockHardness;
   protected float blockResistance;
   protected boolean enableStats;
   protected boolean needsRandomTick;
   protected boolean isBlockContainer;
   protected SoundType blockSoundType;
   public float blockParticleGravity;
   protected final Material blockMaterial;
   protected final MapColor blockMapColor;
   public float slipperiness;
   protected final BlockStateContainer blockState;
   private IBlockState defaultBlockState;
   private String unlocalizedName;
   protected ThreadLocal harvesters;
   private ThreadLocal silk_check_state;
   protected static Random RANDOM = new Random();
   private boolean isTileProvider;
   private String[] harvestTool;
   private int[] harvestLevel;
   protected static ThreadLocal captureDrops = new ThreadLocal() {
      protected Boolean initialValue() {
         return false;
      }
   };
   protected static ThreadLocal capturedDrops = new ThreadLocal() {
      protected List initialValue() {
         return new ArrayList();
      }
   };

   public static int getIdFromBlock(Block var0) {
      return REGISTRY.getIDForObject(blockIn);
   }

   public static int getStateId(IBlockState var0) {
      Block block = state.getBlock();
      return getIdFromBlock(block) + (block.getMetaFromState(state) << 12);
   }

   public static Block getBlockById(int var0) {
      Block ret = (Block)REGISTRY.getObjectById(id);
      return ret == null ? Blocks.AIR : ret;
   }

   public static IBlockState getStateById(int var0) {
      int i = id & 4095;
      int j = id >> 12 & 15;
      return getBlockById(i).getStateFromMeta(j);
   }

   public static Block getBlockFromItem(Item var0) {
      return itemIn instanceof ItemBlock ? ((ItemBlock)itemIn).getBlock() : null;
   }

   @Nullable
   public static Block getBlockFromName(String var0) {
      ResourceLocation resourcelocation = new ResourceLocation(name);
      if (REGISTRY.containsKey(resourcelocation)) {
         return (Block)REGISTRY.getObject(resourcelocation);
      } else {
         try {
            return (Block)REGISTRY.getObjectById(Integer.parseInt(name));
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public boolean isFullyOpaque(IBlockState var1) {
      return state.getMaterial().isOpaque() && state.isFullCube();
   }

   /** @deprecated */
   @Deprecated
   public boolean isFullBlock(IBlockState var1) {
      return this.fullBlock;
   }

   /** @deprecated */
   @Deprecated
   public boolean canEntitySpawn(IBlockState var1, Entity var2) {
      return true;
   }

   /** @deprecated */
   @Deprecated
   public int getLightOpacity(IBlockState var1) {
      return this.lightOpacity;
   }

   /** @deprecated */
   @Deprecated
   @SideOnly(Side.CLIENT)
   public boolean isTranslucent(IBlockState var1) {
      return this.translucent;
   }

   /** @deprecated */
   @Deprecated
   public int getLightValue(IBlockState var1) {
      return this.lightValue;
   }

   /** @deprecated */
   @Deprecated
   public boolean getUseNeighborBrightness(IBlockState var1) {
      return this.useNeighborBrightness;
   }

   /** @deprecated */
   @Deprecated
   public Material getMaterial(IBlockState var1) {
      return this.blockMaterial;
   }

   /** @deprecated */
   @Deprecated
   public MapColor getMapColor(IBlockState var1) {
      return this.blockMapColor;
   }

   /** @deprecated */
   @Deprecated
   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState();
   }

   public int getMetaFromState(IBlockState var1) {
      if (state != null && !state.getPropertyKeys().isEmpty()) {
         throw new IllegalArgumentException("Don't know how to convert " + state + " back into data...");
      } else {
         return 0;
      }
   }

   /** @deprecated */
   @Deprecated
   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state;
   }

   /** @deprecated */
   @Deprecated
   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state;
   }

   /** @deprecated */
   @Deprecated
   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state;
   }

   public Block(Material var1, MapColor var2) {
      this.harvesters = new ThreadLocal();
      this.silk_check_state = new ThreadLocal();
      this.isTileProvider = this instanceof ITileEntityProvider;
      this.harvestTool = new String[16];
      this.harvestLevel = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
      this.enableStats = true;
      this.blockSoundType = SoundType.STONE;
      this.blockParticleGravity = 1.0F;
      this.slipperiness = 0.6F;
      this.blockMaterial = blockMaterialIn;
      this.blockMapColor = blockMapColorIn;
      this.blockState = this.createBlockState();
      this.setDefaultState(this.blockState.getBaseState());
      this.fullBlock = this.getDefaultState().isOpaqueCube();
      this.lightOpacity = this.fullBlock ? 255 : 0;
      this.translucent = !blockMaterialIn.blocksLight();
   }

   public Block(Material var1) {
      this(materialIn, materialIn.getMaterialMapColor());
   }

   protected Block setSoundType(SoundType var1) {
      this.blockSoundType = sound;
      return this;
   }

   public Block setLightOpacity(int var1) {
      this.lightOpacity = opacity;
      return this;
   }

   public Block setLightLevel(float var1) {
      this.lightValue = (int)(15.0F * value);
      return this;
   }

   public Block setResistance(float var1) {
      this.blockResistance = resistance * 3.0F;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public boolean isBlockNormalCube(IBlockState var1) {
      return state.getMaterial().blocksMovement() && state.isFullCube();
   }

   /** @deprecated */
   @Deprecated
   public boolean isNormalCube(IBlockState var1) {
      return state.getMaterial().isOpaque() && state.isFullCube() && !state.canProvidePower();
   }

   public boolean causesSuffocation() {
      return this.blockMaterial.blocksMovement() && this.getDefaultState().isFullCube();
   }

   /** @deprecated */
   @Deprecated
   public boolean isFullCube(IBlockState var1) {
      return true;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return !this.blockMaterial.blocksMovement();
   }

   /** @deprecated */
   @Deprecated
   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public boolean isReplaceable(IBlockAccess var1, BlockPos var2) {
      return worldIn.getBlockState(pos).getMaterial().isReplaceable();
   }

   public Block setHardness(float var1) {
      this.blockHardness = hardness;
      if (this.blockResistance < hardness * 5.0F) {
         this.blockResistance = hardness * 5.0F;
      }

      return this;
   }

   public Block setBlockUnbreakable() {
      this.setHardness(-1.0F);
      return this;
   }

   /** @deprecated */
   @Deprecated
   public float getBlockHardness(IBlockState var1, World var2, BlockPos var3) {
      return this.blockHardness;
   }

   public Block setTickRandomly(boolean var1) {
      this.needsRandomTick = shouldTick;
      return this;
   }

   public boolean getTickRandomly() {
      return this.needsRandomTick;
   }

   /** @deprecated */
   @Deprecated
   public boolean hasTileEntity() {
      return this.hasTileEntity(this.getDefaultState());
   }

   /** @deprecated */
   @Deprecated
   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FULL_BLOCK_AABB;
   }

   public boolean isBlockSolid(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      return worldIn.getBlockState(pos).getMaterial().isSolid();
   }

   /** @deprecated */
   @Deprecated
   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
   }

   protected static void addCollisionBoxToList(BlockPos var0, AxisAlignedBB var1, List var2, @Nullable AxisAlignedBB var3) {
      if (blockBox != NULL_AABB) {
         AxisAlignedBB axisalignedbb = blockBox.offset(pos);
         if (entityBox.intersectsWith(axisalignedbb)) {
            collidingBoxes.add(axisalignedbb);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return blockState.getBoundingBox(worldIn, pos);
   }

   /** @deprecated */
   @Deprecated
   @SideOnly(Side.CLIENT)
   public int getPackedLightmapCoords(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      int i = source.getCombinedLight(pos, state.getLightValue(source, pos));
      if (i == 0 && state.getBlock() instanceof BlockSlab) {
         pos = pos.down();
         state = source.getBlockState(pos);
         return source.getCombinedLight(pos, state.getLightValue(source, pos));
      } else {
         return i;
      }
   }

   /** @deprecated */
   @Deprecated
   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      AxisAlignedBB axisalignedbb = blockState.getBoundingBox(blockAccess, pos);
      switch(side) {
      case DOWN:
         if (axisalignedbb.minY > 0.0D) {
            return true;
         }
         break;
      case UP:
         if (axisalignedbb.maxY < 1.0D) {
            return true;
         }
         break;
      case NORTH:
         if (axisalignedbb.minZ > 0.0D) {
            return true;
         }
         break;
      case SOUTH:
         if (axisalignedbb.maxZ < 1.0D) {
            return true;
         }
         break;
      case WEST:
         if (axisalignedbb.minX > 0.0D) {
            return true;
         }
         break;
      case EAST:
         if (axisalignedbb.maxX < 1.0D) {
            return true;
         }
      }

      return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
   }

   /** @deprecated */
   @Deprecated
   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return state.getBoundingBox(worldIn, pos).offset(pos);
   }

   /** @deprecated */
   @Deprecated
   public boolean isOpaqueCube(IBlockState var1) {
      return true;
   }

   public boolean canCollideCheck(IBlockState var1, boolean var2) {
      return this.isCollidable();
   }

   public boolean isCollidable() {
      return true;
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.updateTick(worldIn, pos, state, random);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
   }

   /** @deprecated */
   @Deprecated
   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
   }

   public int tickRate(World var1) {
      return 10;
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (this.hasTileEntity(state) && !(this instanceof BlockContainer)) {
         worldIn.removeTileEntity(pos);
      }

   }

   public int quantityDropped(Random var1) {
      return 1;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(this);
   }

   /** @deprecated */
   @Deprecated
   public float getPlayerRelativeBlockHardness(IBlockState var1, EntityPlayer var2, World var3, BlockPos var4) {
      return ForgeHooks.blockStrength(state, player, worldIn, pos);
   }

   public final void dropBlockAsItem(World var1, BlockPos var2, IBlockState var3, int var4) {
      this.dropBlockAsItemWithChance(worldIn, pos, state, 1.0F, fortune);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
         List items = this.getDrops(worldIn, pos, state, fortune);
         chance = ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, fortune, chance, false, (EntityPlayer)this.harvesters.get());

         for(ItemStack item : items) {
            if (worldIn.rand.nextFloat() <= chance) {
               spawnAsEntity(worldIn, pos, item);
            }
         }
      }

   }

   public static void spawnAsEntity(World var0, BlockPos var1, ItemStack var2) {
      if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) {
         if (((Boolean)captureDrops.get()).booleanValue()) {
            ((List)capturedDrops.get()).add(stack);
            return;
         }

         float f = 0.5F;
         double d0 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         double d1 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         double d2 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
         EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
         entityitem.setDefaultPickupDelay();
         worldIn.spawnEntity(entityitem);
      }

   }

   public void dropXpOnBlockBreak(World var1, BlockPos var2, int var3) {
      if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops")) {
         while(amount > 0) {
            int i = EntityXPOrb.getXPSplit(amount);
            amount -= i;
            worldIn.spawnEntity(new EntityXPOrb(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, i));
         }
      }

   }

   public int damageDropped(IBlockState var1) {
      return 0;
   }

   public float getExplosionResistance(Entity var1) {
      return this.blockResistance / 5.0F;
   }

   /** @deprecated */
   @Deprecated
   @Nullable
   public RayTraceResult collisionRayTrace(IBlockState var1, World var2, BlockPos var3, Vec3d var4, Vec3d var5) {
      return this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
   }

   @Nullable
   protected RayTraceResult rayTrace(BlockPos var1, Vec3d var2, Vec3d var3, AxisAlignedBB var4) {
      Vec3d vec3d = start.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      Vec3d vec3d1 = end.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
      return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), raytraceresult.sideHit, pos);
   }

   public void onBlockDestroyedByExplosion(World var1, BlockPos var2, Explosion var3) {
   }

   public boolean canReplace(World var1, BlockPos var2, EnumFacing var3, @Nullable ItemStack var4) {
      return this.canPlaceBlockOnSide(worldIn, pos, side);
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.SOLID;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return this.canPlaceBlockAt(worldIn, pos);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      return false;
   }

   public void onEntityWalk(World var1, BlockPos var2, Entity var3) {
   }

   /** @deprecated */
   @Deprecated
   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getStateFromMeta(meta);
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
   }

   public Vec3d modifyAcceleration(World var1, BlockPos var2, Entity var3, Vec3d var4) {
      return motion;
   }

   /** @deprecated */
   @Deprecated
   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return 0;
   }

   /** @deprecated */
   @Deprecated
   public boolean canProvidePower(IBlockState var1) {
      return false;
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
   }

   /** @deprecated */
   @Deprecated
   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return 0;
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      player.addStat(StatList.getBlockStats(this));
      player.addExhaustion(0.025F);
      if (this.canSilkHarvest(worldIn, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
         List items = new ArrayList();
         ItemStack itemstack = this.getSilkTouchDrop(state);
         if (itemstack != null) {
            items.add(itemstack);
         }

         ForgeEventFactory.fireBlockHarvesting(items, worldIn, pos, state, 0, 1.0F, true, player);

         for(ItemStack item : items) {
            spawnAsEntity(worldIn, pos, item);
         }
      } else {
         this.harvesters.set(player);
         int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
         this.dropBlockAsItem(worldIn, pos, state, i);
         this.harvesters.set((Object)null);
      }

   }

   /** @deprecated */
   @Deprecated
   protected boolean canSilkHarvest() {
      return this.getDefaultState().isFullCube() && !this.hasTileEntity((IBlockState)this.silk_check_state.get());
   }

   @Nullable
   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      Item item = Item.getItemFromBlock(this);
      if (item == null) {
         return null;
      } else {
         int i = 0;
         if (item.getHasSubtypes()) {
            i = this.getMetaFromState(state);
         }

         return new ItemStack(item, 1, i);
      }
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return this.quantityDropped(random);
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
   }

   public boolean canSpawnInBlock() {
      return !this.blockMaterial.isSolid() && !this.blockMaterial.isLiquid();
   }

   public Block setUnlocalizedName(String var1) {
      this.unlocalizedName = name;
      return this;
   }

   public String getLocalizedName() {
      return I18n.translateToLocal(this.getUnlocalizedName() + ".name");
   }

   public String getUnlocalizedName() {
      return "tile." + this.unlocalizedName;
   }

   /** @deprecated */
   @Deprecated
   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      return false;
   }

   public boolean getEnableStats() {
      return this.enableStats;
   }

   protected Block disableStats() {
      this.enableStats = false;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public EnumPushReaction getMobilityFlag(IBlockState var1) {
      return this.blockMaterial.getMobilityFlag();
   }

   /** @deprecated */
   @Deprecated
   @SideOnly(Side.CLIENT)
   public float getAmbientOcclusionLightValue(IBlockState var1) {
      return state.isBlockNormalCube() ? 0.2F : 1.0F;
   }

   public void onFallenUpon(World var1, BlockPos var2, Entity var3, float var4) {
      entityIn.fall(fallDistance, 1.0F);
   }

   public void onLanded(World var1, Entity var2) {
      entityIn.motionY = 0.0D;
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      Item item = Item.getItemFromBlock(this);
      return item == null ? null : new ItemStack(item, 1, this.damageDropped(state));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      list.add(new ItemStack(itemIn));
   }

   public Block setCreativeTab(CreativeTabs var1) {
      this.displayOnCreativeTab = tab;
      return this;
   }

   public void onBlockHarvested(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
   }

   @SideOnly(Side.CLIENT)
   public CreativeTabs getCreativeTabToDisplayOn() {
      return this.displayOnCreativeTab;
   }

   public void fillWithRain(World var1, BlockPos var2) {
   }

   public boolean requiresUpdates() {
      return true;
   }

   public boolean canDropFromExplosion(Explosion var1) {
      return true;
   }

   public boolean isAssociatedBlock(Block var1) {
      return this == other;
   }

   public static boolean isEqualTo(Block var0, Block var1) {
      return blockIn != null && other != null ? (blockIn == other ? true : blockIn.isAssociatedBlock(other)) : false;
   }

   /** @deprecated */
   @Deprecated
   public boolean hasComparatorInputOverride(IBlockState var1) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[0]);
   }

   public BlockStateContainer getBlockState() {
      return this.blockState;
   }

   protected final void setDefaultState(IBlockState var1) {
      this.defaultBlockState = state;
   }

   public final IBlockState getDefaultState() {
      return this.defaultBlockState;
   }

   @SideOnly(Side.CLIENT)
   public Block.EnumOffsetType getOffsetType() {
      return Block.EnumOffsetType.NONE;
   }

   /** @deprecated */
   @Deprecated
   public SoundType getSoundType() {
      return this.blockSoundType;
   }

   public String toString() {
      return "Block{" + REGISTRY.getNameForObject(this) + "}";
   }

   public int getLightValue(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      IBlockState other = world.getBlockState(pos);
      return other.getBlock() != this ? other.getLightValue(world, pos) : state.getLightValue();
   }

   public boolean isLadder(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityLivingBase var4) {
      return false;
   }

   public boolean isNormalCube(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.getMaterial().isOpaque() && state.isFullCube() && !state.canProvidePower();
   }

   public boolean doesSideBlockRendering(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return state.isOpaqueCube();
   }

   public boolean isSideSolid(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (base_state.isFullyOpaque() && side == EnumFacing.UP) {
         return true;
      } else if (this instanceof BlockSlab) {
         IBlockState state = this.getActualState(base_state, world, pos);
         return base_state.isFullBlock() || state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP && side == EnumFacing.UP || state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN;
      } else if (!(this instanceof BlockFarmland)) {
         if (this instanceof BlockStairs) {
            IBlockState state = this.getActualState(base_state, world, pos);
            boolean flipped = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
            BlockStairs.EnumShape shape = (BlockStairs.EnumShape)state.getValue(BlockStairs.SHAPE);
            EnumFacing facing = (EnumFacing)state.getValue(BlockStairs.FACING);
            if (side == EnumFacing.UP) {
               return flipped;
            } else if (side == EnumFacing.DOWN) {
               return !flipped;
            } else if (facing == side) {
               return true;
            } else {
               if (flipped) {
                  if (shape == BlockStairs.EnumShape.INNER_LEFT) {
                     return side == facing.rotateYCCW();
                  }

                  if (shape == BlockStairs.EnumShape.INNER_RIGHT) {
                     return side == facing.rotateY();
                  }
               } else {
                  if (shape == BlockStairs.EnumShape.INNER_LEFT) {
                     return side == facing.rotateY();
                  }

                  if (shape == BlockStairs.EnumShape.INNER_RIGHT) {
                     return side == facing.rotateYCCW();
                  }
               }

               return false;
            }
         } else if (this instanceof BlockSnow) {
            IBlockState state = this.getActualState(base_state, world, pos);
            return ((Integer)state.getValue(BlockSnow.LAYERS)).intValue() >= 8;
         } else if (this instanceof BlockHopper && side == EnumFacing.UP) {
            return true;
         } else {
            return this instanceof BlockCompressedPowered ? true : this.isNormalCube(base_state, world, pos);
         }
      } else {
         return side != EnumFacing.DOWN && side != EnumFacing.UP;
      }
   }

   public boolean isBurning(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   public boolean isAir(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.getMaterial() == Material.AIR;
   }

   public boolean canHarvestBlock(IBlockAccess var1, BlockPos var2, EntityPlayer var3) {
      return ForgeHooks.canHarvestBlock(this, player, world, pos);
   }

   public boolean removedByPlayer(IBlockState var1, World var2, BlockPos var3, EntityPlayer var4, boolean var5) {
      this.onBlockHarvested(world, pos, state, player);
      return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
   }

   public int getFlammability(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      return Blocks.FIRE.getFlammability(this);
   }

   public boolean isFlammable(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      return this.getFlammability(world, pos, face) > 0;
   }

   public int getFireSpreadSpeed(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      return Blocks.FIRE.getEncouragement(this);
   }

   public boolean isFireSource(World var1, BlockPos var2, EnumFacing var3) {
      if (this == Blocks.NETHERRACK && side == EnumFacing.UP) {
         return true;
      } else {
         return world.provider instanceof WorldProviderEnd && this == Blocks.BEDROCK && side == EnumFacing.UP;
      }
   }

   public boolean hasTileEntity(IBlockState var1) {
      return this.isTileProvider;
   }

   public TileEntity createTileEntity(World var1, IBlockState var2) {
      return this.isTileProvider ? ((ITileEntityProvider)this).createNewTileEntity(world, this.getMetaFromState(state)) : null;
   }

   public int quantityDropped(IBlockState var1, int var2, Random var3) {
      return this.quantityDroppedWithBonus(fortune, random);
   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      List ret = new ArrayList();
      Random rand = world instanceof World ? ((World)world).rand : RANDOM;
      int count = this.quantityDropped(state, fortune, rand);

      for(int i = 0; i < count; ++i) {
         Item item = this.getItemDropped(state, rand, fortune);
         if (item != null) {
            ret.add(new ItemStack(item, 1, this.damageDropped(state)));
         }
      }

      return ret;
   }

   public boolean canSilkHarvest(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4) {
      this.silk_check_state.set(state);
      boolean ret = this.canSilkHarvest();
      this.silk_check_state.set((Object)null);
      return ret;
   }

   public boolean canCreatureSpawn(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityLiving.SpawnPlacementType var4) {
      return this.isSideSolid(state, world, pos, EnumFacing.UP);
   }

   public boolean isBed(IBlockState var1, IBlockAccess var2, BlockPos var3, Entity var4) {
      return this == Blocks.BED;
   }

   public BlockPos getBedSpawnPosition(IBlockState var1, IBlockAccess var2, BlockPos var3, EntityPlayer var4) {
      return world instanceof World ? BlockBed.getSafeExitLocation((World)world, pos, 0) : null;
   }

   public void setBedOccupied(IBlockAccess var1, BlockPos var2, EntityPlayer var3, boolean var4) {
      if (world instanceof World) {
         IBlockState state = world.getBlockState(pos);
         state = state.getBlock().getActualState(state, world, pos);
         state = state.withProperty(BlockBed.OCCUPIED, Boolean.valueOf(occupied));
         ((World)world).setBlockState(pos, state, 4);
      }

   }

   public EnumFacing getBedDirection(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return (EnumFacing)this.getActualState(state, world, pos).getValue(BlockHorizontal.FACING);
   }

   public boolean isBedFoot(IBlockAccess var1, BlockPos var2) {
      return this.getActualState(world.getBlockState(pos), world, pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT;
   }

   public void beginLeavesDecay(IBlockState var1, World var2, BlockPos var3) {
   }

   public boolean canSustainLeaves(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return false;
   }

   public boolean isLeaves(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.getMaterial() == Material.LEAVES;
   }

   public boolean canBeReplacedByLeaves(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return this.isAir(state, world, pos) || this.isLeaves(state, world, pos);
   }

   public boolean isWood(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   public boolean isReplaceableOreGen(IBlockState var1, IBlockAccess var2, BlockPos var3, Predicate var4) {
      return target.apply(state);
   }

   public float getExplosionResistance(World var1, BlockPos var2, Entity var3, Explosion var4) {
      return this.getExplosionResistance(exploder);
   }

   public void onBlockExploded(World var1, BlockPos var2, Explosion var3) {
      world.setBlockToAir(pos);
      this.onBlockDestroyedByExplosion(world, pos, explosion);
   }

   public boolean canConnectRedstone(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return state.canProvidePower() && side != null;
   }

   public boolean canPlaceTorchOnTop(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      if (state.isSideSolid(world, pos, EnumFacing.UP)) {
         return true;
      } else {
         return this instanceof BlockFence || this == Blocks.GLASS || this == Blocks.COBBLESTONE_WALL || this == Blocks.STAINED_GLASS;
      }
   }

   public ItemStack getPickBlock(IBlockState var1, RayTraceResult var2, World var3, BlockPos var4, EntityPlayer var5) {
      return this.getItem(world, pos, state);
   }

   public boolean isFoliage(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   public boolean addLandingEffects(IBlockState var1, WorldServer var2, BlockPos var3, IBlockState var4, EntityLivingBase var5, int var6) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean addHitEffects(IBlockState var1, World var2, RayTraceResult var3, ParticleManager var4) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean addDestroyEffects(World var1, BlockPos var2, ParticleManager var3) {
      return false;
   }

   public boolean canSustainPlant(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4, IPlantable var5) {
      IBlockState plant = plantable.getPlant(world, pos.offset(direction));
      EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));
      if (plant.getBlock() == Blocks.CACTUS) {
         return this == Blocks.CACTUS || this == Blocks.SAND;
      } else if (plant.getBlock() == Blocks.REEDS && this == Blocks.REEDS) {
         return true;
      } else if (plantable instanceof BlockBush && ((BlockBush)plantable).canSustainBush(state)) {
         return true;
      } else {
         switch(plantType) {
         case Desert:
            return this == Blocks.SAND || this == Blocks.HARDENED_CLAY || this == Blocks.STAINED_HARDENED_CLAY;
         case Nether:
            return this == Blocks.SOUL_SAND;
         case Crop:
            return this == Blocks.FARMLAND;
         case Cave:
            return state.isSideSolid(world, pos, EnumFacing.UP);
         case Plains:
            return this == Blocks.GRASS || this == Blocks.DIRT || this == Blocks.FARMLAND;
         case Water:
            return state.getMaterial() == Material.WATER && ((Integer)state.getValue(BlockLiquid.LEVEL)).intValue() == 0;
         case Beach:
            boolean isBeach = this == Blocks.GRASS || this == Blocks.DIRT || this == Blocks.SAND;
            boolean hasWater = world.getBlockState(pos.east()).getMaterial() == Material.WATER || world.getBlockState(pos.west()).getMaterial() == Material.WATER || world.getBlockState(pos.north()).getMaterial() == Material.WATER || world.getBlockState(pos.south()).getMaterial() == Material.WATER;
            return isBeach && hasWater;
         default:
            return false;
         }
      }
   }

   public void onPlantGrow(IBlockState var1, World var2, BlockPos var3, BlockPos var4) {
      if (this == Blocks.GRASS || this == Blocks.FARMLAND) {
         world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
      }

   }

   public boolean isFertile(World var1, BlockPos var2) {
      if (this == Blocks.FARMLAND) {
         return ((Integer)world.getBlockState(pos).getValue(BlockFarmland.MOISTURE)).intValue() > 0;
      } else {
         return false;
      }
   }

   public int getLightOpacity(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state.getLightOpacity();
   }

   public boolean canEntityDestroy(IBlockState var1, IBlockAccess var2, BlockPos var3, Entity var4) {
      if (!(entity instanceof EntityDragon)) {
         return !(entity instanceof EntityWither) && !(entity instanceof EntityWitherSkull) ? true : EntityWither.canDestroyBlock(this);
      } else {
         return this != Blocks.BARRIER && this != Blocks.OBSIDIAN && this != Blocks.END_STONE && this != Blocks.BEDROCK && this != Blocks.END_PORTAL && this != Blocks.END_PORTAL_FRAME && this != Blocks.COMMAND_BLOCK && this != Blocks.REPEATING_COMMAND_BLOCK && this != Blocks.CHAIN_COMMAND_BLOCK && this != Blocks.IRON_BARS && this != Blocks.END_GATEWAY;
      }
   }

   public boolean isBeaconBase(IBlockAccess var1, BlockPos var2, BlockPos var3) {
      return this == Blocks.EMERALD_BLOCK || this == Blocks.GOLD_BLOCK || this == Blocks.DIAMOND_BLOCK || this == Blocks.IRON_BLOCK;
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var5 = state.getProperties().keySet().iterator();

      while(var5.hasNext()) {
         IProperty prop = (IProperty)var5.next();
         if (prop.getName().equals("facing") || prop.getName().equals("rotation")) {
            world.setBlockState(pos, state.cycleProperty(prop));
            return true;
         }
      }

      return false;
   }

   public EnumFacing[] getValidRotations(World var1, BlockPos var2) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var4 = state.getProperties().keySet().iterator();

      while(var4.hasNext()) {
         IProperty prop = (IProperty)var4.next();
         if (prop.getName().equals("facing") && prop.getValueClass() == EnumFacing.class) {
            Collection values = prop.getAllowedValues();
            return (EnumFacing[])values.toArray(new EnumFacing[values.size()]);
         }
      }

      return null;
   }

   public float getEnchantPowerBonus(World var1, BlockPos var2) {
      return this == Blocks.BOOKSHELF ? 1.0F : 0.0F;
   }

   public boolean recolorBlock(World var1, BlockPos var2, EnumFacing var3, EnumDyeColor var4) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var6 = state.getProperties().keySet().iterator();

      while(var6.hasNext()) {
         IProperty prop = (IProperty)var6.next();
         if (prop.getName().equals("color") && prop.getValueClass() == EnumDyeColor.class) {
            EnumDyeColor current = (EnumDyeColor)state.getValue(prop);
            if (current != color) {
               world.setBlockState(pos, state.withProperty(prop, color));
               return true;
            }
         }
      }

      return false;
   }

   public int getExpDrop(IBlockState var1, IBlockAccess var2, BlockPos var3, int var4) {
      return 0;
   }

   public void onNeighborChange(IBlockAccess var1, BlockPos var2, BlockPos var3) {
   }

   public boolean shouldCheckWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return state.isNormalCube();
   }

   public boolean getWeakChanges(IBlockAccess var1, BlockPos var2) {
      return false;
   }

   public void setHarvestLevel(String var1, int var2) {
      Iterator itr = this.getBlockState().getValidStates().iterator();

      while(((Iterator)itr).hasNext()) {
         this.setHarvestLevel(toolClass, level, (IBlockState)itr.next());
      }

   }

   public void setHarvestLevel(String var1, int var2, IBlockState var3) {
      int idx = this.getMetaFromState(state);
      this.harvestTool[idx] = toolClass;
      this.harvestLevel[idx] = level;
   }

   public String getHarvestTool(IBlockState var1) {
      return this.harvestTool[this.getMetaFromState(state)];
   }

   public int getHarvestLevel(IBlockState var1) {
      return this.harvestLevel[this.getMetaFromState(state)];
   }

   public boolean isToolEffective(String var1, IBlockState var2) {
      if (!"pickaxe".equals(type) || this != Blocks.REDSTONE_ORE && this != Blocks.LIT_REDSTONE_ORE && this != Blocks.OBSIDIAN) {
         return type != null && type.equals(this.getHarvestTool(state));
      } else {
         return false;
      }
   }

   public IBlockState getExtendedState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return state;
   }

   public Boolean isEntityInsideMaterial(IBlockAccess var1, BlockPos var2, IBlockState var3, Entity var4, double var5, Material var7, boolean var8) {
      return null;
   }

   public Boolean isAABBInsideMaterial(World var1, BlockPos var2, AxisAlignedBB var3, Material var4) {
      return null;
   }

   /** @deprecated */
   @Deprecated
   public boolean canRenderInLayer(BlockRenderLayer var1) {
      return this.getBlockLayer() == layer;
   }

   public boolean canRenderInLayer(IBlockState var1, BlockRenderLayer var2) {
      return this.canRenderInLayer(layer);
   }

   protected List captureDrops(boolean var1) {
      if (start) {
         captureDrops.set(Boolean.valueOf(true));
         ((List)capturedDrops.get()).clear();
         return null;
      } else {
         captureDrops.set(Boolean.valueOf(false));
         return (List)capturedDrops.get();
      }
   }

   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
   }

   public SoundType getSoundType(IBlockState var1, World var2, BlockPos var3, @Nullable Entity var4) {
      return this.getSoundType();
   }

   @Nullable
   public float[] getBeaconColorMultiplier(IBlockState var1, World var2, BlockPos var3, BlockPos var4) {
      return null;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8, ItemStack var9) {
      return this.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
   }

   public static void registerBlocks() {
      registerBlock(0, AIR_ID, (new BlockAir()).setUnlocalizedName("air"));
      registerBlock(1, "stone", (new BlockStone()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stone"));
      registerBlock(2, "grass", (new BlockGrass()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("grass"));
      registerBlock(3, "dirt", (new BlockDirt()).setHardness(0.5F).setSoundType(SoundType.GROUND).setUnlocalizedName("dirt"));
      Block block = (new Block(Material.ROCK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stonebrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
      registerBlock(4, "cobblestone", block);
      Block block1 = (new BlockPlanks()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("wood");
      registerBlock(5, "planks", block1);
      registerBlock(6, "sapling", (new BlockSapling()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("sapling"));
      registerBlock(7, "bedrock", (new BlockEmptyDrops(Material.ROCK)).setBlockUnbreakable().setResistance(6000000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("bedrock").disableStats().setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(8, "flowing_water", (new BlockDynamicLiquid(Material.WATER)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
      registerBlock(9, "water", (new BlockStaticLiquid(Material.WATER)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
      registerBlock(10, "flowing_lava", (new BlockDynamicLiquid(Material.LAVA)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
      registerBlock(11, "lava", (new BlockStaticLiquid(Material.LAVA)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
      registerBlock(12, "sand", (new BlockSand()).setHardness(0.5F).setSoundType(SoundType.SAND).setUnlocalizedName("sand"));
      registerBlock(13, "gravel", (new BlockGravel()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("gravel"));
      registerBlock(14, "gold_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreGold"));
      registerBlock(15, "iron_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreIron"));
      registerBlock(16, "coal_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreCoal"));
      registerBlock(17, "log", (new BlockOldLog()).setUnlocalizedName("log"));
      registerBlock(18, "leaves", (new BlockOldLeaf()).setUnlocalizedName("leaves"));
      registerBlock(19, "sponge", (new BlockSponge()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("sponge"));
      registerBlock(20, "glass", (new BlockGlass(Material.GLASS, false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("glass"));
      registerBlock(21, "lapis_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreLapis"));
      registerBlock(22, "lapis_block", (new Block(Material.IRON, MapColor.LAPIS)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blockLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(23, "dispenser", (new BlockDispenser()).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("dispenser"));
      Block block2 = (new BlockSandStone()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("sandStone");
      registerBlock(24, "sandstone", block2);
      registerBlock(25, "noteblock", (new BlockNote()).setSoundType(SoundType.WOOD).setHardness(0.8F).setUnlocalizedName("musicBlock"));
      registerBlock(26, "bed", (new BlockBed()).setSoundType(SoundType.WOOD).setHardness(0.2F).setUnlocalizedName("bed").disableStats());
      registerBlock(27, "golden_rail", (new BlockRailPowered()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("goldenRail"));
      registerBlock(28, "detector_rail", (new BlockRailDetector()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("detectorRail"));
      registerBlock(29, "sticky_piston", (new BlockPistonBase(true)).setUnlocalizedName("pistonStickyBase"));
      registerBlock(30, "web", (new BlockWeb()).setLightOpacity(1).setHardness(4.0F).setUnlocalizedName("web"));
      registerBlock(31, "tallgrass", (new BlockTallGrass()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("tallgrass"));
      registerBlock(32, "deadbush", (new BlockDeadBush()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("deadbush"));
      registerBlock(33, "piston", (new BlockPistonBase(false)).setUnlocalizedName("pistonBase"));
      registerBlock(34, "piston_head", (new BlockPistonExtension()).setUnlocalizedName("pistonBase"));
      registerBlock(35, "wool", (new BlockColored(Material.CLOTH)).setHardness(0.8F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cloth"));
      registerBlock(36, "piston_extension", new BlockPistonMoving());
      registerBlock(37, "yellow_flower", (new BlockYellowFlower()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("flower1"));
      registerBlock(38, "red_flower", (new BlockRedFlower()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("flower2"));
      Block block3 = (new BlockMushroom()).setHardness(0.0F).setSoundType(SoundType.PLANT).setLightLevel(0.125F).setUnlocalizedName("mushroom");
      registerBlock(39, "brown_mushroom", block3);
      Block block4 = (new BlockMushroom()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("mushroom");
      registerBlock(40, "red_mushroom", block4);
      registerBlock(41, "gold_block", (new Block(Material.IRON, MapColor.GOLD)).setHardness(3.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockGold").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(42, "iron_block", (new Block(Material.IRON, MapColor.IRON)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockIron").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(43, "double_stone_slab", (new BlockDoubleStoneSlab()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab"));
      registerBlock(44, "stone_slab", (new BlockHalfStoneSlab()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab"));
      Block block5 = (new Block(Material.ROCK, MapColor.RED)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("brick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
      registerBlock(45, "brick_block", block5);
      registerBlock(46, "tnt", (new BlockTNT()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("tnt"));
      registerBlock(47, "bookshelf", (new BlockBookshelf()).setHardness(1.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("bookshelf"));
      registerBlock(48, "mossy_cobblestone", (new Block(Material.ROCK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneMoss").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(49, "obsidian", (new BlockObsidian()).setHardness(50.0F).setResistance(2000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("obsidian"));
      registerBlock(50, "torch", (new BlockTorch()).setHardness(0.0F).setLightLevel(0.9375F).setSoundType(SoundType.WOOD).setUnlocalizedName("torch"));
      registerBlock(51, "fire", (new BlockFire()).setHardness(0.0F).setLightLevel(1.0F).setSoundType(SoundType.CLOTH).setUnlocalizedName("fire").disableStats());
      registerBlock(52, "mob_spawner", (new BlockMobSpawner()).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("mobSpawner").disableStats());
      registerBlock(53, "oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK))).setUnlocalizedName("stairsWood"));
      registerBlock(54, "chest", (new BlockChest(BlockChest.Type.BASIC)).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("chest"));
      registerBlock(55, "redstone_wire", (new BlockRedstoneWire()).setHardness(0.0F).setSoundType(SoundType.STONE).setUnlocalizedName("redstoneDust").disableStats());
      registerBlock(56, "diamond_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreDiamond"));
      registerBlock(57, "diamond_block", (new Block(Material.IRON, MapColor.DIAMOND)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockDiamond").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(58, "crafting_table", (new BlockWorkbench()).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("workbench"));
      registerBlock(59, "wheat", (new BlockCrops()).setUnlocalizedName("crops"));
      Block block6 = (new BlockFarmland()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("farmland");
      registerBlock(60, "farmland", block6);
      registerBlock(61, "furnace", (new BlockFurnace(false)).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("furnace").setCreativeTab(CreativeTabs.DECORATIONS));
      registerBlock(62, "lit_furnace", (new BlockFurnace(true)).setHardness(3.5F).setSoundType(SoundType.STONE).setLightLevel(0.875F).setUnlocalizedName("furnace"));
      registerBlock(63, "standing_sign", (new BlockStandingSign()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("sign").disableStats());
      registerBlock(64, "wooden_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorOak").disableStats());
      registerBlock(65, "ladder", (new BlockLadder()).setHardness(0.4F).setSoundType(SoundType.LADDER).setUnlocalizedName("ladder"));
      registerBlock(66, "rail", (new BlockRail()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("rail"));
      registerBlock(67, "stone_stairs", (new BlockStairs(block.getDefaultState())).setUnlocalizedName("stairsStone"));
      registerBlock(68, "wall_sign", (new BlockWallSign()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("sign").disableStats());
      registerBlock(69, "lever", (new BlockLever()).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("lever"));
      registerBlock(70, "stone_pressure_plate", (new BlockPressurePlate(Material.ROCK, BlockPressurePlate.Sensitivity.MOBS)).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("pressurePlateStone"));
      registerBlock(71, "iron_door", (new BlockDoor(Material.IRON)).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("doorIron").disableStats());
      registerBlock(72, "wooden_pressure_plate", (new BlockPressurePlate(Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("pressurePlateWood"));
      registerBlock(73, "redstone_ore", (new BlockRedstoneOre(false)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreRedstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).setLightLevel(0.625F).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreRedstone"));
      registerBlock(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("notGate"));
      registerBlock(76, "redstone_torch", (new BlockRedstoneTorch(true)).setHardness(0.0F).setLightLevel(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("notGate").setCreativeTab(CreativeTabs.REDSTONE));
      registerBlock(77, "stone_button", (new BlockButtonStone()).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("button"));
      registerBlock(78, "snow_layer", (new BlockSnow()).setHardness(0.1F).setSoundType(SoundType.SNOW).setUnlocalizedName("snow").setLightOpacity(0));
      registerBlock(79, "ice", (new BlockIce()).setHardness(0.5F).setLightOpacity(3).setSoundType(SoundType.GLASS).setUnlocalizedName("ice"));
      registerBlock(80, "snow", (new BlockSnowBlock()).setHardness(0.2F).setSoundType(SoundType.SNOW).setUnlocalizedName("snow"));
      registerBlock(81, "cactus", (new BlockCactus()).setHardness(0.4F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cactus"));
      registerBlock(82, "clay", (new BlockClay()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("clay"));
      registerBlock(83, "reeds", (new BlockReed()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("reeds").disableStats());
      registerBlock(84, "jukebox", (new BlockJukebox()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("jukebox"));
      registerBlock(85, "fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.OAK.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("fence"));
      Block block7 = (new BlockPumpkin()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkin");
      registerBlock(86, "pumpkin", block7);
      registerBlock(87, "netherrack", (new BlockNetherrack()).setHardness(0.4F).setSoundType(SoundType.STONE).setUnlocalizedName("hellrock"));
      registerBlock(88, "soul_sand", (new BlockSoulSand()).setHardness(0.5F).setSoundType(SoundType.SAND).setUnlocalizedName("hellsand"));
      registerBlock(89, "glowstone", (new BlockGlowstone(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setLightLevel(1.0F).setUnlocalizedName("lightgem"));
      registerBlock(90, "portal", (new BlockPortal()).setHardness(-1.0F).setSoundType(SoundType.GLASS).setLightLevel(0.75F).setUnlocalizedName("portal"));
      registerBlock(91, "lit_pumpkin", (new BlockPumpkin()).setHardness(1.0F).setSoundType(SoundType.WOOD).setLightLevel(1.0F).setUnlocalizedName("litpumpkin"));
      registerBlock(92, "cake", (new BlockCake()).setHardness(0.5F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cake").disableStats());
      registerBlock(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("diode").disableStats());
      registerBlock(94, "powered_repeater", (new BlockRedstoneRepeater(true)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("diode").disableStats());
      registerBlock(95, "stained_glass", (new BlockStainedGlass(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("stainedGlass"));
      registerBlock(96, "trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("trapdoor").disableStats());
      registerBlock(97, "monster_egg", (new BlockSilverfish()).setHardness(0.75F).setUnlocalizedName("monsterStoneEgg"));
      Block block8 = (new BlockStoneBrick()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stonebricksmooth");
      registerBlock(98, "stonebrick", block8);
      registerBlock(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.WOOD, MapColor.DIRT, block3)).setHardness(0.2F).setSoundType(SoundType.WOOD).setUnlocalizedName("mushroom"));
      registerBlock(100, "red_mushroom_block", (new BlockHugeMushroom(Material.WOOD, MapColor.RED, block4)).setHardness(0.2F).setSoundType(SoundType.WOOD).setUnlocalizedName("mushroom"));
      registerBlock(101, "iron_bars", (new BlockPane(Material.IRON, true)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("fenceIron"));
      registerBlock(102, "glass_pane", (new BlockPane(Material.GLASS, false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("thinGlass"));
      Block block9 = (new BlockMelon()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("melon");
      registerBlock(103, "melon_block", block9);
      registerBlock(104, "pumpkin_stem", (new BlockStem(block7)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkinStem"));
      registerBlock(105, "melon_stem", (new BlockStem(block9)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkinStem"));
      registerBlock(106, "vine", (new BlockVine()).setHardness(0.2F).setSoundType(SoundType.PLANT).setUnlocalizedName("vine"));
      registerBlock(107, "fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.OAK)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("fenceGate"));
      registerBlock(108, "brick_stairs", (new BlockStairs(block5.getDefaultState())).setUnlocalizedName("stairsBrick"));
      registerBlock(109, "stone_brick_stairs", (new BlockStairs(block8.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT))).setUnlocalizedName("stairsStoneBrickSmooth"));
      registerBlock(110, "mycelium", (new BlockMycelium()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("mycel"));
      registerBlock(111, "waterlily", (new BlockLilyPad()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("waterlily"));
      Block block10 = (new BlockNetherBrick()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherBrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
      registerBlock(112, "nether_brick", block10);
      registerBlock(113, "nether_brick_fence", (new BlockFence(Material.ROCK, MapColor.NETHERRACK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherFence"));
      registerBlock(114, "nether_brick_stairs", (new BlockStairs(block10.getDefaultState())).setUnlocalizedName("stairsNetherBrick"));
      registerBlock(115, "nether_wart", (new BlockNetherWart()).setUnlocalizedName("netherStalk"));
      registerBlock(116, "enchanting_table", (new BlockEnchantmentTable()).setHardness(5.0F).setResistance(2000.0F).setUnlocalizedName("enchantmentTable"));
      registerBlock(117, "brewing_stand", (new BlockBrewingStand()).setHardness(0.5F).setLightLevel(0.125F).setUnlocalizedName("brewingStand"));
      registerBlock(118, "cauldron", (new BlockCauldron()).setHardness(2.0F).setUnlocalizedName("cauldron"));
      registerBlock(119, "end_portal", (new BlockEndPortal(Material.PORTAL)).setHardness(-1.0F).setResistance(6000000.0F));
      registerBlock(120, "end_portal_frame", (new BlockEndPortalFrame()).setSoundType(SoundType.GLASS).setLightLevel(0.125F).setHardness(-1.0F).setUnlocalizedName("endPortalFrame").setResistance(6000000.0F).setCreativeTab(CreativeTabs.DECORATIONS));
      registerBlock(121, "end_stone", (new Block(Material.ROCK, MapColor.SAND)).setHardness(3.0F).setResistance(15.0F).setSoundType(SoundType.STONE).setUnlocalizedName("whiteStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(122, "dragon_egg", (new BlockDragonEgg()).setHardness(3.0F).setResistance(15.0F).setSoundType(SoundType.STONE).setLightLevel(0.125F).setUnlocalizedName("dragonEgg"));
      registerBlock(123, "redstone_lamp", (new BlockRedstoneLight(false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("redstoneLight").setCreativeTab(CreativeTabs.REDSTONE));
      registerBlock(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("redstoneLight"));
      registerBlock(125, "double_wooden_slab", (new BlockDoubleWoodSlab()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("woodSlab"));
      registerBlock(126, "wooden_slab", (new BlockHalfWoodSlab()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("woodSlab"));
      registerBlock(127, "cocoa", (new BlockCocoa()).setHardness(0.2F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("cocoa"));
      registerBlock(128, "sandstone_stairs", (new BlockStairs(block2.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))).setUnlocalizedName("stairsSandStone"));
      registerBlock(129, "emerald_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreEmerald"));
      registerBlock(130, "ender_chest", (new BlockEnderChest()).setHardness(22.5F).setResistance(1000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("enderChest").setLightLevel(0.5F));
      registerBlock(131, "tripwire_hook", (new BlockTripWireHook()).setUnlocalizedName("tripWireSource"));
      registerBlock(132, "tripwire", (new BlockTripWire()).setUnlocalizedName("tripWire"));
      registerBlock(133, "emerald_block", (new Block(Material.IRON, MapColor.EMERALD)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockEmerald").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(134, "spruce_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE))).setUnlocalizedName("stairsWoodSpruce"));
      registerBlock(135, "birch_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH))).setUnlocalizedName("stairsWoodBirch"));
      registerBlock(136, "jungle_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE))).setUnlocalizedName("stairsWoodJungle"));
      registerBlock(137, "command_block", (new BlockCommandBlock(MapColor.BROWN)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("commandBlock"));
      registerBlock(138, "beacon", (new BlockBeacon()).setUnlocalizedName("beacon").setLightLevel(1.0F));
      registerBlock(139, "cobblestone_wall", (new BlockWall(block)).setUnlocalizedName("cobbleWall"));
      registerBlock(140, "flower_pot", (new BlockFlowerPot()).setHardness(0.0F).setSoundType(SoundType.STONE).setUnlocalizedName("flowerPot"));
      registerBlock(141, "carrots", (new BlockCarrot()).setUnlocalizedName("carrots"));
      registerBlock(142, "potatoes", (new BlockPotato()).setUnlocalizedName("potatoes"));
      registerBlock(143, "wooden_button", (new BlockButtonWood()).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("button"));
      registerBlock(144, "skull", (new BlockSkull()).setHardness(1.0F).setSoundType(SoundType.STONE).setUnlocalizedName("skull"));
      registerBlock(145, "anvil", (new BlockAnvil()).setHardness(5.0F).setSoundType(SoundType.ANVIL).setResistance(2000.0F).setUnlocalizedName("anvil"));
      registerBlock(146, "trapped_chest", (new BlockChest(BlockChest.Type.TRAP)).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("chestTrap"));
      registerBlock(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.IRON, 15, MapColor.GOLD)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("weightedPlate_light"));
      registerBlock(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.IRON, 150)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("weightedPlate_heavy"));
      registerBlock(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("comparator").disableStats());
      registerBlock(150, "powered_comparator", (new BlockRedstoneComparator(true)).setHardness(0.0F).setLightLevel(0.625F).setSoundType(SoundType.WOOD).setUnlocalizedName("comparator").disableStats());
      registerBlock(151, "daylight_detector", new BlockDaylightDetector(false));
      registerBlock(152, "redstone_block", (new BlockCompressedPowered(Material.IRON, MapColor.TNT)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockRedstone").setCreativeTab(CreativeTabs.REDSTONE));
      registerBlock(153, "quartz_ore", (new BlockOre(MapColor.NETHERRACK)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherquartz"));
      registerBlock(154, "hopper", (new BlockHopper()).setHardness(3.0F).setResistance(8.0F).setSoundType(SoundType.METAL).setUnlocalizedName("hopper"));
      Block block11 = (new BlockQuartz()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("quartzBlock");
      registerBlock(155, "quartz_block", block11);
      registerBlock(156, "quartz_stairs", (new BlockStairs(block11.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.DEFAULT))).setUnlocalizedName("stairsQuartz"));
      registerBlock(157, "activator_rail", (new BlockRailPowered()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("activatorRail"));
      registerBlock(158, "dropper", (new BlockDropper()).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("dropper"));
      registerBlock(159, "stained_hardened_clay", (new BlockColored(Material.ROCK)).setHardness(1.25F).setResistance(7.0F).setSoundType(SoundType.STONE).setUnlocalizedName("clayHardenedStained"));
      registerBlock(160, "stained_glass_pane", (new BlockStainedGlassPane()).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("thinStainedGlass"));
      registerBlock(161, "leaves2", (new BlockNewLeaf()).setUnlocalizedName("leaves"));
      registerBlock(162, "log2", (new BlockNewLog()).setUnlocalizedName("log"));
      registerBlock(163, "acacia_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA))).setUnlocalizedName("stairsWoodAcacia"));
      registerBlock(164, "dark_oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK))).setUnlocalizedName("stairsWoodDarkOak"));
      registerBlock(165, "slime", (new BlockSlime()).setUnlocalizedName("slime").setSoundType(SoundType.SLIME));
      registerBlock(166, "barrier", (new BlockBarrier()).setUnlocalizedName("barrier"));
      registerBlock(167, "iron_trapdoor", (new BlockTrapDoor(Material.IRON)).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("ironTrapdoor").disableStats());
      registerBlock(168, "prismarine", (new BlockPrismarine()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("prismarine"));
      registerBlock(169, "sea_lantern", (new BlockSeaLantern(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setLightLevel(1.0F).setUnlocalizedName("seaLantern"));
      registerBlock(170, "hay_block", (new BlockHay()).setHardness(0.5F).setSoundType(SoundType.PLANT).setUnlocalizedName("hayBlock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(171, "carpet", (new BlockCarpet()).setHardness(0.1F).setSoundType(SoundType.CLOTH).setUnlocalizedName("woolCarpet").setLightOpacity(0));
      registerBlock(172, "hardened_clay", (new BlockHardenedClay()).setHardness(1.25F).setResistance(7.0F).setSoundType(SoundType.STONE).setUnlocalizedName("clayHardened"));
      registerBlock(173, "coal_block", (new Block(Material.ROCK, MapColor.BLACK)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blockCoal").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(174, "packed_ice", (new BlockPackedIce()).setHardness(0.5F).setSoundType(SoundType.GLASS).setUnlocalizedName("icePacked"));
      registerBlock(175, "double_plant", new BlockDoublePlant());
      registerBlock(176, "standing_banner", (new BlockBanner.BlockBannerStanding()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("banner").disableStats());
      registerBlock(177, "wall_banner", (new BlockBanner.BlockBannerHanging()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("banner").disableStats());
      registerBlock(178, "daylight_detector_inverted", new BlockDaylightDetector(true));
      Block block12 = (new BlockRedSandstone()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("redSandStone");
      registerBlock(179, "red_sandstone", block12);
      registerBlock(180, "red_sandstone_stairs", (new BlockStairs(block12.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.SMOOTH))).setUnlocalizedName("stairsRedSandStone"));
      registerBlock(181, "double_stone_slab2", (new BlockDoubleStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab2"));
      registerBlock(182, "stone_slab2", (new BlockHalfStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab2"));
      registerBlock(183, "spruce_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.SPRUCE)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("spruceFenceGate"));
      registerBlock(184, "birch_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.BIRCH)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("birchFenceGate"));
      registerBlock(185, "jungle_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.JUNGLE)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("jungleFenceGate"));
      registerBlock(186, "dark_oak_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.DARK_OAK)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("darkOakFenceGate"));
      registerBlock(187, "acacia_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.ACACIA)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("acaciaFenceGate"));
      registerBlock(188, "spruce_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.SPRUCE.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("spruceFence"));
      registerBlock(189, "birch_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.BIRCH.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("birchFence"));
      registerBlock(190, "jungle_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.JUNGLE.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("jungleFence"));
      registerBlock(191, "dark_oak_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.DARK_OAK.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("darkOakFence"));
      registerBlock(192, "acacia_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.ACACIA.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("acaciaFence"));
      registerBlock(193, "spruce_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorSpruce").disableStats());
      registerBlock(194, "birch_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorBirch").disableStats());
      registerBlock(195, "jungle_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorJungle").disableStats());
      registerBlock(196, "acacia_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorAcacia").disableStats());
      registerBlock(197, "dark_oak_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorDarkOak").disableStats());
      registerBlock(198, "end_rod", (new BlockEndRod()).setHardness(0.0F).setLightLevel(0.9375F).setSoundType(SoundType.WOOD).setUnlocalizedName("endRod"));
      registerBlock(199, "chorus_plant", (new BlockChorusPlant()).setHardness(0.4F).setSoundType(SoundType.WOOD).setUnlocalizedName("chorusPlant"));
      registerBlock(200, "chorus_flower", (new BlockChorusFlower()).setHardness(0.4F).setSoundType(SoundType.WOOD).setUnlocalizedName("chorusFlower"));
      Block block13 = (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("purpurBlock");
      registerBlock(201, "purpur_block", block13);
      registerBlock(202, "purpur_pillar", (new BlockRotatedPillar(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("purpurPillar"));
      registerBlock(203, "purpur_stairs", (new BlockStairs(block13.getDefaultState())).setUnlocalizedName("stairsPurpur"));
      registerBlock(204, "purpur_double_slab", (new BlockPurpurSlab.Double()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("purpurSlab"));
      registerBlock(205, "purpur_slab", (new BlockPurpurSlab.Half()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("purpurSlab"));
      registerBlock(206, "end_bricks", (new Block(Material.ROCK)).setSoundType(SoundType.STONE).setHardness(0.8F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("endBricks"));
      registerBlock(207, "beetroots", (new BlockBeetroot()).setUnlocalizedName("beetroots"));
      Block block14 = (new BlockGrassPath()).setHardness(0.65F).setSoundType(SoundType.PLANT).setUnlocalizedName("grassPath").disableStats();
      registerBlock(208, "grass_path", block14);
      registerBlock(209, "end_gateway", (new BlockEndGateway(Material.PORTAL)).setHardness(-1.0F).setResistance(6000000.0F));
      registerBlock(210, "repeating_command_block", (new BlockCommandBlock(MapColor.PURPLE)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("repeatingCommandBlock"));
      registerBlock(211, "chain_command_block", (new BlockCommandBlock(MapColor.GREEN)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("chainCommandBlock"));
      registerBlock(212, "frosted_ice", (new BlockFrostedIce()).setHardness(0.5F).setLightOpacity(3).setSoundType(SoundType.GLASS).setUnlocalizedName("frostedIce"));
      registerBlock(213, "magma", (new BlockMagma()).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("magma"));
      registerBlock(214, "nether_wart_block", (new Block(Material.GRASS, MapColor.RED)).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("netherWartBlock"));
      registerBlock(215, "red_nether_brick", (new BlockNetherBrick()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("redNetherBrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
      registerBlock(216, "bone_block", (new BlockBone()).setUnlocalizedName("boneBlock"));
      registerBlock(217, "structure_void", (new BlockStructureVoid()).setUnlocalizedName("structureVoid"));
      registerBlock(255, "structure_block", (new BlockStructure()).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("structureBlock"));
      REGISTRY.validateKey();

      for(Block block15 : REGISTRY) {
         if (block15.blockMaterial == Material.AIR) {
            block15.useNeighborBrightness = false;
         } else {
            boolean flag = false;
            boolean flag1 = block15 instanceof BlockStairs;
            boolean flag2 = block15 instanceof BlockSlab;
            boolean flag3 = block15 == block6 || block15 == block14;
            boolean flag4 = block15.translucent;
            boolean flag5 = block15.lightOpacity == 0;
            if (flag1 || flag2 || flag3 || flag4 || flag5) {
               flag = true;
            }

            block15.useNeighborBrightness = flag;
         }
      }

      Set set = Sets.newHashSet(new Block[]{(Block)REGISTRY.getObject(new ResourceLocation("tripwire"))});

      for(Block block16 : REGISTRY) {
         if (set.contains(block16)) {
            for(int i = 0; i < 15; ++i) {
               int j = REGISTRY.getIDForObject(block16) << 4 | i;
               BLOCK_STATE_IDS.put(block16.getStateFromMeta(i), j);
            }
         }
      }

   }

   private static void registerBlock(int var0, ResourceLocation var1, Block var2) {
      REGISTRY.register(id, textualID, block_);
   }

   private static void registerBlock(int var0, String var1, Block var2) {
      registerBlock(id, new ResourceLocation(textualID), block_);
   }

   @SideOnly(Side.CLIENT)
   public static enum EnumOffsetType {
      NONE,
      XZ,
      XYZ;
   }
}
