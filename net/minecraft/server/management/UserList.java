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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

   public UserList(File file) {
      this.saveFile = file;
      GsonBuilder gsonbuilder = (new GsonBuilder()).setPrettyPrinting();
      gsonbuilder.registerTypeHierarchyAdapter(UserListEntry.class, new UserList.Serializer((Object)null));
      this.gson = gsonbuilder.create();
   }

   public boolean isLanServer() {
      return this.lanServer;
   }

   public void setLanServer(boolean flag) {
      this.lanServer = flag;
   }

   public File getSaveFile() {
      return this.saveFile;
   }

   public void addEntry(UserListEntry v0) {
      this.values.put(this.getObjectKey(v0.getValue()), v0);

      try {
         this.writeChanges();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after adding a user.", var3);
      }

   }

   public UserListEntry getEntry(Object k0) {
      this.removeExpired();
      return (UserListEntry)this.values.get(this.getObjectKey(k0));
   }

   public void removeEntry(Object k0) {
      this.values.remove(this.getObjectKey(k0));

      try {
         this.writeChanges();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after removing a user.", var3);
      }

   }

   public String[] getKeys() {
      return (String[])this.values.keySet().toArray(new String[this.values.size()]);
   }

   public Collection getValues() {
      return this.values.values();
   }

   public boolean isEmpty() {
      return this.values.size() < 1;
   }

   protected String getObjectKey(Object k0) {
      return k0.toString();
   }

   protected boolean hasEntry(Object k0) {
      return this.values.containsKey(this.getObjectKey(k0));
   }

   private void removeExpired() {
      ArrayList arraylist = Lists.newArrayList();

      for(UserListEntry jsonlistentry : this.values.values()) {
         if (jsonlistentry.hasBanExpired()) {
            arraylist.add(jsonlistentry.getValue());
         }
      }

      for(Object object : arraylist) {
         this.values.remove(object);
      }

   }

   protected UserListEntry createEntry(JsonObject jsonobject) {
      return new UserListEntry((Object)null, jsonobject);
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

   public void readSavedFile() throws FileNotFoundException {
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

         for(UserListEntry jsonlistentry : collection) {
            if (jsonlistentry.getValue() != null) {
               this.values.put(this.getObjectKey(jsonlistentry.getValue()), jsonlistentry);
            }
         }
      }

   }

   class Serializer implements JsonDeserializer, JsonSerializer {
      private Serializer() {
      }

      public JsonElement serialize(UserListEntry param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public UserListEntry deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }

      public JsonElement serialize(UserListEntry param1, Type param2, JsonSerializationContext param3) {
         // $FF: Couldn't be decompiled
      }

      public UserListEntry deserialize(JsonElement param1, Type param2, JsonDeserializationContext param3) throws JsonParseException {
         // $FF: Couldn't be decompiled
      }

      Serializer(Object object) {
         this();
      }
   }
}
