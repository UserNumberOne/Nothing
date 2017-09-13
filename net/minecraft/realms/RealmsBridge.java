package net.minecraft.realms;

import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class RealmsBridge extends RealmsScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private GuiScreen previousScreen;

   public void switchToRealms(GuiScreen var1) {
      this.previousScreen = var1;

      try {
         Class var2 = Class.forName("com.mojang.realmsclient.RealmsMainScreen");
         Constructor var3 = var2.getDeclaredConstructor(RealmsScreen.class);
         var3.setAccessible(true);
         Object var4 = var3.newInstance(this);
         Minecraft.getMinecraft().displayGuiScreen(((RealmsScreen)var4).getProxy());
      } catch (ClassNotFoundException var5) {
         LOGGER.error("Realms module missing");
      } catch (Exception var6) {
         LOGGER.error("Failed to load Realms module", var6);
      }

   }

   public GuiScreenRealmsProxy getNotificationScreen(GuiScreen var1) {
      try {
         this.previousScreen = var1;
         Class var2 = Class.forName("com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen");
         Constructor var3 = var2.getDeclaredConstructor(RealmsScreen.class);
         var3.setAccessible(true);
         Object var4 = var3.newInstance(this);
         return ((RealmsScreen)var4).getProxy();
      } catch (ClassNotFoundException var5) {
         LOGGER.error("Realms module missing");
      } catch (Exception var6) {
         LOGGER.error("Failed to load Realms module", var6);
      }

      return null;
   }

   public void init() {
      Minecraft.getMinecraft().displayGuiScreen(this.previousScreen);
   }
}
