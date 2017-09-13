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
      return this.registryObjects.get(var1);
   }

   public void putObject(Object var1, Object var2) {
      Validate.notNull(var1);
      Validate.notNull(var2);
      this.values = null;
      if (this.registryObjects.containsKey(var1)) {
         LOGGER.debug("Adding duplicate key '{}' to registry", new Object[]{var1});
      }

      this.registryObjects.put(var1, var2);
   }

   public Set getKeys() {
      return Collections.unmodifiableSet(this.registryObjects.keySet());
   }

   @Nullable
   public Object getRandomObject(Random var1) {
      if (this.values == null) {
         Collection var2 = this.registryObjects.values();
         if (var2.isEmpty()) {
            return null;
         }

         this.values = var2.toArray(new Object[var2.size()]);
      }

      return this.values[var1.nextInt(this.values.length)];
   }

   public boolean containsKey(Object var1) {
      return this.registryObjects.containsKey(var1);
   }

   public Iterator iterator() {
      return this.registryObjects.values().iterator();
   }
}
