package net.minecraft.src;

import net.minecraft.crash.ICrashReportDetail;

class MinecraftServer$4 implements ICrashReportDetail {
   // $FF: synthetic field
   final MinecraftServer a;

   MinecraftServer$4(MinecraftServer var1) {
      this.a = var1;
   }

   public String a() {
      return MinecraftServer.a(this.a).getCurrentPlayerCount() + " / " + MinecraftServer.a(this.a).getMaxPlayers() + "; " + MinecraftServer.a(this.a).getPlayers();
   }

   // $FF: synthetic method
   public Object call() throws Exception {
      return this.a();
   }
}
