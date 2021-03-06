package net.minecraft.client.multiplayer;

import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ServerAddress {
   private final String ipAddress;
   private final int serverPort;

   private ServerAddress(String var1, int var2) {
      this.ipAddress = var1;
      this.serverPort = var2;
   }

   public String getIP() {
      try {
         return IDN.toASCII(this.ipAddress);
      } catch (IllegalArgumentException var2) {
         return "";
      }
   }

   public int getPort() {
      return this.serverPort;
   }

   public static ServerAddress fromString(String var0) {
      if (var0 == null) {
         return null;
      } else {
         String[] var1 = var0.split(":");
         if (var0.startsWith("[")) {
            int var2 = var0.indexOf("]");
            if (var2 > 0) {
               String var3 = var0.substring(1, var2);
               String var4 = var0.substring(var2 + 1).trim();
               if (var4.startsWith(":") && !var4.isEmpty()) {
                  var4 = var4.substring(1);
                  var1 = new String[]{var3, var4};
               } else {
                  var1 = new String[]{var3};
               }
            }
         }

         if (var1.length > 2) {
            var1 = new String[]{var0};
         }

         String var5 = var1[0];
         int var6 = var1.length > 1 ? getInt(var1[1], 25565) : 25565;
         if (var6 == 25565) {
            String[] var8 = getServerAddress(var5);
            var5 = var8[0];
            var6 = getInt(var8[1], 25565);
         }

         return new ServerAddress(var5, var6);
      }
   }

   private static String[] getServerAddress(String var0) {
      try {
         String var1 = "com.sun.jndi.dns.DnsContextFactory";
         Class.forName("com.sun.jndi.dns.DnsContextFactory");
         Hashtable var2 = new Hashtable();
         var2.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
         var2.put("java.naming.provider.url", "dns:");
         var2.put("com.sun.jndi.dns.timeout.retries", "1");
         InitialDirContext var3 = new InitialDirContext(var2);
         Attributes var4 = var3.getAttributes("_minecraft._tcp." + var0, new String[]{"SRV"});
         String[] var5 = var4.get("srv").get().toString().split(" ", 4);
         return new String[]{var5[3], var5[2]};
      } catch (Throwable var6) {
         return new String[]{var0, Integer.toString(25565)};
      }
   }

   private static int getInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0.trim());
      } catch (Exception var3) {
         return var1;
      }
   }
}
