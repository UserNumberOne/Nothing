package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

public class BlockStateContainer {
   private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private static final Function GET_NAME_FUNC = new Function() {
      @Nullable
      public String apply(@Nullable IProperty var1) {
         return var1 == null ? "<NULL>" : var1.getName();
      }

      // $FF: synthetic method
      public Object apply(Object var1) {
         return this.apply((IProperty)var1);
      }
   };
   private final Block block;
   private final ImmutableSortedMap properties;
   private final ImmutableList validStates;

   public BlockStateContainer(Block var1, IProperty... var2) {
      this.block = var1;
      HashMap var3 = Maps.newHashMap();

      for(IProperty var7 : var2) {
         validateProperty(var1, var7);
         var3.put(var7.getName(), var7);
      }

      this.properties = ImmutableSortedMap.copyOf(var3);
      LinkedHashMap var11 = Maps.newLinkedHashMap();
      ArrayList var12 = Lists.newArrayList();

      for(List var8 : Cartesian.cartesianProduct(this.getAllowedValues())) {
         Map var9 = MapPopulator.createMap(this.properties.values(), var8);
         BlockStateContainer.StateImplementation var10 = new BlockStateContainer.StateImplementation(var1, ImmutableMap.copyOf(var9));
         var11.put(var9, var10);
         var12.add(var10);
      }

      for(BlockStateContainer.StateImplementation var16 : var12) {
         var16.buildPropertyValueTable(var11);
      }

      this.validStates = ImmutableList.copyOf(var12);
   }

   public static String validateProperty(Block var0, IProperty var1) {
      String var2 = var1.getName();
      if (!NAME_PATTERN.matcher(var2).matches()) {
         throw new IllegalArgumentException("Block: " + var0.getClass() + " has invalidly named property: " + var2);
      } else {
         for(Comparable var4 : var1.getAllowedValues()) {
            String var5 = var1.getName(var4);
            if (!NAME_PATTERN.matcher(var5).matches()) {
               throw new IllegalArgumentException("Block: " + var0.getClass() + " has property: " + var2 + " with invalidly named value: " + var5);
            }
         }

         return var2;
      }
   }

   public ImmutableList getValidStates() {
      return this.validStates;
   }

   private List getAllowedValues() {
      ArrayList var1 = Lists.newArrayList();

      for(IProperty var4 : this.properties.values()) {
         var1.add(var4.getAllowedValues());
      }

      return var1;
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
      return (IProperty)this.properties.get(var1);
   }

   static class StateImplementation extends BlockStateBase {
      private final Block block;
      private final ImmutableMap properties;
      private ImmutableTable propertyValueTable;

      private StateImplementation(Block var1, ImmutableMap var2) {
         this.block = var1;
         this.properties = var2;
      }

      public Collection getPropertyKeys() {
         return Collections.unmodifiableCollection(this.properties.keySet());
      }

      public Comparable getValue(IProperty var1) {
         Comparable var2 = (Comparable)this.properties.get(var1);
         if (var2 == null) {
            throw new IllegalArgumentException("Cannot get property " + var1 + " as it does not exist in " + this.block.getBlockState());
         } else {
            return (Comparable)var1.getValueClass().cast(var2);
         }
      }

      public IBlockState withProperty(IProperty var1, Comparable var2) {
         Comparable var3 = (Comparable)this.properties.get(var1);
         if (var3 == null) {
            throw new IllegalArgumentException("Cannot set property " + var1 + " as it does not exist in " + this.block.getBlockState());
         } else if (var3 == var2) {
            return this;
         } else {
            IBlockState var4 = (IBlockState)this.propertyValueTable.get(var1, var2);
            if (var4 == null) {
               throw new IllegalArgumentException("Cannot set property " + var1 + " to " + var2 + " on block " + Block.REGISTRY.getNameForObject(this.block) + ", it is not an allowed value");
            } else {
               return var4;
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
         return this == var1;
      }

      public int hashCode() {
         return this.properties.hashCode();
      }

      public void buildPropertyValueTable(Map var1) {
         if (this.propertyValueTable != null) {
            throw new IllegalStateException();
         } else {
            HashBasedTable var2 = HashBasedTable.create();

            for(Entry var4 : this.properties.entrySet()) {
               IProperty var5 = (IProperty)var4.getKey();

               for(Comparable var7 : var5.getAllowedValues()) {
                  if (var7 != var4.getValue()) {
                     var2.put(var5, var7, var1.get(this.getPropertiesWithValue(var5, var7)));
                  }
               }
            }

            this.propertyValueTable = ImmutableTable.copyOf(var2);
         }
      }

      private Map getPropertiesWithValue(IProperty var1, Comparable var2) {
         HashMap var3 = Maps.newHashMap(this.properties);
         var3.put(var1, var2);
         return var3;
      }

      public Material getMaterial() {
         return this.block.getMaterial(this);
      }

      public boolean isFullBlock() {
         return this.block.isFullBlock(this);
      }

      public boolean canEntitySpawn(Entity var1) {
         return this.block.canEntitySpawn(this, var1);
      }

      public int getLightOpacity() {
         return this.block.getLightOpacity(this);
      }

      public int getLightValue() {
         return this.block.getLightValue(this);
      }

      public boolean useNeighborBrightness() {
         return this.block.getUseNeighborBrightness(this);
      }

      public MapColor getMapColor() {
         return this.block.getMapColor(this);
      }

      public IBlockState withRotation(Rotation var1) {
         return this.block.withRotation(this, var1);
      }

      public IBlockState withMirror(Mirror var1) {
         return this.block.withMirror(this, var1);
      }

      public boolean isFullCube() {
         return this.block.isFullCube(this);
      }

      public EnumBlockRenderType getRenderType() {
         return this.block.getRenderType(this);
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
         return this.block.getWeakPower(this, var1, var2, var3);
      }

      public boolean hasComparatorInputOverride() {
         return this.block.hasComparatorInputOverride(this);
      }

      public int getComparatorInputOverride(World var1, BlockPos var2) {
         return this.block.getComparatorInputOverride(this, var1, var2);
      }

      public float getBlockHardness(World var1, BlockPos var2) {
         return this.block.getBlockHardness(this, var1, var2);
      }

      public float getPlayerRelativeBlockHardness(EntityPlayer var1, World var2, BlockPos var3) {
         return this.block.getPlayerRelativeBlockHardness(this, var1, var2, var3);
      }

      public int getStrongPower(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
         return this.block.getStrongPower(this, var1, var2, var3);
      }

      public EnumPushReaction getMobilityFlag() {
         return this.block.getMobilityFlag(this);
      }

      public IBlockState getActualState(IBlockAccess var1, BlockPos var2) {
         return this.block.getActualState(this, var1, var2);
      }

      public boolean isOpaqueCube() {
         return this.block.isOpaqueCube(this);
      }

      @Nullable
      public AxisAlignedBB getCollisionBoundingBox(World var1, BlockPos var2) {
         return this.block.getCollisionBoundingBox(this, var1, var2);
      }

      public void addCollisionBoxToList(World var1, BlockPos var2, AxisAlignedBB var3, List var4, @Nullable Entity var5) {
         this.block.addCollisionBoxToList(this, var1, var2, var3, var4, var5);
      }

      public AxisAlignedBB getBoundingBox(IBlockAccess var1, BlockPos var2) {
         return this.block.getBoundingBox(this, var1, var2);
      }

      public RayTraceResult collisionRayTrace(World var1, BlockPos var2, Vec3d var3, Vec3d var4) {
         return this.block.collisionRayTrace(this, var1, var2, var3, var4);
      }

      public boolean isFullyOpaque() {
         return this.block.isFullyOpaque(this);
      }

      public boolean onBlockEventReceived(World var1, BlockPos var2, int var3, int var4) {
         return this.block.eventReceived(this, var1, var2, var3, var4);
      }

      public void neighborChanged(World var1, BlockPos var2, Block var3) {
         this.block.neighborChanged(this, var1, var2, var3);
      }
   }
}
