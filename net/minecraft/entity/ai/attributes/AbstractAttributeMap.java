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

   public IAttributeInstance getAttributeInstance(IAttribute attribute) {
      return (IAttributeInstance)this.attributes.get(attribute);
   }

   public IAttributeInstance getAttributeInstanceByName(String attributeName) {
      return (IAttributeInstance)this.attributesByName.get(attributeName);
   }

   public IAttributeInstance registerAttribute(IAttribute attribute) {
      if (this.attributesByName.containsKey(attribute.getName())) {
         throw new IllegalArgumentException("Attribute is already registered!");
      } else {
         IAttributeInstance iattributeinstance = this.createInstance(attribute);
         this.attributesByName.put(attribute.getName(), iattributeinstance);
         this.attributes.put(attribute, iattributeinstance);

         for(IAttribute iattribute = attribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
            this.descendantsByParent.put(iattribute, attribute);
         }

         return iattributeinstance;
      }
   }

   protected abstract IAttributeInstance createInstance(IAttribute var1);

   public Collection getAllAttributes() {
      return this.attributesByName.values();
   }

   public void onAttributeModified(IAttributeInstance instance) {
   }

   public void removeAttributeModifiers(Multimap modifiers) {
      for(Entry entry : modifiers.entries()) {
         IAttributeInstance iattributeinstance = this.getAttributeInstanceByName((String)entry.getKey());
         if (iattributeinstance != null) {
            iattributeinstance.removeModifier((AttributeModifier)entry.getValue());
         }
      }

   }

   public void applyAttributeModifiers(Multimap modifiers) {
      for(Entry entry : modifiers.entries()) {
         IAttributeInstance iattributeinstance = this.getAttributeInstanceByName((String)entry.getKey());
         if (iattributeinstance != null) {
            iattributeinstance.removeModifier((AttributeModifier)entry.getValue());
            iattributeinstance.applyModifier((AttributeModifier)entry.getValue());
         }
      }

   }
}
