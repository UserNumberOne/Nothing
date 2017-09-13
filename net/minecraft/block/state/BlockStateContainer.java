package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MapPopulator;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Cartesian;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStateContainer {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private static final Function GET_NAME_FUNC = new Function() {
      @Nullable
      public String apply(@Nullable IProperty var1) {
         return p_apply_1_ == null ? "<NULL>" : p_apply_1_.getName();
      }
   };
   private final Block block;
   private final ImmutableSortedMap properties;
   private final ImmutableList validStates;

   public BlockStateContainer(Block var1, IProperty... var2) {
      this(blockIn, properties, (ImmutableMap)null);
   }

   protected BlockStateContainer.StateImplementation createState(Block var1, ImmutableMap var2, ImmutableMap var3) {
      return new BlockStateContainer.StateImplementation(block, properties);
   }

   protected BlockStateContainer(Block var1, IProperty[] var2, ImmutableMap var3) {
      this.block = blockIn;
      Map map = Maps.newHashMap();

      for(IProperty iproperty : properties) {
         validateProperty(blockIn, iproperty);
         map.put(iproperty.getName(), iproperty);
      }

      this.properties = ImmutableSortedMap.copyOf(map);
      Map map2 = Maps.newLinkedHashMap();
      List list1 = Lists.newArrayList();

      for(List list : Cartesian.cartesianProduct(this.getAllowedValues())) {
         Map map1 = MapPopulator.createMap(this.properties.values(), list);
         BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation = this.createState(blockIn, ImmutableMap.copyOf(map1), unlistedProperties);
         map2.put(map1, blockstatecontainer$stateimplementation);
         list1.add(blockstatecontainer$stateimplementation);
      }

      for(BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation1 : list1) {
         blockstatecontainer$stateimplementation1.buildPropertyValueTable(map2);
      }

      this.validStates = ImmutableList.copyOf(list1);
   }

   public static String validateProperty(Block var0, IProperty var1) {
      String s = property.getName();
      if (!NAME_PATTERN.matcher(s).matches()) {
         throw new IllegalArgumentException("Block: " + block.getClass() + " has invalidly named property: " + s);
      } else {
         for(Comparable t : property.getAllowedValues()) {
            String s1 = property.getName(t);
            if (!NAME_PATTERN.matcher(s1).matches()) {
               throw new IllegalArgumentException("Block: " + block.getClass() + " has property: " + s + " with invalidly named value: " + s1);
            }
         }

         return s;
      }
   }

   public ImmutableList getValidStates() {
      return this.validStates;
   }

   private List getAllowedValues() {
      List list = Lists.newArrayList();
      UnmodifiableIterator var2 = this.properties.values().iterator();

      while(var2.hasNext()) {
         IProperty iproperty = (IProperty)var2.next();
         list.add(iproperty.getAllowedValues());
      }

      return list;
   }

   public IBlockState getBaseState() {
      return (IBlockState)this.validStates.get(0);
   }

   public Block getBlock() {
      return this.block;
   }

   public Collection getProperties() {
      return this.properties.values();
   }

   public String toString() {
      return Objects.toStringHelper(this).add("block", Block.REGISTRY.getNameForObject(this.block)).add("properties", Iterables.transform(this.properties.values(), GET_NAME_FUNC)).toString();
   }

   @Nullable
   public IProperty getProperty(String var1) {
      return (IProperty)this.properties.get(propertyName);
   }

   public static class Builder {
      private final Block block;
      private final List listed = Lists.newArrayList();
      private final List unlisted = Lists.newArrayList();

      public Builder(Block var1) {
         this.block = block;
      }

      public BlockStateContainer.Builder add(IProperty... var1) {
         for(IProperty prop : props) {
            this.listed.add(prop);
         }

         return this;
      }

      public BlockStateContainer.Builder add(IUnlistedProperty... var1) {
         for(IUnlistedProperty prop : props) {
            this.unlisted.add(prop);
         }

         return this;
      }

      public BlockStateContainer build() {
         IProperty[] listed = new IProperty[this.listed.size()];
         listed = (IProperty[])this.listed.toArray(listed);
         if (this.unlisted.size() == 0) {
            return new BlockStateContainer(this.block, listed);
         } else {
            IUnlistedProperty[] unlisted = new IUnlistedProperty[this.unlisted.size()];
            unlisted = (IUnlistedProperty[])this.unlisted.toArray(unlisted);
            return new ExtendedBlockState(this.block, listed, unlisted);
         }
      }
   }

   public static class StateImplementation extends BlockStateBase {
      private final Block block;
      private final ImmutableMap properties;
      protected ImmutableTable propertyValueTable;

      protected StateImplementation(Block var1, ImmutableMap var2) {
         this.block = blockIn;
         this.properties = propertiesIn;
      }

      protected StateImplementation(Block var1, ImmutableMap var2, ImmutableTable var3) {
         this.block = blockIn;
         this.properties = propertiesIn;
         this.propertyValueTable = propertyValueTable;
      }

      public Collection getPropertyKeys() {
         return Collections.unmodifiableCollection(this.properties.keySet());
      }

      public Comparable getValue(IProperty var1) {
         Comparable comparable = (Comparable)this.properties.get(property);
         if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.block.getBlockState());
         } else {
            return (Comparable)property.getValueClass().cast(comparable);
         }
      }

      public IBlockState withProperty(IProperty var1, Comparable var2) {
         Comparable comparable = (Comparable)this.properties.get(property);
         if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.block.getBlockState());
         } else if (comparable == value) {
            return this;
         } else {
            IBlockState iblockstate = (IBlockState)this.propertyValueTable.get(property, value);
            if (iblockstate == null) {
               throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(this.block) + ", it is not an allowed value");
            } else {
               return iblockstate;
            }
         }
      }

      public ImmutableMap getProperties() {
         return this.properties;
      }

      public Block getBlock() {
         return this.block;
      }

      public boolean equals(Object var1) {
         return this == p_equals_1_;
      }

      public int hashCode() {
         return this.properties.hashCode();
      }

      public void buildPropertyValueTable(Map var1) {
         if (this.propertyValueTable != null) {
            throw new IllegalStateException();
         } else {
            Table table = HashBasedTable.create();
            UnmodifiableIterator var3 = this.properties.entrySet().iterator();

            while(var3.hasNext()) {
               Entry entry = (Entry)var3.next();
               IProperty iproperty = (IProperty)entry.getKey();

               for(Comparable comparable : iproperty.getAllowedValues()) {
                  if (comparable != entry.getValue()) {
                     table.put(iproperty, comparable, map.get(this.getPropertiesWithValue(iproperty, comparable)));
                  }
               }
            }

            this.propertyValueTable = ImmutableTable.copyOf(table);
         }
      }

      private Map getPropertiesWithValue(IProperty var1, Comparable var2) {
         Map map = Maps.newHashMap(this.properties);
         map.put(property, value);
         return map;
      }

      public Material getMaterial() {
         return this.block.getMaterial(this);
      }

      public boolean isFullBlock() {
         return this.block.isFullBlock(this);
      }

      public boolean canEntitySpawn(Entity var1) {
         return this.block.canEntitySpawn(this, entityIn);
      }

      public int getLightOpacity() {
         return this.block.getLightOpacity(this);
      }

      public int getLightValue() {
         return this.block.getLightValue(this);
      }

      @SideOnly(Side.CLIENT)
      public boolean isTranslucent() {
         return this.block.isTranslucent(this);
      }

      public boolean useNeighborBrightness() {
         return this.block.getUseNeighborBrightness(this);
      }

      public MapColor getMapColor() {
         return this.block.getMapColor(this);
      }

      public IBlockState withRotation(Rotation var1) {
         return this.block.withRotation(this, rot);
      }

      public IBlockState withMirror(Mirror var1) {
         return this.block.withMirror(this, mirrorIn);
      }

      public boolean isFullCube() {
         return this.block.isFullCube(this);
      }

      public EnumBlockRenderType getRenderType() {
         return this.block.getRenderType(this);
      }

      @SideOnly(Side.CLIENT)
      public int getPackedLightmapCoords(IBlockAccess var1, BlockPos var2) {
         return this.block.getPackedLightmapCoords(this, source, pos);
      }

      @SideOnly(Side.CLIENT)
      public float getAmbientOcclusionLightValue() {
         return this.block.getAmbientOcclusionLightValue(this);
      }

      public boolean isBlockNormalCube() {
         return this.block.isBlockNormalCube(this);
      }

      public boolean isNormalCube() {
         return this.block.isNormalCube(this);
      }

      public boolean canProvidePower() {
         return this.block.canProvidePower(this);
      }

      public int getWeakPower(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.getWeakPower(this, blockAccess, pos, side);
      }

      public boolean hasComparatorInputOverride() {
         return this.block.hasComparatorInputOverride(this);
      }

      public int getComparatorInputOverride(World var1, BlockPos var2) {
         return this.block.getComparatorInputOverride(this, worldIn, pos);
      }

      public float getBlockHardness(World var1, BlockPos var2) {
         return this.block.getBlockHardness(this, worldIn, pos);
      }

      public float getPlayerRelativeBlockHardness(EntityPlayer var1, World var2, BlockPos var3) {
         return this.block.getPlayerRelativeBlockHardness(this, player, worldIn, pos);
      }

      public int getStrongPower(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.getStrongPower(this, blockAccess, pos, side);
      }

      public EnumPushReaction getMobilityFlag() {
         return this.block.getMobilityFlag(this);
      }

      public IBlockState getActualState(IBlockAccess var1, BlockPos var2) {
         return this.block.getActualState(this, blockAccess, pos);
      }

      @SideOnly(Side.CLIENT)
      public AxisAlignedBB getSelectedBoundingBox(World var1, BlockPos var2) {
         return this.block.getSelectedBoundingBox(this, worldIn, pos);
      }

      @SideOnly(Side.CLIENT)
      public boolean shouldSideBeRendered(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.shouldSideBeRendered(this, blockAccess, pos, facing);
      }

      public boolean isOpaqueCube() {
         return this.block.isOpaqueCube(this);
      }

      @Nullable
      public AxisAlignedBB getCollisionBoundingBox(World var1, BlockPos var2) {
         return this.block.getCollisionBoundingBox(this, worldIn, pos);
      }

      public void addCollisionBoxToList(World var1, BlockPos var2, AxisAlignedBB var3, List var4, @Nullable Entity var5) {
         this.block.addCollisionBoxToList(this, worldIn, pos, p_185908_3_, p_185908_4_, p_185908_5_);
      }

      public AxisAlignedBB getBoundingBox(IBlockAccess var1, BlockPos var2) {
         return this.block.getBoundingBox(this, blockAccess, pos);
      }

      public RayTraceResult collisionRayTrace(World var1, BlockPos var2, Vec3d var3, Vec3d var4) {
         return this.block.collisionRayTrace(this, worldIn, pos, start, end);
      }

      public boolean isFullyOpaque() {
         return this.block.isFullyOpaque(this);
      }

      public boolean onBlockEventReceived(World var1, BlockPos var2, int var3, int var4) {
         return this.block.eventReceived(this, worldIn, pos, id, param);
      }

      public void neighborChanged(World var1, BlockPos var2, Block var3) {
         this.block.neighborChanged(this, worldIn, pos, blockIn);
      }

      public ImmutableTable getPropertyValueTable() {
         return this.propertyValueTable;
      }

      public int getLightOpacity(IBlockAccess var1, BlockPos var2) {
         return this.block.getLightOpacity(this, world, pos);
      }

      public int getLightValue(IBlockAccess var1, BlockPos var2) {
         return this.block.getLightValue(this, world, pos);
      }

      public boolean isSideSolid(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.isSideSolid(this, world, pos, side);
      }

      public boolean doesSideBlockRendering(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.doesSideBlockRendering(this, world, pos, side);
      }
   }
}
