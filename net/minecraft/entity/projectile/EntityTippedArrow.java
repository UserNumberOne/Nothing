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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTippedArrow extends EntityArrow {
   private static final DataParameter COLOR = EntityDataManager.createKey(EntityTippedArrow.class, DataSerializers.VARINT);
   private PotionType potion = PotionTypes.EMPTY;
   private final Set customPotionEffects = Sets.newHashSet();

   public EntityTippedArrow(World var1) {
      super(var1);
   }

   public EntityTippedArrow(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public EntityTippedArrow(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   public void setPotionEffect(ItemStack var1) {
      if (var1.getItem() == Items.TIPPED_ARROW) {
         this.potion = PotionUtils.getPotionTypeFromNBT(var1.getTagCompound());
         List var2 = PotionUtils.getFullEffectsFromItem(var1);
         if (!var2.isEmpty()) {
            for(PotionEffect var4 : var2) {
               this.customPotionEffects.add(new PotionEffect(var4));
            }
         }

         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, var2))));
      } else if (var1.getItem() == Items.ARROW) {
         this.potion = PotionTypes.EMPTY;
         this.customPotionEffects.clear();
         this.dataManager.set(COLOR, Integer.valueOf(0));
      }

   }

   public void addEffect(PotionEffect var1) {
      this.customPotionEffects.add(var1);
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
      int var2 = this.getColor();
      if (var2 != 0 && var1 > 0) {
         double var3 = (double)(var2 >> 16 & 255) / 255.0D;
         double var5 = (double)(var2 >> 8 & 255) / 255.0D;
         double var7 = (double)(var2 >> 0 & 255) / 255.0D;

         for(int var9 = 0; var9 < var1; ++var9) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, var3, var5, var7);
         }
      }

   }

   public int getColor() {
      return ((Integer)this.dataManager.get(COLOR)).intValue();
   }

   public static void registerFixesTippedArrow(DataFixer var0) {
      EntityArrow.registerFixesArrow(var0, "TippedArrow");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      if (this.potion != PotionTypes.EMPTY && this.potion != null) {
         var1.setString("Potion", ((ResourceLocation)PotionType.REGISTRY.getNameForObject(this.potion)).toString());
      }

      if (!this.customPotionEffects.isEmpty()) {
         NBTTagList var2 = new NBTTagList();

         for(PotionEffect var4 : this.customPotionEffects) {
            var2.appendTag(var4.writeCustomPotionEffectToNBT(new NBTTagCompound()));
         }

         var1.setTag("CustomPotionEffects", var2);
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("Potion", 8)) {
         this.potion = PotionUtils.getPotionTypeFromNBT(var1);
      }

      for(PotionEffect var3 : PotionUtils.getFullEffectsFromTag(var1)) {
         this.addEffect(var3);
      }

      if (this.potion != PotionTypes.EMPTY || !this.customPotionEffects.isEmpty()) {
         this.dataManager.set(COLOR, Integer.valueOf(PotionUtils.getPotionColorFromEffectList(PotionUtils.mergeEffects(this.potion, this.customPotionEffects))));
      }

   }

   protected void arrowHit(EntityLivingBase var1) {
      super.arrowHit(var1);

      for(PotionEffect var3 : this.potion.getEffects()) {
         if (var3.getPotion().isInstant()) {
            var3.getPotion().affectEntity(this, this.shootingEntity, var1, var3.getAmplifier(), 1.0D);
         } else {
            var1.addPotionEffect(new PotionEffect(var3.getPotion(), var3.getDuration() / 8, var3.getAmplifier(), var3.getIsAmbient(), var3.doesShowParticles()));
         }
      }

      if (!this.customPotionEffects.isEmpty()) {
         for(PotionEffect var5 : this.customPotionEffects) {
            var1.addPotionEffect(var5);
         }
      }

   }

   protected ItemStack getArrowStack() {
      if (this.customPotionEffects.isEmpty() && this.potion == PotionTypes.EMPTY) {
         return new ItemStack(Items.ARROW);
      } else {
         ItemStack var1 = new ItemStack(Items.TIPPED_ARROW);
         PotionUtils.addPotionToItemStack(var1, this.potion);
         PotionUtils.appendEffects(var1, this.customPotionEffects);
         return var1;
      }
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 0) {
         int var2 = this.getColor();
         if (var2 > 0) {
            double var3 = (double)(var2 >> 16 & 255) / 255.0D;
            double var5 = (double)(var2 >> 8 & 255) / 255.0D;
            double var7 = (double)(var2 >> 0 & 255) / 255.0D;

            for(int var9 = 0; var9 < 20; ++var9) {
               this.world.spawnParticle(EnumParticleTypes.SPELL_MOB, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, var3, var5, var7);
            }
         }
      } else {
         super.handleStatusUpdate(var1);
      }

   }
}
