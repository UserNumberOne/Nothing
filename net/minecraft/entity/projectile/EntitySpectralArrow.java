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
   private int duration = 200;

   public EntitySpectralArrow(World var1) {
      super(worldIn);
   }

   public EntitySpectralArrow(World var1, EntityLivingBase var2) {
      super(worldIn, shooter);
   }

   public EntitySpectralArrow(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
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
      super.arrowHit(living);
      PotionEffect potioneffect = new PotionEffect(MobEffects.GLOWING, this.duration, 0);
      living.addPotionEffect(potioneffect);
   }

   public static void registerFixesSpectralArrow(DataFixer var0) {
      EntityArrow.registerFixesArrow(fixer, "SpectralArrow");
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("Duration")) {
         this.duration = compound.getInteger("Duration");
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("Duration", this.duration);
   }
}
