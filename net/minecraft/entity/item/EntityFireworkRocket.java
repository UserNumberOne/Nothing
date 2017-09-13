package net.minecraft.entity.item;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityFireworkRocket extends Entity {
   public static final DataParameter FIREWORK_ITEM = EntityDataManager.createKey(EntityFireworkRocket.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private int fireworkAge;
   public int lifetime;

   public EntityFireworkRocket(World world) {
      super(world);
      this.setSize(0.25F, 0.25F);
   }

   protected void entityInit() {
      this.dataManager.register(FIREWORK_ITEM, Optional.absent());
   }

   public EntityFireworkRocket(World world, double d0, double d1, double d2, @Nullable ItemStack itemstack) {
      super(world);
      this.fireworkAge = 0;
      this.setSize(0.25F, 0.25F);
      this.setPosition(d0, d1, d2);
      int i = 1;
      if (itemstack != null && itemstack.hasTagCompound()) {
         this.dataManager.set(FIREWORK_ITEM, Optional.of(itemstack));
         NBTTagCompound nbttagcompound = itemstack.getTagCompound();
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Fireworks");
         i += nbttagcompound1.getByte("Flight");
      }

      this.motionX = this.rand.nextGaussian() * 0.001D;
      this.motionZ = this.rand.nextGaussian() * 0.001D;
      this.motionY = 0.05D;
      this.lifetime = 10 * i + this.rand.nextInt(6) + this.rand.nextInt(7);
   }

   public void onUpdate() {
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      super.onUpdate();
      this.motionX *= 1.15D;
      this.motionZ *= 1.15D;
      this.motionY += 0.04D;
      this.move(this.motionX, this.motionY, this.motionZ);
      float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

      for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
         ;
      }

      while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }

      while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }

      while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      if (this.fireworkAge == 0 && !this.isSilent()) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
      }

      ++this.fireworkAge;
      if (this.world.isRemote && this.fireworkAge % 2 < 2) {
         this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX, this.posY - 0.3D, this.posZ, this.rand.nextGaussian() * 0.05D, -this.motionY * 0.5D, this.rand.nextGaussian() * 0.05D);
      }

      if (!this.world.isRemote && this.fireworkAge > this.lifetime) {
         if (!CraftEventFactory.callFireworkExplodeEvent(this).isCancelled()) {
            this.world.setEntityState(this, (byte)17);
         }

         this.setDead();
      }

   }

   public static void registerFixesFireworkRocket(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackData("FireworksRocketEntity", new String[]{"FireworksItem"}));
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("Life", this.fireworkAge);
      nbttagcompound.setInteger("LifeTime", this.lifetime);
      ItemStack itemstack = (ItemStack)((Optional)this.dataManager.get(FIREWORK_ITEM)).orNull();
      if (itemstack != null) {
         nbttagcompound.setTag("FireworksItem", itemstack.writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.fireworkAge = nbttagcompound.getInteger("Life");
      this.lifetime = nbttagcompound.getInteger("LifeTime");
      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("FireworksItem");
      if (nbttagcompound1 != null) {
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         if (itemstack != null) {
            this.dataManager.set(FIREWORK_ITEM, Optional.of(itemstack));
         }
      }

   }

   public float getBrightness(float f) {
      return super.getBrightness(f);
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }
}
