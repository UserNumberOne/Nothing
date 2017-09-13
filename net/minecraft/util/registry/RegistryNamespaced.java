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
      this.underlyingIntegerMap.put(var3, var1);
      this.putObject(var2, var3);
   }

   protected Map createUnderlyingMap() {
      return HashBiMap.create();
   }

   @Nullable
   public Object getObject(@Nullable Object var1) {
      return super.getObject(var1);
   }

   @Nullable
   public Object getNameForObject(Object var1) {
      return this.inverseObjectRegistry.get(var1);
   }

   public boolean containsKey(Object var1) {
      return super.containsKey(var1);
   }

   public int getIDForObject(Object var1) {
      return this.underlyingIntegerMap.getId(var1);
   }

   @Nullable
   public Object getObjectById(int var1) {
      return this.underlyingIntegerMap.get(var1);
   }

   public Iterator iterator() {
      return this.underlyingIntegerMap.iterator();
   }
}
