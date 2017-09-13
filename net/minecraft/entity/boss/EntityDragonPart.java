package net.minecraft.entity.boss;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

public class EntityDragonPart extends Entity {
   public final IEntityMultiPart entityDragonObj;
   public final String partName;

   public EntityDragonPart(IEntityMultiPart var1, String var2, float var3, float var4) {
      super(parent.getWorld());
      this.setSize(base, sizeHeight);
      this.entityDragonObj = parent;
      this.partName = partName;
   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return this.isEntityInvulnerable(source) ? false : this.entityDragonObj.attackEntityFromPart(this, source, amount);
   }

   public boolean isEntityEqual(Entity var1) {
      return this == entityIn || this.entityDragonObj == entityIn;
   }
}
