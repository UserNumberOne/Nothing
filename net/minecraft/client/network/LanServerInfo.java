package net.minecraft.client.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LanServerInfo {
   private final String lanServerMotd;
   private final String lanServerIpPort;
   private long timeLastSeen;

   public LanServerInfo(String var1, String var2) {
      this.lanServerMotd = p_i47130_1_;
      this.lanServerIpPort = p_i47130_2_;
      this.timeLastSeen = Minecraft.getSystemTime();
   }

   public String getServerMotd() {
      return this.lanServerMotd;
   }

   public String getServerIpPort() {
      return this.lanServerIpPort;
   }

   public void updateLastSeen() {
      this.timeLastSeen = Minecraft.getSystemTime();
   }
}
