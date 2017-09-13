package net.minecraft.entity.ai.attributes;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAttributeInstance {
   IAttribute getAttribute();

   double getBaseValue();

   void setBaseValue(double var1);

   Collection getModifiersByOperation(int var1);

   Collection getModifiers();

   boolean hasModifier(AttributeModifier var1);

   @Nullable
   AttributeModifier getModifier(UUID var1);

   void applyModifier(AttributeModifier var1);

   void removeModifier(AttributeModifier var1);

   void removeModifier(UUID var1);

   @SideOnly(Side.CLIENT)
   void removeAllModifiers();

   double getAttributeValue();
}
