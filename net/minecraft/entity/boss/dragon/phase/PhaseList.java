package net.minecraft.entity.boss.dragon.phase;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import net.minecraft.entity.boss.EntityDragon;

public class PhaseList {
   private static PhaseList[] phases = new PhaseList[0];
   public static final PhaseList HOLDING_PATTERN = create(PhaseHoldingPattern.class, "HoldingPattern");
   public static final PhaseList STRAFE_PLAYER = create(PhaseStrafePlayer.class, "StrafePlayer");
   public static final PhaseList LANDING_APPROACH = create(PhaseLandingApproach.class, "LandingApproach");
   public static final PhaseList LANDING = create(PhaseLanding.class, "Landing");
   public static final PhaseList TAKEOFF = create(PhaseTakeoff.class, "Takeoff");
   public static final PhaseList SITTING_FLAMING = create(PhaseSittingFlaming.class, "SittingFlaming");
   public static final PhaseList SITTING_SCANNING = create(PhaseSittingScanning.class, "SittingScanning");
   public static final PhaseList SITTING_ATTACKING = create(PhaseSittingAttacking.class, "SittingAttacking");
   public static final PhaseList CHARGING_PLAYER = create(PhaseChargingPlayer.class, "ChargingPlayer");
   public static final PhaseList DYING = create(PhaseDying.class, "Dying");
   public static final PhaseList HOVER = create(PhaseHover.class, "Hover");
   private final Class clazz;
   private final int id;
   private final String name;

   private PhaseList(int var1, Class var2, String var3) {
      this.id = var1;
      this.clazz = var2;
      this.name = var3;
   }

   public IPhase createPhase(EntityDragon var1) {
      try {
         Constructor var2 = this.getConstructor();
         return (IPhase)var2.newInstance(var1);
      } catch (Exception var3) {
         throw new Error(var3);
      }
   }

   protected Constructor getConstructor() throws NoSuchMethodException {
      return this.clazz.getConstructor(EntityDragon.class);
   }

   public int getId() {
      return this.id;
   }

   public String toString() {
      return this.name + " (#" + this.id + ")";
   }

   public static PhaseList getById(int var0) {
      return var0 >= 0 && var0 < phases.length ? phases[var0] : HOLDING_PATTERN;
   }

   public static int getTotalPhases() {
      return phases.length;
   }

   private static PhaseList create(Class var0, String var1) {
      PhaseList var2 = new PhaseList(phases.length, var0, var1);
      phases = (PhaseList[])Arrays.copyOf(phases, phases.length + 1);
      phases[var2.getId()] = var2;
      return var2;
   }
}
