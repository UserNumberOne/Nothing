package net.minecraft.entity.projectile;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class EntityPotion extends EntityThrowable {
   private static final DataParameter ITEM = EntityDataManager.createKey(EntityPotion.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private static final Logger LOGGER = LogManager.getLogger();

   public EntityPotion(World world) {
      super(world);
   }

   public EntityPotion(World world, EntityLivingBase entityliving, ItemStack itemstack) {
      super(world, entityliving);
      this.setItem(itemstack);
   }

   public EntityPotion(World world, double d0, double d1, double d2, @Nullable ItemStack itemstack) {
      super(world, d0, d1, d2);
      if (itemstack != null) {
         this.setItem(itemstack);
      }

   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
   }

   public ItemStack getPotion() {
      ItemStack itemstack = (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
      if (itemstack == null || itemstack.getItem() != Items.SPLASH_POTION && itemstack.getItem() != Items.LINGERING_POTION) {
         if (this.world != null) {
            LOGGER.error("ThrownPotion entity {} has no item?!", new Object[]{this.getEntityId()});
         }

         return new ItemStack(Items.SPLASH_POTION);
      } else {
         return itemstack;
      }
   }

   public void setItem(@Nullable ItemStack itemstack) {
      this.getDataManager().set(ITEM, Optional.fromNullable(itemstack));
      this.getDataManager().setDirty(ITEM);
   }

   protected float getGravityVelocity() {
      return 0.05F;
   }

   protected void onImpact(RayTraceResult movingobjectposition) {
      if (!this.world.isRemote) {
         ItemStack itemstack = this.getPotion();
         PotionType potionregistry = PotionUtils.getPotionFromItem(itemstack);
         List list = PotionUtils.getEffectsFromStack(itemstack);
         if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK && potionregistry == PotionTypes.WATER && list.isEmpty()) {
            BlockPos blockposition = movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit);
            this.extinguishFires(blockposition);

            for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
               this.extinguishFires(blockposition.offset(enumdirection));
            }

            this.world.playEvent(2002, new BlockPos(this), PotionType.getID(potionregistry));
            this.setDead();
         } else if (this.isLingering()) {
            EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
            entityareaeffectcloud.projectileSource = this.projectileSource;
            entityareaeffectcloud.setOwner(this.getThrower());
            entityareaeffectcloud.setRadius(3.0F);
            entityareaeffectcloud.setRadiusOnUse(-0.5F);
            entityareaeffectcloud.setWaitTime(10);
            entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float)entityareaeffectcloud.getDuration());
            entityareaeffectcloud.setPotion(potionregistry);

            for(PotionEffect mobeffect : PotionUtils.getFullEffectsFromItem(itemstack)) {
               entityareaeffectcloud.addEffect(new PotionEffect(mobeffect.getPotion(), mobeffect.getDuration(), mobeffect.getAmplifier()));
            }

            LingeringPotionSplashEvent event = CraftEventFactory.callLingeringPotionSplashEvent(this, entityareaeffectcloud);
            if (!event.isCancelled() && !entityareaeffectcloud.isDead) {
               this.world.spawnEntity(entityareaeffectcloud);
            } else {
               entityareaeffectcloud.isDead = true;
            }
         } else {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
            List list1 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);
            HashMap affected = new HashMap();
            if (!list1.isEmpty()) {
               for(EntityLivingBase entityliving : list1) {
                  if (entityliving.canBeHitWithPotion()) {
                     double d0 = this.getDistanceSqToEntity(entityliving);
                     if (d0 < 16.0D) {
                        double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                        if (entityliving == movingobjectposition.entityHit) {
                           d1 = 1.0D;
                        }

                        affected.put((LivingEntity)entityliving.getBukkitEntity(), Double.valueOf(d1));
                     }
                  }
               }
            }

            PotionSplashEvent event = CraftEventFactory.callPotionSplashEvent(this, affected);
            if (!event.isCancelled() && list != null && !list.isEmpty()) {
               label123:
               for(LivingEntity victim : event.getAffectedEntities()) {
                  if (victim instanceof CraftLivingEntity) {
                     EntityLivingBase entityliving = ((CraftLivingEntity)victim).getHandle();
                     double d1 = event.getIntensity(victim);
                     Iterator iterator2 = list.iterator();

                     while(true) {
                        PotionEffect mobeffect1;
                        Potion mobeffectlist;
                        while(true) {
                           if (!iterator2.hasNext()) {
                              continue label123;
                           }

                           mobeffect1 = (PotionEffect)iterator2.next();
                           mobeffectlist = mobeffect1.getPotion();
                           if (this.world.pvpMode || !(this.getThrower() instanceof EntityPlayerMP) || !(entityliving instanceof EntityPlayerMP) || entityliving == this.getThrower()) {
                              break;
                           }

                           int i = Potion.getIdFromPotion(mobeffectlist);
                           if (i != 2 && i != 4 && i != 7 && i != 15 && i != 17 && i != 18 && i != 19) {
                              break;
                           }
                        }

                        if (mobeffectlist.isInstant()) {
                           mobeffectlist.affectEntity(this, this.getThrower(), entityliving, mobeffect1.getAmplifier(), d1);
                        } else {
                           int i = (int)(d1 * (double)mobeffect1.getDuration() + 0.5D);
                           if (i > 20) {
                              entityliving.addPotionEffect(new PotionEffect(mobeffectlist, i, mobeffect1.getAmplifier()));
                           }
                        }
                     }
                  }
               }
            }
         }

         this.world.playEvent(2002, new BlockPos(this), PotionType.getID(potionregistry));
         this.setDead();
      }

   }

   public boolean isLingering() {
      return this.getPotion().getItem() == Items.LINGERING_POTION;
   }

   private void extinguishFires(BlockPos blockposition) {
      if (this.world.getBlockState(blockposition).getBlock() == Blocks.FIRE) {
         if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, Blocks.AIR, 0).isCancelled()) {
            return;
         }

         this.world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 2);
      }

   }

   public static void registerFixesPotion(DataFixer dataconvertermanager) {
      EntityThrowable.registerFixesThrowable(dataconvertermanager, "ThrownPotion");
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackData("ThrownPotion", new String[]{"Potion"}));
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("Potion"));
      if (itemstack == null) {
         this.setDead();
      } else {
         this.setItem(itemstack);
      }

   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      ItemStack itemstack = this.getPotion();
      if (itemstack != null) {
         nbttagcompound.setTag("Potion", itemstack.writeToNBT(new NBTTagCompound()));
      }

   }
}
