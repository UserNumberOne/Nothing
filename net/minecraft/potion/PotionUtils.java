package net.minecraft.potion;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionUtils {
   public static List getEffectsFromStack(ItemStack var0) {
      return getEffectsFromTag(stack.getTagCompound());
   }

   public static List mergeEffects(PotionType var0, Collection var1) {
      List list = Lists.newArrayList();
      list.addAll(potionIn.getEffects());
      list.addAll(effects);
      return list;
   }

   public static List getEffectsFromTag(@Nullable NBTTagCompound var0) {
      List list = Lists.newArrayList();
      list.addAll(getPotionTypeFromNBT(tag).getEffects());
      addCustomPotionEffectToList(tag, list);
      return list;
   }

   public static List getFullEffectsFromItem(ItemStack var0) {
      return getFullEffectsFromTag(itemIn.getTagCompound());
   }

   public static List getFullEffectsFromTag(@Nullable NBTTagCompound var0) {
      List list = Lists.newArrayList();
      addCustomPotionEffectToList(tag, list);
      return list;
   }

   public static void addCustomPotionEffectToList(@Nullable NBTTagCompound var0, List var1) {
      if (tag != null && tag.hasKey("CustomPotionEffects", 9)) {
         NBTTagList nbttaglist = tag.getTagList("CustomPotionEffects", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            PotionEffect potioneffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound);
            if (potioneffect != null) {
               effectList.add(potioneffect);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public static int getPotionColor(PotionType var0) {
      return getPotionColorFromEffectList(potionIn.getEffects());
   }

   public static int getPotionColorFromEffectList(Collection var0) {
      int i = 3694022;
      if (effects.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         int j = 0;

         for(PotionEffect potioneffect : effects) {
            if (potioneffect.doesShowParticles()) {
               int k = potioneffect.getPotion().getLiquidColor();
               int l = potioneffect.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               f1 += (float)(l * (k >> 8 & 255)) / 255.0F;
               f2 += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            f1 = f1 / (float)j * 255.0F;
            f2 = f2 / (float)j * 255.0F;
            return (int)f << 16 | (int)f1 << 8 | (int)f2;
         }
      }
   }

   public static PotionType getPotionFromItem(ItemStack var0) {
      return getPotionTypeFromNBT(itemIn.getTagCompound());
   }

   public static PotionType getPotionTypeFromNBT(@Nullable NBTTagCompound var0) {
      return tag == null ? PotionTypes.WATER : PotionType.getPotionTypeForName(tag.getString("Potion"));
   }

   public static ItemStack addPotionToItemStack(ItemStack var0, PotionType var1) {
      ResourceLocation resourcelocation = (ResourceLocation)PotionType.REGISTRY.getNameForObject(potionIn);
      if (resourcelocation != null) {
         NBTTagCompound nbttagcompound = itemIn.hasTagCompound() ? itemIn.getTagCompound() : new NBTTagCompound();
         nbttagcompound.setString("Potion", resourcelocation.toString());
         itemIn.setTagCompound(nbttagcompound);
      }

      return itemIn;
   }

   public static ItemStack appendEffects(ItemStack var0, Collection var1) {
      if (effects.isEmpty()) {
         return itemIn;
      } else {
         NBTTagCompound nbttagcompound = (NBTTagCompound)Objects.firstNonNull(itemIn.getTagCompound(), new NBTTagCompound());
         NBTTagList nbttaglist = nbttagcompound.getTagList("CustomPotionEffects", 9);

         for(PotionEffect potioneffect : effects) {
            nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         nbttagcompound.setTag("CustomPotionEffects", nbttaglist);
         itemIn.setTagCompound(nbttagcompound);
         return itemIn;
      }
   }

   @SideOnly(Side.CLIENT)
   public static void addPotionTooltip(ItemStack var0, List var1, float var2) {
      List list = getEffectsFromStack(itemIn);
      List list1 = Lists.newArrayList();
      if (list.isEmpty()) {
         String s = I18n.translateToLocal("effect.none").trim();
         lores.add(TextFormatting.GRAY + s);
      } else {
         for(PotionEffect potioneffect : list) {
            String s1 = I18n.translateToLocal(potioneffect.getEffectName()).trim();
            Potion potion = potioneffect.getPotion();
            Map map = potion.getAttributeModifierMap();
            if (!map.isEmpty()) {
               for(Entry entry : map.entrySet()) {
                  AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
                  AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), potion.getAttributeModifierAmount(potioneffect.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                  list1.add(new Tuple(((IAttribute)entry.getKey()).getName(), attributemodifier1));
               }
            }

            if (potioneffect.getAmplifier() > 0) {
               s1 = s1 + " " + I18n.translateToLocal("potion.potency." + potioneffect.getAmplifier()).trim();
            }

            if (potioneffect.getDuration() > 20) {
               s1 = s1 + " (" + Potion.getPotionDurationString(potioneffect, durationFactor) + ")";
            }

            if (potion.isBadEffect()) {
               lores.add(TextFormatting.RED + s1);
            } else {
               lores.add(TextFormatting.BLUE + s1);
            }
         }
      }

      if (!list1.isEmpty()) {
         lores.add("");
         lores.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("potion.whenDrank"));

         for(Tuple tuple : list1) {
            AttributeModifier attributemodifier2 = (AttributeModifier)tuple.getSecond();
            double d0 = attributemodifier2.getAmount();
            double d1;
            if (attributemodifier2.getOperation() != 1 && attributemodifier2.getOperation() != 2) {
               d1 = attributemodifier2.getAmount();
            } else {
               d1 = attributemodifier2.getAmount() * 100.0D;
            }

            if (d0 > 0.0D) {
               lores.add(TextFormatting.BLUE + I18n.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)tuple.getFirst())));
            } else if (d0 < 0.0D) {
               d1 = d1 * -1.0D;
               lores.add(TextFormatting.RED + I18n.translateToLocalFormatted("attribute.modifier.take." + attributemodifier2.getOperation(), ItemStack.DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + (String)tuple.getFirst())));
            }
         }
      }

   }
}
