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
      super(var1, var2);
      this.indirectEntity = var3;
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
      ITextComponent var2 = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
      ItemStack var3 = this.indirectEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.indirectEntity).getHeldItemMainhand() : null;
      String var4 = "death.attack." + this.damageType;
      String var5 = var4 + ".item";
      return var3 != null && var3.hasDisplayName() && I18n.canTranslate(var5) ? new TextComponentTranslation(var5, new Object[]{var1.getDisplayName(), var2, var3.getTextComponent()}) : new TextComponentTranslation(var4, new Object[]{var1.getDisplayName(), var2});
   }
}
