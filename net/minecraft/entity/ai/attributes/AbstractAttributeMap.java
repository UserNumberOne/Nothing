package net.minecraft.entity.ai.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.LowerStringMap;

public abstract class AbstractAttributeMap {
   protected final Map attributes = Maps.newHashMap();
   protected final Map attributesByName = new LowerStringMap();
   protected final Multimap descendantsByParent = HashMultimap.create();

   public IAttributeInstance getAttributeInstance(IAttribute var1) {
      return (IAttributeInstance)this.attributes.get(var1);
   }

   public IAttributeInstance getAttributeInstanceByName(String var1) {
      return (IAttributeInstance)this.attributesByName.get(var1);
   }

   public IAttributeInstance registerAttribute(IAttribute var1) {
      if (this.attributesByName.containsKey(var1.getName())) {
         throw new IllegalArgumentException("Attribute is already registered!");
      } else {
         IAttributeInstance var2 = this.createInstance(var1);
         this.attributesByName.put(var1.getName(), var2);
         this.attributes.put(var1, var2);

         for(IAttribute var3 = var1.getParent(); var3 != null; var3 = var3.getParent()) {
            this.descendantsByParent.put(var3, var1);
         }

         return var2;
      }
   }

   protected abstract IAttributeInstance createInstance(IAttribute var1);

   public Collection getAllAttributes() {
      return this.attributesByName.values();
   }

   public void onAttributeModified(IAttributeInstance var1) {
   }

   public void removeAttributeModifiers(Multimap var1) {
      for(Entry var3 : var1.entries()) {
         IAttributeInstance var4 = this.getAttributeInstanceByName((String)var3.getKey());
         if (var4 != null) {
            var4.removeModifier((AttributeModifier)var3.getValue());
         }
      }

   }

   public void applyAttributeModifiers(Multimap var1) {
      for(Entry var3 : var1.entries()) {
         IAttributeInstance var4 = this.getAttributeInstanceByName((String)var3.getKey());
         if (var4 != null) {
            var4.removeModifier((AttributeModifier)var3.getValue());
            var4.applyModifier((AttributeModifier)var3.getValue());
         }
      }

   }
}
