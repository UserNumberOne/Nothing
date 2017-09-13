package net.minecraft.client.resources.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MetadataSerializer {
   private final IRegistry metadataSectionSerializerRegistry = new RegistrySimple();
   private final GsonBuilder gsonBuilder = new GsonBuilder();
   private Gson gson;

   public MetadataSerializer() {
      this.gsonBuilder.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer());
      this.gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
      this.gsonBuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
   }

   public void registerMetadataSectionType(IMetadataSectionSerializer var1, Class var2) {
      this.metadataSectionSerializerRegistry.putObject(var1.getSectionName(), new MetadataSerializer.Registration(var1, var2));
      this.gsonBuilder.registerTypeAdapter(var2, var1);
      this.gson = null;
   }

   public IMetadataSection parseMetadataSection(String var1, JsonObject var2) {
      if (var1 == null) {
         throw new IllegalArgumentException("Metadata section name cannot be null");
      } else if (!var2.has(var1)) {
         return (IMetadataSection)null;
      } else if (!var2.get(var1).isJsonObject()) {
         throw new IllegalArgumentException("Invalid metadata for '" + var1 + "' - expected object, found " + var2.get(var1));
      } else {
         MetadataSerializer.Registration var3 = (MetadataSerializer.Registration)this.metadataSectionSerializerRegistry.getObject(var1);
         if (var3 == null) {
            throw new IllegalArgumentException("Don't know how to handle metadata section '" + var1 + "'");
         } else {
            return (IMetadataSection)this.getGson().fromJson(var2.getAsJsonObject(var1), var3.clazz);
         }
      }
   }

   private Gson getGson() {
      if (this.gson == null) {
         this.gson = this.gsonBuilder.create();
      }

      return this.gson;
   }

   @SideOnly(Side.CLIENT)
   class Registration {
      final IMetadataSectionSerializer section;
      final Class clazz;

      private Registration(IMetadataSectionSerializer var2, Class var3) {
         this.section = var2;
         this.clazz = var3;
      }
   }
}
