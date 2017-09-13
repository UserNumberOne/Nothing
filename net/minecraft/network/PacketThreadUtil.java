package net.minecraft.network;

import net.minecraft.util.IThreadListener;

public class PacketThreadUtil {
   public static void checkThreadAndEnqueue(final Packet var0, final INetHandler var1, IThreadListener var2) throws ThreadQuickExitException {
      if (!scheduler.isCallingFromMinecraftThread()) {
         scheduler.addScheduledTask(new Runnable() {
            public void run() {
               packetIn.processPacket(processor);
            }
         });
         throw ThreadQuickExitException.INSTANCE;
      }
   }
}
