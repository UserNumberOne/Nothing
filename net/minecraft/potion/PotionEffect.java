package net.minecraft.potion;

import com.google.common.collect.ComparisonChain;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PotionEffect implements Comparable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Potion potion;
   private int duration;
   private int amplifier;
   private boolean isSplashPotion;
   private boolean isAmbient;
   @SideOnly(Side.CLIENT)
   private boolean isPotionDurationMax;
   private boolean showParticles;
   private List curativeItems;

   public PotionEffect(Potion var1) {
      this(potionIn, 0, 0);
   }

   public PotionEffect(Potion var1, int var2) {
      this(potionIn, durationIn, 0);
   }

   public PotionEffect(Potion var1, int var2, int var3) {
      this(potionIn, durationIn, amplifierIn, false, true);
   }

   public PotionEffect(Potion var1, int var2, int var3, boolean var4, boolean var5) {
      this.potion = potionIn;
      this.duration = durationIn;
      this.amplifier = amplifierIn;
      this.isAmbient = ambientIn;
      this.showParticles = showParticlesIn;
   }

   public PotionEffect(PotionEffect var1) {
      this.potion = other.potion;
      this.duration = other.duration;
      this.amplifier = other.amplifier;
      this.isAmbient = other.isAmbient;
      this.showParticles = other.showParticles;
      this.curativeItems = other.curativeItems;
   }

   public void combine(PotionEffect var1) {
      if (this.potion != other.potion) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      if (other.amplifier > this.amplifier) {
         this.amplifier = other.amplifier;
         this.duration = other.duration;
      } else if (other.amplifier == this.amplifier && this.duration < other.duration) {
         this.duration = other.duration;
      } else if (!other.isAmbient && this.isAmbient) {
         this.isAmbient = other.isAmbient;
      }

      this.showParticles = other.showParticles;
   }

   public Potion getPotion() {
      return this.potion;
   }

   public int getDuration() {
      return this.duration;
   }

   public int getAmplifier() {
      return this.amplifier;
   }

   public boolean getIsAmbient() {
      return this.isAmbient;
   }

   public boolean doesShowParticles() {
      return this.showParticles;
   }

   public boolean onUpdate(EntityLivingBase var1) {
      if (this.duration > 0) {
         if (this.potion.isReady(this.duration, this.amplifier)) {
            this.performEffect(entityIn);
         }

         this.deincrementDuration();
      }

      return this.duration > 0;
   }

   private int deincrementDuration() {
      return --this.duration;
   }

   public void performEffect(EntityLivingBase var1) {
      if (this.duration > 0) {
         this.potion.performEffect(entityIn, this.amplifier);
      }

   }

   public String getEffectName() {
      return this.potion.getName();
   }

   public String toString() {
      String s;
      if (this.amplifier > 0) {
         s = this.getEffectName() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
      } else {
         s = this.getEffectName() + ", Duration: " + this.duration;
      }

      if (this.isSplashPotion) {
         s = s + ", Splash: true";
      }

      if (!this.showParticles) {
         s = s + ", Particles: false";
      }

      return s;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof PotionEffect)) {
         return false;
      } else {
         PotionEffect potioneffect = (PotionEffect)p_equals_1_;
         return this.duration == potioneffect.duration && this.amplifier == potioneffect.amplifier && this.isSplashPotion == potioneffect.isSplashPotion && this.isAmbient == potioneffect.isAmbient && this.potion.equals(potioneffect.potion);
      }
   }

   public int hashCode() {
      int i = this.potion.hashCode();
      i = 31 * i + this.duration;
      i = 31 * i + this.amplifier;
      i = 31 * i + (this.isSplashPotion ? 1 : 0);
      i = 31 * i + (this.isAmbient ? 1 : 0);
      return i;
   }

   public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound var1) {
      nbt.setByte("Id", (byte)Potion.getIdFromPotion(this.getPotion()));
      nbt.setByte("Amplifier", (byte)this.getAmplifier());
      nbt.setInteger("Duration", this.getDuration());
      nbt.setBoolean("Ambient", this.getIsAmbient());
      nbt.setBoolean("ShowParticles", this.doesShowParticles());
      return nbt;
   }

   public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound var0) {
      int i = nbt.getByte("Id") & 255;
      Potion potion = Potion.getPotionById(i);
      if (potion == null) {
         return null;
      } else {
         int j = nbt.getByte("Amplifier");
         int k = nbt.getInteger("Duration");
         boolean flag = nbt.getBoolean("Ambient");
         boolean flag1 = true;
         if (nbt.hasKey("ShowParticles", 1)) {
            flag1 = nbt.getBoolean("ShowParticles");
         }

         return new PotionEffect(potion, k, j, flag, flag1);
      }
   }

   @SideOnly(Side.CLIENT)
   public void setPotionDurationMax(boolean var1) {
      this.isPotionDurationMax = maxDuration;
   }

   public int compareTo(PotionEffect var1) {
      int i = 32147;
      return this.getDuration() > 32147 && p_compareTo_1_.getDuration() > 32147 || this.getIsAmbient() && p_compareTo_1_.getIsAmbient() ? ComparisonChain.start().compare(Boolean.valueOf(this.getIsAmbient()), Boolean.valueOf(p_compareTo_1_.getIsAmbient())).compare(this.getPotion().getLiquidColor(), p_compareTo_1_.getPotion().getLiquidColor()).result() : ComparisonChain.start().compare(Boolean.valueOf(this.getIsAmbient()), Boolean.valueOf(p_compareTo_1_.getIsAmbient())).compare(this.getDuration(), p_compareTo_1_.getDuration()).compare(this.getPotion().getLiquidColor(), p_compareTo_1_.getPotion().getLiquidColor()).result();
   }

   @SideOnly(Side.CLIENT)
   public boolean getIsPotionDurationMax() {
      return this.isPotionDurationMax;
   }

   public List getCurativeItems() {
      if (this.curativeItems == null) {
         this.curativeItems = new ArrayList();
         this.curativeItems.add(new ItemStack(Items.MILK_BUCKET));
      }

      return this.curativeItems;
   }

   public boolean isCurativeItem(ItemStack var1) {
      for(ItemStack curativeItem : this.getCurativeItems()) {
         if (curativeItem.isItemEqual(stack)) {
            return true;
         }
      }

      return false;
   }

   public void setCurativeItems(List var1) {
      this.curativeItems = curativeItems;
   }

   public void addCurativeItem(ItemStack var1) {
      if (!this.isCurativeItem(stack)) {
         this.getCurativeItems().add(stack);
      }

   }
}
