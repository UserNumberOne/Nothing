package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserList {
   protected static final Logger LOGGER = LogManager.getLogger();
   protected final Gson gson;
   private final File saveFile;
   private final Map values = Maps.newHashMap();
   private boolean lanServer = true;
   private static final ParameterizedType USER_LIST_ENTRY_TYPE = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
         return new Type[]{UserListEntry.class};
      }

      public Type getRawType() {
         return List.class;
      }

      public Type getOwnerType() {
         return null;
      }
   };

   public UserList(File var1) {
      this.saveFile = saveFile;
      GsonBuilder gsonbuilder = (new GsonBuilder()).setPrettyPrinting();
      gsonbuilder.registerTypeHierarchyAdapter(UserListEntry.class, new UserList.Serializer());
      this.gson = gsonbuilder.create();
   }

   public boolean isLanServer() {
      return this.lanServer;
   }

   public void setLanServer(boolean var1) {
      this.lanServer = state;
   }

   public void addEntry(UserListEntry var1) {
      this.values.put(this.getObjectKey(entry.getValue()), entry);

      try {
         this.writeChanges();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after adding a user.", var3);
      }

   }

   public UserListEntry getEntry(Object var1) {
      this.removeExpired();
      return (UserListEntry)this.values.get(this.getObjectKey(obj));
   }

   public void removeEntry(Object var1) {
      this.values.remove(this.getObjectKey(entry));

      try {
         this.writeChanges();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after removing a user.", var3);
      }

   }

   @SideOnly(Side.SERVER)
   public File getSaveFile() {
      return this.saveFile;
   }

   public String[] getKeys() {
      return (String[])this.values.keySet().toArray(new String[this.values.size()]);
   }

   protected String getObjectKey(Object var1) {
      return obj.toString();
   }

   protected boolean hasEntry(Object var1) {
      return this.values.containsKey(this.getObjectKey(entry));
   }

   private void removeExpired() {
      List list = Lists.newArrayList();

      for(UserListEntry v : this.values.values()) {
         if (v.hasBanExpired()) {
            list.add(v.getValue());
         }
      }

      for(Object k : list) {
         this.values.remove(k);
      }

   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListEntry((Object)null, entryData);
   }

   protected Map getValues() {
      return this.values;
   }

   public void writeChanges() throws IOException {
      Collection collection = this.values.values();
      String s = this.gson.toJson(collection);
      BufferedWriter bufferedwriter = null;

      try {
         bufferedwriter = Files.newWriter(this.saveFile, Charsets.UTF_8);
         bufferedwriter.write(s);
      } finally {
         IOUtils.closeQuietly(bufferedwriter);
      }

   }

   @SideOnly(Side.SERVER)
   public boolean isEmpty() {
      return this.values.size() < 1;
   }

   @SideOnly(Side.SERVER)
   public void readSavedFile() throws IOException, FileNotFoundException {
      Collection collection = null;
      BufferedReader bufferedreader = null;

      try {
         bufferedreader = Files.newReader(this.saveFile, Charsets.UTF_8);
         collection = (Collection)this.gson.fromJson(bufferedreader, USER_LIST_ENTRY_TYPE);
      } finally {
         IOUtils.closeQuietly(bufferedreader);
      }

      if (collection != null) {
         this.values.clear();

         for(UserListEntry userlistentry : collection) {
            if (userlistentry.getValue() != null) {
               this.values.put(this.getObjectKey(userlistentry.getValue()), userlistentry);
            }
         }
      }

   }

   class Serializer implements JsonDeserializer, JsonSerializer {
      private Serializer() {
      }

      public JsonElement serialize(UserListEntry var1, Type var2, JsonSerializationContext var3) {
         JsonObject jsonobject = new JsonObject();
         p_serialize_1_.onSerialization(jsonobject);
         return jsonobject;
      }

      public UserListEntry deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (p_deserialize_1_.isJsonObject()) {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            return UserList.this.createEntry(jsonobject);
         } else {
            return null;
         }
      }
   }
}
