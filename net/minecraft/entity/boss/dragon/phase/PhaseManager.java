package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEnderDragon;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;

public class PhaseManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EntityDragon dragon;
   private final IPhase[] phases = new IPhase[PhaseList.getTotalPhases()];
   private IPhase phase;

   public PhaseManager(EntityDragon entityenderdragon) {
      this.dragon = entityenderdragon;
      this.setPhase(PhaseList.HOVER);
   }

   public void setPhase(PhaseList dragoncontrollerphase) {
      if (this.phase == null || dragoncontrollerphase != this.phase.getPhaseList()) {
         if (this.phase != null) {
            this.phase.removeAreaEffect();
         }

         EnderDragonChangePhaseEvent event = new EnderDragonChangePhaseEvent((CraftEnderDragon)this.dragon.getBukkitEntity(), this.phase == null ? null : CraftEnderDragon.getBukkitPhase(this.phase.getPhaseList()), CraftEnderDragon.getBukkitPhase(dragoncontrollerphase));
         this.dragon.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }

         dragoncontrollerphase = CraftEnderDragon.getMinecraftPhase(event.getNewPhase());
         this.phase = this.getPhase(dragoncontrollerphase);
         if (!this.dragon.world.isRemote) {
            this.dragon.getDataManager().set(EntityDragon.PHASE, Integer.valueOf(dragoncontrollerphase.getId()));
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", new Object[]{dragoncontrollerphase, this.dragon.world.isRemote ? "client" : "server"});
         this.phase.initPhase();
      }

   }

   public IPhase getCurrentPhase() {
      return this.phase;
   }

   public IPhase getPhase(PhaseList dragoncontrollerphase) {
      int i = dragoncontrollerphase.getId();
      if (this.phases[i] == null) {
         this.phases[i] = dragoncontrollerphase.createPhase(this.dragon);
      }

      return this.phases[i];
   }
}
