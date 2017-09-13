package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemOverride {
   private final ResourceLocation location;
   private final Map mapResourceValues;

   public ItemOverride(ResourceLocation var1, Map var2) {
      this.location = locationIn;
      this.mapResourceValues = propertyValues;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   boolean matchesItemStack(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
      Item item = stack.getItem();

      for(Entry entry : this.mapResourceValues.entrySet()) {
         IItemPropertyGetter iitempropertygetter = item.getPropertyGetter((ResourceLocation)entry.getKey());
         if (iitempropertygetter == null || iitempropertygetter.apply(stack, worldIn, livingEntity) < ((Float)entry.getValue()).floatValue()) {
            return false;
         }
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public ItemOverride deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "model"));
         Map map = this.makeMapResourceValues(jsonobject);
         return new ItemOverride(resourcelocation, map);
      }

      protected Map makeMapResourceValues(JsonObject var1) {
         Map map = Maps.newLinkedHashMap();
         JsonObject jsonobject = JsonUtils.getJsonObject(p_188025_1_, "predicate");

         for(Entry entry : jsonobject.entrySet()) {
            map.put(new ResourceLocation((String)entry.getKey()), Float.valueOf(JsonUtils.getFloat((JsonElement)entry.getValue(), (String)entry.getKey())));
         }

         return map;
      }
   }
}
