package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.LowerStringMap;

public class AttributeMap extends AbstractAttributeMap {
   private final Set attributeInstanceSet = Sets.newHashSet();
   protected final Map descriptionToAttributeInstanceMap = new LowerStringMap();

   public ModifiableAttributeInstance getAttributeInstance(IAttribute var1) {
      return (ModifiableAttributeInstance)super.getAttributeInstance(var1);
   }

   public ModifiableAttributeInstance getAttributeInstanceByName(String var1) {
      IAttributeInstance var2 = super.getAttributeInstanceByName(var1);
      if (var2 == null) {
         var2 = (IAttributeInstance)this.descriptionToAttributeInstanceMap.get(var1);
      }

      return (ModifiableAttributeInstance)var2;
   }

   public IAttributeInstance registerAttribute(IAttribute var1) {
      IAttributeInstance var2 = super.registerAttribute(var1);
      if (var1 instanceof RangedAttribute && ((RangedAttribute)var1).getDescription() != null) {
         this.descriptionToAttributeInstanceMap.put(((RangedAttribute)var1).getDescription(), var2);
      }

      return var2;
   }

   protected IAttributeInstance createInstance(IAttribute var1) {
      return new ModifiableAttributeInstance(this, var1);
   }

   public void onAttributeModified(IAttributeInstance var1) {
      if (var1.getAttribute().getShouldWatch()) {
         this.attributeInstanceSet.add(var1);
      }

      for(IAttribute var3 : this.descendantsByParent.get(var1.getAttribute())) {
         ModifiableAttributeInstance var4 = this.getAttributeInstance(var3);
         if (var4 != null) {
            var4.flagForUpdate();
         }
      }

   }

   public Set getAttributeInstanceSet() {
      return this.attributeInstanceSet;
   }

   public Collection getWatchedAttributes() {
      HashSet var1 = Sets.newHashSet();

      for(IAttributeInstance var3 : this.getAllAttributes()) {
         if (var3.getAttribute().getShouldWatch()) {
            var1.add(var3);
         }
      }

      return var1;
   }

   // $FF: synthetic method
   public IAttributeInstance getAttributeInstanceByName(String var1) {
      return this.getAttributeInstanceByName(var1);
   }

   // $FF: synthetic method
   public IAttributeInstance getAttributeInstance(IAttribute var1) {
      return this.getAttributeInstance(var1);
   }
}
