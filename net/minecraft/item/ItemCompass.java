package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCompass extends Item {
   public ItemCompass() {
      this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         double rotation;
         @SideOnly(Side.CLIENT)
         double rota;
         @SideOnly(Side.CLIENT)
         long lastUpdateTick;

         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            if (entityIn == null && !stack.isOnItemFrame()) {
               return 0.0F;
            } else {
               boolean flag = entityIn != null;
               Entity entity = (Entity)(flag ? entityIn : stack.getItemFrame());
               if (worldIn == null) {
                  worldIn = entity.world;
               }

               double d0;
               if (worldIn.provider.isSurfaceWorld()) {
                  double d1 = flag ? (double)entity.rotationYaw : this.getFrameRotation((EntityItemFrame)entity);
                  d1 = d1 % 360.0D;
                  double d2 = this.getSpawnToAngle(worldIn, entity);
                  d0 = 3.141592653589793D - ((d1 - 90.0D) * 0.01745329238474369D - d2);
               } else {
                  d0 = Math.random() * 6.283185307179586D;
               }

               if (flag) {
                  d0 = this.wobble(worldIn, d0);
               }

               float f = (float)(d0 / 6.283185307179586D);
               return MathHelper.positiveModulo(f, 1.0F);
            }
         }

         @SideOnly(Side.CLIENT)
         private double wobble(World var1, double var2) {
            if (worldIn.getTotalWorldTime() != this.lastUpdateTick) {
               this.lastUpdateTick = worldIn.getTotalWorldTime();
               double d0 = p_185093_2_ - this.rotation;
               d0 = d0 % 6.283185307179586D;
               d0 = MathHelper.clamp(d0, -1.0D, 1.0D);
               this.rota += d0 * 0.1D;
               this.rota *= 0.8D;
               this.rotation += this.rota;
            }

            return this.rotation;
         }

         @SideOnly(Side.CLIENT)
         private double getFrameRotation(EntityItemFrame var1) {
            return (double)MathHelper.clampAngle(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
         }

         @SideOnly(Side.CLIENT)
         private double getSpawnToAngle(World var1, Entity var2) {
            BlockPos blockpos = p_185092_1_.getSpawnPoint();
            return Math.atan2((double)blockpos.getZ() - p_185092_2_.posZ, (double)blockpos.getX() - p_185092_2_.posX);
         }
      });
   }
}
