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
      this.version = versionIn;
   }

   public NBTTagCompound process(IFixType var1, NBTTagCompound var2) {
      int i = compound.hasKey("DataVersion", 99) ? compound.getInteger("DataVersion") : -1;
      return i >= 512 ? compound : this.process(type, compound, i);
   }

   public NBTTagCompound process(IFixType var1, NBTTagCompound var2, int var3) {
      if (versionIn < this.version) {
         compound = this.processFixes(type, compound, versionIn);
         compound = this.processWalkers(type, compound, versionIn);
      }

      return compound;
   }

   private NBTTagCompound processFixes(IFixType var1, NBTTagCompound var2, int var3) {
      List list = (List)this.fixMap.get(type);
      if (list != null) {
         for(int i = 0; i < list.size(); ++i) {
            IFixableData ifixabledata = (IFixableData)list.get(i);
            if (ifixabledata.getFixVersion() > versionIn) {
               compound = ifixabledata.fixTagCompound(compound);
            }
         }
      }

      return compound;
   }

   private NBTTagCompound processWalkers(IFixType var1, NBTTagCompound var2, int var3) {
      List list = (List)this.walkerMap.get(type);
      if (list != null) {
         for(int i = 0; i < list.size(); ++i) {
            compound = ((IDataWalker)list.get(i)).process(this, compound, versionIn);
         }
      }

      return compound;
   }

   public void registerWalker(FixTypes var1, IDataWalker var2) {
      this.registerWalkerAdd(type, walker);
   }

   public void registerWalkerAdd(IFixType var1, IDataWalker var2) {
      this.getTypeList(this.walkerMap, type).add(walker);
   }

   public void registerFix(IFixType var1, IFixableData var2) {
      List list = this.getTypeList(this.fixMap, type);
      int i = fixable.getFixVersion();
      if (i > this.version) {
         LOGGER.warn("Ignored fix registered for version: {} as the DataVersion of the game is: {}", new Object[]{i, this.version});
      } else if (!list.isEmpty() && ((IFixableData)Util.getLastElement(list)).getFixVersion() > i) {
         for(int j = 0; j < list.size(); ++j) {
            if (((IFixableData)list.get(j)).getFixVersion() > i) {
               list.add(j, fixable);
               break;
            }
         }
      } else {
         list.add(fixable);
      }

   }

   private List getTypeList(Map var1, IFixType var2) {
      List list = (List)map.get(type);
      if (list == null) {
         list = Lists.newArrayList();
         map.put(type, list);
      }

      return list;
   }
}
