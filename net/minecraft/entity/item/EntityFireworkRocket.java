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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFireworkRocket extends Entity {
   private static final DataParameter FIREWORK_ITEM = EntityDataManager.createKey(EntityFireworkRocket.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private int fireworkAge;
   private int lifetime;

   public EntityFireworkRocket(World var1) {
      super(var1);
      this.setSize(0.25F, 0.25F);
   }

   protected void entityInit() {
      this.dataManager.register(FIREWORK_ITEM, Optional.absent());
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      return var1 < 4096.0D;
   }

   public EntityFireworkRocket(World var1, double var2, double var4, double var6, @Nullable ItemStack var8) {
      super(var1);
      this.fireworkAge = 0;
      this.setSize(0.25F, 0.25F);
      this.setPosition(var2, var4, var6);
      int var9 = 1;
      if (var8 != null && var8.hasTagCompound()) {
         this.dataManager.set(FIREWORK_ITEM, Optional.of(var8));
         NBTTagCompound var10 = var8.getTagCompound();
         NBTTagCompound var11 = var10.getCompoundTag("Fireworks");
         var9 += var11.getByte("Flight");
      }

      this.motionX = this.rand.nextGaussian() * 0.001D;
      this.motionZ = this.rand.nextGaussian() * 0.001D;
      this.motionY = 0.05D;
      this.lifetime = 10 * var9 + this.rand.nextInt(6) + this.rand.nextInt(7);
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         float var7 = MathHelper.sqrt(var1 * var1 + var5 * var5);
         this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.29577951308232D);
         this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var7) * 57.29577951308232D);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

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
      float var1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

      for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var1) * 57.29577951308232D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
         this.world.setEntityState(this, (byte)17);
         this.setDead();
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 17 && this.world.isRemote) {
         ItemStack var2 = (ItemStack)((Optional)this.dataManager.get(FIREWORK_ITEM)).orNull();
         NBTTagCompound var3 = null;
         if (var2 != null && var2.hasTagCompound()) {
            var3 = var2.getTagCompound().getCompoundTag("Fireworks");
         }

         this.world.makeFireworks(this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ, var3);
      }

      super.handleStatusUpdate(var1);
   }

   public static void registerFixesFireworkRocket(DataFixer var0) {
      var0.registerWalker(FixTypes.ENTITY, new ItemStackData("FireworksRocketEntity", new String[]{"FireworksItem"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("Life", this.fireworkAge);
      var1.setInteger("LifeTime", this.lifetime);
      ItemStack var2 = (ItemStack)((Optional)this.dataManager.get(FIREWORK_ITEM)).orNull();
      if (var2 != null) {
         var1.setTag("FireworksItem", var2.writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.fireworkAge = var1.getInteger("Life");
      this.lifetime = var1.getInteger("LifeTime");
      NBTTagCompound var2 = var1.getCompoundTag("FireworksItem");
      if (var2 != null) {
         ItemStack var3 = ItemStack.loadItemStackFromNBT(var2);
         if (var3 != null) {
            this.dataManager.set(FIREWORK_ITEM, Optional.of(var3));
         }
      }

   }

   public float getBrightness(float var1) {
      return super.getBrightness(var1);
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      return super.getBrightnessForRender(var1);
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }
}
