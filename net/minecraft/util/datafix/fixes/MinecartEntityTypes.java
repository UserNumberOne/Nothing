package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class MinecartEntityTypes implements IFixableData {
   private static final List MINECART_TYPE_LIST = Lists.newArrayList(new String[]{"MinecartRideable", "MinecartChest", "MinecartFurnace", "MinecartTNT", "MinecartSpawner", "MinecartHopper", "MinecartCommandBlock"});

   public int getFixVersion() {
      return 106;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("Minecart".equals(var1.getString("id"))) {
         String var2 = "MinecartRideable";
         int var3 = var1.getInteger("Type");
         if (var3 > 0 && var3 < MINECART_TYPE_LIST.size()) {
            var2 = (String)MINECART_TYPE_LIST.get(var3);
         }

         var1.setString("id", var2);
         var1.removeTag("Type");
      }

      return var1;
   }
}
