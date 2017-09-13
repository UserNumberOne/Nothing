package net.minecraft.entity.projectile;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;

public class EntityTippedArrow extends EntityArrow {
   private static final DataParameter COLOR = EntityDataManager.createKey(EntityTippedArrow.class, DataSerializers.VARINT);
   private PotionType potion = PotionTypes.EMPTY;
   public final Set customPotionEffects = Sets.newHashSet();

   public EntityTippedArrow(World world) {
      super(world);
   }

   public EntityTippedArrow(World world, double d0, double d1, double d2) {
      super(world, d0, d1, d2);
   }

   public EntityTippedArrow(World world, EntityLivingBase entityliving) {
      super(world, entityliving);
   }

   public void setPotionEffect(ItemStack itemstack) {
      if (itemstack.getItem() == Items.TIPPED_ARROW) {
         this.potion = PotionUtils.getPotionTypeFromNBT(itemstack.getTagCompound());
         List list = PotionUtils.getFullEffectsFromItem(itemstack);
         if (!list.isEmpty()) {
            for(PotionEffect mobeffect : list) {
               this.customPotionEffects.add(new PotionEffect(mobeffect));
            }
         }

         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, list))));
      } else if (itemstack.getItem() == Items.ARROW) {
         this.potion = PotionTypes.EMPTY;
         this.customPotionEffects.clear();
         this.dataManager.set(COLOR, Integer.valueOf(0));
      }

   }

   public void addEffect(PotionEffect mobeffect) {
      this.customPotionEffects.add(mobeffect);
      this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(COLOR, Integer.valueOf(0));
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         if (this.inGround) {
            if (this.timeInGround % 5 == 0) {
               this.spawnPotionParticles(1);
            }
         } else {
            this.spawnPotionParticles(2);
         }
      } else if (this.inGround && this.timeInGround != 0 && !this.customPotionEffects.isEmpty() && this.timeInGround >= 600) {
         this.world.setEntityState(this, (byte)0);
         this.potion = PotionTypes.EMPTY;
         this.customPotionEffects.clear();
         this.dataManager.set(COLOR, Integer.valueOf(0));
      }

   }

   private void spawnPotionParticles(int i) {
      int j = this.getColor();
      if (j != 0 && i > 0) {
         double d0 = (double)(j >> 16 & 255) / 255.0D;
         double d1 = (double)(j >> 8 & 255) / 255.0D;
         double d2 = (double)(j >> 0 & 255) / 255.0D;

         for(int k = 0; k < i; ++k) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, d0, d1, d2);
         }
      }

   }

   public void refreshEffects() {
      this.getDataManager().set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
   }

   public String getType() {
      return ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString();
   }

   public void setType(String string) {
      this.potion = (PotionType)PotionType.REGISTRY.getObject(new ResourceLocation(string));
      this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
   }

   public boolean isTipped() {
      return !this.customPotionEffects.isEmpty() || this.potion != PotionTypes.EMPTY;
   }

   public int getColor() {
      return ((Integer)this.dataManager.get(COLOR)).intValue();
   }

   public static void registerFixesTippedArrow(DataFixer dataconvertermanager) {
      EntityArrow.registerFixesArrow(dataconvertermanager, "TippedArrow");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      if (this.potion != PotionTypes.EMPTY && this.potion != null) {
         nbttagcompound.setString("Potion", ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString());
      }

      if (!this.customPotionEffects.isEmpty()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(PotionEffect mobeffect : this.customPotionEffects) {
            nbttaglist.appendTag(mobeffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         nbttagcompound.setTag("CustomPotionEffects", nbttaglist);
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("Potion", 8)) {
         this.potion = PotionUtils.getPotionTypeFromNBT(nbttagcompound);
      }

      for(PotionEffect mobeffect : PotionUtils.getFullEffectsFromTag(nbttagcompound)) {
         this.addEffect(mobeffect);
      }

      if (this.potion != PotionTypes.EMPTY || !this.customPotionEffects.isEmpty()) {
         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
      }

   }

   protected void arrowHit(EntityLivingBase entityliving) {
      super.arrowHit(entityliving);

      for(PotionEffect mobeffect : this.potion.getEffects()) {
         entityliving.addPotionEffect(new PotionEffect(mobeffect.getPotion(), mobeffect.getDuration() / 8, mobeffect.getAmplifier(), mobeffect.getIsAmbient(), mobeffect.doesShowParticles()));
      }

      if (!this.customPotionEffects.isEmpty()) {
         for(PotionEffect mobeffect : this.customPotionEffects) {
            entityliving.addPotionEffect(mobeffect);
         }
      }

   }

   protected ItemStack getArrowStack() {
      if (this.customPotionEffects.isEmpty() && this.potion == PotionTypes.EMPTY) {
         return new ItemStack(Items.ARROW);
      } else {
         ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
         PotionUtils.addPotionToItemStack(itemstack, this.potion);
         PotionUtils.appendEffects(itemstack, this.customPotionEffects);
         return itemstack;
      }
   }
}
