package net.minecraft.entity.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityExpBottle extends EntityThrowable {
   public EntityExpBottle(World var1) {
      super(worldIn);
   }

   public EntityExpBottle(World var1, EntityLivingBase var2) {
      super(worldIn, throwerIn);
   }

   public EntityExpBottle(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesExpBottle(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(fixer, "ThrowableExpBottle");
   }

   protected float getGravityVelocity() {
      return 0.07F;
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         this.world.playEvent(2002, new BlockPos(this), 0);
         int i = 3 + this.world.rand.nextInt(5) + this.world.rand.nextInt(5);

         while(i > 0) {
            int j = EntityXPOrb.getXPSplit(i);
            i -= j;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
         }

         this.setDead();
      }

   }
}
