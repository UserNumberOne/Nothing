package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
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
      this.world = worldIn;
      this.renderer = rendererIn;

      for(int i = 0; i < 4; ++i) {
         this.fxLayers[i] = new ArrayDeque[2];

         for(int j = 0; j < 2; ++j) {
            this.fxLayers[i][j] = Queues.newArrayDeque();
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
      this.particleTypes.put(Integer.valueOf(id), particleFactory);
   }

   public void emitParticleAtEntity(Entity var1, EnumParticleTypes var2) {
      this.particleEmitters.add(new ParticleEmitter(this.world, entityIn, particleTypes));
   }

   @Nullable
   public Particle spawnEffectParticle(int var1, double var2, double var4, double var6, double var8, double var10, double var12, int... var14) {
      IParticleFactory iparticlefactory = (IParticleFactory)this.particleTypes.get(Integer.valueOf(particleId));
      if (iparticlefactory != null) {
         Particle particle = iparticlefactory.createParticle(particleId, this.world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
         if (particle != null) {
            this.addEffect(particle);
            return particle;
         }
      }

      return null;
   }

   public void addEffect(Particle var1) {
      if (effect != null) {
         this.queueEntityFX.add(effect);
      }
   }

   public void updateEffects() {
      for(int i = 0; i < 4; ++i) {
         this.updateEffectLayer(i);
      }

      if (!this.particleEmitters.isEmpty()) {
         List list = Lists.newArrayList();

         for(ParticleEmitter particleemitter : this.particleEmitters) {
            particleemitter.onUpdate();
            if (!particleemitter.isAlive()) {
               list.add(particleemitter);
            }
         }

         this.particleEmitters.removeAll(list);
      }

      if (!this.queueEntityFX.isEmpty()) {
         for(Particle particle = (Particle)this.queueEntityFX.poll(); particle != null; particle = (Particle)this.queueEntityFX.poll()) {
            int j = particle.getFXLayer();
            int k = particle.isTransparent() ? 0 : 1;
            if (this.fxLayers[j][k].size() >= 16384) {
               this.fxLayers[j][k].removeFirst();
            }

            this.fxLayers[j][k].add(particle);
         }
      }

   }

   private void updateEffectLayer(int var1) {
      this.world.theProfiler.startSection(layer + "");

      for(int i = 0; i < 2; ++i) {
         this.world.theProfiler.startSection(i + "");
         this.tickParticleList(this.fxLayers[layer][i]);
         this.world.theProfiler.endSection();
      }

      this.world.theProfiler.endSection();
   }

   private void tickParticleList(Queue var1) {
      if (!p_187240_1_.isEmpty()) {
         Iterator iterator = p_187240_1_.iterator();

         while(iterator.hasNext()) {
            Particle particle = (Particle)iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               iterator.remove();
            }
         }
      }

   }

   private void tickParticle(final Particle var1) {
      try {
         particle.onUpdate();
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.makeCrashReport(var6, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
         final int i = particle.getFXLayer();
         crashreportcategory.setDetail("Particle", new ICrashReportDetail() {
            public String call() throws Exception {
               return particle.toString();
            }
         });
         crashreportcategory.setDetail("Particle Type", new ICrashReportDetail() {
            public String call() throws Exception {
               return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
            }
         });
         throw new ReportedException(crashreport);
      }
   }

   public void renderParticles(Entity var1, float var2) {
      float f = ActiveRenderInfo.getRotationX();
      float f1 = ActiveRenderInfo.getRotationZ();
      float f2 = ActiveRenderInfo.getRotationYZ();
      float f3 = ActiveRenderInfo.getRotationXY();
      float f4 = ActiveRenderInfo.getRotationXZ();
      Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
      Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
      Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
      Particle.cameraViewDir = entityIn.getLook(partialTicks);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      GlStateManager.alphaFunc(516, 0.003921569F);

      for(int i_nf = 0; i_nf < 3; ++i_nf) {
         final int i = i_nf;

         for(int j = 0; j < 2; ++j) {
            if (!this.fxLayers[i][j].isEmpty()) {
               switch(j) {
               case 0:
                  GlStateManager.depthMask(false);
                  break;
               case 1:
                  GlStateManager.depthMask(true);
               }

               switch(i) {
               case 0:
               default:
                  this.renderer.bindTexture(PARTICLE_TEXTURES);
                  break;
               case 1:
                  this.renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
               }

               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               Tessellator tessellator = Tessellator.getInstance();
               VertexBuffer vertexbuffer = tessellator.getBuffer();
               vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

               for(final Particle particle : this.fxLayers[i][j]) {
                  try {
                     particle.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
                  } catch (Throwable var18) {
                     CrashReport crashreport = CrashReport.makeCrashReport(var18, "Rendering Particle");
                     CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                     crashreportcategory.setDetail("Particle", new ICrashReportDetail() {
                        public String call() throws Exception {
                           return particle.toString();
                        }
                     });
                     crashreportcategory.setDetail("Particle Type", new ICrashReportDetail() {
                        public String call() throws Exception {
                           return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                        }
                     });
                     throw new ReportedException(crashreport);
                  }
               }

               tessellator.draw();
            }
         }
      }

      GlStateManager.depthMask(true);
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
   }

   public void renderLitParticles(Entity var1, float var2) {
      float f = 0.017453292F;
      float f1 = MathHelper.cos(entityIn.rotationYaw * 0.017453292F);
      float f2 = MathHelper.sin(entityIn.rotationYaw * 0.017453292F);
      float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
      float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
      float f5 = MathHelper.cos(entityIn.rotationPitch * 0.017453292F);

      for(int i = 0; i < 2; ++i) {
         Queue queue = this.fxLayers[3][i];
         if (!queue.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();

            for(Particle particle : queue) {
               particle.renderParticle(vertexbuffer, entityIn, partialTick, f1, f5, f2, f3, f4);
            }
         }
      }

   }

   public void clearEffects(@Nullable World var1) {
      this.world = worldIn;

      for(int i = 0; i < 4; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.fxLayers[i][j].clear();
         }
      }

      this.particleEmitters.clear();
   }

   public void addBlockDestroyEffects(BlockPos var1, IBlockState var2) {
      if (!state.getBlock().isAir(state, this.world, pos) && !state.getBlock().addDestroyEffects(this.world, pos, this)) {
         state = state.getActualState(this.world, pos);
         int i = 4;

         for(int j = 0; j < 4; ++j) {
            for(int k = 0; k < 4; ++k) {
               for(int l = 0; l < 4; ++l) {
                  double d0 = (double)pos.getX() + ((double)j + 0.5D) / 4.0D;
                  double d1 = (double)pos.getY() + ((double)k + 0.5D) / 4.0D;
                  double d2 = (double)pos.getZ() + ((double)l + 0.5D) / 4.0D;
                  this.addEffect((new ParticleDigging(this.world, d0, d1, d2, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, state)).setBlockPos(pos));
               }
            }
         }
      }

   }

   public void addBlockHitEffects(BlockPos var1, EnumFacing var2) {
      IBlockState iblockstate = this.world.getBlockState(pos);
      if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(this.world, pos);
         double d0 = (double)i + this.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
         double d1 = (double)j + this.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
         double d2 = (double)k + this.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;
         if (side == EnumFacing.DOWN) {
            d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
         }

         if (side == EnumFacing.UP) {
            d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
         }

         if (side == EnumFacing.NORTH) {
            d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
         }

         if (side == EnumFacing.SOUTH) {
            d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
         }

         if (side == EnumFacing.WEST) {
            d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
         }

         if (side == EnumFacing.EAST) {
            d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
         }

         this.addEffect((new ParticleDigging(this.world, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate)).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
      }

   }

   public String getStatistics() {
      int i = 0;

      for(int j = 0; j < 4; ++j) {
         for(int k = 0; k < 2; ++k) {
            i += this.fxLayers[j][k].size();
         }
      }

      return "" + i;
   }

   public void addBlockHitEffects(BlockPos var1, RayTraceResult var2) {
      IBlockState state = this.world.getBlockState(pos);
      if (state != null && !state.getBlock().addHitEffects(state, this.world, target, this)) {
         this.addBlockHitEffects(pos, target.sideHit);
      }

   }
}
