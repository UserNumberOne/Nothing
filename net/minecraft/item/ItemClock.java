package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemClock extends Item {
   public ItemClock() {
      this.addPropertyOverride(new ResourceLocation("time"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         double rotation;
         @SideOnly(Side.CLIENT)
         double rota;
         @SideOnly(Side.CLIENT)
         long lastUpdateTick;

         @SideOnly(Side.CLIENT)
         public float apply(ItemStack var1, @Nullable World var2, @Nullable EntityLivingBase var3) {
            boolean var4 = var3 != null;
            Object var5 = var4 ? var3 : var1.getItemFrame();
            if (var2 == null && var5 != null) {
               var2 = ((Entity)var5).world;
            }

            if (var2 == null) {
               return 0.0F;
            } else {
               double var6;
               if (var2.provider.isSurfaceWorld()) {
                  var6 = (double)var2.getCelestialAngle(1.0F);
               } else {
                  var6 = Math.random();
               }

               var6 = this.wobble(var2, var6);
               return MathHelper.positiveModulo((float)var6, 1.0F);
            }
         }

         @SideOnly(Side.CLIENT)
         private double wobble(World var1, double var2) {
            if (var1.getTotalWorldTime() != this.lastUpdateTick) {
               this.lastUpdateTick = var1.getTotalWorldTime();
               double var4 = var2 - this.rotation;
               if (var4 < -0.5D) {
                  ++var4;
               }

               this.rota += var4 * 0.1D;
               this.rota *= 0.9D;
               this.rotation += this.rota;
            }

            return this.rotation;
         }
      });
   }
}
