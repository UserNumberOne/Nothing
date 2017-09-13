package net.minecraft.network.datasync;

import net.minecraft.network.PacketBuffer;

public interface DataSerializer {
   void write(PacketBuffer var1, Object var2);

   Object read(PacketBuffer var1);

   DataParameter createKey(int var1);
}
