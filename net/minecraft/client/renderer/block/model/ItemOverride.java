package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
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
      this.location = var1;
      this.mapResourceValues = var2;
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   boolean matchesItemStack(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
      Item var4 = var1.getItem();

      for(Entry var6 : this.mapResourceValues.entrySet()) {
         IItemPropertyGetter var7 = var4.getPropertyGetter((ResourceLocation)var6.getKey());
         if (var7 == null || var7.apply(var1, var2, var3) < ((Float)var6.getValue()).floatValue()) {
            return false;
         }
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   static class Deserializer implements JsonDeserializer {
      public ItemOverride deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         ResourceLocation var5 = new ResourceLocation(JsonUtils.getString(var4, "model"));
         Map var6 = this.makeMapResourceValues(var4);
         return new ItemOverride(var5, var6);
      }

      protected Map makeMapResourceValues(JsonObject var1) {
         LinkedHashMap var2 = Maps.newLinkedHashMap();
         JsonObject var3 = JsonUtils.getJsonObject(var1, "predicate");

         for(Entry var5 : var3.entrySet()) {
            var2.put(new ResourceLocation((String)var5.getKey()), Float.valueOf(JsonUtils.getFloat((JsonElement)var5.getValue(), (String)var5.getKey())));
         }

         return var2;
      }
   }
}
