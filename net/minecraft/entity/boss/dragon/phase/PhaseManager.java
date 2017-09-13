package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EntityDragon dragon;
   private final IPhase[] phases = new IPhase[PhaseList.getTotalPhases()];
   private IPhase phase;

   public PhaseManager(EntityDragon var1) {
      this.dragon = var1;
      this.setPhase(PhaseList.HOVER);
   }

   public void setPhase(PhaseList var1) {
      if (this.phase == null || var1 != this.phase.getPhaseList()) {
         if (this.phase != null) {
            this.phase.removeAreaEffect();
         }

         this.phase = this.getPhase(var1);
         if (!this.dragon.world.isRemote) {
            this.dragon.getDataManager().set(EntityDragon.PHASE, Integer.valueOf(var1.getId()));
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", new Object[]{var1, this.dragon.world.isRemote ? "client" : "server"});
         this.phase.initPhase();
      }

   }

   public IPhase getCurrentPhase() {
      return this.phase;
   }

   public IPhase getPhase(PhaseList var1) {
      int var2 = var1.getId();
      if (this.phases[var2] == null) {
         this.phases[var2] = var1.createPhase(this.dragon);
      }

      return this.phases[var2];
   }
}
