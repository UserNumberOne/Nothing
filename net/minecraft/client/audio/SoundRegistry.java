package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundRegistry extends RegistrySimple {
   private Map soundRegistry;

   protected Map createUnderlyingMap() {
      this.soundRegistry = Maps.newHashMap();
      return this.soundRegistry;
   }

   public void add(SoundEventAccessor var1) {
      this.putObject(accessor.getLocation(), accessor);
   }

   public void clearMap() {
      this.soundRegistry.clear();
   }
}
