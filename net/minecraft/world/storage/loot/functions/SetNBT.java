package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class SetNBT extends LootFunction {
   private final NBTTagCompound tag;

   public SetNBT(LootCondition[] var1, NBTTagCompound var2) {
      super(var1);
      this.tag = var2;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      NBTTagCompound var4 = var1.getTagCompound();
      if (var4 == null) {
         var4 = this.tag.copy();
      } else {
         var4.merge(this.tag);
      }

      var1.setTagCompound(var4);
      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      public Serializer() {
         super(new ResourceLocation("set_nbt"), SetNBT.class);
      }

      public void serialize(JsonObject var1, SetNBT var2, JsonSerializationContext var3) {
         var1.addProperty("tag", var2.tag.toString());
      }

      public SetNBT deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         try {
            NBTTagCompound var4 = JsonToNBT.getTagFromJson(JsonUtils.getString(var1, "tag"));
            return new SetNBT(var3, var4);
         } catch (NBTException var5) {
            throw new JsonSyntaxException(var5);
         }
      }

      // $FF: synthetic method
      public LootFunction deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return this.deserialize(var1, var2, var3);
      }
   }
}
