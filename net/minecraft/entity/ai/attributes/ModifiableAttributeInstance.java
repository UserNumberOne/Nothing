package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class ModifiableAttributeInstance implements IAttributeInstance {
   private final AbstractAttributeMap attributeMap;
   private final IAttribute genericAttribute;
   private final Map mapByOperation = Maps.newHashMap();
   private final Map mapByName = Maps.newHashMap();
   private final Map mapByUUID = Maps.newHashMap();
   private double baseValue;
   private boolean needsUpdate = true;
   private double cachedValue;

   public ModifiableAttributeInstance(AbstractAttributeMap var1, IAttribute var2) {
      this.attributeMap = var1;
      this.genericAttribute = var2;
      this.baseValue = var2.getDefaultValue();

      for(int var3 = 0; var3 < 3; ++var3) {
         this.mapByOperation.put(Integer.valueOf(var3), Sets.newHashSet());
      }

   }

   public IAttribute getAttribute() {
      return this.genericAttribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double var1) {
      if (var1 != this.getBaseValue()) {
         this.baseValue = var1;
         this.flagForUpdate();
      }
   }

   public Collection getModifiersByOperation(int var1) {
      return (Collection)this.mapByOperation.get(Integer.valueOf(var1));
   }

   public Collection getModifiers() {
      HashSet var1 = Sets.newHashSet();

      for(int var2 = 0; var2 < 3; ++var2) {
         var1.addAll(this.getModifiersByOperation(var2));
      }

      return var1;
   }

   @Nullable
   public AttributeModifier getModifier(UUID var1) {
      return (AttributeModifier)this.mapByUUID.get(var1);
   }

   public boolean hasModifier(AttributeModifier var1) {
      return this.mapByUUID.get(var1.getID()) != null;
   }

   public void applyModifier(AttributeModifier var1) {
      if (this.getModifier(var1.getID()) != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         Object var2 = (Set)this.mapByName.get(var1.getName());
         if (var2 == null) {
            var2 = Sets.newHashSet();
            this.mapByName.put(var1.getName(), var2);
         }

         ((Set)this.mapByOperation.get(Integer.valueOf(var1.getOperation()))).add(var1);
         ((Set)var2).add(var1);
         this.mapByUUID.put(var1.getID(), var1);
         this.flagForUpdate();
      }
   }

   protected void flagForUpdate() {
      this.needsUpdate = true;
      this.attributeMap.onAttributeModified(this);
   }

   public void removeModifier(AttributeModifier var1) {
      for(int var2 = 0; var2 < 3; ++var2) {
         Set var3 = (Set)this.mapByOperation.get(Integer.valueOf(var2));
         var3.remove(var1);
      }

      Set var4 = (Set)this.mapByName.get(var1.getName());
      if (var4 != null) {
         var4.remove(var1);
         if (var4.isEmpty()) {
            this.mapByName.remove(var1.getName());
         }
      }

      this.mapByUUID.remove(var1.getID());
      this.flagForUpdate();
   }

   public void removeModifier(UUID var1) {
      AttributeModifier var2 = this.getModifier(var1);
      if (var2 != null) {
         this.removeModifier(var2);
      }

   }

   public double getAttributeValue() {
      if (this.needsUpdate) {
         this.cachedValue = this.computeValue();
         this.needsUpdate = false;
      }

      return this.cachedValue;
   }

   private double computeValue() {
      double var1 = this.getBaseValue();

      for(AttributeModifier var4 : this.getAppliedModifiers(0)) {
         var1 += var4.getAmount();
      }

      double var5 = var1;

      for(AttributeModifier var8 : this.getAppliedModifiers(1)) {
         var5 += var1 * var8.getAmount();
      }

      for(AttributeModifier var10 : this.getAppliedModifiers(2)) {
         var5 *= 1.0D + var10.getAmount();
      }

      return this.genericAttribute.clampValue(var5);
   }

   private Collection getAppliedModifiers(int var1) {
      HashSet var2 = Sets.newHashSet(this.getModifiersByOperation(var1));

      for(IAttribute var3 = this.genericAttribute.getParent(); var3 != null; var3 = var3.getParent()) {
         IAttributeInstance var4 = this.attributeMap.getAttributeInstance(var3);
         if (var4 != null) {
            var2.addAll(var4.getModifiersByOperation(var1));
         }
      }

      return var2;
   }
}
