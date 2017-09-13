package net.minecraft.util.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.IObjectIntIterable;
import net.minecraft.util.IntIdentityHashBiMap;

public class RegistryNamespaced extends RegistrySimple implements IObjectIntIterable {
   protected final IntIdentityHashBiMap underlyingIntegerMap = new IntIdentityHashBiMap(256);
   protected final Map inverseObjectRegistry;

   public RegistryNamespaced() {
      this.inverseObjectRegistry = ((BiMap)this.registryObjects).inverse();
   }

   public void register(int var1, Object var2, Object var3) {
      this.underlyingIntegerMap.put(value, id);
      this.putObject(key, value);
   }

   protected Map createUnderlyingMap() {
      return HashBiMap.create();
   }

   @Nullable
   public Object getObject(@Nullable Object var1) {
      return super.getObject(name);
   }

   @Nullable
   public Object getNameForObject(Object var1) {
      return this.inverseObjectRegistry.get(value);
   }

   public boolean containsKey(Object var1) {
      return super.containsKey(key);
   }

   public int getIDForObject(Object var1) {
      return this.underlyingIntegerMap.getId(value);
   }

   @Nullable
   public Object getObjectById(int var1) {
      return this.underlyingIntegerMap.get(id);
   }

   public Iterator iterator() {
      return this.underlyingIntegerMap.iterator();
   }
}
