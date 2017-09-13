package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

   public Explosion(World world, Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
      this.world = world;
      this.exploder = entity;
      this.explosionSize = (float)Math.max((double)f, 0.0D);
      this.explosionX = d0;
      this.explosionY = d1;
      this.explosionZ = d2;
      this.isFlaming = flag;
      this.isSmoking = flag1;
   }

   public void doExplosionA() {
      if (this.explosionSize >= 0.1F) {
         HashSet hashset = Sets.newHashSet();

         for(int k = 0; k < 16; ++k) {
            for(int i = 0; i < 16; ++i) {
               for(int j = 0; j < 16; ++j) {
                  if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                     double d0 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                     double d1 = (double)((float)i / 15.0F * 2.0F - 1.0F);
                     double d2 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                     double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                     d0 = d0 / d3;
                     d1 = d1 / d3;
                     d2 = d2 / d3;
                     float f = this.explosionSize * (0.7F + this.world.rand.nextFloat() * 0.6F);
                     double d4 = this.explosionX;
                     double d5 = this.explosionY;

                     for(double d6 = this.explosionZ; f > 0.0F; f -= 0.22500001F) {
                        BlockPos blockposition = new BlockPos(d4, d5, d6);
                        IBlockState iblockdata = this.world.getBlockState(blockposition);
                        if (iblockdata.getMaterial() != Material.AIR) {
                           float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, blockposition, iblockdata) : iblockdata.getBlock().getExplosionResistance((Entity)null);
                           f -= (f2 + 0.3F) * 0.3F;
                        }

                        if (f > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.world, blockposition, iblockdata, f)) && blockposition.getY() < 256 && blockposition.getY() >= 0) {
                           hashset.add(blockposition);
                        }

                        d4 += d0 * 0.30000001192092896D;
                        d5 += d1 * 0.30000001192092896D;
                        d6 += d2 * 0.30000001192092896D;
                     }
                  }
               }
            }
         }

         this.affectedBlockPositions.addAll(hashset);
         float f3 = this.explosionSize * 2.0F;
         int i = MathHelper.floor(this.explosionX - (double)f3 - 1.0D);
         int j = MathHelper.floor(this.explosionX + (double)f3 + 1.0D);
         int l = MathHelper.floor(this.explosionY - (double)f3 - 1.0D);
         int i1 = MathHelper.floor(this.explosionY + (double)f3 + 1.0D);
         int j1 = MathHelper.floor(this.explosionZ - (double)f3 - 1.0D);
         int k1 = MathHelper.floor(this.explosionZ + (double)f3 + 1.0D);
         List list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)i, (double)l, (double)j1, (double)j, (double)i1, (double)k1));
         Vec3d vec3d = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

         for(int l1 = 0; l1 < list.size(); ++l1) {
            Entity entity = (Entity)list.get(l1);
            if (!entity.isImmuneToExplosions()) {
               double d7 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)f3;
               if (d7 <= 1.0D) {
                  double d8 = entity.posX - this.explosionX;
                  double d9 = entity.posY + (double)entity.getEyeHeight() - this.explosionY;
                  double d10 = entity.posZ - this.explosionZ;
                  double d11 = (double)MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);
                  if (d11 != 0.0D) {
                     d8 = d8 / d11;
                     d9 = d9 / d11;
                     d10 = d10 / d11;
                     double d12 = (double)this.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
                     double d13 = (1.0D - d7) * d12;
                     CraftEventFactory.entityDamage = this.exploder;
                     entity.forceExplosionKnockback = false;
                     boolean wasDamaged = entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d13 * d13 + d13) / 2.0D * 7.0D * (double)f3 + 1.0D)));
                     CraftEventFactory.entityDamage = null;
                     if (wasDamaged || entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock || entity.forceExplosionKnockback) {
                        double d14 = 1.0D;
                        if (entity instanceof EntityLivingBase) {
                           d14 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d13);
                        }

                        entity.motionX += d8 * d14;
                        entity.motionY += d9 * d14;
                        entity.motionZ += d10 * d14;
                        if (entity instanceof EntityPlayer) {
                           EntityPlayer entityhuman = (EntityPlayer)entity;
                           if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.capabilities.isFlying)) {
                              this.playerKnockbackMap.put(entityhuman, new Vec3d(d8 * d13, d9 * d13, d10 * d13));
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public void doExplosionB(boolean flag) {
      this.world.playSound((EntityPlayer)null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
      if (this.explosionSize >= 2.0F && this.isSmoking) {
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
      } else {
         this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
      }

      if (this.isSmoking) {
         org.bukkit.World bworld = this.world.getWorld();
         org.bukkit.entity.Entity explode = this.exploder == null ? null : this.exploder.getBukkitEntity();
         Location location = new Location(bworld, this.explosionX, this.explosionY, this.explosionZ);
         List blockList = Lists.newArrayList();

         for(int i1 = this.affectedBlockPositions.size() - 1; i1 >= 0; --i1) {
            BlockPos cpos = (BlockPos)this.affectedBlockPositions.get(i1);
            Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
            if (bblock.getType() != org.bukkit.Material.AIR) {
               blockList.add(bblock);
            }
         }

         boolean cancelled;
         List bukkitBlocks;
         float yield;
         if (explode != null) {
            EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, 1.0F / this.explosionSize);
            this.world.getServer().getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            bukkitBlocks = event.blockList();
            yield = event.getYield();
         } else {
            BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1.0F / this.explosionSize);
            this.world.getServer().getPluginManager().callEvent(event);
            cancelled = event.isCancelled();
            bukkitBlocks = event.blockList();
            yield = event.getYield();
         }

         this.affectedBlockPositions.clear();

         for(Block bblock : bukkitBlocks) {
            BlockPos coords = new BlockPos(bblock.getX(), bblock.getY(), bblock.getZ());
            this.affectedBlockPositions.add(coords);
         }

         if (cancelled) {
            this.wasCanceled = true;
            return;
         }

         for(BlockPos blockposition : this.affectedBlockPositions) {
            IBlockState iblockdata = this.world.getBlockState(blockposition);
            net.minecraft.block.Block block = iblockdata.getBlock();
            if (flag) {
               double d0 = (double)((float)blockposition.getX() + this.world.rand.nextFloat());
               double d1 = (double)((float)blockposition.getY() + this.world.rand.nextFloat());
               double d2 = (double)((float)blockposition.getZ() + this.world.rand.nextFloat());
               double d3 = d0 - this.explosionX;
               double d4 = d1 - this.explosionY;
               double d5 = d2 - this.explosionZ;
               double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
               d3 = d3 / d6;
               d4 = d4 / d6;
               d5 = d5 / d6;
               double d7 = 0.5D / (d6 / (double)this.explosionSize + 0.1D);
               d7 = d7 * (double)(this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
               d3 = d3 * d7;
               d4 = d4 * d7;
               d5 = d5 * d7;
               this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.explosionX) / 2.0D, (d1 + this.explosionY) / 2.0D, (d2 + this.explosionZ) / 2.0D, d3, d4, d5);
               this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
            }

            if (iblockdata.getMaterial() != Material.AIR) {
               if (block.canDropFromExplosion(this)) {
                  block.dropBlockAsItemWithChance(this.world, blockposition, this.world.getBlockState(blockposition), yield, 0);
               }

               this.world.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 3);
               block.onBlockDestroyedByExplosion(this.world, blockposition, this);
            }
         }
      }

      if (this.isFlaming) {
         for(BlockPos blockposition : this.affectedBlockPositions) {
            if (this.world.getBlockState(blockposition).getMaterial() == Material.AIR && this.world.getBlockState(blockposition.down()).isFullBlock() && this.explosionRNG.nextInt(3) == 0 && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
               this.world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
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
