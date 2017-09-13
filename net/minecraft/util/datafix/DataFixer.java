package net.minecraft.util.datafix;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataFixer implements IDataFixer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map walkerMap = Maps.newHashMap();
   private final Map fixMap = Maps.newHashMap();
   private final int version;

   public DataFixer(int var1) {
      this.version = var1;
   }

   public NBTTagCompound process(IFixType var1, NBTTagCompound var2) {
      int var3 = var2.hasKey("DataVersion", 99) ? var2.getInteger("DataVersion") : -1;
      return var3 >= 512 ? var2 : this.process(var1, var2, var3);
   }

   public NBTTagCompound process(IFixType var1, NBTTagCompound var2, int var3) {
      if (var3 < this.version) {
         var2 = this.processFixes(var1, var2, var3);
         var2 = this.processWalkers(var1, var2, var3);
      }

      return var2;
   }

   private NBTTagCompound processFixes(IFixType var1, NBTTagCompound var2, int var3) {
      List var4 = (List)this.fixMap.get(var1);
      if (var4 != null) {
         for(int var5 = 0; var5 < var4.size(); ++var5) {
            IFixableData var6 = (IFixableData)var4.get(var5);
            if (var6.getFixVersion() > var3) {
               var2 = var6.fixTagCompound(var2);
            }
         }
      }

      return var2;
   }

   private NBTTagCompound processWalkers(IFixType var1, NBTTagCompound var2, int var3) {
      List var4 = (List)this.walkerMap.get(var1);
      if (var4 != null) {
         for(int var5 = 0; var5 < var4.size(); ++var5) {
            var2 = ((IDataWalker)var4.get(var5)).process(this, var2, var3);
         }
      }

      return var2;
   }

   public void registerWalker(FixTypes var1, IDataWalker var2) {
      this.registerWalkerAdd(var1, var2);
   }

   public void registerWalkerAdd(IFixType var1, IDataWalker var2) {
      this.getTypeList(this.walkerMap, var1).add(var2);
   }

   public void registerFix(IFixType var1, IFixableData var2) {
      List var3 = this.getTypeList(this.fixMap, var1);
      int var4 = var2.getFixVersion();
      if (var4 > this.version) {
         LOGGER.warn("Ignored fix registered for version: {} as the DataVersion of the game is: {}", new Object[]{var4, this.version});
      } else {
         if (!var3.isEmpty() && ((IFixableData)Util.getLastElement(var3)).getFixVersion() > var4) {
            for(int var5 = 0; var5 < var3.size(); ++var5) {
               if (((IFixableData)var3.get(var5)).getFixVersion() > var4) {
                  var3.add(var5, var2);
                  break;
               }
            }
         } else {
            var3.add(var2);
         }

      }
   }

   private List getTypeList(Map var1, IFixType var2) {
      Object var3 = (List)var1.get(var2);
      if (var3 == null) {
         var3 = Lists.newArrayList();
         var1.put(var2, var3);
      }

      return (List)var3;
   }
}
