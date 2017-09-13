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
            if (var3 == null && !var1.isOnItemFrame()) {
               return 0.0F;
            } else {
               boolean var4 = var3 != null;
               Object var5 = var4 ? var3 : var1.getItemFrame();
               if (var2 == null) {
                  var2 = ((Entity)var5).world;
               }

               double var6;
               if (var2.provider.isSurfaceWorld()) {
                  double var8 = var4 ? (double)((Entity)var5).rotationYaw : this.getFrameRotation((EntityItemFrame)var5);
                  var8 = var8 % 360.0D;
                  double var10 = this.getSpawnToAngle(var2, (Entity)var5);
                  var6 = 3.141592653589793D - ((var8 - 90.0D) * 0.01745329238474369D - var10);
               } else {
                  var6 = Math.random() * 6.283185307179586D;
               }

               if (var4) {
                  var6 = this.wobble(var2, var6);
               }

               float var13 = (float)(var6 / 6.283185307179586D);
               return MathHelper.positiveModulo(var13, 1.0F);
            }
         }

         @SideOnly(Side.CLIENT)
         private double wobble(World var1, double var2) {
            if (var1.getTotalWorldTime() != this.lastUpdateTick) {
               this.lastUpdateTick = var1.getTotalWorldTime();
               double var4 = var2 - this.rotation;
               var4 = var4 % 6.283185307179586D;
               var4 = MathHelper.clamp(var4, -1.0D, 1.0D);
               this.rota += var4 * 0.1D;
               this.rota *= 0.8D;
               this.rotation += this.rota;
            }

            return this.rotation;
         }

         @SideOnly(Side.CLIENT)
         private double getFrameRotation(EntityItemFrame var1) {
            return (double)MathHelper.clampAngle(180 + var1.facingDirection.getHorizontalIndex() * 90);
         }

         @SideOnly(Side.CLIENT)
         private double getSpawnToAngle(World var1, Entity var2) {
            BlockPos var3 = var1.getSpawnPoint();
            return Math.atan2((double)var3.getZ() - var2.posZ, (double)var3.getX() - var2.posX);
         }
      });
   }
}
