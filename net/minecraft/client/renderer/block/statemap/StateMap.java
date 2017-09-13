package net.minecraft.client.renderer.block.statemap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.LinkedHashMap;
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

   private StateMap(@Nullable IProperty var1, @Nullable String var2, List var3) {
      this.name = var1;
      this.suffix = var2;
      this.ignored = var3;
   }

   protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
      LinkedHashMap var2 = Maps.newLinkedHashMap(var1.getProperties());
      String var3;
      if (this.name == null) {
         var3 = ((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock())).toString();
      } else {
         var3 = String.format("%s:%s", ((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock())).getResourceDomain(), this.removeName(this.name, var2));
      }

      if (this.suffix != null) {
         var3 = var3 + this.suffix;
      }

      for(IProperty var5 : this.ignored) {
         var2.remove(var5);
      }

      return new ModelResourceLocation(var3, this.getPropertyString(var2));
   }

   private String removeName(IProperty var1, Map var2) {
      return var1.getName((Comparable)var2.remove(this.name));
   }

   @SideOnly(Side.CLIENT)
   public static class Builder {
      private IProperty name;
      private String suffix;
      private final List ignored = Lists.newArrayList();

      public StateMap.Builder withName(IProperty var1) {
         this.name = var1;
         return this;
      }

      public StateMap.Builder withSuffix(String var1) {
         this.suffix = var1;
         return this;
      }

      public StateMap.Builder ignore(IProperty... var1) {
         Collections.addAll(this.ignored, var1);
         return this;
      }

      public StateMap build() {
         return new StateMap(this.name, this.suffix, this.ignored);
      }
   }
}
