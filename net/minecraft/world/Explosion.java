package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Explosion {
   private final boolean isFlaming;
   private final boolean isSmoking;
   private final Random explosionRNG;
   private final World world;
   private final double explosionX;
   private final double explosionY;
   private final double explosionZ;
   private final Entity exploder;
   private final float explosionSize;
   private final List affectedBlockPositions;
   private final Map playerKnockbackMap;
   private final Vec3d position;

   @SideOnly(Side.CLIENT)
   public Explosion(World var1, Entity var2, double var3, double var5, double var7, float var9, List var10) {
      this(var1, var2, var3, var5, var7, var9, false, true, var10);
   }

   @SideOnly(Side.CLIENT)
   public Explosion(World var1, Entity var2, double var3, double var5, double var7, float var9, boolean var10, boolean var11, List var12) {
      this(var1, var2, var3, var5, var7, var9, var10, var11);
      this.affectedBlockPositions.addAll(var12);
   }

   public Explosion(World var1, Entity var2, double var3, double var5, double var7, float var9, boolean var10, boolean var11) {
      this.explosionRNG = new Random();
      this.affectedBlockPositions = Lists.newArrayList();
      this.playerKnockbackMap = Maps.newHashMap();
      this.world = var1;
      this.exploder = var2;
      this.explosionSize = var9;
      this.explosionX = var3;
      this.explosionY = var5;
      this.explosionZ = var7;
      this.isFlaming = var10;
      this.isSmoking = var11;
      this.position = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);
   }

   public void doExplosionA() {
      HashSet var1 = Sets.newHashSet();
      boolean var2 = true;

      for(int var3 = 0; var3 < 16; ++var3) {
         for(int var4 = 0; var4 < 16; ++var4) {
            for(int var5 = 0; var5 < 16; ++var5) {
               if (var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15 || var5 == 0 || var5 == 15) {
                  double var6 = (double)((float)var3 / 15.0F * 2.0F - 1.0F);
                  double var8 = (double)((float)var4 / 15.0F * 2.0F - 1.0F);
                  double var10 = (double)((float)var5 / 15.0F * 2.0F - 1.0F);
                  double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
                  var6 = var6 / var12;
                  var8 = var8 / var12;
                  var10 = var10 / var12;
                  float var14 = this.explosionSize * (0.7F + this.world.rand.nextFloat() * 0.6F);
                  double var15 = this.explosionX;
                  double var17 = this.explosionY;
                  double var19 = this.explosionZ;

                  for(float var21 = 0.3F; var14 > 0.0F; var14 -= 0.22500001F) {
                     BlockPos var22 = new BlockPos(var15, var17, var19);
                     IBlockState var23 = this.world.getBlockState(var22);
                     if (var23.getMaterial() != Material.AIR) {
                        float var24 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, var22, var23) : var23.getBlock().getExplosionResistance(this.world, var22, (Entity)null, this);
                        var14 -= (var24 + 0.3F) * 0.3F;
                     }

                     if (var14 > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.world, var22, var23, var14))) {
                        var1.add(var22);
                     }

                     var15 += var6 * 0.30000001192092896D;
                     var17 += var8 * 0.30000001192092896D;
                     var19 += var10 * 0.30000001192092896D;
                  }
               }
            }
         }
      }

      this.affectedBlockPositions.addAll(var1);
      float var31 = this.explosionSize * 2.0F;
      int var32 = MathHelper.floor(this.explosionX - (double)var31 - 1.0D);
      int var33 = MathHelper.floor(this.explosionX + (double)var31 + 1.0D);
      int var35 = MathHelper.floor(this.explosionY - (double)var31 - 1.0D);
      int var7 = MathHelper.floor(this.explosionY + (double)var31 + 1.0D);
      int var37 = MathHelper.floor(this.explosionZ - (double)var31 - 1.0D);
      int var9 = MathHelper.floor(this.explosionZ + (double)var31 + 1.0D);
      List var39 = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)var32, (double)var35, (double)var37, (double)var33, (double)var7, (double)var9));
      ForgeEventFactory.onExplosionDetonate(this.world, this, var39, (double)var31);
      Vec3d var11 = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

      for(int var40 = 0; var40 < var39.size(); ++var40) {
         Entity var13 = (Entity)var39.get(var40);
         if (!var13.isImmuneToExplosions()) {
            double var41 = var13.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)var31;
            if (var41 <= 1.0D) {
               double var16 = var13.posX - this.explosionX;
               double var18 = var13.posY + (double)var13.getEyeHeight() - this.explosionY;
               double var20 = var13.posZ - this.explosionZ;
               double var45 = (double)MathHelper.sqrt(var16 * var16 + var18 * var18 + var20 * var20);
               if (var45 != 0.0D) {
                  var16 = var16 / var45;
                  var18 = var18 / var45;
                  var20 = var20 / var45;
                  double var46 = (double)this.world.getBlockDensity(var11, var13.getEntityBoundingBox());
                  double var26 = (1.0D - var41) * var46;
                  var13.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((var26 * var26 + var26) / 2.0D * 7.0D * (double)var31 + 1.0D)));
                  double var28 = 1.0D;
                  if (var13 instanceof EntityLivingBase) {
                     var28 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)var13, var26);
                  }

                  var13.motionX += var16 * var28;
                  var13.motionY += var18 * var28;
                  var13.motionZ += var20 * var28;
                  if (var13 instanceof EntityPlayer) {
                     EntityPlayer var30 = (EntityPlayer)var13;
                     if (!var30.isSpectator() && (!var30.isCreative() || !var30.capabilities.isFlying)) {
                        this.playerKnockbackMap.put(var30, new Vec3d(var16 * var26, var18 * var26, var20 * var26));
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
         for(BlockPos var3 : this.affectedBlockPositions) {
            IBlockState var4 = this.world.getBlockState(var3);
            Block var5 = var4.getBlock();
            if (var1) {
               double var6 = (double)((float)var3.getX() + this.world.rand.nextFloat());
               double var8 = (double)((float)var3.getY() + this.world.rand.nextFloat());
               double var10 = (double)((float)var3.getZ() + this.world.rand.nextFloat());
               double var12 = var6 - this.explosionX;
               double var14 = var8 - this.explosionY;
               double var16 = var10 - this.explosionZ;
               double var18 = (double)MathHelper.sqrt(var12 * var12 + var14 * var14 + var16 * var16);
               var12 = var12 / var18;
               var14 = var14 / var18;
               var16 = var16 / var18;
               double var20 = 0.5D / (var18 / (double)this.explosionSize + 0.1D);
               var20 = var20 * (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
               var12 = var12 * var20;
               var14 = var14 * var20;
               var16 = var16 * var20;
               this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (var6 + this.explosionX) / 2.0D, (var8 + this.explosionY) / 2.0D, (var10 + this.explosionZ) / 2.0D, var12, var14, var16);
               this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var6, var8, var10, var12, var14, var16);
            }

            if (var4.getMaterial() != Material.AIR) {
               if (var5.canDropFromExplosion(this)) {
                  var5.dropBlockAsItemWithChance(this.world, var3, this.world.getBlockState(var3), 1.0F / this.explosionSize, 0);
               }

               var5.onBlockExploded(this.world, var3, this);
            }
         }
      }

      if (this.isFlaming) {
         for(BlockPos var23 : this.affectedBlockPositions) {
            if (this.world.getBlockState(var23).getMaterial() == Material.AIR && this.world.getBlockState(var23.down()).isFullBlock() && this.explosionRNG.nextInt(3) == 0) {
               this.world.setBlockState(var23, Blocks.FIRE.getDefaultState());
            }
         }
      }

   }

   public Map getPlayerKnockbackMap() {
      return this.playerKnockbackMap;
   }

   public EntityLivingBase getExplosivePlacedBy() {
      return this.exploder == null ? null : (this.exploder instanceof EntityTNTPrimed ? ((EntityTNTPrimed)this.exploder).getTntPlacedBy() : (this.exploder instanceof EntityLivingBase ? (EntityLivingBase)this.exploder : null));
   }

   public void clearAffectedBlockPositions() {
      this.affectedBlockPositions.clear();
   }

   public List getAffectedBlockPositions() {
      return this.affectedBlockPositions;
   }

   public Vec3d getPosition() {
      return this.position;
   }
}
