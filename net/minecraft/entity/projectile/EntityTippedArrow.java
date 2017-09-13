package net.minecraft.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTippedArrow extends EntityArrow {
   private static final DataParameter COLOR = EntityDataManager.createKey(EntityTippedArrow.class, DataSerializers.VARINT);
   private PotionType potion = PotionTypes.EMPTY;
   private final Set customPotionEffects = Sets.newHashSet();

   public EntityTippedArrow(World var1) {
      super(worldIn);
   }

   public EntityTippedArrow(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public EntityTippedArrow(World var1, EntityLivingBase var2) {
      super(worldIn, shooter);
   }

   public void setPotionEffect(ItemStack var1) {
      if (stack.getItem() == Items.TIPPED_ARROW) {
         this.potion = PotionUtils.getPotionTypeFromNBT(stack.getTagCompound());
         Collection collection = PotionUtils.getFullEffectsFromItem(stack);
         if (!collection.isEmpty()) {
            for(PotionEffect potioneffect : collection) {
               this.customPotionEffects.add(new PotionEffect(potioneffect));
            }
         }

         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, collection))));
      } else if (stack.getItem() == Items.ARROW) {
         this.potion = PotionTypes.EMPTY;
         this.customPotionEffects.clear();
         this.dataManager.set(COLOR, Integer.valueOf(0));
      }

   }

   public void addEffect(PotionEffect var1) {
      this.customPotionEffects.add(effect);
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

   private void spawnPotionParticles(int var1) {
      int i = this.getColor();
      if (i != 0 && particleCount > 0) {
         double d0 = (double)(i >> 16 & 255) / 255.0D;
         double d1 = (double)(i >> 8 & 255) / 255.0D;
         double d2 = (double)(i >> 0 & 255) / 255.0D;

         for(int j = 0; j < particleCount; ++j) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, d0, d1, d2);
         }
      }

   }

   public int getColor() {
      return ((Integer)this.dataManager.get(COLOR)).intValue();
   }

   public static void registerFixesTippedArrow(DataFixer var0) {
      EntityArrow.registerFixesArrow(fixer, "TippedArrow");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      if (this.potion != PotionTypes.EMPTY && this.potion != null) {
         compound.setString("Potion", ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString());
      }

      if (!this.customPotionEffects.isEmpty()) {
         NBTTagList nbttaglist = new NBTTagList();

         for(PotionEffect potioneffect : this.customPotionEffects) {
            nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         compound.setTag("CustomPotionEffects", nbttaglist);
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("Potion", 8)) {
         this.potion = PotionUtils.getPotionTypeFromNBT(compound);
      }

      for(PotionEffect potioneffect : PotionUtils.getFullEffectsFromTag(compound)) {
         this.addEffect(potioneffect);
      }

      if (this.potion != PotionTypes.EMPTY || !this.customPotionEffects.isEmpty()) {
         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
      }

   }

   protected void arrowHit(EntityLivingBase var1) {
      super.arrowHit(living);

      for(PotionEffect potioneffect : this.potion.getEffects()) {
         if (potioneffect.getPotion().isInstant()) {
            potioneffect.getPotion().affectEntity(this, this.shootingEntity, living, potioneffect.getAmplifier(), 1.0D);
         } else {
            living.addPotionEffect(new PotionEffect(potioneffect.getPotion(), potioneffect.getDuration() / 8, potioneffect.getAmplifier(), potioneffect.getIsAmbient(), potioneffect.doesShowParticles()));
         }
      }

      if (!this.customPotionEffects.isEmpty()) {
         for(PotionEffect potioneffect1 : this.customPotionEffects) {
            living.addPotionEffect(potioneffect1);
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

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 0) {
         int i = this.getColor();
         if (i > 0) {
            double d0 = (double)(i >> 16 & 255) / 255.0D;
            double d1 = (double)(i >> 8 & 255) / 255.0D;
            double d2 = (double)(i >> 0 & 255) / 255.0D;

            for(int j = 0; j < 20; ++j) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, d0, d1, d2);
            }
         }
      } else {
         super.handleStatusUpdate(id);
      }

   }
}
