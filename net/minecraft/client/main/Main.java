package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Main {
   public static void main(String[] var0) {
      OptionParser var1 = new OptionParser();
      var1.allowsUnrecognizedOptions();
      var1.accepts("demo");
      var1.accepts("fullscreen");
      var1.accepts("checkGlErrors");
      ArgumentAcceptingOptionSpec var2 = var1.accepts("server").withRequiredArg();
      ArgumentAcceptingOptionSpec var3 = var1.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(25565), new Integer[0]);
      ArgumentAcceptingOptionSpec var4 = var1.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), new File[0]);
      ArgumentAcceptingOptionSpec var5 = var1.accepts("assetsDir").withRequiredArg().ofType(File.class);
      ArgumentAcceptingOptionSpec var6 = var1.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
      ArgumentAcceptingOptionSpec var7 = var1.accepts("proxyHost").withRequiredArg();
      ArgumentAcceptingOptionSpec var8 = var1.accepts("proxyPort").withRequiredArg().defaultsTo("8080", new String[0]).ofType(Integer.class);
      ArgumentAcceptingOptionSpec var9 = var1.accepts("proxyUser").withRequiredArg();
      ArgumentAcceptingOptionSpec var10 = var1.accepts("proxyPass").withRequiredArg();
      ArgumentAcceptingOptionSpec var11 = var1.accepts("username").withRequiredArg().defaultsTo("Player" + Minecraft.getSystemTime() % 1000L, new String[0]);
      ArgumentAcceptingOptionSpec var12 = var1.accepts("uuid").withRequiredArg();
      ArgumentAcceptingOptionSpec var13 = var1.accepts("accessToken").withRequiredArg().required();
      ArgumentAcceptingOptionSpec var14 = var1.accepts("version").withRequiredArg().required();
      ArgumentAcceptingOptionSpec var15 = var1.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(854), new Integer[0]);
      ArgumentAcceptingOptionSpec var16 = var1.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(Integer.valueOf(480), new Integer[0]);
      ArgumentAcceptingOptionSpec var17 = var1.accepts("userProperties").withRequiredArg().defaultsTo("{}", new String[0]);
      ArgumentAcceptingOptionSpec var18 = var1.accepts("profileProperties").withRequiredArg().defaultsTo("{}", new String[0]);
      ArgumentAcceptingOptionSpec var19 = var1.accepts("assetIndex").withRequiredArg();
      ArgumentAcceptingOptionSpec var20 = var1.accepts("userType").withRequiredArg().defaultsTo("legacy", new String[0]);
      ArgumentAcceptingOptionSpec var21 = var1.accepts("versionType").withRequiredArg().defaultsTo("release", new String[0]);
      NonOptionArgumentSpec var22 = var1.nonOptions();
      OptionSet var23 = var1.parse(var0);
      List var24 = var23.valuesOf(var22);
      if (!var24.isEmpty()) {
         System.out.println("Completely ignored arguments: " + var24);
      }

      String var25 = (String)var23.valueOf(var7);
      Proxy var26 = Proxy.NO_PROXY;
      if (var25 != null) {
         try {
            var26 = new Proxy(Type.SOCKS, new InetSocketAddress(var25, ((Integer)var23.valueOf(var8)).intValue()));
         } catch (Exception var48) {
            ;
         }
      }

      final String var27 = (String)var23.valueOf(var9);
      final String var28 = (String)var23.valueOf(var10);
      if (!var26.equals(Proxy.NO_PROXY) && isNullOrEmpty(var27) && isNullOrEmpty(var28)) {
         Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(var27, var28.toCharArray());
            }
         });
      }

      int var29 = ((Integer)var23.valueOf(var15)).intValue();
      int var30 = ((Integer)var23.valueOf(var16)).intValue();
      boolean var31 = var23.has("fullscreen");
      boolean var32 = var23.has("checkGlErrors");
      boolean var33 = var23.has("demo");
      String var34 = (String)var23.valueOf(var14);
      Gson var35 = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new Serializer()).create();
      PropertyMap var36 = (PropertyMap)JsonUtils.gsonDeserialize(var35, (String)var23.valueOf(var17), PropertyMap.class);
      PropertyMap var37 = (PropertyMap)JsonUtils.gsonDeserialize(var35, (String)var23.valueOf(var18), PropertyMap.class);
      String var38 = (String)var23.valueOf(var21);
      File var39 = (File)var23.valueOf(var4);
      File var40 = var23.has(var5) ? (File)var23.valueOf(var5) : new File(var39, "assets/");
      File var41 = var23.has(var6) ? (File)var23.valueOf(var6) : new File(var39, "resourcepacks/");
      String var42 = var23.has(var12) ? (String)var12.value(var23) : (String)var11.value(var23);
      String var43 = var23.has(var19) ? (String)var19.value(var23) : null;
      String var44 = (String)var23.valueOf(var2);
      Integer var45 = (Integer)var23.valueOf(var3);
      Session var46 = new Session((String)var11.value(var23), var42, (String)var13.value(var23), (String)var20.value(var23));
      GameConfiguration var47 = new GameConfiguration(new GameConfiguration.UserInformation(var46, var36, var37, var26), new GameConfiguration.DisplayInformation(var29, var30, var31, var32), new GameConfiguration.FolderInformation(var39, var41, var40, var43), new GameConfiguration.GameInformation(var33, var34, var38), new GameConfiguration.ServerInformation(var44, var45.intValue()));
      Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
         public void run() {
            Minecraft.stopIntegratedServer();
         }
      });
      Thread.currentThread().setName("Client thread");
      (new Minecraft(var47)).run();
   }

   private static boolean isNullOrEmpty(String var0) {
      return var0 != null && !var0.isEmpty();
   }
}
