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
      return this.getCooldown(itemIn, 0.0F) > 0.0F;
   }

   public float getCooldown(Item var1, float var2) {
      CooldownTracker.Cooldown cooldowntracker$cooldown = (CooldownTracker.Cooldown)this.cooldowns.get(itemIn);
      if (cooldowntracker$cooldown != null) {
         float f = (float)(cooldowntracker$cooldown.expireTicks - cooldowntracker$cooldown.createTicks);
         float f1 = (float)cooldowntracker$cooldown.expireTicks - ((float)this.ticks + partialTicks);
         return MathHelper.clamp(f1 / f, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      ++this.ticks;
      if (!this.cooldowns.isEmpty()) {
         Iterator iterator = this.cooldowns.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            if (((CooldownTracker.Cooldown)entry.getValue()).expireTicks <= this.ticks) {
               iterator.remove();
               this.notifyOnRemove((Item)entry.getKey());
            }
         }
      }

   }

   public void setCooldown(Item var1, int var2) {
      this.cooldowns.put(itemIn, new CooldownTracker.Cooldown(this.ticks, this.ticks + ticksIn));
      this.notifyOnSet(itemIn, ticksIn);
   }

   @SideOnly(Side.CLIENT)
   public void removeCooldown(Item var1) {
      this.cooldowns.remove(itemIn);
      this.notifyOnRemove(itemIn);
   }

   protected void notifyOnSet(Item var1, int var2) {
   }

   protected void notifyOnRemove(Item var1) {
   }

   class Cooldown {
      final int createTicks;
      final int expireTicks;

      private Cooldown(int var2, int var3) {
         this.createTicks = createTicksIn;
         this.expireTicks = expireTicksIn;
      }
   }
}
