package net.minecraft.entity.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.entity.ExpBottleEvent;

public class EntityExpBottle extends EntityThrowable {
   public EntityExpBottle(World world) {
      super(world);
   }

   public EntityExpBottle(World world, EntityLivingBase entityliving) {
      super(world, entityliving);
   }

   public EntityExpBottle(World world, double d0, double d1, double d2) {
      super(world, d0, d1, d2);
   }

   public static void registerFixesExpBottle(DataFixer dataconvertermanager) {
      EntityThrowable.registerFixesThrowable(dataconvertermanager, "ThrowableExpBottle");
   }

   protected float getGravityVelocity() {
      return 0.07F;
   }

   protected void onImpact(RayTraceResult movingobjectposition) {
      if (!this.world.isRemote) {
         int i = 3 + this.world.rand.nextInt(5) + this.world.rand.nextInt(5);
         ExpBottleEvent event = CraftEventFactory.callExpBottleEvent(this, i);
         i = event.getExperience();
         if (event.getShowEffect()) {
            this.world.playEvent(2002, new BlockPos(this), 0);
         }

         while(i > 0) {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
         }

         this.setDead();
      }

   }
}
