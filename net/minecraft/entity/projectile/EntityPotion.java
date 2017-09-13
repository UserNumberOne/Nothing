package net.minecraft.entity.projectile;

import com.google.common.base.Optional;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
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
            BlockPos var18 = var1.getBlockPos().offset(var1.sideHit);
            this.extinguishFires(var18);

            for(EnumFacing var22 : EnumFacing.Plane.HORIZONTAL) {
               this.extinguishFires(var18.offset(var22));
            }

            this.world.playEvent(2002, new BlockPos(this), PotionType.getID(var3));
            this.setDead();
         } else {
            if (!var4.isEmpty()) {
               if (this.isLingering()) {
                  EntityAreaEffectCloud var17 = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
                  var17.setOwner(this.getThrower());
                  var17.setRadius(3.0F);
                  var17.setRadiusOnUse(-0.5F);
                  var17.setWaitTime(10);
                  var17.setRadiusPerTick(-var17.getRadius() / (float)var17.getDuration());
                  var17.setPotion(var3);

                  for(PotionEffect var21 : PotionUtils.getFullEffectsFromItem(var2)) {
                     var17.addEffect(new PotionEffect(var21.getPotion(), var21.getDuration(), var21.getAmplifier()));
                  }

                  this.world.spawnEntity(var17);
               } else {
                  AxisAlignedBB var5 = this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D);
                  List var6 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, var5);
                  if (!var6.isEmpty()) {
                     for(EntityLivingBase var8 : var6) {
                        if (var8.canBeHitWithPotion()) {
                           double var9 = this.getDistanceSqToEntity(var8);
                           if (var9 < 16.0D) {
                              double var11 = 1.0D - Math.sqrt(var9) / 4.0D;
                              if (var8 == var1.entityHit) {
                                 var11 = 1.0D;
                              }

                              for(PotionEffect var14 : var4) {
                                 Potion var15 = var14.getPotion();
                                 if (var15.isInstant()) {
                                    var15.affectEntity(this, this.getThrower(), var8, var14.getAmplifier(), var11);
                                 } else {
                                    int var16 = (int)(var11 * (double)var14.getDuration() + 0.5D);
                                    if (var16 > 20) {
                                       var8.addPotionEffect(new PotionEffect(var15, var16, var14.getAmplifier()));
                                    }
                                 }
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

   }

   private boolean isLingering() {
      return this.getPotion().getItem() == Items.LINGERING_POTION;
   }

   private void extinguishFires(BlockPos var1) {
      if (this.world.getBlockState(var1).getBlock() == Blocks.FIRE) {
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
