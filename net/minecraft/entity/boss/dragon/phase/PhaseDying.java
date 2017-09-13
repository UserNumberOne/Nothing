package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseDying extends PhaseBase {
   private Vec3d targetLocation;
   private int time;

   public PhaseDying(EntityDragon var1) {
      super(var1);
   }

   public void doClientRenderEffects() {
      if (this.time++ % 10 == 0) {
         float var1 = (this.dragon.getRNG().nextFloat() - 0.5F) * 8.0F;
         float var2 = (this.dragon.getRNG().nextFloat() - 0.5F) * 4.0F;
         float var3 = (this.dragon.getRNG().nextFloat() - 0.5F) * 8.0F;
         this.dragon.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.dragon.posX + (double)var1, this.dragon.posY + 2.0D + (double)var2, this.dragon.posZ + (double)var3, 0.0D, 0.0D, 0.0D);
      }

   }

   public void doLocalUpdate() {
      ++this.time;
      if (this.targetLocation == null) {
         BlockPos var1 = this.dragon.world.getHeight(WorldGenEndPodium.END_PODIUM_LOCATION);
         this.targetLocation = new Vec3d((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
      }

      double var2 = this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ);
      if (var2 >= 100.0D && var2 <= 22500.0D && !this.dragon.isCollidedHorizontally && !this.dragon.isCollidedVertically) {
         this.dragon.setHealth(1.0F);
      } else {
         this.dragon.setHealth(0.0F);
      }

   }

   public void initPhase() {
      this.targetLocation = null;
      this.time = 0;
   }

   public float getMaxRiseOrFall() {
      return 3.0F;
   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   public PhaseList getPhaseList() {
      return PhaseList.DYING;
   }
}
