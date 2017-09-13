package net.minecraft.client.renderer.block.statemap;

import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class StateMapperBase implements IStateMapper {
   protected Map mapStateModelLocations = Maps.newLinkedHashMap();

   public String getPropertyString(Map values) {
      StringBuilder stringbuilder = new StringBuilder();

      for(Entry entry : values.entrySet()) {
         if (stringbuilder.length() != 0) {
            stringbuilder.append(",");
         }

         IProperty iproperty = (IProperty)entry.getKey();
         stringbuilder.append(iproperty.getName());
         stringbuilder.append("=");
         stringbuilder.append(this.getPropertyName(iproperty, (Comparable)entry.getValue()));
      }

      if (stringbuilder.length() == 0) {
         stringbuilder.append("normal");
      }

      return stringbuilder.toString();
   }

   private String getPropertyName(IProperty property, Comparable value) {
      return property.getName(value);
   }

   public Map putStateModelLocations(Block blockIn) {
      UnmodifiableIterator var2 = blockIn.getBlockState().getValidStates().iterator();

      while(var2.hasNext()) {
         IBlockState iblockstate = (IBlockState)var2.next();
         this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
      }

      return this.mapStateModelLocations;
   }

   protected abstract ModelResourceLocation getModelResourceLocation(IBlockState var1);
}
