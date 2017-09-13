package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;

public class CookedFishIDTypo implements IFixableData {
   private static final ResourceLocation WRONG = new ResourceLocation("cooked_fished");

   public int getFixVersion() {
      return 502;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if (var1.hasKey("id", 8) && WRONG.equals(new ResourceLocation(var1.getString("id")))) {
         var1.setString("id", "minecraft:cooked_fish");
      }

      return var1;
   }
}
