package net.minecraft.util.registry;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistrySimple implements IRegistry {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final Map registryObjects = this.createUnderlyingMap();
   private Object[] values;

   protected Map createUnderlyingMap() {
      return Maps.newHashMap();
   }

   @Nullable
   public Object getObject(@Nullable Object var1) {
      return this.registryObjects.get(name);
   }

   public void putObject(Object var1, Object var2) {
      Validate.notNull(key);
      Validate.notNull(value);
      this.values = null;
      if (this.registryObjects.containsKey(key)) {
         LOGGER.debug("Adding duplicate key '{}' to registry", new Object[]{key});
      }

      this.registryObjects.put(key, value);
   }

   public Set getKeys() {
      return Collections.unmodifiableSet(this.registryObjects.keySet());
   }

   @Nullable
   public Object getRandomObject(Random var1) {
      if (this.values == null) {
         Collection collection = this.registryObjects.values();
         if (collection.isEmpty()) {
            return null;
         }

         this.values = collection.toArray(new Object[collection.size()]);
      }

      return this.values[random.nextInt(this.values.length)];
   }

   public boolean containsKey(Object var1) {
      return this.registryObjects.containsKey(key);
   }

   public Iterator iterator() {
      return this.registryObjects.values().iterator();
   }
}
