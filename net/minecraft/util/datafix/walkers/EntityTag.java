package net.minecraft.util.datafix.walkers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTag implements IDataWalker {
   private static final Logger LOGGER = LogManager.getLogger();

   public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
      NBTTagCompound var4 = var2.getCompoundTag("tag");
      if (var4.hasKey("EntityTag", 10)) {
         NBTTagCompound var5 = var4.getCompoundTag("EntityTag");
         String var6 = var2.getString("id");
         String var7;
         if ("minecraft:armor_stand".equals(var6)) {
            var7 = "ArmorStand";
         } else {
            if (!"minecraft:spawn_egg".equals(var6)) {
               return var2;
            }

            var7 = var5.getString("id");
         }

         boolean var8;
         if (var7 == null) {
            LOGGER.warn("Unable to resolve Entity for ItemInstance: {}", new Object[]{var6});
            var8 = false;
         } else {
            var8 = !var5.hasKey("id", 8);
            var5.setString("id", var7);
         }

         var1.process(FixTypes.ENTITY, var5, var3);
         if (var8) {
            var5.removeTag("id");
         }
      }

      return var2;
   }
}
