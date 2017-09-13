package net.minecraft.entity.monster;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IAnimals;

public interface IMob extends IAnimals {
   Predicate MOB_SELECTOR = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof IMob;
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
   Predicate VISIBLE_MOB_SELECTOR = new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof IMob && !var1.isInvisible();
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((Entity)var1);
      }
   };
}
