package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CooldownTracker {
   private final Map cooldowns = Maps.newHashMap();
   private int ticks;

   public boolean hasCooldown(Item var1) {
      return this.getCooldown(var1, 0.0F) > 0.0F;
   }

   public float getCooldown(Item var1, float var2) {
      CooldownTracker.Cooldown var3 = (CooldownTracker.Cooldown)this.cooldowns.get(var1);
      if (var3 != null) {
         float var4 = (float)(var3.expireTicks - var3.createTicks);
         float var5 = (float)var3.expireTicks - ((float)this.ticks + var2);
         return MathHelper.clamp(var5 / var4, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      ++this.ticks;
      if (!this.cooldowns.isEmpty()) {
         Iterator var1 = this.cooldowns.entrySet().iterator();

         while(var1.hasNext()) {
            Entry var2 = (Entry)var1.next();
            if (((CooldownTracker.Cooldown)var2.getValue()).expireTicks <= this.ticks) {
               var1.remove();
               this.notifyOnRemove((Item)var2.getKey());
            }
         }
      }

   }

   public void setCooldown(Item var1, int var2) {
      this.cooldowns.put(var1, new CooldownTracker.Cooldown(this.ticks, this.ticks + var2));
      this.notifyOnSet(var1, var2);
   }

   @SideOnly(Side.CLIENT)
   public void removeCooldown(Item var1) {
      this.cooldowns.remove(var1);
      this.notifyOnRemove(var1);
   }

   protected void notifyOnSet(Item var1, int var2) {
   }

   protected void notifyOnRemove(Item var1) {
   }

   class Cooldown {
      final int createTicks;
      final int expireTicks;

      private Cooldown(int var2, int var3) {
         this.createTicks = var2;
         this.expireTicks = var3;
      }
   }
}
