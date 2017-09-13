package net.minecraft.realms;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Session;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Realms {
   public static boolean isTouchScreen() {
      return Minecraft.getMinecraft().gameSettings.touchscreen;
   }

   public static Proxy getProxy() {
      return Minecraft.getMinecraft().getProxy();
   }

   public static String sessionId() {
      Session var0 = Minecraft.getMinecraft().getSession();
      return var0 == null ? null : var0.getSessionID();
   }

   public static String userName() {
      Session var0 = Minecraft.getMinecraft().getSession();
      return var0 == null ? null : var0.getUsername();
   }

   public static long currentTimeMillis() {
      return Minecraft.getSystemTime();
   }

   public static String getSessionId() {
      return Minecraft.getMinecraft().getSession().getSessionID();
   }

   public static String getUUID() {
      return Minecraft.getMinecraft().getSession().getPlayerID();
   }

   public static String getName() {
      return Minecraft.getMinecraft().getSession().getUsername();
   }

   public static String uuidToName(String var0) {
      return Minecraft.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(var0), (String)null), false).getName();
   }

   public static void setScreen(RealmsScreen var0) {
      Minecraft.getMinecraft().displayGuiScreen(var0.getProxy());
   }

   public static String getGameDirectoryPath() {
      return Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
   }

   public static int survivalId() {
      return GameType.SURVIVAL.getID();
   }

   public static int creativeId() {
      return GameType.CREATIVE.getID();
   }

   public static int adventureId() {
      return GameType.ADVENTURE.getID();
   }

   public static int spectatorId() {
      return GameType.SPECTATOR.getID();
   }

   public static void setConnectedToRealms(boolean var0) {
      Minecraft.getMinecraft().setConnectedToRealms(var0);
   }

   public static ListenableFuture downloadResourcePack(String var0, String var1) {
      return Minecraft.getMinecraft().getResourcePackRepository().downloadResourcePack(var0, var1);
   }

   public static void clearResourcePack() {
      Minecraft.getMinecraft().getResourcePackRepository().clearResourcePack();
   }

   public static boolean getRealmsNotificationsEnabled() {
      return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS);
   }

   public static boolean inTitleScreen() {
      return Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu;
   }

   public static void deletePlayerTag(File var0) {
      if (var0.exists()) {
         try {
            NBTTagCompound var1 = CompressedStreamTools.readCompressed(new FileInputStream(var0));
            NBTTagCompound var2 = var1.getCompoundTag("Data");
            var2.removeTag("Player");
            CompressedStreamTools.writeCompressed(var1, new FileOutputStream(var0));
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }

   }
}
