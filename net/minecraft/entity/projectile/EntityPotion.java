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

   public EntityPotion(World var1) {
      super(var1);
   }

   public EntityPotion(World var1, EntityLivingBase var2, ItemStack var3) {
      super(var1, var2);
      this.setItem(var3);
   }

   public EntityPotion(World var1, double var2, double var4, double var6, @Nullable ItemStack var8) {
      super(var1, var2, var4, var6);
      if (var8 != null) {
         this.setItem(var8);
      }

   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
   }

   public ItemStack getPotion() {
      ItemStack var1 = (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
      if (var1 == null || var1.getItem() != Items.SPLASH_POTION && var1.getItem() != Items.LINGERING_POTION) {
         if (this.world != null) {
            LOGGER.error("ThrownPotion entity {} has no item?!", new Object[]{this.getEntityId()});
         }

         return new ItemStack(Items.SPLASH_POTION);
      } else {
         return var1;
      }
   }

   public void setItem(@Nullable ItemStack var1) {
      this.getDataManager().set(ITEM, Optional.fromNullable(var1));
      this.getDataManager().setDirty(ITEM);
   }

   protected float getGravityVelocity() {
      return 0.05F;
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         ItemStack var2 = this.getPotion();
         PotionType var3 = PotionUtils.getPotionFromItem(var2);
         List var4 = PotionUtils.getEffectsFromStack(var2);
         if (var1.typeOfHit == RayTraceResult.Type.BLOCK && var3 == PotionTypes.WATER && var4.isEmpty()) {
            BlockPos var22 = var1.getBlockPos().offset(var1.sideHit);
            this.extinguishFires(var22);

            for(EnumFacing var26 : EnumFacing.Plane.HORIZONTAL) {
               this.extinguishFires(var22.offset(var26));
            }

            this.world.playEvent(2002, new BlockPos(this), PotionType.getID(var3));
            this.setDead();
         } else if (this.isLingering()) {
            EntityAreaEffectCloud var21 = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
            var21.projectileSource = this.projectileSource;
            var21.setOwner(this.getThrower());
            var21.setRadius(3.0F);
            var21.setRadiusOnUse(-0.5F);
            var21.setWaitTime(10);
            var21.setRadiusPerTick(-var21.getRadius() / (float)var21.getDuration());
            var21.setPotion(var3);

            for(PotionEffect var24 : PotionUtils.getFullEffectsFromItem(var2)) {
               var21.addEffect(new PotionEffect(var24.getPotion(), var24.getDuration(), var24.getAmplifier()));
            }

            LingeringPotionSplashEvent var25 = CraftEventFactory.callLingeringPotionSplashEvent(this, var21);
            if (!var25.isCancelled() && !var21.isDead) {
               this.world.spawnEntity(var21);
            } else {
               var21.isDead = true;
            }
         } else {
            AxisAlignedBB var5 = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
            List var7 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, var5);
            HashMap var8 = new HashMap();
            if (!var7.isEmpty()) {
               for(EntityLivingBase var10 : var7) {
                  if (var10.canBeHitWithPotion()) {
                     double var11 = this.getDistanceSqToEntity(var10);
                     if (var11 < 16.0D) {
                        double var13 = 1.0D - Math.sqrt(var11) / 4.0D;
                        if (var10 == var1.entityHit) {
                           var13 = 1.0D;
                        }

                        var8.put((LivingEntity)var10.getBukkitEntity(), Double.valueOf(var13));
                     }
                  }
               }
            }

            PotionSplashEvent var27 = CraftEventFactory.callPotionSplashEvent(this, var8);
            if (!var27.isCancelled() && var4 != null && !var4.isEmpty()) {
               label123:
               for(LivingEntity var28 : var27.getAffectedEntities()) {
                  if (var28 instanceof CraftLivingEntity) {
                     EntityLivingBase var16 = ((CraftLivingEntity)var28).getHandle();
                     double var29 = var27.getIntensity(var28);
                     Iterator var17 = var4.iterator();

                     while(true) {
                        PotionEffect var18;
                        Potion var19;
                        while(true) {
                           if (!var17.hasNext()) {
                              continue label123;
                           }

                           var18 = (PotionEffect)var17.next();
                           var19 = var18.getPotion();
                           if (this.world.pvpMode || !(this.getThrower() instanceof EntityPlayerMP) || !(var16 instanceof EntityPlayerMP) || var16 == this.getThrower()) {
                              break;
                           }

                           int var20 = Potion.getIdFromPotion(var19);
                           if (var20 != 2 && var20 != 4 && var20 != 7 && var20 != 15 && var20 != 17 && var20 != 18 && var20 != 19) {
                              break;
                           }
                        }

                        if (var19.isInstant()) {
                           var19.affectEntity(this, this.getThrower(), var16, var18.getAmplifier(), var29);
                        } else {
                           int var30 = (int)(var29 * (double)var18.getDuration() + 0.5D);
                           if (var30 > 20) {
                              var16.addPotionEffect(new PotionEffect(var19, var30, var18.getAmplifier()));
                           }
                        }
                     }
                  }
               }
            }
         }

         this.world.playEvent(2002, new BlockPos(this), PotionType.getID(var3));
         this.setDead();
      }

   }

   public boolean isLingering() {
      return this.getPotion().getItem() == Items.LINGERING_POTION;
   }

   private void extinguishFires(BlockPos var1) {
      if (this.world.getBlockState(var1).getBlock() == Blocks.FIRE) {
         if (CraftEventFactory.callEntityChangeBlockEvent(this, var1, Blocks.AIR, 0).isCancelled()) {
            return;
         }

         this.world.setBlockState(var1, Blocks.AIR.getDefaultState(), 2);
      }

   }

   public static void registerFixesPotion(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(var0, "ThrownPotion");
      var0.registerWalker(FixTypes.ENTITY, new ItemStackData("ThrownPotion", new String[]{"Potion"}));
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      ItemStack var2 = ItemStack.loadItemStackFromNBT(var1.getCompoundTag("Potion"));
      if (var2 == null) {
         this.setDead();
      } else {
         this.setItem(var2);
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      ItemStack var2 = this.getPotion();
      if (var2 != null) {
         var1.setTag("Potion", var2.writeToNBT(new NBTTagCompound()));
      }

   }
}
