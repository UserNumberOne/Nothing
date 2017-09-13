package net.minecraft.util.datafix.fixes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.datafix.IFixableData;

public class PaintingDirection implements IFixableData {
   public int getFixVersion() {
      return 111;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      String var2 = var1.getString("id");
      boolean var3 = "Painting".equals(var2);
      boolean var4 = "ItemFrame".equals(var2);
      if ((var3 || var4) && !var1.hasKey("Facing", 99)) {
         EnumFacing var5;
         if (var1.hasKey("Direction", 99)) {
            var5 = EnumFacing.getHorizontal(var1.getByte("Direction"));
            var1.setInteger("TileX", var1.getInteger("TileX") + var5.getFrontOffsetX());
            var1.setInteger("TileY", var1.getInteger("TileY") + var5.getFrontOffsetY());
            var1.setInteger("TileZ", var1.getInteger("TileZ") + var5.getFrontOffsetZ());
            var1.removeTag("Direction");
            if (var4 && var1.hasKey("ItemRotation", 99)) {
               var1.setByte("ItemRotation", (byte)(var1.getByte("ItemRotation") * 2));
            }
         } else {
            var5 = EnumFacing.getHorizontal(var1.getByte("Dir"));
            var1.removeTag("Dir");
         }

         var1.setByte("Facing", (byte)var5.getHorizontalIndex());
      }

      return var1;
   }
}
