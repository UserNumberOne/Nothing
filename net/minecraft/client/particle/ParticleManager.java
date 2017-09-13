package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleManager {
   private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
   protected World world;
   private final ArrayDeque[][] fxLayers = new ArrayDeque[4][];
   private final Queue particleEmitters = Queues.newArrayDeque();
   private final TextureManager renderer;
   private final Random rand = new Random();
   private final Map particleTypes = Maps.newHashMap();
   private final Queue queueEntityFX = Queues.newArrayDeque();

   public ParticleManager(World var1, TextureManager var2) {
      this.world = var1;
      this.renderer = var2;

      for(int var3 = 0; var3 < 4; ++var3) {
         this.fxLayers[var3] = new ArrayDeque[2];

         for(int var4 = 0; var4 < 2; ++var4) {
            this.fxLayers[var3][var4] = Queues.newArrayDeque();
         }
      }

      this.registerVanillaParticles();
   }

   private void registerVanillaParticles() {
      this.registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new ParticleExplosion.Factory());
      this.registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new ParticleBubble.Factory());
      this.registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new ParticleSplash.Factory());
      this.registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new ParticleWaterWake.Factory());
      this.registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new ParticleRain.Factory());
      this.registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new ParticleSuspend.Factory());
      this.registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new ParticleSuspendedTown.Factory());
      this.registerParticle(EnumParticleTypes.CRIT.getParticleID(), new ParticleCrit.Factory());
      this.registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new ParticleCrit.MagicFactory());
      this.registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new ParticleSmokeNormal.Factory());
      this.registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new ParticleSmokeLarge.Factory());
      this.registerParticle(EnumParticleTypes.SPELL.getParticleID(), new ParticleSpell.Factory());
      this.registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new ParticleSpell.InstantFactory());
      this.registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new ParticleSpell.MobFactory());
      this.registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new ParticleSpell.AmbientMobFactory());
      this.registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new ParticleSpell.WitchFactory());
      this.registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new ParticleDrip.WaterFactory());
      this.registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new ParticleDrip.LavaFactory());
      this.registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new ParticleHeart.AngryVillagerFactory());
      this.registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new ParticleSuspendedTown.HappyVillagerFactory());
      this.registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new ParticleSuspendedTown.Factory());
      this.registerParticle(EnumParticleTypes.NOTE.getParticleID(), new ParticleNote.Factory());
      this.registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new ParticlePortal.Factory());
      this.registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new ParticleEnchantmentTable.EnchantmentTable());
      this.registerParticle(EnumParticleTypes.FLAME.getParticleID(), new ParticleFlame.Factory());
      this.registerParticle(EnumParticleTypes.LAVA.getParticleID(), new ParticleLava.Factory());
      this.registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new ParticleFootStep.Factory());
      this.registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new ParticleCloud.Factory());
      this.registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new ParticleRedstone.Factory());
      this.registerParticle(EnumParticleTypes.FALLING_DUST.getParticleID(), new ParticleFallingDust.Factory());
      this.registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new ParticleBreaking.SnowballFactory());
      this.registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new ParticleSnowShovel.Factory());
      this.registerParticle(EnumParticleTypes.SLIME.getParticleID(), new ParticleBreaking.SlimeFactory());
      this.registerParticle(EnumParticleTypes.HEART.getParticleID(), new ParticleHeart.Factory());
      this.registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
      this.registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new ParticleBreaking.Factory());
      this.registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new ParticleDigging.Factory());
      this.registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new ParticleBlockDust.Factory());
      this.registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new ParticleExplosionHuge.Factory());
      this.registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new ParticleExplosionLarge.Factory());
      this.registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new ParticleFirework.Factory());
      this.registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new ParticleMobAppearance.Factory());
      this.registerParticle(EnumParticleTypes.DRAGON_BREATH.getParticleID(), new ParticleDragonBreath.Factory());
      this.registerParticle(EnumParticleTypes.END_ROD.getParticleID(), new ParticleEndRod.Factory());
      this.registerParticle(EnumParticleTypes.DAMAGE_INDICATOR.getParticleID(), new ParticleCrit.DamageIndicatorFactory());
      this.registerParticle(EnumParticleTypes.SWEEP_ATTACK.getParticleID(), new ParticleSweepAttack.Factory());
   }

   public void registerParticle(int var1, IParticleFactory var2) {
      this.particleTypes.put(Integer.valueOf(var1), var2);
   }

   public void emitParticleAtEntity(Entity var1, EnumParticleTypes var2) {
      this.particleEmitters.add(new ParticleEmitter(this.world, var1, var2));
   }

   @Nullable
   public Particle spawnEffectParticle(int var1, double var2, double var4, double var6, double var8, double var10, double var12, int... var14) {
      IParticleFactory var15 = (IParticleFactory)this.particleTypes.get(Integer.valueOf(var1));
      if (var15 != null) {
         Particle var16 = var15.createParticle(var1, this.world, var2, var4, var6, var8, var10, var12, var14);
         if (var16 != null) {
            this.addEffect(var16);
            return var16;
         }
      }

      return null;
   }

   public void addEffect(Particle var1) {
      if (var1 != null) {
         this.queueEntityFX.add(var1);
      }
   }

   public void updateEffects() {
      for(int var1 = 0; var1 < 4; ++var1) {
         this.updateEffectLayer(var1);
      }

      if (!this.particleEmitters.isEmpty()) {
         ArrayList var4 = Lists.newArrayList();

         for(ParticleEmitter var3 : this.particleEmitters) {
            var3.onUpdate();
            if (!var3.isAlive()) {
               var4.add(var3);
            }
         }

         this.particleEmitters.removeAll(var4);
      }

      if (!this.queueEntityFX.isEmpty()) {
         for(Particle var5 = (Particle)this.queueEntityFX.poll(); var5 != null; var5 = (Particle)this.queueEntityFX.poll()) {
            int var6 = var5.getFXLayer();
            int var7 = var5.isTransparent() ? 0 : 1;
            if (this.fxLayers[var6][var7].size() >= 16384) {
               this.fxLayers[var6][var7].removeFirst();
            }

            this.fxLayers[var6][var7].add(var5);
         }
      }

   }

   private void updateEffectLayer(int var1) {
      this.world.theProfiler.startSection(var1 + "");

      for(int var2 = 0; var2 < 2; ++var2) {
         this.world.theProfiler.startSection(var2 + "");
         this.tickParticleList(this.fxLayers[var1][var2]);
         this.world.theProfiler.endSection();
      }

      this.world.theProfiler.endSection();
   }

   private void tickParticleList(Queue var1) {
      if (!var1.isEmpty()) {
         Iterator var2 = var1.iterator();

         while(var2.hasNext()) {
            Particle var3 = (Particle)var2.next();
            this.tickParticle(var3);
            if (!var3.isAlive()) {
               var2.remove();
            }
         }
      }

   }

   private void tickParticle(final Particle var1) {
      try {
         var1.onUpdate();
      } catch (Throwable var6) {
         CrashReport var3 = CrashReport.makeCrashReport(var6, "Ticking Particle");
         CrashReportCategory var4 = var3.makeCategory("Particle being ticked");
         final int var5 = var1.getFXLayer();
         var4.setDetail("Particle", new ICrashReportDetail() {
            public String call() throws Exception {
               return var1.toString();
            }
         });
         var4.setDetail("Particle Type", new ICrashReportDetail() {
            public String call() throws Exception {
               return var5 == 0 ? "MISC_TEXTURE" : (var5 == 1 ? "TERRAIN_TEXTURE" : (var5 == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + var5));
            }
         });
         throw new ReportedException(var3);
      }
   }

   public void renderParticles(Entity var1, float var2) {
      float var3 = ActiveRenderInfo.getRotationX();
      float var4 = ActiveRenderInfo.getRotationZ();
      float var5 = ActiveRenderInfo.getRotationYZ();
      float var6 = ActiveRenderInfo.getRotationXY();
      float var7 = ActiveRenderInfo.getRotationXZ();
      Particle.interpPosX = var1.lastTickPosX + (var1.posX - var1.lastTickPosX) * (double)var2;
      Particle.interpPosY = var1.lastTickPosY + (var1.posY - var1.lastTickPosY) * (double)var2;
      Particle.interpPosZ = var1.lastTickPosZ + (var1.posZ - var1.lastTickPosZ) * (double)var2;
      Particle.cameraViewDir = var1.getLook(var2);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      GlStateManager.alphaFunc(516, 0.003921569F);

      for(int var8 = 0; var8 < 3; ++var8) {
         final int var9 = var8;

         for(int var10 = 0; var10 < 2; ++var10) {
            if (!this.fxLayers[var9][var10].isEmpty()) {
               switch(var10) {
               case 0:
                  GlStateManager.depthMask(false);
                  break;
               case 1:
                  GlStateManager.depthMask(true);
               }

               switch(var9) {
               case 0:
               default:
                  this.renderer.bindTexture(PARTICLE_TEXTURES);
                  break;
               case 1:
                  this.renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Tessellator var11 = Tessellator.getInstance();
               VertexBuffer var12 = var11.getBuffer();
               var12.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

               for(final Particle var14 : this.fxLayers[var9][var10]) {
                  try {
                     var14.renderParticle(var12, var1, var2, var3, var7, var4, var5, var6);
                  } catch (Throwable var18) {
                     CrashReport var16 = CrashReport.makeCrashReport(var18, "Rendering Particle");
                     CrashReportCategory var17 = var16.makeCategory("Particle being rendered");
                     var17.setDetail("Particle", new ICrashReportDetail() {
                        public String call() throws Exception {
                           return var14.toString();
                        }
                     });
                     var17.setDetail("Particle Type", new ICrashReportDetail() {
                        public String call() throws Exception {
                           return var9 == 0 ? "MISC_TEXTURE" : (var9 == 1 ? "TERRAIN_TEXTURE" : (var9 == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + var9));
                        }
                     });
                     throw new ReportedException(var16);
                  }
               }

               var11.draw();
            }
         }
      }

      GlStateManager.depthMask(true);
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
   }

   public void renderLitParticles(Entity var1, float var2) {
      float var3 = 0.017453292F;
      float var4 = MathHelper.cos(var1.rotationYaw * 0.017453292F);
      float var5 = MathHelper.sin(var1.rotationYaw * 0.017453292F);
      float var6 = -var5 * MathHelper.sin(var1.rotationPitch * 0.017453292F);
      float var7 = var4 * MathHelper.sin(var1.rotationPitch * 0.017453292F);
      float var8 = MathHelper.cos(var1.rotationPitch * 0.017453292F);

      for(int var9 = 0; var9 < 2; ++var9) {
         ArrayDeque var10 = this.fxLayers[3][var9];
         if (!var10.isEmpty()) {
            Tessellator var11 = Tessellator.getInstance();
            VertexBuffer var12 = var11.getBuffer();

            for(Particle var14 : var10) {
               var14.renderParticle(var12, var1, var2, var4, var8, var5, var6, var7);
            }
         }
      }

   }

   public void clearEffects(@Nullable World var1) {
      this.world = var1;

      for(int var2 = 0; var2 < 4; ++var2) {
         for(int var3 = 0; var3 < 2; ++var3) {
            this.fxLayers[var2][var3].clear();
         }
      }

      this.particleEmitters.clear();
   }

   public void addBlockDestroyEffects(BlockPos var1, IBlockState var2) {
      if (!var2.getBlock().isAir(var2, this.world, var1) && !var2.getBlock().addDestroyEffects(this.world, var1, this)) {
         var2 = var2.getActualState(this.world, var1);
         boolean var3 = true;

         for(int var4 = 0; var4 < 4; ++var4) {
            for(int var5 = 0; var5 < 4; ++var5) {
               for(int var6 = 0; var6 < 4; ++var6) {
                  double var7 = (double)var1.getX() + ((double)var4 + 0.5D) / 4.0D;
                  double var9 = (double)var1.getY() + ((double)var5 + 0.5D) / 4.0D;
                  double var11 = (double)var1.getZ() + ((double)var6 + 0.5D) / 4.0D;
                  this.addEffect((new ParticleDigging(this.world, var7, var9, var11, var7 - (double)var1.getX() - 0.5D, var9 - (double)var1.getY() - 0.5D, var11 - (double)var1.getZ() - 0.5D, var2)).setBlockPos(var1));
               }
            }
         }
      }

   }

   public void addBlockHitEffects(BlockPos var1, EnumFacing var2) {
      IBlockState var3 = this.world.getBlockState(var1);
      if (var3.getRenderType() != EnumBlockRenderType.INVISIBLE) {
         int var4 = var1.getX();
         int var5 = var1.getY();
         int var6 = var1.getZ();
         float var7 = 0.1F;
         AxisAlignedBB var8 = var3.getBoundingBox(this.world, var1);
         double var9 = (double)var4 + this.rand.nextDouble() * (var8.maxX - var8.minX - 0.20000000298023224D) + 0.10000000149011612D + var8.minX;
         double var11 = (double)var5 + this.rand.nextDouble() * (var8.maxY - var8.minY - 0.20000000298023224D) + 0.10000000149011612D + var8.minY;
         double var13 = (double)var6 + this.rand.nextDouble() * (var8.maxZ - var8.minZ - 0.20000000298023224D) + 0.10000000149011612D + var8.minZ;
         if (var2 == EnumFacing.DOWN) {
            var11 = (double)var5 + var8.minY - 0.10000000149011612D;
         }

         if (var2 == EnumFacing.UP) {
            var11 = (double)var5 + var8.maxY + 0.10000000149011612D;
         }

         if (var2 == EnumFacing.NORTH) {
            var13 = (double)var6 + var8.minZ - 0.10000000149011612D;
         }

         if (var2 == EnumFacing.SOUTH) {
            var13 = (double)var6 + var8.maxZ + 0.10000000149011612D;
         }

         if (var2 == EnumFacing.WEST) {
            var9 = (double)var4 + var8.minX - 0.10000000149011612D;
         }

         if (var2 == EnumFacing.EAST) {
            var9 = (double)var4 + var8.maxX + 0.10000000149011612D;
         }

         this.addEffect((new ParticleDigging(this.world, var9, var11, var13, 0.0D, 0.0D, 0.0D, var3)).setBlockPos(var1).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
      }

   }

   public String getStatistics() {
      int var1 = 0;

      for(int var2 = 0; var2 < 4; ++var2) {
         for(int var3 = 0; var3 < 2; ++var3) {
            var1 += this.fxLayers[var2][var3].size();
         }
      }

      return "" + var1;
   }

   public void addBlockHitEffects(BlockPos var1, RayTraceResult var2) {
      IBlockState var3 = this.world.getBlockState(var1);
      if (var3 != null && !var3.getBlock().addHitEffects(var3, this.world, var2, this)) {
         this.addBlockHitEffects(var1, var2.sideHit);
      }

   }
}
