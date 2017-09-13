package net.minecraft.entity.ai;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAITasks {
   private static final Logger LOGGER = LogManager.getLogger();
   public final Set taskEntries = Sets.newLinkedHashSet();
   private final Set executingTaskEntries = Sets.newLinkedHashSet();
   private final Profiler theProfiler;
   private int tickCount;
   private int tickRate = 3;
   private int disabledControlFlags;

   public EntityAITasks(Profiler var1) {
      this.theProfiler = var1;
   }

   public void addTask(int var1, EntityAIBase var2) {
      this.taskEntries.add(new EntityAITasks.EntityAITaskEntry(var1, var2));
   }

   public void removeTask(EntityAIBase var1) {
      Iterator var2 = this.taskEntries.iterator();

      while(var2.hasNext()) {
         EntityAITasks.EntityAITaskEntry var3 = (EntityAITasks.EntityAITaskEntry)var2.next();
         EntityAIBase var4 = var3.action;
         if (var4 == var1) {
            if (var3.using) {
               var3.using = false;
               var3.action.resetTask();
               this.executingTaskEntries.remove(var3);
            }

            var2.remove();
            return;
         }
      }

   }

   public void onUpdateTasks() {
      this.theProfiler.startSection("goalSetup");
      if (this.tickCount++ % this.tickRate == 0) {
         for(EntityAITasks.EntityAITaskEntry var2 : this.taskEntries) {
            if (var2.using) {
               if (!this.canUse(var2) || !this.canContinue(var2)) {
                  var2.using = false;
                  var2.action.resetTask();
                  this.executingTaskEntries.remove(var2);
               }
            } else if (this.canUse(var2) && var2.action.shouldExecute()) {
               var2.using = true;
               var2.action.startExecuting();
               this.executingTaskEntries.add(var2);
            }
         }
      } else {
         Iterator var3 = this.executingTaskEntries.iterator();

         while(var3.hasNext()) {
            EntityAITasks.EntityAITaskEntry var5 = (EntityAITasks.EntityAITaskEntry)var3.next();
            if (!this.canContinue(var5)) {
               var5.using = false;
               var5.action.resetTask();
               var3.remove();
            }
         }
      }

      this.theProfiler.endSection();
      if (!this.executingTaskEntries.isEmpty()) {
         this.theProfiler.startSection("goalTick");

         for(EntityAITasks.EntityAITaskEntry var6 : this.executingTaskEntries) {
            var6.action.updateTask();
         }

         this.theProfiler.endSection();
      }

   }

   private boolean canContinue(EntityAITasks.EntityAITaskEntry var1) {
      return var1.action.continueExecuting();
   }

   private boolean canUse(EntityAITasks.EntityAITaskEntry var1) {
      if (this.executingTaskEntries.isEmpty()) {
         return true;
      } else if (this.isControlFlagDisabled(var1.action.getMutexBits())) {
         return false;
      } else {
         for(EntityAITasks.EntityAITaskEntry var3 : this.executingTaskEntries) {
            if (var3 != var1) {
               if (var1.priority >= var3.priority) {
                  if (!this.areTasksCompatible(var1, var3)) {
                     return false;
                  }
               } else if (!var3.action.isInterruptible()) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry var1, EntityAITasks.EntityAITaskEntry var2) {
      return (var1.action.getMutexBits() & var2.action.getMutexBits()) == 0;
   }

   public boolean isControlFlagDisabled(int var1) {
      return (this.disabledControlFlags & var1) > 0;
   }

   public void disableControlFlag(int var1) {
      this.disabledControlFlags |= var1;
   }

   public void enableControlFlag(int var1) {
      this.disabledControlFlags &= ~var1;
   }

   public void setControlFlag(int var1, boolean var2) {
      if (var2) {
         this.enableControlFlag(var1);
      } else {
         this.disableControlFlag(var1);
      }

   }

   public class EntityAITaskEntry {
      public final EntityAIBase action;
      public final int priority;
      public boolean using;

      public EntityAITaskEntry(int var2, EntityAIBase var3) {
         this.priority = var2;
         this.action = var3;
      }

      public boolean equals(@Nullable Object var1) {
         return this == var1 ? true : (var1 != null && this.getClass() == var1.getClass() ? this.action.equals(((EntityAITasks.EntityAITaskEntry)var1).action) : false);
      }

      public int hashCode() {
         return this.action.hashCode();
      }
   }
}
