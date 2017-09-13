package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Explosion {
   private final boolean isFlaming;
   private final boolean isSmoking;
   private final Random explosionRNG = new Random();
   private final World world;
   private final double explosionX;
   private final double explosionY;
   private final double explosionZ;
   public final Entity exploder;
   private final float explosionSize;
   private final List affectedBlockPositions = Lists.newArrayList();
   private final Map playerKnockbackMap = Maps.newHashMap();
   public boolean wasCanceled = false;

   public Explosion(World var1, Entity var2, double var3, double var5, double var7, float var9, boolean var10, boolean var11) {
      this.world = var1;
      this.exploder = var2;
      this.explosionSize = (float)Math.max((double)var9, 0.0D);
      this.explosionX = var3;
      this.explosionY = var5;
      this.explosionZ = var7;
      this.isFlaming = var10;
      this.isSmoking = var11;
   }

   public void doExplosionA() {
      if (this.explosionSize >= 0.1F) {
         HashSet var1 = Sets.newHashSet();

         for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
               for(int var4 = 0; var4 < 16; ++var4) {
                  if (var2 == 0 || var2 == 15 || var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15) {
                     double var5 = (double)((float)var2 / 15.0F * 2.0F - 1.0F);
                     double var7 = (double)((float)var3 / 15.0F * 2.0F - 1.0F);
                     double var9 = (double)((float)var4 / 15.0F * 2.0F - 1.0F);
                     double var11 = Math.sqrt(var5 * var5 + var7 * var7 + var9 * var9);
                     var5 = var5 / var11;
                     var7 = var7 / var11;
                     var9 = var9 / var11;
                     float var13 = this.explosionSize * (0.7F + this.world.rand.nextFloat() * 0.6F);
                     double var14 = this.explosionX;
                     double var16 = this.explosionY;

                     for(double var18 = this.explosionZ; var13 > 0.0F; var13 -= 0.22500001F) {
                        BlockPos var20 = new BlockPos(var14, var16, var18);
                        IBlockState var21 = this.world.getBlockState(var20);
                        if (var21.getMaterial() != Material.AIR) {
                           float var22 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, var20, var21) : var21.getBlock().getExplosionResistance((Entity)null);
                           var13 -= (var22 + 0.3F) * 0.3F;
                        }

                        if (var13 > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.world, var20, var21, var13)) && var20.getY() < 256 && var20.getY() >= 0) {
                           var1.add(var20);
                        }

                        var14 += var5 * 0.30000001192092896D;
                        var16 += var7 * 0.30000001192092896D;
                        var18 += var9 * 0.30000001192092896D;
                     }
                  }
               }
            }
         }

         this.affectedBlockPositions.addAll(var1);
         float var49 = this.explosionSize * 2.0F;
         int var50 = MathHelper.floor(this.explosionX - (double)var49 - 1.0D);
         int var51 = MathHelper.floor(this.explosionX + (double)var49 + 1.0D);
         int var23 = MathHelper.floor(this.explosionY - (double)var49 - 1.0D);
         int var24 = MathHelper.floor(this.explosionY + (double)var49 + 1.0D);
         int var25 = MathHelper.floor(this.explosionZ - (double)var49 - 1.0D);
         int var26 = MathHelper.floor(this.explosionZ + (double)var49 + 1.0D);
         List var27 = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)var50, (double)var23, (double)var25, (double)var51, (double)var24, (double)var26));
         Vec3d var28 = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

         for(int var29 = 0; var29 < var27.size(); ++var29) {
            Entity var30 = (Entity)var27.get(var29);
            if (!var30.isImmuneToExplosions()) {
               double var31 = var30.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)var49;
               if (var31 <= 1.0D) {
                  double var33 = var30.posX - this.explosionX;
                  double var35 = var30.posY + (double)var30.getEyeHeight() - this.explosionY;
                  double var37 = var30.posZ - this.explosionZ;
                  double var39 = (double)MathHelper.sqrt(var33 * var33 + var35 * var35 + var37 * var37);
                  if (var39 != 0.0D) {
                     var33 = var33 / var39;
                     var35 = var35 / var39;
                     var37 = var37 / var39;
                     double var41 = (double)this.world.getBlockDensity(var28, var30.getEntityBoundingBox());
                     double var43 = (1.0D - var31) * var41;
                     CraftEventFactory.entityDamage = this.exploder;
                     var30.forceExplosionKnockback = false;
                     boolean var45 = var30.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((var43 * var43 + var43) / 2.0D * 7.0D * (double)var49 + 1.0D)));
                     CraftEventFactory.entityDamage = null;
                     if (var45 || var30 instanceof EntityTNTPrimed || var30 instanceof EntityFallingBlock || var30.forceExplosionKnockback) {
                        double var46 = 1.0D;
                        if (var30 instanceof EntityLivingBase) {
                           var46 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)var30, var43);
                        }

                        var30.motionX += var33 * var46;
                        var30.motionY += var35 * var46;
                        var30.motionZ += var37 * var46;
                        if (var30 instanceof EntityPlayer) {
                           EntityPlayer var48 = (EntityPlayer)var30;
                           if (!var48.isSpectator() && (!var48.isCreative() || !var48.capabilities.isFlying)) {
                              this.playerKnockbackMap.put(var48, new Vec3d(var33 * var43, var35 * var43, var37 * var43));
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public void doExplosionB(boolean var1) {
      this.world.playSound((EntityPlayer)null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
      if (this.explosionSize >= 2.0F && this.isSmoking) {
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
      } else {
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
      }

      if (this.isSmoking) {
         CraftWorld var2 = this.world.getWorld();
         CraftEntity var3 = this.exploder == null ? null : this.exploder.getBukkitEntity();
         Location var4 = new Location(var2, this.explosionX, this.explosionY, this.explosionZ);
         ArrayList var5 = Lists.newArrayList();

         for(int var6 = this.affectedBlockPositions.size() - 1; var6 >= 0; --var6) {
            BlockPos var7 = (BlockPos)this.affectedBlockPositions.get(var6);
            Block var8 = var2.getBlockAt(var7.getX(), var7.getY(), var7.getZ());
            if (var8.getType() != org.bukkit.Material.AIR) {
               var5.add(var8);
            }
         }

         boolean var30;
         List var31;
         float var32;
         if (var3 != null) {
            EntityExplodeEvent var9 = new EntityExplodeEvent(var3, var4, var5, 1.0F / this.explosionSize);
            this.world.getServer().getPluginManager().callEvent(var9);
            var30 = var9.isCancelled();
            var31 = var9.blockList();
            var32 = var9.getYield();
         } else {
            BlockExplodeEvent var33 = new BlockExplodeEvent(var4.getBlock(), var5, 1.0F / this.explosionSize);
            this.world.getServer().getPluginManager().callEvent(var33);
            var30 = var33.isCancelled();
            var31 = var33.blockList();
            var32 = var33.getYield();
         }

         this.affectedBlockPositions.clear();

         for(Block var34 : var31) {
            BlockPos var11 = new BlockPos(var34.getX(), var34.getY(), var34.getZ());
            this.affectedBlockPositions.add(var11);
         }

         if (var30) {
            this.wasCanceled = true;
            return;
         }

         for(BlockPos var13 : this.affectedBlockPositions) {
            IBlockState var35 = this.world.getBlockState(var13);
            net.minecraft.block.Block var36 = var35.getBlock();
            if (var1) {
               double var14 = (double)((float)var13.getX() + this.world.rand.nextFloat());
               double var16 = (double)((float)var13.getY() + this.world.rand.nextFloat());
               double var18 = (double)((float)var13.getZ() + this.world.rand.nextFloat());
               double var20 = var14 - this.explosionX;
               double var22 = var16 - this.explosionY;
               double var24 = var18 - this.explosionZ;
               double var26 = (double)MathHelper.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
               var20 = var20 / var26;
               var22 = var22 / var26;
               var24 = var24 / var26;
               double var28 = 0.5D / (var26 / (double)this.explosionSize + 0.1D);
               var28 = var28 * (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
               var20 = var20 * var28;
               var22 = var22 * var28;
               var24 = var24 * var28;
               this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (var14 + this.explosionX) / 2.0D, (var16 + this.explosionY) / 2.0D, (var18 + this.explosionZ) / 2.0D, var20, var22, var24);
               this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var14, var16, var18, var20, var22, var24);
            }

            if (var35.getMaterial() != Material.AIR) {
               if (var36.canDropFromExplosion(this)) {
                  var36.dropBlockAsItemWithChance(this.world, var13, this.world.getBlockState(var13), var32, 0);
               }

               this.world.setBlockState(var13, Blocks.AIR.getDefaultState(), 3);
               var36.onBlockDestroyedByExplosion(this.world, var13, this);
            }
         }
      }

      if (this.isFlaming) {
         for(BlockPos var38 : this.affectedBlockPositions) {
            if (this.world.getBlockState(var38).getMaterial() == Material.AIR && this.world.getBlockState(var38.down()).isFullBlock() && this.explosionRNG.nextInt(3) == 0 && !CraftEventFactory.callBlockIgniteEvent(this.world, var38.getX(), var38.getY(), var38.getZ(), this).isCancelled()) {
               this.world.setBlockState(var38, Blocks.FIRE.getDefaultState());
            }
         }
      }

   }

   public Map getPlayerKnockbackMap() {
      return this.playerKnockbackMap;
   }

   public EntityLivingBase getExplosivePlacedBy() {
      return this.exploder == null ? null : (this.exploder instanceof EntityTNTPrimed ? ((EntityTNTPrimed)this.exploder).getTntPlacedBy() : (this.exploder instanceof EntityLivingBase ? (EntityLivingBase)this.exploder : (this.exploder instanceof EntityFireball ? ((EntityFireball)this.exploder).shootingEntity : null)));
   }

   public void clearAffectedBlockPositions() {
      this.affectedBlockPositions.clear();
   }

   public List getAffectedBlockPositions() {
      return this.affectedBlockPositions;
   }
}
