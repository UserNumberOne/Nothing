package net.minecraft.server.gui;

import java.util.Vector;
import javax.swing.JList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class PlayerListComponent extends JList implements ITickable {
   private final MinecraftServer server;
   private int ticks;

   public PlayerListComponent(MinecraftServer var1) {
      this.server = var1;
      var1.registerTickable(this);
   }

   public void update() {
      if (this.ticks++ % 20 == 0) {
         Vector var1 = new Vector();

         for(int var2 = 0; var2 < this.server.getPlayerList().getPlayers().size(); ++var2) {
            var1.add(((EntityPlayerMP)this.server.getPlayerList().getPlayers().get(var2)).getName());
         }

         this.setListData(var1);
      }

   }
}
