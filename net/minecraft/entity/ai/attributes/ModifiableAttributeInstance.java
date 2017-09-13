package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
      this.attributeMap = attributeMapIn;
      this.genericAttribute = genericAttributeIn;
      this.baseValue = genericAttributeIn.getDefaultValue();

      for(int i = 0; i < 3; ++i) {
         this.mapByOperation.put(Integer.valueOf(i), Sets.newHashSet());
      }

   }

   public IAttribute getAttribute() {
      return this.genericAttribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double var1) {
      if (baseValue != this.getBaseValue()) {
         this.baseValue = baseValue;
         this.flagForUpdate();
      }

   }

   public Collection getModifiersByOperation(int var1) {
      return (Collection)this.mapByOperation.get(Integer.valueOf(operation));
   }

   public Collection getModifiers() {
      Set set = Sets.newHashSet();

      for(int i = 0; i < 3; ++i) {
         set.addAll(this.getModifiersByOperation(i));
      }

      return set;
   }

   @Nullable
   public AttributeModifier getModifier(UUID var1) {
      return (AttributeModifier)this.mapByUUID.get(uuid);
   }

   public boolean hasModifier(AttributeModifier var1) {
      return this.mapByUUID.get(modifier.getID()) != null;
   }

   public void applyModifier(AttributeModifier var1) {
      if (this.getModifier(modifier.getID()) != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         Set set = (Set)this.mapByName.get(modifier.getName());
         if (set == null) {
            set = Sets.newHashSet();
            this.mapByName.put(modifier.getName(), set);
         }

         ((Set)this.mapByOperation.get(Integer.valueOf(modifier.getOperation()))).add(modifier);
         set.add(modifier);
         this.mapByUUID.put(modifier.getID(), modifier);
         this.flagForUpdate();
      }
   }

   protected void flagForUpdate() {
      this.needsUpdate = true;
      this.attributeMap.onAttributeModified(this);
   }

   public void removeModifier(AttributeModifier var1) {
      for(int i = 0; i < 3; ++i) {
         Set set = (Set)this.mapByOperation.get(Integer.valueOf(i));
         set.remove(modifier);
      }

      Set set1 = (Set)this.mapByName.get(modifier.getName());
      if (set1 != null) {
         set1.remove(modifier);
         if (set1.isEmpty()) {
            this.mapByName.remove(modifier.getName());
         }
      }

      this.mapByUUID.remove(modifier.getID());
      this.flagForUpdate();
   }

   public void removeModifier(UUID var1) {
      AttributeModifier attributemodifier = this.getModifier(p_188479_1_);
      if (attributemodifier != null) {
         this.removeModifier(attributemodifier);
      }

   }

   @SideOnly(Side.CLIENT)
   public void removeAllModifiers() {
      Collection collection = this.getModifiers();
      if (collection != null) {
         for(AttributeModifier attributemodifier : Lists.newArrayList(collection)) {
            this.removeModifier(attributemodifier);
         }
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
      double d0 = this.getBaseValue();

      for(AttributeModifier attributemodifier : this.getAppliedModifiers(0)) {
         d0 += attributemodifier.getAmount();
      }

      double d1 = d0;

      for(AttributeModifier attributemodifier1 : this.getAppliedModifiers(1)) {
         d1 += d0 * attributemodifier1.getAmount();
      }

      for(AttributeModifier attributemodifier2 : this.getAppliedModifiers(2)) {
         d1 *= 1.0D + attributemodifier2.getAmount();
      }

      return this.genericAttribute.clampValue(d1);
   }

   private Collection getAppliedModifiers(int var1) {
      Set set = Sets.newHashSet(this.getModifiersByOperation(operation));

      for(IAttribute iattribute = this.genericAttribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
         IAttributeInstance iattributeinstance = this.attributeMap.getAttributeInstance(iattribute);
         if (iattributeinstance != null) {
            set.addAll(iattributeinstance.getModifiersByOperation(operation));
         }
      }

      return set;
   }
}
