package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class LowerStringMap implements Map {
   private final Map internalMap = Maps.newLinkedHashMap();

   public int size() {
      return this.internalMap.size();
   }

   public boolean isEmpty() {
      return this.internalMap.isEmpty();
   }

   public boolean containsKey(Object var1) {
      return this.internalMap.containsKey(p_containsKey_1_.toString().toLowerCase());
   }

   public boolean containsValue(Object var1) {
      return this.internalMap.containsKey(p_containsValue_1_);
   }

   public Object get(Object var1) {
      return this.internalMap.get(p_get_1_.toString().toLowerCase());
   }

   public Object put(String var1, Object var2) {
      return this.internalMap.put(p_put_1_.toLowerCase(), p_put_2_);
   }

   public Object remove(Object var1) {
      return this.internalMap.remove(p_remove_1_.toString().toLowerCase());
   }

   public void putAll(Map var1) {
      for(Entry entry : p_putAll_1_.entrySet()) {
         this.put((String)entry.getKey(), entry.getValue());
      }

   }

   public void clear() {
      this.internalMap.clear();
   }

   public Set keySet() {
      return this.internalMap.keySet();
   }

   public Collection values() {
      return this.internalMap.values();
   }

   public Set entrySet() {
      return this.internalMap.entrySet();
   }
}
