package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;

public class EntitySpectralArrow extends EntityArrow {
   public int duration = 200;

   public EntitySpectralArrow(World var1) {
      super(var1);
   }

   public EntitySpectralArrow(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   public EntitySpectralArrow(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && !this.inGround) {
         this.world.spawnParticle(EnumParticleTypes.SPELL_INSTANT, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
      }

   }

   protected ItemStack getArrowStack() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }

   protected void arrowHit(EntityLivingBase var1) {
      super.arrowHit(var1);
      PotionEffect var2 = new PotionEffect(MobEffects.GLOWING, this.duration, 0);
      var1.addPotionEffect(var2);
   }

   public static void registerFixesSpectralArrow(DataFixer var0) {
      EntityArrow.registerFixesArrow(var0, "SpectralArrow");
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("Duration")) {
         this.duration = var1.getInteger("Duration");
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Duration", this.duration);
   }
}
