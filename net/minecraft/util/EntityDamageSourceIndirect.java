package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

public class EntityDamageSourceIndirect extends EntityDamageSource {
   private final Entity indirectEntity;

   public EntityDamageSourceIndirect(String var1, Entity var2, @Nullable Entity var3) {
      super(damageTypeIn, source);
      this.indirectEntity = indirectEntityIn;
   }

   @Nullable
   public Entity getSourceOfDamage() {
      return this.damageSourceEntity;
   }

   @Nullable
   public Entity getEntity() {
      return this.indirectEntity;
   }

   public ITextComponent getDeathMessage(EntityLivingBase var1) {
      ITextComponent itextcomponent = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
      ItemStack itemstack = this.indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.indirectEntity).getHeldItemMainhand() : null;
      String s = "death.attack." + this.damageType;
      String s1 = s + ".item";
      return itemstack != null && itemstack.hasDisplayName() && I18n.canTranslate(s1) ? new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName(), itextcomponent, itemstack.getTextComponent()}) : new TextComponentTranslation(s, new Object[]{entityLivingBaseIn.getDisplayName(), itextcomponent});
   }
}
