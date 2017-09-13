package net.minecraft.client.renderer.block.statemap;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockStateMapper {
   private final Map blockStateMap = Maps.newIdentityHashMap();
   private final Set setBuiltInBlocks = Sets.newIdentityHashSet();

   public void registerBlockStateMapper(Block blockIn, IStateMapper stateMapper) {
      this.blockStateMap.put(blockIn, stateMapper);
   }

   public void registerBuiltInBlocks(Block... blockIn) {
      Collections.addAll(this.setBuiltInBlocks, blockIn);
   }

   public Map putAllStateModelLocations() {
      Map map = Maps.newIdentityHashMap();

      for(Block block : Block.REGISTRY) {
         map.putAll(this.getVariants(block));
      }

      return map;
   }

   public Set getBlockstateLocations(Block blockIn) {
      if (this.setBuiltInBlocks.contains(blockIn)) {
         return Collections.emptySet();
      } else {
         IStateMapper istatemapper = (IStateMapper)this.blockStateMap.get(blockIn);
         if (istatemapper == null) {
            return Collections.singleton(Block.REGISTRY.getNameForObject(blockIn));
         } else {
            Set set = Sets.newHashSet();

            for(ModelResourceLocation modelresourcelocation : istatemapper.putStateModelLocations(blockIn).values()) {
               set.add(new ResourceLocation(modelresourcelocation.getResourceDomain(), modelresourcelocation.getResourcePath()));
            }

            return set;
         }
      }
   }

   public Map getVariants(Block blockIn) {
      return this.setBuiltInBlocks.contains(blockIn) ? Collections.emptyMap() : ((IStateMapper)Objects.firstNonNull(this.blockStateMap.get(blockIn), new DefaultStateMapper())).putStateModelLocations(blockIn);
   }
}
