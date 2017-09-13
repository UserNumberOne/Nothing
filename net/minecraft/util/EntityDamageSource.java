package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

public class EntityDamageSource extends DamageSource {
   protected Entity damageSourceEntity;
   private boolean isThornsDamage;

   public EntityDamageSource(String var1, Entity var2) {
      super(var1);
      this.damageSourceEntity = var2;
   }

   public EntityDamageSource setIsThornsDamage() {
      this.isThornsDamage = true;
      return this;
   }

   public boolean getIsThornsDamage() {
      return this.isThornsDamage;
   }

   @Nullable
   public Entity getEntity() {
      return this.damageSourceEntity;
   }

   public ITextComponent getDeathMessage(EntityLivingBase var1) {
      ItemStack var2 = this.damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.damageSourceEntity).getHeldItemMainhand() : null;
      String var3 = "death.attack." + this.damageType;
      String var4 = var3 + ".item";
      return var2 != null && var2.hasDisplayName() && I18n.canTranslate(var4) ? new TextComponentTranslation(var4, new Object[]{var1.getDisplayName(), this.damageSourceEntity.getDisplayName(), var2.getTextComponent()}) : new TextComponentTranslation(var3, new Object[]{var1.getDisplayName(), this.damageSourceEntity.getDisplayName()});
   }

   public boolean isDifficultyScaled() {
      return this.damageSourceEntity != null && this.damageSourceEntity instanceof EntityLivingBase && !(this.damageSourceEntity instanceof EntityPlayer);
   }

   @Nullable
   public Vec3d getDamageLocation() {
      return new Vec3d(this.damageSourceEntity.posX, this.damageSourceEntity.posY, this.damageSourceEntity.posZ);
   }
}
