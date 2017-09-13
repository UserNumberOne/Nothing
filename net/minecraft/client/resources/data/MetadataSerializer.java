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
      this.metadataSectionSerializerRegistry.putObject(metadataSectionSerializer.getSectionName(), new MetadataSerializer.Registration(metadataSectionSerializer, clazz));
      this.gsonBuilder.registerTypeAdapter(clazz, metadataSectionSerializer);
      this.gson = null;
   }

   public IMetadataSection parseMetadataSection(String var1, JsonObject var2) {
      if (sectionName == null) {
         throw new IllegalArgumentException("Metadata section name cannot be null");
      } else if (!json.has(sectionName)) {
         return (IMetadataSection)null;
      } else if (!json.get(sectionName).isJsonObject()) {
         throw new IllegalArgumentException("Invalid metadata for '" + sectionName + "' - expected object, found " + json.get(sectionName));
      } else {
         MetadataSerializer.Registration registration = (MetadataSerializer.Registration)this.metadataSectionSerializerRegistry.getObject(sectionName);
         if (registration == null) {
            throw new IllegalArgumentException("Don't know how to handle metadata section '" + sectionName + "'");
         } else {
            return (IMetadataSection)this.getGson().fromJson(json.getAsJsonObject(sectionName), registration.clazz);
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
         this.section = metadataSectionSerializer;
         this.clazz = clazzToRegister;
      }
   }
}
