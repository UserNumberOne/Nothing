package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSleepMP extends GuiChat {
   public void initGui() {
      super.initGui();
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 40, I18n.format("multiplayer.stopSleeping")));
   }

   protected void keyTyped(char var1, int var2) throws IOException {
      if (var2 == 1) {
         this.wakeFromSleep();
      } else if (var2 != 28 && var2 != 156) {
         super.keyTyped(var1, var2);
      } else {
         String var3 = this.inputField.getText().trim();
         if (!var3.isEmpty()) {
            this.sendChatMessage(var3);
         }

         this.inputField.setText("");
         this.mc.ingameGUI.getChatGUI().resetScroll();
      }

   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == 1) {
         this.wakeFromSleep();
      } else {
         super.actionPerformed(var1);
      }

   }

   private void wakeFromSleep() {
      NetHandlerPlayClient var1 = this.mc.player.connection;
      var1.sendPacket(new CPacketEntityAction(this.mc.player, CPacketEntityAction.Action.STOP_SLEEPING));
   }
}
