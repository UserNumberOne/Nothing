package net.minecraft.client.renderer.block.statemap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StateMap extends StateMapperBase {
   private final IProperty name;
   private final String suffix;
   private final List ignored;

   private StateMap(@Nullable IProperty name, @Nullable String suffix, List ignored) {
      this.name = name;
      this.suffix = suffix;
      this.ignored = ignored;
   }

   protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
      Map map = Maps.newLinkedHashMap(state.getProperties());
      String s;
      if (this.name == null) {
         s = ((ResourceLocation)Block.REGISTRY.getNameForObject(state.getBlock())).toString();
      } else {
         s = String.format("%s:%s", ((ResourceLocation)Block.REGISTRY.getNameForObject(state.getBlock())).getResourceDomain(), this.removeName(this.name, map));
      }

      if (this.suffix != null) {
         s = s + this.suffix;
      }

      for(IProperty iproperty : this.ignored) {
         map.remove(iproperty);
      }

      return new ModelResourceLocation(s, this.getPropertyString(map));
   }

   private String removeName(IProperty p_187490_1_, Map p_187490_2_) {
      return p_187490_1_.getName((Comparable)p_187490_2_.remove(this.name));
   }

   @SideOnly(Side.CLIENT)
   public static class Builder {
      private IProperty name;
      private String suffix;
      private final List ignored = Lists.newArrayList();

      public StateMap.Builder withName(IProperty builderPropertyIn) {
         this.name = builderPropertyIn;
         return this;
      }

      public StateMap.Builder withSuffix(String builderSuffixIn) {
         this.suffix = builderSuffixIn;
         return this;
      }

      public StateMap.Builder ignore(IProperty... p_178442_1_) {
         Collections.addAll(this.ignored, p_178442_1_);
         return this;
      }

      public StateMap build() {
         return new StateMap(this.name, this.suffix, this.ignored);
      }
   }
}