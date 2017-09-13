package net.minecraft.potion;

import com.google.common.collect.ComparisonChain;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PotionEffect implements Comparable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Potion potion;
   private int duration;
   private int amplifier;
   private boolean isSplashPotion;
   private boolean isAmbient;
   private boolean showParticles;

   public PotionEffect(Potion var1) {
      this(var1, 0, 0);
   }

   public PotionEffect(Potion var1, int var2) {
      this(var1, var2, 0);
   }

   public PotionEffect(Potion var1, int var2, int var3) {
      this(var1, var2, var3, false, true);
   }

   public PotionEffect(Potion var1, int var2, int var3, boolean var4, boolean var5) {
      this.potion = var1;
      this.duration = var2;
      this.amplifier = var3;
      this.isAmbient = var4;
      this.showParticles = var5;
   }

   public PotionEffect(PotionEffect var1) {
      this.potion = var1.potion;
      this.duration = var1.duration;
      this.amplifier = var1.amplifier;
      this.isAmbient = var1.isAmbient;
      this.showParticles = var1.showParticles;
   }

   public void combine(PotionEffect var1) {
      if (this.potion != var1.potion) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      if (var1.amplifier > this.amplifier) {
         this.amplifier = var1.amplifier;
         this.duration = var1.duration;
      } else if (var1.amplifier == this.amplifier && this.duration < var1.duration) {
         this.duration = var1.duration;
      } else if (!var1.isAmbient && this.isAmbient) {
         this.isAmbient = var1.isAmbient;
      }

      this.showParticles = var1.showParticles;
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
            this.performEffect(var1);
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
         this.potion.performEffect(var1, this.amplifier);
      }

   }

   public String getEffectName() {
      return this.potion.getName();
   }

   public String toString() {
      String var1;
      if (this.amplifier > 0) {
         var1 = this.getEffectName() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
      } else {
         var1 = this.getEffectName() + ", Duration: " + this.duration;
      }

      if (this.isSplashPotion) {
         var1 = var1 + ", Splash: true";
      }

      if (!this.showParticles) {
         var1 = var1 + ", Particles: false";
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof PotionEffect)) {
         return false;
      } else {
         PotionEffect var2 = (PotionEffect)var1;
         return this.duration == var2.duration && this.amplifier == var2.amplifier && this.isSplashPotion == var2.isSplashPotion && this.isAmbient == var2.isAmbient && this.potion.equals(var2.potion);
      }
   }

   public int hashCode() {
      int var1 = this.potion.hashCode();
      var1 = 31 * var1 + this.duration;
      var1 = 31 * var1 + this.amplifier;
      var1 = 31 * var1 + (this.isSplashPotion ? 1 : 0);
      var1 = 31 * var1 + (this.isAmbient ? 1 : 0);
      return var1;
   }

   public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound var1) {
      var1.setByte("Id", (byte)Potion.getIdFromPotion(this.getPotion()));
      var1.setByte("Amplifier", (byte)this.getAmplifier());
      var1.setInteger("Duration", this.getDuration());
      var1.setBoolean("Ambient", this.getIsAmbient());
      var1.setBoolean("ShowParticles", this.doesShowParticles());
      return var1;
   }

   public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound var0) {
      byte var1 = var0.getByte("Id");
      Potion var2 = Potion.getPotionById(var1);
      if (var2 == null) {
         return null;
      } else {
         byte var3 = var0.getByte("Amplifier");
         int var4 = var0.getInteger("Duration");
         boolean var5 = var0.getBoolean("Ambient");
         boolean var6 = true;
         if (var0.hasKey("ShowParticles", 1)) {
            var6 = var0.getBoolean("ShowParticles");
         }

         return new PotionEffect(var2, var4, var3, var5, var6);
      }
   }

   public int compareTo(PotionEffect var1) {
      boolean var2 = true;
      return (this.getDuration() <= 32147 || var1.getDuration() <= 32147) && (!this.getIsAmbient() || !var1.getIsAmbient()) ? ComparisonChain.start().compare(Boolean.valueOf(this.getIsAmbient()), Boolean.valueOf(var1.getIsAmbient())).compare(this.getDuration(), var1.getDuration()).compare(this.getPotion().getLiquidColor(), var1.getPotion().getLiquidColor()).result() : ComparisonChain.start().compare(Boolean.valueOf(this.getIsAmbient()), Boolean.valueOf(var1.getIsAmbient())).compare(this.getPotion().getLiquidColor(), var1.getPotion().getLiquidColor()).result();
   }

   // $FF: synthetic method
   public int compareTo(Object var1) {
      return this.compareTo((PotionEffect)var1);
   }
}
