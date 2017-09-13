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

   public UserList(File var1) {
      this.saveFile = var1;
      GsonBuilder var2 = (new GsonBuilder()).setPrettyPrinting();
      var2.registerTypeHierarchyAdapter(UserListEntry.class, new UserList.Serializer((Object)null));
      this.gson = var2.create();
   }

   public boolean isLanServer() {
      return this.lanServer;
   }

   public void setLanServer(boolean var1) {
      this.lanServer = var1;
   }

   public File getSaveFile() {
      return this.saveFile;
   }

   public void addEntry(UserListEntry var1) {
      this.values.put(this.getObjectKey(var1.getValue()), var1);

      try {
         this.writeChanges();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after adding a user.", var3);
      }

   }

   public UserListEntry getEntry(Object var1) {
      this.removeExpired();
      return (UserListEntry)this.values.get(this.getObjectKey(var1));
   }

   public void removeEntry(Object var1) {
      this.values.remove(this.getObjectKey(var1));

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

   protected String getObjectKey(Object var1) {
      return var1.toString();
   }

   protected boolean hasEntry(Object var1) {
      return this.values.containsKey(this.getObjectKey(var1));
   }

   private void removeExpired() {
      ArrayList var1 = Lists.newArrayList();

      for(UserListEntry var3 : this.values.values()) {
         if (var3.hasBanExpired()) {
            var1.add(var3.getValue());
         }
      }

      for(Object var5 : var1) {
         this.values.remove(var5);
      }

   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListEntry((Object)null, var1);
   }

   protected Map getValues() {
      return this.values;
   }

   public void writeChanges() throws IOException {
      Collection var1 = this.values.values();
      String var2 = this.gson.toJson(var1);
      BufferedWriter var3 = null;

      try {
         var3 = Files.newWriter(this.saveFile, Charsets.UTF_8);
         var3.write(var2);
      } finally {
         IOUtils.closeQuietly(var3);
      }

   }

   public void readSavedFile() throws FileNotFoundException {
      Collection var1 = null;
      BufferedReader var2 = null;

      try {
         var2 = Files.newReader(this.saveFile, Charsets.UTF_8);
         var1 = (Collection)this.gson.fromJson(var2, USER_LIST_ENTRY_TYPE);
      } finally {
         IOUtils.closeQuietly(var2);
      }

      if (var1 != null) {
         this.values.clear();

         for(UserListEntry var4 : var1) {
            if (var4.getValue() != null) {
               this.values.put(this.getObjectKey(var4.getValue()), var4);
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

      Serializer(Object var2) {
         this();
      }
   }
}
