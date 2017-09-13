package net.minecraft.util.registry;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRegistry extends Iterable {
   @Nullable
   @SideOnly(Side.CLIENT)
   Object getObject(Object var1);

   void putObject(Object var1, Object var2);

   @SideOnly(Side.CLIENT)
   Set getKeys();
}
