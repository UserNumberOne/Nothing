package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.LowerStringMap;

public class AttributeMap extends AbstractAttributeMap {
   private final Set attributeInstanceSet = Sets.newHashSet();
   protected final Map descriptionToAttributeInstanceMap = new LowerStringMap();

   public ModifiableAttributeInstance getAttributeInstance(IAttribute var1) {
      return (ModifiableAttributeInstance)super.getAttributeInstance(attribute);
   }

   public ModifiableAttributeInstance getAttributeInstanceByName(String var1) {
      IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);
      if (iattributeinstance == null) {
         iattributeinstance = (IAttributeInstance)this.descriptionToAttributeInstanceMap.get(attributeName);
      }

      return (ModifiableAttributeInstance)iattributeinstance;
   }

   public IAttributeInstance registerAttribute(IAttribute var1) {
      IAttributeInstance iattributeinstance = super.registerAttribute(attribute);
      if (attribute instanceof RangedAttribute && ((RangedAttribute)attribute).getDescription() != null) {
         this.descriptionToAttributeInstanceMap.put(((RangedAttribute)attribute).getDescription(), iattributeinstance);
      }

      return iattributeinstance;
   }

   protected IAttributeInstance createInstance(IAttribute var1) {
      return new ModifiableAttributeInstance(this, attribute);
   }

   public void onAttributeModified(IAttributeInstance var1) {
      if (instance.getAttribute().getShouldWatch()) {
         this.attributeInstanceSet.add(instance);
      }

      for(IAttribute iattribute : this.descendantsByParent.get(instance.getAttribute())) {
         ModifiableAttributeInstance modifiableattributeinstance = this.getAttributeInstance(iattribute);
         if (modifiableattributeinstance != null) {
            modifiableattributeinstance.flagForUpdate();
         }
      }

   }

   public Set getAttributeInstanceSet() {
      return this.attributeInstanceSet;
   }

   public Collection getWatchedAttributes() {
      Set set = Sets.newHashSet();

      for(IAttributeInstance iattributeinstance : this.getAllAttributes()) {
         if (iattributeinstance.getAttribute().getShouldWatch()) {
            set.add(iattributeinstance);
         }
      }

      return set;
   }
}
