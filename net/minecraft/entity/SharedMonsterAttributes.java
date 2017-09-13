package net.minecraft.entity;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedMonsterAttributes {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final IAttribute MAX_HEALTH = (new RangedAttribute((IAttribute)null, "generic.maxHealth", 20.0D, 0.0D, 1024.0D)).setDescription("Max Health").setShouldWatch(true);
   public static final IAttribute FOLLOW_RANGE = (new RangedAttribute((IAttribute)null, "generic.followRange", 32.0D, 0.0D, 2048.0D)).setDescription("Follow Range");
   public static final IAttribute KNOCKBACK_RESISTANCE = (new RangedAttribute((IAttribute)null, "generic.knockbackResistance", 0.0D, 0.0D, 1.0D)).setDescription("Knockback Resistance");
   public static final IAttribute MOVEMENT_SPEED = (new RangedAttribute((IAttribute)null, "generic.movementSpeed", 0.699999988079071D, 0.0D, 1024.0D)).setDescription("Movement Speed").setShouldWatch(true);
   public static final IAttribute ATTACK_DAMAGE = new RangedAttribute((IAttribute)null, "generic.attackDamage", 2.0D, 0.0D, 2048.0D);
   public static final IAttribute ATTACK_SPEED = (new RangedAttribute((IAttribute)null, "generic.attackSpeed", 4.0D, 0.0D, 1024.0D)).setShouldWatch(true);
   public static final IAttribute ARMOR = (new RangedAttribute((IAttribute)null, "generic.armor", 0.0D, 0.0D, 30.0D)).setShouldWatch(true);
   public static final IAttribute ARMOR_TOUGHNESS = (new RangedAttribute((IAttribute)null, "generic.armorToughness", 0.0D, 0.0D, 20.0D)).setShouldWatch(true);
   public static final IAttribute LUCK = (new RangedAttribute((IAttribute)null, "generic.luck", 0.0D, -1024.0D, 1024.0D)).setShouldWatch(true);

   public static NBTTagList writeBaseAttributeMapToNBT(AbstractAttributeMap var0) {
      NBTTagList nbttaglist = new NBTTagList();

      for(IAttributeInstance iattributeinstance : map.getAllAttributes()) {
         nbttaglist.appendTag(writeAttributeInstanceToNBT(iattributeinstance));
      }

      return nbttaglist;
   }

   private static NBTTagCompound writeAttributeInstanceToNBT(IAttributeInstance var0) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      IAttribute iattribute = instance.getAttribute();
      nbttagcompound.setString("Name", iattribute.getName());
      nbttagcompound.setDouble("Base", instance.getBaseValue());
      Collection collection = instance.getModifiers();
      if (collection != null && !collection.isEmpty()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(AttributeModifier attributemodifier : collection) {
            if (attributemodifier.isSaved()) {
               nbttaglist.appendTag(writeAttributeModifierToNBT(attributemodifier));
            }
         }

         nbttagcompound.setTag("Modifiers", nbttaglist);
      }

      return nbttagcompound;
   }

   public static NBTTagCompound writeAttributeModifierToNBT(AttributeModifier var0) {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      nbttagcompound.setString("Name", modifier.getName());
      nbttagcompound.setDouble("Amount", modifier.getAmount());
      nbttagcompound.setInteger("Operation", modifier.getOperation());
      nbttagcompound.setUniqueId("UUID", modifier.getID());
      return nbttagcompound;
   }

   public static void setAttributeModifiers(AbstractAttributeMap var0, NBTTagList var1) {
      for(int i = 0; i < list.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
         IAttributeInstance iattributeinstance = map.getAttributeInstanceByName(nbttagcompound.getString("Name"));
         if (iattributeinstance != null) {
            applyModifiersToAttributeInstance(iattributeinstance, nbttagcompound);
         } else {
            LOGGER.warn("Ignoring unknown attribute '{}'", new Object[]{nbttagcompound.getString("Name")});
         }
      }

   }

   private static void applyModifiersToAttributeInstance(IAttributeInstance var0, NBTTagCompound var1) {
      instance.setBaseValue(compound.getDouble("Base"));
      if (compound.hasKey("Modifiers", 9)) {
         NBTTagList nbttaglist = compound.getTagList("Modifiers", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            AttributeModifier attributemodifier = readAttributeModifierFromNBT(nbttaglist.getCompoundTagAt(i));
            if (attributemodifier != null) {
               AttributeModifier attributemodifier1 = instance.getModifier(attributemodifier.getID());
               if (attributemodifier1 != null) {
                  instance.removeModifier(attributemodifier1);
               }

               instance.applyModifier(attributemodifier);
            }
         }
      }

   }

   @Nullable
   public static AttributeModifier readAttributeModifierFromNBT(NBTTagCompound var0) {
      UUID uuid = compound.getUniqueId("UUID");

      try {
         return new AttributeModifier(uuid, compound.getString("Name"), compound.getDouble("Amount"), compound.getInteger("Operation"));
      } catch (Exception var3) {
         LOGGER.warn("Unable to create attribute: {}", new Object[]{var3.getMessage()});
         return null;
      }
   }
}
