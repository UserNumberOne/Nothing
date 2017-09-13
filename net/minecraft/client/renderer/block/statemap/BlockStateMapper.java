package net.minecraft.client.renderer.block.statemap;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockStateMapper {
   private final Map blockStateMap = Maps.newIdentityHashMap();
   private final Set setBuiltInBlocks = Sets.newIdentityHashSet();

   public void registerBlockStateMapper(Block var1, IStateMapper var2) {
      this.blockStateMap.put(var1, var2);
   }

   public void registerBuiltInBlocks(Block... var1) {
      Collections.addAll(this.setBuiltInBlocks, var1);
   }

   public Map putAllStateModelLocations() {
      IdentityHashMap var1 = Maps.newIdentityHashMap();

      for(Block var3 : Block.REGISTRY) {
         var1.putAll(this.getVariants(var3));
      }

      return var1;
   }

   public Set getBlockstateLocations(Block var1) {
      if (this.setBuiltInBlocks.contains(var1)) {
         return Collections.emptySet();
      } else {
         IStateMapper var2 = (IStateMapper)this.blockStateMap.get(var1);
         if (var2 == null) {
            return Collections.singleton(Block.REGISTRY.getNameForObject(var1));
         } else {
            HashSet var3 = Sets.newHashSet();

            for(ModelResourceLocation var5 : var2.putStateModelLocations(var1).values()) {
               var3.add(new ResourceLocation(var5.getResourceDomain(), var5.getResourcePath()));
            }

            return var3;
         }
      }
   }

   public Map getVariants(Block var1) {
      return this.setBuiltInBlocks.contains(var1) ? Collections.emptyMap() : ((IStateMapper)Objects.firstNonNull(this.blockStateMap.get(var1), new DefaultStateMapper())).putStateModelLocations(var1);
   }
}
