package net.minecraft.network;

import net.minecraft.util.IThreadListener;

public class PacketThreadUtil {
   public static void checkThreadAndEnqueue(final Packet var0, final INetHandler var1, IThreadListener var2) throws ThreadQuickExitException {
      if (!var2.isCallingFromMinecraftThread()) {
         var2.addScheduledTask(new Runnable() {
            public void run() {
               var0.processPacket(var1);
            }
         });
         throw ThreadQuickExitException.INSTANCE;
      }
   }
}
