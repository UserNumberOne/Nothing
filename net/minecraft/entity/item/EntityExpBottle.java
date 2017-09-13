package net.minecraft.entity.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityExpBottle extends EntityThrowable {
   public EntityExpBottle(World var1) {
      super(var1);
   }

   public EntityExpBottle(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   public EntityExpBottle(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public static void registerFixesExpBottle(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(var0, "ThrowableExpBottle");
   }

   protected float getGravityVelocity() {
      return 0.07F;
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         this.world.playEvent(2002, new BlockPos(this), 0);
         int var2 = 3 + this.world.rand.nextInt(5) + this.world.rand.nextInt(5);

         while(var2 > 0) {
            int var3 = EntityXPOrb.getXPSplit(var2);
            var2 -= var3;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, var3));
         }

         this.setDead();
      }

   }
}
